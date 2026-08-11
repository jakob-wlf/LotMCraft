package de.jakob.lotm.beyonders.abilities.door;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.beyonders.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SealingAbility extends Ability {
    private static HashMap<UUID, Integer> durationMap = new HashMap<>();

    public SealingAbility(String id) {
        super(id, 25, "sealing");
        canBeCopied = false;
        interactionRadius = 5;
        interactionCacheTicks = 20 * 14;
        postsUsedAbilityEventManually = true;
        canBeShared = false;

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(10, 15, 25));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(24000f, 12000f, 7500f));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 2));
    }

    @Override
    public float getSpiritualityCost() {
        return 5000;
    }

    private final DustParticleOptions dustOptions = new DustParticleOptions(new Vector3f(120 / 255f, 208 / 255f, 245 / 255f), 3f);
    private final DustParticleOptions dustOptions2 = new DustParticleOptions(new Vector3f(224 / 255f, 120 / 255f, 245 / 255f), 2.5f);

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide)
            return;

        int radius = 10;

        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, baseDistance, 2);

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent((ServerLevel) level, targetLoc, entity, this, interactionFlags, interactionRadius, interactionCacheTicks));

        List<LivingEntity> sealedEntities = AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, targetLoc, radius, false);
        sealedEntities.forEach(e -> {
            int duration = 0;

            if ((BeyonderData.getPathway(e).equals("door") && AbilityUtil.getSequenceDifference(entitySeq, BeyonderData.getSequence(e)) <= 0)) {
                return;
            } else if (entitySeq > BeyonderData.getSequence(e)) {
                duration = 20;
            } else {
                duration = 20 * 10;
            }

            BeyonderData.addModifierWithTimeLimit(e, "sealed", .6, duration);

            DisabledAbilitiesComponent component = e.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
            component.disableAbilityUsageForTime("sealed", duration, e);

            durationMap.put(e.getUUID(), duration);

            if (!(e instanceof Player) && !BeyonderData.isBeyonder(e) && e instanceof Mob mob) {
                mob.setNoAi(true);
            }
        });

        level.playSound(null, targetLoc.x, targetLoc.y, targetLoc.z, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1f, 1f);
        level.playSound(null, targetLoc.x, targetLoc.y, targetLoc.z, SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 1f, 1f);

        AtomicInteger totalDuration = new AtomicInteger(0);
        final UUID[] taskIdHolder = new UUID[1];

        taskIdHolder[0] = ServerScheduler.scheduleForDuration(0, 4, 20 * 10, () -> {
            Location sealLoc = new Location(targetLoc, level);

            if (InteractionHandler.isInteractionPossible(sealLoc, "explosion", entitySeq) || InteractionHandler.isInteractionPossible(sealLoc, "sealing_malfunction", entitySeq)) {
                ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.END_ROD, targetLoc, 200, 2, .2);
                ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.PORTAL, targetLoc, 200, 2, .2);

                sealedEntities.forEach(e -> {
                    e.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                    if (BeyonderData.isBeyonder(e)) {
                        DisabledAbilitiesComponent comp = e.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
                        comp.enableAbilityUsage("sealed");
                    }
                    if (!(e instanceof Player) && !BeyonderData.isBeyonder(e) && e instanceof Mob mob) {
                        mob.setNoAi(false);
                    }
                });
                if (taskIdHolder[0] != null) ServerScheduler.cancel(taskIdHolder[0]);
                return;
            }

            ParticleUtil.spawnSphereParticles((ServerLevel) level, ParticleTypes.END_ROD, targetLoc, radius, 80);
            ParticleUtil.spawnSphereParticles((ServerLevel) level, dustOptions, targetLoc, radius, 60);
            ParticleUtil.spawnSphereParticles((ServerLevel) level, dustOptions2, targetLoc, radius, 40);

            sealedEntities.forEach(e -> {
                if(!durationMap.containsKey(e.getUUID())) return;

                int duration = durationMap.get(e.getUUID());

                if(totalDuration.get() >= duration) return;

                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 100, false, false, false));
                e.setDeltaMovement(new Vec3(0, 0, 0));
                e.hurtMarked = true;
                ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.STAR.get(), e.getEyePosition().subtract(0, .5, 0), 15, .4, .9, .4, .05);
            });
            totalDuration.set(totalDuration.get() + 1);
        }, () -> {
            sealedEntities.forEach(e -> {
                if (!(e instanceof Player) && !BeyonderData.isBeyonder(e) && e instanceof Mob mob) {
                    mob.setNoAi(false);
                }
            });
        }, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(targetLoc, level)));
    }
}

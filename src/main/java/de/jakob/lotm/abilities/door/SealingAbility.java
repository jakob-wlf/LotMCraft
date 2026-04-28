package de.jakob.lotm.abilities.door;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SealingAbility extends Ability {
    public SealingAbility(String id) {
        super(id, 25, "sealing");
        canBeCopied = false;
        interactionRadius = 5;
        interactionCacheTicks = 20 * 14;
        postsUsedAbilityEventManually = true;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 3));
    }

    @Override
    public float getSpiritualityCost() {
        return 5000;
    }

    private final DustParticleOptions dustOptions = new DustParticleOptions(new Vector3f(120 / 255f, 208 / 255f, 245 / 255f), 3f);
    private final DustParticleOptions dustOptions2 = new DustParticleOptions(new Vector3f(224 / 255f, 120 / 255f, 245 / 255f), 2.5f);

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        int radius = 5*(int) Math.max(multiplier(entity)/4,1);

        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 20*(int) Math.max(multiplier(entity)/4,1), 2);

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent((ServerLevel) level, targetLoc, entity, this, interactionFlags, interactionRadius, interactionCacheTicks));

        List<LivingEntity> sealedEntities = AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, targetLoc, radius, false);
        sealedEntities.forEach(e -> {
            int duration =0;
            if(AbilityUtil.getSequenceDifference(entitySeq, BeyonderData.getSequence(e))+1 < 0) {
                //LOTMCraft.LOGGER.info("Cant seal normal");
                return;
            }
            if((BeyonderData.getPathway(e).equals("door") && AbilityUtil.getSequenceDifference(entitySeq, BeyonderData.getSequence(e)) <= 0)) {
                return;
            }else{
                duration = 20*14;
            };

            BeyonderData.addModifierWithTimeLimit(e, "sealed", .3,duration);
            int seq = AbilityUtil.getSeqWithArt(entity, this);
            if  (seq<=1)
                {
                   if (!(BeyonderData.getSequence(e) ==0)) {
                       DisabledAbilitiesComponent component = e.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
                       component.disableAbilityUsageForTime("sealed", duration, e);
                   };
                };
            if(BeyonderData.isBeyonder(e) && BeyonderData.getSequence(e) > entitySeq) {
                DisabledAbilitiesComponent component = e.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
                component.disableAbilityUsageForTime("sealed", duration, e);
            }
            if(!(e instanceof Player) && !BeyonderData.isBeyonder(e) && e instanceof Mob mob) {
                mob.setNoAi(true);
            }
        });

        level.playSound(null, targetLoc.x, targetLoc.y, targetLoc.z, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1f, 1f);
        level.playSound(null, targetLoc.x, targetLoc.y, targetLoc.z, SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 1f, 1f);

        final UUID[] taskIdHolder = new UUID[1];
        taskIdHolder[0] = ServerScheduler.scheduleForDuration(0, 4, 20 * 14, () -> {
            Location sealLoc = new Location(targetLoc, level);

            if(InteractionHandler.isInteractionPossible(sealLoc, "explosion", entitySeq) || InteractionHandler.isInteractionPossible(sealLoc, "sealing_malfunction", entitySeq)) {
                ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.END_ROD, targetLoc, 200, 2, .2);
                ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.PORTAL, targetLoc, 200, 2, .2);

                sealedEntities.forEach(e -> {
                    e.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                    if(BeyonderData.isBeyonder(e)) {
                        DisabledAbilitiesComponent comp = e.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
                        comp.enableAbilityUsage("sealed");
                    }
                    if(!(e instanceof Player) && !BeyonderData.isBeyonder(e) && e instanceof Mob mob) {
                        mob.setNoAi(false);
                    }
                });
                if(taskIdHolder[0] != null) ServerScheduler.cancel(taskIdHolder[0]);
                return;
            }

            ParticleUtil.spawnSphereParticles((ServerLevel) level, ParticleTypes.END_ROD, targetLoc, radius, 80);
            ParticleUtil.spawnSphereParticles((ServerLevel) level, dustOptions, targetLoc, radius, 60);
            ParticleUtil.spawnSphereParticles((ServerLevel) level, dustOptions2, targetLoc, radius, 40);

            sealedEntities.forEach(e -> {
                int duration=0;
                if(AbilityUtil.getSequenceDifference(entitySeq, BeyonderData.getSequence(e))+1 < 0) {
                    return;
                }
                if((BeyonderData.getPathway(e).equals("door") && AbilityUtil.getSequenceDifference(entitySeq, BeyonderData.getSequence(e)) <= 0)) {
                    return;
                }else{
                    duration = 20*14;
                };
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 100, false, false, false));
                e.setDeltaMovement(new Vec3(0, 0, 0));
                e.hurtMarked = true;
                ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.STAR.get(), e.getEyePosition().subtract(0, .5, 0), 15, .4, .9, .4, .05);
            });
        }, () -> {
            sealedEntities.forEach(e -> {
                if(!(e instanceof Player) && !BeyonderData.isBeyonder(e) && e instanceof Mob mob) {
                    mob.setNoAi(false);
                }
            });
        }, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(targetLoc, level)));
    }
}

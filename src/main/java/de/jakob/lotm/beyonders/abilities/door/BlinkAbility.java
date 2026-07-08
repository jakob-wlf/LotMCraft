package de.jakob.lotm.beyonders.abilities.door;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.CopiedAbilityComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.demoness.CharmAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.custom.ability_entities.door_pathway.BlinkAfterimageEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.TeleportationUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlinkAbility extends SelectableAbility {

    private final HashSet<UUID> performingBlinkBarrage = new HashSet<>();

    public BlinkAbility(String id) {
        super(id, .1f, "blink_escape", "escape");
        interactionRadius = 3;
        interactionCacheTicks = 40;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 5));
    }

    @Override
    public float getSpiritualityCost() {
        return 210;
    }

    private final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(.5f, .78f, .93f),
            1.75f
    );

    private final DustParticleOptions dust2 = new DustParticleOptions(
            new Vector3f(.8f, .34f, .93f),
            1.75f
    );

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.blink.blink", "ability.lotmcraft.blink.barrage"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        switch (selectedAbility) {
            case 0 -> performBlink(level, entity);
            case 1 -> blinkBarrage(level, entity);
        }
    }

    private void blinkBarrage(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        if(performingBlinkBarrage.contains(entity.getUUID())) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 10, 1f);
        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("lotmcraft.no_target").withColor(0x00AAFF));
            return;
        }

        if(BeyonderData.getSpirituality(entity) < 950) return;

        BeyonderData.reduceSpirituality(entity, 950);

        Vec3 originalPos = entity.position();

        performingBlinkBarrage.add(entity.getUUID());
        AtomicBoolean isAtTarget = new AtomicBoolean(false);
        ServerScheduler.scheduleForDuration(0, 2, 20 * 3, () -> {
            Vec3 targetPos = target.position();
            if(!isAtTarget.get()) {
                target.hurt(ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, entity), (float) DamageLookup.lookupDamage(5, .1f) * multiplier(entity) * .45f);
                isAtTarget.set(true);
            }
            else {
                targetPos = target.position().add((random.nextDouble() - .5) * 12, random.nextDouble() * 4, (random.nextDouble() - .5) * 12);
                isAtTarget.set(false);
            }

            level.playSound(null, targetPos.x, targetPos.y, targetPos.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, .5f, 1);

            entity.teleportTo(targetPos.x, targetPos.y, targetPos.z);

            if (BeyonderData.getSequence(entity) <= 5) {
                CharmAbility.removeCharm(entity.getUUID());
            }

            ParticleUtil.spawnParticles((ServerLevel) level, dust, targetPos.add(0, .5, 0), 30, .4, 1, .4, 0);
            ParticleUtil.spawnParticles((ServerLevel) level, dust2, targetPos.add(0, .5, 0), 30, .4, 1, .4, 0);
            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.END_ROD, targetPos.add(0, .5, 0), 30, .4, 1, .4, 0);
        }, () -> {
            entity.teleportTo(originalPos.x, originalPos.y, originalPos.z);
            performingBlinkBarrage.remove(entity.getUUID());
        }, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));
    }

    private void performBlink(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 targetLocBuff = AbilityUtil.getTargetBlock(entity, 8*multiplier(entity), true).getCenter().add(0, 1, 0);
        Vec3 originalPos = entity.position();
        var targetLoc = TeleportationUtil.clampToBorder((ServerLevel) level, targetLocBuff);

        level.playSound(null, targetLoc.x, targetLoc.y, targetLoc.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, .5f, 1);

        entity.teleportTo(targetLoc.x, targetLoc.y, targetLoc.z);

        if (BeyonderData.getSequence(entity) <= 5) {
            CharmAbility.removeCharm(entity.getUUID());
        }

        ParticleUtil.spawnParticles((ServerLevel) level, dust, targetLoc.add(0, .5, 0), 30, .4, 1, .4, 0);
        ParticleUtil.spawnParticles((ServerLevel) level, dust2, targetLoc.add(0, .5, 0), 30, .4, 1, .4, 0);
        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.END_ROD, targetLoc.add(0, .5, 0), 30, .4, 1, .4, 0);

        // Spawn Afterimage if Afterimage ability is active
        if(!((BlinkAfterimageAbility) LOTMCraft.abilityHandler.getById("blink_afterimage_ability")).isActiveForEntity(entity))
            return;

        if(BeyonderData.getCowardWormAmount(entity) <= 0)
            return;

        BeyonderData.incrementWormAmount(entity, -1);

        BlinkAfterimageEntity blinkAfterimage = new BlinkAfterimageEntity(level, originalPos, entity, getAbilityToUse(entity));
        blinkAfterimage.setTarget(getTargetEntity(entity));
        level.addFreshEntity(blinkAfterimage);
        BeyonderData.setBeyonder(blinkAfterimage, BeyonderData.getPathway(entity), BeyonderData.getSequence(entity), true, true, false, true, true, false);
    }

    private Ability getAbilityToUse(LivingEntity entity) {
        CopiedAbilityComponent component = entity.getData(ModAttachments.COPIED_ABILITY_COMPONENT);
        List<Ability> usableAbilities = component.getAbilities()
                                            .stream()
                                            .map(a -> LOTMCraft.abilityHandler.getById(a.abilityId()))
                                            .filter(a -> a != null && a.canBeUsedByNPC)
                                            .toList();

        if(usableAbilities.isEmpty()) {
            usableAbilities = LOTMCraft.abilityHandler.getByPathwayAndSequenceOrderedBySequence(BeyonderData.getPathway(entity), BeyonderData.getSequence(entity))
                    .stream()
                    .filter(a -> a.canBeUsedByNPC)
                    .toList();
        }

        if(!usableAbilities.isEmpty()) {
            return usableAbilities.get(entity.getRandom().nextInt(usableAbilities.size()));
        }

        return null;
    }

    private LivingEntity getTargetEntity(LivingEntity entity) {
        LivingEntity target = entity.getLastHurtMob();
        if(target == null) target = entity.getLastHurtByMob();
        if(target == null) target = AbilityUtil.getTargetEntity(entity, 20, 2);

        return target;
    }
}

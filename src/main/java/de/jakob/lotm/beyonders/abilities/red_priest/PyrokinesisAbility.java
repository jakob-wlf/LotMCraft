package de.jakob.lotm.beyonders.abilities.red_priest;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.tyrant.WindManipulationFlightAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.custom.FireRavenEntity;
import de.jakob.lotm.entity.custom.projectiles.FireballEntity;
import de.jakob.lotm.entity.custom.projectiles.FlamingSpearProjectileEntity;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.AbilitySelectionPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class PyrokinesisAbility extends SelectableAbility {
    private final HashSet<UUID> transformedEntities = new HashSet<>();

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(1.0f, .95f, .95f), 2.0f);

    private PyrokinesisFlightAbility flightSkill;

    public PyrokinesisAbility(String id) {
        super(id, 1.25f, "burning");

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(1, 1, 1, 1, 2, 2, 3, 3));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(2400f, 1000f, 800f, 450f, 390f, 250f, 225f, 208f));

        baseDamage = 13f;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "red_priest", 7
        ));
    }

    @Override
    protected float getSpiritualityCost() {
        return 30;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.pyrokinesis.fireball",
                "ability.lotmcraft.pyrokinesis.flame_wave",
                "ability.lotmcraft.pyrokinesis.wall_of_fire",
                "ability.lotmcraft.pyrokinesis.fire_ravens",
                "ability.lotmcraft.pyrokinesis.flaming_spear",
                "ability.lotmcraft.pyrokinesis.flame_transformation"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        switch(abilityIndex) {
            case 0 -> fireball(level, entity);
            case 1 -> flameWave(level, entity);
            case 2 -> wallOfFire(level, entity);
            case 3 -> fireRavens(level, entity);
            case 4 -> flamingSpear(level, entity);
            case 5 -> flameTransformation(level, entity);
        }
    }

    private void flameTransformation(Level levelAny, LivingEntity entity) {
        if(levelAny.isClientSide || !(levelAny instanceof ServerLevel level)){
            return;
        }

        level.playSound(null, entity.blockPosition(), net.minecraft.sounds.SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 1.0f, 1.0f);

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        if(entitySeq <= 4){
            if(flightSkill == null)
                flightSkill = (PyrokinesisFlightAbility) LOTMCraft.abilityHandler.getById("pyrokinesis_flight");

            if(flightSkill == null) return;

            flightSkill.useAbility((ServerLevel) level, entity);

            return;
        }

        UUID entityId = entity.getUUID();

        if(transformedEntities.contains(entityId)) {
            transformedEntities.remove(entityId);
            return;
        }

        transformedEntities.add(entityId);
        AtomicBoolean shouldStop = new AtomicBoolean(false);

        ServerScheduler.scheduleUntil(level, () -> {
            BeyonderData.reduceSpirituality(entity, 3);

            if (BeyonderData.getSpirituality(entity) <= 0) {
                if (entity instanceof net.minecraft.server.level.ServerPlayer player) {
                    player.connection.send(new ClientboundSetActionBarTextPacket(
                            Component.literal("Your spirituality is exhausted.").withColor(0xFF422a2a)
                    ));
                }
                transformedEntities.remove(entityId);
                shouldStop.set(true);
                return;
            }

            if(!transformedEntities.contains(entityId)) {
                shouldStop.set(true);
                return;
            }

            ParticleUtil.spawnParticles(level, ParticleTypes.FLAME, entity.getEyePosition(), 60, 1.2, .05);
            ParticleUtil.spawnParticles(level, dust, entity.getEyePosition(), 30, 1.2, .05);

            if(!entity.isShiftKeyDown())
                entity.setDeltaMovement(entity.getLookAngle().normalize());
            else
                entity.setDeltaMovement(0, 0, 0);

            entity.hurtMarked = true;
        }, 2, null, shouldStop);
    }

    private void flamingSpear(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(), 0, random.nextDouble(1, 2.85f), random.nextDouble(-.1, .6));
        Vec3 direction = AbilityUtil.getTargetLocation(entity, baseDistance, 1.4f).subtract(startPos).normalize();

        level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 1.0f, 1.0f);

        float damage = baseDamage;

        FlamingSpearProjectileEntity spear = new FlamingSpearProjectileEntity(level, entity, damage, BeyonderData.isGriefingEnabled(entity));
        spear.setPos(startPos.x, startPos.y, startPos.z); // Set initial position
        spear.shoot(direction.x, direction.y, direction.z, 3f, 0);
        level.addFreshEntity(spear);

    }

    private void fireRavens(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        for(int i = 0; i < 9 - entitySeq; i++) {
            Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(), random.nextDouble(.5, 11f), random.nextDouble(-10.5, 10.5), random.nextDouble(.1, 9));

            LivingEntity target = AbilityUtil.getTargetEntity(entity, baseDistance, 1.4f);
            FireRavenEntity fireRaven;

            float damage = baseDamage;
            if(target == null) {
                Vec3 targetPos = AbilityUtil.getTargetLocation(entity, baseDistance, 1.4f);
                fireRaven = new FireRavenEntity(level, targetPos, entity, damage, BeyonderData.isGriefingEnabled(entity));
            }
            else {
                fireRaven = new FireRavenEntity(level, target, entity, damage, BeyonderData.isGriefingEnabled(entity));
            }

            fireRaven.setInvulnerable(true);

            fireRaven.setPos(startPos);
            level.addFreshEntity(fireRaven);
        }
    }

    private void wallOfFire(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, baseDistance, 1.4f);

        Vec3 perpendicular = VectorUtil.getPerpendicularVector(entity.getLookAngle()).normalize();

        double multiplier = multiplier(entity);
        float damage = baseDamage/6;

        ServerScheduler.scheduleForDuration(0, 1, 20 * 20, () -> {
            if(random.nextInt(10) == 0)
                level.playSound(null, targetPos.x, targetPos.y, targetPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 1.0f, 1.0f);

            for(int i = -1; i < 6; i++) {
                for(int j = -7; j < 8; j++) {
                    Vec3 pos = targetPos.add(perpendicular.scale(j)).add(0, i, 0);

                    ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.FLAME, pos, 1, 0.5, 0.02);
                    ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.SMOKE, pos, 1, 0.5, 0.02);

                    AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 1f, ModDamageTypes.FIRE ,damage, pos, true, false);

                    for(LivingEntity target : AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, pos, 1f)) {
                        Vec3 knockback = target.position().subtract(pos).normalize().add(0, .2, 0).scale(0.8f);
                        target.setDeltaMovement(knockback);
                    }
                }
            }
        }, null, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new de.jakob.lotm.util.data.Location(targetPos, level)));
    }

    //TODO: PLace flame blocks on griefing
    private void flameWave(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = entity.getEyePosition().add(0, .5, 0);

        level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 1.0f, 1.0f);

        double multiplier = multiplier(entity);
        float damage = baseDamage;

        ServerScheduler.scheduleDelayed(18, () -> {
            AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 5.5, ModDamageTypes.FIRE, damage/2, entity.position().add(0, .2, 0), true, false);
            AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 5.5, ModDamageTypes.SOUL_FIRE, damage/2, entity.position().add(0, .2, 0), true, false);});

        AtomicDouble i = new AtomicDouble(0.6);
        ServerScheduler.scheduleForDuration(0, 1, 24, () -> {
            double ySubtraction = 2 * ((1/((10 * i.get()) - 9)) - 1);
            Vec3 currentPos = startPos.add(0, ySubtraction, 0);
            double radius = i.get() < .71 ? i.get() : i.get() * 2;
            ParticleUtil.spawnCircleParticles((ServerLevel) level, ParticleTypes.FLAME, currentPos, radius, (int) (radius * 25));
            ParticleUtil.spawnCircleParticles((ServerLevel) level, ParticleTypes.SMOKE, currentPos, radius, (int) (radius * 6));
            i.set(i.get() + .1);
        }, null, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new de.jakob.lotm.util.data.Location(startPos, level)));
    }

    private void fireball(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(), 0, random.nextDouble(1, 2.85f), random.nextDouble(-.1, .6));
        Vec3 direction = AbilityUtil.getTargetLocation(entity, baseDistance, 1.4f).subtract(startPos).normalize();

        level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 1.0f, 1.0f);

        FireballEntity fireball = new FireballEntity(level, entity, baseDamage, BeyonderData.isGriefingEnabled(entity));
        fireball.setPos(startPos.x, startPos.y, startPos.z); // Set initial position
        fireball.shoot(direction.x, direction.y, direction.z, 3f, 0);
        level.addFreshEntity(fireball);
    }

    @Override
    public void nextAbility(LivingEntity entity){
        if(getAbilityNames().length == 0)
            return;

        if(!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        int selectedAbility = selectedAbilities.get(entity.getUUID());
        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        selectedAbility++;
        if(selectedAbility >= getAbilityNames().length) {
            selectedAbility = 0;
        }

        if(entitySeq > 6 && selectedAbility >= 5){
            selectedAbility = 0;
        }

        selectedAbilities.put(entity.getUUID(), selectedAbility);
        PacketHandler.sendToServer(new AbilitySelectionPacket(getId(), selectedAbility));
    }

    @Override
    public void previousAbility(LivingEntity entity){
        if(getAbilityNames().length == 0)
            return;

        if(!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        int selectedAbility = selectedAbilities.get(entity.getUUID());
        selectedAbility--;
        if(selectedAbility <= -1) {
            selectedAbility = getAbilityNames().length - 1;
        }

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        if(entitySeq > 6 && selectedAbility >= 5) {
            selectedAbility = 4;
        }

        selectedAbilities.put(entity.getUUID(), selectedAbility);
        PacketHandler.sendToServer(new AbilitySelectionPacket(getId(), selectedAbility));
    }
}

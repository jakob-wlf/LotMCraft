package de.jakob.lotm.abilities.mother;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.mother.handler.HybridMobData;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.network.packets.toClient.HybridMobSyncPacket;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CrossbreedingAbility extends Ability {
    private final HashMap<UUID, LivingEntity> targets = new HashMap<>();

    public CrossbreedingAbility(String id) {
        super(id, 1);

        canBeUsedByNPC = false;
        canBeCopied = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("mother", 6));
    }

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(112 / 255f, 212 / 255f, 130 / 255f), 1.25f);

    @Override
    public float getSpiritualityCost() {
        return 220;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel))
            return;

        // entities to exclude from
        List<EntityType<?>> notValidTargets = List.of(
                EntityType.ENDER_DRAGON,
                EntityType.WITHER
        );

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 2);
        if(target == null || target instanceof BeyonderNPCEntity || target instanceof Player || notValidTargets.contains(target.getType())) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.crossbreeding.not_valid_mob").withColor(0xFF88c276));
            return;
        }

        if(!targets.containsKey(entity.getUUID())) {
            targets.put(entity.getUUID(), target);
            ParticleUtil.spawnParticles(serverLevel, dust, target.position().add(0, target.getBbHeight() / 2, 0), 60, 0.5f, 0.5f, 0.5f, 0.1f);
        } else {
            LivingEntity previousTarget = targets.get(entity.getUUID());
            targets.remove(entity.getUUID());
            if(previousTarget == target) {
                AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.crossbreeding.not_same_mob").withColor(0xFF88c276));
                return;
            }
            ParticleUtil.spawnParticles(serverLevel, dust, target.position().add(0, target.getBbHeight() / 2, 0), 60, 0.5f, 0.5f, 0.5f, 0.1f);
            if(!previousTarget.isAlive()) {
                targets.put(entity.getUUID(), target);
                return;
            }

            // Spawn the hybrid mob
            spawnHybridMob(level, previousTarget, target);

            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.crossbreeding.success").withColor(0xFF88c276));
        }
    }

    private void spawnHybridMob(Level level, LivingEntity firstMob, LivingEntity secondMob) {
        EntityType<?> entityType = secondMob.getType();
        Entity newEntity = entityType.create(level);

        if(!(newEntity instanceof LivingEntity newMob)) {
            return;
        }

        Vec3 spawnPos = secondMob.position();
        newMob.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, secondMob.getYRot(), secondMob.getXRot());

        HybridMobData hybridData = new HybridMobData(
                EntityType.getKey(firstMob.getType()),
                firstMob.getDimensions(firstMob.getPose())
        );

        CompoundTag tag = hybridData.save();
        newMob.getPersistentData().put("HybridMobData", tag);

        level.addFreshEntity(newMob);
        newMob.refreshDimensions();

        // Send packet to all nearby clients
        if(level instanceof ServerLevel serverLevel) {
            HybridMobSyncPacket packet = new HybridMobSyncPacket(newMob.getId(), tag);
            PacketDistributor.sendToPlayersTrackingEntity(newMob, packet);
        }
    }
}
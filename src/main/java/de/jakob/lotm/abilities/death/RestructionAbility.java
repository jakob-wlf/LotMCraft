package de.jakob.lotm.abilities.death;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.subordinates.SubordinateUtils;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RestructionAbility extends SelectableAbility {


    private static final String[] MODES = {
            "ability.lotmcraft.restruction.summon",
            "ability.lotmcraft.restruction.release"
    };

    private static final HashMap<UUID, List<Mob>> summonedMobs = new HashMap<>();

    public RestructionAbility(String id) {
        super(id, 20f);
        canBeCopied = false;
        cannotBeStolen = true;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("death", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1500;
    }

    @Override
    protected String[] getAbilityNames() {
        return MODES;
    }

    @Override
    public void useAbility(ServerLevel serverLevel, LivingEntity entity, boolean consumeSpirituality, boolean hasToHaveAbility, boolean hasToMeetRequirements) {
        // Release sub-ability bypasses cooldown and spirituality cost
        if (getSelectedAbilityIndex(entity.getUUID()) == 1) {
            onAbilityUse(serverLevel, entity);
            return;
        }
        super.useAbility(serverLevel, entity, consumeSpirituality, hasToHaveAbility, hasToMeetRequirements);
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (selectedAbility == 0 && InteractionHandler.isInteractionPossibleStrictlyHigher(new Location(entity.position(), serverLevel), "purification", BeyonderData.getSequence(entity), -1)) return;

        switch (selectedAbility) {
            case 0 -> summon(serverLevel, entity);
            case 1 -> release(entity);
        }
    }
    private void summon(ServerLevel serverLevel, LivingEntity entity) {
        despawnMobsForPlayer(entity.getUUID(), serverLevel);

        ParticleUtil.spawnParticles(serverLevel, ParticleTypes.SOUL, entity.position(), 750, 6, .5, 6, 0);
        ParticleUtil.spawnParticles(serverLevel, ParticleTypes.LARGE_SMOKE, entity.position(), 450, 6, .5, 6, 0);

        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 1, 1);

        List<Mob> mobs = summonedMobs.computeIfAbsent(entity.getUUID(), k -> new ArrayList<>());
        int SKELETON_COUNT = Math.round(8* Math.max(multiplier(entity)/4,1));
        int ZOMBIE_COUNT = Math.round(8* Math.max(multiplier(entity)/4,1));
        for (int i = 0; i < SKELETON_COUNT; i++) {
            Vec3 spawnPos = findSpawnPos(entity, serverLevel);

            Skeleton skeleton = new Skeleton(net.minecraft.world.entity.EntityType.SKELETON, serverLevel);
            skeleton.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            skeleton.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            skeleton.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
            skeleton.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
            skeleton.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
            skeleton.setDropChance(EquipmentSlot.HEAD, 0f);
            skeleton.setDropChance(EquipmentSlot.CHEST, 0f);
            skeleton.setDropChance(EquipmentSlot.LEGS, 0f);
            skeleton.setDropChance(EquipmentSlot.FEET, 0f);

            if(skeleton.getAttribute(Attributes.ATTACK_DAMAGE) != null && skeleton.getAttribute(Attributes.MAX_HEALTH) != null) {
                skeleton.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(skeleton.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) * 3);
                skeleton.getAttribute(Attributes.MAX_HEALTH).setBaseValue(skeleton.getAttributeBaseValue(Attributes.MAX_HEALTH) * 3);
            }

            skeleton.setHealth(skeleton.getMaxHealth());

            serverLevel.addFreshEntity(skeleton);
            SubordinateUtils.turnEntityIntoSubordinate(skeleton, entity, false);
            mobs.add(skeleton);
        }

        for (int i = 0; i < ZOMBIE_COUNT; i++) {
            Vec3 spawnPos = findSpawnPos(entity, serverLevel);

            Zombie zombie = new Zombie(net.minecraft.world.entity.EntityType.ZOMBIE, serverLevel);
            zombie.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            zombie.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
            zombie.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
            zombie.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
            zombie.setDropChance(EquipmentSlot.HEAD, 0f);
            zombie.setDropChance(EquipmentSlot.CHEST, 0f);
            zombie.setDropChance(EquipmentSlot.LEGS, 0f);
            zombie.setDropChance(EquipmentSlot.FEET, 0f);

            if(zombie.getAttribute(Attributes.ATTACK_DAMAGE) != null && zombie.getAttribute(Attributes.MAX_HEALTH) != null) {
                zombie.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(zombie.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) * 3);
                zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(zombie.getAttributeBaseValue(Attributes.MAX_HEALTH) * 3);
            }
            zombie.setHealth(zombie.getMaxHealth());

            serverLevel.addFreshEntity(zombie);
            SubordinateUtils.turnEntityIntoSubordinate(zombie, entity, false);
            mobs.add(zombie);
        }

        ServerScheduler.scheduleDelayed(20 * 45, () -> {
            despawnMobsForPlayer(entity.getUUID(), serverLevel);
        }, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), serverLevel)));
    }

    private void release(LivingEntity entity) {
        despawnMobsForPlayer(entity.getUUID(), (ServerLevel) entity.level());
    }

    private void despawnMobsForPlayer(UUID playerUUID, ServerLevel level) {
        List<Mob> mobs = summonedMobs.remove(playerUUID);
        if (mobs != null) {
            for (Mob mob : mobs) {
                if (mob.isAlive()) {
                    ParticleUtil.spawnParticles(level, ParticleTypes.LARGE_SMOKE, mob.position().add(0, mob.getBbHeight() / 2, 0), 40, .4, .9, .4, 0);
                    ParticleUtil.spawnParticles(level, ParticleTypes.SOUL_FIRE_FLAME, mob.position().add(0, mob.getBbHeight() / 2, 0), 40, .4, .9, .4, 0);
                    mob.discard();
                }
            }
        }

        summonedMobs.remove(playerUUID);
    }

    private Vec3 findSpawnPos(LivingEntity entity, ServerLevel level) {
        for (int attempt = 0; attempt < 10; attempt++) {
            double offsetX = random.nextDouble(-4, 4);
            double offsetZ = random.nextDouble(-4, 4);
            Vec3 candidate = entity.position().add(offsetX, 0, offsetZ);

            net.minecraft.core.BlockPos blockPos = net.minecraft.core.BlockPos.containing(candidate);
            while (blockPos.getY() > level.getMinBuildHeight() &&
                    level.getBlockState(blockPos).isAir()) {
                blockPos = blockPos.below();
            }

            if (!level.getBlockState(blockPos).isAir()) {
                return Vec3.atBottomCenterOf(blockPos.above());
            }
        }
        return entity.position();
    }
}

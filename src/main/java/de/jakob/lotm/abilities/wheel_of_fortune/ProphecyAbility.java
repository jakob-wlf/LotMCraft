package de.jakob.lotm.abilities.wheel_of_fortune;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.attachments.LuckComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.tyrant_pathway.GiantLightningEntity;
import de.jakob.lotm.entity.custom.ability_entities.MeteorEntity;
import de.jakob.lotm.entity.custom.ability_entities.TornadoEntity;
import de.jakob.lotm.entity.custom.ability_entities.tyrant_pathway.TsunamiEntity;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.VectorUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ProphecyAbility extends SelectableAbility {
    public ProphecyAbility(String id) {
        super(id, 4);
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 2));
    }

    @Override
    public float getSpiritualityCost() {
        return 1000;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.prophecy.disaster",
                "ability.lotmcraft.prophecy.fortune",
                "ability.lotmcraft.prophecy.misfortune_for_enemy"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if(level.isClientSide()) return;

        switch(selectedAbility) {
            case 0 -> manifestDisaster(level, entity);
            case 1 -> manifestFortune(level, entity);
            case 2 -> manifestMisfortuneForEnemy(level, entity);
        }
    }

    private void manifestDisaster(Level level, LivingEntity entity) {
        switch (random.nextInt(4)) {
            case 0 -> {
                Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 85, 3);

                MeteorEntity meteor = new MeteorEntity(level, 3.25f,  (float) DamageLookup.lookupDamage(2, 1) * (int) Math.max(BeyonderData.getMultiplier(entity)/2,1), 4, entity, BeyonderData.isGriefingEnabled(entity), 20, 34);
                meteor.setPosition(targetLoc);
                level.addFreshEntity(meteor);
            }
            case 1 -> {
                Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 70, 2, true);
                for(int i = 0; i < 35; i++) {
                    BlockState state = level.getBlockState(BlockPos.containing(targetLoc.subtract(0, 1, 0)));
                    if(state.getCollisionShape(level, BlockPos.containing(targetLoc)).isEmpty())
                        targetLoc = targetLoc.subtract(0, 1, 0);
                }

                GiantLightningEntity lightning = new GiantLightningEntity(level, entity, targetLoc, 50, 6, DamageLookup.lookupDamage(2, 1)  * (int) Math.max(BeyonderData.getMultiplier(entity)/2,1), BeyonderData.isGriefingEnabled(entity), 13, 200, 0x6522a8);
                level.addFreshEntity(lightning);
            }
            case 2 -> {
                LivingEntity target = AbilityUtil.getTargetEntity(entity, 12, 3);

                Vec3 pos = AbilityUtil.getTargetLocation(entity, 12, 2);

                TornadoEntity tornado = target == null ? new TornadoEntity(ModEntities.TORNADO.get(), level, .15f, (float) DamageLookup.lookupDamage(2, 1), entity) : new TornadoEntity(ModEntities.TORNADO.get(), level, .15f, 32.5f * (int) Math.max(BeyonderData.getMultiplier(entity)/2,1), entity, target, 2);
                tornado.setPos(pos);
                level.addFreshEntity(tornado);

                for(int i = 0; i < 5; i++) {
                    TornadoEntity additionalTornado = target == null || (new Random()).nextInt(4) != 0 ? new TornadoEntity(ModEntities.TORNADO.get(), level, .15f, (float) DamageLookup.lookupDamage(2, .75) * (int) Math.max(BeyonderData.getMultiplier(entity)/2,1), entity) : new TornadoEntity(ModEntities.TORNADO.get(), level, .15f, (float) DamageLookup.lookupDamage(2, .75) * (int) Math.max(BeyonderData.getMultiplier(entity)/2,1), entity, target, 2);
                    Vec3 randomOffset = new Vec3((level.random.nextDouble() - 0.5) * 40, 3, (level.random.nextDouble() - 0.5) * 40);
                    additionalTornado.setPos(pos.add(randomOffset));
                    level.addFreshEntity(additionalTornado);
                }
            }
            case 3 -> {
                Vec3 position = VectorUtil.getRelativePosition(entity.position(), entity.getLookAngle().normalize(), 10, random.nextDouble(-11, 11), 0);
                Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 40, 2);
                Vec3 direction = targetPos.subtract(position).normalize();

                level.playSound(null, entity.blockPosition(), SoundEvents.GENERIC_SPLASH, entity.getSoundSource(), 5, 1.0f);

                TsunamiEntity tsunami = new TsunamiEntity(level, position, direction, (float) (DamageLookup.lookupDamage(2, 1) * (int) Math.max(BeyonderData.getMultiplier(entity)/2,1)), BeyonderData.isGriefingEnabled(entity), entity);
                level.addFreshEntity(tsunami);
            }
        }
    }

    private void manifestMisfortuneForEnemy(Level level, LivingEntity entity) {
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 40, 6);
        if(target == null) return;

        switch (random.nextInt(3)) {
            case 0 -> {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 40, 4, false, true, true));
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 40, 4, false, true, true));
                target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 40, 4, false, true, true));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 40, 4, false, true, true));
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * 40, 6, false, true, true));
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 40, 6, false, true, true));
                target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 20 * 40, 6, false, true, true));
                BeyonderData.addModifierWithTimeLimit(target, "prophecy", 0.6, 20 * 40);
            }
            case 1 -> {
                LuckComponent component = target.getData(ModAttachments.LUCK_COMPONENT.get());
                int amplifier = (int) Math.min(Math.round(BeyonderData.getMultiplier(entity) * 6.25f) * 100, 6500);
                component.addLuckWithMin(-amplifier, (int) (-amplifier * 1.5));
                EffectManager.playEffect(EffectManager.Effect.MISFORTUNE_CURSE, target.getX(), target.getY() + 1, target.getZ(), (ServerLevel) level);
            }
            case 2 -> {
                double xOffset = (random.nextDouble() - 0.5) * 30;
                double yOffset = (random.nextDouble()) * 60;
                double zOffset = (random.nextDouble() - 0.5) * 30;
                Vec3 position = target.position().add(xOffset, yOffset, zOffset);
                target.teleportTo(position.x, position.y, position.z);
            }
        }
    }

    private void manifestFortune(Level level, LivingEntity entity) {
        switch (random.nextInt(2)) {
            case 0 -> rainGoodItems(level, entity);
            case 1 -> giveLuckEffect(level, entity);
        }
    }

    private void giveLuckEffect(Level level, LivingEntity entity) {
        LuckComponent component = entity.getData(ModAttachments.LUCK_COMPONENT.get());
        component.addLuckWithMax(3000, 3000);
        EffectManager.playEffect(EffectManager.Effect.BLESSING, entity.getX(), entity.getY() + 1, entity.getZ(), (ServerLevel) level);
    }

    private void rainGoodItems(Level level, LivingEntity entity) {
        Vec3 startLoc = entity.position().add(0, 7, 0);
        for(int i = 0; i < 50; i++) {
            ItemStack goodItem = getGoodItem();
            Vec3 spawnLoc = startLoc.add((random.nextDouble() - 0.5) * 12, 0, (random.nextDouble() - 0.5) * 12);
            BlockPos pos = BlockPos.containing(spawnLoc);
            Block.popResource(level, pos, goodItem);
        }
    }

    private ItemStack getGoodItem() {
        return switch (random.nextInt(10)) {
            case 0 -> new ItemStack(Items.EMERALD, random.nextInt(1, 5));
            case 1, 2 -> new ItemStack(Items.DIAMOND, random.nextInt(1, 5));
            case 3 -> new ItemStack(Items.GOLD_INGOT, random.nextInt(1, 32));
            case 4 -> new ItemStack(Items.IRON_BLOCK, random.nextInt(1, 10));
            case 5 -> new ItemStack(Items.SHULKER_SHELL, random.nextInt(1, 5));
            case 6 -> new ItemStack(Items.GLOWSTONE, random.nextInt(1, 32));
            case 7 -> new ItemStack(Items.GOLDEN_CARROT, random.nextInt(1, 32));
            case 8 -> new ItemStack(Items.ANCIENT_DEBRIS, 1);
            case 9 -> new ItemStack(Items.ENDER_PEARL, random.nextInt(1, 8));
            default -> new ItemStack(Items.DIAMOND, 1);
        };
    }
}

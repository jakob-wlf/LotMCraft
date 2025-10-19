package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.joml.Vector3f;

import java.util.List;
import java.util.Random;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class LuckEventHandler {

    // Double Loot handled in DoubleLootModifier

    private static final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(192 / 255f, 246 / 255f, 252 / 255f),
            1.5f
    );

    // Dodge Damage
    @SubscribeEvent
    public static void onLivingDamage(LivingIncomingDamageEvent event) {
        if(!event.getEntity().hasEffect(ModEffects.LUCK) || !(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }

        int amplifier = event.getEntity().getEffect(ModEffects.LUCK).getAmplifier();
        double dodgeChance = getDodgeChance(amplifier);

        if (Math.random() >= dodgeChance) {
            return;
        }

        event.setCanceled(true);

        Entity entity = event.getEntity();
        ParticleUtil.spawnParticles(level, dust, entity.position().add(0, entity.getEyeHeight() / 2, 0), 55, .4, entity.getEyeHeight() / 2, .4, 0);

        if(event.getEntity() instanceof ServerPlayer player) {
            Component actionBarText = Component.translatable("ability.lotmcraft.passive_luck.dodge").withColor(0xFFc0f6fc);
            sendActionBar(player, actionBarText);
        }

    }

    // Critical Hits
    @SubscribeEvent
    public static void onLivingDamageLiving(LivingIncomingDamageEvent event) {
        Entity damager = event.getSource().getEntity();

        if(!(damager instanceof LivingEntity entity)) {
            return;
        }

        if(!entity.hasEffect(ModEffects.LUCK)) {
            return;
        }

        if(!(entity.level() instanceof ServerLevel level)) {
            return;
        }

        int amplifier = entity.getEffect(ModEffects.LUCK).getAmplifier();
        double critChance = getCritChance(amplifier);
        if (Math.random() >= critChance) {
            return;
        }

        float originalDamage = event.getAmount();
        float critDamage = originalDamage * 1.75f;
        event.setAmount(critDamage);
        ParticleUtil.spawnParticles(level, dust, event.getEntity().position().add(0, event.getEntity().getEyeHeight() / 2, 0), 55, .4, event.getEntity().getEyeHeight() / 2, .4, 0);

        if(entity instanceof ServerPlayer player) {
            Component actionBarText = Component.translatable("ability.lotmcraft.passive_luck.crit").withColor(0xFFc0f6fc);
            sendActionBar(player, actionBarText);
        }
    }

    // Mine Multiple Blocks
    @SubscribeEvent
    public static void onMineBlocks(BlockDropsEvent event) {
        if(!(event.getBreaker() instanceof LivingEntity entity)) {
            return;
        }

        if(!entity.hasEffect(ModEffects.LUCK) || !(entity.level() instanceof ServerLevel level)) {
            return;
        }

        int amplifier = entity.getEffect(ModEffects.LUCK).getAmplifier();
        double multipleBlocksChance = getMultipleBlocksWhenMiningChance(amplifier);

        if (Math.random() >= multipleBlocksChance) {
            return;
        }


        if(new Random().nextBoolean())
            ParticleUtil.spawnParticles(level, dust, event.getPos().getCenter(), 12, .6, .6, .6, 0);

        List<ItemEntity> drops = event.getDrops();

        if (!drops.isEmpty()) {
            ItemStack randomDrop = drops.get(level.getRandom().nextInt(drops.size())).getItem().copy();

            Block.popResource(level, event.getPos(), randomDrop);
        }

    }

    // Remove Harmful Effects
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if(!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if(!entity.hasEffect(ModEffects.LUCK) || !(entity.level() instanceof ServerLevel level)) {
            return;
        }

        int amplifier = entity.getEffect(ModEffects.LUCK).getAmplifier();

        if(Math.random() < getChanceForPotionEffectRemoval(amplifier)) {
            removeHarmfulEffects(entity, level);
        }

    }

    private static void removeHarmfulEffects(LivingEntity entity, ServerLevel level) {
        List<Holder<MobEffect>> harmfulEffects = entity.getActiveEffects().stream()
                .map(MobEffectInstance::getEffect)
                .filter(effect -> effect.value().getCategory() == MobEffectCategory.HARMFUL)
                .toList();

        if(harmfulEffects.isEmpty()) {
            return;
        }

        harmfulEffects.forEach(entity::removeEffect);

        ParticleUtil.spawnParticles(level, dust, entity.position().add(0, entity.getEyeHeight() / 2, 0), 55, .4, entity.getEyeHeight() / 2, .4, 0);

        if(entity instanceof ServerPlayer player) {
            Component actionBarText = Component.translatable("ability.lotmcraft.passive_luck.effect_remove").withColor(0xFFc0f6fc);
            sendActionBar(player, actionBarText);
        }

    }


    private static void sendActionBar(ServerPlayer player, Component message) {
        ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(message);
        player.connection.send(packet);
    }

    private static double getCritChance(int amplifier) {
        return Math.max(Math.min(0.04 * (amplifier + 1), 0.9), 0.05);
    }

    private static double getDodgeChance(int amplifier) {
        return Math.max(Math.min(0.05 * (amplifier + 1), 0.9), 0.05);
    }

    private static double getMultipleBlocksWhenMiningChance(int amplifier) {
        return Math.max(Math.min(0.1 + .045 * (amplifier + 1), 0.99), 0.1);
    }

    private static double getChanceForPotionEffectRemoval(int amplifier) {
        return amplifier >= 19 ? 0.05 : 0.0025 + (0.05 - 0.0025) / 19 * Math.max(amplifier, 0);
    }

    private static double getExtraLootChance(int amplifier) {
        return Math.min(0.04 * (amplifier + 1), 0.75);
    }

}

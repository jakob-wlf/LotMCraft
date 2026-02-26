package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.effect.LoosingControlEffect;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.effect.UnluckEffect;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.joml.Vector3f;

import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class LuckEventHandler {

    private static final HashMap<UUID, CombatTarget> combatTargets = new HashMap<>();

    // Double Loot handled in DoubleLootModifier

    private static final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(192 / 255f, 246 / 255f, 252 / 255f),
            1.5f
    );

    // Randomly drop good items when mining blocks
    @SubscribeEvent
    public static void onBreakBlocks(BlockDropsEvent event) {
        if(!(event.getBreaker() instanceof LivingEntity entity)) {
            return;
        }

        if(!entity.hasEffect(ModEffects.LUCK) || !(entity.level() instanceof ServerLevel level)) {
            return;
        }

        int amplifier = entity.getEffect(ModEffects.LUCK).getAmplifier();
        double chance = getChanceForRandomDrop(amplifier);

        if (Math.random() >= chance) {
            return;
        }


        if(new Random().nextBoolean())
            ParticleUtil.spawnParticles(level, dust, event.getPos().getCenter(), 12, .6, .6, .6, 0);

        dropRandomItem(event.getPos().getCenter(), level);
    }

    // Dodge Damage
    @SubscribeEvent
    public static void onLivingDamage(LivingIncomingDamageEvent event) {
        if(!event.getEntity().hasEffect(ModEffects.LUCK) || !(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }

        if(event.getAmount() > 500) {
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

    //Check for targets
    @SubscribeEvent
    public static void onLivingDamageLiving(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        Entity damager = event.getSource().getEntity();

        if(!(damager instanceof LivingEntity livingDamager)) {
            return;
        }

        if(entity.hasEffect(ModEffects.LUCK)) {
            long currentTime = System.currentTimeMillis();
            CombatTarget combatTarget = new CombatTarget(livingDamager, currentTime);
            combatTargets.put(entity.getUUID(), combatTarget);
        }
        if(livingDamager.hasEffect(ModEffects.LUCK)) {
            long currentTime = System.currentTimeMillis();
            CombatTarget combatTarget = new CombatTarget(entity, currentTime);
            combatTargets.put(livingDamager.getUUID(), combatTarget);
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
            if(drops.stream().anyMatch(itemEntity -> itemEntity.getItem().is(Items.SHULKER_BOX))) {
                return;
            }
            ItemStack randomDrop = drops.get(level.getRandom().nextInt(drops.size())).getItem().copy();

            Block.popResource(level, event.getPos(), randomDrop);
            Block.popResource(level, event.getPos(), randomDrop);
        }

    }

    // Remove Harmful Effects / Add Hero of the Village effect / Random drops / Make Enemies trip
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

        if(amplifier > 1) {
            entity.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 40, amplifier + 1, false, false, false));
        }

        if(Math.random() < getChanceForRandomDrop(amplifier)) {
            // Remove random drops for now, later add them, maybe when killing enemies
            // dropRandomItem(entity, level);
        }

        if(Math.random() < getChanceForEntityTrip(amplifier)) {
            makeEntityTrip(entity, amplifier, level);
        }

    }

    private static void makeEntityTrip(LivingEntity entity, int amplifier, ServerLevel level) {
        if(!combatTargets.containsKey(entity.getUUID())) {
            return;
        }

        CombatTarget combatTarget = combatTargets.get(entity.getUUID());
        if(System.currentTimeMillis() - combatTarget.timestamp() > 6000) {
            combatTargets.remove(entity.getUUID());
            return;
        }

        LivingEntity target = combatTarget.target();
        if(target.isDeadOrDying()) {
            combatTargets.remove(entity.getUUID());
            return;
        }

        if(target.level() != level) {
            combatTargets.remove(entity.getUUID());
            return;
        }

        Random random = new Random();

        target.hurt(target.damageSources().generic(), 2.5f * (amplifier + 1));
        target.setDeltaMovement(random.nextDouble(-.5, .5), random.nextDouble(0, .2), random.nextDouble(-.5, .5));
        target.hurtMarked = true;

        ParticleUtil.spawnParticles(level, dust, target.position().add(0, target.getEyeHeight() / 2, 0), 55, .4, target.getEyeHeight() / 2, .4, 0);

        if(entity instanceof ServerPlayer player) {
            Component actionBarText = Component.translatable("ability.lotmcraft.passive_luck.trip").withColor(0xFFc0f6fc);
            sendActionBar(player, actionBarText);
        }
    }

    private static final ItemDrop[] possibleDrops = new ItemDrop[] {
            new ItemDrop(Items.GOLDEN_CARROT, 32, 0.3),
            new ItemDrop(Items.DIAMOND, 6, 0.05),
            new ItemDrop(Items.GOLD_INGOT, 22, 0.15),
            new ItemDrop(Items.EMERALD, 22, 0.15),
            new ItemDrop(Items.LAPIS_LAZULI, 22, 0.12),
            new ItemDrop(Items.REDSTONE_BLOCK, 20, 0.11),
            new ItemDrop(Items.IRON_INGOT, 28, 0.15),
            new ItemDrop(Items.COAL, 25, 0.20),
            new ItemDrop(Items.QUARTZ, 22, 0.12),
            new ItemDrop(Items.NETHER_STAR, 1, 0.02),
    };

    private static void dropRandomItem(Vec3 startPos, ServerLevel level) {
        ItemStack randomItemStack = getRandomItemStack();
        BlockPos pos = BlockPos.containing(startPos);
        Block.popResource(level, pos, randomItemStack);

        ParticleUtil.spawnParticles(level, dust, pos.getCenter().add(0, .25, 0), 55, .4, .4, .4, 0);
    }

    private static ItemStack getRandomItemStack() {
        List<ItemDrop> scaledDrops = new ArrayList<>();
        for(ItemDrop drop : possibleDrops) {
            int amountInList = (int) (100 * drop.dropChance());
            for(int i = 0; i < amountInList; i++) {
                scaledDrops.add(drop);
            }
        }

        Random random = new Random();
        ItemDrop selectedDrop = scaledDrops.get(random.nextInt(scaledDrops.size()));

        return new ItemStack(selectedDrop.item(), random.nextInt(1, selectedDrop.count() + 1));
    }

    private static void removeHarmfulEffects(LivingEntity entity, ServerLevel level) {
        List<Holder<MobEffect>> harmfulEffects = entity.getActiveEffects().stream()
                .map(MobEffectInstance::getEffect)
                .filter(effect -> effect.value().getCategory() == MobEffectCategory.HARMFUL && !(effect.value() instanceof LoosingControlEffect) && !(effect.value() instanceof UnluckEffect))
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
        return Math.max(Math.min(0.035 * (amplifier + 1), 0.65), 0.035);
    }

    private static double getMultipleBlocksWhenMiningChance(int amplifier) {
        return Math.max(Math.min(0.1 + .045 * (amplifier + 1), 0.99), 0.1);
    }

    private static double getChanceForPotionEffectRemoval(int amplifier) {
        return amplifier >= 19 ? 0.05 : 0.0025 + (0.05 - 0.0025) / 19 * Math.max(amplifier, 0);
    }

    private static double getChanceForEntityTrip(int amplifier) {
        return lerpClamped(amplifier, 0, 19, 0.002, 0.035);
    }

    private static double lerpClamped(double amplifier, double minAmplifier, double maxAmplifier, double minValue, double maxValue) {
        double t = (amplifier - minAmplifier) / (maxAmplifier - minAmplifier);
        t = Math.max(0.0, Math.min(1.0, t)); // clamp to [0,1]
        return minValue + t * (maxValue - minValue);
    }

    private static double getChanceForRandomDrop(int amplifier) {
        double value = 0.01 * amplifier + 0.01;
        return Math.min(value, 0.2);
    }

    private record ItemDrop(Item item, int count, double dropChance) {}

    private record CombatTarget(LivingEntity target, long timestamp) {}
}

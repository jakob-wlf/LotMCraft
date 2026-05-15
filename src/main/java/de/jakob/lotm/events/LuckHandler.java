package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.damage.ModDamageTypes;
import net.minecraft.client.multiplayer.ClientLevel;
import de.jakob.lotm.effect.LoosingControlEffect;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.joml.Vector3f;

import java.util.*;

/**
 * Luck Stages (luck value -3000 to 3000):
 *
 * Severe Unluck  [-3000, -1500) — All unluck penalties are active at full strength.
 *                                  Drops destroyed, tool damaged, extra damage taken,
 *                                  weak hits dealt, harmful effects applied, trips,
 *                                  mob spawns, item drops from inventory, slipping,
 *                                  and — for Beyonders — multiplier reduction and
 *                                  ability disabling.
 *
 * Mild Unluck    [-1500,    -1] — The same unluck systems are active but at reduced
 *                                  intensity, scaling linearly from minimum at -1
 *                                  to maximum at -3000.
 *
 * Neutral        [       0    ] — No luck or unluck effects apply.
 *
 * Mild Luck      [    1, 1500] — Luck bonuses are active at reduced intensity,
 *                                  scaling linearly from minimum at 1 to maximum
 *                                  at 3000. Includes dodge chance, crit hits, double
 *                                  block drops, random item drops, harmful effect
 *                                  removal, enemy trips, and Hero of the Village
 *                                  at higher values.
 *
 * Peak Luck      [1500,  3000] — All luck bonuses active at full strength.
 */
@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class LuckHandler {

    private static final HashMap<UUID, CombatTarget> combatTargets = new HashMap<>();

    private static final HashMap<UUID, Long> lastSpawnTime             = new HashMap<>();
    private static final HashMap<UUID, Long> lastSlipTime              = new HashMap<>();
    private static final HashMap<UUID, Long> lastMultiplierReductionTime = new HashMap<>();
    private static final HashMap<UUID, Long> lastAbilityDisableTime    = new HashMap<>();
    private static final HashMap<UUID, Long> lastTripTime              = new HashMap<>();

    private static final DustParticleOptions LUCK_DUST = new DustParticleOptions(
            new Vector3f(192 / 255f, 246 / 255f, 252 / 255f), 1.5f
    );
    private static final DustParticleOptions UNLUCK_DUST = new DustParticleOptions(
            new Vector3f(161 / 255f, 114 / 255f, 58 / 255f), 1.5f
    );

    private static final ItemDrop[] POSSIBLE_LUCK_DROPS = {
            new ItemDrop(Items.GOLDEN_CARROT,  32, 0.30),
            new ItemDrop(Items.DIAMOND,         6, 0.05),
            new ItemDrop(Items.GOLD_INGOT,     22, 0.15),
            new ItemDrop(Items.EMERALD,        22, 0.15),
            new ItemDrop(Items.LAPIS_LAZULI,   22, 0.12),
            new ItemDrop(Items.REDSTONE_BLOCK, 20, 0.11),
            new ItemDrop(Items.IRON_INGOT,     28, 0.15),
            new ItemDrop(Items.COAL,           25, 0.20),
            new ItemDrop(Items.QUARTZ,         22, 0.12),
            new ItemDrop(Items.NETHER_STAR,     1, 0.02),
    };

    // -------------------------------------------------------------------------
    // Block break events
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public static void onBreakBlocks(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof LivingEntity entity)) return;
        if (!(entity.level() instanceof ServerLevel level)) return;

        int luck = getLuck(entity);
        if (luck == 0) return;

        if (luck > 0) {
            handleLuckBlockBreak(event, entity, level, luck);
        } else {
            handleUnluckBlockBreak(event, entity, level, luck);
        }
    }

    private static void handleLuckBlockBreak(BlockDropsEvent event, LivingEntity entity, ServerLevel level, int luck) {
        if (Math.random() < getChanceForRandomDrop(luck)) {
            if (new Random().nextBoolean())
                ParticleUtil.spawnParticles(level, LUCK_DUST, event.getPos().getCenter(), 12, .6, .6, .6, 0);
            dropRandomLuckItem(event.getPos().getCenter(), level);
        }

        if (Math.random() < getMultipleBlocksChance(luck)) {
            List<ItemEntity> drops = event.getDrops();
            if (drops.isEmpty()) return;
            if (drops.stream().anyMatch(ie -> ie.getItem().is(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "shulker_boxes"))))) return;

            if (new Random().nextBoolean())
                ParticleUtil.spawnParticles(level, LUCK_DUST, event.getPos().getCenter(), 12, .6, .6, .6, 0);

            ItemStack copy = drops.get(level.getRandom().nextInt(drops.size())).getItem().copy();
            Block.popResource(level, event.getPos(), copy);
            Block.popResource(level, event.getPos(), copy.copy());
        }
    }

    private static void handleUnluckBlockBreak(BlockDropsEvent event, LivingEntity entity, ServerLevel level, int luck) {
        int magnitude = -luck;

        if (Math.random() < getItemDestroyChance(magnitude)) {
            event.getDrops().clear();
            ParticleUtil.spawnParticles(level, UNLUCK_DUST, event.getPos().getCenter(), 20, .6, .6, .6, 0);
            level.playSound(null, event.getPos(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 0.8f);
        }

        if (Math.random() < getToolDamageChance(magnitude)) {
            ItemStack heldItem = entity.getMainHandItem();
            if (!heldItem.isEmpty() && heldItem.isDamageableItem()) {
                int damage = (int) (5 * lerpClamped(magnitude, 0, 3000, 1, 6));
                heldItem.hurtAndBreak(damage, entity, LivingEntity.getSlotForHand(entity.getUsedItemHand()));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Incoming damage — dodge (luck) or extra damage (unluck)
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        if (event.getAmount() > 500) return;

        int luck = getLuck(event.getEntity());

        if (luck > 0) {
            if (Math.random() < getDodgeChance(luck)) {
                event.setCanceled(true);
                Entity entity = event.getEntity();
                ParticleUtil.spawnParticles(level, LUCK_DUST, entity.position().add(0, entity.getEyeHeight() / 2, 0), 55, .4, entity.getEyeHeight() / 2, .4, 0);
                if (entity instanceof ServerPlayer player)
                    sendActionBar(player, Component.translatable("ability.lotmcraft.passive_luck.dodge").withColor(0xFFc0f6fc));
            }
            return;
        }

        if (luck < 0) {
            int magnitude = -luck;
            if (Math.random() < getExtraDamageChance(magnitude)) {
                float multiplier = 1.5f + (float) lerpClamped(magnitude, 0, 3000, 0, 1.0);
                event.setAmount(event.getAmount() * multiplier);
                Entity entity = event.getEntity();
                ParticleUtil.spawnParticles(level, UNLUCK_DUST, entity.position().add(0, entity.getEyeHeight() / 2, 0), 55, .4, entity.getEyeHeight() / 2, .4, 0);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Outgoing damage — crits (luck) or weak hits (unluck)
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public static void onOutgoingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        if (!(attacker.level() instanceof ServerLevel level)) return;

        int luck = getLuck(attacker);

        if (luck > 0) {
            if (Math.random() < getCritChance(luck)) {
                event.setAmount(event.getAmount() * 1.75f);
                ParticleUtil.spawnParticles(level, LUCK_DUST, event.getEntity().position().add(0, event.getEntity().getEyeHeight() / 2, 0), 55, .4, event.getEntity().getEyeHeight() / 2, .4, 0);
                if (attacker instanceof ServerPlayer player)
                    sendActionBar(player, Component.translatable("ability.lotmcraft.passive_luck.crit").withColor(0xFFc0f6fc));
            }
            return;
        }

        if (luck < 0) {
            int magnitude = -luck;
            if (Math.random() < getWeakHitChance(magnitude)) {
                event.setAmount(event.getAmount() * 0.4f);
                ParticleUtil.spawnParticles(level, UNLUCK_DUST, event.getEntity().position().add(0, event.getEntity().getEyeHeight() / 2, 0), 30, .4, event.getEntity().getEyeHeight() / 2, .4, 0);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Track combat targets for the luck trip ability
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public static void onDamageDealt(LivingDamageEvent.Post event) {
        LivingEntity victim = event.getEntity();
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;

        long now = System.currentTimeMillis();

        if (getLuck(victim) > 0)
            combatTargets.put(victim.getUUID(), new CombatTarget(attacker, now));

        if (getLuck(attacker) > 0)
            combatTargets.put(attacker.getUUID(), new CombatTarget(victim, now));
    }

    // -------------------------------------------------------------------------
    // Per-tick effects
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!(entity.level() instanceof ServerLevel level)) return;

        int luck = getLuck(entity);
        if (luck == 0) return;

        if (luck > 0) {
            tickLuckEffects(entity, level, luck);
        } else {
            tickUnluckEffects(entity, level, -luck);
        }
    }

    private static void tickLuckEffects(LivingEntity entity, ServerLevel level, int luck) {
        if (Math.random() < getChanceForPotionEffectRemoval(luck))
            removeLuckHarmfulEffects(entity, level);

        if (luck >= 1500)
            entity.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 40,
                    (int) lerpClamped(luck, 1500, 3000, 0, 3), false, false, false));

        if (Math.random() < getChanceForEntityTrip(luck))
            makeCombatTargetTrip(entity, luck, level);
    }

    private static void tickUnluckEffects(LivingEntity entity, ServerLevel level, int magnitude) {
        if (Math.random() < getHarmfulEffectChance(magnitude))
            applyRandomHarmfulEffect(entity, level);

        if (Math.random() < getTripChance(magnitude))
            tripAndTakeDamage(entity, level, magnitude);

        if (magnitude >= 1500)
            entity.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, 100,
                    (int) lerpClamped(magnitude, 1500, 3000, 0, 5), false, false, true));

        if (Math.random() < getMobSpawnChance(magnitude))
            spawnHostileMob(entity, level, magnitude);

        if (Math.random() < getItemDropChance(magnitude))
            dropRandomInventoryItem(entity, level);

        if (Math.random() < getSlipChance(magnitude))
            makeEntitySlip(entity, level);

        if (BeyonderData.isBeyonder(entity)) {
           /* if (Math.random() < getMultiplierReductionChance(magnitude))
                reduceMultiplierTemporarily(entity, level, magnitude);*/
            if (Math.random() < getAbilityDisableChance(magnitude))
                disableAbilitiesTemporarily(entity, level, magnitude);
        }
    }

    // -------------------------------------------------------------------------
    // Luck helpers
    // -------------------------------------------------------------------------

    private static void removeLuckHarmfulEffects(LivingEntity entity, ServerLevel level) {
        List<Holder<MobEffect>> harmful = entity.getActiveEffects().stream()
                .map(MobEffectInstance::getEffect)
                .filter(e -> e.value().getCategory() == MobEffectCategory.HARMFUL
                        && !(e.value() instanceof LoosingControlEffect))
                .toList();

        if (harmful.isEmpty()) return;

        harmful.forEach(entity::removeEffect);
        ParticleUtil.spawnParticles(level, LUCK_DUST, entity.position().add(0, entity.getEyeHeight() / 2, 0), 55, .4, entity.getEyeHeight() / 2, .4, 0);

        if (entity instanceof ServerPlayer player)
            sendActionBar(player, Component.translatable("ability.lotmcraft.passive_luck.effect_remove").withColor(0xFFc0f6fc));
    }

    private static void makeCombatTargetTrip(LivingEntity entity, int luck, ServerLevel level) {
        if (!combatTargets.containsKey(entity.getUUID())) return;

        CombatTarget combatTarget = combatTargets.get(entity.getUUID());
        if (System.currentTimeMillis() - combatTarget.timestamp() > 6000) {
            combatTargets.remove(entity.getUUID());
            return;
        }

        LivingEntity target = combatTarget.target();
        if (target.isDeadOrDying() || target.level() != level) {
            combatTargets.remove(entity.getUUID());
            return;
        };
        float scalable_damage = (float) (Math.abs(luck) *0.0054+ 2.4722);
        float damage =(float) lerpClamped(luck, 0, 3000, 1, scalable_damage);
        target.hurt(target.damageSources().generic(), damage);

        Random random = new Random();
        target.setDeltaMovement(random.nextDouble(-.5, .5), random.nextDouble(0, .2), random.nextDouble(-.5, .5));
        target.hurtMarked = true;

        ParticleUtil.spawnParticles(level, LUCK_DUST, target.position().add(0, target.getEyeHeight() / 2, 0), 55, .4, target.getEyeHeight() / 2, .4, 0);

        if (entity instanceof ServerPlayer player)
            sendActionBar(player, Component.translatable("ability.lotmcraft.passive_luck.trip").withColor(0xFFc0f6fc));
    }

    private static void dropRandomLuckItem(Vec3 pos, ServerLevel level) {
        List<ItemDrop> pool = new ArrayList<>();
        for (ItemDrop drop : POSSIBLE_LUCK_DROPS) {
            int weight = (int) (100 * drop.dropChance());
            for (int i = 0; i < weight; i++) pool.add(drop);
        }

        Random random = new Random();
        ItemDrop selected = pool.get(random.nextInt(pool.size()));
        ItemStack stack = new ItemStack(selected.item(), random.nextInt(1, selected.count() + 1));

        BlockPos blockPos = BlockPos.containing(pos);
        Block.popResource(level, blockPos, stack);
        ParticleUtil.spawnParticles(level, LUCK_DUST, pos.add(0, .25, 0), 55, .4, .4, .4, 0);
    }

    // -------------------------------------------------------------------------
    // Unluck helpers
    // -------------------------------------------------------------------------

    private static void applyRandomHarmfulEffect(LivingEntity entity, ServerLevel level) {
        Holder<MobEffect>[] effects = new Holder[]{
                MobEffects.WEAKNESS, MobEffects.HUNGER, MobEffects.POISON, MobEffects.WITHER,
                MobEffects.BLINDNESS, MobEffects.MOVEMENT_SLOWDOWN, MobEffects.DIG_SLOWDOWN
        };
        entity.addEffect(new MobEffectInstance(effects[new Random().nextInt(effects.length)], 100, 0, false, true, true));
        ParticleUtil.spawnParticles(level, UNLUCK_DUST, entity.position().add(0, entity.getEyeHeight() / 2, 0), 30, .4, entity.getEyeHeight() / 2, .4, 0);
    }

    private static void tripAndTakeDamage(LivingEntity entity, ServerLevel level, int magnitude) {
        UUID uuid = entity.getUUID();
        long now = System.currentTimeMillis();
        if (lastTripTime.containsKey(uuid) && now - lastTripTime.get(uuid) < 2000) return;
        lastTripTime.put(uuid, now);
        float scalable_damage = (float) (Math.abs(magnitude) *0.0077+ 3.3088);
        // scalable_damage = 20+(Math.abs(magnitude)*0.018);
        float damage = (float) lerpClamped(magnitude, 0, 3000, 5, scalable_damage);
        entity.hurt(ModDamageTypes.source(level, ModDamageTypes.UNLUCK), damage);

        Random random = new Random();
        entity.setDeltaMovement(random.nextDouble(-0.2, 0.2), 0.1, random.nextDouble(-0.2, 0.2));
        entity.hurtMarked = true;

        ParticleUtil.spawnParticles(level, UNLUCK_DUST, entity.position(), 40, .5, .2, .5, 0);
        level.playSound(null, entity.blockPosition(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 0.7f, 0.8f);

        if (entity instanceof ServerPlayer player)
            player.connection.send(new ClientboundSetActionBarTextPacket(
                    Component.literal("You tripped!").withStyle(s -> s.withColor(0xA17234))
            ));
    }

    private static void spawnHostileMob(LivingEntity entity, ServerLevel level, int magnitude) {
        UUID uuid = entity.getUUID();
        long now = System.currentTimeMillis();
        if (lastSpawnTime.containsKey(uuid) && now - lastSpawnTime.get(uuid) < 15000) return;
        lastSpawnTime.put(uuid, now);

        EntityType<?>[] mobs = {EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER, EntityType.CREEPER};
        Random random = new Random();
        Vec3 pos = entity.position().add(random.nextDouble(-5, 5), 0, random.nextDouble(-5, 5));

        Entity mob = mobs[random.nextInt(mobs.length)].create(level);
        if (mob == null) return;

        mob.moveTo(pos.x, pos.y, pos.z, random.nextFloat() * 360, 0);
        level.addFreshEntity(mob);

        ParticleUtil.spawnParticles(level, UNLUCK_DUST, pos, 40, .5, 1, .5, 0);
        level.playSound(null, BlockPos.containing(pos), SoundEvents.PORTAL_AMBIENT, SoundSource.HOSTILE, 0.5f, 0.5f);
    }

    private static void dropRandomInventoryItem(LivingEntity entity, ServerLevel level) {
        if (!(entity instanceof ServerPlayer player)) return;

        List<ItemStack> valid = new ArrayList<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && !stack.is(Items.AIR)) valid.add(stack);
        }
        if (valid.isEmpty()) return;

        ItemStack dropped = valid.get(level.getRandom().nextInt(valid.size())).split(1);
        level.addFreshEntity(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), dropped));
        ParticleUtil.spawnParticles(level, UNLUCK_DUST, entity.position().add(0, entity.getEyeHeight() / 2, 0), 20, .3, .3, .3, 0);
    }

    private static void makeEntitySlip(LivingEntity entity, ServerLevel level) {
        UUID uuid = entity.getUUID();
        long now = System.currentTimeMillis();
        if (lastSlipTime.containsKey(uuid) && now - lastSlipTime.get(uuid) < 3000) return;
        lastSlipTime.put(uuid, now);

        Random random = new Random();
        Vec3 vel = entity.getDeltaMovement();
        entity.setDeltaMovement(vel.x * 1.5 + random.nextDouble(-0.3, 0.3), -0.2, vel.z * 1.5 + random.nextDouble(-0.3, 0.3));
        entity.hurtMarked = true;

        ParticleUtil.spawnParticles(level, UNLUCK_DUST, entity.position(), 30, .5, .1, .5, 0);
    }
/* For some reason putting minus there breaks all unluck and without minus it just buffs the target...
    private static void reduceMultiplierTemporarily(LivingEntity entity, ServerLevel level, int magnitude) {
        UUID uuid = entity.getUUID();
        long now = System.currentTimeMillis();
        if (lastMultiplierReductionTime.containsKey(uuid) && now - lastMultiplierReductionTime.get(uuid) < 8000) return;
        lastMultiplierReductionTime.put(uuid, now);

        double reduction = lerpClamped(magnitude, 0, 3000, 0.4, 0.7);
        int duration = (int) lerpClamped(magnitude, 0, 3000, 3000, 6000);

        BeyonderData.addModifierWithTimeLimit(entity, "unluck_multiplier_reduction", reduction, duration);
        ParticleUtil.spawnParticles(level, UNLUCK_DUST, entity.position().add(0, entity.getEyeHeight() / 2, 0), 40, .4, entity.getEyeHeight() / 2, .4, 0);
    }*/

    private static void disableAbilitiesTemporarily(LivingEntity entity, ServerLevel level, int magnitude) {
        UUID uuid = entity.getUUID();
        long now = System.currentTimeMillis();
        if (lastAbilityDisableTime.containsKey(uuid) && now - lastAbilityDisableTime.get(uuid) < 5000) return;
        lastAbilityDisableTime.put(uuid, now);
        double entityMultiplier = Math.max(BeyonderData.getMultiplier(entity)/2,1);
        int duration = (int) (lerpClamped(magnitude, 0, 3000, 2000, 5000))/(int) entityMultiplier;

        DisabledAbilitiesComponent component = entity.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
        component.disableAbilityUsageForTime("unluck_ability_disabled", duration, entity);

        ParticleUtil.spawnParticles(level, UNLUCK_DUST, entity.position().add(0, entity.getEyeHeight() / 2, 0), 50, .5, entity.getEyeHeight() / 2, .5, 0);
        level.playSound(null, entity.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.5f, 0.5f);
    }

    // -------------------------------------------------------------------------
    // Probability scaling — luck and magnitude are always positive [0, 3000]
    // -------------------------------------------------------------------------

    private static double getCritChance(int luck) {
        return lerpClamped(luck, 0, 3000, 0.04, 0.90);
    }

    private static double getDodgeChance(int luck) {
        return lerpClamped(luck, 0, 3000, 0.035, 0.65);
    }

    private static double getMultipleBlocksChance(int luck) {
        return lerpClamped(luck, 0, 3000, 0.10, 0.99);
    }

    private static double getChanceForPotionEffectRemoval(int luck) {
        return lerpClamped(luck, 0, 3000, 0.0025, 0.05);
    }

    private static double getChanceForEntityTrip(int luck) {
        return lerpClamped(luck, 0, 3000, 0.002, 0.035);
    }

    private static double getChanceForRandomDrop(int luck) {
        return lerpClamped(luck, 0, 3000, 0.01, 0.20);
    }

    private static double getExtraDamageChance(int magnitude) {
        return lerpClamped(magnitude, 0, 3000, 0.06, 0.85);
    }

    private static double getWeakHitChance(int magnitude) {
        return lerpClamped(magnitude, 0, 3000, 0.07, 0.90);
    }

    private static double getItemDestroyChance(int magnitude) {
        return lerpClamped(magnitude, 0, 3000, 0.03, 0.40);
    }

    private static double getToolDamageChance(int magnitude) {
        return lerpClamped(magnitude, 0, 3000, 0.08, 0.60);
    }

    private static double getHarmfulEffectChance(int magnitude) {
        return lerpClamped(magnitude, 0, 3000, 0.001, 0.04);
    }

    private static double getMobSpawnChance(int magnitude) {
        return lerpClamped(magnitude, 0, 3000, 0.0005, 0.008);
    }

    private static double getItemDropChance(int magnitude) {
        return lerpClamped(magnitude, 0, 3000, 0.0008, 0.012);
    }

    private static double getSlipChance(int magnitude) {
        return lerpClamped(magnitude, 0, 3000, 0.002, 0.025);
    }

    /*
    private static double getMultiplierReductionChance(int magnitude) {
        return lerpClamped(magnitude, 0, 3000, 0.0008, 0.010);
    }
    */

    private static double getAbilityDisableChance(int magnitude) {
        return lerpClamped(magnitude, 0, 3000, 0.0003, 0.005);
    }

    private static double getTripChance(int magnitude) {
        return lerpClamped(magnitude, 0, 3000, 0.0005, 0.015);
    }

    // -------------------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------------------

    private static int getLuck(LivingEntity entity) {
        return entity.getData(ModAttachments.LUCK_COMPONENT).getLuck();
    }

    private static double lerpClamped(double value, double minIn, double maxIn, double minOut, double maxOut) {
        double t = Math.max(0.0, Math.min(1.0, (value - minIn) / (maxIn - minIn)));
        return minOut + t * (maxOut - minOut);
    }

    private static void sendActionBar(ServerPlayer player, Component message) {
        player.connection.send(new ClientboundSetActionBarTextPacket(message));
    }

    private record ItemDrop(Item item, int count, double dropChance) {}

    private record CombatTarget(LivingEntity target, long timestamp) {}
}
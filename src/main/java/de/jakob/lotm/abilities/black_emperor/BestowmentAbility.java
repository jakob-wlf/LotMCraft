package de.jakob.lotm.abilities.black_emperor;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PhysicalEnhancementsAbility;
import de.jakob.lotm.abilities.core.AbilityUseEvent;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.RingEffectManager;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class BestowmentAbility extends SelectableAbility {

    // How long the target is forced into each state.
    // Tweak these if you want the effects to last longer or shorter.
    private static final String ANXIOUS_UNTIL_KEY = "lotm_bestowment_anxious_until";
    private static final String SEALED_UNTIL_KEY = "lotm_bestowment_sealed_until";
    private static final String RASH_UNTIL_KEY = "lotm_bestowment_rash_until";
    private static final String SLUGGISH_NEXT_CAST_UNTIL_KEY = "lotm_bestowment_sluggish_next_cast_until";
    private static final String MONEY_UNTIL_KEY = "lotm_bestowment_money_until";
    private static final String MONEY_TARGET_POS_KEY = "lotm_bestowment_money_target";

    // How far the money-focus scan reaches.
    // Increase this if you want the target to search farther for ores.
    private static final int MONEY_SEARCH_RADIUS = 192;

    // How often the target gets re-steered toward the ore.
    // Lower = smoother tracking. Higher = cheaper.
    private static final int MONEY_UPDATE_INTERVAL_TICKS = 5;

    // Walking speed used by mobs when they are drawn toward the ore.
    private static final double MONEY_MOB_WALK_SPEED = 1.0D;

    // Gentle pull for players and other non-mob entities.
    private static final double MONEY_ENTITY_PULL = 0.045D;

    public BestowmentAbility(String id) {
        super(id, 45.0f);
        canBeCopied = false;
        canBeReplicated = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("black_emperor", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 0;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.bestowment.money_focus",
                "ability.lotmcraft.bestowment.rash",
                "ability.lotmcraft.bestowment.sluggish",
                "ability.lotmcraft.bestowment.anxiety",
                "ability.lotmcraft.bestowment.will_to_fight"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        BeyonderData.reduceSpirituality(entity, BeyonderData.getMaxSpirituality(BeyonderData.getPathway(entity), BeyonderData.getSequence(entity)) * 0.20f);

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 15, 1.5f);
        if (target == null) {
            AbilityUtil.sendActionBar(entity,
                    Component.literal("No target in range.").withColor(0xFF5555));
            return;
        }

        if (!canBestow(entity, target)) {
            AbilityUtil.sendActionBar(entity,
                    Component.literal("Target resists Bestowment.").withColor(0xFF5555));
            return;
        }

        switch (abilityIndex) {
            case 0 -> bestowMoneyFocus(serverLevel, entity, target);
            case 1 -> bestowRash(serverLevel, entity, target);
            case 2 -> bestowSluggish(serverLevel, entity, target);
            case 3 -> bestowAnxiety(serverLevel, entity, target);
            case 4 -> bestowWillToFightSeal(serverLevel, entity, target);
            default -> {
            }
        }
    }

    /**
     * Making the target only focused on money.
     * Target ignores everything and moves toward the nearest ore at a walking pace.
     */
    private static final int MONEY_TOOL_BREAK_LIMIT = 2;
    private static final double MONEY_TOOL_STEP = 0.85D;

    private void bestowMoneyFocus(ServerLevel level, LivingEntity caster, LivingEntity target) {
        int duration = BlackEmperorProgression.scaleTicks(caster, 20 * 20, 40, 20 * 40);
        long until = level.getGameTime() + duration;

        target.getPersistentData().putLong(MONEY_UNTIL_KEY, until);

        BlockPos ore = findNearestMoneyOre(level, target, MONEY_SEARCH_RADIUS);
        if (ore == null) {
            AbilityUtil.sendActionBar(caster,
                    Component.literal("No ore found nearby.").withColor(0xFF5555));
            target.getPersistentData().remove(MONEY_UNTIL_KEY);
            return;
        }

        target.getPersistentData().putLong(MONEY_TARGET_POS_KEY, ore.asLong());

        RingEffectManager.createRingForAll(target.position(), 2.8f, 20,
                0.70f, 0.38f, 0.88f, 1.0f, 0.16f, 0.8f, level);
        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                target.position().add(0, 1, 0), 1.1f, 16);

        ServerScheduler.scheduleForDuration(
                0,
                MONEY_UPDATE_INTERVAL_TICKS,
                duration,
                () -> {
                    if (!target.isAlive()) return;

                    long stored = target.getPersistentData().getLong(MONEY_TARGET_POS_KEY);
                    BlockPos currentOre = BlockPos.of(stored);

                    if (level.getBlockState(currentOre).isAir()) {
                        BlockPos nextOre = findNearestMoneyOre(level, target, MONEY_SEARCH_RADIUS);
                        if (nextOre != null) {
                            currentOre = nextOre;
                            target.getPersistentData().putLong(MONEY_TARGET_POS_KEY, currentOre.asLong());
                        } else {
                            return;
                        }
                    }

                    Vec3 oreCenter = Vec3.atCenterOf(currentOre);
                    Vec3 toOre = oreCenter.subtract(target.position());
                    if (toOre.lengthSqr() < 0.01D) return;

                    Vec3 dir = toOre.normalize();

                    if (target instanceof ServerPlayer player) {
                        clearMoneyPath(level, player, dir);

                        player.setDeltaMovement(player.getDeltaMovement().add(
                                dir.x * 0.08D,
                                0.0D,
                                dir.z * 0.08D
                        ));
                        player.hasImpulse = true;
                        player.hurtMarked = true;

                        AbilityUtil.sendActionBar(player,
                                Component.literal("Money... ore... treasure...").withColor(0xAA77FF));
                    } else if (target instanceof Mob mob) {
                        mob.setTarget(null);
                        mob.getNavigation().moveTo(
                                oreCenter.x,
                                oreCenter.y,
                                oreCenter.z,
                                MONEY_MOB_WALK_SPEED
                        );
                    } else {
                        target.setDeltaMovement(target.getDeltaMovement().add(
                                dir.x * MONEY_ENTITY_PULL,
                                dir.y * 0.02D,
                                dir.z * MONEY_ENTITY_PULL
                        ));
                        target.hurtMarked = true;
                    }
                },
                () -> {
                    target.getPersistentData().remove(MONEY_TARGET_POS_KEY);
                    target.getPersistentData().remove(MONEY_UNTIL_KEY);
                },
                level
        );
    }

    private void clearMoneyPath(ServerLevel level, LivingEntity target, Vec3 dir) {
        Vec3 eye = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);

        int broken = 0;

        BlockPos feet = target.blockPosition();
        broken += tryBreakMoneyBlock(level, target, feet) ? 1 : 0;
        if (broken < MONEY_TOOL_BREAK_LIMIT) {
            broken += tryBreakMoneyBlock(level, target, feet.above()) ? 1 : 0;
        }

        double[] steps = {MONEY_TOOL_STEP, MONEY_TOOL_STEP * 2.0D, MONEY_TOOL_STEP * 3.0D};
        for (double step : steps) {
            if (broken >= MONEY_TOOL_BREAK_LIMIT) break;

            BlockPos front = BlockPos.containing(eye.add(dir.scale(step)));
            broken += tryBreakMoneyBlock(level, target, front) ? 1 : 0;

            if (broken >= MONEY_TOOL_BREAK_LIMIT) break;
            broken += tryBreakMoneyBlock(level, target, front.above()) ? 1 : 0;
        }
    }

    private boolean tryBreakMoneyBlock(ServerLevel level, LivingEntity breaker, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return false;
        if (state.getDestroySpeed(level, pos) < 0.0F) return false;

        return level.destroyBlock(pos, true, breaker);
    }

    /**
     * Turning the target eager and rash.
     * Spams random abilities and adds a direct damage pulse so allies can still be hurt.
     */
    private void bestowRash(ServerLevel level, LivingEntity caster, LivingEntity target) {
        int duration = BlackEmperorProgression.scaleTicks(caster, 20 * 10, 20, 20 * 20);
        target.getPersistentData().putLong(RASH_UNTIL_KEY, level.getGameTime() + duration);

        RingEffectManager.createRingForAll(target.position(), 2.6f, 18,
                0.76f, 0.18f, 0.92f, 1.0f, 0.15f, 0.9f, level);
        ParticleUtil.createParticleSpirals(level, ModParticles.LIGHTNING.get(),
                target.position().add(0, 0.5, 0),
                0.25, 0.9, 1.4, 0.07, 8, 24, 2, 2);

        ServerScheduler.scheduleForDuration(
                0, 8, duration,
                () -> {
                    if (!target.isAlive()) return;

                    long now = level.getGameTime();

                    target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 30, 0, false, false, true));
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 1, false, false, true));

                    target.setDeltaMovement(target.getDeltaMovement().add(
                            (level.random.nextDouble() - 0.5D) * 0.18D,
                            0.0D,
                            (level.random.nextDouble() - 0.5D) * 0.18D
                    ));

                    if (now % 140 == 0) {
                        dropRandomEquipment(level, target);
                    }

                    if (now % 80 == 0) {
                        LivingEntity pulseVictim = findNearestLiving(level, target, 10);
                        if (pulseVictim != null && pulseVictim != target) {
                            float pulseDamage = BlackEmperorProgression.scaleFloat(target, 1.5f, 0.4f, 4.0f);
                            pulseVictim.hurt(target.damageSources().mobAttack(target), pulseDamage);
                        }
                    }
                },
                () -> target.getPersistentData().remove(RASH_UNTIL_KEY),
                level
        );
    }

    private void dropRandomEquipment(ServerLevel level, LivingEntity target) {
        List<net.minecraft.world.entity.EquipmentSlot> candidates = new java.util.ArrayList<>();

        for (net.minecraft.world.entity.EquipmentSlot slot : new net.minecraft.world.entity.EquipmentSlot[]{
                net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                net.minecraft.world.entity.EquipmentSlot.OFFHAND,
                net.minecraft.world.entity.EquipmentSlot.HEAD,
                net.minecraft.world.entity.EquipmentSlot.CHEST,
                net.minecraft.world.entity.EquipmentSlot.LEGS,
                net.minecraft.world.entity.EquipmentSlot.FEET
        }) {
            if (!target.getItemBySlot(slot).isEmpty()) {
                candidates.add(slot);
            }
        }

        if (candidates.isEmpty()) return;

        net.minecraft.world.entity.EquipmentSlot chosen = candidates.get(level.random.nextInt(candidates.size()));
        net.minecraft.world.item.ItemStack item = target.getItemBySlot(chosen).copy();
        target.setItemSlot(chosen, net.minecraft.world.item.ItemStack.EMPTY);
        target.spawnAtLocation(item);
    }

    /**
     * The feeling of sluggishness.
     * Reduces spirituality, cancels regeneration, and slows the target.
     */
    private void bestowSluggish(ServerLevel level, LivingEntity caster, LivingEntity target) {
        int duration = BlackEmperorProgression.scaleTicks(caster, 20 * 12, 12, 20 * 18);
        long until = level.getGameTime() + duration;

        target.getPersistentData().putLong(SLUGGISH_NEXT_CAST_UNTIL_KEY, until);

        // Suppress passive regeneration for the full duration
        PhysicalEnhancementsAbility.suppressRegen(target, duration * 50L);

        RingEffectManager.createRingForAll(target.position(), 2.3f, 18,
                0.56f, 0.12f, 0.72f, 1.0f, 0.16f, 0.8f, level);
        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                target.position().add(0, 1, 0), 0.9f, 14);

        ServerScheduler.scheduleForDuration(
                0, 40, duration,
                () -> {
                    if (!target.isAlive()) return;
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, false, true));
                },
                () -> target.getPersistentData().remove(SLUGGISH_NEXT_CAST_UNTIL_KEY),
                level
        );

        ServerScheduler.scheduleForDuration(
                0, 50, duration,
                () -> {
                    if (!target.isAlive()) return;
                    BeyonderData.reduceSpirituality(target, BlackEmperorProgression.scaleFloat(target, 0.08f, 0.02f, 0.18f));
                },
                () -> target.getPersistentData().remove(SLUGGISH_NEXT_CAST_UNTIL_KEY),
                level
        );

        AbilityUtil.sendActionBar(caster,
                Component.literal("Sluggishness bestowed.").withColor(0xAA77FF));
    }

    /**
     * Making the target anxious.
     * Uses the mod's existing losing-control effect so sanity drops naturally.
     */
    private void bestowAnxiety(ServerLevel level, LivingEntity caster, LivingEntity target) {
        int duration = 20 * 10;
        target.getPersistentData().putLong(ANXIOUS_UNTIL_KEY, level.getGameTime() + duration);

        RingEffectManager.createRingForAll(target.position(), 2.4f, 18,
                0.62f, 0.18f, 0.78f, 1.0f, 0.16f, 0.85f, level);
        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                target.position().add(0, 1, 0), 1.0f, 16);

        ServerScheduler.scheduleForDuration(
                0, 20, duration,
                () -> {
                    if (!target.isAlive()) return;
                    target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 40, 0, false, false, true));
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, false, true));
                    var sanity = target.getData(ModAttachments.SANITY_COMPONENT);
                    sanity.increaseSanityAndSync(-0.02f, target);
                },
                () -> target.getPersistentData().remove(ANXIOUS_UNTIL_KEY),
                level
        );

        AbilityUtil.sendActionBar(caster,
                Component.literal("Anxiety bestowed.").withColor(0xAA77FF));
    }

    /**
     * Losing the will to fight.
     * Seals Beyonder abilities for a short period.
     */
    private void bestowWillToFightSeal(ServerLevel level, LivingEntity caster, LivingEntity target) {
        int duration = BlackEmperorProgression.scaleTicks(caster, 20 * 10, 20, Integer.MAX_VALUE);
        target.getPersistentData().putLong(SEALED_UNTIL_KEY, level.getGameTime() + duration);

        RingEffectManager.createRingForAll(target.position(), 2.5f, 20,
                0.68f, 0.14f, 0.82f, 1.0f, 0.18f, 0.9f, level);
        ParticleUtil.spawnSphereParticles(level, ModParticles.LIGHTNING.get(),
                target.position().add(0, 1, 0), 1.0f, 18);

        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 1, false, false, true));

        AbilityUtil.sendActionBar(caster,
                Component.literal("Will to fight sealed.").withColor(0xAA77FF));

        ServerScheduler.scheduleDelayed(duration, () ->
                target.getPersistentData().remove(SEALED_UNTIL_KEY), level);
    }

    @SubscribeEvent
    public static void onAbilityUse(AbilityUseEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null || entity.level().isClientSide) return;

        long now = entity.level().getGameTime();
        long sealedUntil = entity.getPersistentData().getLong(SEALED_UNTIL_KEY);
        if (sealedUntil > now) {
            event.setCanceled(true);
            AbilityUtil.sendActionBar(entity,
                    Component.literal("Your Beyonder abilities are sealed.").withColor(0xFF5555));
            return;
        }

    }

    private boolean canBestow(LivingEntity caster, LivingEntity target) {
        if (!BeyonderData.isBeyonder(target)) return true;

        int casterSeq = BeyonderData.getSequence(caster);
        int targetSeq = BeyonderData.getSequence(target);

        if (targetSeq >= casterSeq) return true;
        if (targetSeq == 0 && casterSeq != 0) return false;

        int diff = casterSeq - targetSeq;
        if (diff == 1) return random.nextFloat() < 0.40f;
        return false;
    }

    private static BlockPos findNearestMoneyOre(Level level, LivingEntity entity, int radius) {
        BlockPos origin = entity.blockPosition();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);

                    if (!isMoneyOre(state)) continue;

                    double dist = pos.distSqr(origin);
                    if (dist < bestDist) {
                        bestDist = dist;
                        best = pos.immutable();
                    }
                }
            }
        }

        return best;
    }

    private static boolean isMoneyOre(BlockState state) {
        return state.is(Blocks.IRON_ORE)
                || state.is(Blocks.DEEPSLATE_IRON_ORE)
                || state.is(Blocks.GOLD_ORE)
                || state.is(Blocks.DEEPSLATE_GOLD_ORE)
                || state.is(Blocks.DIAMOND_ORE)
                || state.is(Blocks.DEEPSLATE_DIAMOND_ORE);
    }

    private static LivingEntity findNearestLiving(Level level, LivingEntity source, int radius) {
        return level.getEntitiesOfClass(
                        LivingEntity.class,
                        source.getBoundingBox().inflate(radius),
                        e -> e != source && e.isAlive()
                ).stream()
                .min((a, b) -> Double.compare(a.distanceToSqr(source), b.distanceToSqr(source)))
                .orElse(null);
    }
}
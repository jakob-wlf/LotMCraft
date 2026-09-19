package de.jakob.lotm.dimension;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.fluid.ModFluids;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public final class UnderworldTouchOfDeathHandler {
    private static final String EXPOSURE_TICKS_KEY = "lotm_underworld_touch_exposure";
    private static final String LAST_STYX_STACK_KEY = "lotm_last_styx_touch_stack";
    private static final String DEAD_STATE_KEY = "lotm_dead_state";
    private static final int BASE_INTERVAL_TICKS = 20 * 60 * 5;
    private static final int STYX_COOLDOWN_TICKS = 20 * 60;
    private static final int TOUCH_DURATION_TICKS = 20 * 60 * 30;
    private static final int MAX_STACKS = 5;

    private UnderworldTouchOfDeathHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide || event.getEntity().tickCount % 20 != 0) return;

        var player = event.getEntity();
        if (player.getPersistentData().getBoolean(DEAD_STATE_KEY)) {
            if (!player.hasEffect(ModEffects.DEAD)) {
                player.addEffect(new MobEffectInstance(ModEffects.DEAD,
                        Integer.MAX_VALUE, 0, false, true, true));
            }
            return;
        }
        if (!player.level().dimension().equals(ModDimensions.UNDERWORLD_DIMENSION_KEY)) {
            player.getPersistentData().remove(EXPOSURE_TICKS_KEY);
            return;
        }

        int interval = getEnvironmentalInterval(player);
        if (interval == Integer.MAX_VALUE) return;

        int exposure = player.getPersistentData().getInt(EXPOSURE_TICKS_KEY) + 20;
        if (exposure >= interval) {
            exposure = 0;
            addStack(player);
        }
        player.getPersistentData().putInt(EXPOSURE_TICKS_KEY, exposure);

        if (isInStyxWater(player)) {
            long now = player.level().getGameTime();
            long lastStack = player.getPersistentData().getLong(LAST_STYX_STACK_KEY);
            if (now - lastStack >= STYX_COOLDOWN_TICKS) {
                player.getPersistentData().putLong(LAST_STYX_STACK_KEY, now);
                addStack(player);
            }
        }
    }

    public static boolean applyFromDeathBeyonder(LivingEntity target, LivingEntity caster) {
        if (!"death".equals(BeyonderData.getPathway(caster))) return false;
        if ("death".equals(BeyonderData.getPathway(target))
                && BeyonderData.getSequence(caster) >= BeyonderData.getSequence(target)) return false;
        addStack(target);
        return true;
    }

    public static boolean isFullyUndead(LivingEntity entity) {
        return entity.getPersistentData().getBoolean(DEAD_STATE_KEY);
    }

    public static void placate(LivingEntity entity) {
        entity.getPersistentData().remove(DEAD_STATE_KEY);
        entity.getPersistentData().remove(EXPOSURE_TICKS_KEY);
        entity.getPersistentData().remove(LAST_STYX_STACK_KEY);
        entity.removeEffect(ModEffects.TOUCH_OF_DEATH);
        entity.removeEffect(ModEffects.DEAD);
    }

    private static int getEnvironmentalInterval(LivingEntity entity) {
        if (!"death".equals(BeyonderData.getPathway(entity))) return BASE_INTERVAL_TICKS;

        int sequence = BeyonderData.getSequence(entity);
        if (sequence <= 4) return Integer.MAX_VALUE;
        if (sequence == 5) return BASE_INTERVAL_TICKS * 3;
        if (sequence == 6 && isZombieDisguiseActive(entity)) return BASE_INTERVAL_TICKS * 2;
        return BASE_INTERVAL_TICKS;
    }

    private static boolean isZombieDisguiseActive(LivingEntity entity) {
        return LOTMCraft.abilityHandler.getById("zombie_disguise_ability") instanceof ToggleAbility ability
                && ability.isActiveForEntity(entity);
    }

    private static void addStack(LivingEntity entity) {
        if (entity.hasEffect(ModEffects.DEAD)) return;
        MobEffectInstance current = entity.getEffect(ModEffects.TOUCH_OF_DEATH);
        int stacks = current == null ? 1 : Math.min(MAX_STACKS, current.getAmplifier() + 2);
        if (stacks >= MAX_STACKS) {
            entity.removeEffect(ModEffects.TOUCH_OF_DEATH);
            entity.getPersistentData().putBoolean(DEAD_STATE_KEY, true);
            entity.addEffect(new MobEffectInstance(ModEffects.DEAD,
                Integer.MAX_VALUE, 0, false, true, true));
            entity.sendSystemMessage(Component.literal(
                "You have become Dead, a fully undead creature. Only Placate can restore you.")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        entity.addEffect(new MobEffectInstance(ModEffects.TOUCH_OF_DEATH,
            TOUCH_DURATION_TICKS, stacks - 1, false, true, true));
    }

    private static boolean isInStyxWater(LivingEntity entity) {
        return isStyx(entity.level().getFluidState(entity.blockPosition()))
                || isStyx(entity.level().getFluidState(BlockPos.containing(
                        entity.getX(), entity.getEyeY(), entity.getZ())));
    }

    private static boolean isStyx(FluidState state) {
        return state.is(ModFluids.WATER_OF_THE_RIVER_STYX_SOURCE.get())
                || state.is(ModFluids.WATER_OF_THE_RIVER_STYX_FLOWING.get());
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (!isFullyUndead(target)) return;

        boolean purification = event.getSource().is(ModDamageTypes.PURIFICATION)
                || event.getSource().is(ModDamageTypes.PURIFICATION_INDIRECT);
        boolean fire = event.getSource().is(DamageTypeTags.IS_FIRE);
        Entity sourceEntity = event.getSource().getEntity();
        boolean sun = sourceEntity instanceof LivingEntity attacker
                && "sun".equals(BeyonderData.getPathway(attacker));
        boolean smite = sourceEntity instanceof LivingEntity attacker
                && attacker.getMainHandItem().getEnchantmentLevel(
                        attacker.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                                .getHolderOrThrow(Enchantments.SMITE)) > 0;

        if (purification) event.setAmount(event.getAmount() * 2.0f);
        else if (sun || smite || fire) event.setAmount(event.getAmount() * 1.5f);
    }
}
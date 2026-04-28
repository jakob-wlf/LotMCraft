package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class WorldJudgmentHandler {

    /** Caster UUID → judged entity UUID */
    public static final Map<UUID, UUID> JUDGED_BY_CASTER = new ConcurrentHashMap<>();
    /** Judged entity UUID → current violation tier (0 = designated, not yet violated) */
    public static final Map<UUID, Integer> JUDGMENT_TIER = new ConcurrentHashMap<>();

    /**
     * Call at each prohibition or law violation point.
     * Increments the tier and applies escalating punishment to the judged entity.
     */
    public static void escalate(LivingEntity entity) {
        UUID uuid = entity.getUUID();
        if (!JUDGMENT_TIER.containsKey(uuid)) return;
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        int tier = JUDGMENT_TIER.merge(uuid, 1, Integer::sum);
        applyTier(entity, serverLevel, tier);
    }

    private static void applyTier(LivingEntity entity, ServerLevel serverLevel, int tier) {
        if (tier == 1) {
            entity.hurt(serverLevel.damageSources().magic(), 15f);
            notify(entity, Component.translatable("lotmcraft.world_judgment.tier_1"));
        } else if (tier == 2) {
            entity.hurt(serverLevel.damageSources().magic(), 15f);
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 10, 3, false, true));
            notify(entity, Component.translatable("lotmcraft.world_judgment.tier_2"));
        } else if (tier == 3) {
            entity.hurt(serverLevel.damageSources().magic(), 15f);
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 10, 3, false, true));
            // Strip all beneficial effects
            List<Holder<MobEffect>> toRemove = entity.getActiveEffects().stream()
                    .filter(e -> e.getEffect().value().isBeneficial())
                    .map(MobEffectInstance::getEffect)
                    .toList();
            toRemove.forEach(entity::removeEffect);
            notify(entity, Component.translatable("lotmcraft.world_judgment.tier_3"));
        } else if (tier == 4) {
            entity.hurt(serverLevel.damageSources().magic(), 15f);
            // Paralysis: extreme slowness + levitation prevents all movement
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 10, 127, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20 * 10, 0, false, true));
            notify(entity, Component.translatable("lotmcraft.world_judgment.tier_4"));
        } else {
            // Tier 5+: death
            entity.hurt(serverLevel.damageSources().magic(), Float.MAX_VALUE);
            notify(entity, Component.translatable("lotmcraft.world_judgment.tier_5"));
        }
    }

    private static void notify(LivingEntity entity, Component msg) {
        if (entity instanceof ServerPlayer sp) {
            sp.sendSystemMessage(msg.copy().withStyle(ChatFormatting.DARK_RED));
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        UUID uuid = event.getEntity().getUUID();
        if (JUDGMENT_TIER.remove(uuid) != null) {
            JUDGED_BY_CASTER.values().remove(uuid);
        }
    }
}

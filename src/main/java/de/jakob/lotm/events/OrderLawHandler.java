package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.justiciar.OrderProxyAbility;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class OrderLawHandler {

    private static final Random RANDOM = new Random();
    private static int tickCounter = 0;

    private static final List<Holder<MobEffect>> PUNISHMENT_EFFECTS = List.of(
            MobEffects.BLINDNESS,
            MobEffects.CONFUSION,
            MobEffects.DARKNESS,
            MobEffects.DIG_SLOWDOWN,
            MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.POISON,
            MobEffects.WEAKNESS,
            MobEffects.WITHER,
            MobEffects.HUNGER,
            MobEffects.LEVITATION,
            ModEffects.LOOSING_CONTROL
    );

    // ── Prohibition: Dying — floor health at 1 ────────────────────────────────

    @SubscribeEvent
    public static void onLivingDamageDyingProhibition(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;
        if (OrderProxyAbility.PERMANENT_PROHIBITION_ZONES.isEmpty()) return;

        Vec3 pos = entity.position();
        boolean inDyingZone = OrderProxyAbility.PERMANENT_PROHIBITION_ZONES.stream()
                .anyMatch(z -> z.type() == OrderProxyAbility.OrderProhibitionType.DYING && z.isInZone(pos, serverLevel));
        if (!inDyingZone) return;

        float currentHealth = entity.getHealth();
        if (event.getNewDamage() >= currentHealth) {
            event.setNewDamage(currentHealth - 1f);
            if (entity instanceof ServerPlayer sp) {
                sp.sendSystemMessage(Component.translatable("lotmcraft.order_law.dying_prohibited")
                        .withStyle(ChatFormatting.GOLD));
            }
        }
    }

    // ── Prohibition: Resurrecting — block revivals ────────────────────────────

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;
        if (OrderProxyAbility.PERMANENT_PROHIBITION_ZONES.isEmpty()) return;

        Vec3 pos = entity.position();
        boolean inResurrectingZone = OrderProxyAbility.PERMANENT_PROHIBITION_ZONES.stream()
                .anyMatch(z -> z.type() == OrderProxyAbility.OrderProhibitionType.RESURRECTING && z.isInZone(pos, serverLevel));
        if (!inResurrectingZone) return;

        UUID uuid = entity.getUUID();
        OrderProxyAbility.NO_REVIVAL_ENTITIES.add(uuid);
        ServerScheduler.scheduleDelayed(20 * 30, () -> OrderProxyAbility.NO_REVIVAL_ENTITIES.remove(uuid));
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (OrderProxyAbility.NO_REVIVAL_ENTITIES.contains(event.getEntity().getUUID())) {
            event.setCanceled(true);
        }
    }

    // ── Prohibition: Demigods — expel Seq 3 or lower ─────────────────────────

    @SubscribeEvent
    public static void onPlayerTickDemigodsProhibition(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        if (OrderProxyAbility.PERMANENT_PROHIBITION_ZONES.isEmpty()) return;
        if (!BeyonderData.isBeyonder(player)) return;
        if (BeyonderData.getSequence(player) > 3) return;

        Vec3 pos = player.position();
        for (OrderProxyAbility.PermanentProhibitionZone zone : OrderProxyAbility.PERMANENT_PROHIBITION_ZONES) {
            if (zone.type() != OrderProxyAbility.OrderProhibitionType.DEMIGODS) continue;
            if (!zone.level().equals(serverLevel)) continue;
            if (zone.ownerId().equals(player.getUUID())) continue;

            double dist = pos.distanceTo(zone.center());
            if (dist <= OrderProxyAbility.PROHIBITION_RADIUS) {
                Vec3 direction = pos.subtract(zone.center()).normalize();
                if (direction.lengthSqr() < 0.001) direction = new Vec3(1, 0, 0);
                player.setDeltaMovement(direction.scale(1.5));
                player.hurtMarked = true;
                player.sendSystemMessage(Component.translatable("lotmcraft.order_law.demigods_prohibited")
                        .withStyle(ChatFormatting.RED));
            }
        }
    }

    // ── Law: Combat — block damage and punish attacker ────────────────────────

    @SubscribeEvent
    public static void onLivingDamageCombatLaw(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        if (!(attacker.level() instanceof ServerLevel serverLevel)) return;
        if (OrderProxyAbility.PERMANENT_LAW_ZONES.isEmpty()) return;

        Vec3 pos = attacker.position();
        boolean inCombatZone = OrderProxyAbility.PERMANENT_LAW_ZONES.stream()
                .anyMatch(z -> z.type() == OrderProxyAbility.LawType.COMBAT && z.isInZone(pos, serverLevel));
        if (!inCombatZone) return;

        event.setNewDamage(0);
        applyPunishment(attacker);
        WorldJudgmentHandler.escalate(attacker);

        if (attacker instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.translatable("lotmcraft.order_law.combat")
                    .withStyle(ChatFormatting.RED));
        }
    }

    // ── Law: Losing Control — punish entities with LOOSING_CONTROL in zone ───

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        if (tickCounter % 20 != 0) return;
        if (OrderProxyAbility.PERMANENT_LAW_ZONES.isEmpty()) return;

        event.getServer().getAllLevels().forEach(serverLevel -> {
            for (OrderProxyAbility.PermanentLawZone zone : OrderProxyAbility.PERMANENT_LAW_ZONES) {
                if (zone.type() != OrderProxyAbility.LawType.LOSING_CONTROL) continue;
                if (!zone.level().equals(serverLevel)) continue;

                serverLevel.getPlayers(p -> zone.isInZone(p.position(), serverLevel)).forEach(player -> {
                    if (!player.hasEffect(ModEffects.LOOSING_CONTROL)) return;
                    applyPunishment(player);
                    WorldJudgmentHandler.escalate(player);
                    player.sendSystemMessage(Component.translatable("lotmcraft.order_law.losing_control")
                            .withStyle(ChatFormatting.RED));
                });
            }
        });
    }

    // ── Punishment ────────────────────────────────────────────────────────────

    private static void applyPunishment(LivingEntity entity) {
        boolean alreadyPunished = PUNISHMENT_EFFECTS.stream().anyMatch(entity::hasEffect);
        if (alreadyPunished) return;

        Holder<MobEffect> effect = PUNISHMENT_EFFECTS.get(RANDOM.nextInt(PUNISHMENT_EFFECTS.size()));
        int amplifier = RANDOM.nextInt(4);
        entity.addEffect(new MobEffectInstance(effect, 20 * 10, amplifier, false, true));
    }
}

package de.jakob.lotm.beyonders.abilities.black_emperor;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class CommandingOrdersAbility extends ToggleAbility {

    private static final String COMMAND_ORDER_LAST_TICK_KEY = "lotm_commanding_orders_last_tick";
    private static final long COMMAND_ORDER_COOLDOWN_TICKS = 90;
    private static final int COMMAND_ORDER_RANGE = 25;

    private static final Map<UUID, ActiveOrder> ACTIVE_ORDERS = new HashMap<>();
    private static final Map<UUID, Long> REFLECTED_UNTIL = new HashMap<>();
    private static final long REFLECT_DURATION_TICKS = 60L;

    private static final HashSet<UUID> active = new HashSet<>();

    private record ActiveOrder(
            UUID casterId,
            int casterSeq,
            String command,
            long expiresAtTick
    ) {}

    public CommandingOrdersAbility(String id) {
        super(id);
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("black_emperor", 3));
    }

    @Override
    public float getSpiritualityCost() {
        return 10.0f;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        entity.sendSystemMessage(Component.literal("§5Commanding Orders: ON"));
        active.add(entity.getUUID());
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        // No passive aura here.
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        entity.sendSystemMessage(Component.literal("§cCommanding Orders: OFF"));
        active.remove(entity.getUUID());
    }

    public static boolean handleAuthorityChat(LivingEntity caster, String rawMessage) {
        if (caster == null || caster.level().isClientSide) return false;
        if (!(caster.level() instanceof ServerLevel serverLevel)) return false;

        if(!active.contains(caster.getUUID())) return false;

        String commandRaw = rawMessage.trim().toLowerCase(Locale.ROOT);
        if (commandRaw.isEmpty()) return false;

        String command;
        if (commandRaw.startsWith("kneel")) {
            command = "kneel";
        } else if (commandRaw.startsWith("halt") || commandRaw.startsWith("stop")) {
            command = "halt";
        } else if (commandRaw.startsWith("retreat") || commandRaw.startsWith("back")) {
            command = "retreat";
        } else if (commandRaw.startsWith("advance") || commandRaw.startsWith("forward")) {
            command = "advance";
        } else if (commandRaw.startsWith("silence") || commandRaw.startsWith("submit")) {
            command = "silence";
        } else {
            return false;
        }

        long now = serverLevel.getGameTime();
        long lastOrderTick = caster.getPersistentData().getLong(COMMAND_ORDER_LAST_TICK_KEY);
        if (now - lastOrderTick < COMMAND_ORDER_COOLDOWN_TICKS) {
            if (caster instanceof Player player) {
                AbilityUtil.sendActionBar(player,
                        Component.literal("Orders are still settling.").withColor(0xFF5555));
            }
            return true;
        }

        List<ServerPlayer> targets = findNearbyPlayers(serverLevel, caster);
        if (targets.isEmpty()) {
            if (caster instanceof Player player) {
                AbilityUtil.sendActionBar(player,
                        Component.literal("No players in range.").withColor(0xFF5555));
            }
            return true;
        }

        caster.getPersistentData().putLong(COMMAND_ORDER_LAST_TICK_KEY, now);

        int casterSeq = BeyonderData.getSequence(caster);
        applyAuthorityOrder(serverLevel, caster, targets, casterSeq, command);

        if (caster instanceof Player player) {
            AbilityUtil.sendActionBar(player,
                    Component.literal("Order issued: " + command + " (" + targets.size() + " players)")
                            .withColor(0xAA77FF));
        }

        return true;
    }

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        if (handleAuthorityChat(event.getPlayer(), event.getRawText())) {
            event.setCanceled(true);
        }
    }

    private static List<ServerPlayer> findNearbyPlayers(ServerLevel level, LivingEntity caster) {
        AABB area = caster.getBoundingBox().inflate(COMMAND_ORDER_RANGE);
        return level.getEntitiesOfClass(
                ServerPlayer.class,
                area,
                player -> player.isAlive() && player != caster
        );
    }

    private static void applyAuthorityOrder(ServerLevel level, LivingEntity caster, List<ServerPlayer> targets, int casterSeq, String command) {
        long durationTicks = getOrderDuration(command);

        for (ServerPlayer target : targets) {
            ACTIVE_ORDERS.put(target.getUUID(), new ActiveOrder(
                    caster.getUUID(),
                    casterSeq,
                    command,
                    level.getGameTime() + durationTicks
            ));

            enforceAuthorityOrder(level, caster, target, casterSeq, command);
        }
    }

    private static long getOrderDuration(String command) {
        return switch (command) {
            case "kneel" -> 50L;
            case "halt" -> 60L;
            case "retreat" -> 40L;
            case "advance" -> 40L;
            case "silence" -> 100L;
            default -> 40L;
        };
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        ActiveOrder order = ACTIVE_ORDERS.get(player.getUUID());
        if (order == null) return;

        if (serverLevel.getGameTime() >= order.expiresAtTick()) {
            clearActiveOrder(player);
            return;
        }

        LivingEntity caster = resolveCaster(serverLevel, order.casterId());
        if (caster == null || !caster.isAlive()) {
            clearActiveOrder(player);
            return;
        }

        enforceAuthorityOrder(serverLevel, caster, player, order.casterSeq(), order.command());
    }

    private static LivingEntity resolveCaster(ServerLevel level, UUID casterId) {
        return level.getServer().getPlayerList().getPlayer(casterId);
    }

    private static void clearActiveOrder(LivingEntity entity) {
        ACTIVE_ORDERS.remove(entity.getUUID());
    }

    private static void enforceAuthorityOrder(ServerLevel level, LivingEntity caster, LivingEntity target, int casterSeq, String command) {
        if (!(target instanceof ServerPlayer player)) {
            return;
        }

        Vec3 away = player.position().subtract(caster.position()).normalize();
        Vec3 toward = caster.position().subtract(player.position()).normalize();

        switch (command) {
            case "kneel" -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 2, false, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 2, false, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 25, 2, false, false, false));
                lockHeadDown(player, caster, casterSeq);
                syncPlayerPosition(player, level, player.position(), player.getYRot(), player.getXRot());
            }
            case "halt" -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 10, false, false, false));
                lockHeadDown(player, caster, casterSeq);
                syncPlayerPosition(player, level, player.position(), player.getYRot(), player.getXRot());
            }
            case "retreat" -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, false, false));
                Vec3 next = player.position().add(
                        away.x * 0.65D,
                        0.12D,
                        away.z * 0.65D
                );
                syncPlayerPosition(player, level, next, player.getYRot(), player.getXRot());
            }
            case "advance" -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 0, false, false, false));
                Vec3 next = player.position().add(
                        toward.x * 0.55D,
                        0.04D,
                        toward.z * 0.55D
                ).add(
                        toward.x * 0.12D,
                        0.0D,
                        toward.z * 0.12D
                );
                syncPlayerPosition(player, level, next, player.getYRot(), player.getXRot());
            }
            case "silence" -> {
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50, 0, false, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 35, 0, false, false, false));
                lockHeadDown(player, caster, casterSeq);

                // Stop all active toggle abilities immediately
                ToggleAbility.cleanUp(level, player);

                // Disable all Beyonder abilities for 5 seconds
                if (BeyonderData.isBeyonder(player)) {
                    DisabledAbilitiesComponent component = player.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
                    component.disableAbilityUsageForTime("commanding_silence", 20 * 5, player);
                }

                syncPlayerPosition(player, level, player.position(), player.getYRot(), player.getXRot());
            }
        }

        ParticleUtil.spawnSphereParticles(level, ModParticles.BLACK.get(),
                player.position().add(0, 1, 0), 0.8, 12);
    }

    private static void syncPlayerPosition(ServerPlayer player, ServerLevel level, Vec3 pos, float yRot, float xRot) {
        player.teleportTo(level, pos.x, pos.y, pos.z, yRot, xRot);
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
    }

    private static boolean canBeCommandedBy(LivingEntity caster, LivingEntity target) {
        if (!BeyonderData.isBeyonder(target)) return true;

        int casterSeq = BeyonderData.getSequence(caster);
        int targetSeq = BeyonderData.getSequence(target);

        return targetSeq >= casterSeq;
    }

    private static void lockHeadDown(LivingEntity target, LivingEntity caster, int casterSeq) {
        float forcedPitch;

        if (!BeyonderData.isBeyonder(target)) {
            forcedPitch = Math.max(target.getXRot(), 40.0f);
        } else {
            int targetSeq = BeyonderData.getSequence(target);
            if (targetSeq < casterSeq + 2 && !(target instanceof Player)) {
                return;
            }
            forcedPitch = 36.0f + Math.min(18.0f, Math.max(0, targetSeq - casterSeq) * 4.0f);
        }

        target.setXRot(forcedPitch);
        target.xRotO = forcedPitch;
        target.setYRot(target.getYRot());
        target.yRotO = target.getYRot();
        target.yBodyRot = target.getYRot();
        target.yBodyRotO = target.getYRot();
        target.yHeadRot = target.getYRot();
        target.yHeadRotO = target.getYRot();
        target.setDeltaMovement(target.getDeltaMovement().multiply(0.88D, 1.0D, 0.88D));

        if (target instanceof ServerPlayer player) {
            player.teleportTo(player.serverLevel(), player.getX(), player.getY(), player.getZ(), player.getYRot(), forcedPitch);
            player.hurtMarked = true;
        }
    }

    public static boolean isActive(UUID uuid) {
        return ACTIVE_ORDERS.containsKey(uuid);
    }

    @SubscribeEvent
    public static void onPresenceChallenged(LivingDamageEvent.Pre event) {
        LivingEntity victim = event.getEntity();
        LivingEntity attacker = resolveAttacker(event.getSource());
        if (attacker == null) return;

        if (!CommandingPresenceAbility.isActive(victim.getUUID())) return;

        // Stronger Beyonders (lower sequence) are not debuffed
        if (BeyonderData.isBeyonder(attacker)) {
            int attackerSeq = BeyonderData.getSequence(attacker);
            int casterSeq = BeyonderData.getSequence(victim);
            if (attackerSeq < casterSeq) return;
        }

        // Debuff attacker's outgoing damage by 50% for 3 seconds
        REFLECTED_UNTIL.put(attacker.getUUID(), attacker.level().getGameTime() + REFLECT_DURATION_TICKS);

        Vec3 pushDir = attacker.position().subtract(victim.position()).normalize();
        attacker.setDeltaMovement(pushDir.x * 0.45, 0.28, pushDir.z * 0.45);
        attacker.hasImpulse = true;

        if (attacker instanceof Player player) {
            AbilityUtil.sendActionBar(player,
                    Component.literal("Fighting the presence exhausts you.")
                            .withColor(0x8800CC));
        }
    }

    @SubscribeEvent
    public static void onReflectedDamage(LivingDamageEvent.Pre event) {
        LivingEntity attacker = resolveAttacker(event.getSource());
        if (attacker == null) return;

        Long until = REFLECTED_UNTIL.get(attacker.getUUID());
        if (until == null) return;

        if (attacker.level().getGameTime() > until) {
            REFLECTED_UNTIL.remove(attacker.getUUID());
            return;
        }

        event.setNewDamage(event.getNewDamage() * 0.5f);
    }

    private static LivingEntity resolveAttacker(DamageSource source) {
        var direct = source.getDirectEntity();
        if (direct instanceof LivingEntity le) return le;
        if (direct instanceof Projectile p && p.getOwner() instanceof LivingEntity le) return le;

        var owner = source.getEntity();
        if (owner instanceof LivingEntity le) return le;

        return null;
    }
}

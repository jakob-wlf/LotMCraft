package de.jakob.lotm.beyonders.boss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public final class BossRuntime {
    static final String ANCHOR_SET = "neoboss_anchor_set";
    static final String ANCHOR_X = "neoboss_anchor_x";
    static final String ANCHOR_Y = "neoboss_anchor_y";
    static final String ANCHOR_Z = "neoboss_anchor_z";
    private static final String RETALIATION_UUID = "neoboss_retaliation_uuid";
    private static final String RETALIATION_UNTIL = "neoboss_retaliation_until";
    private static final int RETALIATION_DURATION = 1200;
    private static final Map<Mob, BossAiGoals.Installed> APPLIED = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<java.util.UUID, ServerBossEvent> BARS = new HashMap<>();

    private BossRuntime() {
    }

    public static void refresh(Mob mob) {
        if (BossRegistry.getDefinition(mob).isPresent()) {
            applyGoals(mob);
        } else {
            unapplyGoals(mob);
            removeBossBar(mob);
        }
    }

    static void applyGoals(Mob mob) {
        synchronized (APPLIED) {
            if (APPLIED.containsKey(mob)) {
                return;
            }
            APPLIED.put(mob, BossAiGoals.install(mob));
        }
    }

    static void unapplyGoals(Mob mob) {
        BossAiGoals.Installed installed;
        synchronized (APPLIED) {
            installed = APPLIED.remove(mob);
        }
        if (installed != null) {
            BossAiGoals.uninstall(mob, installed);
        }
    }

    static void enforceTargetPolicy(Mob mob, BossDefinition definition) {
        LivingEntity target = mob.getTarget();
        if (target == null) {
            return;
        }
        if (!(target instanceof Player player)
            || (definition.hostility() == BossDefinition.Hostility.RETALIATE && !isRetaliationTarget(mob, player))) {
            mob.setTarget(null);
        }
    }

    static void ensureAnchor(Mob mob) {
        CompoundTag data = mob.getPersistentData();
        if (data.getBoolean(ANCHOR_SET)) {
            return;
        }
        data.putDouble(ANCHOR_X, mob.getX());
        data.putDouble(ANCHOR_Y, mob.getY());
        data.putDouble(ANCHOR_Z, mob.getZ());
        data.putBoolean(ANCHOR_SET, true);
    }

    static void tick(Mob mob, BossDefinition definition) {
        if (!mob.isAlive()) {
            removeBossBar(mob);
            return;
        }
        if (!(mob.level() instanceof ServerLevel level)) {
            return;
        }
        ServerBossEvent bar = BARS.computeIfAbsent(mob.getUUID(), id -> new ServerBossEvent(
            definition.barTitle(), definition.barColor(), definition.barOverlay()
        ));
        if (!bar.getName().equals(definition.barTitle())) {
            bar.setName(definition.barTitle());
        }
        if (bar.getColor() != definition.barColor()) {
            bar.setColor(definition.barColor());
        }
        if (bar.getOverlay() != definition.barOverlay()) {
            bar.setOverlay(definition.barOverlay());
        }
        float maxHealth = mob.getMaxHealth();
        bar.setProgress(maxHealth <= 0.0F ? 0.0F : Math.max(0.0F, Math.min(1.0F, mob.getHealth() / maxHealth)));
        bar.setVisible(true);
        double rangeSquared = definition.barRange() * definition.barRange();
        Set<ServerPlayer> desired = new HashSet<>();

        if (mob.getTarget() instanceof ServerPlayer target
                && target.isAlive()
                && target.distanceToSqr(mob) <= rangeSquared) {
            desired.add(target);
        }
        for (ServerPlayer player : new ArrayList<>(bar.getPlayers())) {
            if (!desired.contains(player)) {
                bar.removePlayer(player);
            }
        }
        for (ServerPlayer player : desired) {
            if (!bar.getPlayers().contains(player)) {
                bar.addPlayer(player);
            }
        }
    }

    static void removeBossBar(Mob mob) {
        ServerBossEvent bar = BARS.remove(mob.getUUID());
        if (bar != null) {
            bar.removeAllPlayers();
        }
    }

    static void alertPlayer(Mob mob, Player player, BossDefinition definition) {
        if (!definition.hasAlert() || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        serverPlayer.connection.send(new ClientboundSetTitlesAnimationPacket(
            definition.alertFadeIn(), definition.alertStay(), definition.alertFadeOut()
        ));
        serverPlayer.connection.send(new ClientboundSetTitleTextPacket(definition.alertTitle()));
        serverPlayer.connection.send(new ClientboundSetSubtitleTextPacket(definition.alertSubtitle()));
    }

    static void rememberRetaliation(Mob mob, Player player) {
        CompoundTag data = mob.getPersistentData();
        data.putUUID(RETALIATION_UUID, player.getUUID());
        data.putLong(RETALIATION_UNTIL, mob.level().getGameTime() + RETALIATION_DURATION);
    }

    static boolean isRetaliationTarget(Mob mob, Player player) {
        CompoundTag data = mob.getPersistentData();
        return data.hasUUID(RETALIATION_UUID)
            && data.getLong(RETALIATION_UNTIL) > mob.level().getGameTime()
            && data.getUUID(RETALIATION_UUID).equals(player.getUUID());
    }

    static void tickRetaliation(Mob mob, BossDefinition definition) {
        if (definition.hostility() != BossDefinition.Hostility.RETALIATE) {
            return;
        }
        CompoundTag data = mob.getPersistentData();
        if (!data.hasUUID(RETALIATION_UUID)) {
            return;
        }
        if (data.getLong(RETALIATION_UNTIL) <= mob.level().getGameTime()) {
            if (mob.getTarget() instanceof Player player && player.getUUID().equals(data.getUUID(RETALIATION_UUID))) {
                mob.setTarget(null);
            }
            data.remove(RETALIATION_UUID);
            data.remove(RETALIATION_UNTIL);
        }
    }
}

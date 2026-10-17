package de.jakob.lotm.util.helper;

import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class BribeConnectionRegistry {

    private record Link(UUID player, UUID target, long expiry, AtomicBoolean cancelled) {}

    private static final Map<UUID, Link> LINKS = new ConcurrentHashMap<>();
    private static final byte GLOW_BIT = 0x40;
    private static final EntityDataAccessor<Byte> SHARED_FLAGS_ACCESSOR;
    static {
        try {
            Field f = Entity.class.getDeclaredField("DATA_SHARED_FLAGS_ID");
            f.setAccessible(true);
            //noinspection unchecked
            SHARED_FLAGS_ACCESSOR = (EntityDataAccessor<Byte>) f.get(null);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    private BribeConnectionRegistry() {}

    public static void link(LivingEntity player, LivingEntity target, int durationTicks) {
        Link previous = LINKS.remove(player.getUUID());
        if (previous != null) {
            previous.cancelled().set(true);
            clearGlow(player, previous.target());
        }
        long expiry = System.currentTimeMillis() + (durationTicks * 50L);
        Link link = new Link(player.getUUID(), target.getUUID(), expiry, new AtomicBoolean(false));
        if (player instanceof ServerPlayer sp) BribeConnectionRegistry.sendGlowPacket(sp, target, true);
//        if (target instanceof ServerPlayer sp && sp != player) BribeConnectionRegistry.sendGlowPacket(sp, player, true);
        if (player.level() instanceof ServerLevel serverLevel) {
            ServerScheduler.scheduleUntil(serverLevel, () -> {
                if (System.currentTimeMillis() >= link.expiry) unlink(player, target);
                if (target.getHealth() <= 0 || player.getHealth() <= 0) unlink(player, target);
                if (LINKS.get(player.getUUID()).target != target.getUUID()) unlink(player, target);
                if (player instanceof ServerPlayer serverPlayer) {
                    ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(getText(target));
                    serverPlayer.connection.send(packet);
                }
//            if(target instanceof ServerPlayer targetPlayer) {
//                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(getText(player));
//                targetPlayer.connection.send(packet);
//            }
            }, 5, null, link.cancelled);
            LINKS.put(player.getUUID(), link);
        }
    }

    private static @NotNull MutableComponent getText(LivingEntity player) {
        MutableComponent text =  Component.literal(player.getDisplayName().getString() + ": ").withColor(16755200).append(Component.literal(player.getHealth() + "/" + player.getMaxHealth() + "❤").withColor(16733525));
        if(player instanceof ServerPlayer serverPlayer && BeyonderData.isBeyonder(serverPlayer) ) {
            text = text.append(Component.literal(" ").withColor(16755200));
            text = text.append(Component.literal(Math.round(BeyonderData.getSpirituality(serverPlayer)) + "/" + BeyonderData.getMaxSpirituality(serverPlayer) + "◎").withColor(5636095));
        }
        return text;
    }

    public static void  unlink(LivingEntity player, LivingEntity target) {
        Link playerLink = LINKS.get(player.getUUID());
        playerLink.cancelled.set(true);
        LINKS.remove(player.getUUID());
        if (target instanceof ServerPlayer) clearGlow(player, target);
//        if (target instanceof ServerPlayer st) sendGlowPacket(st, player, false);
    }
    public static UUID getLinkedPartner(UUID entity) {
        Link link = LINKS.get(entity);
        if (link == null) return null;
        return entity.equals(link.player) ? link.target : link.player;
    }
    public static void sendGlowPacket(ServerPlayer viewer, LivingEntity target, boolean glowing) {
        byte currentFlags = target.getEntityData().get(SHARED_FLAGS_ACCESSOR);

        byte newFlags = glowing
                ? (byte) (currentFlags | GLOW_BIT)
                : (byte) (currentFlags & ~GLOW_BIT);

        SynchedEntityData.DataValue<Byte> flagsValue =
                new SynchedEntityData.DataValue<>(
                        SHARED_FLAGS_ACCESSOR.id(),
                        EntityDataSerializers.BYTE,
                        newFlags
                );

        ClientboundSetEntityDataPacket packet =
                new ClientboundSetEntityDataPacket(target.getId(), List.of(flagsValue));

        viewer.connection.send(packet);
    }

    private static void clearGlow(LivingEntity viewer, @Nullable UUID targetId) {
        if (targetId == null) return;
        if (!(viewer instanceof ServerPlayer sp)) return;
        if (!(sp.level() instanceof ServerLevel level)) return;

        Entity targetEntity = level.getEntity(targetId);
        if (targetEntity instanceof LivingEntity living) {
            sendGlowPacket(sp, living, false);
        }
    }

    private static void clearGlow(LivingEntity player, LivingEntity target) {
        if (player instanceof ServerPlayer sp) {
            sendGlowPacket(sp, target, false);
        }
    }
}
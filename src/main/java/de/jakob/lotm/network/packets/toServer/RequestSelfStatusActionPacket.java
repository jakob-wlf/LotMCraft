package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.SelfEnvisionStatusData;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncEnvisionStatusPacket;
import de.jakob.lotm.status.StatusSnapshot;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Client → Server: Self Envisioning Status action.
 * action="SAVE"    slot=N  → capture self state into slot N
 * action="RESTORE" slot=N  → apply slot N state for 30 minutes, then revert
 */
public record RequestSelfStatusActionPacket(String action, int slot)
        implements CustomPacketPayload {

    public static final Type<RequestSelfStatusActionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "req_self_status_action"));

    public static final StreamCodec<FriendlyByteBuf, RequestSelfStatusActionPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> { buf.writeUtf(pkt.action()); buf.writeInt(pkt.slot()); },
                    buf -> new RequestSelfStatusActionPacket(buf.readUtf(), buf.readInt())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(RequestSelfStatusActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            SelfEnvisionStatusData data  = SelfEnvisionStatusData.get(player.getServer());
            int slot = packet.slot();

            if ("SYNC".equals(packet.action())) {
                // Client just opened the screen — just send a sync back

            } else if ("SAVE".equals(packet.action())) {
                // Block saves while a restore is active — slots are reserved for the saved state
                if (data.hasActiveRestore(player.getUUID())) {
                    player.sendSystemMessage(Component.literal(
                            "§cCannot overwrite a slot while a restore is active."));
                    sendSync(player, data);
                    return;
                }
                StatusSnapshot snap = StatusSnapshot.capture(player);
                String err = data.trySaveSlot(player.getUUID(), slot, snap);
                if (err != null) {
                    player.sendSystemMessage(Component.literal("§c" + err));
                } else {
                    player.sendSystemMessage(Component.literal("§aSlot " + (slot + 1) + " saved."));
                }

            } else if ("RESTORE".equals(packet.action())) {
                if (data.hasActiveRestore(player.getUUID())) {
                    player.sendSystemMessage(Component.literal("§cA restore is already active."));
                    sendSync(player, data);
                    return;
                }
                SelfEnvisionStatusData.Slot s = data.getSlot(player.getUUID(), slot);
                if (s.isEmpty()) {
                    player.sendSystemMessage(Component.literal("§cSlot " + (slot + 1) + " is empty."));
                    sendSync(player, data);
                    return;
                }
                StatusSnapshot pre = StatusSnapshot.capture(player);
                String err = data.tryStartRestore(player.getUUID(), slot, pre);
                if (err != null) {
                    player.sendSystemMessage(Component.literal("§c" + err));
                } else {
                    s.snapshot.applyTo(player);
                    player.sendSystemMessage(Component.literal(
                            "§bEnvisioning status restored. Reverts in 30 minutes."));
                }

            } else if ("CANCEL".equals(packet.action())) {
                if (!data.hasActiveRestore(player.getUUID())) {
                    player.sendSystemMessage(Component.literal("§cNo active restore to cancel."));
                } else {
                    StatusSnapshot pre = data.cancelRestore(player.getUUID());
                    if (pre != null) {
                        pre.applyTo(player);
                        player.sendSystemMessage(Component.literal(
                                "\u00a7aStatus restore cancelled \u2014 reverted to pre-restore state."));
                    }
                }
            }

            sendSync(player, data);
        });
    }

    public static void sendSync(ServerPlayer player, SelfEnvisionStatusData data) {
        SelfEnvisionStatusData.Slot[] slots = data.getSlots(player.getUUID());
        List<SyncEnvisionStatusPacket.SlotInfo> infos = new ArrayList<>();
        boolean hasDeath = data.getSlots(player.getUUID()) != null;
        Long lastDeath = null; // accessed below per-slot

        // Rebuild to access lastDeathTime — use reflection-free approach via public accessor
        for (int i = 0; i < SelfEnvisionStatusData.MAX_SLOTS; i++) {
            SelfEnvisionStatusData.Slot s = slots[i];
            if (s.isEmpty()) {
                infos.add(SyncEnvisionStatusPacket.SlotInfo.empty());
            } else {
                // Death check: if player died after save time → blocked
                SelfEnvisionStatusData.ActiveRestore ar = data.getActiveRestore(player.getUUID());
                // We need lastDeathTime exposed — add a public accessor
                boolean blocked = data.isBlockedByDeath(player.getUUID(), s.snapshot.captureTimeMs);
                infos.add(new SyncEnvisionStatusPacket.SlotInfo(
                        false,
                        s.snapshot.pathway,
                        s.snapshot.sequence,
                        s.snapshot.hasUniqueness,
                        s.snapshot.uniquenessPathway,
                        s.snapshot.effects.size(),
                        s.snapshot.captureTimeMs,
                        s.cooldownRemainingMs(),
                        blocked
                ));
            }
        }

        SelfEnvisionStatusData.ActiveRestore ar = data.getActiveRestore(player.getUUID());
        PacketHandler.sendToPlayer(player, new SyncEnvisionStatusPacket(
                infos,
                ar != null && !ar.cancelled,
                ar != null ? ar.expiryMs : 0L
        ));
    }
}

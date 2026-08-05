package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.TargetEnvisionStatusData;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncTargetEnvisionStatusPacket;
import de.jakob.lotm.status.StatusSnapshot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Client → Server: Target Envisioning Status action.
 * action="SAVE"    slot=N  targetName=X  → capture target X's state into slot N
 * action="RESTORE" slot=N                → apply slot N state onto that slot's target
 */
public record RequestTargetStatusActionPacket(String action, int slot, String targetName)
        implements CustomPacketPayload {

    public static final Type<RequestTargetStatusActionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "req_target_status_action"));

    public static final StreamCodec<FriendlyByteBuf, RequestTargetStatusActionPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeUtf(pkt.action());
                        buf.writeInt(pkt.slot());
                        buf.writeUtf(pkt.targetName());
                    },
                    buf -> new RequestTargetStatusActionPacket(buf.readUtf(), buf.readInt(), buf.readUtf())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(RequestTargetStatusActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sender)) return;

            TargetEnvisionStatusData data = TargetEnvisionStatusData.get(sender.getServer());
            int slot = packet.slot();

            if ("SYNC".equals(packet.action())) {
                // Client just opened the screen — just send a sync back

            } else if ("SAVE".equals(packet.action())) {
                // Block saves while a restore is active — slots are reserved for the saved state
                UUID casterUUID = sender.getUUID();
                UUID activeTarget = null;
                for (Map.Entry<java.util.UUID, de.jakob.lotm.attachments.TargetEnvisionStatusData.ActiveTargetRestore> e :
                        data.getActiveRestoreMap().entrySet()) {
                    if (casterUUID.equals(e.getValue().casterUUID)) { activeTarget = e.getKey(); break; }
                }
                if (activeTarget != null) {
                    sender.sendSystemMessage(Component.literal(
                            "§cCannot overwrite a slot while a restore is active."));
                    sendSync(sender, data);
                    return;
                }
                ServerPlayer target = sender.getServer().getPlayerList()
                        .getPlayerByName(packet.targetName());
                if (target == null) {
                    sender.sendSystemMessage(Component.literal("§cPlayer not found: " + packet.targetName()));
                    sendSync(sender, data);
                    return;
                }
                StatusSnapshot snap = StatusSnapshot.capture(target);
                String err = data.trySaveSlot(sender.getUUID(), slot, snap, target.getName().getString());
                if (err != null) {
                    sender.sendSystemMessage(Component.literal("§c" + err));
                } else {
                    sender.sendSystemMessage(Component.literal(
                            "§aSlot " + (slot + 1) + " saved for " + target.getName().getString() + "."));
                }

            } else if ("RESTORE".equals(packet.action())) {
                TargetEnvisionStatusData.TargetSlot s = data.getSlot(sender.getUUID(), slot);
                if (s.isEmpty()) {
                    sender.sendSystemMessage(Component.literal("§cSlot " + (slot + 1) + " is empty."));
                    sendSync(sender, data);
                    return;
                }
                ServerPlayer target = sender.getServer().getPlayerList()
                        .getPlayerByName(s.targetName);
                if (target == null) {
                    sender.sendSystemMessage(Component.literal(
                            "§cTarget player §f" + s.targetName + " §cis not online."));
                    sendSync(sender, data);
                    return;
                }

                // Determine restore duration based on Sefirot/GOO
                boolean specialTarget = TargetEnvisionStatusData.targetHasSefirotOrGOO(target);
                long durationMs = specialTarget
                        ? TargetEnvisionStatusData.RESTORE_SEFIROT_MS
                        : TargetEnvisionStatusData.RESTORE_NORMAL_MS;

                StatusSnapshot pre = StatusSnapshot.capture(target);
                String err = data.tryStartRestore(sender.getUUID(), slot,
                        target.getUUID(), pre, durationMs);
                if (err != null) {
                    sender.sendSystemMessage(Component.literal("§c" + err));
                } else {
                    s.snapshot.applyTo(target);
                    long secs = durationMs / 1000;
                    sender.sendSystemMessage(Component.literal(
                            "§bStatus restored on §f" + target.getName().getString() +
                            " §bfor " + (secs / 60) + " min" +
                            (specialTarget ? " §7(Sefirot/GOO limit)" : "") + "."));
                    target.sendSystemMessage(Component.literal(
                            "§7Your beyonder state has been temporarily altered by an Envisioning."));
                }

            } else if ("CANCEL".equals(packet.action())) {
                TargetEnvisionStatusData.ActiveTargetRestore ar = data.cancelActiveRestoreForCaster(sender.getUUID());
                if (ar == null) {
                    sender.sendSystemMessage(Component.literal("\u00a7cNo active target restore to cancel."));
                } else {
                    net.minecraft.server.level.ServerPlayer target =
                            sender.getServer().getPlayerList().getPlayer(ar.targetUUID);
                    if (target != null) {
                        ar.preSnapshot.applyTo(target);
                        sender.sendSystemMessage(Component.literal(
                                "\u00a7aTarget restore cancelled \u2014 reverted to pre-restore state."));
                        target.sendSystemMessage(Component.literal(
                                "\u00a77The Envisioning restore was cancelled \u2014 your state has been reverted."));
                    } else {
                        sender.sendSystemMessage(Component.literal(
                                "\u00a7eRestore cancelled, but target is offline \u2014 they will revert on next login."));
                    }
                }
            }

            sendSync(sender, data);
        });
    }

    public static void sendSync(ServerPlayer sender, TargetEnvisionStatusData data) {
        TargetEnvisionStatusData.TargetSlot[] slots = data.getSlots(sender.getUUID());
        List<SyncTargetEnvisionStatusPacket.TargetSlotInfo> infos = new ArrayList<>();

        for (int i = 0; i < TargetEnvisionStatusData.MAX_SLOTS; i++) {
            TargetEnvisionStatusData.TargetSlot s = slots[i];
            if (s.isEmpty()) {
                infos.add(SyncTargetEnvisionStatusPacket.TargetSlotInfo.empty());
            } else {
                // Check if this slot's target is currently being restored
                boolean beingRestored = false;
                ServerPlayer tp = sender.getServer().getPlayerList().getPlayerByName(s.targetName);
                if (tp != null) {
                    beingRestored = data.isTargetBeingRestored(tp.getUUID());
                }
                infos.add(new SyncTargetEnvisionStatusPacket.TargetSlotInfo(
                        false,
                        s.targetName,
                        s.snapshot.pathway,
                        s.snapshot.sequence,
                        s.snapshot.hasUniqueness,
                        s.snapshot.uniquenessPathway,
                        s.snapshot.effects.size(),
                        s.snapshot.captureTimeMs,
                        s.cooldownRemainingMs(),
                        beingRestored
                ));
            }
        }

        PacketHandler.sendToPlayer(sender, new SyncTargetEnvisionStatusPacket(
                infos, data.restoreCooldownRemainingMs(sender.getUUID())
        ));
    }
}

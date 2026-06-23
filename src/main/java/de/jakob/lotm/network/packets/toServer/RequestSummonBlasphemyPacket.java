package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.SummonedBlasphemyData;
import de.jakob.lotm.events.BlasphemySlateItemHandler;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncSummonedBlasphemyPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client → Server: toggle (summon or dismiss) a blasphemy card for the sending player.
 * An empty {@code pathway} string means "sync only" – no card state change.
 * After processing the current state is sent back via {@link SyncSummonedBlasphemyPacket}.
 */
public record RequestSummonBlasphemyPacket(String pathway)
        implements CustomPacketPayload {

    public static final Type<RequestSummonBlasphemyPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "request_summon_blasphemy"));

    public static final StreamCodec<FriendlyByteBuf, RequestSummonBlasphemyPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> buf.writeUtf(pkt.pathway()),
                    buf -> new RequestSummonBlasphemyPacket(buf.readUtf())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    // ── Server handler ────────────────────────────────────────────────────────

    public static void handle(RequestSummonBlasphemyPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            SummonedBlasphemyData data = SummonedBlasphemyData.get(player.getServer());

            String pathway = packet.pathway().trim();

            if (pathway.equals("__lock__")) {
                // Lock all currently summoned cards when the GUI is closed
                data.lockAll(player.getUUID());
            } else if (!pathway.isEmpty()) {
                if (data.hasSummoned(player.getUUID(), pathway)) {
                    // Already summoned → try to dismiss (blocked if locked)
                    boolean removed = data.dismiss(player.getUUID(), pathway);
                    if (removed) {
                        removeEnvisionCard(player, pathway);
                    } else {
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "§cThis card is locked — use it up or destroy the item to remove it."));
                    }
                } else {
                    // Not summoned → check cooldown first
                    long cdMs = data.getCooldownRemainingMs(player.getUUID(), pathway);
                    if (cdMs > 0) {
                        long totalSecs = cdMs / 1000;
                        long hours = totalSecs / 3600;
                        long minutes = (totalSecs % 3600) / 60;
                        long secs = totalSecs % 60;
                        String timeStr = hours > 0 ? hours + "h " + minutes + "m" : minutes > 0 ? minutes + "m " + secs + "s" : secs + "s";
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "§cThis card is on cooldown for " + timeStr + "."));
                    } else if (data.occupiedSlots(player.getUUID()) >= SummonedBlasphemyData.MAX_CARDS) {
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "§cYou can only have " + SummonedBlasphemyData.MAX_CARDS +
                                " blasphemy cards summoned at a time."));
                    } else if (data.summon(player.getUUID(), pathway)) {
                        giveEnvisionCard(player, pathway);
                    }
                }
            }

            // Always send updated state back to client
            PacketHandler.sendToPlayer(player,
                    new SyncSummonedBlasphemyPacket(data.getCards(player.getUUID()), data.getLockedCards(player.getUUID()), data.getCooldownExpiryMap(player.getUUID())));
        });
    }

    // ── Item helpers ─────────────────────────────────────────────────────────

    private static void giveEnvisionCard(ServerPlayer player, String pathway) {
        ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, pathway + "_blasphemy_card");
        Item item = BuiltInRegistries.ITEM.get(itemId);
        if (item == null || item == net.minecraft.world.item.Items.AIR) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§cNo blasphemy card item found for: " + pathway));
            return;
        }
        ItemStack stack = new ItemStack(item);
        BlasphemySlateItemHandler.markEnvisionSummoned(stack);
        player.addItem(stack);
    }

    private static void removeEnvisionCard(ServerPlayer player, String pathway) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof de.jakob.lotm.item.custom.BlasphemyCardItem card
                    && card.getPathway().equals(pathway)
                    && BlasphemySlateItemHandler.isEnvisionSummoned(stack)) {
                player.getInventory().removeItem(i, 1);
                return;
            }
        }
    }
}

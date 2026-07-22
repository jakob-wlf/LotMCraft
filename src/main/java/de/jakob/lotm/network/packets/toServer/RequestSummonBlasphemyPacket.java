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
            if (!pathway.isEmpty()) {
                if (data.hasSummoned(player.getUUID(), pathway)) {
                    // Already summoned → dismiss: remove the tagged item from inventory
                    data.dismiss(player.getUUID(), pathway);
                    removeEnvisionCard(player, pathway);
                } else {
                    // Not summoned → try to summon
                    if (data.activeCount(player.getUUID()) >= SummonedBlasphemyData.MAX_CARDS) {
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
                    new SyncSummonedBlasphemyPacket(
                        data.getCards(player.getUUID()),
                        data.getLockedCards(player.getUUID()),
                        data.getCooldownExpiryMap(player.getUUID())));
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

package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.DailySpinComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.gui.custom.DailySpin.DailySpinHandler;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.OpenSellYourSoulScreenPacket;
import de.jakob.lotm.util.BeyonderData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client → Server: player activates the Sell Your Soul wheel. */
public record RequestSellYourSoulPacket() implements CustomPacketPayload {

    public static final Type<RequestSellYourSoulPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "request_sell_your_soul"));

    public static final StreamCodec<ByteBuf, RequestSellYourSoulPacket> STREAM_CODEC =
            StreamCodec.unit(new RequestSellYourSoulPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(RequestSellYourSoulPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isServer()) return;
            if (!(context.player() instanceof ServerPlayer player)) return;

            DailySpinComponent spinComp = player.getData(ModAttachments.DAILY_SPIN_COMPONENT);

            // ── Cooldown check ─────────────────────────────────────────────────
            if (!spinComp.canSellSoul()) {
                long secs = spinComp.getSellSoulCooldownSeconds();
                player.sendSystemMessage(Component.literal(
                        "\u00a7cYou must wait " + formatTime(secs) + " before selling your soul again."));
                return;
            }

            DailySpinHandler.SoulResult result = DailySpinHandler.buildSoulResult(player);

            // ── Apply server-side effects ──────────────────────────────────────
            switch (result.outcome()) {
                case 0 -> {
                    // Sanity → 50%
                    SanityComponent sanity = player.getData(ModAttachments.SANITY_COMPONENT);
                    sanity.setSanityAndSync(0.5f, player);
                }
                case 1 -> {
                    // Digestion → 0
                    BeyonderData.setDigestionProgress(player, 0f);
                }
                case 2 -> {
                    // Ad overlay — client-side only
                }
                case 3 -> {
                    // Give characteristic
                    if (!result.rewardItem().isEmpty()) {
                        player.getInventory().add(result.rewardItem().copy());
                    }
                }
                case 4 -> {
                    // Revert to Sequence 9 of current pathway
                    String pathway = BeyonderData.getPathway(player);
                    BeyonderData.setBeyonder(player, pathway, 9,
                            true, false, false, true);
                }
            }

            spinComp.markSellSoulUsed();
            PacketHandler.sendToPlayer(player,
                    new OpenSellYourSoulScreenPacket(result.outcome(), result.rewardName()));
        });
    }

    private static String formatTime(long seconds) {
        long m = seconds / 60;
        long s = seconds % 60;
        return m + "m " + s + "s";
    }
}

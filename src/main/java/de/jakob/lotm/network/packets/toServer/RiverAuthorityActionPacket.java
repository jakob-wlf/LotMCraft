package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.DeathImprintData;
import de.jakob.lotm.attachments.CorruptionComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.events.DeathImprintHandler;
import de.jakob.lotm.sefirah.SefirahHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * Client → Server: player performed an action in the River Authority GUI.
 *
 * actionType:
 *   0 = Rivers Call  (requires imprint tier 3)
 *   1 = Locate       (requires imprint tier 2)
 */
public record RiverAuthorityActionPacket(int actionType, UUID targetUUID)
        implements CustomPacketPayload {

    public static final Type<RiverAuthorityActionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "river_authority_action"));

    public static final StreamCodec<ByteBuf, RiverAuthorityActionPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, RiverAuthorityActionPacket::actionType,
                    ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), RiverAuthorityActionPacket::targetUUID,
                    RiverAuthorityActionPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RiverAuthorityActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isServer()) return;
            if (!(context.player() instanceof ServerPlayer player)) return;

            // Security: only the river owner can use these actions
            if (!"river_of_eternal_darkness".equals(SefirahHandler.getClaimedSefirot(player))) return;

            switch (packet.actionType()) {
                case 0 -> DeathImprintHandler.executeRiversCall(player, packet.targetUUID());
                case 1 -> DeathImprintHandler.executeLocate(player, packet.targetUUID());
                case 2 -> {
                    // Toggle per-player corruption leakage exempt
                    ServerPlayer target = player.getServer().getPlayerList().getPlayer(packet.targetUUID());
                    if (target != null) {
                        CorruptionComponent comp = target.getData(ModAttachments.CORRUPTION_COMPONENT);
                        comp.setLeakageExempt(!comp.isLeakageExempt());
                        target.sendSystemMessage(Component.literal(comp.isLeakageExempt()
                                ? "§5Corruption leakage has been disabled for you by the River."
                                : "§5Corruption leakage has been re-enabled for you."));
                    }
                }
                case 3 -> {
                    // Toggle global corruption leakage
                    DeathImprintData imprintData = DeathImprintData.get(player.getServer());
                    imprintData.setGlobalLeakageOff(!imprintData.isGlobalLeakageOff());
                }
            }
        });
    }
}

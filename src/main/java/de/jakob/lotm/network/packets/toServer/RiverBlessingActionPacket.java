package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.sefirah.RiverBlessingManager;
import de.jakob.lotm.sefirah.SefirahHandler;
import de.jakob.lotm.util.BeyonderData;
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
 * Client → Server: River owner blesses or unblesses a player.
 *
 * actionType:
 *   {@link #BLESS}   (0) — grant the River's blessing to {@code targetUUID}
 *   {@link #UNBLESS} (1) — remove the River's blessing from {@code targetUUID}
 */
public record RiverBlessingActionPacket(int actionType, UUID targetUUID) implements CustomPacketPayload {

    public static final int BLESS   = 0;
    public static final int UNBLESS = 1;

    public static final Type<RiverBlessingActionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "river_blessing_action"));

    public static final StreamCodec<ByteBuf, RiverBlessingActionPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, RiverBlessingActionPacket::actionType,
                    ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), RiverBlessingActionPacket::targetUUID,
                    RiverBlessingActionPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(RiverBlessingActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isServer()) return;
            if (!(context.player() instanceof ServerPlayer owner)) return;

            // Security: only the river owner may use this
            if (!"river_of_eternal_darkness".equals(SefirahHandler.getClaimedSefirot(owner))) return;

            UUID target = packet.targetUUID();

            switch (packet.actionType()) {
                case BLESS -> {
                    boolean applied = RiverBlessingManager.blessPlayer(owner, target);
                    if (applied) {
                        // Notify the blessed player
                        ServerPlayer blessed = owner.getServer().getPlayerList().getPlayer(target);
                        if (blessed != null) {
                            blessed.sendSystemMessage(Component.literal(
                                    "You have received the blessing of the River of Eternal Darkness. " +
                                    "Sleep cannot claim you, and your presence is veiled.")
                                    .withStyle(net.minecraft.ChatFormatting.AQUA));
                        }
                    } else {
                        owner.sendSystemMessage(Component.literal(
                                "You cannot grant any more blessings at your current sequence.")
                                .withStyle(net.minecraft.ChatFormatting.RED));
                    }
                }
                case UNBLESS -> {
                    RiverBlessingManager.unblessPlayer(owner.getUUID(), target);
                    ServerPlayer wasBlessed = owner.getServer().getPlayerList().getPlayer(target);
                    if (wasBlessed != null) {
                        wasBlessed.sendSystemMessage(Component.literal(
                                "The River's blessing has been withdrawn.")
                                .withStyle(net.minecraft.ChatFormatting.GRAY));
                    }
                }
            }
        });
    }
}

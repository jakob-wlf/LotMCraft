package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

/**
 * Server → Client: open the ability-seal selector screen for the given target player.
 *
 * targetUUIDStr   — UUID string of the player whose abilities will be sealed
 * targetName      — display name of the target player
 * abilityIds      — all ability IDs that the target player currently has
 * abilityNames    — translated display names for each ability (parallel to abilityIds)
 * currentlySealed — ability IDs that are already sealed (may be empty)
 */
public record OpenAbilitySealScreenPacket(
        String targetUUIDStr,
        String targetName,
        List<String> abilityIds,
        List<String> abilityNames,
        List<String> currentlySealed
) implements CustomPacketPayload {

    public static final Type<OpenAbilitySealScreenPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_ability_seal_screen"));

    private static final StreamCodec<ByteBuf, List<String>> STR_LIST =
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list());

    public static final StreamCodec<ByteBuf, OpenAbilitySealScreenPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, OpenAbilitySealScreenPacket::targetUUIDStr,
            ByteBufCodecs.STRING_UTF8, OpenAbilitySealScreenPacket::targetName,
            STR_LIST,                  OpenAbilitySealScreenPacket::abilityIds,
            STR_LIST,                  OpenAbilitySealScreenPacket::abilityNames,
            STR_LIST,                  OpenAbilitySealScreenPacket::currentlySealed,
            OpenAbilitySealScreenPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(OpenAbilitySealScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isClient()) return;
            ClientHandler.openAbilitySealScreen(packet);
        });
    }
}

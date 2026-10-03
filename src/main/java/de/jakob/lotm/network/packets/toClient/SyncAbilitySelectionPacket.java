package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncAbilitySelectionPacket(String abilityId, int selectedIndex) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncAbilitySelectionPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_ability_selection"));

    public static final StreamCodec<ByteBuf, SyncAbilitySelectionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SyncAbilitySelectionPacket::abilityId,
            ByteBufCodecs.VAR_INT,
            SyncAbilitySelectionPacket::selectedIndex,
            SyncAbilitySelectionPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncAbilitySelectionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientHandler.handleAbilitySelectionPacket(packet));
    }
}

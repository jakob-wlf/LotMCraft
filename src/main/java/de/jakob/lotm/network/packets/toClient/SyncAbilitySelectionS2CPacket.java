package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncAbilitySelectionS2CPacket(String abilityId, int selectedIndex) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncAbilitySelectionS2CPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_ability_selection"));

    public static final StreamCodec<ByteBuf, SyncAbilitySelectionS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SyncAbilitySelectionS2CPacket::abilityId,
            ByteBufCodecs.VAR_INT,
            SyncAbilitySelectionS2CPacket::selectedIndex,
            SyncAbilitySelectionS2CPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncAbilitySelectionS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientHandler.handleAbilitySelectionPacket(packet));
    }
}

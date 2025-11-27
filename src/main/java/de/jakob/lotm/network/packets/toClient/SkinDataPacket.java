package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.helper.skin.ClientSkinHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SkinDataPacket(String playerName, String skinTexture, String skinSignature)
    implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<SkinDataPacket> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "skin_data"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, SkinDataPacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SkinDataPacket::playerName,
            ByteBufCodecs.STRING_UTF8,
            SkinDataPacket::skinTexture,
            ByteBufCodecs.STRING_UTF8,
            SkinDataPacket::skinSignature,
            SkinDataPacket::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(SkinDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientSkinHandler.updatePlayerSkin(packet.playerName(), packet.skinTexture(), packet.skinSignature());
            }
        });
    }
}
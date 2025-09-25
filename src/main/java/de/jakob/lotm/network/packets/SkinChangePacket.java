package de.jakob.lotm.network.packets;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.helper.skin.SkinManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SkinChangePacket(String targetPlayerName, String newSkinPlayerName)
    implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<SkinChangePacket> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "skin_change"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkinChangePacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SkinChangePacket::targetPlayerName,
            ByteBufCodecs.STRING_UTF8, 
            SkinChangePacket::newSkinPlayerName,
            SkinChangePacket::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(SkinChangePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player != null) {
                SkinManager.changeSkin(player, packet.targetPlayerName(), packet.newSkinPlayerName());
            }
        });
    }
}
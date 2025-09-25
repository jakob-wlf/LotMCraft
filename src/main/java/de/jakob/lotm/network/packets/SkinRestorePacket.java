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

public record SkinRestorePacket(String targetPlayerName) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<SkinRestorePacket> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "skin_restore"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, SkinRestorePacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SkinRestorePacket::targetPlayerName,
            SkinRestorePacket::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(SkinRestorePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player != null) {
                ServerPlayer targetPlayer = player.getServer().getPlayerList().getPlayerByName(packet.targetPlayerName());
                if (targetPlayer != null) {
                    SkinManager.restoreOriginalSkin(targetPlayer);
                }
            }
        });
    }
}
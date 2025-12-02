package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.AbilitySelectionScreen;
import de.jakob.lotm.gui.custom.IntrospectScreen;
import de.jakob.lotm.util.ClientBeyonderCache;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncIntrospectMenuPacket(int sequence, String pathway) implements CustomPacketPayload {
    public static final Type<SyncIntrospectMenuPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_introspect_menu"));
    
    public static final StreamCodec<FriendlyByteBuf, SyncIntrospectMenuPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, SyncIntrospectMenuPacket::sequence,
        ByteBufCodecs.STRING_UTF8, SyncIntrospectMenuPacket::pathway,
        SyncIntrospectMenuPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(SyncIntrospectMenuPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof IntrospectScreen screen) {
                screen.updateMenuData(packet.sequence(), packet.pathway(), ClientBeyonderCache.getDigestionProgress(context.player().getUUID()));
            }
        });
    }
}

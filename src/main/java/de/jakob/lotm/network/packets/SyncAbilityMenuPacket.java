package de.jakob.lotm.network.packets;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.AbilitySelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncAbilityMenuPacket(int sequence, String pathway) implements CustomPacketPayload {
    public static final Type<SyncAbilityMenuPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_ability_menu"));
    
    public static final StreamCodec<FriendlyByteBuf, SyncAbilityMenuPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, SyncAbilityMenuPacket::sequence,
        ByteBufCodecs.STRING_UTF8, SyncAbilityMenuPacket::pathway,
        SyncAbilityMenuPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(SyncAbilityMenuPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof AbilitySelectionScreen screen) {
                screen.updateMenuData(packet.sequence(), packet.pathway());
            }
        });
    }
}

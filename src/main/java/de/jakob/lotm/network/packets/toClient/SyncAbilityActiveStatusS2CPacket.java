package de.jakob.lotm.network.packets.toClient;


import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.rendering.AbilityIconRenderer;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncAbilityActiveStatusS2CPacket(boolean isActive) implements CustomPacketPayload {
    public static final Type<SyncAbilityActiveStatusS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_ability_active_status"));

    public static final StreamCodec<FriendlyByteBuf, SyncAbilityActiveStatusS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SyncAbilityActiveStatusS2CPacket::isActive,
                    SyncAbilityActiveStatusS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncAbilityActiveStatusS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            AbilityIconRenderer.isDeactivated = !packet.isActive();
        });
    }
}
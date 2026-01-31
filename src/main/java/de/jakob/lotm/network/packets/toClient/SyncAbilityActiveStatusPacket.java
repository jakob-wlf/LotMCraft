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

public record SyncAbilityActiveStatusPacket(boolean isActive) implements CustomPacketPayload {
    public static final Type<SyncAbilityActiveStatusPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_ability_active_status"));

    public static final StreamCodec<FriendlyByteBuf, SyncAbilityActiveStatusPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SyncAbilityActiveStatusPacket::isActive,
                    SyncAbilityActiveStatusPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncAbilityActiveStatusPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            AbilityIconRenderer.isDeactivated = !packet.isActive();
        });
    }
}
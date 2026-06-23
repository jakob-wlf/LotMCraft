package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncEyeOfDeathAbilityS2CPacket(boolean active, int entityId) implements CustomPacketPayload {

    public static final Type<SyncEyeOfDeathAbilityS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_eye_of_death_ability"));

    public static final StreamCodec<FriendlyByteBuf, SyncEyeOfDeathAbilityS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SyncEyeOfDeathAbilityS2CPacket::active,
                    ByteBufCodecs.INT, SyncEyeOfDeathAbilityS2CPacket::entityId,
                    SyncEyeOfDeathAbilityS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncEyeOfDeathAbilityS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                ClientHandler.syncEyeOfDeathAbility(packet, context.player());
            }
        });
    }
}

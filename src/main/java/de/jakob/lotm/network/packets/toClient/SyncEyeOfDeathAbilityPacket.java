package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncEyeOfDeathAbilityPacket(boolean active, int entityId) implements CustomPacketPayload {

    public static final Type<SyncEyeOfDeathAbilityPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_eye_of_death_ability"));

    public static final StreamCodec<FriendlyByteBuf, SyncEyeOfDeathAbilityPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SyncEyeOfDeathAbilityPacket::active,
                    ByteBufCodecs.INT, SyncEyeOfDeathAbilityPacket::entityId,
                    SyncEyeOfDeathAbilityPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncEyeOfDeathAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                ClientHandler.syncEyeOfDeathAbility(packet, context.player());
            }
        });
    }
}

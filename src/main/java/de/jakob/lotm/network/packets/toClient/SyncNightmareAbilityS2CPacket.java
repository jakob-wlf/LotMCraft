package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.darkness.NightmareAbility;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncNightmareAbilityS2CPacket(double x, double y, double z, double radius, boolean active) implements CustomPacketPayload {

    public static final Type<SyncNightmareAbilityS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_nightmare_ability"));

    public static final StreamCodec<FriendlyByteBuf, SyncNightmareAbilityS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, SyncNightmareAbilityS2CPacket::x,
                    ByteBufCodecs.DOUBLE, SyncNightmareAbilityS2CPacket::y,
                    ByteBufCodecs.DOUBLE, SyncNightmareAbilityS2CPacket::z,
                    ByteBufCodecs.DOUBLE, SyncNightmareAbilityS2CPacket::radius,
                    ByteBufCodecs.BOOL, SyncNightmareAbilityS2CPacket::active,
                    SyncNightmareAbilityS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncNightmareAbilityS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                NightmareAbility.syncToClient(context.player().getUUID(), packet.x, packet.y, packet.z, packet.radius, packet.active);
            }
        });
    }
}

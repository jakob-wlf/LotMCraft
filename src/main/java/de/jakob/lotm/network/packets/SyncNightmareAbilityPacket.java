package de.jakob.lotm.network.packets;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.darkness.NightmareAbility;
import de.jakob.lotm.overlay.CullOverlay;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncNightmareAbilityPacket(double x, double y, double z, double radius, boolean active) implements CustomPacketPayload {

    public static final Type<SyncNightmareAbilityPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_nightmare_ability"));

    public static final StreamCodec<FriendlyByteBuf, SyncNightmareAbilityPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, SyncNightmareAbilityPacket::x,
                    ByteBufCodecs.DOUBLE, SyncNightmareAbilityPacket::y,
                    ByteBufCodecs.DOUBLE, SyncNightmareAbilityPacket::z,
                    ByteBufCodecs.DOUBLE, SyncNightmareAbilityPacket::radius,
                    ByteBufCodecs.BOOL, SyncNightmareAbilityPacket::active,
                    SyncNightmareAbilityPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncNightmareAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                NightmareAbility.syncToClient(context.player().getUUID(), packet.x, packet.y, packet.z, packet.radius, packet.active);
            }
        });
    }
}

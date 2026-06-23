package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.door.TeleportationAuthorityAbility;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UseTeleportationAuthorityC2SPacket(String use, double x, double y, double z, int id) implements CustomPacketPayload {
    public static final Type<UseTeleportationAuthorityC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "use_teleportation_authority"));

    public static final StreamCodec<FriendlyByteBuf, UseTeleportationAuthorityC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, UseTeleportationAuthorityC2SPacket::use,
                    ByteBufCodecs.DOUBLE, UseTeleportationAuthorityC2SPacket::x,
                    ByteBufCodecs.DOUBLE, UseTeleportationAuthorityC2SPacket::y,
                    ByteBufCodecs.DOUBLE, UseTeleportationAuthorityC2SPacket::z,
                    ByteBufCodecs.INT, UseTeleportationAuthorityC2SPacket::id,
                    UseTeleportationAuthorityC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UseTeleportationAuthorityC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isServer()) {
                if(context.player().getId() != packet.id) {
                    return;
                }
                switch (packet.use) {
                    case "teleportation_authority_self" -> TeleportationAuthorityAbility.teleportSelf((ServerPlayer) context.player(), new Vec3(packet.x, packet.y, packet.z));
                    case "teleportation_authority_self_and_nearby" -> TeleportationAuthorityAbility.teleportSelfAndOthers((ServerPlayer) context.player(), new Vec3(packet.x, packet.y, packet.z));
                    case "teleportation_authority_targets" -> TeleportationAuthorityAbility.teleportTargets((ServerPlayer) context.player(), new Vec3(packet.x, packet.y, packet.z));
                }
            }
        });
    }
}
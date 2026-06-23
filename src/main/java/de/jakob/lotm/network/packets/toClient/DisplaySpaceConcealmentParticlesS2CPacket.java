package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DisplaySpaceConcealmentParticlesS2CPacket(double x, double y, double z) implements CustomPacketPayload {
    public static final Type<DisplaySpaceConcealmentParticlesS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "display_space_concealment_particles"));

    public static final StreamCodec<FriendlyByteBuf, DisplaySpaceConcealmentParticlesS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, DisplaySpaceConcealmentParticlesS2CPacket::x,
                    ByteBufCodecs.DOUBLE, DisplaySpaceConcealmentParticlesS2CPacket::y,
                    ByteBufCodecs.DOUBLE, DisplaySpaceConcealmentParticlesS2CPacket::z,
                    DisplaySpaceConcealmentParticlesS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DisplaySpaceConcealmentParticlesS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ParticleUtil.spawnParticles((ClientLevel) context.player().level(), ParticleTypes.END_ROD, new Vec3(packet.x, packet.y, packet.z), 2, .2, 0));
    }
}
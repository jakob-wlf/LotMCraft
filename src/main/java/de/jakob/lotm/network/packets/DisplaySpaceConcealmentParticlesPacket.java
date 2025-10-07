package de.jakob.lotm.network.packets;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ClientScheduler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3f;

public record DisplaySpaceConcealmentParticlesPacket(double x, double y, double z) implements CustomPacketPayload {
    public static final Type<DisplaySpaceConcealmentParticlesPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "display_space_concealment_particles"));

    public static final StreamCodec<FriendlyByteBuf, DisplaySpaceConcealmentParticlesPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, DisplaySpaceConcealmentParticlesPacket::x,
                    ByteBufCodecs.DOUBLE, DisplaySpaceConcealmentParticlesPacket::y,
                    ByteBufCodecs.DOUBLE, DisplaySpaceConcealmentParticlesPacket::z,
                    DisplaySpaceConcealmentParticlesPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DisplaySpaceConcealmentParticlesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ParticleUtil.spawnParticles((ClientLevel) context.player().level(), ParticleTypes.END_ROD, new Vec3(packet.x, packet.y, packet.z), 2, .2, 0));
    }
}
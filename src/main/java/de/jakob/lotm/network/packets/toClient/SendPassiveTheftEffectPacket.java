package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.helper.ParticleUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3f;

public record SendPassiveTheftEffectPacket(double x, double y, double z) implements CustomPacketPayload {
    
    public static final Type<SendPassiveTheftEffectPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "send_passive_theft_particles"));
    
    public static final StreamCodec<ByteBuf, SendPassiveTheftEffectPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.DOUBLE,
        SendPassiveTheftEffectPacket::x,
        ByteBufCodecs.DOUBLE,
        SendPassiveTheftEffectPacket::y,
        ByteBufCodecs.DOUBLE,
        SendPassiveTheftEffectPacket::z,
        SendPassiveTheftEffectPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(71 / 255f, 66 / 255f, 201 / 255f),
            1.25f
    );

    public static void handle(SendPassiveTheftEffectPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ParticleUtil.spawnParticles((ClientLevel) context.player().level(), dust, new Vec3(packet.x, packet.y, packet.z), 80, .8, 0);

                context.player().playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 1, 1);
            }
        });
    }
}
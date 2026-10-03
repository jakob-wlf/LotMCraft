package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ClientScheduler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3f;

public record DisplayShadowParticlesPacket(int duration) implements CustomPacketPayload {
    public static final Type<DisplayShadowParticlesPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "display_shadow_particles"));

    public static final StreamCodec<FriendlyByteBuf, DisplayShadowParticlesPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, DisplayShadowParticlesPacket::duration,
                    DisplayShadowParticlesPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DisplayShadowParticlesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientScheduler.scheduleForDuration(0, 2, packet.duration, () -> {
                ParticleUtil.spawnParticles((ClientLevel) context.player().level(), new DustParticleOptions(new Vector3f(0, 0, 0), 2), context.player().position().add(0, context.player().getEyeHeight() / 2, 0), 3, .4, 1.2, .4, 0);
            }, (ClientLevel)  context.player().level());
        });
    }
}
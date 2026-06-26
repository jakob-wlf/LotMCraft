package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.WaypointComponent;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Set;

public record WaypointSelectedPacket(WaypointComponent.ClientWaypoint waypoint, String use) implements CustomPacketPayload {

    public static final Type<WaypointSelectedPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "waypoint_selected"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WaypointSelectedPacket> STREAM_CODEC =
            StreamCodec.composite(
                    WaypointComponent.ClientWaypoint.STREAM_CODEC,
                    WaypointSelectedPacket::waypoint,
                    ByteBufCodecs.STRING_UTF8,
                    WaypointSelectedPacket::use,
                    WaypointSelectedPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(WaypointSelectedPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            ServerLevel serverLevel = player.serverLevel();
            MinecraftServer server = player.getServer();

            if (server == null) return;
            if(packet.use.equals("teleport")) {
                ServerLevel destinationLevel = resolveLevel(packet.waypoint.id(), server);

                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1, 1);
                player.teleportTo(destinationLevel, packet.waypoint.x(), packet.waypoint.y(), packet.waypoint.z(), Set.of(), player.getYRot(), player.getXRot());
                serverLevel.playSound(null, packet.waypoint.x(), packet.waypoint.y(), packet.waypoint.z(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1, 1);
                EffectManager.playEffect(EffectManager.Effect.WAYPOINT, packet.waypoint.x(), packet.waypoint.y() + 1, packet.waypoint.z(), serverLevel);
                EffectManager.playEffect(EffectManager.Effect.WAYPOINT, player.getX(), player.getY() + 1, player.getZ(), serverLevel);
            }
            else if (packet.use.equals("delete")) {
                WaypointComponent waypointComponent = player.getData(ModAttachments.WAYPOINT_COMPONENT);
                WaypointComponent.Waypoint waypoint = waypointComponent.findByClientWaypoint(packet.waypoint, server);

                if(waypoint == null) {
                    return;
                }

                waypointComponent.deleteWaypoint(waypoint);
                AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.waypoint.deleted").withColor(0x91f6ff));
            }
        });
    }

    public static ServerLevel resolveLevel(String levelKey, MinecraftServer server) {
        ResourceKey<Level> key = ResourceKey.create(
                Registries.DIMENSION,
                ResourceLocation.parse(levelKey)
        );
        return server.getLevel(key);
    }
}

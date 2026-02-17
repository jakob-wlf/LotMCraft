package de.jakob.lotm.network.packets.toClient;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.shapeShifting.PlayerSkinData;
import de.jakob.lotm.util.shapeShifting.TransformData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record ShapeShiftingSyncPacket(UUID playerId, String shapeString) implements CustomPacketPayload {
    public static final Type<ShapeShiftingSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "shape_shifting_sync"));

    public static final StreamCodec<FriendlyByteBuf, ShapeShiftingSyncPacket> STREAM_CODEC =
            CustomPacketPayload.codec(
                    ShapeShiftingSyncPacket::write,
                    ShapeShiftingSyncPacket::new);

    // honestly i dont know if this is the best way to do this if someone can improve this if will be great
    public ShapeShiftingSyncPacket(FriendlyByteBuf buf) {
        this(buf.readUUID(),
                buf.readBoolean() ? buf.readUtf() : null);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(playerId);
        buf.writeBoolean(shapeString != null);
        if (shapeString != null) buf.writeUtf(shapeString);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ShapeShiftingSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            // update local player's transformation
            Player player = mc.level.getPlayerByUUID(packet.playerId);
            if (player instanceof TransformData data) {
                data.setCurrentShape(packet.shapeString);
            }

            // get skin if transforming into another player
            String shape = packet.shapeString;
            if (shape != null && shape.startsWith("player:")) {
                String[] parts = shape.split(":");
                if (parts.length >= 3) {
                    try {
                        UUID targetUUID = UUID.fromString(parts[2]);
                        String targetName = parts[1];

                        // get profile or fall back to default skin
                        MinecraftSessionService sessionService = mc.getMinecraftSessionService();
                        ProfileResult result = sessionService.fetchProfile(targetUUID, true);
                        if (result != null) {
                            GameProfile fullProfile = result.profile();
                            PlayerSkinData.fetchAndCacheSkin(targetUUID, fullProfile);
                        } else {
                            GameProfile fallbackProfile = new GameProfile(targetUUID, targetName);
                            PlayerSkinData.fetchAndCacheSkin(targetUUID, fallbackProfile);
                        }
                    } catch (Exception ignored) {}
                }
            }
        });
    }
}
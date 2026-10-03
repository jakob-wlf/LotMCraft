package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.ShapeShiftComponent;
import de.jakob.lotm.util.shapeShifting.PlayerSkinData;
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

            // update client player's transformation
            Player player = mc.level.getPlayerByUUID(packet.playerId);
            ShapeShiftComponent data = player.getData(ModAttachments.SHAPE_SHIFT);
            data.setShape(packet.shapeString);

            // update client player dimension
            player.refreshDimensions();

            // get skin if transforming into another player
            if (packet.shapeString.startsWith("player:")) {
                String[] parts = packet.shapeString.split(":");
                if (parts.length >= 3) {
                    try {
                        UUID targetUUID = UUID.fromString(parts[2]);

                        if (PlayerSkinData.getSkinTexture(targetUUID) == null) {
                            PlayerSkinData.fetchAndCacheSkin(targetUUID);
                        }

                    } catch (Exception ignored) {}
                }
            }
        });
    }
}
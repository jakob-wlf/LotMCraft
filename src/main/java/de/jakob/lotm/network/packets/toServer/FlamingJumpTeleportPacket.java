package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.flaming_jump.FlamingJumpMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.BaseFireBlock;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record FlamingJumpTeleportPacket(int fireIndex) implements CustomPacketPayload {
    public static final Type<FlamingJumpTeleportPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "flaming_jump_teleport"));

    public static final StreamCodec<FriendlyByteBuf, FlamingJumpTeleportPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, FlamingJumpTeleportPacket::fireIndex,
                    FlamingJumpTeleportPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FlamingJumpTeleportPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            if (!(player.containerMenu instanceof FlamingJumpMenu menu)) return;

            List<BlockPos> fires = menu.getFireLocations();
            int index = packet.fireIndex();
            if (index < 0 || index >= fires.size()) return;

            BlockPos fire = fires.get(index);
            ServerLevel level = player.serverLevel();

            if (!level.hasChunkAt(fire)) {
                player.closeContainer();
                return;
            }
            if (!(level.getBlockState(fire).getBlock() instanceof BaseFireBlock)) {
                player.closeContainer();
                return;
            }

            double tx = fire.getX() + 0.5;
            double ty = fire.getY();
            double tz = fire.getZ() + 0.5;

            var movedBox = player.getBoundingBox().move(tx - player.getX(), ty - player.getY(), tz - player.getZ());
            if (!level.noCollision(player, movedBox)) {
                player.closeContainer();
                return;
            }

            player.closeContainer();
            player.teleportTo(level, tx, ty, tz, player.getYRot(), player.getXRot());
            player.resetFallDistance();
        });
    }
}
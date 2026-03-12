package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.DivinationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record PlayerDivinationSelectedPacket(UUID selectedPlayerUuid) implements CustomPacketPayload {
    public static final Type<PlayerDivinationSelectedPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "player_divination_selected"));

    private static final StreamCodec<RegistryFriendlyByteBuf, UUID> UUID_CODEC =
            StreamCodec.of(
                    (buf, uuid) -> buf.writeUUID(uuid),
                    (buf) -> buf.readUUID()
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerDivinationSelectedPacket> STREAM_CODEC =
            StreamCodec.composite(
                    UUID_CODEC,
                    PlayerDivinationSelectedPacket::selectedPlayerUuid,
                    PlayerDivinationSelectedPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PlayerDivinationSelectedPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            ServerPlayer targetPlayer = player.serverLevel().getServer().getPlayerList()
                    .getPlayer(packet.selectedPlayerUuid);

            if (targetPlayer == null || !(player.level().dimension() == targetPlayer.level().dimension())) {
                player.sendSystemMessage(Component.literal("§cPlayer not found"));
                return;
            }

            int playerSequence = BeyonderData.getSequence(player);
            int targetSequence = BeyonderData.getSequence(targetPlayer);


            int divinationDifference = 3 + DivinationUtil.getDivinationPower(player) - DivinationUtil.getConcealmentPower(targetPlayer);
            if (divinationDifference <= 0){
                player.sendSystemMessage(Component.literal("§cDivination failed"));
                if(playerSequence < 4 && targetSequence > 3){
                    player.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 200, 2));
                }
                return;
            }

            BlockPos playerPos = player.blockPosition();
            BlockPos targetPos = targetPlayer.blockPosition();

            int dx = targetPos.getX() - playerPos.getX();
            int dz = targetPos.getZ() - playerPos.getZ();

            int distance = (int) Math.sqrt(dx * dx + dz * dz);

            // distance still isn't balanced
            int maxDistance = switch (playerSequence) {
                case 9, 8, 7, 6, 5 -> 200 * (10 - playerSequence);
                case 4             -> 2500;
                case 3             -> 5000;
                case 2             -> ((int) (player.level().getWorldBorder().getSize() * 0.001) > 5000) ? (int) (player.level().getWorldBorder().getSize() * 0.001) : 7500;
                case 1             -> ((int) (player.level().getWorldBorder().getSize() * 0.01) > 7500) ? (int) (player.level().getWorldBorder().getSize() * 0.01) : 15000;
                default            -> 0;
            };

            if (distance >= maxDistance) {
                player.sendSystemMessage(Component.literal("§cPlayer is very far from you"));
                return;
            }

            if (divinationDifference < 4){
                if(playerSequence < 4 && targetSequence > 3){
                    player.sendSystemMessage(Component.literal("§cDivination failed"));
                    return;
                }
                player.sendSystemMessage(Component.literal(String.format(
                        "§5You sense §d%s§5 to the §d%s§5",
                        targetPlayer.getGameProfile().getName(),
                        getDirection(dx, dz)
                )));

            } else if(divinationDifference < 8) {
                player.sendSystemMessage(Component.literal(String.format(
                        "§5You sense §d%s§5 to the §d%s§5, about §d%d blocks §5away...",
                        targetPlayer.getGameProfile().getName(),
                        getDirection(dx, dz),
                        distance
                )));
            }
            else if (divinationDifference >= 10) {
                player.sendSystemMessage(Component.literal(String.format(
                        "§5You sense §d%s§5 to the §d%s§5 at cords §d%d, %d, %d§5, about §d%d blocks §5away...",
                        targetPlayer.getGameProfile().getName(),
                        getDirection(dx, dz),
                        targetPlayer.blockPosition().getX(),
                        targetPlayer.blockPosition().getY(),
                        targetPlayer.blockPosition().getZ(),
                        distance
                )));
            }
        });
    }

    private static String getDirection(int dx, int dz) {
        double angle = Math.toDegrees(Math.atan2(dz, dx));
        if (angle < 0) angle += 360;

        String[] directions = {
                "East", "South-East", "South", "South-West",
                "West", "North-West", "North", "North-East", "East"
        };

        int index = (int) Math.floor((angle + 22.5) / 45);
        return directions[index];
    }
}
package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.visionary.DreamTraversalAbility;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.beyonders.abilities.visionary.passives.MetaAwarenessAbility;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.DivinationUtil;
import de.jakob.lotm.util.data.PlayerSelectionWorkType;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

import static de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler.checkAsleep;

public record PlayerDivinationSelectedC2SPacket(UUID selectedPlayerUuid, PlayerSelectionWorkType types) implements CustomPacketPayload {
    public static final Type<PlayerDivinationSelectedC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "player_divination_selected"));

    private static final StreamCodec<RegistryFriendlyByteBuf, UUID> UUID_CODEC =
            StreamCodec.of(
                    (buf, uuid) -> buf.writeUUID(uuid),
                    (buf) -> buf.readUUID()
            );

    private static final StreamCodec<RegistryFriendlyByteBuf, PlayerSelectionWorkType> TYPE_CODEC =
            StreamCodec.of(
                    FriendlyByteBuf::writeEnum,
                    buf -> buf.readEnum(PlayerSelectionWorkType.class)
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerDivinationSelectedC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    UUID_CODEC,
                    PlayerDivinationSelectedC2SPacket::selectedPlayerUuid,
                    TYPE_CODEC,
                    PlayerDivinationSelectedC2SPacket::types,
                    PlayerDivinationSelectedC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PlayerDivinationSelectedC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            switch (packet.types){
                case DIVINATION -> performDivination(packet, player);
                case DREAM_TRAVERSAL -> performDreamTraversal(packet, player);
            }
        });
    }

    private static void performDreamTraversal(PlayerDivinationSelectedC2SPacket packet, ServerPlayer player){
        ServerPlayer targetPlayer = player.serverLevel().getServer().getPlayerList()
                .getPlayer(packet.selectedPlayerUuid);

        if (targetPlayer == null || !(player.level().dimension() == targetPlayer.level().dimension())) {
            player.sendSystemMessage(Component.literal("§cPlayer not found"));
            return;
        }

        int playerSeq = BeyonderData.getSequence(player);
        int targetSeq = BeyonderData.getSequence(targetPlayer);
        String targetPath = BeyonderData.getPathway(targetPlayer);

        if(VisionaryHandler.shouldFailAndTrigger(playerSeq, player, targetPlayer, null)){
            return;
        }

        if (checkAsleep(player, targetPlayer)) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.dream_traversal.must_be_asleep").withColor(0xFFff124d));
            return;
        }

        if(targetPath.equals("door") && (targetSeq <= 2 && playerSeq <= targetSeq-1)){
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.dream_traversal.failed").withColor(0xFFff124d));
            return;
        }

        int distance = (int) player.distanceTo(targetPlayer);
        if(distance <= DreamTraversalAbility.getRangeBySeq(BeyonderData.getSequence(player))){
            DreamTraversalAbility.performTeleport(player, targetPlayer);
        }
    }

    private static void performDivination(PlayerDivinationSelectedC2SPacket packet, ServerPlayer player){
        ServerPlayer targetPlayer = player.serverLevel().getServer().getPlayerList()
                .getPlayer(packet.selectedPlayerUuid);

        MetaAwarenessAbility.onDivined(player, targetPlayer);

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
                case 0             -> ((int) (player.level().getWorldBorder().getSize() * 0.1) > 15000) ? (int) (player.level().getWorldBorder().getSize() * 0.01) : 100000;
                default            -> 100;
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

        } else if(divinationDifference < 10) {
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
        }     // Trigger MetaAwareness passive if target has it
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

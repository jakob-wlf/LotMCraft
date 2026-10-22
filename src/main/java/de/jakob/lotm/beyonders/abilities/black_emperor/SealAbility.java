package de.jakob.lotm.beyonders.abilities.black_emperor;

import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.black_emperor.handlers.RoomDetector;
import de.jakob.lotm.beyonders.abilities.black_emperor.handlers.SealRegistry;
import de.jakob.lotm.beyonders.abilities.black_emperor.handlers.SealedRegion;
import de.jakob.lotm.network.packets.toClient.SyncSealPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SealAbility extends ToggleAbility {

    public SealAbility(String id) { super(id); }

    public static final Map<UUID, SealedRegion> lastRegionSent = new ConcurrentHashMap<>();
    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("black_emperor", 6);
    }

    @Override
    public float getSpiritualityCost() { return 40.0f; }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player) || !(level instanceof ServerLevel serverLevel)) return;
        SealedRegion region = SealRegistry.getByOwner(player.getUUID());
        if (region != null && SealRegistry.isBreached(serverLevel, region)) {
            unseal(player, region);
            player.sendSystemMessage(Component.literal("Your seal has been breached!").withStyle(ChatFormatting.RED));
            forceOff(player, serverLevel);
        }
        SealedRegion inside = SealRegistry.getRegionContaining(serverLevel, player.position());
        SealedRegion previous = lastRegionSent.get(player.getUUID());

        if (inside != previous) {
            if (inside == null) {
                lastRegionSent.remove(player.getUUID());
                PacketDistributor.sendToPlayer(player, new SyncSealPayload(
                        false,
                        0.0, 0.0, 0.0,
                        0.0, 0.0, 0.0
                ));
            } else {
                lastRegionSent.put(player.getUUID(), inside);
                PacketDistributor.sendToPlayer(player, new SyncSealPayload(
                        true,
                        inside.interiorBounds.minX,
                        inside.interiorBounds.minY,
                        inside.interiorBounds.minZ,
                        inside.interiorBounds.maxX,
                        inside.interiorBounds.maxY,
                        inside.interiorBounds.maxZ
                ));
            }
        }
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player) || !(level instanceof ServerLevel serverLevel)) return;

        if (SealRegistry.getByOwner(player.getUUID()) != null) {
            player.sendSystemMessage(Component.literal("You already have an active seal.").withStyle(ChatFormatting.RED));
            forceOff(player, serverLevel);
            return;
        }

        SealedRegion existing = SealRegistry.getRegionContaining(serverLevel, player.position());
        if (existing != null) {
            player.sendSystemMessage(Component.literal("You cannot seal from inside another seal.").withStyle(ChatFormatting.RED));
            forceOff(player, serverLevel);
            return;
        }

        RoomDetector.DetectedRoom room = RoomDetector.detect(serverLevel, player.blockPosition());
        if (room == null) {
            player.sendSystemMessage(Component.literal("You are not in an enclosed room (min 3x3x2 max 16x16x16).").withStyle(ChatFormatting.RED));
            forceOff(player, serverLevel);
            return;
        }

        for (BlockPos pos : room.boundary()) {
            BlockState state = serverLevel.getBlockState(pos);
            if (state.getBlock() instanceof DoorBlock && state.hasProperty(BlockStateProperties.OPEN)
                    && state.getValue(BlockStateProperties.OPEN)) {
                serverLevel.setBlock(pos, state.setValue(BlockStateProperties.OPEN, false), 3);
            }
        }

        SealedRegion region = new SealedRegion(
                player.getUUID(), serverLevel.dimension(),
                room.boundary(), room.interior(), room.interiorBounds(),
                serverLevel, serverLevel.getGameTime()
        );
        SealRegistry.add(region);

        player.sendSystemMessage(Component.literal(
                "Seal established.").withStyle(ChatFormatting.GOLD).append("(" + room.interior().size() + " air blocks)").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player) || !(level instanceof ServerLevel serverLevel)) return;
        SealedRegion region = SealRegistry.getByOwner(player.getUUID());
        if (region != null) unseal(player, region);
    }

    private void unseal(ServerPlayer player, SealedRegion region) {
        SealRegistry.remove(region);
        player.sendSystemMessage(Component.literal("Seal released.").withStyle(ChatFormatting.GREEN));
    }

    private void forceOff(ServerPlayer player, ServerLevel level) {
        cancel(level, player);
    }
}
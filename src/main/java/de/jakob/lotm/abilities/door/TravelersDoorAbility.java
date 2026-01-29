package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.TravelersDoorEntity;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.OpenCoordinateScreenTravelersDoorPacket;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class TravelersDoorAbility extends SelectableAbility {

    public static final HashMap<UUID, BlockPos> travelersDoorUsers = new HashMap<>();

    public TravelersDoorAbility(String id) {
        super(id, 3);

        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 5));
    }

    @Override
    protected float getSpiritualityCost() {
        return 70;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[] {"ability.lotmcraft.travelers_door.coordinates",
                "ability.lotmcraft.travelers_door.spirit_world"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer player)) {
            return;
        }

        Vec3 targetLoc = AbilityUtil.getTargetBlock(entity, 5, true).getBottomCenter();
        if(!level.getBlockState(BlockPos.containing(targetLoc)).getCollisionShape(entity.level(), BlockPos.containing(targetLoc)).isEmpty()) {
            return;
        }

        for(int i = 1; i < 3; i++) {
            if(level.getBlockState(BlockPos.containing(targetLoc.add(0, -i, 0))).getCollisionShape(entity.level(), BlockPos.containing(targetLoc)).isEmpty())
                continue;
            targetLoc = targetLoc.add(0, -i + 1, 0);
            break;
        }

        if(abilityIndex == 0) {
            createDoorWithCoordinates(serverLevel, player, targetLoc);
        }
        else if (abilityIndex == 1) {
            createSpiritWorldDoor(serverLevel, player, targetLoc);
        }
    }

    private void createSpiritWorldDoor(ServerLevel serverLevel, ServerPlayer player, Vec3 targetLoc) {
        TravelersDoorEntity door = new TravelersDoorEntity(ModEntities.TRAVELERS_DOOR.get(), serverLevel, player.getLookAngle().normalize().scale(-1), targetLoc, 2);
        serverLevel.addFreshEntity(door);
        serverLevel.playSound(null, BlockPos.containing(targetLoc), SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 1, 1);

        if(serverLevel.getBlockState(BlockPos.containing(targetLoc)).getCollisionShape(serverLevel, BlockPos.containing(targetLoc)).isEmpty())
            serverLevel.setBlockAndUpdate(BlockPos.containing(targetLoc), Blocks.LIGHT.defaultBlockState());

        ServerScheduler.scheduleDelayed(20 * 10, () -> {
            door.discard();
            if(serverLevel.getBlockState(BlockPos.containing(targetLoc)).getBlock() == Blocks.LIGHT)
                serverLevel.setBlockAndUpdate(BlockPos.containing(targetLoc), Blocks.AIR.defaultBlockState());
        });
    }

    private void createDoorWithCoordinates(ServerLevel serverLevel, ServerPlayer player, Vec3 targetLoc) {
        PacketHandler.sendToPlayer(player, new OpenCoordinateScreenTravelersDoorPacket());

        AtomicBoolean hasInputCoordinates = new AtomicBoolean(false);
        ServerScheduler.scheduleForDuration(0, 5, 20 * 60 * 5, () -> {
            if(hasInputCoordinates.get())
                return;

            if(travelersDoorUsers.containsKey(player.getUUID())) {
                hasInputCoordinates.set(true);

                BlockPos pos = travelersDoorUsers.get(player.getUUID());

                travelersDoorUsers.remove(player.getUUID());

                TravelersDoorEntity door = new TravelersDoorEntity(ModEntities.TRAVELERS_DOOR.get(), serverLevel, player.getLookAngle().normalize().scale(-1), targetLoc, pos.getX(), pos.getY(), pos.getZ());
                serverLevel.addFreshEntity(door);
                serverLevel.playSound(null, BlockPos.containing(targetLoc), SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 1, 1);

                if(serverLevel.getBlockState(BlockPos.containing(targetLoc)).getCollisionShape(serverLevel, BlockPos.containing(targetLoc)).isEmpty())
                    serverLevel.setBlockAndUpdate(BlockPos.containing(targetLoc), Blocks.LIGHT.defaultBlockState());

                ServerScheduler.scheduleDelayed(20 * 10, () -> {
                    door.discard();
                    if(serverLevel.getBlockState(BlockPos.containing(targetLoc)).getBlock() == Blocks.LIGHT)
                        serverLevel.setBlockAndUpdate(BlockPos.containing(targetLoc), Blocks.AIR.defaultBlockState());
                });
            }
        }, () -> {
            if(!hasInputCoordinates.get()) {
                travelersDoorUsers.remove(player.getUUID());
            }
        }, serverLevel);
    }
}

package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.TravelersDoorEntity;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.OpenCoordinateScreenTravelersDoorPacket;
import de.jakob.lotm.network.packets.RemoveDreamDivinationUserPacket;
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

public class TravelersDoorAbility extends AbilityItem {
    public static final HashMap<UUID, BlockPos> travelersDoorUsers = new HashMap<>();


    public TravelersDoorAbility(Properties properties) {
        super(properties, 3);

        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 5));
    }

    @Override
    protected float getSpiritualityCost() {
        return 65;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

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

        if(!(entity instanceof ServerPlayer player))
            return;

        PacketHandler.sendToPlayer(player, new OpenCoordinateScreenTravelersDoorPacket());

        AtomicBoolean hasInputCoordinates = new AtomicBoolean(false);
        Vec3 finalTargetLoc = targetLoc;
        ServerScheduler.scheduleForDuration(0, 5, 20 * 60 * 5, () -> {
            if(hasInputCoordinates.get())
                return;

            if(travelersDoorUsers.containsKey(entity.getUUID())) {
                hasInputCoordinates.set(true);

                BlockPos pos = travelersDoorUsers.get(entity.getUUID());

                travelersDoorUsers.remove(entity.getUUID());

                TravelersDoorEntity door = new TravelersDoorEntity(ModEntities.TRAVELERS_DOOR.get(), level, entity.getLookAngle().normalize().scale(-1), finalTargetLoc, pos.getX(), pos.getY(), pos.getZ());
                level.addFreshEntity(door);
                level.playSound(null, BlockPos.containing(finalTargetLoc), SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 1, 1);

                if(level.getBlockState(BlockPos.containing(finalTargetLoc)).getCollisionShape(level, BlockPos.containing(finalTargetLoc)).isEmpty())
                    level.setBlockAndUpdate(BlockPos.containing(finalTargetLoc), Blocks.LIGHT.defaultBlockState());

                ServerScheduler.scheduleDelayed(20 * 10, () -> {
                    door.discard();
                    if(level.getBlockState(BlockPos.containing(finalTargetLoc)).getBlock() == Blocks.LIGHT)
                        level.setBlockAndUpdate(BlockPos.containing(finalTargetLoc), Blocks.AIR.defaultBlockState());
                });
            }
        }, () -> {
            if(!hasInputCoordinates.get()) {
                travelersDoorUsers.remove(entity.getUUID());
                PacketHandler.sendToPlayer(player, new RemoveDreamDivinationUserPacket());
            }
        }, (ServerLevel) level);

    }
}

package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.TravelersDoorEntity;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class TravelersDoorAbility extends AbilityItem {
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

        level.playSound(null, BlockPos.containing(targetLoc), SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 1, 1);

        TravelersDoorEntity door = new TravelersDoorEntity(ModEntities.TRAVELERS_DOOR.get(), level, entity.getLookAngle().normalize().scale(-1), targetLoc);
        level.addFreshEntity(door);

        if(level.getBlockState(BlockPos.containing(targetLoc)).getCollisionShape(level, BlockPos.containing(targetLoc)).isEmpty())
            level.setBlockAndUpdate(BlockPos.containing(targetLoc), Blocks.LIGHT.defaultBlockState());

        Vec3 finalLoc = new Vec3(targetLoc.x, targetLoc.y, targetLoc.z);

        ServerScheduler.scheduleDelayed(20 * 5, () -> {
            door.discard();
            if(level.getBlockState(BlockPos.containing(finalLoc)).getBlock() == Blocks.LIGHT)
                level.setBlockAndUpdate(BlockPos.containing(finalLoc), Blocks.AIR.defaultBlockState());
        });
    }
}

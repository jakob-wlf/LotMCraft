package de.jakob.lotm.addons.beyonderLawOfConvergence;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class ConvergenceEvent {

    private static Map<UUID, Integer> timer = new HashMap<>();

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!(player.level() instanceof ServerLevel level)) return;
        if(!BeyonderData.isBeyonder(player)) return;

        if(level.dimension() != Level.OVERWORLD) return;

        if(!timer.containsKey(player.getUUID())){
            timer.put(player.getUUID(), 0);
        }

        timer.put(player.getUUID(), timer.get(player.getUUID()) + 1);

        int seq = BeyonderData.getSequence(player);

        int spawnTime = 20 * getTime(seq);

        if(timer.get(player.getUUID()) < spawnTime) {
            return;
        }

        String path = BeyonderData.getPathway(player);


        Random random = new Random();
        double baseChance = 0.0010;
        double sequenceMultiplier = 1.0 - (seq * 0.09);

        double spawnChance = baseChance * sequenceMultiplier;

        if (random.nextDouble() > spawnChance) {
            return;
        }

        int radius = 70;
        double angle = random.nextDouble() * Math.PI * 2.0;
        double distance = Math.sqrt(random.nextDouble()) * radius;

        int x = Mth.floor(player.getX() + Math.cos(angle) * distance);
        int z = Mth.floor(player.getZ() + Math.sin(angle) * distance);

        int y = level.getHeight(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                x,
                z
        );

        BlockPos spawnPos = new BlockPos(x, y, z);

        if (spawnPos.distSqr(player.blockPosition()) < 15 * 15) {
            return;
        }

        BlockState ground = level.getBlockState(spawnPos.below());
        BlockState feet = level.getBlockState(spawnPos);
        BlockState head = level.getBlockState(spawnPos.above());

        if (!ground.isSolid()) {
            return;
        }

        if (!feet.isAir() || !head.isAir()) {
            return;
        }

        if (spawnPos.getY() <= level.getMinBuildHeight()) {
            return;
        }

        int entitySeq = getRandomEntitySequence(seq, random);
        BeyonderNPCEntity entity = new BeyonderNPCEntity(ModEntities.BEYONDER_NPC.get(), level, true, path, entitySeq);

        entity.moveTo(
                spawnPos.getX() + 0.5,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5,
                random.nextFloat() * 360.0F,
                0.0F
        );

        if (!level.noCollision(entity)) {
            return;
        }

        level.addFreshEntity(entity);
        entity.setTarget(player);

        timer.remove(player.getUUID());
    }


    private static int getRandomEntitySequence(
            int playerSequence,
            Random random
    ) {

        int minSequence = Math.max(1, playerSequence - 2);
        int maxSequence = Math.min(9, playerSequence + 2);

        if (playerSequence == 0) {
            minSequence = 1;
            maxSequence = 3;
        }

        return random.nextInt(maxSequence - minSequence + 1) + minSequence;
    }

    private static int getTime(int seq){
        return switch (seq){
            case 9,8,7 -> 60 * 60 * 4;
            case 6,5 -> 60 * 60 * 3;
            case 4,3,2,1,0 -> 60 * 60 * 2;
            default -> 0;
        };
    }
}

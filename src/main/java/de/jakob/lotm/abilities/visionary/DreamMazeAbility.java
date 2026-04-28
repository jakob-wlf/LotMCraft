package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.visionary.passives.MetaAwarenessAbility;
import de.jakob.lotm.attachments.DreamMazeData;
import de.jakob.lotm.dimension.DreamMazeEventHandler;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class DreamMazeAbility extends SelectableAbility {

    // To resize the maze, change MAZE_SIZE in DreamMazeData. This reads from there automatically.
    private static final int MAZE_SIZE = DreamMazeData.MAZE_SIZE;
    private static final int MAZE_HEIGHT = 7;
    private static final int FLOOR_Y_OFFSET = 0;
    private static final int CEILING_Y_OFFSET = MAZE_HEIGHT + 1;
    private static final int DOOR_COUNT = 15;
    private static final int SURROUNDING_RADIUS = 25;

    private static final Map<UUID, List<BlockPos>> DOOR_POSITIONS = new HashMap<>();
    private boolean[][] removedHorizWalls;
    private boolean[][] removedVertWalls;

    public DreamMazeAbility(String id) {
        super(id, 7);
        this.canBeCopied = false;
        this.canBeUsedByNPC = false;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 2));
    }

    @Override
    public float getSpiritualityCost() {
        return 4000;
    }

    @Override
    public String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.dream_maze.self", "ability.lotmcraft.dream_maze.others"};
    }

    @Override
    public void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(entity instanceof Player)) abilityIndex = 0;
        switch (abilityIndex) {
            case 0 -> bringSelf(level, entity);
            case 1 -> bringOthers(level, entity);
        }
    }

    private void bringSelf(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(entity instanceof ServerPlayer player)) return;

        ParticleUtil.spawnParticles(serverLevel, ParticleTypes.END_ROD, entity.position(),1000, 1, .5, 1, .05);

        DreamMazeData data = DreamMazeData.get(serverLevel.getServer());

        // If already inside the dream maze, send them back out
        if (serverLevel.dimension().equals(ModDimensions.DREAM_MAZE_DIMENSION_KEY)) {
            if (data.isOccupant(player.getUUID())) {
                DreamMazeEventHandler.ejectPlayer(player, serverLevel.getServer(), data);
            }
            return;
        }

        sendIntoDreamMaze(player, player.getUUID(), serverLevel, data);
    }


    private void bringOthers(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(entity instanceof ServerPlayer caster)) return;

        ParticleUtil.spawnParticles(serverLevel, ParticleTypes.END_ROD, entity.position(),500, SURROUNDING_RADIUS, .5, SURROUNDING_RADIUS, .05);

        // Cannot use from inside the maze
        if (serverLevel.dimension().equals(ModDimensions.DREAM_MAZE_DIMENSION_KEY)) return;

        DreamMazeData data = DreamMazeData.get(serverLevel.getServer());

        List<LivingEntity> nearby = AbilityUtil.getNearbyEntities(
                entity, serverLevel, entity.position(), SURROUNDING_RADIUS, false, true);

        for (LivingEntity target : nearby) {
            if (!target.hasEffect(ModEffects.ASLEEP)) continue;
            if (target.getUUID().equals(caster.getUUID())) continue;
            if (data.isOccupant(target.getUUID())) continue;

            int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
            int targetSeq = BeyonderData.getSequence(target);
            if(BeyonderData.getPathway(target).equals("visionary") && targetSeq < entitySeq){
                AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.dream_traversal.failed").withColor(0xFFff124d));

                if(targetSeq <= 1 && target instanceof ServerPlayer targetPlayer){
                    MetaAwarenessAbility.onDivined(caster, targetPlayer);
                }

                return;
            }

            sendIntoDreamMaze(target, caster.getUUID(), serverLevel, data);
        }
    }

    private void sendIntoDreamMaze(LivingEntity target, UUID casterUUID,
                                   ServerLevel fromLevel, DreamMazeData data) {
        ServerLevel mazeLevel = fromLevel.getServer().getLevel(ModDimensions.DREAM_MAZE_DIMENSION_KEY);
        if (mazeLevel == null) return;

        BlockPos origin = data.getOrCreateMazeOrigin(casterUUID);

        if (!data.isMazeGenerated(casterUUID)) {
            generateMaze(mazeLevel, origin, casterUUID);
            data.markMazeGenerated(casterUUID);
        }

        BlockPos spawnPos = pickRandomCorridorSpawn(origin);

        data.addOccupant(casterUUID, target.getUUID(), target.position(), fromLevel.dimension());
        DreamMazeEventHandler.resetTimer(target.getUUID());

        if (target instanceof ServerPlayer player) {
            player.teleportTo(mazeLevel,
                    spawnPos.getX() + 0.5,
                    spawnPos.getY(),
                    spawnPos.getZ() + 0.5,
                    player.getYRot(),
                    player.getXRot());
        } else {
            target.teleportTo(mazeLevel,
                    spawnPos.getX() + 0.5,
                    spawnPos.getY(),
                    spawnPos.getZ() + 0.5,
                    java.util.Set.of(),
                    target.getYRot(),
                    target.getXRot());
        }
    }

    private void generateMaze(ServerLevel level, BlockPos origin, UUID casterUUID) {
        int size = MAZE_SIZE;
        int floorY = origin.getY() + FLOOR_Y_OFFSET;
        int ceilY  = origin.getY() + CEILING_Y_OFFSET;
        int cells  = (size - 1) / 2;

        removedHorizWalls = new boolean[cells][cells];
        removedVertWalls  = new boolean[cells][cells];
        boolean[][] visited = new boolean[cells][cells];

        generatePassages(visited, cells, 0, 0, new Random());

        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                boolean isWall = isMazeWall(x, z, size, cells);

                level.setBlockAndUpdate(
                        new BlockPos(origin.getX() + x, floorY, origin.getZ() + z),
                        de.jakob.lotm.block.ModBlocks.SOLID_VOID.get().defaultBlockState());
                level.setBlockAndUpdate(
                        new BlockPos(origin.getX() + x, ceilY, origin.getZ() + z),
                        de.jakob.lotm.block.ModBlocks.SOLID_VOID.get().defaultBlockState());

                for (int y = floorY + 1; y < ceilY; y++) {
                    BlockPos pos = new BlockPos(origin.getX() + x, y, origin.getZ() + z);
                    level.setBlockAndUpdate(pos, isWall
                            ? Blocks.STONE_BRICKS.defaultBlockState()
                            : Blocks.AIR.defaultBlockState());
                }
            }
        }

        List<BlockPos> corridors = findCorridorBlocks(origin, size, cells, floorY);
        List<BlockPos> doorPos = placeDoors(level, corridors, DOOR_COUNT, new Random());
        DOOR_POSITIONS.put(casterUUID, doorPos);
    }

    private void generatePassages(boolean[][] visited, int cells, int cx, int cz, Random rand) {
        visited[cx][cz] = true;

        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
        for (int i = dirs.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int[] tmp = dirs[i]; dirs[i] = dirs[j]; dirs[j] = tmp;
        }

        for (int[] d : dirs) {
            int nx = cx + d[0];
            int nz = cz + d[1];
            if (nx < 0 || nz < 0 || nx >= cells || nz >= cells) continue;
            if (visited[nx][nz]) continue;

            if (d[0] == 1)  removedVertWalls[cx][cz]  = true;
            if (d[0] == -1) removedVertWalls[nx][cz]  = true;
            if (d[1] == 1)  removedHorizWalls[cx][cz] = true;
            if (d[1] == -1) removedHorizWalls[cx][nz] = true;

            generatePassages(visited, cells, nx, nz, rand);
        }
    }

    private boolean isMazeWall(int x, int z, int size, int cells) {
        if (x == 0 || x == size - 1 || z == 0 || z == size - 1) return true;
        int cx = (x - 1) / 2;
        int cz = (z - 1) / 2;
        int rx = (x - 1) % 2;
        int rz = (z - 1) % 2;

        if (rx == 0 && rz == 0) return false; // cell center — always corridor

        if (rx == 0 && rz == 1) { // horizontal wall between (cx,cz) and (cx,cz+1)
            if (cz + 1 >= cells) return true;
            return !removedHorizWalls[cx][cz];
        }
        if (rx == 1 && rz == 0) { // vertical wall between (cx,cz) and (cx+1,cz)
            if (cx + 1 >= cells) return true;
            return !removedVertWalls[cx][cz];
        }
        return true; // corner
    }

    private List<BlockPos> findCorridorBlocks(BlockPos origin, int size, int cells, int floorY) {
        List<BlockPos> list = new ArrayList<>();
        for (int x = 1; x < size - 1; x++) {
            for (int z = 1; z < size - 1; z++) {
                if (!isMazeWall(x, z, size, cells)) {
                    list.add(new BlockPos(origin.getX() + x, floorY + 1, origin.getZ() + z));
                }
            }
        }
        return list;
    }

    private List<BlockPos> placeDoors(ServerLevel level, List<BlockPos> corridors,
                                      int count, Random rand) {
        List<BlockPos> placed = new ArrayList<>();
        if (corridors.isEmpty()) return placed;

        List<BlockPos> shuffled = new ArrayList<>(corridors);
        Collections.shuffle(shuffled, rand);

        int placedCount = 0;
        for (BlockPos candidate : shuffled) {
            if (placedCount >= count) break;
            BlockPos above = candidate.above();

            if (!level.getBlockState(candidate).isAir()) continue;
            if (!level.getBlockState(above).isAir()) continue;

            level.setBlockAndUpdate(candidate,
                    Blocks.OAK_DOOR.defaultBlockState()
                            .setValue(DoorBlock.FACING, Direction.NORTH)
                            .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
                            .setValue(DoorBlock.OPEN, false));
            level.setBlockAndUpdate(above,
                    Blocks.OAK_DOOR.defaultBlockState()
                            .setValue(DoorBlock.FACING, Direction.NORTH)
                            .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)
                            .setValue(DoorBlock.OPEN, false));

            placed.add(candidate);
            placedCount++;
        }
        return placed;
    }


    private BlockPos pickRandomCorridorSpawn(BlockPos origin) {
        int cells = (MAZE_SIZE - 1) / 2;
        int floorY = origin.getY() + FLOOR_Y_OFFSET;
        Random rand = new Random();
        int cx = rand.nextInt(cells);
        int cz = rand.nextInt(cells);
        int bx = origin.getX() + 1 + cx * 2;
        int bz = origin.getZ() + 1 + cz * 2;
        return new BlockPos(bx, floorY + 1, bz);
    }

    // escape interactions

    @SubscribeEvent
    public static void onDoorInteract(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.dimension().equals(ModDimensions.DREAM_MAZE_DIMENSION_KEY)) return;

        BlockState state = serverLevel.getBlockState(event.getPos());
        if (!state.is(Blocks.OAK_DOOR)) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        BlockPos doorPos = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER
                ? event.getPos().below() : event.getPos();

        BlockState lower = Blocks.OAK_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.NORTH)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
                .setValue(DoorBlock.OPEN, true);
        serverLevel.setBlockAndUpdate(doorPos, lower);
        serverLevel.setBlockAndUpdate(doorPos.above(), lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));

        DreamMazeData data = DreamMazeData.get(serverLevel.getServer());
        if (data.isOccupant(player.getUUID())) {
            DreamMazeEventHandler.ejectPlayer(player, serverLevel.getServer(), data);
        }

        event.setCanceled(true);
    }
}

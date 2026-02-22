package de.jakob.lotm.quest.impl.kill_beyonder_quests;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.QuestComponent;
import de.jakob.lotm.block.ModBlocks;
import de.jakob.lotm.block.custom.MysticalRingBlock;
import de.jakob.lotm.block.custom.MysticalRingBlockEntity;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.quest.Quest;
import de.jakob.lotm.quest.QuestManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public abstract class KillBeyonderQuest extends Quest {

    private final String pathway;
    private final int beyonderSequence;

    private final HashMap<UUID, Integer> tickCooldownTillDiscard = new HashMap<>();

    public KillBeyonderQuest(String id, int sequence, String pathway, int beyonderSequence) {
        super(id, sequence);
        this.pathway = pathway;
        this.beyonderSequence = beyonderSequence;
    }

    @Override
    public boolean canAccept(ServerPlayer player) {
        return BeyonderData.getSequence(player) <= sequence + 1;
    }

    @Override
    public boolean canGiveQuest(BeyonderNPCEntity npc) {
        return BeyonderData.getSequence(npc) == sequence + 1;
    }

    @Override
    public void tick(ServerPlayer player) {
        QuestComponent component = player.getData(ModAttachments.QUEST_COMPONENT);
        if(!component.getQuestLocation().containsKey(getId())) {
            QuestManager.discardQuest(player, getId());
            return;
        }

        Vec3 location = component.getQuestLocation().get(getId());
        BlockState state = player.level().getBlockState(BlockPos.containing(location));
        if(!state.is(ModBlocks.MYSTICAL_RING.get()) && AbilityUtil.getNearbyEntities(player, player.serverLevel(), player.position(), 120)
                .stream()
                .noneMatch(e -> {
                    return e instanceof BeyonderNPCEntity &&
                            e.getPersistentData().hasUUID("lotm_beyonder_summoner") &&
                            e.getPersistentData().getUUID("lotm_beyonder_summoner").equals(player.getUUID());
                })) {
            tickCooldownTillDiscard.putIfAbsent(player.getUUID(), 0);
            tickCooldownTillDiscard.put(player.getUUID(), tickCooldownTillDiscard.get(player.getUUID()) + 1);
            if(tickCooldownTillDiscard.get(player.getUUID()) > 20 * 9) {
                QuestManager.discardQuest(player, getId());
            }
            return;
        }

        tickCooldownTillDiscard.put(player.getUUID(), 0);
    }

    @Override
    protected void onLivingDeath(LivingEntity entity) {
        if(!(entity.level() instanceof ServerLevel level)) {
            return;
        }

        if(!entity.getPersistentData().hasUUID("lotm_beyonder_summoner")) {
           return;
        }

        ServerPlayer player = level.getServer().getPlayerList().getPlayer(entity.getPersistentData().getUUID("lotm_beyonder_summoner"));
        if(player == null) {
            return;
        }

        QuestComponent component = player.getData(ModAttachments.QUEST_COMPONENT);
        if(!component.getQuestProgress().containsKey(id)) {
            return;
        }

        QuestManager.progressQuest(player, id, 1f);
    }

    @Override
    public void startQuest(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos playerPos = player.blockPosition();
        Random random = new Random();
        BlockPos structurePos = null;

        int maxAttempts = 800;
        boolean structurePlaced = false;

        for (int attempt = 0; attempt < maxAttempts && !structurePlaced; attempt++) {
            // Generate random position within radius 75-150
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = 75 + random.nextDouble() * 75; // 75 to 150

            int offsetX = (int) (Math.cos(angle) * distance);
            int offsetZ = (int) (Math.sin(angle) * distance);

            int searchX = playerPos.getX() + offsetX;
            int searchZ = playerPos.getZ() + offsetZ;

            // Find suitable ground level (search from max build height down)
            BlockPos groundPos = findGroundLevel(level, searchX, searchZ);

            if (groundPos != null && canPlaceStructure(level, groundPos)) {
                placeStructure(level, groundPos, random);
                structurePos = groundPos;
                structurePlaced = true;
            }
        }

        if (!structurePlaced) {
            QuestManager.discardQuest(player, getId());
            return;
        }

        level.setBlockAndUpdate(structurePos.above(), ModBlocks.MYSTICAL_RING.get().defaultBlockState());
        BlockEntity blockEntity = level.getBlockEntity(structurePos.above());
        if (blockEntity instanceof MysticalRingBlockEntity ringBE) {
            ringBE.setPathway(pathway.isEmpty() ? BeyonderData.implementedPathways.get(random.nextInt(BeyonderData.implementedPathways.size())) : pathway);
            ringBE.setSequence(beyonderSequence);
        }

        QuestComponent component = player.getData(ModAttachments.QUEST_COMPONENT);
        component.getQuestLocation().put(getId(), new Vec3(structurePos.getX(), structurePos.getY() + 1, structurePos.getZ()));
    }

    /**
     * Finds the highest solid ground level at the given X/Z coordinates
     */
    private BlockPos findGroundLevel(ServerLevel level, int x, int z) {
        int maxY = level.getMaxBuildHeight() - 1;
        int minY = level.getMinBuildHeight();

        for (int y = maxY; y >= minY; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            BlockState above = level.getBlockState(pos.above());

            // Found ground: solid block with air above
            if (!state.isAir() && state.isSolidRender(level, pos) && above.isAir()) {
                return pos.above(); // Return the air block above ground
            }
        }

        return null; // No suitable ground found
    }

    /**
     * Checks if the structure can be placed at this location (only air blocks)
     */
    private boolean canPlaceStructure(ServerLevel level, BlockPos center) {
        int radius = 3;
        int pillarHeight = 4;

        // More lenient check - allow replacing grass/plants on floor
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos checkPos = center.offset(x, 0, z);
                BlockState state = level.getBlockState(checkPos);
                // Allow air or replaceable blocks (grass, flowers, etc.)
                if (!state.isAir() && !state.canBeReplaced()) {
                    return false;
                }
            }
        }

        // Check pillar clearance
        int[][] pillarOffsets = {{-2, -2}, {-2, 2}, {2, -2}, {2, 2}};
        for (int[] offset : pillarOffsets) {
            for (int y = 1; y <= pillarHeight; y++) {
                BlockPos pillarPos = center.offset(offset[0], y, offset[1]);
                BlockState state = level.getBlockState(pillarPos);
                if (!state.isAir() && !state.canBeReplaced()) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Places the stone structure at the given location
     */
    private void placeStructure(ServerLevel level, BlockPos center, Random random) {
        int radius = 3;

        // Place varied stone floor (7x7) with pattern
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos floorPos = center.offset(x, 0, z);

                // Create a border pattern
                if (Math.abs(x) == radius || Math.abs(z) == radius) {
                    level.setBlock(floorPos, Blocks.STONE_BRICKS.defaultBlockState(), 3);
                } else {
                    // Inner floor variation
                    float rand = random.nextFloat();
                    if (rand < 0.6f) {
                        level.setBlock(floorPos, Blocks.STONE.defaultBlockState(), 3);
                    } else if (rand < 0.85f) {
                        level.setBlock(floorPos, Blocks.COBBLESTONE.defaultBlockState(), 3);
                    } else {
                        level.setBlock(floorPos, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 3);
                    }
                }
            }
        }

        // Place 4 stone brick pillars at corners with variation
        int[][] pillarOffsets = {{-2, -2}, {-2, 2}, {2, -2}, {2, 2}};
        int pillarHeight = 4;

        for (int[] offset : pillarOffsets) {
            for (int y = 1; y <= pillarHeight; y++) {
                BlockPos pillarPos = center.offset(offset[0], y, offset[1]);

                // Add variation to pillars
                if (y == 1 || y == pillarHeight) {
                    level.setBlock(pillarPos, Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 3);
                } else if (random.nextFloat() < 0.2f) {
                    level.setBlock(pillarPos, Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), 3);
                } else {
                    level.setBlock(pillarPos, Blocks.STONE_BRICKS.defaultBlockState(), 3);
                }
            }

            // Add stone brick slab on top of each pillar
            BlockPos topPos = center.offset(offset[0], pillarHeight + 1, offset[1]);
            level.setBlock(topPos, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3);
        }

        // Add connecting stone brick walls between pillars at height 1
        // North wall
        for (int x = -1; x <= 1; x++) {
            BlockPos wallPos = center.offset(x, 1, -2);
            level.setBlock(wallPos, Blocks.STONE_BRICK_WALL.defaultBlockState(), 3);
        }
        // South wall
        for (int x = -1; x <= 1; x++) {
            BlockPos wallPos = center.offset(x, 1, 2);
            level.setBlock(wallPos, Blocks.STONE_BRICK_WALL.defaultBlockState(), 3);
        }
        // West wall
        for (int z = -1; z <= 1; z++) {
            BlockPos wallPos = center.offset(-2, 1, z);
            level.setBlock(wallPos, Blocks.STONE_BRICK_WALL.defaultBlockState(), 3);
        }
        // East wall
        for (int z = -1; z <= 1; z++) {
            BlockPos wallPos = center.offset(2, 1, z);
            level.setBlock(wallPos, Blocks.STONE_BRICK_WALL.defaultBlockState(), 3);
        }

        // Enhanced center decoration - small pedestal
        level.setBlock(center, Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 3);
        level.setBlock(center.above(), Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3);

        // Add corner accent blocks
        int[][] innerCorners = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        for (int[] corner : innerCorners) {
            BlockPos cornerPos = center.offset(corner[0], 0, corner[1]);
            level.setBlock(cornerPos, Blocks.POLISHED_ANDESITE.defaultBlockState(), 3);
        }

        // Add lanterns on pillars
        for (int[] offset : pillarOffsets) {
            BlockPos lanternPos = center.offset(offset[0], pillarHeight + 1, offset[1]);
            level.setBlock(lanternPos, Blocks.LANTERN.defaultBlockState(), 3);
        }

        // Add some scattered decorative blocks
        if (random.nextFloat() < 0.5f) {
            level.setBlock(center.offset(0, 1, -1), Blocks.POTTED_FERN.defaultBlockState(), 3);
        }
        if (random.nextFloat() < 0.5f) {
            level.setBlock(center.offset(0, 1, 1), Blocks.POTTED_DEAD_BUSH.defaultBlockState(), 3);
        }
    }
}

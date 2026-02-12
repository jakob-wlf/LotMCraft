package de.jakob.lotm.block.custom;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MysticalRingBlock extends Block {
    // Carpet-like hitbox (1 pixel tall = 1/16 of a block)
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 0.001D, 16.0D);

    // Optional override parameters (can be set via commands or code)
    private String forcedPathway = null;
    private Integer forcedSequence = null;

    // Lookup table: Block -> Pathway mapping
    private static final Map<Block, String> BLOCK_TO_PATHWAY = new HashMap<>();

    static {
        // Initialize the 22 pathway mappings (using placeholders for now)
        BLOCK_TO_PATHWAY.put(Blocks.PURPLE_WOOL, "fool");
        BLOCK_TO_PATHWAY.put(Blocks.BLUE_STAINED_GLASS, "door");
        BLOCK_TO_PATHWAY.put(Blocks.STONE, "error");
        BLOCK_TO_PATHWAY.put(Blocks.GLOWSTONE, "sun");
        BLOCK_TO_PATHWAY.put(Blocks.WHITE_WOOL, "visionary");
        BLOCK_TO_PATHWAY.put(Blocks.PRISMARINE, "tyrant");
        BLOCK_TO_PATHWAY.put(Blocks.OAK_PLANKS, "white_tower");
        BLOCK_TO_PATHWAY.put(Blocks.COAL_BLOCK, "hanged_man");
        BLOCK_TO_PATHWAY.put(Blocks.CRYING_OBSIDIAN, "darkness");
        BLOCK_TO_PATHWAY.put(Blocks.SHROOMLIGHT, "twilight_giant");
        BLOCK_TO_PATHWAY.put(Blocks.BONE_BLOCK, "death");
        BLOCK_TO_PATHWAY.put(Blocks.GLASS, "demoness");
        BLOCK_TO_PATHWAY.put(Blocks.NETHERRACK, "red_priest");
        BLOCK_TO_PATHWAY.put(Blocks.CRAFTING_TABLE, "paragon");
        BLOCK_TO_PATHWAY.put(Blocks.SANDSTONE, "hermit");
        BLOCK_TO_PATHWAY.put(Blocks.RED_SANDSTONE, "wheel_of_fortune");
        BLOCK_TO_PATHWAY.put(Blocks.NETHER_BRICKS, "chained");
        BLOCK_TO_PATHWAY.put(Blocks.DARK_PRISMARINE, "abyss");
        BLOCK_TO_PATHWAY.put(Blocks.SEA_LANTERN, "mother");
        BLOCK_TO_PATHWAY.put(Blocks.ICE, "moon");
        BLOCK_TO_PATHWAY.put(Blocks.AMETHYST_BLOCK, "justiciar");
        BLOCK_TO_PATHWAY.put(Blocks.COPPER_BLOCK, "black_emperor");
    }

    public MysticalRingBlock(Properties properties) {
        super(properties);
    }

    // Methods to set pathway and sequence via commands or code
    public void setPathway(String pathway) {
        this.forcedPathway = pathway;
    }

    public void setSequence(int sequence) {
        this.forcedSequence = sequence;
    }

    public void clearOverrides() {
        this.forcedPathway = null;
        this.forcedSequence = null;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            spawnBeyonder(player, (ServerLevel) level, pos);
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.CONSUME;
    }

    private void spawnBeyonder(Player player, ServerLevel level, BlockPos pos) {
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());

        String pathway;
        int sequence;

        // Use forced values if set, otherwise determine from block structure
        if (forcedPathway != null && forcedSequence != null) {
            pathway = forcedPathway;
            sequence = forcedSequence;
        } else {
            // Get the block below the ring
            BlockPos belowPos = pos.below();
            BlockState belowState = level.getBlockState(belowPos);
            Block baseBlock = belowState.getBlock();

            // Determine pathway from lookup table
            pathway = BLOCK_TO_PATHWAY.getOrDefault(baseBlock,
                    BeyonderData.implementedPathways.get(new Random().nextInt(BeyonderData.implementedPathways.size())));

            // Count consecutive matching blocks below (including the base block)
            int consecutiveBlocks = countConsecutiveBlocks(level, belowPos, baseBlock);

            // Determine sequence based on consecutive block count
            // 4 consecutive blocks = sequence 1
            // 3 consecutive blocks = sequence 2
            // 2 consecutive blocks = sequence 3
            // 1 consecutive block = sequence 4
            sequence = switch (consecutiveBlocks) {
                case 4 -> 1;
                case 3 -> 2;
                case 2 -> 3;
                default -> 4; // 1 or 0 blocks
            };

            // Use forced values if only one is set
            if (forcedPathway != null) {
                pathway = forcedPathway;
            }
            if (forcedSequence != null) {
                sequence = forcedSequence;
            }
        }

        int colorInt = BeyonderData.pathwayInfos.get(pathway).color();
        float baseRed = ((colorInt >> 16) & 0xFF) / 255.0f;
        float baseGreen = ((colorInt >> 8) & 0xFF) / 255.0f;
        float baseBlue = (colorInt & 0xFF) / 255.0f;

        DustParticleOptions dustParticle = new DustParticleOptions(
                new Vector3f(baseRed, baseGreen, baseBlue),
                2f
        );

        ParticleUtil.createParticleSpirals(level, dustParticle, pos.getCenter(), 1, 1.75, 2, .5, 2, 20 * 8, 8, 4);

        int finalSequence = sequence;
        String finalPathway = pathway;
        ServerScheduler.scheduleDelayed(20 * 8, () -> {
            BeyonderNPCEntity beyonder = new BeyonderNPCEntity(ModEntities.BEYONDER_NPC.get(), level, true, finalPathway, finalSequence);
            beyonder.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            level.addFreshEntity(beyonder);
        });
    }

    /**
     * Counts consecutive blocks of the same type going downward from the starting position
     * @param level The world level
     * @param startPos The starting position
     * @param targetBlock The block type to count
     * @return Number of consecutive matching blocks (max 4)
     */
    private int countConsecutiveBlocks(Level level, BlockPos startPos, Block targetBlock) {
        int count = 0;
        BlockPos currentPos = startPos;

        // Count up to 4 consecutive blocks
        for (int i = 0; i < 4; i++) {
            BlockState currentState = level.getBlockState(currentPos);
            if (currentState.getBlock() == targetBlock) {
                count++;
                currentPos = currentPos.below();
            } else {
                break;
            }
        }

        return count;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F; // Full brightness, prevents darkening
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true; // Allows light to pass through
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            spawnBeyonder(player, (ServerLevel) level, pos);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }
}
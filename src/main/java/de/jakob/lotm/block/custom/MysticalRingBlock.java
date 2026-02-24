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
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MysticalRingBlock extends Block implements EntityBlock {
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 0.001D, 16.0D);

    private static final Map<Block, String> BLOCK_TO_PATHWAY = new HashMap<>();

    static {
        BLOCK_TO_PATHWAY.put(Blocks.NETHERRACK, "red_priest");
        BLOCK_TO_PATHWAY.put(Blocks.STONE, "pathway_placeholder_1");
        BLOCK_TO_PATHWAY.put(Blocks.OBSIDIAN, "pathway_placeholder_2");
        BLOCK_TO_PATHWAY.put(Blocks.DIAMOND_BLOCK, "pathway_placeholder_3");
        BLOCK_TO_PATHWAY.put(Blocks.GOLD_BLOCK, "pathway_placeholder_4");
        BLOCK_TO_PATHWAY.put(Blocks.IRON_BLOCK, "pathway_placeholder_5");
        BLOCK_TO_PATHWAY.put(Blocks.EMERALD_BLOCK, "pathway_placeholder_6");
        BLOCK_TO_PATHWAY.put(Blocks.LAPIS_BLOCK, "pathway_placeholder_7");
        BLOCK_TO_PATHWAY.put(Blocks.REDSTONE_BLOCK, "pathway_placeholder_8");
        BLOCK_TO_PATHWAY.put(Blocks.COAL_BLOCK, "pathway_placeholder_9");
        BLOCK_TO_PATHWAY.put(Blocks.QUARTZ_BLOCK, "pathway_placeholder_10");
        BLOCK_TO_PATHWAY.put(Blocks.PRISMARINE, "pathway_placeholder_11");
        BLOCK_TO_PATHWAY.put(Blocks.END_STONE, "pathway_placeholder_12");
        BLOCK_TO_PATHWAY.put(Blocks.PURPUR_BLOCK, "pathway_placeholder_13");
        BLOCK_TO_PATHWAY.put(Blocks.SANDSTONE, "pathway_placeholder_14");
        BLOCK_TO_PATHWAY.put(Blocks.RED_SANDSTONE, "pathway_placeholder_15");
        BLOCK_TO_PATHWAY.put(Blocks.NETHER_BRICKS, "pathway_placeholder_16");
        BLOCK_TO_PATHWAY.put(Blocks.DARK_PRISMARINE, "pathway_placeholder_17");
        BLOCK_TO_PATHWAY.put(Blocks.SEA_LANTERN, "pathway_placeholder_18");
        BLOCK_TO_PATHWAY.put(Blocks.GLOWSTONE, "pathway_placeholder_19");
        BLOCK_TO_PATHWAY.put(Blocks.AMETHYST_BLOCK, "pathway_placeholder_20");
        BLOCK_TO_PATHWAY.put(Blocks.COPPER_BLOCK, "pathway_placeholder_21");
    }

    public MysticalRingBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MysticalRingBlockEntity(pos, state);
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
        BlockEntity blockEntity = level.getBlockEntity(pos);

        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());

        String pathway;
        int sequence;

        // Check if BlockEntity has stored settings
        if (blockEntity instanceof MysticalRingBlockEntity ringBE && ringBE.hasSettings()) {
            pathway = ringBE.getPathway();
            sequence = ringBE.getSequence();
        } else {
            // Determine from block structure
            BlockPos belowPos = pos.below();
            BlockState belowState = level.getBlockState(belowPos);
            Block baseBlock = belowState.getBlock();

            pathway = BLOCK_TO_PATHWAY.getOrDefault(baseBlock,
                    BeyonderData.implementedPathways.get(new Random().nextInt(BeyonderData.implementedPathways.size())));

            int consecutiveBlocks = countConsecutiveBlocks(level, belowPos, baseBlock);

            sequence = switch (consecutiveBlocks) {
                case 4 -> 1;
                case 3 -> 2;
                case 2 -> 3;
                default -> 4;
            };
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

        player.getPersistentData().putBoolean("lotm_summoned_beyonder_with_ring", true);

        int finalSequence = sequence;
        String finalPathway = pathway;
        ServerScheduler.scheduleDelayed(20 * 8, () -> {
            BeyonderNPCEntity beyonder = new BeyonderNPCEntity(ModEntities.BEYONDER_NPC.get(), level, true, finalPathway, finalSequence);
            beyonder.getPersistentData().putUUID("lotm_beyonder_summoner", player.getUUID());
            beyonder.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            level.addFreshEntity(beyonder);
        });
    }

    private int countConsecutiveBlocks(Level level, BlockPos startPos, Block targetBlock) {
        int count = 0;
        BlockPos currentPos = startPos;

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
        return 1.0F;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
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
package de.jakob.lotm.item.custom;

import de.jakob.lotm.fluid.ModFluids;
import de.jakob.lotm.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * A sealed bottle made from an empty bottle + door characteristic in an anvil.
 * Right-clicking it while looking at Drops of Eternal Darkness fluid collects
 * the fluid into an Eternal Darkness River Water Bottle.
 */
public class SealedBottleItem extends Item {

    public SealedBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // pick() with includeFluid=true so the raycast hits fluid blocks
        HitResult hit = player.pick(5.0, 1.0F, true);
        ItemStack held = player.getItemInHand(hand);

        if (!(hit instanceof BlockHitResult blockHit)) {
            return InteractionResultHolder.pass(held);
        }

        BlockPos pos = blockHit.getBlockPos();
        FluidState fluidState = level.getFluidState(pos);

        boolean isDark = fluidState.is(ModFluids.DROPS_OF_ETERNAL_DARKNESS_SOURCE.get())
                || fluidState.is(ModFluids.DROPS_OF_ETERNAL_DARKNESS_FLOWING.get());

        if (!isDark) {
            return InteractionResultHolder.pass(held);
        }

        if (level.isClientSide) {
            return InteractionResultHolder.success(held);
        }

        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0f, 1.0f);

        ItemStack result = new ItemStack(ModItems.ETERNAL_DARKNESS_RIVER_WATER_BOTTLE.get());
        held.shrink(1);
        if (held.isEmpty()) {
            player.setItemInHand(hand, result);
            return InteractionResultHolder.consume(result);
        } else {
            if (!player.getInventory().add(result)) {
                player.drop(result, false);
            }
            return InteractionResultHolder.consume(held);
        }
    }
}

package de.jakob.lotm.addons.factions.mixin;

import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(targets = "com.simibubi.create.foundation.utility.BlockHelper")
public class FactionMixin {

    @Inject(
            method = "destroyBlockAs(Lnet/minecraft/world/level/Level;" +
                    "Lnet/minecraft/core/BlockPos;" +
                    "Lnet/minecraft/world/entity/player/Player;" +
                    "Lnet/minecraft/world/item/ItemStack;" +
                    "FLjava/util/function/Consumer;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void claims$blockCheck(
            Level level,
            BlockPos pos,
            Player player,
            ItemStack usedTool,
            float effectChance,
            Consumer<ItemStack> droppedItemCallback,
            CallbackInfo ci
    ) {

        if (level.isClientSide()) {
            return;
        }

        if (player != null) {
            return;
        }

        ChunkPos chunkPos = new ChunkPos(pos);

        if (!BeyonderData.factionStorage.getFaction(chunkPos).isEmpty()) {
            ci.cancel();
        }
    }
}
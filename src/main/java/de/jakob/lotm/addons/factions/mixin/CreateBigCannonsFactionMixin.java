package de.jakob.lotm.addons.factions.mixin;

import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "rbasamoyai.createbigcannons.munitions.ProjectileDamageHooks")
public class CreateBigCannonsFactionMixin {

    @Inject(
            method = "canDamageTerrain(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void checkClaim(
            Level level,
            BlockPos pos,
            CallbackInfoReturnable<Boolean> cir
    ) {

        if (level.isClientSide()) {
            return;
        }

        ChunkPos chunkPos = new ChunkPos(pos);

        if (!BeyonderData.factionStorage.getFaction(chunkPos).isEmpty()) {
            cir.setReturnValue(false);
        }
    }
}

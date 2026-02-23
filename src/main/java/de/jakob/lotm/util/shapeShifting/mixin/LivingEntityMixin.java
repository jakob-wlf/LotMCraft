package de.jakob.lotm.util.shapeShifting.mixin;

import de.jakob.lotm.util.shapeShifting.TransformData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

//    @Inject(method = "getJumpPower", at = @At("HEAD"), cancellable = true)
//    private void onGetJumpPower(CallbackInfoReturnable<Float> cir) {
//        if ((Object)this instanceof Player player) {
//            cir.setReturnValue(0.42f * calculateScale(player));
//        }
//    }

    @Inject(method = "maxUpStep", at = @At("HEAD"), cancellable = true)
    private void onMaxUpStep(CallbackInfoReturnable<Float> cir) {
        if ((Object)this instanceof Player player) {
            cir.setReturnValue(0.6f * calculateScale(player));
        }
    }

    private float calculateScale(Player player) {
        TransformData data = (TransformData) player;
        String shapeKey = data.getCurrentShape();

        // skip for players and beyonders because the same model
        if (shapeKey != null && !shapeKey.startsWith("player:") && !shapeKey.startsWith("lotmcraft:beyonder_npc:")) {
            EntityType<?> type = null;

            ResourceLocation id = ResourceLocation.tryParse(shapeKey);
            if (id != null) {
                type = BuiltInRegistries.ENTITY_TYPE.get(id);
            }

            if (type != null) {
                Entity entity = type.create(player.level());
                if (entity != null) {
                    return Math.max(entity.getBbHeight() / 2.0f, 1.0f);
                }
            }
        }
        return 1.0f;
    }
}
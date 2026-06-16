package de.jakob.lotm.util.shapeShifting.mixin;

import de.jakob.lotm.util.shapeShifting.ShapeShiftAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    private EntityDimensions dimensions;
    @Shadow private float eyeHeight;
    @Shadow public abstract Pose getPose();
    @Shadow public abstract float getEyeHeight(Pose pose);
    @Shadow public abstract AABB getBoundingBox();
    @Shadow public abstract void move(MoverType type, Vec3 vec);
    @Shadow public abstract double getX();
    @Shadow public abstract double getY();
    @Shadow public abstract double getZ();

    private Entity createShapeEntity(String shapeKey, Player player) {
        if (shapeKey == null || shapeKey.isEmpty()) return null;

        // skip for players and beyonders because same shape
        if (shapeKey.startsWith("player:") || shapeKey.startsWith("lotmcraft:beyonder_npc:")) return null;

        EntityType<?> type = null;

        // get the entity type (like minecraft:something or lotmcraft:something)
        ResourceLocation entityID = ResourceLocation.tryParse(shapeKey);
        if (entityID != null) {
            type = BuiltInRegistries.ENTITY_TYPE.get(entityID);
        }
        if (type == null) return null;

        return type.create(player.level());
    }

    @Inject(method = "getBbWidth", at = @At("HEAD"), cancellable = true)
    private void getBbWidth(CallbackInfoReturnable<Float> cir) {
        if ((Entity)(Object)this instanceof Player) {
            cir.setReturnValue(this.dimensions.width());
        }
    }

    @Inject(method = "getBbHeight", at = @At("HEAD"), cancellable = true)
    private void getBbHeight(CallbackInfoReturnable<Float> cir) {
        if ((Entity)(Object)this instanceof Player) {
            cir.setReturnValue(this.dimensions.height());
        }
    }

    @Inject(method = "getEyeHeight()F", at = @At("HEAD"), cancellable = true)
    private void getEyeHeight(CallbackInfoReturnable<Float> cir) {
        if ((Entity)(Object)this instanceof Player) {
            cir.setReturnValue(this.eyeHeight);
        }
    }

    // inject at the end of the method so that we can override any changes we want
    @Inject(method = "refreshDimensions", at = @At("TAIL"))
    private void onRefreshDimensions(CallbackInfo ci) {
        Entity self = (Entity)(Object)this;
        if (self instanceof Player player) {
            String shape = ShapeShiftAccess.getShapeShift(player).getShape();
            boolean onlySkin = ShapeShiftAccess.getShapeShift(player).isSkinOnly();
            if (!shape.isEmpty()) {
                Entity entityShape = createShapeEntity(shape, player);

                if (entityShape != null && !onlySkin) {
                    Pose pose = player.getPose();
                    EntityDimensions shapeDims = entityShape.getDimensions(pose);
                    this.dimensions = shapeDims;

                    // for changing eye height based on player pose even for other entities
                    float baseEye = entityShape.getEyeHeight(pose);

                    if (pose == Pose.SWIMMING || pose == Pose.FALL_FLYING) {
                        this.eyeHeight = baseEye * 0.35f;
                    } else if (pose == Pose.CROUCHING) {
                        this.eyeHeight = baseEye * 0.92f;
                    } else {
                        this.eyeHeight = baseEye;
                    }

                    // change hit box
                    double x = self.getX(), y = self.getY(), z = self.getZ();
                    double w = shapeDims.width();
                    double h = shapeDims.height();
                    self.setBoundingBox(new AABB(x-w/2, y, z-w/2, x+w/2, y+h, z+w/2));
                }
            }
        }
    }
}
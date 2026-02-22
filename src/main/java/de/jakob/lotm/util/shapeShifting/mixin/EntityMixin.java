package de.jakob.lotm.util.shapeShifting.mixin;

import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.util.shapeShifting.DimensionsRefresher;
import de.jakob.lotm.util.shapeShifting.TransformData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements DimensionsRefresher {
    @Shadow
    private EntityDimensions dimensions;
    @Shadow private float eyeHeight;
    @Shadow protected boolean firstTick;
    @Shadow public abstract Pose getPose();
    @Shadow public abstract EntityDimensions getDimensions(Pose pose);
    @Shadow public abstract float getEyeHeight(Pose pose);
    @Shadow public abstract AABB getBoundingBox();
    @Shadow public abstract void setBoundingBox(AABB aabb);
    @Shadow public abstract void move(MoverType type, Vec3 vec);
    @Shadow public abstract double getX();
    @Shadow public abstract double getY();
    @Shadow public abstract double getZ();

    private Entity createShapeEntity(String shapeKey, Player player) {

        // skip for players and beyonders because same shape, if in the future beyonders had new models this will change
        if (shapeKey == null || shapeKey.startsWith("player:") || shapeKey.startsWith("lotmcraft:beyonder_npc:")) return null;

        EntityType<?> type = null;

        ResourceLocation id = ResourceLocation.tryParse(shapeKey);
        if (id != null) {
            type = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(id);
        }

        if (type == null) return null;

        Entity shape = type.create(player.level());

        if (shape != null) {
            shape.setPose(player.getPose());

            if (shape instanceof BeyonderNPCEntity npc
                    && shapeKey.startsWith("lotmcraft:beyonder_npc:")) {

                String skinName = shapeKey.substring("lotmcraft:beyonder_npc:".length());
                if (!skinName.isEmpty()) {
                    npc.setSkinName(skinName);
                }
            }
        }

        return shape;
    }

    @Inject(method = "getBbWidth", at = @At("HEAD"), cancellable = true)
    private void getBbWidth(CallbackInfoReturnable<Float> cir) {
        if ((Entity)(Object)this instanceof Player player) {
            cir.setReturnValue(this.dimensions.width());
        }
    }

    @Inject(method = "getBbHeight", at = @At("HEAD"), cancellable = true)
    private void getBbHeight(CallbackInfoReturnable<Float> cir) {
        if ((Entity)(Object)this instanceof Player player) {
            cir.setReturnValue(this.dimensions.height());
        }
    }

    @Inject(method = "getEyeHeight()F", at = @At("HEAD"), cancellable = true)
    private void getEyeHeight(CallbackInfoReturnable<Float> cir) {
        if ((Entity)(Object)this instanceof Player player) {
            cir.setReturnValue(this.eyeHeight);
        }
    }

    @Inject(method = "refreshDimensions", at = @At("TAIL"))
    private void onRefreshDimensions(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        this.shape_refreshDimensions();
    }

    // refreshes entity dimensions
    @Override
    public void shape_refreshDimensions() {
        Entity self = (Entity)(Object)this;
        if (self instanceof Player player) {
            TransformData data = (TransformData) player;
            Entity shape = createShapeEntity(data.getCurrentShape(), player);
            if (shape != null) {
                Pose pose = player.getPose();
                EntityDimensions shapeDims = shape.getDimensions(pose);
                this.dimensions = shapeDims;

                // for changing eye height based on player pose even for other entities
                float baseEye = shape.getEyeHeight(pose);

                if (pose == Pose.SWIMMING || pose == Pose.FALL_FLYING) {
                    this.eyeHeight = baseEye * 0.35f;
                } else if (pose == Pose.CROUCHING) {
                    this.eyeHeight = baseEye * 0.92f;
                } else {
                    this.eyeHeight = baseEye;
                }

                double x = self.getX(), y = self.getY(), z = self.getZ();
                double w = shapeDims.width();
                double h = shapeDims.height();
                self.setBoundingBox(new AABB(x-w/2, y, z-w/2, x+w/2, y+h, z+w/2));
                return;
            }
        }

        Pose pose = self.getPose();
        EntityDimensions newDims = self.getDimensions(pose);
        this.dimensions = newDims;
        this.eyeHeight = self.getEyeHeight(pose);

        AABB box = self.getBoundingBox();
        self.setBoundingBox(new AABB(box.minX, box.minY, box.minZ,
                box.minX + newDims.width(),
                box.minY + newDims.height(),
                box.minZ + newDims.width()));

        if (!this.firstTick) {
            float diff = this.dimensions.width() - newDims.width();
            self.move(MoverType.SELF, new Vec3(diff, 0.0, diff));
        }
    }
}
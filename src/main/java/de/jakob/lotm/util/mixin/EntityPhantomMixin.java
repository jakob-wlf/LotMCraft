package de.jakob.lotm.util.mixin;

import de.jakob.lotm.beyonders.abilities.abyss.passives.DevilBloodlines.DevilBloodlineAbilites.Phantom.PhantomFormAttachment;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Entity.class)
public abstract class EntityPhantomMixin {

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void lotm$phantomMove(MoverType type, Vec3 movement, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof Player player) || !PhantomFormAttachment.isGhost(player)) return;
        Vec3 pos = player.position();
        AABB box = player.getBoundingBox();
        double newX = pos.x + movement.x;
        double newZ = pos.z + movement.z;
        double newY;
        boolean landed;
        if (player.onGround() && movement.y <= 0) {
            double step = player.maxUpStep();
            double desiredDown = step - movement.y; // positive distance
            AABB raised = box.move(movement.x, step, movement.z);
            double down = Shapes.collide(Direction.Axis.Y, raised, shapes(player, raised.expandTowards(0, -desiredDown, 0)), -desiredDown);
            newY = pos.y + step + down;
            landed = down > -desiredDown + 1.0E-7;
        } else {
            // Jumping or falling: plain vertical collision at the new position
            AABB moved = box.move(movement.x, 0, movement.z);
            double y = Shapes.collide(Direction.Axis.Y, moved, shapes(player, moved.expandTowards(0, movement.y, 0)), movement.y);
            newY = pos.y + y;
            landed = movement.y < 0 && Math.abs(movement.y - y) > 1.0E-7;
        }

        player.setPos(newX, newY, newZ);
        boolean verticalHit = landed || Math.abs(movement.y - (newY - pos.y)) > 1.0E-7 && movement.y > 0;
        player.horizontalCollision = false;
        player.verticalCollision = verticalHit;
        player.verticalCollisionBelow = landed;
        player.setOnGround(landed);
        if (verticalHit) {
            Vec3 delta = player.getDeltaMovement();
            player.setDeltaMovement(delta.x, 0, delta.z);
        }
        ci.cancel();
    }
    private static List<VoxelShape> shapes(Player player, AABB area) {
        List<VoxelShape> shapes = new ArrayList<>();
        for (VoxelShape shape : player.level().getBlockCollisions(player, area)) {
            shapes.add(shape);
        }
        return shapes;
    }
}
package de.jakob.lotm.util.shapeShifting.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.jakob.lotm.util.shapeShifting.TransformData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerMixin implements TransformData {
    @Unique
    private String currentShape = null;

    @Override
    public String getCurrentShape() {
        return currentShape;
    }

    @Override
    public void setCurrentShape(String shape) {
        this.currentShape = shape;
    }

    // for changing the entity and block reach
    @ModifyReturnValue(method = "entityInteractionRange", at = @At("RETURN"))
    private double entityInteractionRangeChange(double original) {
        if ((Object)this instanceof Player player) {
            return original * calculateScale(player);
        }
        return original;
    }

    @ModifyReturnValue(method = "blockInteractionRange", at = @At("RETURN"))
    private double blockInteractionRangeChange(double original) {
        if ((Object)this instanceof Player player) {
            return original * calculateScale(player);
        }
        return original;
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
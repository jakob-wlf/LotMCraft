package de.jakob.lotm.util.shapeShifting.mixin;

import com.mojang.authlib.GameProfile;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.ShapeShiftComponent;
import de.jakob.lotm.util.shapeShifting.PlayerSkinData;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends Player {

    public AbstractClientPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    // change the players skin for player models
    @Inject(method = "getSkin", at = @At("HEAD"), cancellable = true)
    private void onGetSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;
        ShapeShiftComponent data = player.getData(ModAttachments.SHAPE_SHIFT);
        String shape = data.getShape();

        if (shape != null && !shape.isEmpty()) {
            if (shape.startsWith("player:")) {
                String[] parts = shape.split(":");

                // check if uuid exists or else the game will crash
                if (parts[2] != null) {
                    UUID targetUUID = UUID.fromString(parts[2]);
                    ResourceLocation texture = PlayerSkinData.getSkinTexture(targetUUID);
                    boolean slim = PlayerSkinData.isSlimModel(targetUUID);

                    if (texture != null) {
                        PlayerSkin customSkin = new PlayerSkin(
                                texture,
                                null,
                                null,
                                null,
                                slim ? PlayerSkin.Model.SLIM : PlayerSkin.Model.WIDE,
                                false
                        );
                        cir.setReturnValue(customSkin);
                    }
                }
            }
        }
    }
}
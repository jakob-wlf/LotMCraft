package de.jakob.lotm.util.helper;

import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class AnimationUtil {

    private static final ResourceLocation openArmsAnimationID = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "animation.model.open_arms");


    public static void playOpenArmAnimation(Player player) {
        if (player.level().isClientSide && player instanceof AbstractClientPlayer clientPlayer) {
            PlayerAnimationController controller = (PlayerAnimationController) PlayerAnimationAccess.getPlayerAnimationLayer(clientPlayer, LOTMCraft.ANIMATION_LAYER_ID);
            if(controller == null) {
                return;
            }
            controller.triggerAnimation(openArmsAnimationID);
        }
    }

}

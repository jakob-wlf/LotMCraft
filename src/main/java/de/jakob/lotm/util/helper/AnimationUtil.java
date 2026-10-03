package de.jakob.lotm.util.helper;

import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.PlayAnimationPacket;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public class AnimationUtil {

    private static final ResourceLocation openArmsAnimationID = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "animation.model.open_arms");


    public static void playOpenArmAnimation(Player player) {
        if (player.level().isClientSide && player instanceof AbstractClientPlayer clientPlayer) {
            playAnimation(clientPlayer, openArmsAnimationID);
        }
        else {
            PacketHandler.sendToTrackingAndSelf(player, new PlayAnimationPacket(player.getId(), "open_arms"));
        }
    }

    public static void playAnimation(AbstractClientPlayer player, ResourceLocation id) {
        PlayerAnimationController controller = (PlayerAnimationController) PlayerAnimationAccess.getPlayerAnimationLayer(player, LOTMCraft.ANIMATION_LAYER_ID);
        if(controller == null) {
            return;
        }
        controller.triggerAnimation(id);
    }

    public static ResourceLocation getResourceLocationById(String id) {
        return switch (id) {
            case "open_arms" -> openArmsAnimationID;
            default -> null;
        };
    }

}

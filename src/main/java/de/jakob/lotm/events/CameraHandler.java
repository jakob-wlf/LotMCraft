package de.jakob.lotm.events;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TransformationComponent;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

import java.lang.reflect.Method;

@EventBusSubscriber(value = Dist.CLIENT, modid = "lotmcraft")
public class CameraHandler {
    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null || mc.options.getCameraType().isFirstPerson()) {
            return;
        }

        TransformationComponent transformationComponent = player.getData(ModAttachments.TRANSFORMATION_COMPONENT);

        if (transformationComponent.isTransformed() &&
                transformationComponent.getTransformationIndex() == 101) { // Values between 100 and 200 are reserved for Mythical Creature Forms

            event.setYaw(event.getYaw() + 45);
        }
    }
}
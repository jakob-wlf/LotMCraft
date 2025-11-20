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

    private static Method moveMethod;
    private static Method getMaxZoomMethod;

    static {
        try {
            moveMethod = Camera.class.getDeclaredMethod("move", float.class, float.class, float.class);
            moveMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            System.err.println("Could not find Camera.move method!");
            e.printStackTrace();
        }

        try {
            getMaxZoomMethod = Camera.class.getDeclaredMethod("getMaxZoom", float.class);
            getMaxZoomMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            System.err.println("Could not find Camera.getMaxZoom method!");
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null || mc.options.getCameraType().isFirstPerson()) {
            return;
        }

        TransformationComponent transformationComponent = player.getData(ModAttachments.TRANSFORMATION_COMPONENT);

        if (transformationComponent.isTransformed() &&
                transformationComponent.getTransformationIndex() == TransformationComponent.TransformationType.TYRANT_MYTHICAL_CREATURE.getIndex()) {

            if (moveMethod == null) {
                return;
            }

            try {
                Camera camera = event.getCamera();

                moveMethod.invoke(camera, -4.0f, 0.0f, 0.0f);

                System.out.println("Moved camera back an additional 4 blocks");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
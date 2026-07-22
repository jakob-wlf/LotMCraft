package de.jakob.lotm.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.HashMap;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class DiscernmentRenderer {

    public static final HashMap<UUID, Integer> activeDiscernment = new HashMap<>();


    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        event.getSkins().forEach(skin -> {
            addLayerIfPossible(event.getSkin(skin));
        });

        event.getEntityTypes().forEach(entityType -> {
            addLayerIfPossible(event.getRenderer(entityType));
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addLayerIfPossible(Object renderer) {
        if (renderer instanceof LivingEntityRenderer livingRenderer) {
            livingRenderer.addLayer(new DiscernmentRendererLayer(livingRenderer));
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        if(!activeDiscernment.containsKey(mc.player.getUUID())) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();

        for (Entity e : mc.level.entitiesForRendering()) {

            if (!(e instanceof LivingEntity entity)) continue;
            if (entity == mc.player) continue;
            if (entity.distanceTo(mc.player) > activeDiscernment.get(mc.player.getUUID())) continue;

            EntityRenderer<? super LivingEntity> renderer =
                    mc.getEntityRenderDispatcher().getRenderer(entity);

            poseStack.pushPose();

            poseStack.translate(
                    entity.getX() - cam.x,
                    entity.getY() - cam.y,
                    entity.getZ() - cam.z
            );

            boolean invisible = entity.isInvisible();
            entity.setInvisible(false);

            renderer.render(
                    entity,
                    entity.getYRot(),
                    event.getPartialTick().getGameTimeDeltaPartialTick(false),
                    poseStack,
                    buffer,
                    LightTexture.FULL_BRIGHT
            );

            entity.setInvisible(invisible);

            poseStack.popPose();
        }

        buffer.endBatch();
    }
}

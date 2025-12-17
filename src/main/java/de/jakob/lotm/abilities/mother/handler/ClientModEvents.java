package de.jakob.lotm.abilities.mother.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class ClientModEvents {
    
    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Pre<?, ?> event) {
        if(event.getEntity() instanceof LivingEntity living) {
            CompoundTag persistentData = living.getPersistentData();
            
            if(persistentData.contains("HybridMobData")) {
                HybridMobData hybridData = HybridMobData.load(persistentData.getCompound("HybridMobData"));
                ResourceLocation modelEntityType = hybridData.getModelEntityType();
                
                EntityType<?> originalType = EntityType.byString(modelEntityType.toString()).orElse(null);
                
                if(originalType != null) {
                    Entity dummyEntity = originalType.create(living.level());
                    
                    if(dummyEntity instanceof LivingEntity livingDummy) {
                        // Copy all rendering-relevant data
                        livingDummy.copyPosition(living);
                        livingDummy.setYRot(living.getYRot());
                        livingDummy.setXRot(living.getXRot());
                        livingDummy.yBodyRot = living.yBodyRot;
                        livingDummy.yHeadRot = living.yHeadRot;
                        livingDummy.yBodyRotO = living.yBodyRotO;
                        livingDummy.yHeadRotO = living.yHeadRotO;

                        // Get and use the original renderer
                        EntityRenderer<?> originalRenderer = Minecraft.getInstance()
                            .getEntityRenderDispatcher()
                            .getRenderer(livingDummy);

                        renderEntity((EntityRenderer) originalRenderer, livingDummy,
                                living.getYRot(),
                                event.getPartialTick(),
                                event.getPoseStack(),
                                event.getMultiBufferSource(),
                                event.getPackedLight());

                        // Cancel the original rendering
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity> void renderEntity(EntityRenderer<T> renderer, T entity,
                                                        float yaw, float partialTick, PoseStack poseStack,
                                                        MultiBufferSource buffer, int light) {
        renderer.render(entity, yaw, partialTick, poseStack, buffer, light);
    }

}
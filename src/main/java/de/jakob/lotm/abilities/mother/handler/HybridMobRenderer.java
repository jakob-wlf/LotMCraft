package de.jakob.lotm.abilities.mother.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@SuppressWarnings("unchecked")
@OnlyIn(Dist.CLIENT)
public class HybridMobRenderer extends LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> {
    
    public HybridMobRenderer(EntityRendererProvider.Context context) {
        super(context, null, 0.5f);
    }

    @Override
    public void render(LivingEntity entity, float entityYaw, float partialTicks, 
                      PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        
        CompoundTag persistentData = entity.getPersistentData();
        
        if(persistentData.contains("HybridMobData")) {
            HybridMobData hybridData = HybridMobData.load(persistentData.getCompound("HybridMobData"));
            ResourceLocation modelEntityType = hybridData.getModelEntityType();
            
            // Get the original entity type's renderer
            EntityType<?> originalType = EntityType.byString(modelEntityType.toString()).orElse(null);
            
            if(originalType != null) {
                Entity dummyEntity = originalType.create(entity.level());
                
                if(dummyEntity instanceof LivingEntity livingDummy) {
                    // Copy relevant data to dummy entity for rendering
                    livingDummy.copyPosition(entity);
                    livingDummy.setYRot(entity.getYRot());
                    livingDummy.setXRot(entity.getXRot());
                    livingDummy.yBodyRot = entity.yBodyRot;
                    livingDummy.yHeadRot = entity.yHeadRot;
                    livingDummy.yBodyRotO = entity.yBodyRotO;
                    livingDummy.yHeadRotO = entity.yHeadRotO;
                    
                    // Get the renderer for the original entity type
                    EntityRenderer<?> originalRenderer = Minecraft.getInstance()
                        .getEntityRenderDispatcher()
                        .getRenderer(livingDummy);

                    // Render using the original model
                    renderEntity((EntityRenderer) originalRenderer, livingDummy,
                            entityYaw, partialTicks, poseStack, buffer, packedLight);
                    return;
                }
            }
        }
        
        // Fallback to default rendering
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(LivingEntity entity) {
        CompoundTag persistentData = entity.getPersistentData();

        if(persistentData.contains("HybridMobData")) {
            HybridMobData hybridData = HybridMobData.load(persistentData.getCompound("HybridMobData"));
            ResourceLocation modelEntityType = hybridData.getModelEntityType();

            EntityType<?> originalType = EntityType.byString(modelEntityType.toString()).orElse(null);
            if(originalType != null) {
                Entity dummyEntity = originalType.create(entity.level());
                if(dummyEntity instanceof LivingEntity livingDummy) {
                    EntityRenderer<?> originalRenderer = Minecraft.getInstance()
                        .getEntityRenderDispatcher()
                        .getRenderer(livingDummy);
                    return getEntityTexture((EntityRenderer) originalRenderer, livingDummy);
                }
            }
        }

        return ResourceLocation.withDefaultNamespace("textures/entity/pig/pig.png");
    }

    private <T extends Entity> void renderEntity(EntityRenderer<T> renderer, T entity,
                                                 float yaw, float partialTicks, PoseStack poseStack,
                                                 MultiBufferSource buffer, int light) {
        renderer.render(entity, yaw, partialTicks, poseStack, buffer, light);
    }

    private <T extends Entity> ResourceLocation getEntityTexture(EntityRenderer<T> renderer, T entity) {
        return renderer.getTextureLocation(entity);
    }
}
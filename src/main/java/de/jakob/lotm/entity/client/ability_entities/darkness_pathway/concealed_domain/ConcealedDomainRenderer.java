package de.jakob.lotm.entity.client.ability_entities.darkness_pathway.concealed_domain;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.entity.custom.ability_entities.darkness_pathway.ConcealedDomainEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ConcealedDomainRenderer extends EntityRenderer<ConcealedDomainEntity> {

    public ConcealedDomainRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ConcealedDomainEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Intentionally empty — domain is represented by void blocks
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull ConcealedDomainEntity entity) {
        return ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    }
}
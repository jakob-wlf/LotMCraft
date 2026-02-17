package de.jakob.lotm.entity.client;

import de.jakob.lotm.entity.OriginalBodyEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;


public class OriginalBodyRenderer extends LivingEntityRenderer<OriginalBodyEntity, PlayerModel<OriginalBodyEntity>> {
    private static final ResourceLocation DEFAULT_STEVE = ResourceLocation.withDefaultNamespace("textures/entity/player/slim/steve.png");

    public OriginalBodyRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(OriginalBodyEntity entity) {
        return DEFAULT_STEVE;
    }
}
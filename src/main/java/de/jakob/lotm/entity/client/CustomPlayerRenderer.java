package de.jakob.lotm.entity.client;

import de.jakob.lotm.entity.custom.CustomPlayerEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CustomPlayerRenderer extends MobRenderer<CustomPlayerEntity, PlayerModel<CustomPlayerEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/player/wide/steve.png");

    public CustomPlayerRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(CustomPlayerEntity entity) {
        return TEXTURE;
    }
}

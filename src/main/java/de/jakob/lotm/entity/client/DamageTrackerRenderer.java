package de.jakob.lotm.entity.client;

import de.jakob.lotm.entity.custom.DamageTrackerEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class DamageTrackerRenderer extends MobRenderer<DamageTrackerEntity, PlayerModel<DamageTrackerEntity>> {

    public DamageTrackerRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(DamageTrackerEntity entity) {
        return DefaultPlayerSkin.getDefaultTexture();
    }
}

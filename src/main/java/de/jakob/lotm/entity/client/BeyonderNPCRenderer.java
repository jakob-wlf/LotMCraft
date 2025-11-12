package de.jakob.lotm.entity.client;

import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class BeyonderNPCRenderer extends MobRenderer<BeyonderNPCEntity, PlayerModel<BeyonderNPCEntity>> {
    public BeyonderNPCRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        this.addLayer(new QuestMarkerLayer(this));
        this.addLayer(new PuppetSoldierLayer(this, context.getModelSet()));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(BeyonderNPCEntity entity) {
        return entity.getSkinTexture();
    }
}


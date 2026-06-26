package de.jakob.lotm.entity.client.beyonder_npc;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.util.shapeShifting.PlayerSkinData;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class BeyonderNPCRenderer extends MobRenderer<BeyonderNPCEntity, PlayerModel<BeyonderNPCEntity>> {
    private final PlayerModel<BeyonderNPCEntity> wideModel;
    private final PlayerModel<BeyonderNPCEntity> slimModel;

    public BeyonderNPCRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        this.wideModel = this.model;
        this.slimModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        this.addLayer(new QuestMarkerLayer(this));
        this.addLayer(new TradeIndicatorLayer(this));
        this.addLayer(new PuppetSoldierLayer(this, context.getModelSet()));
    }

    @Override
    public void render(BeyonderNPCEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // switch the active model based on the cached skin data
        entity.getTargetPlayerUUID().ifPresent(uuid -> {
            this.model = PlayerSkinData.isSlimModel(uuid) ? slimModel : wideModel;
        });

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(BeyonderNPCEntity entity) {
        if (entity.getTargetPlayerUUID().isPresent()) {

            // try to get the cached skin
            ResourceLocation cached = PlayerSkinData.getSkinTexture(entity.getTargetPlayerUUID().get());
            if (cached != null) {
                return cached;
            }

            // trigger background fetch if not cached
            PlayerSkinData.fetchAndCacheSkin(entity.getTargetPlayerUUID().get());

            // return vanilla default (should be the same skin as the player even for non original minecraft)
            return DefaultPlayerSkin.get(entity.getTargetPlayerUUID().get()).texture();
        }
        else {
            return entity.getSkinTexture();
        }
    }
}
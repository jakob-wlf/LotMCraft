package de.jakob.lotm.entity.client;

import de.jakob.lotm.attachments.ControllingDataComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.entity.OriginalBodyEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

import static de.jakob.lotm.util.shapeShifting.PlayerSkinData.getSkinTexture;


public class OriginalBodyRenderer extends LivingEntityRenderer<OriginalBodyEntity, PlayerModel<OriginalBodyEntity>> {

    public OriginalBodyRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(OriginalBodyEntity entity) {
        ControllingDataComponent originalBodyData = entity.getData(ModAttachments.CONTROLLING_DATA);

        if (originalBodyData == null || originalBodyData.getOwnerUUID() == null) {
            return DefaultPlayerSkin.getDefaultTexture();
        }

        return getSkinTexture(originalBodyData.getOwnerUUID());
    }
}
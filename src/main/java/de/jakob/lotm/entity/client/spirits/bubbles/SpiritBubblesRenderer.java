package de.jakob.lotm.entity.client.spirits.bubbles;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.spirits.dervish.SpiritDervishModel;
import de.jakob.lotm.entity.custom.spirits.SpiritBubblesEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritDervishEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class SpiritBubblesRenderer extends MobRenderer<SpiritBubblesEntity, SpiritBubblesModel<SpiritBubblesEntity>> {
    public SpiritBubblesRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiritBubblesModel<>(context.bakeLayer(SpiritBubblesModel.LAYER_LOCATION)), .3f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(SpiritBubblesEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/spirits/spirit_bubbles/spirit_bubbles.png");
    }

    @Override
    public void render(SpiritBubblesEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.translate(0, -.32, 0);

        Random random = new Random(entity.getUUID().getMostSignificantBits() ^ entity.getUUID().getLeastSignificantBits());
        float scale = 1f + random.nextFloat();
        poseStack.scale(scale, scale, scale);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Nullable
    @Override
    protected RenderType getRenderType(SpiritBubblesEntity livingEntity, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(this.getTextureLocation(livingEntity));
    }
}

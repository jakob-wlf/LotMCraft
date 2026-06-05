package de.jakob.lotm.entity.client.uniqueness;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.uniqueness.UniquenessEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Renders the UniquenessEntity as a floating, slowly rotating item on the ground,
 * using the corresponding pathway uniqueness item texture.
 */
@OnlyIn(Dist.CLIENT)
public class UniquenessEntityRenderer extends EntityRenderer<UniquenessEntity> {

    public UniquenessEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(UniquenessEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        String pathway = entity.getPathway();
        if (pathway.isEmpty()) return;

        ItemStack stack = getUniquenessItemStack(pathway);
        if (stack.isEmpty()) return;

        poseStack.pushPose();

        float tick = entity.getTicksExisted() + partialTick;

        poseStack.scale(1, 1, 1);
        poseStack.translate(0, .75, 0);

        poseStack.mulPose(Axis.YP.rotationDegrees(tick * .5f));

        // Render the item using the game's item renderer
        Minecraft mc = Minecraft.getInstance();
        mc.getItemRenderer().renderStatic(
                stack,
                net.minecraft.world.item.ItemDisplayContext.GROUND,
                packedLight,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                mc.level,
                entity.getId()
        );

        poseStack.popPose();
    }

    private ItemStack getUniquenessItemStack(String pathway) {
        try {
            net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, pathway + "_uniqueness")
            );
            if (item == Items.AIR) return ItemStack.EMPTY;
            return new ItemStack(item);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public ResourceLocation getTextureLocation(UniquenessEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

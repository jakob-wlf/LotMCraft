package de.jakob.lotm.entity.client.projectiles.water_bolt;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.projectiles.fireball.FireballModel;
import de.jakob.lotm.entity.custom.projectiles.FireballEntity;
import de.jakob.lotm.entity.custom.projectiles.WaterBoltEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class WaterBoltRenderer extends EntityRenderer<WaterBoltEntity> {


    public WaterBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(WaterBoltEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
    }

    protected int getBlockLightLevel(WaterBoltEntity projectileEntity, BlockPos blockpos) {
        return 15;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull WaterBoltEntity flamingSpearProjectileEntity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/fireball/fireball.png");
    }
}
package de.jakob.lotm.entity.client.ability_entities.body_double;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.entity.custom.ability_entities.ControlBodyDouble;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.UUID;


public class ControlBodyDoubleRenderer extends LivingEntityRenderer<ControlBodyDouble, PlayerModel<ControlBodyDouble>> {

    private static final ResourceLocation DEFAULT_SKIN =
            ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");

    private final PlayerModel<ControlBodyDouble> wideModel;
    private final PlayerModel<ControlBodyDouble> slimModel;

    public ControlBodyDoubleRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        this.wideModel = this.getModel();
        this.slimModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);

    }

    @Override
    public void render(ControlBodyDouble entity, float entityYaw, float partialTicks,
                        PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        PlayerSkin skin = resolveSkin(entity.getOwnerUUID());
        this.model = skin.model() == PlayerSkin.Model.SLIM ? this.slimModel : this.wideModel;
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(ControlBodyDouble entity) {
        return resolveSkin(entity.getOwnerUUID()).texture();
    }

    private static PlayerSkin resolveSkin(@Nullable UUID ownerUUID) {
        if (ownerUUID != null && Minecraft.getInstance().getConnection() != null) {
            PlayerInfo info = Minecraft.getInstance().getConnection().getPlayerInfo(ownerUUID);
            if (info != null) {
                return info.getSkin();
            }
        }

        return new PlayerSkin(DEFAULT_SKIN, null, null, null, PlayerSkin.Model.WIDE, true);
    }

    @Override
    protected boolean shouldShowName(ControlBodyDouble entity) {
        return false;
    }
}
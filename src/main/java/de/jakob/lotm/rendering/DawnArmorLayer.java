package de.jakob.lotm.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.beyonders.abilities.twilight_giant.ArsenalOfDawnAbility;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;

public class DawnArmorLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public DawnArmorLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffer, int light, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float age, float yaw, float pitch) {
        boolean helmet = worn(player, EquipmentSlot.HEAD);
        boolean chest = worn(player, EquipmentSlot.CHEST);
        boolean legs = worn(player, EquipmentSlot.LEGS);
        boolean boots = worn(player, EquipmentSlot.FEET);
        if (!helmet && !chest && !legs && !boots) return;
        PlayerModel<AbstractClientPlayer> model = getParentModel();
        VertexConsumer consumer = buffer.getBuffer(RenderType.armorCutoutNoCull(DawnArmorModels.TEXTURE));
        int overlay = LivingEntityRenderer.getOverlayCoords(player, 0.0F);
        if (helmet) DawnArmorModels.render(pose, consumer, light, overlay, model.head, "head");
        if (chest) {
            DawnArmorModels.render(pose, consumer, light, overlay, model.body, "body");
            DawnArmorModels.render(pose, consumer, light, overlay, model.body, "waist");
            DawnArmorModels.render(pose, consumer, light, overlay, model.rightArm, "right_arm");
            DawnArmorModels.render(pose, consumer, light, overlay, model.leftArm, "left_arm");
        }
        if (legs) {
            DawnArmorModels.render(pose, consumer, light, overlay, model.rightLeg, "right_leg");
            DawnArmorModels.render(pose, consumer, light, overlay, model.leftLeg, "left_leg");
        }
        if (boots) {
            DawnArmorModels.render(pose, consumer, light, overlay, model.rightLeg, "right_shoe");
            DawnArmorModels.render(pose, consumer, light, overlay, model.leftLeg, "left_shoe");
        }
    }

    private static boolean worn(AbstractClientPlayer player, EquipmentSlot slot) {
        String kind = switch (slot) {
            case HEAD -> ArsenalOfDawnAbility.HELMET;
            case CHEST -> ArsenalOfDawnAbility.CHESTPLATE;
            case LEGS -> ArsenalOfDawnAbility.LEGGINGS;
            case FEET -> ArsenalOfDawnAbility.BOOTS;
            default -> "";
        };
        return ArsenalOfDawnAbility.is(player.getItemBySlot(slot), kind);
    }
}

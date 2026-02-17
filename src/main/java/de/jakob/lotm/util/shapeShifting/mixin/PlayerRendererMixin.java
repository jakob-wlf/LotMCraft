package de.jakob.lotm.util.shapeShifting.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.util.shapeShifting.NameStorage;
import de.jakob.lotm.util.shapeShifting.TransformData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public PlayerRendererMixin(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context,
                               PlayerModel<AbstractClientPlayer> model, float shadowRadius) {
        super(context, model, shadowRadius);
    }


    // ---- name display part ----
    @ModifyVariable(
            method = "renderNameTag(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IF)V",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private Component modifyDisplayName(Component displayName, AbstractClientPlayer entity) {
        String nickname = NameStorage.mapping.get(entity.getUUID());
        if (nickname != null) {
            return Component.literal(nickname);
        }
        return displayName;
    }

    // ---- shape render part ----

    @Unique
    private Entity cachedRenderEntity = null;
    @Unique
    private EntityType<?> lastShapeType = null;
    @Unique
    private String lastShapeKey = null;

    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"), cancellable = true)
    private void renderTransformed(AbstractClientPlayer player, float yaw, float tickDelta,
                                   PoseStack matrices, MultiBufferSource vertexConsumers,
                                   int light, CallbackInfo ci) {
        TransformData data = (TransformData) player;
        String shapeKey = data.getCurrentShape();

        if (shapeKey != null && !shapeKey.startsWith("player:")) {
            ci.cancel();
            updateCachedEntity(player, shapeKey);

            if (cachedRenderEntity != null) {
                updateEntityState(player, cachedRenderEntity);
                EntityRenderer<?> entityRenderer = Minecraft.getInstance()
                        .getEntityRenderDispatcher().getRenderer(cachedRenderEntity);

                ((EntityRenderer<Entity>) entityRenderer).render(
                        cachedRenderEntity, yaw, tickDelta,
                        matrices, vertexConsumers, light);
            }
        }
    }

    private EntityType<?> parseEntityTypeFromKey(String shapeKey) {
        if (shapeKey.startsWith("lotmcraft:beyonder_npc:")) {
            return net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(
                    ResourceLocation.parse("lotmcraft:beyonder_npc")
            );
        } else {
            ResourceLocation id = ResourceLocation.tryParse(shapeKey);
            if (id != null) {
                return net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(id);
            }
        }
        return null;
    }

    @Unique
    private void updateCachedEntity(AbstractClientPlayer player, String shapeKey) {
        EntityType<?> shapeType = parseEntityTypeFromKey(shapeKey);
        if (shapeType == null) {
            cachedRenderEntity = null;
            lastShapeType = null;
            lastShapeKey = null;
            return;
        }

        boolean needsUpdate = cachedRenderEntity == null
                || lastShapeType == null
                || !lastShapeType.equals(shapeType)
                || lastShapeKey == null
                || !lastShapeKey.equals(shapeKey);

        if (needsUpdate) {
            cachedRenderEntity = shapeType.create(player.level());
            if (cachedRenderEntity != null) {
                applySkinIfNeeded(cachedRenderEntity, shapeKey);
                cachedRenderEntity.setPos(player.getX(), player.getY(), player.getZ());
            }
            lastShapeType = shapeType;
            lastShapeKey = shapeKey;
        } else {
            applySkinIfNeeded(cachedRenderEntity, shapeKey);
        }
    }

    @Unique
    private void applySkinIfNeeded(Entity entity, String shapeKey) {
        if (entity instanceof BeyonderNPCEntity npc
                && shapeKey.startsWith("lotmcraft:beyonder_npc:")) {

            String skinName = shapeKey.substring("lotmcraft:beyonder_npc:".length());

            if (!skinName.isEmpty() && !npc.getSkinName().equals(skinName)) {
                npc.setSkinName(skinName);
            }
        }
    }

    @Unique
    private void updateEntityState(AbstractClientPlayer player, Entity entity) {
        if (entity == null) return;

        entity.setPos(player.getX(), player.getY(), player.getZ());
        entity.setYRot(player.getYRot());
        entity.setXRot(player.getXRot());
        entity.yRotO = player.yRotO;
        entity.xRotO = player.xRotO;
        entity.setPose(player.getPose());

        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.yBodyRot = player.yBodyRot;
            livingEntity.yBodyRotO = player.yBodyRotO;
            livingEntity.yHeadRot = player.yHeadRot;
            livingEntity.yHeadRotO = player.yHeadRotO;

            synchronizeAnimations(player, livingEntity);
        }

        entity.tickCount++;
    }

    @Unique
    private void synchronizeAnimations(AbstractClientPlayer player, LivingEntity livingEntity) {
        synchronizeWalkAnimation(player, livingEntity);
        synchronizeSwingAnimation(player, livingEntity);
    }

    @Unique
    private void synchronizeWalkAnimation(AbstractClientPlayer player, LivingEntity livingEntity) {
        LimbAnimatorAccessor playerAnim = (LimbAnimatorAccessor) player.walkAnimation;
        LimbAnimatorAccessor entityAnim = (LimbAnimatorAccessor) livingEntity.walkAnimation;

        entityAnim.setPos(player.walkAnimation.position());
        entityAnim.setSpeed(playerAnim.getSpeed());
        entityAnim.setPrevSpeed(playerAnim.getPrevSpeed());

        livingEntity.walkAnimation.speed(player.walkAnimation.speed());
        livingEntity.walkAnimation.update(player.walkAnimation.speed(), 1.0f);
    }

    @Unique
    private void synchronizeSwingAnimation(AbstractClientPlayer player, LivingEntity livingEntity) {
        livingEntity.swingTime = player.swingTime;
        livingEntity.swinging = player.swinging;
        livingEntity.attackAnim = player.attackAnim;
        livingEntity.oAttackAnim = player.oAttackAnim;

        if (player.swinging) {
            livingEntity.swingingArm = player.swingingArm;
        } else {
            livingEntity.swingingArm = null;
        }
    }
}
package de.jakob.lotm.abilities.error;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.ParasitationComponent;
import de.jakob.lotm.attachments.TransformationComponent;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ParasitationAbility extends ToggleAbility {

    private static final HashMap<UUID, UUID> hostMap = new HashMap<>();
    public ParasitationAbility(String id) {
        super(id);

        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 1;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            return;
        }

        LivingEntity host = AbilityUtil.getTargetEntity(entity, 8, 2);
        if(host == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.parasitation.no_target").withColor(0x3240bf));
            return;
        }

        if(BeyonderData.isBeyonder(host) && BeyonderData.getSequence(host) <= BeyonderData.getSequence(entity)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.parasitation.target_too_strong").withColor(0xbf3232));
            return;
        }

        hostMap.put(entity.getUUID(), host.getUUID());

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        transformationComponent.setTransformedAndSync(true, entity);
        transformationComponent.setTransformationIndexAndSync(TransformationComponent.TransformationType.PARASTATION, entity);

        ParasitationComponent parasitationComponent = host.getData(ModAttachments.PARASITE_COMPONENT);
        parasitationComponent.setParasited(true);
        parasitationComponent.setParasiteUUID(entity.getUUID());
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if(entity instanceof Player player) {
            player.setBoundingBox(new AABB(
                    player.getX(), player.getY(), player.getZ(),
                    player.getX(), player.getY(), player.getZ()
            ));
            player.onUpdateAbilities();
            player.hurtMarked = true;
        }

        if(!hostMap.containsKey(entity.getUUID())) {
            cancel(serverLevel, entity);
            return;
        }

        Entity host = serverLevel.getEntity(hostMap.get(entity.getUUID()));
        if(host == null || host.isRemoved() || host.distanceToSqr(entity) > 128 || !host.isAlive() || !(host instanceof LivingEntity)) {
            cancel(serverLevel, entity);
            return;
        }

        Vec3 dir = new Vec3(entity.getLookAngle().x(), 0, entity.getLookAngle().z()).normalize().scale(Math.min(-.85f, -1 * host.getBbWidth()));
        Vec3 hostPos = entity.position().add(dir);

        host.teleportTo(serverLevel, hostPos.x(), hostPos.y(), hostPos.z(), Set.of(), entity.getYRot(), entity.getXRot());

        AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 64).forEach(e -> {
            if(e != host && e != entity && e instanceof Mob mob && mob.getTarget() == entity) {
                mob.setTarget(null);
                mob.hurtMarked = true;
            }
        });

        if(host instanceof Mob mob) {
            mob.setTarget(entity.getLastHurtMob());
            mob.hurtMarked = true;
        }

        host.fallDistance = 0;
        host.hurtMarked = true;

        entity.setRemainingFireTicks(0);
        entity.getActiveEffects().stream()
                .map(MobEffectInstance::getEffect)
                .filter(effect -> effect.value().getCategory() == MobEffectCategory.HARMFUL)
                .toList()
                .forEach(entity::removeEffect);


        // Stop when overridden by another transformation^1
        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if (!transformationComponent.isTransformed() || transformationComponent.getTransformationIndex() != TransformationComponent.TransformationType.PARASTATION.getIndex()) {
            cancel((ServerLevel) level, entity);
            return;
        }
    }



    @Override
    public void stop(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel))
            return;

        if(hostMap.containsKey(entity.getUUID())) {
            Entity host = serverLevel.getEntity(hostMap.get(entity.getUUID()));
            if(host != null && host.isAlive() && host instanceof LivingEntity livingHost) {
                ParasitationComponent parasitationComponent = livingHost.getData(ModAttachments.PARASITE_COMPONENT);
                parasitationComponent.setParasited(false);
                parasitationComponent.setParasiteUUID(null);
            }
        }

        hostMap.remove(entity.getUUID());

        if(entity instanceof Player player) {
            player.setBoundingBox(player.getDimensions(player.getPose()).makeBoundingBox(
                    player.getX(), player.getY(), player.getZ()
            ));
            player.onUpdateAbilities();
            player.hurtMarked = true;
        }

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if(transformationComponent.isTransformed() && transformationComponent.getTransformationIndex() == TransformationComponent.TransformationType.PARASTATION.getIndex()) {
            transformationComponent.setTransformedAndSync(false, entity);
        }
    }

    public static LivingEntity getHostForEntity(ServerLevel serverLevel, LivingEntity parasite) {
        if(!hostMap.containsKey(parasite.getUUID())) {
            return null;
        }

        UUID hostUUID = hostMap.get(parasite.getUUID());
        Entity host = serverLevel.getEntity(hostUUID);
        if(host instanceof LivingEntity livingHost) {
            return livingHost;
        }

        return null;
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }

        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (newTarget == null || !hostMap.containsKey(newTarget.getUUID())) {
            return;
        }

        event.setCanceled(true);
    }
}

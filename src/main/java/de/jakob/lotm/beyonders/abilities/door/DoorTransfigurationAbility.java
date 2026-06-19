package de.jakob.lotm.beyonders.abilities.door;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TransformationComponent;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class DoorTransfigurationAbility extends ToggleAbility {
    private static final HashMap<UUID, Integer> doorSizeModifier = new HashMap<>();

    public DoorTransfigurationAbility(String id) {
        super(id, "escape");

        this.tickRate = 1;
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide) return;
        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if (!transformationComponent.isTransformed() ||
                transformationComponent.getTransformationIndex() <= TransformationComponent.TransformationType.DOOR_TRANSFIGURATION.getIndex() ||
                transformationComponent.getTransformationIndex() >= TransformationComponent.TransformationType.DOOR_TRANSFIGURATION.getIndex() + 20
        ) {
            cancel((ServerLevel) level, entity);
            return;
        }

        if(BeyonderData.getSpirituality(entity) < 280) {
            cancel((ServerLevel) level, entity);
            return;
        }

        entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 30, 1, false, false, false));
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        doorSizeModifier.put(entity.getUUID(), 2);

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        transformationComponent.setTransformedAndSync(true, entity);
        transformationComponent.setTransformationIndexAndSync(TransformationComponent.TransformationType.DOOR_TRANSFIGURATION.getIndex() + 2, entity);
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        doorSizeModifier.remove(entity.getUUID());

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if(transformationComponent.isTransformed() &&
                transformationComponent.getTransformationIndex() >= TransformationComponent.TransformationType.DOOR_TRANSFIGURATION.getIndex() &&
                transformationComponent.getTransformationIndex() <= (TransformationComponent.TransformationType.DOOR_TRANSFIGURATION.getIndex() + 20)) {
            transformationComponent.setTransformedAndSync(false, entity);
        }
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("door", 4);
    }

    @Override
    protected float getSpiritualityCost() {
        return 14;
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if(entity.level().isClientSide) {
            return;
        }

        if(!((DoorTransfigurationAbility) LOTMCraft.abilityHandler.getById("door_transfiguration_ability")).isActiveForEntity(entity)) {
            return;
        }

        doorSizeModifier.computeIfPresent(entity.getUUID(), (k, v) -> Math.min(15, v + 1));
        if(!doorSizeModifier.containsKey(entity.getUUID())) return;
        int sizeModifier = doorSizeModifier.get(entity.getUUID());

        System.out.println(sizeModifier);

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if(transformationComponent.isTransformed() &&
                transformationComponent.getTransformationIndex() >= TransformationComponent.TransformationType.DOOR_TRANSFIGURATION.getIndex() &&
                transformationComponent.getTransformationIndex() <= (TransformationComponent.TransformationType.DOOR_TRANSFIGURATION.getIndex() + 20)) {
            transformationComponent.setTransformedAndSync(true, entity);
            transformationComponent.setTransformationIndexAndSync(TransformationComponent.TransformationType.DOOR_TRANSFIGURATION.getIndex() + sizeModifier, entity);
        }

        if(BeyonderData.getSpirituality(entity) >= 280) {
            BeyonderData.reduceSpirituality(entity, 280);
        }

        event.setCanceled(true);

        entity.level().playSound(null, entity.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, entity.getSoundSource(), .1f, 1f);

        Vec3 particleLoc = entity.getEyePosition().subtract(0, entity.getEyeHeight() / 2, 0);
        ParticleUtil.spawnParticles((ServerLevel) entity.level(), ParticleTypes.END_ROD, particleLoc, 120, 1.2, 1.75, 1.2, .1);
        ParticleUtil.spawnParticles((ServerLevel) entity.level(), ModParticles.STAR.get(), particleLoc, 100, 1.2, 1.75f, 1.2, .1);

        Vec3 newTeleportLoc = findSafeTeleportLoc(entity);
        if(newTeleportLoc != null) {
            entity.teleportTo(newTeleportLoc.x, newTeleportLoc.y, newTeleportLoc.z);
        }
    }

    private static Vec3 findSafeTeleportLoc(LivingEntity entity) {
        RandomSource random = entity.getRandom();
        Vec3 currentLoc = entity.position();
        Vec3 teleportLoc;
        for(int i = 0; i < 100; i++) {
            teleportLoc = currentLoc.add((random.nextDouble() - .5) * 5, random.nextDouble(), (random.nextDouble() - .5) * 5);
            BlockState state = entity.level().getBlockState(BlockPos.containing(teleportLoc));
            if(state.getCollisionShape(entity.level(), BlockPos.containing(teleportLoc)).isEmpty()) {
                return teleportLoc;
            }
        }

        return null;
    }
}

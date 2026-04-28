package de.jakob.lotm.abilities.fool;

import com.zigythebird.playeranimcore.math.Vec3f;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.FogComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TransformationComponent;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class HistoricalVoidHidingAbility extends ToggleAbility {
    private final HashMap<UUID, Vec3> locations = new HashMap<>();

    public HistoricalVoidHidingAbility(String id) {
        super(id);

        canAlwaysBeUsed = true;
        cannotBeStolen = true;
        canBeCopied = false;
        canBeReplicated = false;
        canBeUsedInArtifact = false;
        canBeShared = false;
    }

    @Override
    public float getSpiritualityCost() {
        return 250;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 3));
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            return;
        }

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        transformationComponent.setTransformedAndSync(true, entity);
        transformationComponent.setTransformationIndexAndSync(TransformationComponent.TransformationType.FOG_OF_HISTORY, entity);

        locations.put(entity.getUUID(), entity.position().add(0, 5, 0));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            return;
        }

        // Fog effect
        FogComponent fogComponent = entity.getData(ModAttachments.FOG_COMPONENT);
        fogComponent.setActiveAndSync(true, entity);
        fogComponent.setFogIndexAndSync(FogComponent.FOG_TYPE.FOG_OF_HISTORY, entity);
        fogComponent.setFogColorAndSync(new Vec3f(1, 1, 1), entity);

        // Stop from moving
        entity.setDeltaMovement(0, 0, 0);
        entity.setNoGravity(true);
        if(locations.containsKey(entity.getUUID())) {
            entity.teleportTo(locations.get(entity.getUUID()).x, locations.get(entity.getUUID()).y, locations.get(entity.getUUID()).z);
        }

        //Stop from taking damage and make invisible
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 50, 10, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 50, 10, false, false, false));

        //Stop nearby mobs from attacking
        AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.position(), 20).forEach(e -> {
            if(e instanceof Mob mob && mob.getTarget() == entity) {
                mob.setTarget(null);
            }
        });

        DisabledAbilitiesComponent component = entity.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
        component.disableAbilityUsageForTime("hidden_in_historical_void", 30, entity);

        // Stop when overridden by another transformation
        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if (!transformationComponent.isTransformed() || transformationComponent.getTransformationIndex() != TransformationComponent.TransformationType.FOG_OF_HISTORY.getIndex()) {
            cancel((ServerLevel) level, entity);
            return;
        }

    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        entity.setNoGravity(false);
        locations.remove(entity.getUUID());

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if(transformationComponent.isTransformed() && transformationComponent.getTransformationIndex() == TransformationComponent.TransformationType.FOG_OF_HISTORY.getIndex()) {
            transformationComponent.setTransformedAndSync(false, entity);
        }

    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if(!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity entity = event.getEntity();

        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if (transformationComponent.isTransformed() && transformationComponent.getTransformationIndex() == TransformationComponent.TransformationType.FOG_OF_HISTORY.getIndex())
            event.setCanceled(true);

    }
}

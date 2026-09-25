package de.jakob.lotm.beyonders.abilities.fool.marionettes;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.EntityControllingComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.PhysicalEnhancementsAbility;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.entity.custom.ability_entities.ControlBodyDouble;
import de.jakob.lotm.events.BeyonderDataTickHandler;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncControllingPacket;
import de.jakob.lotm.network.packets.toServer.RequestControllingSyncPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityBarHelper;
import de.jakob.lotm.util.helper.AbilityWheelHelper;
import de.jakob.lotm.util.helper.AllyUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import de.jakob.lotm.util.shapeShifting.ShapeShiftingUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Set;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ControllingUtils {

    public static boolean startControlling(ServerPlayer player, LivingEntity target, boolean keepOwnAbilities, boolean spawnOriginalBody) {
        if(target == null || !target.isAlive() || target instanceof Player) return false;

        if(target.level() != player.level()) {
            return false;
        }

        EntityControllingComponent component = player.getData(ModAttachments.ENTITY_CONTROLLING_COMPONENT);

        if(component.isControlling()) {
            return false;
        }

        ServerLevel level = player.serverLevel();

        component.captureAttributesFrom(player);
        System.out.println("very first call");
        PhysicalEnhancementsAbility.removeAllEnhancementsForEntity(player);
        System.out.println("this after");
        BeyonderDataTickHandler.invalidateCache(player);

        ControlBodyDouble controlBodyDouble = ControlBodyDouble.create(level, player, !spawnOriginalBody);
        level.addFreshEntity(controlBodyDouble);
        component.setBodyDouble(controlBodyDouble);
        player.removeAllEffects();

        AllyUtil.makeAllies(player, controlBodyDouble, false);

        component.setControlling(true);
        component.setCanUseOwnAbilities(keepOwnAbilities);
        component.setControlledEntity(target);
        component.setControlledEntityPathway(BeyonderData.getPathway(target));
        component.setControlledEntitySequence(BeyonderData.getSequence(target));

        component.setActiveToggles(ToggleAbility.getActiveAbilitiesForEntity(player));
        ToggleAbility.cleanUp(player.serverLevel(), player);

        component.setAbilityWheelAbilities(player.getData(ModAttachments.ABILITY_WHEEL_COMPONENT).getAbilities());
        component.setSelectedAbilityInWheel(player.getData(ModAttachments.ABILITY_WHEEL_COMPONENT).getSelectedAbility());
        component.setAbilityBarAbilities(player.getData(ModAttachments.ABILITY_BAR_COMPONENT).getAbilities());

        AbilityWheelHelper.setAbilities(player, target.getData(ModAttachments.ABILITY_WHEEL_COMPONENT).getAbilities());
        AbilityWheelHelper.setSelectedAbility(player, target.getData(ModAttachments.ABILITY_WHEEL_COMPONENT).getSelectedAbility());
        AbilityBarHelper.setAbilities(player, target.getData(ModAttachments.ABILITY_BAR_COMPONENT).getAbilities());

        syncControllingData(player);

        ShapeShiftingUtil.shapeShift(player, target, false);
        player.teleportTo(level, target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());

        copyAttributesAndHealthFrom(target, player);

        target.discard();
        return true;
    }

    public static void copyAttributesAndHealthFrom(LivingEntity source, LivingEntity target) {
        System.out.println("ayyributes");
        for (Holder<Attribute> attributeHolder : BuiltInRegistries.ATTRIBUTE.asHolderIdMap()) {

            if (attributeHolder.is(Attributes.MOVEMENT_SPEED)) {
                continue;
            }

            AttributeInstance sourceInstance = source.getAttribute(attributeHolder);
            AttributeInstance targetInstance = target.getAttribute(attributeHolder);

            if (sourceInstance != null && targetInstance != null) {
                targetInstance.setBaseValue(sourceInstance.getBaseValue());

                // clear then copy all modifiers as well, to avoid health not updating and to avoid scheduling as well
                for (AttributeModifier modifier : targetInstance.getModifiers()) {
                    targetInstance.removeModifier(modifier);
                }

                for (AttributeModifier modifier : sourceInstance.getModifiers()) {
                    targetInstance.addTransientModifier(modifier);
                }
            }
        }
        System.out.println("health");
        float maxHealth = target.getMaxHealth();
        float sourceHealth = source.getHealth();
        target.setHealth(Math.min(sourceHealth, maxHealth));
        System.out.println("health done");
    }

    public static PathwayData currentlyControlling(Player player) {
        if(player.level().isClientSide()) {
            PacketHandler.sendToServer(new RequestControllingSyncPacket());
        }
        EntityControllingComponent component = player.getData(ModAttachments.ENTITY_CONTROLLING_COMPONENT);
        return new PathwayData(component.getControlledEntityPathway(), component.getControlledEntitySequence());
    }

    public static boolean isControlling(Player player) {
        if (player.level().isClientSide()) {
            PacketHandler.sendToServer(new RequestControllingSyncPacket());
        }
        return player.getData(ModAttachments.ENTITY_CONTROLLING_COMPONENT).isControlling();
    }

    public static boolean canUseOwnAbilitiesWhileControlling(Player player) {
        if(player.level().isClientSide()) {
            PacketHandler.sendToServer(new RequestControllingSyncPacket());
        }
        return player.getData(ModAttachments.ENTITY_CONTROLLING_COMPONENT).isCanUseOwnAbilities();
    }

    public static void cancel(ServerPlayer player, float damage, boolean returnPreviousEntity, boolean killPreviousEntity) {
        if(player == null) return;

        EntityControllingComponent component = player.getData(ModAttachments.ENTITY_CONTROLLING_COMPONENT);

        if(!component.isControlling()) {
            return;
        }

        component.setControlling(false);

        ServerLevel level = player.serverLevel();

        if(returnPreviousEntity) {
            LivingEntity controlled = component.getControlledEntity();
            if(controlled != null) {
                controlled.unsetRemoved();
                controlled.setPos(player.position());
                level.addFreshEntity(controlled);
                controlled.setHealth(Math.clamp(player.getHealth(), 1, controlled.getHealth()));
                if(killPreviousEntity) {
                    controlled.hurt(controlled.damageSources().generic(), Float.MAX_VALUE);
                }

                AbilityWheelHelper.setAbilitiesForEntity(player, controlled, player.getData(ModAttachments.ABILITY_WHEEL_COMPONENT).getAbilities());
                AbilityBarHelper.setAbilities(controlled, player.getData(ModAttachments.ABILITY_BAR_COMPONENT).getAbilities());
            }
        }

        ShapeShiftingUtil.resetShape(player);
        BeyonderDataTickHandler.invalidateCache(player);
        PhysicalEnhancementsAbility.removeAllEnhancementsForEntity(player);


        ControlBodyDouble controlBodyDouble = component.getBodyDouble();
        if(controlBodyDouble == null || !controlBodyDouble.isAlive()) {
            component.restoreAttributesTo(player);
        }
        else {
            copyAttributesAndHealthFrom(controlBodyDouble, player);
            controlBodyDouble.reapplyHeldEffectsTo(player);

            if(!controlBodyDouble.renderInvisible()) {
                ServerLevel returnLevel = (ServerLevel) controlBodyDouble.level(); // Should always be the same level but just to be sure

                player.teleportTo(returnLevel, controlBodyDouble.getX(), controlBodyDouble.getY(), controlBodyDouble.getZ(), Set.of(), controlBodyDouble.getYRot(), controlBodyDouble.getXRot());
                if(damage > 0) {
                    player.hurt(player.damageSources().generic(), damage);
                }
            }

            AllyUtil.removeAllies(player, controlBodyDouble, false);
            controlBodyDouble.discard();
        }

        AbilityBarHelper.setAbilities(player, component.getAbilityBarAbilities());
        AbilityWheelHelper.setAbilities(player, component.getAbilityWheelAbilities());
        AbilityWheelHelper.setSelectedAbility(player, component.getSelectedAbilityInWheel());

        component.getActiveToggles().forEach(toggle -> toggle.useAbility(level, player));

        component.reset();
    }

    public static void syncControllingData(ServerPlayer player) {
        PacketHandler.sendToPlayer(player, new SyncControllingPacket(
                isControlling(player),
                currentlyControlling(player).pathway(),
                currentlyControlling(player).sequence(),
                canUseOwnAbilitiesWhileControlling(player)
        ));
    }

    public static String getControlledPathway(Player player) {
        return player.getData(ModAttachments.ENTITY_CONTROLLING_COMPONENT).getControlledEntityPathway();
    }

    public static int getControlledSequence(Player player) {
        return player.getData(ModAttachments.ENTITY_CONTROLLING_COMPONENT).getControlledEntitySequence();
    }

    public static void handleRejoin(ServerPlayer player) {
        EntityControllingComponent controllingComponent = player.getData(ModAttachments.ENTITY_CONTROLLING_COMPONENT);
        if(!controllingComponent.isControlling()) return;

        controllingComponent.restoreAttributesTo(player);
        controllingComponent.setControlling(false);
    }

    public record PathwayData(@NonNull String pathway, int sequence) {}

    @SubscribeEvent
    public static void onDimensionChange(EntityTravelToDimensionEvent event) {
        if (!(event.getEntity() instanceof Player player) || !isControlling(player)) return;
        // let seq1/seq2 players control puppets that change dimensions to vanilla/unconcealed dimensions
        if (getManipulationDistance(BeyonderData.getSequence(player)) < 0) {
            if (event.getDimension().equals(Level.NETHER) ||
                    event.getDimension().equals(Level.END) ||
                    event.getDimension().equals(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY)) return;
        }
        // let puppets enter and leave the historical void/overworld
        if (event.getDimension().equals(Level.OVERWORLD) ||
                event.getDimension().equals(ModDimensions.HISTORICAL_VOID_DIMENSION_KEY)) return;
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(event.getAmount() < player.getHealth()) return;
        if(isControlling(player)) {
            event.setCanceled(true);
            cancel(player, 0, true, true);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(isControlling(player)) {
            event.setCanceled(true);
            cancel(player, 0, true, true);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;

        if(!isControlling(player)) return;
        System.out.println("Pathway: " + BeyonderData.getPathway(player) + " - Sequence" + BeyonderData.getSequence(player) + " - Controlled Sequence: " + getControlledSequence(player));
        EntityControllingComponent component = player.getData(ModAttachments.ENTITY_CONTROLLING_COMPONENT);
        if(component.bodyDouble == null) return;
        if(component.bodyDouble.level() != player.level() || !component.bodyDouble.isAlive()) return;

        if(player.distanceTo(component.bodyDouble) > getManipulationDistance(BeyonderData.getSequence(player)) * 5) {
            cancel(player, 0, true, false);
        }
    }

    private static int getManipulationDistance(int sequence) {
        return switch (sequence) {
            default -> 7;
            case 4 -> 75;
            case 3 -> 200;
            case 2 -> 500;
            case 1 -> 2000;
            case 0 -> 5000;
        };
    }
}

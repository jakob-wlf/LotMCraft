package de.jakob.lotm.beyonders.abilities.fool.marionettes;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.EntityControllingComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.PhysicalEnhancementsAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.custom.ability_entities.ControlBodyDouble;
import de.jakob.lotm.events.BeyonderDataTickHandler;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncControllingPacket;
import de.jakob.lotm.network.packets.toServer.RequestControllingSyncPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityBarHelper;
import de.jakob.lotm.util.helper.AbilityWheelHelper;
import de.jakob.lotm.util.shapeShifting.ShapeShiftingUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
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

        PhysicalEnhancementsAbility.removeAllEnhancementsForEntity(player);
        BeyonderDataTickHandler.invalidateCache(player);

        if(spawnOriginalBody) {
            ControlBodyDouble controlBodyDouble = ControlBodyDouble.create(level, player);
            level.addFreshEntity(controlBodyDouble);
            component.setBodyDouble(controlBodyDouble);
        }

        component.setControlling(true);
        component.setCanUseOwnAbilities(keepOwnAbilities);
        component.setControlledEntity(target);
        component.setControlledEntityPathway(BeyonderData.getPathway(target));
        component.setControlledEntitySequence(BeyonderData.getSequence(target));

        component.setAbilityWheelAbilities(player.getData(ModAttachments.ABILITY_WHEEL_COMPONENT).getAbilities());
        component.setSelectedAbilityInWheel(player.getData(ModAttachments.ABILITY_WHEEL_COMPONENT).getSelectedAbility());
        component.setAbilityBarAbilities(player.getData(ModAttachments.ABILITY_BAR_COMPONENT).getAbilities());


        syncControllingData(player);

        ShapeShiftingUtil.shapeShift(player, target, false);
        player.teleportTo(level, target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());

        copyAttributesAndHealthFrom(target, player);

        target.discard();
        return true;
    }

    public static void copyAttributesAndHealthFrom(LivingEntity source, LivingEntity target) {
        for (Holder<Attribute> attributeHolder : BuiltInRegistries.ATTRIBUTE.asHolderIdMap()) {

            if (attributeHolder.is(Attributes.MOVEMENT_SPEED)) {
                continue;
            }

            AttributeInstance sourceInstance = source.getAttribute(attributeHolder);
            AttributeInstance targetInstance = target.getAttribute(attributeHolder);

            if (sourceInstance != null && targetInstance != null) {
                targetInstance.setBaseValue(sourceInstance.getBaseValue());
            }
        }

        if(source.getAttribute(Attributes.MAX_HEALTH) == null && target.getAttribute(Attributes.MAX_HEALTH) != null) {
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(source.getMaxHealth());
        }

        float maxHealth = target.getMaxHealth();
        float sourceHealth = source.getHealth();
        float newHealth = Math.min(sourceHealth, maxHealth);

        target.setHealth(newHealth);
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
                level.addFreshEntity(controlled);
                controlled.setHealth(Math.clamp(player.getHealth(), 1, controlled.getHealth()));
                controlled.setPos(player.position());
                if(killPreviousEntity) {
                    controlled.hurt(controlled.damageSources().generic(), Float.MAX_VALUE);
                }
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

            ServerLevel returnLevel = (ServerLevel) controlBodyDouble.level(); // Should always be the same level but just to be sure

            player.teleportTo(returnLevel, controlBodyDouble.getX(), controlBodyDouble.getY(), controlBodyDouble.getZ(), Set.of(), controlBodyDouble.getYRot(), controlBodyDouble.getXRot());
            if(damage > 0) {
                player.hurt(player.damageSources().generic(), damage);
            }

            controlBodyDouble.discard();
        }

        AbilityBarHelper.setAbilities(player, component.getAbilityBarAbilities());
        AbilityWheelHelper.setAbilities(player, component.getAbilityWheelAbilities());
        AbilityWheelHelper.setSelectedAbility(player, component.getSelectedAbilityInWheel());

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
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(event.getAmount() < player.getHealth()) return;
        cancel(player, 0, true, true);
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;

        if(!isControlling(player)) return;
        EntityControllingComponent component = player.getData(ModAttachments.ENTITY_CONTROLLING_COMPONENT);
        if(component.bodyDouble == null) return;
        if(component.bodyDouble.level() != player.level() || !component.bodyDouble.isAlive()) return;

        if(player.distanceToSqr(component.bodyDouble) > 10000) { // 100 blocks
            cancel(player, 0, true, false);
        }
    }
}

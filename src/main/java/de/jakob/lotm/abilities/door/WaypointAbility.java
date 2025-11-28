package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.WaypointComponent;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WaypointAbility extends SelectableAbilityItem {
    public WaypointAbility(Properties properties) {
        super(properties, 1);

        canBeCopied = false;
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 900;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.waypoint.teleport", "ability.lotmcraft.waypoint.set", "ability.lotmcraft.waypoint.delete"};
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        switch(abilityIndex) {
            case 0 -> teleportToWaypoint(serverLevel, entity);
            case 1 -> createWaypoint(serverLevel, entity);
            case 2 -> deleteWaypoint(serverLevel, entity);
        }
    }

    private void deleteWaypoint(ServerLevel serverLevel, LivingEntity entity) {
        WaypointComponent waypointComponent = entity.getData(ModAttachments.WAYPOINT_COMPONENT);
        WaypointComponent.Waypoint waypoint = waypointComponent.getSelectedWaypoint();

        if(waypoint == null) {
            return;
        }

        waypointComponent.deleteWaypoint(waypoint);
        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.waypoint.deleted").withColor(0x91f6ff));
    }

    private void createWaypoint(ServerLevel serverLevel, LivingEntity entity) {
        WaypointComponent waypointComponent = entity.getData(ModAttachments.WAYPOINT_COMPONENT);
        waypointComponent.createWaypoint(entity.getX(), entity.getY(), entity.getZ(), serverLevel);

        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.waypoint.created").withColor(0x91f6ff));
        EffectManager.playEffect(EffectManager.Effect.WAYPOINT, entity.getX(), entity.getY() + 1, entity.getZ(), serverLevel);
    }

    private void teleportToWaypoint(ServerLevel serverLevel, LivingEntity entity) {
        WaypointComponent waypointComponent = entity.getData(ModAttachments.WAYPOINT_COMPONENT);
        WaypointComponent.Waypoint waypoint = waypointComponent.getSelectedWaypoint();

        if(waypoint == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.waypoint.no_waypoints").withColor(0x91f6ff));
            return;
        }

        serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1, 1);
        entity.teleportTo(waypoint.serverLevel(), waypoint.x(), waypoint.y(), waypoint.z(), Set.of(), entity.getYRot(), entity.getXRot());
        serverLevel.playSound(null, waypoint.x(), waypoint.y(), waypoint.z(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1, 1);
        EffectManager.playEffect(EffectManager.Effect.WAYPOINT, waypoint.x(), waypoint.y() + 1, waypoint.z(), serverLevel);
        EffectManager.playEffect(EffectManager.Effect.WAYPOINT, entity.getX(), entity.getY() + 1, entity.getZ(), serverLevel);

    }

    long abilitySwitchCooldown = 0;

    @Override
    public void onHold(Level level, LivingEntity entity) {
        WaypointComponent waypointComponent = entity.getData(ModAttachments.WAYPOINT_COMPONENT);

        WaypointComponent.Waypoint waypoint = waypointComponent.getSelectedWaypoint();

        if(waypoint == null) {
            return;
        }

        AbilityUtil.sendActionBar(entity, Component.translatable("lotm.selected").append(": X: " + Math.round(waypoint.x()) + ", Y: " + Math.round(waypoint.y()) + ", Z: " + Math.round(waypoint.z())).withColor(0x91f6ff));

        if(entity.isShiftKeyDown() && abilitySwitchCooldown <= System.currentTimeMillis()) {
            waypointComponent.selectNextWaypoint();

            abilitySwitchCooldown = System.currentTimeMillis() + 500;
        }
    }
}

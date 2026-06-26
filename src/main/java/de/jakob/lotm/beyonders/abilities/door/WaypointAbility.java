package de.jakob.lotm.beyonders.abilities.door;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.WaypointComponent;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.OpenWaypointSelectionScreenPacket;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WaypointAbility extends SelectableAbility {
    public WaypointAbility(String id) {
        super(id, 1);
        canBeCopied = false;
        canBeUsedByNPC = false;
        canBeReplicated = false;
        canBeShared = false;
        cannotBeStolen = true;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 5000;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.waypoint.set",
                "ability.lotmcraft.waypoint.delete",
                "ability.lotmcraft.waypoint.select"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        switch (abilityIndex) {
            case 0 -> createWaypoint(serverLevel, entity);
            case 1 -> deleteWaypoint(entity);
            case 2 -> selectWaypoint(entity);
        }
    }

    public void selectWaypoint(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer serverPlayer)) return;

        List<WaypointComponent.ClientWaypoint> waypoints = entity.getData(ModAttachments.WAYPOINT_COMPONENT).getWaypoints().stream().map(WaypointComponent.ClientWaypoint::fromWaypoint).toList();

        PacketHandler.sendToPlayer(serverPlayer, new OpenWaypointSelectionScreenPacket(waypoints, "teleport"));
    }

    private void deleteWaypoint(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer serverPlayer)) return;

        List<WaypointComponent.ClientWaypoint> waypoints = entity.getData(ModAttachments.WAYPOINT_COMPONENT).getWaypoints().stream().map(WaypointComponent.ClientWaypoint::fromWaypoint).toList();

        PacketHandler.sendToPlayer(serverPlayer, new OpenWaypointSelectionScreenPacket(waypoints, "delete"));
    }

    private void createWaypoint(ServerLevel serverLevel, LivingEntity entity) {
        WaypointComponent waypointComponent = entity.getData(ModAttachments.WAYPOINT_COMPONENT);
        waypointComponent.createWaypoint(entity.getX(), entity.getY(), entity.getZ(), serverLevel);

        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.waypoint.created").withColor(0x91f6ff));
        EffectManager.playEffect(EffectManager.Effect.WAYPOINT, entity.getX(), entity.getY() + 1, entity.getZ(), serverLevel);
    }
}
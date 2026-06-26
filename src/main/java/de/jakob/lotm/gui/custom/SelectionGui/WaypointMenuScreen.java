package de.jakob.lotm.gui.custom.SelectionGui;

import de.jakob.lotm.attachments.WaypointComponent;
import de.jakob.lotm.network.packets.toServer.WanderingSelectedPacket;
import de.jakob.lotm.network.packets.toServer.WaypointSelectedPacket;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Arrays;
import java.util.List;

public class WaypointMenuScreen extends ButtonListGui<String> {

    private final String use;
    private final List<WaypointComponent.ClientWaypoint> waypoints;

    public WaypointMenuScreen(List<WaypointComponent.ClientWaypoint> waypoints, String use) {
        super(Component.literal("Select Dimension"), waypoints.stream().map(WaypointMenuScreen::convertToString).toList());
        this.use = use;
        this.waypoints = waypoints;
    }

    @Override
    protected Component getItemName(String convertedWayoint) {
        return Component.literal(convertedWayoint);
    }

    private static String convertToString(WaypointComponent.ClientWaypoint waypoint) {
        String dimensionId = waypoint.id().toString();
        String dimensionName = Arrays.stream(dimensionId.split(":"))
                .reduce((first, second) -> second)
                .map(name -> name.replace('_', ' '))
                .map(name -> Character.toUpperCase(name.charAt(0)) + name.substring(1))
                .orElse(dimensionId);

        return Math.round(waypoint.x()) + ", " + Math.round(waypoint.y()) + ", " + Math.round(waypoint.z()) + ", " + dimensionName;
    }

    @Override
    protected void onItemSelected(String convertedString) {
        WaypointComponent.ClientWaypoint selectedWaypoint = null;
        for (WaypointComponent.ClientWaypoint waypoint : waypoints) {
            if (convertToString(waypoint).equals(convertedString)) {
                selectedWaypoint = waypoint;
                break;
            }
        }

        if (selectedWaypoint != null) {
            PacketDistributor.sendToServer(new WaypointSelectedPacket(selectedWaypoint, use));
        }

        minecraft.setScreen(null);
    }
}
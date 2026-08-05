package de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives;

import de.jakob.lotm.attachments.SefirotData;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityItem;
import de.jakob.lotm.beyonders.abilities.red_priest.CullAbility;
import de.jakob.lotm.beyonders.sefirah.SefirotAuthorityManager;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncAbsolutePerceptionColorsPacket;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AbsolutePerceptionAbility extends PassiveAbilityItem {
    private static final int syncIntervalTicks = 10;
    private final Map<UUID, Set<Entity>> glowingOwners = new java.util.HashMap<>();

    public AbsolutePerceptionAbility(Item.Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("wheel_of_fortune", 4);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)
                || !(entity instanceof ServerPlayer observer)
                || entity.tickCount % syncIntervalTicks != 0) {
            return;
        }

        int renderDistance = observer.server.getPlayerList().getViewDistance() * 16;
        double maximumDistanceSquared = (double) renderDistance * renderDistance;
        SefirotData sefirotData = SefirotData.get(observer.server);
        Set<Entity> visibleOwners = new HashSet<>();
        Map<Integer, Integer> outlineColors = new HashMap<>();
        for (ServerPlayer target : serverLevel.players()) {
            if (target.distanceToSqr(observer) > maximumDistanceSquared) continue;
            String sefirot = sefirotData.getClaimedSefirot(target.getUUID());
            if (!sefirot.isEmpty()) {
                visibleOwners.add(target);
                outlineColors.put(target.getId(), getBlendedSefirotColor(sefirot));
            }
        }
        updatePrivateGlow(observer, visibleOwners);
        PacketHandler.sendToPlayer(observer, new SyncAbsolutePerceptionColorsPacket(outlineColors));
    }

    @Override
    public void onPassiveAbilityRemoved(LivingEntity entity, ServerLevel serverLevel) {
        if (entity instanceof ServerPlayer player) {
            updatePrivateGlow(player, Set.of());
            PacketHandler.sendToPlayer(player, new SyncAbsolutePerceptionColorsPacket(Map.of()));
        }
    }

    private static int getBlendedSefirotColor(String sefirot) {
        var pathways = SefirotAuthorityManager.neighboringPaths.getOrDefault(sefirot, java.util.List.of());
        if (pathways.isEmpty()) return 0xFFFFFF;

        int red = 0;
        int green = 0;
        int blue = 0;
        int count = 0;
        for (String pathway : pathways) {
            if (!BeyonderData.pathwayInfos.containsKey(pathway)) continue;
            int color = BeyonderData.pathwayInfos.get(pathway).color();
            red += color >> 16 & 0xFF;
            green += color >> 8 & 0xFF;
            blue += color & 0xFF;
            count++;
        }
        return count == 0 ? 0xFFFFFF
                : (red / count << 16) | (green / count << 8) | blue / count;
    }

    private void updatePrivateGlow(ServerPlayer observer, Set<Entity> visibleOwners) {
        Set<Entity> previousOwners = glowingOwners.getOrDefault(observer.getUUID(), Set.of());
        previousOwners.stream()
                .filter(owner -> !visibleOwners.contains(owner))
                .forEach(owner -> CullAbility.setGlowingForPlayer(owner, observer, false));
        visibleOwners.stream()
                .filter(owner -> !previousOwners.contains(owner))
                .forEach(owner -> CullAbility.setGlowingForPlayer(owner, observer, true));

        if (visibleOwners.isEmpty()) {
            glowingOwners.remove(observer.getUUID());
        } else {
            glowingOwners.put(observer.getUUID(), visibleOwners);
        }
    }
}
package de.jakob.lotm.beyonders.abilities.wheel_of_fortune;

import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.red_priest.CullAbility;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives.PassiveCalamityAttraction;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives.PassiveLuckAccumulationAbility;
import de.jakob.lotm.entity.custom.ability_entities.MeteorEntity;
import de.jakob.lotm.entity.custom.ability_entities.TornadoEntity;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncLuckResourcePacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.LuckManager;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SpiritualForesightAbility extends ToggleAbility {
    private static final int activationLuckCost = 25;
    private static final int upkeepLuckCost = 1;
    private static final int upkeepIntervalTicks = 20 * 12;
    private static final int playerDetectionLuckCost = 20;
    private static final float playerDetectionSpiritualityCost = 5;
    private static final int enhancementThreshold = 25;
    private static final int minimumEnhancedRange = 64;
    private static final int detectionIntervalTicks = 5;
    private final Map<UUID, Set<Entity>> glowingEntities = new HashMap<>();
    private final Map<UUID, Set<UUID>> sensedPlayers = new HashMap<>();
    private final Map<UUID, Set<UUID>> sensedThreats = new HashMap<>();
    private final Map<UUID, Integer> upkeepTicks = new HashMap<>();
    private final Set<UUID> warnedOfScheduledCalamity = new HashSet<>();

    public SpiritualForesightAbility(String id) {
        super(id);
        tickRate = 5;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 7));
    }

    @Override
    protected float getSpiritualityCost() {
        return 0.25f;
    }

    @Override
    public int luckCost() {
        return activationLuckCost;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!level.isClientSide && !isActiveForEntity(entity)
                && !PassiveLuckAccumulationAbility.consumeExactStoredLuck(entity, activationLuckCost)) {
            entity.sendSystemMessage(Component.literal(
                "\u00A7cSpiritual Foresight requires " + activationLuckCost + " luck."));
            return;
        }
        super.onAbilityUse(level, entity);
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            entity.sendSystemMessage(Component.literal("\u00A7bSpiritual Foresight active"));
        }
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer player)) {
            return;
        }

        int elapsedUpkeepTicks = upkeepTicks.merge(entity.getUUID(), tickRate, Integer::sum);
        if (elapsedUpkeepTicks >= upkeepIntervalTicks) {
            upkeepTicks.put(entity.getUUID(), elapsedUpkeepTicks - upkeepIntervalTicks);
            if (!PassiveLuckAccumulationAbility.consumeExactStoredLuck(entity, upkeepLuckCost)) {
                entity.sendSystemMessage(Component.literal("\u00A7cSpiritual Foresight ended: insufficient luck."));
                cancel(serverLevel, entity);
                return;
            }
        }

        boolean enhanced = LuckManager.getLuck(entity) >= enhancementThreshold;

        int range = getDetectionRange(entity, enhanced);
        if (entity.tickCount % detectionIntervalTicks != 0) return;

        List<Player> nearbyPlayers = serverLevel.getEntitiesOfClass(
                Player.class, entity.getBoundingBox().inflate(range), target -> target != entity && !target.isSpectator());
        List<Entity> immediateThreats = serverLevel.getEntitiesOfClass(
            Entity.class, entity.getBoundingBox().inflate(range), this::isCalamityThreat);
        player.displayClientMessage(Component.literal("\u00A7b[Foresight] \u00A7fPlayers: \u00A7e"
            + nearbyPlayers.size() + " \u00A7f| Threats: \u00A7c" + immediateThreats.size()
            + " \u00A7f| Range: \u00A7b" + range), true);

        boolean detailedForesight = BeyonderData.getSequence(entity) <= 4;
        notifyNewPlayers(player, nearbyPlayers, detailedForesight);
        notifyNewThreats(player, immediateThreats, detailedForesight);
        notifyScheduledCalamity(player, detailedForesight);

        List<Entity> glowing = new java.util.ArrayList<>();
        if (detailedForesight) {
            glowing.addAll(nearbyPlayers);
            glowing.addAll(immediateThreats);
        }
        updatePrivateGlow(player, glowing);
    }

    private static int getDetectionRange(LivingEntity entity, boolean enhanced) {
        int sequenceRange = switch (BeyonderData.getSequence(entity)) {
            case 0 -> 128;
            case 1 -> 104;
            case 2 -> 80;
            case 3 -> 64;
            case 4 -> 52;
            case 5 -> 40;
            case 6 -> 32;
            default -> 24;
        };
        return enhanced ? Math.max(minimumEnhancedRange, Math.round(sequenceRange * 1.5f)) : sequenceRange;
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level.isClientSide || !(entity instanceof ServerPlayer player)) {
            return;
        }
        updatePrivateGlow(player, List.of());
        sensedPlayers.remove(player.getUUID());
        sensedThreats.remove(player.getUUID());
        upkeepTicks.remove(player.getUUID());
        warnedOfScheduledCalamity.remove(player.getUUID());
        clearArtifactScaling(entity);
    }

    private void notifyNewPlayers(ServerPlayer player, List<Player> currentPlayers, boolean detailed) {
        Set<UUID> previous = sensedPlayers.getOrDefault(player.getUUID(), Set.of());
        List<Player> newlySensed = currentPlayers.stream()
                .filter(target -> !previous.contains(target.getUUID()))
                .toList();

        if (!newlySensed.isEmpty()) {
            int luckCost = playerDetectionLuckCost * newlySensed.size();
            float spiritualityCost = playerDetectionSpiritualityCost * newlySensed.size();
            if (BeyonderData.getSpirituality(player) < spiritualityCost
                || !PassiveLuckAccumulationAbility.consumeExactStoredLuck(player, luckCost)) {
                return;
            }
            BeyonderData.reduceSpirituality(player, spiritualityCost);
            if (detailed) {
                newlySensed.forEach(target -> player.sendSystemMessage(Component.literal(
                        "\u00A7b[Foresight] \u00A7f" + target.getName().getString() + " sensed "
                                + describeLocation(player, target.position()))));
            } else {
                player.sendSystemMessage(Component.literal(
                        "\u00A7b[Foresight] \u00A7fYou sense " + newlySensed.size() + " nearby player presence(s)."));
            }
        }
        sensedPlayers.put(player.getUUID(), currentPlayers.stream()
                .map(Entity::getUUID).collect(java.util.stream.Collectors.toSet()));
    }

    private void notifyNewThreats(ServerPlayer player, List<Entity> currentThreats, boolean detailed) {
        Set<UUID> previous = sensedThreats.getOrDefault(player.getUUID(), Set.of());
        List<Entity> newlySensed = currentThreats.stream()
                .filter(target -> !previous.contains(target.getUUID()))
                .toList();

        if (!newlySensed.isEmpty()) {
            if (detailed) {
                newlySensed.forEach(target -> player.sendSystemMessage(Component.literal(
                        "\u00A7c[Foresight] \u00A7f" + target.getType().getDescription().getString() + " approaching "
                                + describeLocation(player, target.position()))));
            } else {
                player.sendSystemMessage(Component.literal("\u00A7c[Foresight] A calamity is approaching."));
            }
        }
        sensedThreats.put(player.getUUID(), currentThreats.stream()
                .map(Entity::getUUID).collect(java.util.stream.Collectors.toSet()));
    }

    private void notifyScheduledCalamity(ServerPlayer player, boolean detailed) {
        long calamityTicks = PassiveCalamityAttraction.getTicksUntilCalamity(player);
        boolean approaching = calamityTicks >= 0 && calamityTicks <= 20 * 30;
        if (!approaching) {
            warnedOfScheduledCalamity.remove(player.getUUID());
            return;
        }
        if (!warnedOfScheduledCalamity.add(player.getUUID())) return;

        Vec3 position = PassiveCalamityAttraction.getCalamityPosition(player);
        String location = detailed && position != null ? " " + describeLocation(player, position) : "";
        player.sendSystemMessage(Component.literal(
                "\u00A7c[Foresight] A calamity will manifest in " + Math.max(1, Math.round(calamityTicks / 20f))
                        + " seconds." + location));
    }

    private boolean isCalamityThreat(Entity entity) {
        return entity instanceof PrimedTnt
                || entity instanceof Projectile
                || entity instanceof MeteorEntity
                || entity instanceof TornadoEntity;
    }

    private String describeLocation(LivingEntity observer, Vec3 target) {
        double deltaX = target.x - observer.getX();
        double deltaZ = target.z - observer.getZ();
        double angle = Math.toDegrees(Math.atan2(deltaX, -deltaZ));
        if (angle < 0) angle += 360;
        String[] directions = {"north", "north-east", "east", "south-east", "south", "south-west", "west", "north-west"};
        String direction = directions[(int) Math.round(angle / 45) % directions.length];
        return Math.round(observer.position().distanceTo(target)) + " blocks " + direction;
    }

    private void updatePrivateGlow(ServerPlayer player, List<? extends Entity> currentEntities) {
        Set<Entity> previousEntities = glowingEntities.getOrDefault(player.getUUID(), Set.of());
        Set<Entity> currentSet = new HashSet<>(currentEntities);

        previousEntities.stream()
                .filter(entity -> !currentSet.contains(entity))
                .forEach(entity -> CullAbility.setGlowingForPlayer(entity, player, false));
        currentSet.stream()
                .filter(entity -> !previousEntities.contains(entity))
                .forEach(entity -> CullAbility.setGlowingForPlayer(entity, player, true));

        if (currentSet.isEmpty()) {
            glowingEntities.remove(player.getUUID());
        } else {
            glowingEntities.put(player.getUUID(), currentSet);
        }
    }
}
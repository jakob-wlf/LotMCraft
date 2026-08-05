package de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives;

import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityItem;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.calamities.Calamity;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.calamities.Earthquake;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.calamities.Meteor;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.calamities.Tornado;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PassiveCalamityAttraction extends PassiveAbilityItem {
    private static final double minimumSpawnDistance = 4;
    private static final double maximumSpawnDistance = 20;

    public PassiveCalamityAttraction(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 6));
    }

    private final Calamity[] calamities = new Calamity[]{new Tornado(), new Earthquake(), new Meteor()};

    private static final HashMap<UUID, Long> nextCalamity = new HashMap<>();
    private static final HashMap<UUID, Vec3> nextCalamityOffset = new HashMap<>();

    public static long getTicksUntilCalamity(LivingEntity entity) {
        return nextCalamity.getOrDefault(entity.getUUID(), -1L);
    }

    public static Vec3 getCalamityPosition(LivingEntity entity) {
        Vec3 offset = nextCalamityOffset.get(entity.getUUID());
        return offset == null ? null : entity.position().add(offset);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if(BeyonderData.getSequence(entity) <= 5) {
            return;
        }

        if(!nextCalamity.containsKey(entity.getUUID())) {
            nextCalamity.put(entity.getUUID(), (long) random.nextInt(20 * 20, 20 * 90));
            nextCalamityOffset.put(entity.getUUID(), createRandomCalamityOffset(serverLevel));
            return;
        }

        nextCalamity.replace(entity.getUUID(), nextCalamity.get(entity.getUUID()) - 5);

        if(nextCalamity.get(entity.getUUID()) <= 20 * 12) {
            if(entity instanceof ServerPlayer player) {
                Component actionBarText = Component.translatable("ability.lotmcraft.passive_calamity_attraction.approaching_calamity").withColor(0xFFc0f6fc);
                sendActionBar(player, actionBarText);
            }
        }

        if(nextCalamity.get(entity.getUUID()) <= 0) {
            Calamity calamity = calamities[random.nextInt(calamities.length)];
            calamity.spawnCalamity(
                    serverLevel,
                    getCalamityPosition(entity),
                    (float) BeyonderData.getMultiplier(entity),
                    BeyonderData.isGriefingEnabled(entity),
                    BeyonderData.getSequence(entity));

            nextCalamity.put(entity.getUUID(), (long) random.nextInt(20 * 20, 20 * 90));
            nextCalamityOffset.put(entity.getUUID(), createRandomCalamityOffset(serverLevel));
        }
    }

    private static Vec3 createRandomCalamityOffset(ServerLevel level) {
        double angle = level.random.nextDouble() * Math.PI * 2;
        double distance = minimumSpawnDistance
                + level.random.nextDouble() * (maximumSpawnDistance - minimumSpawnDistance);
        return new Vec3(Math.cos(angle) * distance, 0, Math.sin(angle) * distance);
    }

    private static void sendActionBar(ServerPlayer player, Component message) {
        ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(message);
        player.connection.send(packet);
    }
}

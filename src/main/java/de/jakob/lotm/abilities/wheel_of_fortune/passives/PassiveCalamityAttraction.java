package de.jakob.lotm.abilities.wheel_of_fortune.passives;

import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.abilities.wheel_of_fortune.calamities.Calamity;
import de.jakob.lotm.abilities.wheel_of_fortune.calamities.Earthquake;
import de.jakob.lotm.abilities.wheel_of_fortune.calamities.Tornado;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PassiveCalamityAttraction extends PassiveAbilityItem {
    public PassiveCalamityAttraction(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 6));
    }

    private final Calamity[] calamities = new Calamity[]{new Tornado(), new Earthquake()};

    private final HashMap<UUID, Long> nextCalamity = new HashMap<>();

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if(BeyonderData.getSequence(entity) <= 4) {
            return;
        }

        if(!nextCalamity.containsKey(entity.getUUID())) {
            nextCalamity.put(entity.getUUID(), (long) random.nextInt(20 * 60, 20 * 60 * 4));
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
            calamity.spawnCalamity(serverLevel, entity.position().offsetRandom(serverLevel.random, 6f), (float) BeyonderData.getMultiplier(entity));

            nextCalamity.put(entity.getUUID(), (long) random.nextInt(20 * 60, 20 * 60 * 4));
        }
    }

    private static void sendActionBar(ServerPlayer player, Component message) {
        ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(message);
        player.connection.send(packet);
    }
}

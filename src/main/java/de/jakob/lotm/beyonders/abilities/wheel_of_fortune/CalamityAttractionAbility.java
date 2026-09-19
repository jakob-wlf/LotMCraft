package de.jakob.lotm.beyonders.abilities.wheel_of_fortune;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.calamities.Calamity;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.calamities.Earthquake;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.calamities.Meteor;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.calamities.Tornado;
import de.jakob.lotm.beyonders.sefirah.SefirahHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class CalamityAttractionAbility extends Ability {
    private static final int unlimitedLookRange = 4096;

    public CalamityAttractionAbility(String id) {
        super(id, 10);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 6));
    }

    @Override
    public float getSpiritualityCost() {
        return 190;
    }

    private final Calamity[] calamities = new Calamity[]{new Tornado(), new Earthquake(), new Meteor()};

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if(entity instanceof ServerPlayer player) {
            Component actionBarText = Component.translatable("ability.lotmcraft.passive_calamity_attraction.approaching_calamity").withColor(0xFFc0f6fc);
            ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(actionBarText);
            player.connection.send(packet);
        }

        int sequence = BeyonderData.getSequence(entity);
        int targetRange = getTargetRange(entity, sequence);
        Vec3 targetPos = getCalamityTarget(entity, targetRange);

        double multiplier = multiplier(entity);
        ServerScheduler.scheduleDelayed(random.nextInt(31, 60), () -> {
            Calamity calamity = calamities[random.nextInt(calamities.length)];
            calamity.spawnCalamity(
                    serverLevel, targetPos, (float) multiplier, BeyonderData.isGriefingEnabled(entity), sequence);
        }, serverLevel);
    }

    private static int getTargetRange(LivingEntity entity, int sequence) {
        if (sequence < 0 || sequence == 0 && entity instanceof ServerPlayer player
                && "key_of_light".equals(SefirahHandler.getClaimedSefirot(player))) {
            return Integer.MAX_VALUE;
        }
        return switch (sequence) {
            case 0 -> 1000;
            case 1 -> 800;
            case 2 -> 600;
            case 3 -> 400;
            case 4 -> 250;
            case 5 -> 100;
            default -> 50;
        };
    }

    private static Vec3 getCalamityTarget(LivingEntity entity, int targetRange) {
        boolean unlimited = targetRange == Integer.MAX_VALUE;
        int raycastRange = unlimited ? unlimitedLookRange : targetRange;
        Vec3 target = AbilityUtil.getTargetLocation(entity, raycastRange, 2, true);
        if (unlimited || target.distanceToSqr(entity.position()) <= (double) targetRange * targetRange) {
            return target;
        }

        Vec3 direction = target.subtract(entity.position()).normalize();
        return entity.position().add(direction.scale(targetRange));
    }
}

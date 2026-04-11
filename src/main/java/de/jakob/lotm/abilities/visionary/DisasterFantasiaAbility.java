package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.wheel_of_fortune.calamities.Earthquake;
import de.jakob.lotm.abilities.wheel_of_fortune.calamities.Meteor;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DisasterFantasiaAbility extends SelectableAbility {

    private static final Earthquake EARTHQUAKE = new Earthquake();
    private static final Meteor METEOR = new Meteor();
    private static final int METEOR_COUNT = 7;
    private static final double METEOR_RADIUS = 50.0;

    public DisasterFantasiaAbility(String id) {
        super(id, 30f, "destruction");
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 1));
    }

    @Override
    public float getSpiritualityCost() {
        return 2500;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.disaster_fantasia.earthquake",
                "ability.lotmcraft.disaster_fantasia.meteor",
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 150, 3);
        float multiplier = (float) multiplier(entity);
        boolean griefing = BeyonderData.isGriefingEnabled(entity);

        switch (abilityIndex) {
            case 0 -> EARTHQUAKE.spawnCalamity(serverLevel, targetPos, multiplier, griefing);
            case 1 -> spawnMeteorShower(serverLevel, targetPos, multiplier, griefing);
        }
    }

    public static void spawnCurrentDisaster(int disasterIndex, ServerLevel level,
                                            Vec3 position, float multiplier, boolean griefing) {
        switch (disasterIndex) {
            case 0 -> EARTHQUAKE.spawnCalamity(level, position, multiplier, griefing);
            case 1 -> spawnMeteorShower(level, position, multiplier, griefing);
        }
    }

    private static void spawnMeteorShower(ServerLevel level, Vec3 center,
                                          float multiplier, boolean griefing) {
        Random rand = new Random();
        for (int i = 0; i < METEOR_COUNT; i++) {
            double angle = rand.nextDouble() * 2 * Math.PI;
            double distance = rand.nextDouble() * METEOR_RADIUS;
            double offsetX = Math.cos(angle) * distance;
            double offsetZ = Math.sin(angle) * distance;
            Vec3 meteorPos = new Vec3(center.x + offsetX, center.y, center.z + offsetZ);
            METEOR.spawnCalamity(level, meteorPos, multiplier, griefing);
        }
    }
}
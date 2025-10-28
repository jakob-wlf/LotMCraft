package de.jakob.lotm.abilities.wheel_of_fortune;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.wheel_of_fortune.calamities.Calamity;
import de.jakob.lotm.abilities.wheel_of_fortune.calamities.Earthquake;
import de.jakob.lotm.abilities.wheel_of_fortune.calamities.Meteor;
import de.jakob.lotm.abilities.wheel_of_fortune.calamities.Tornado;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class CalamityAttractionAbility extends AbilityItem {
    public CalamityAttractionAbility(Properties properties) {
        super(properties, 10);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 190;
    }

    private final Calamity[] calamities = new Calamity[]{new Tornado(), new Earthquake(), new Meteor()};

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 14, 2, true);
        Calamity calamity = calamities[random.nextInt(calamities.length)];
        calamity.spawnCalamity(serverLevel, targetPos, (float) BeyonderData.getMultiplier(entity), BeyonderData.isGriefingEnabled(entity));
    }
}

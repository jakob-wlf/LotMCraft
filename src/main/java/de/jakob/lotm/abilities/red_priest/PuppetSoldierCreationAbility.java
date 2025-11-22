package de.jakob.lotm.abilities.red_priest;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.subordinates.SubordinateUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class PuppetSoldierCreationAbility extends AbilityItem {
    public PuppetSoldierCreationAbility(Properties properties) {
        super(properties, 20 * 270); // 4 min, 30 seconds
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 2000;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        for(int i = 0; i < 6; i++) {
            Vec3 spawnPos = entity.position().add(random.nextDouble(-3, 3), 0, random.nextDouble(-3, 3));

            BeyonderNPCEntity puppetSoldier = new BeyonderNPCEntity(ModEntities.BEYONDER_NPC.get(), serverLevel, false, "knight", "red_priest", 4);
            puppetSoldier.setPos(spawnPos);
            puppetSoldier.setPuppetWarrior(true);
            serverLevel.addFreshEntity(puppetSoldier);

            SubordinateUtils.turnEntityIntoSubordinate(puppetSoldier, entity);

            BeyonderData.addModifier(puppetSoldier, "puppet_soldier", 1.8f);
        }
    }
}

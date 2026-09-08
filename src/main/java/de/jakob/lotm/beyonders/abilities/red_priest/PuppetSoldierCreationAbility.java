package de.jakob.lotm.beyonders.abilities.red_priest;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.subordinates.SubordinateUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PuppetSoldierCreationAbility extends Ability {
    public PuppetSoldierCreationAbility(String id) {
        super(id, 20 * 60 * 2);
        canBeShared = false;

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(40, 90));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(24000f, 10000f));
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
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        for(int i = 0; i < 6; i++) {
            Vec3 spawnPos = entity.position().add(random.nextDouble(-3, 3), 0, random.nextDouble(-3, 3));

            BeyonderNPCEntity puppetSoldier = new BeyonderNPCEntity(ModEntities.BEYONDER_NPC.get(), serverLevel, false, "knight", "red_priest", 4, false, false);
            puppetSoldier.setPos(spawnPos);
            puppetSoldier.setPuppetWarrior(true);

            SubordinateUtils.turnEntityIntoSubordinate(puppetSoldier, entity);

            BeyonderData.addModifier(puppetSoldier, "puppet_soldier", 1.25f);

            serverLevel.addFreshEntity(puppetSoldier);
        }
    }
}

package de.jakob.lotm.beyonders.abilities.demoness;

import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.entity.custom.ability_entities.demoness_pathway.ChaosVortexEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.DamageLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class ChaosAuthorityAbility extends SelectableAbility {
    public ChaosAuthorityAbility(String id) {
        super(id, 30);
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.chaos_authority.chaos_vortex"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        switch (selectedAbility) {
            case 0 -> spawnChaosVortex(serverLevel, entity);
        }
    }

    private void spawnChaosVortex(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 direction = (new Vec3(entity.getLookAngle().x, 0, entity.getLookAngle().z)).normalize();
        Vec3 spawnPos = entity.getEyePosition().add(direction.scale(6));
        ChaosVortexEntity vortex = new ChaosVortexEntity(serverLevel, spawnPos, 5, 20 * 20, direction, entity, DamageLookup.lookupDps(0, .9, 10, 60) * multiplier(entity), BeyonderData.isGriefingEnabled(entity));
        vortex.setPos(spawnPos);
        serverLevel.addFreshEntity(vortex);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("demoness", 0);
    }

    @Override
    protected float getSpiritualityCost() {
        return 8000;
    }
}

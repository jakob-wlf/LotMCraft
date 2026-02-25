package de.jakob.lotm.abilities.tyrant;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.entity.custom.GiantLightningEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class HeavenlyPunishmentAbility extends Ability {
    public HeavenlyPunishmentAbility(String id) {
        super(id, 2);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 1));
    }

    @Override
    public float getSpiritualityCost() {
        return 1950;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 70, 2, true);
        for(int i = 0; i < 35; i++) {
            BlockState state = level.getBlockState(BlockPos.containing(targetLoc.subtract(0, 1, 0)));
            if(state.getCollisionShape(level, BlockPos.containing(targetLoc)).isEmpty())
                targetLoc = targetLoc.subtract(0, 1, 0);
        }

        GiantLightningEntity lightning = new GiantLightningEntity(level, entity, targetLoc, 50, 6, DamageLookup.lookupDamage(1, .9) * multiplier(entity), BeyonderData.isGriefingEnabled(entity), 13, 200, 0x6522a8);
        level.addFreshEntity(lightning);
    }
}

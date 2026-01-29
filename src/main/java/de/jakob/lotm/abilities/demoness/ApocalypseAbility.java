package de.jakob.lotm.abilities.demoness;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class ApocalypseAbility extends Ability {
    public ApocalypseAbility(String id) {
        super(id, 10);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("demoness", 1));
    }

    @Override
    public float getSpiritualityCost() {
        return 2500;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 loc = entity.position();

        // Play effect
        EffectManager.playEffect(EffectManager.Effect.APOCALYPSE, loc.x, loc.y, loc.z, serverLevel);

        double yLevel = loc.y - 1;

        // Remove blocks and damage entities
        AtomicDouble radius = new AtomicDouble(2);

        ServerScheduler.scheduleForDuration(0, 2, 110, () -> {
            if(BeyonderData.isGriefingEnabled(entity)) {
                AbilityUtil.getBlocksInSphereRadius(serverLevel, loc, radius.get(), true, true, false).forEach(blockPos -> {
                    if(blockPos.getY() <= yLevel && random.nextBoolean()) {
                        serverLevel.setBlockAndUpdate(blockPos, Blocks.OBSIDIAN.defaultBlockState());
                    }
                    else if(!serverLevel.getBlockState(blockPos).is(Blocks.OBSIDIAN)) {
                        serverLevel.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
                    }
                });
            }

            AbilityUtil.damageNearbyEntities(serverLevel, entity, radius.get() - 2.1, radius.get() + 2.1, DamageLookup.lookupDps(1, 1, 2, 30) * multiplier(entity), loc, true, false, true, 0);

            radius.addAndGet(0.8);
        });
    }
}

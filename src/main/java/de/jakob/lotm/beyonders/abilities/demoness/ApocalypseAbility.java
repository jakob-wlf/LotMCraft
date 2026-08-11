package de.jakob.lotm.beyonders.abilities.demoness;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.rendering.effectRendering.EffectIds;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ApocalypseAbility extends Ability {
    public ApocalypseAbility(String id) {
        super(id, 60, "destruction");
        autoClear = false;
        interactionRadius = 50;
        interactionCacheTicks = 110;
        canBeShared = false;

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(10, 20));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(20000f, 12000f));

        baseDamage = 17f; // 3 attacks with this damage
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

        EffectManager.playEffect(EffectIds.APOCALYPSE, loc.x, loc.y, loc.z, serverLevel, entity);

        double yLevel = loc.y - 1;

        AtomicDouble radius = new AtomicDouble(2);

        ServerScheduler.scheduleForDuration(0, 2, 20 * 3, () -> {
            if(BeyonderData.isGriefingEnabled(entity)) {
                AbilityUtil.getBlocksInSphereRadius(serverLevel, loc, radius.get(), true, true, false).forEach(blockPos -> {
                    if(level.getBlockState(blockPos).getDestroySpeed(level, blockPos) < 0) {
                        return;
                    }
                    if(blockPos.getY() <= yLevel && random.nextBoolean()) {
                        serverLevel.setBlockAndUpdate(blockPos, Blocks.OBSIDIAN.defaultBlockState());
                    }
                    else if(!serverLevel.getBlockState(blockPos).is(Blocks.OBSIDIAN)) {
                        serverLevel.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
                    }
                });
            }

            AbilityUtil.damageNearbyEntities(serverLevel, entity, radius.get(), ModDamageTypes.CHAOS, baseDamage, loc, true, false, false, 20);
            radius.addAndGet(0.8);
        }, () -> clearArtifactScaling(entity), serverLevel, () -> AbilityUtil.getTimeInArea(entity, new de.jakob.lotm.util.data.Location(entity.position(), serverLevel)));
    }
}

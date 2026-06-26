package de.jakob.lotm.beyonders.abilities.demoness;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.damage.ModDamageTypes;
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
        super(id, 60, "destruction");
        autoClear = false;
        interactionRadius = 50;
        interactionCacheTicks = 110;
        canBeShared = false;
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
        EffectManager.playEffect(EffectManager.Effect.APOCALYPSE, loc.x, loc.y, loc.z, serverLevel, entity);

        double yLevel = loc.y - 1;

        // Remove blocks and damage entities
        AtomicDouble radius = new AtomicDouble(2);

        ServerScheduler.scheduleForDuration(0, 2, (int) (55*multiplier(entity)), () -> {
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

            AbilityUtil.damageNearbyEntities(serverLevel, entity, radius.get(), DamageLookup.lookupDamage(1, .8) *multiplier(entity), loc, true, false, false, 20, ModDamageTypes.source(level, ModDamageTypes.DEMONESS_GENERIC, entity));
            AbilityUtil.getNearbyEntities(entity, serverLevel, entity.getEyePosition(), radius.get()).forEach(e -> e.getData(ModAttachments.SANITY_COMPONENT).increaseSanityAndSync((float) (-0.08f * multiplier(entity)), e));
            radius.addAndGet(0.8);
        }, () -> clearArtifactScaling(entity), serverLevel, () -> AbilityUtil.getTimeInArea(entity, new de.jakob.lotm.util.data.Location(entity.position(), serverLevel)));
    }
}

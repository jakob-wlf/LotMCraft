package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.block.ModBlocks;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.rendering.effectRendering.MovableEffectFactory;
import de.jakob.lotm.rendering.effectRendering.MovableEffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class SpaceTimeStormAbility extends SelectableAbility {
    public SpaceTimeStormAbility(String id) {
        super(id, 50, "explosion", "destruction");
        canBeCopied = false;
        interactionRadius = 60;
        interactionCacheTicks = 20 * 12;
        canBeShared = false;
        postsUsedAbilityEventManually = true;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 0));
    }

    @Override
    public float getSpiritualityCost() {
        return 40000;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.space_time_storm.area", "ability.lotmcraft.space_time_storm.targeted"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if(level.isClientSide) return;
        if(selectedAbility == 0) castAOEStorm((ServerLevel) level, entity);
        else                     castTargetedStorm((ServerLevel) level, entity);
    }

    private void castTargetedStorm(ServerLevel level, LivingEntity entity) {
        boolean griefing = BeyonderData.isGriefingEnabled(entity);

        Location loc = new Location(entity.position(), level);
        UUID effectId = MovableEffectManager.playEffect(MovableEffectManager.MovableEffect.SPACE_TEAR, loc, 20 * 10, false, level);

        AtomicInteger ticks = new AtomicInteger();

        ServerScheduler.scheduleForDuration(0, 1, 20 * 10, () -> {
            Vec3 target = AbilityUtil.getTargetLocation(entity, 60*(int) multiplier(entity), 3);
            if(target == null) return;

            ticks.addAndGet(1);

            loc.setPosition(target);
            MovableEffectManager.updateEffectPosition(effectId, loc, level);

            level.playSound(null, BlockPos.containing(target), SoundEvents.WITHER_SHOOT, SoundSource.AMBIENT, 1.5f, 0.75f + random.nextFloat() * 0.5f);

            if(ticks.get() % 10 == 0) {
                AbilityUtil.damageNearbyEntities(level, entity, 15* multiplier(entity), DamageLookup.lookupDamage(1, .4) *(int) Math.max(multiplier(entity)/6,1), target, true, false);
            }

            if(griefing) {
                List<BlockPos> blocks = AbilityUtil.getBlocksInSphereRadius(level, target, 11, true, true, false);
                blocks.stream().filter(b -> random.nextInt(5) == 0 && !level.getBlockState(b).is(ModBlocks.VOID.get())).forEach(b -> {
                    if(random.nextBoolean()) level.setBlockAndUpdate(b, Blocks.AIR.defaultBlockState());
                    else                     level.setBlockAndUpdate(b, ModBlocks.VOID.get().defaultBlockState());
                });
            }
        }, null, level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));
    }

    private void castAOEStorm(ServerLevel serverLevel, LivingEntity entity) {
        boolean griefing = BeyonderData.isGriefingEnabled(entity);
        Vec3 center = AbilityUtil.getTargetLocation(entity, 60*(int) multiplier(entity), 3);

        EffectManager.playEffect(EffectManager.Effect.SPACE_FRAGMENTATION, center.x, center.y, center.z, serverLevel, entity);

        serverLevel.playSound(null, BlockPos.containing(center), SoundEvents.WITHER_SPAWN, SoundSource.AMBIENT, 1.5f, 0.75f + random.nextFloat() * 0.5f);

        AtomicInteger ticks = new AtomicInteger();

        List<BlockPos> blocks = AbilityUtil.getBlocksInSphereRadius(serverLevel, center, 60, true, true, false);
        ServerScheduler.scheduleForDuration(0, 2, 20 * 25, () -> {
            ticks.addAndGet(1);

            if (ticks.get() % 10 == 0) {
                AbilityUtil.damageNearbyEntities(serverLevel, entity, 60* multiplier(entity), DamageLookup.lookupDamage(1, .4) * (int) Math.max(multiplier(entity)/6,1), center, true, false);
            }

            if(griefing) {
                blocks.stream().filter(b -> random.nextInt(175) == 0 && !serverLevel.getBlockState(b).is(ModBlocks.VOID.get())).forEach(b -> serverLevel.setBlockAndUpdate(b, Blocks.AIR.defaultBlockState()));
                blocks.stream().filter(b -> random.nextInt(60) == 0 && !serverLevel.getBlockState(b).isAir()).forEach(b -> serverLevel.setBlockAndUpdate(b, ModBlocks.VOID.get().defaultBlockState()));
            }
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(center, serverLevel)));
    }
}

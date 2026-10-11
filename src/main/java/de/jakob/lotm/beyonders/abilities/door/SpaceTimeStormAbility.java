package de.jakob.lotm.beyonders.abilities.door;

import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.block.ModBlocks;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.rendering.effectRendering.EffectIds;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
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
        super(id, 10, "explosion", "destruction");
        canBeCopied = false;
        interactionRadius = 60;
        interactionCacheTicks = 20 * 12;
        canBeShared = false;
        postsUsedAbilityEventManually = true;

        baseDamage = 1f; //needed to hook into multiplier
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 0));
    }

    @Override
    public float getSpiritualityCost() {
        return 50000;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.space_time_storm.area",
                "ability.lotmcraft.space_time_storm.targeted"
        };
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
        UUID effectId = EffectManager.playEffect(EffectIds.SPACE_TEAR, loc.getX(), loc.getY(), loc.getZ(), level, entity);

        AtomicInteger ticks = new AtomicInteger();
        final float damage = baseDamage * 7f;

        ServerScheduler.scheduleForDuration(0, 1, 20 * 10, () -> {
            Vec3 target = AbilityUtil.getTargetLocation(entity, 60, 3);
            if(target == null) return;

            ticks.addAndGet(1);

            loc.setPosition(target);
            if(entity.level() != level) {
                EffectManager.cancelEffect(effectId, level);
                return;
            }
            EffectManager.updateEffectPosition(effectId, loc.getX(), loc.getY(), loc.getZ(), level);

            level.playSound(null, BlockPos.containing(target), SoundEvents.WITHER_SHOOT, SoundSource.AMBIENT, 1.5f, 0.75f + random.nextFloat() * 0.5f);

            if(ticks.get() % 10 == 0) {
                AbilityUtil.damageNearbyEntities(level, entity, 15, ModDamageTypes.SPACE_DESTRUCTION, damage, target, true, false);
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
        Vec3 center = AbilityUtil.getTargetLocation(entity, (int) (60*multiplier(entity)), 3);

        EffectManager.playEffect(EffectIds.SPACE_FRAGMENTATION, center.x, center.y, center.z, serverLevel, entity);

        serverLevel.playSound(null, BlockPos.containing(center), SoundEvents.WITHER_SPAWN, SoundSource.AMBIENT, 1.5f, 0.75f + random.nextFloat() * 0.5f);

        AtomicInteger ticks = new AtomicInteger();
        final float damage = baseDamage * 3f;

        List<BlockPos> blocks = AbilityUtil.getBlocksInSphereRadius(serverLevel, center, 60, true, true, false);
        ServerScheduler.scheduleForDuration(0, 2, 20 * 25, () -> {
            ticks.addAndGet(1);

            if (ticks.get() % 10 == 0) {
                AbilityUtil.damageNearbyEntities(serverLevel, entity, 60, ModDamageTypes.SPACE_DESTRUCTION, damage, center, true, false);
            }

            if(griefing) {
                blocks.stream().filter(b -> random.nextInt(175) == 0 && !serverLevel.getBlockState(b).is(ModBlocks.VOID.get())).forEach(b -> serverLevel.setBlockAndUpdate(b, Blocks.AIR.defaultBlockState()));
                blocks.stream().filter(b -> random.nextInt(60) == 0 && !serverLevel.getBlockState(b).isAir()).forEach(b -> serverLevel.setBlockAndUpdate(b, ModBlocks.VOID.get().defaultBlockState()));
            }
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(center, serverLevel)));
    }
}

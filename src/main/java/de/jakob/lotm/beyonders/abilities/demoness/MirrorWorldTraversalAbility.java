package de.jakob.lotm.beyonders.abilities.demoness;

import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.demoness.handlers.GlassScanJob;
import de.jakob.lotm.dimension.MirrorGateManager;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.rendering.effectRendering.EffectIds;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.rendering.effectRendering.EffectParams;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

import java.util.Map;
import java.util.Set;

public class MirrorWorldTraversalAbility extends SelectableAbility {

    public MirrorWorldTraversalAbility(String id) {
        super(id,3);
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.mirror_world_traversal.enter", "ability.lotmcraft.mirror_world_traversal.scan"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if(!(level instanceof ServerLevel serverLevel)) return;
        switch (selectedAbility) {
            case 0 -> enterMirrorWorld(serverLevel, entity);
            case 1 -> scanForMirrors(serverLevel, entity);
        }
    }

    private void scanForMirrors(ServerLevel serverLevel, LivingEntity entity) {
        if (!serverLevel.dimension().equals(ModDimensions.MIRROR_WORLD_DIMENSION_KEY)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.mirror_world_traversal.not_in_mirror").withColor(getColorForPathway("demoness")));
            serverLevel.playSound(null, entity.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 1.0F, 0.8F);
            return;
        }

        ServerLevel overWorldLevel = serverLevel.getServer().getLevel(Level.OVERWORLD);
        if(overWorldLevel == null) {
            if(entity instanceof ServerPlayer player) player.sendSystemMessage(Component.translatable("ability.lotmcraft.mirror_world_traversal.no_mirrors_found").withColor(getColorForPathway("demoness")));
            serverLevel.playSound(null, entity.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 1.0F, 0.8F);
            return;
        }

        if(entity instanceof ServerPlayer player) player.sendSystemMessage(Component.translatable("ability.lotmcraft.mirror_world_traversal.scanning").withColor(getColorForPathway("demoness")));
        EffectManager.playEffect(EffectIds.RING_PULSE, entity.getX(), entity.getY() + 1, entity.getZ(), serverLevel, EffectParams.ofParams(0.5f, 0.1f, 0.7f));

        ServerScheduler.scheduleDelayed(1, () -> {
            Vec3 searchCenter = MirrorGateManager.getCoordinatesInOverworld(entity.blockPosition(), serverLevel).getCenter();

            new GlassScanJob(overWorldLevel, searchCenter, 100, nearestGlassBlock -> {
                if (nearestGlassBlock == null) {
                    if (entity instanceof ServerPlayer player) player.sendSystemMessage(Component.translatable("ability.lotmcraft.mirror_world_traversal.no_mirrors_found").withColor(getColorForPathway("demoness")));
                    serverLevel.playSound(null, entity.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 1.0F, 0.8F);
                    return;
                }

                Vec3 mirrorWorldPos = MirrorGateManager.getCoordinatesInMirrorWorld(nearestGlassBlock, serverLevel).getCenter();
                MirrorGateManager.createMirrorGate(serverLevel, BlockPos.containing(mirrorWorldPos), overWorldLevel, nearestGlassBlock);
                serverLevel.playSound(null, entity.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.2F);
            }).start();
        });
    }

    private void enterMirrorWorld(ServerLevel level, LivingEntity entity) {
        BlockPos nearestGlassBlock = getNearestGlassBlock(level, entity.position(), 20);
        if (nearestGlassBlock == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.mirror_world_traversal.no_glass").withColor(getColorForPathway("demoness")));
            level.playSound(null, entity.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 1.0F, 0.8F);
            return;
        }

        ServerLevel mirrorWorldLevel = level.getServer().getLevel(ModDimensions.MIRROR_WORLD_DIMENSION_KEY);
        if(mirrorWorldLevel == null) return;

        if(level == mirrorWorldLevel) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.mirror_world_traversal.already_in_mirror").withColor(getColorForPathway("demoness")));
            level.playSound(null, entity.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 1.0F, 0.8F);
            return;
        }

        level.playSound(null, entity.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 0.7F);

        BlockPos mirrorWorldPos = MirrorGateManager.getCoordinatesInMirrorWorld(nearestGlassBlock, mirrorWorldLevel);
        Vec3 teleportPos = mirrorWorldPos.getCenter();
        entity.teleportTo(mirrorWorldLevel, teleportPos.x, teleportPos.y, teleportPos.z, Set.of(), entity.getYRot(), entity.getXRot());
        entity.setPortalCooldown(20 * 5);

        mirrorWorldLevel.playSound(null, mirrorWorldPos, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 1.0F, 1.0F);

        ServerScheduler.scheduleDelayed(20, () -> {
            MirrorGateManager.createMirrorGate(mirrorWorldLevel, mirrorWorldPos, level, nearestGlassBlock);
        });
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("demoness", 4);
    }

    @Override
    protected float getSpiritualityCost() {
        return 500;
    }

    private static BlockPos getNearestGlassBlock(ServerLevel level, Vec3 pos, int searchRadius) {
        return AbilityUtil.getBlocksInSphereRadius(level, pos, searchRadius, true, true, false).stream().filter(b -> {
            BlockState state = level.getBlockState(b);
            return state.is(Tags.Blocks.GLASS_BLOCKS) || state.is(Tags.Blocks.GLASS_PANES);
        }).min((b1, b2) -> {
            double d1 = b1.distToCenterSqr(pos);
            double d2 = b2.distToCenterSqr(pos);
            return Double.compare(d1, d2);
        }).orElse(null);
    }

    @Override
    public void onHold(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel)) return;

        if (!level.dimension().equals(ModDimensions.MIRROR_WORLD_DIMENSION_KEY)) return;

        Vec3 overWorldPos = MirrorGateManager.getCoordinatesInOverworld(entity.blockPosition(), level).getCenter();
        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.mirror_world_traversal.overworld_coordinates", (int) overWorldPos.x, (int) overWorldPos.y, (int) overWorldPos.z).withColor(getColorForPathway("demoness")));
    }
}
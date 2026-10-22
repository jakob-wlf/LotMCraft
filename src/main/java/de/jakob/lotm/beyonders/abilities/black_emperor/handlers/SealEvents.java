package de.jakob.lotm.beyonders.abilities.black_emperor.handlers;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.black_emperor.EloquenceAbility;
import de.jakob.lotm.network.packets.toClient.SyncSealPayload;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

import static de.jakob.lotm.beyonders.abilities.black_emperor.SealAbility.lastRegionSent;


@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class SealEvents {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        SealedRegion region = SealRegistry.getRegionWithBoundary(level, event.getPos());
        if (region != null) {
            Player player = event.getPlayer();
            Player owner = event.getPlayer().getServer().getPlayerList().getPlayer(region.owner);
            if(owner == null) return;
            if(!canDistort(owner, player)) return;
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        SealedRegion region = SealRegistry.getRegionWithBoundary(level, event.getPos());
        if (region != null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if(!(event.getExplosion().getIndirectSourceEntity() instanceof Player player)) {
            event.getAffectedBlocks().removeIf(blockPos -> SealRegistry.getRegionWithBoundary(level, blockPos) != null);
            return;
        }
        List<BlockPos> regionBlocks = event.getAffectedBlocks().stream().filter(pos -> SealRegistry.getRegionWithBoundary(level, pos) != null).toList();
        if(regionBlocks.isEmpty()) return;
        SealedRegion region = SealRegistry.getRegionWithBoundary(level, regionBlocks.getFirst());
        if (region != null) {
            Player owner = player.getServer().getPlayerList().getPlayer(region.owner);
            if(owner == null) return;
            if(!canDistort(owner, player)) return;
            event.getAffectedBlocks().removeIf(blockPos -> SealRegistry.getRegionWithBoundary(level, blockPos) != null);
        }

    }

    @SubscribeEvent
    public static void onAttack(LivingIncomingDamageEvent event) {
        if(!(event.getEntity().level() instanceof ServerLevel level)) return;
        LivingEntity attacked = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        SealedRegion attackedRegion = SealRegistry.getRegionContaining(level, attacked.position());
        SealedRegion attackerRegion = SealRegistry.getRegionContaining(level, attacked.position());
        if(attackedRegion != null) {
            if(attackerRegion == null) event.setCanceled(true);
        }
        if(attackerRegion != null) {
            if(attackedRegion == null) event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        SealedRegion inside = SealRegistry.getRegionContaining(serverLevel, player.position());
        if(inside != null)
            lastRegionSent.put(player.getUUID(), inside);
        PacketDistributor.sendToPlayer(player, new SyncSealPayload(
                inside != null,
                inside == null ? 0.0 : inside.interiorBounds.minX,
                inside == null ? 0.0 : inside.interiorBounds.minY,
                inside == null ? 0.0 : inside.interiorBounds.minZ,
                inside == null ? 0.0 : inside.interiorBounds.maxX,
                inside == null ? 0.0 : inside.interiorBounds.maxY,
                inside == null ? 0.0 : inside.interiorBounds.maxZ
        ));
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        lastRegionSent.remove(event.getEntity().getUUID());
    }
    private static final int BUCKETS = 5;
    private static final double FACE_OFFSET = 0.55;
    private static final ParticleOptions PARTICLE = ParticleTypes.END_ROD;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        int bucket = event.getServer().getTickCount() % BUCKETS;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (SealedRegion region : SealRegistry.getAllInDimension(level.dimension())) {
                emitBoundaryParticles(level, region, bucket);
            }
        }
    }

    private static void emitBoundaryParticles(ServerLevel level, SealedRegion region, int bucket) {
        for (BlockPos pos : region.boundary) {
            if (Math.floorMod(pos.hashCode(), BUCKETS) != bucket) continue;

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.relative(dir);

                if (region.boundary.contains(neighbor)) continue;
                if (region.interior.contains(neighbor)) continue;
                if (!RoomDetector.isPassable(level, neighbor)) continue;

                Vec3 face = Vec3.atCenterOf(pos).add(
                        dir.getStepX() * FACE_OFFSET,
                        dir.getStepY() * FACE_OFFSET,
                        dir.getStepZ() * FACE_OFFSET
                );

                level.sendParticles(
                        PARTICLE,
                        face.x, face.y, face.z,
                        1,
                        0.05, 0.05, 0.05,
                        0.005
                );
            }
        }
    }

    public static boolean canDistort(LivingEntity entity, LivingEntity target) {
        RandomSource random = RandomSource.create();
        if(!BeyonderData.isBeyonder(target)) return true;
        double chance = random.nextDouble();
        return (chance < EloquenceAbility.calculateMultiplier(BeyonderData.getSequence(entity), BeyonderData.getSequence(target)));
    }


}
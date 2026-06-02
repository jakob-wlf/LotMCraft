package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.MysteriousTabletData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class AncientTraderSpawner {
    private static final int CHECK_INTERVAL_TICKS = 20 * 60 * 5;
    private static final double SPAWN_CHANCE = 0.01;
    private static final int MIN_SPAWN_RADIUS = 24;
    private static final int MAX_SPAWN_RADIUS = 64;
    private static final int MAX_ATTEMPTS = 10;

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        ServerLevel level = (ServerLevel) event.getLevel();
        if (level.getGameTime() % CHECK_INTERVAL_TICKS != 0) {
            return;
        }

        for (ServerPlayer player : level.players()) {
            if (level.random.nextDouble() > SPAWN_CHANCE) {
                continue;
            }

            BlockPos spawnPos = findSpawnPos(level, player.blockPosition(), level.random);
            if (spawnPos == null) {
                continue;
            }

            WanderingTrader trader = EntityType.WANDERING_TRADER.create(level);
            if (trader == null) {
                continue;
            }

            trader.getPersistentData().putBoolean("AncientTrader", true);
            trader.setCustomName(net.minecraft.network.chat.Component.literal("Ancient Trader"));
            trader.setCustomNameVisible(true);
            trader.setDespawnDelay(Integer.MAX_VALUE);
            trader.setPersistenceRequired();

            boolean includeFragment = MysteriousTabletData.get(level.getServer())
                    .canSpawnFragment(MysteriousTabletData.FragmentType.UPPER);
            trader.getOffers().clear();
            trader.getOffers().addAll(VillagerTradesEventHandler.buildAncientTraderOffers(level.random, includeFragment));

            trader.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                    level.random.nextFloat() * 360.0f, 0.0f);

            level.addFreshEntity(trader);
        }
    }

    private static BlockPos findSpawnPos(ServerLevel level, BlockPos center, RandomSource random) {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            int radius = random.nextInt(MAX_SPAWN_RADIUS - MIN_SPAWN_RADIUS + 1) + MIN_SPAWN_RADIUS;
            int x = center.getX() + random.nextInt(-radius, radius + 1);
            int z = center.getZ() + random.nextInt(-radius, radius + 1);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

            if (y <= level.getMinBuildHeight()) {
                continue;
            }

            BlockPos pos = new BlockPos(x, y, z);
            if (!level.getBlockState(pos).isAir()) {
                continue;
            }

            if (!level.getBlockState(pos.below()).isSolid()) {
                continue;
            }

            return pos;
        }

        return null;
    }

    
}

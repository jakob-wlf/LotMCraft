package de.jakob.lotm.events;

import com.mojang.datafixers.util.Pair;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class FragmentStructureProximityHandler {
    private static final int CHECK_INTERVAL_TICKS = 100;
    private static final int SEARCH_RADIUS = 1024;
    private static final int ACTIONBAR_RADIUS = 256;

    private static final ResourceKey<Structure> FRAGMENT_STRUCTURE_KEY = ResourceKey.create(
            Registries.STRUCTURE,
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "fragment_structure")
    );

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel level = event.getServer().getLevel(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY);
        if (level == null) {
            return;
        }

        if (level.getGameTime() % CHECK_INTERVAL_TICKS != 0) {
            return;
        }

        Holder<Structure> holder = level.registryAccess()
                .registry(Registries.STRUCTURE)
                .flatMap(registry -> registry.getHolder(FRAGMENT_STRUCTURE_KEY))
                .orElse(null);
        if (holder == null) {
            return;
        }

        for (ServerPlayer player : level.players()) {
                Pair<BlockPos, Holder<Structure>> nearest = level.getChunkSource()
                    .getGenerator()
                    .findNearestMapStructure(
                        level,
                        HolderSet.direct(holder),
                        player.blockPosition(),
                        SEARCH_RADIUS,
                        false
                    );

            if (nearest == null) {
                continue;
            }

            double distanceSq = nearest.getFirst().distSqr(player.blockPosition());
            if (distanceSq <= (double) ACTIONBAR_RADIUS * ACTIONBAR_RADIUS) {
                player.displayClientMessage(Component.literal("Theres something ancient nearby"), true);
            }
        }
    }
}
package de.jakob.lotm.dimension.abyss.fog;


import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;


@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class AbyssFogSync {
    private static final float BASE_FAR = 140.0f;
    private static final float[] ABYSS_FAR_BY_SEQUENCE = {
            220.0f, // seq 5
            320.0f, // seq 4
            480.0f, // seq 3
            700.0f, // seq 2
            1100.0f, // seq 1
            2000.0f // seq0
    };
    private static final int SYNC_INTERVAL_TICKS = 20;
    private static float farDistance(ServerPlayer player) {
        String pathway = BeyonderData.getPathway(player);
        if (!"abyss".equals(pathway)) return BASE_FAR;
        int sequence = BeyonderData.getSequence(player);
        if (sequence > 5) return BASE_FAR;
        return ABYSS_FAR_BY_SEQUENCE[5 - sequence];
    }
    private static void send(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new AbyssFogNetwork.AbyssFogPayload(farDistance(player)));
    }
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.level().dimension().equals(ModDimensions.ABYSS_DIMENSION_KEY)) return;
        if (player.tickCount % SYNC_INTERVAL_TICKS != 0) return;
        send(player);
    }
    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!event.getTo().equals(ModDimensions.ABYSS_DIMENSION_KEY)) return;
        send(player);
    }
}
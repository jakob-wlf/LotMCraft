package de.jakob.lotm.beyonders.advancementrituals;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Tracks the highest concurrent player count the server has ever had, for population-scaled rituals. */
@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ServerPopulationData extends SavedData {
    private static final String DATA_NAME = "ritual_server_population";

    private int highestPlayerCount = 0;

    public static ServerPopulationData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(new Factory<>(
                ServerPopulationData::new,
                ServerPopulationData::load
        ), DATA_NAME);
    }

    public int getHighestPlayerCount() {
        return highestPlayerCount;
    }

    private void recordPlayerCount(int count) {
        if (count > highestPlayerCount) {
            highestPlayerCount = count;
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putInt("highestPlayerCount", highestPlayerCount);
        return tag;
    }

    public static ServerPopulationData load(CompoundTag tag, HolderLookup.Provider provider) {
        ServerPopulationData data = new ServerPopulationData();
        data.highestPlayerCount = tag.getInt("highestPlayerCount");
        return data;
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        MinecraftServer server = event.getEntity().getServer();
        if (server == null) return;
        get(server).recordPlayerCount(server.getPlayerList().getPlayerCount());
    }
}

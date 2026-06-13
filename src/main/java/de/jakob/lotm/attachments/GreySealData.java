package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.UUID;

/**
 * Persists the Grey Fog seal state across server restarts.
 * The seal lasts permanently until the owner manually revokes it or loses the Sefirah Castle.
 */
public class GreySealData extends SavedData {

    private static final String DATA_NAME = "grey_seal_data";

    public boolean sealActive      = false;
    public UUID    ownerUUID       = null;
    public double  centerX         = 0;
    public double  centerY         = 0;
    public double  centerZ         = 0;
    public String  dimensionKey    = "minecraft:overworld";
    public long    cooldownExpiryMs = 0L;

    public static GreySealData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(
                new Factory<>(GreySealData::new, GreySealData::load),
                DATA_NAME);
    }

    public static GreySealData load(CompoundTag tag, HolderLookup.Provider registries) {
        GreySealData data = new GreySealData();
        data.sealActive       = tag.getBoolean("sealActive");
        data.cooldownExpiryMs = tag.getLong("cooldownExpiryMs");
        if (data.sealActive && tag.hasUUID("ownerUUID")) {
            data.ownerUUID    = tag.getUUID("ownerUUID");
            data.centerX      = tag.getDouble("centerX");
            data.centerY      = tag.getDouble("centerY");
            data.centerZ      = tag.getDouble("centerZ");
            data.dimensionKey = tag.getString("dimensionKey");
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("sealActive", sealActive);
        tag.putLong("cooldownExpiryMs", cooldownExpiryMs);
        if (sealActive && ownerUUID != null) {
            tag.putUUID("ownerUUID", ownerUUID);
            tag.putDouble("centerX", centerX);
            tag.putDouble("centerY", centerY);
            tag.putDouble("centerZ", centerZ);
            tag.putString("dimensionKey", dimensionKey);
        }
        return tag;
    }
}

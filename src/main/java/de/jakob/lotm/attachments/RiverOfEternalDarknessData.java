package de.jakob.lotm.attachments;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class RiverOfEternalDarknessData extends SavedData {

    private static final String DATA_NAME = "river_of_eternal_darkness";

    private boolean wellPlaced = false;
    private BlockPos wellPos = null;
    private String wellDimension = "";

    private boolean riverBuilt = false;

    public static RiverOfEternalDarknessData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(new Factory<>(
                RiverOfEternalDarknessData::new,
                RiverOfEternalDarknessData::load
        ), DATA_NAME);
    }

    public boolean isWellPlaced() {
        return wellPlaced;
    }

    public BlockPos getWellPos() {
        return wellPos;
    }

    public String getWellDimension() {
        return wellDimension;
    }

    public void setWellPlaced(BlockPos pos, ResourceLocation dimension) {
        this.wellPlaced = true;
        this.wellPos = pos;
        this.wellDimension = dimension.toString();
        setDirty();
    }

    public boolean isRiverBuilt() {
        return riverBuilt;
    }

    public void setRiverBuilt(boolean built) {
        this.riverBuilt = built;
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        tag.putBoolean("wellPlaced", wellPlaced);
        tag.putBoolean("riverBuilt", riverBuilt);

        if (wellPos != null) {
            tag.putInt("wellX", wellPos.getX());
            tag.putInt("wellY", wellPos.getY());
            tag.putInt("wellZ", wellPos.getZ());
        }

        if (wellDimension != null) {
            tag.putString("wellDimension", wellDimension);
        }

        return tag;
    }

    public static RiverOfEternalDarknessData load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        RiverOfEternalDarknessData data = new RiverOfEternalDarknessData();
        data.wellPlaced = tag.getBoolean("wellPlaced");
        data.riverBuilt = tag.getBoolean("riverBuilt");

        if (tag.contains("wellX")) {
            int x = tag.getInt("wellX");
            int y = tag.getInt("wellY");
            int z = tag.getInt("wellZ");
            data.wellPos = new BlockPos(x, y, z);
        }

        if (tag.contains("wellDimension")) {
            data.wellDimension = tag.getString("wellDimension");
        }

        return data;
    }
}

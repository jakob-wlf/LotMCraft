package de.jakob.lotm.attachments;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static de.jakob.lotm.beyonders.abilities.fool.FlamingJumpAbility.FIRE_MAP;


public class FlamingJumpData extends SavedData {

    private static final String DATA_NAME = "lotm_fire_tracker";

    public static final Factory<FlamingJumpData> FACTORY =
            new Factory<>(
                    FlamingJumpData::new,
                    FlamingJumpData::load,
                    null
            );

    public static FlamingJumpData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Map.Entry<ResourceKey<Level>, Set<BlockPos>> entry : FIRE_MAP.entrySet()) {
            String dimStr = entry.getKey().location().toString();
            for (BlockPos pos : entry.getValue()) {
                CompoundTag fireTag = new CompoundTag();
                fireTag.putString("Dimension", dimStr);
                fireTag.putLong("Pos", pos.asLong());
                list.add(fireTag);
            }
        }
        tag.put("Fires", list);
        return tag;
    }

    public static FlamingJumpData load(CompoundTag tag, HolderLookup.Provider provider) {
        FlamingJumpData data = new FlamingJumpData();
        ListTag list = tag.getList("Fires", Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag fireTag = list.getCompound(i);
            ResourceLocation dimLoc = ResourceLocation.parse(fireTag.getString("Dimension"));
            ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, dimLoc);
            BlockPos pos = BlockPos.of(fireTag.getLong("Pos"));

            FIRE_MAP.computeIfAbsent(dimKey, k -> ConcurrentHashMap.newKeySet()).add(pos);
        }
        return data;
    }
}
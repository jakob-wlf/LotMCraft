package de.jakob.lotm.dimension.spirit_world.spirit_patch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public class SpiritPortalLinks extends SavedData {

    private static final String NAME = "lotm_spirit_portal_links";

    private final Map<Long, Long> toSpirit = new HashMap<>();
    private final Map<Long, Long> toOverworld = new HashMap<>();

    public static SavedData.Factory<SpiritPortalLinks> factory() {
        return new SavedData.Factory<>(SpiritPortalLinks::new, SpiritPortalLinks::load, null);
    }

    public static SpiritPortalLinks get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(factory(), NAME);
    }

    public Long spiritFor(BlockPos overworldAnchor) { return toSpirit.get(overworldAnchor.asLong()); }
    public Long overworldFor(BlockPos spiritAnchor) { return toOverworld.get(spiritAnchor.asLong()); }

    public void link(BlockPos overworldAnchor, BlockPos spiritAnchor) {
        toSpirit.put(overworldAnchor.asLong(), spiritAnchor.asLong());
        toOverworld.put(spiritAnchor.asLong(), overworldAnchor.asLong());
        setDirty();
    }

    public BlockPos freeSpiritAnchor(BlockPos wanted) {
        BlockPos p = wanted;
        boolean clash = true;
        while (clash) {
            clash = false;
            for (long other : toOverworld.keySet()) {
                BlockPos o = BlockPos.of(other);
                if (Math.abs(o.getX() - p.getX()) < 10 && Math.abs(o.getZ() - p.getZ()) < 10) {
                    clash = true;
                    p = p.offset(12, 0, 0);
                    break;
                }
            }
        }
        return p;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLongArray("ToSpirit", flatten(toSpirit));
        return tag;
    }

    private static SpiritPortalLinks load(CompoundTag tag, HolderLookup.Provider registries) {
        SpiritPortalLinks links = new SpiritPortalLinks();
        long[] flat = tag.getLongArray("ToSpirit");
        for (int i = 0; i + 1 < flat.length; i += 2) {
            links.toSpirit.put(flat[i], flat[i + 1]);
            links.toOverworld.put(flat[i + 1], flat[i]);
        }
        return links;
    }

    private static long[] flatten(Map<Long, Long> m) {
        long[] out = new long[m.size() * 2];
        int i = 0;
        for (Map.Entry<Long, Long> e : m.entrySet()) { out[i++] = e.getKey(); out[i++] = e.getValue(); }
        return out;
    }
}

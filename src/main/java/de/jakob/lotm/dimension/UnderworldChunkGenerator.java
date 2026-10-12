package de.jakob.lotm.dimension;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class UnderworldChunkGenerator extends ChunkGenerator {

    public static final MapCodec<UnderworldChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
            ).apply(instance, instance.stable(UnderworldChunkGenerator::new)));

    

    record World(BlockState top, BlockState filler, BlockState deep,
                 BlockState prop, int propHeight, double propChance) {}

    private static World world(Block top, Block filler, Block deep, Block prop, int propHeight, double chance) {
        return new World(top.defaultBlockState(), filler.defaultBlockState(), deep.defaultBlockState(),
                prop == null ? null : prop.defaultBlockState(), propHeight, chance);
    }

    
    static final World[] WORLDS = {
            world(Blocks.GRASS_BLOCK,     Blocks.DIRT,        Blocks.STONE,       Blocks.SHORT_GRASS,    1, 0.06),  
            world(Blocks.SAND,            Blocks.SANDSTONE,   Blocks.SANDSTONE,   Blocks.DEAD_BUSH,      1, 0.012), 
            world(Blocks.SNOW_BLOCK,      Blocks.PACKED_ICE,  Blocks.STONE,       Blocks.PACKED_ICE,     5, 0.004), 
            world(Blocks.BLACKSTONE,      Blocks.BASALT,      Blocks.BLACKSTONE,  Blocks.BASALT,         6, 0.006), 
            world(Blocks.END_STONE,       Blocks.END_STONE,   Blocks.END_STONE,   Blocks.END_ROD,        1, 0.004), 
            world(Blocks.STONE,           Blocks.ANDESITE,    Blocks.STONE,       Blocks.COBBLESTONE,    2, 0.008), 
            world(Blocks.BONE_BLOCK,      Blocks.BONE_BLOCK,  Blocks.CALCITE,     Blocks.BONE_BLOCK,     3, 0.008), 
    };

    private static final BlockState AIR         = Blocks.AIR.defaultBlockState();
    private static final BlockState BEDROCK     = Blocks.BEDROCK.defaultBlockState();
    private static final BlockState DEEPSLATE   = Blocks.DEEPSLATE.defaultBlockState();
    private static final BlockState TUFF        = Blocks.TUFF.defaultBlockState();
    private static final BlockState STONE       = Blocks.STONE.defaultBlockState();
    private static final BlockState ANDESITE    = Blocks.ANDESITE.defaultBlockState();
    private static final BlockState WATER       = Blocks.WATER.defaultBlockState();
    private static final BlockState LAVA        = Blocks.LAVA.defaultBlockState();
    private static final BlockState GRAVEL      = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState BRIDGE      = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
    private static final BlockState RAILING     = Blocks.BLACKSTONE_WALL.defaultBlockState();

    private static final BlockState CALCITE     = Blocks.CALCITE.defaultBlockState();
    private static final BlockState DIORITE     = Blocks.DIORITE.defaultBlockState();
    private static final BlockState SNOW        = Blocks.SNOW_BLOCK.defaultBlockState();
    private static final BlockState BLACKSTONE  = Blocks.BLACKSTONE.defaultBlockState();
    private static final BlockState BASALT      = Blocks.BASALT.defaultBlockState();
    private static final BlockState BONE        = Blocks.BONE_BLOCK.defaultBlockState();
    private static final BlockState SCULK       = Blocks.SCULK.defaultBlockState();
    private static final BlockState MOSS        = Blocks.MOSS_BLOCK.defaultBlockState();
    private static final BlockState SEA_LANTERN = Blocks.SEA_LANTERN.defaultBlockState();
    private static final BlockState SHROOMLIGHT = Blocks.SHROOMLIGHT.defaultBlockState();
    private static final BlockState CRYING_OBS  = Blocks.CRYING_OBSIDIAN.defaultBlockState();
    private static final BlockState SOUL_LANTERN = Blocks.SOUL_LANTERN.defaultBlockState();
    private static final BlockState DEAD_LOG    = Blocks.BIRCH_LOG.defaultBlockState();
    private static final BlockState AMETHYST    = Blocks.AMETHYST_CLUSTER.defaultBlockState();
    private static final BlockState TIP_UP = Blocks.POINTED_DRIPSTONE.defaultBlockState()
            .setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.UP)
            .setValue(PointedDripstoneBlock.THICKNESS, DripstoneThickness.TIP);
    private static final BlockState TIP_DOWN = Blocks.POINTED_DRIPSTONE.defaultBlockState()
            .setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN)
            .setValue(PointedDripstoneBlock.THICKNESS, DripstoneThickness.TIP);

    

    public UnderworldChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    

    private static double h3(int x, int y, int z) { return UnderworldTerrain.hash3(x, y, z); }

    private static BlockState stateAt(UnderworldTerrain.Column col, int x, int y, int z, int depth) {
        BlockState s = switch (col.zone()) {
            case NATION -> nationState(col, x, y, z, depth);
            case CHASM  -> gorgeState(x, y, z, depth);
            case WILD   -> wildState(col, x, y, z, depth);
        };
        if (depth > 6 && y < 150) {
            BlockState ore = ore(x, y, z);
            if (ore != null) return ore;
        }
        return s;
    }

    private static BlockState ore(int x, int y, int z) {
        double o = h3(x * 5 + 1, y * 3 + 7, z * 5 - 3);
        if (o > 0.0107) return null;
        boolean deep = y < 38;
        if (o < 0.0040) return (deep ? Blocks.DEEPSLATE_COAL_ORE : Blocks.COAL_ORE).defaultBlockState();
        if (o < 0.0075) return (deep ? Blocks.DEEPSLATE_IRON_ORE : Blocks.IRON_ORE).defaultBlockState();
        if (o < 0.0095 && y < 70) return (deep ? Blocks.DEEPSLATE_GOLD_ORE : Blocks.GOLD_ORE).defaultBlockState();
        if (o < 0.0107 && y < 30) return Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState();
        return null;
    }

    private static BlockState paleTop(int x, int z) {
        double h = UnderworldTerrain.hash2(x, z);
        return h < 0.7 ? CALCITE : h < 0.9 ? SNOW : DIORITE;
    }

    private static BlockState paleRock(int x, int y, int z) {
        if (y < 38) return h3(x, y, z) < 0.25 ? TUFF : DEEPSLATE;
        return h3(x, y, z) < 0.5 ? DIORITE : CALCITE;
    }

    private static BlockState nationState(UnderworldTerrain.Column col, int x, int y, int z, int depth) {
        if (col.top() < 20) { 
            double h = h3(x, y, z);
            return h < 0.4 ? BLACKSTONE : h < 0.7 ? DEEPSLATE : h < 0.85 ? SCULK : BASALT;
        }
        if (depth == 0) return paleTop(x, z);
        if (depth <= 3) return CALCITE;
        return paleRock(x, y, z);
    }

    private static BlockState gorgeState(int x, int y, int z, int depth) {
        double h = h3(x, y, z);
        if (depth == 0) return h < 0.5 ? BLACKSTONE : h < 0.8 ? BASALT : h < 0.9 ? CRYING_OBS : SCULK;
        if (depth <= 3) return h < 0.7 ? BLACKSTONE : BASALT;
        return y < 50 ? (h < 0.2 ? TUFF : DEEPSLATE) : (h < 0.3 ? ANDESITE : STONE);
    }

    private static World worldFor(int x, int z) {
        return WORLDS[UnderworldTerrain.worldIndex(x, z)];
    }

    private static BlockState wildState(UnderworldTerrain.Column col, int x, int y, int z, int depth) {
        double h = h3(x, y, z);
        if (col.water() >= 0) { 
            if (depth == 0) return h < 0.3 ? SCULK : h < 0.8 ? BLACKSTONE : GRAVEL;
            if (depth <= 3) return h < 0.6 ? BLACKSTONE : GRAVEL;
        }
        if (depth == 0 && col.bank()) { 
            double b = UnderworldTerrain.hash2(x * 3, z * 3 + 1);
            if (b < 0.12) return CRYING_OBS;
            if (b < 0.50) return BLACKSTONE;
        }
        
        if (col.top() >= 122 && depth <= 4) {
            if (depth == 0 && col.top() >= 150) return SNOW;
            return h < 0.5 ? STONE : h < 0.8 ? ANDESITE : TUFF;
        }
        if (col.pale()) {
            if (depth == 0) return paleTop(x, z);
            if (depth <= 3) return CALCITE;
            return paleRock(x, y, z);
        }
        World w = worldFor(x, z);
        if (depth == 0) return w.top();
        if (depth <= 3) return w.filler();
        return y < 38 ? DEEPSLATE : w.deep();
    }

    private static BlockState ceilingState(UnderworldTerrain.Column col, int x, int y, int z, int depth) {
        if (y >= UnderworldTerrain.HEIGHT - 1) return BEDROCK;
        double h = h3(x, y, z);
        if (depth == 0 && h < 0.004) return col.pale() ? SEA_LANTERN : SHROOMLIGHT;
        if (col.pale()) return h < 0.5 ? DIORITE : CALCITE;
        return h < 0.5 ? DEEPSLATE : h < 0.8 ? TUFF : BLACKSTONE;
    }

    

    private static boolean solid(BlockState s) {
        return s != null && s != AIR && s != WATER && s != LAVA;
    }

    
    static void buildColumn(UnderworldTerrain.Column col, int wx, int wz, BlockState[] stack, BlockState[] deco) {
        final int top = col.top();
        final boolean nation = col.zone() == UnderworldTerrain.Zone.NATION;
        final int carveCap = top - (nation ? 10 : 4); 

        stack[0] = BEDROCK;
        for (int y = 1; y <= top; y++) {
            BlockState s;
            if (y <= 2) {
                s = h3(wx, y, wz) < (y == 1 ? 0.6 : 0.3) ? BEDROCK : DEEPSLATE;
            } else if (col.shaft() && y >= 12) {
                s = y <= 17 ? WATER : AIR;
            } else if (y <= carveCap && UnderworldTerrain.cave(wx, y, wz)) {
                s = y <= 9 ? (nation ? WATER : LAVA) : AIR; 
            } else {
                s = stateAt(col, wx, y, wz, top - y);
            }
            stack[y] = s;
        }

        for (int y = top + 1; y <= col.water(); y++) stack[y] = WATER;

        
        if (!col.shaft()) {
            for (int y = 4; y <= carveCap; y++) {
                if (stack[y] != AIR) continue;
                BlockState below = stack[y - 1], above = stack[y + 1];
                if (solid(below) && below != BEDROCK) {
                    double h = h3(wx * 3 + 1, y, wz * 3 + 2);
                    double t = h3(wx * 3 + 2, y, wz * 3 + 1);
                    if (col.pale()) { if (t < 0.20) stack[y - 1] = SCULK; }
                    else if (t < 0.30) stack[y - 1] = MOSS;
                    if (h < 0.010) deco[y] = col.pale() ? AMETHYST : TIP_UP;
                    else if (h < 0.022 && !col.pale()) deco[y] = TIP_UP;
                    else if (h < 0.0235) stack[y - 1] = col.pale() ? SEA_LANTERN : SHROOMLIGHT;
                }
                if (deco[y] == null && solid(above)) {
                    double g = h3(wx * 7 + 5, y * 3, wz * 7 + 1);
                    if (g < 0.03) deco[y] = TIP_DOWN;
                    else if (g < 0.034) stack[y + 1] = col.pale() ? SEA_LANTERN : SHROOMLIGHT;
                }
            }
            for (int y = 4; y <= carveCap; y++) {
                if (deco[y] != null && stack[y] == AIR) stack[y] = deco[y];
            }
        }

        
        if (col.deckTop() >= 0) {
            for (int y = col.deckTop() - 2; y <= col.deckTop(); y++) stack[y] = BRIDGE;
            if (col.edge()) stack[col.deckTop() + 1] = RAILING;
        }

        surfaceProps(col, wx, wz, stack);

        
        int ceil = UnderworldTerrain.ceilingBottom(wx, wz);
        for (int y = ceil; y < UnderworldTerrain.HEIGHT; y++) stack[y] = ceilingState(col, wx, y, wz, y - ceil);
    }

    
    private static void surfaceProps(UnderworldTerrain.Column col, int wx, int wz, BlockState[] stack) {
        if (col.shaft() || col.deckTop() >= 0 || col.water() >= 0) return;
        final int top = col.top();
        BlockState prop;
        int height;

        if (col.zone() == UnderworldTerrain.Zone.WILD) {
            if (col.bank()) {
                if (h3(wx, 9010, wz) < 0.03) stack[top + 1] = SOUL_LANTERN;
                return;
            }
            if (col.top() >= 122) return;
            if (col.pale()) {
                double h = h3(wx, 9003, wz);
                if (h < 0.004)       { prop = BONE;     height = 1 + (int) (h3(wx, 9004, wz) * 3); }
                else if (h < 0.0055) { prop = DEAD_LOG; height = 3 + (int) (h3(wx, 9005, wz) * 4); }
                else return;
            } else {
                World w = worldFor(wx, wz);
                if (w.prop() == null || h3(wx, 9001, wz) >= w.propChance()) return;
                prop = w.prop();
                height = 1 + (int) (h3(wx, 9002, wz) * w.propHeight());
            }
        } else if (col.zone() == UnderworldTerrain.Zone.NATION && top > 40) {
            double h = h3(wx, 9003, wz);
            if (h < 0.004)       { prop = BONE;     height = 1 + (int) (h3(wx, 9004, wz) * 3); }
            else if (h < 0.0055) { prop = DEAD_LOG; height = 3 + (int) (h3(wx, 9005, wz) * 4); }
            else return;
        } else {
            return;
        }

        for (int i = 1; i <= height && top + i < UnderworldTerrain.HEIGHT - 1; i++) stack[top + i] = prop;
    }

    

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState random,
                                                        StructureManager structures, ChunkAccess chunk) {
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        int minY = getMinY();
        int maxY = minY + getGenDepth() - 1;

        Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap surface    = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        BlockState[] stack = new BlockState[UnderworldTerrain.HEIGHT];
        BlockState[] deco  = new BlockState[UnderworldTerrain.HEIGHT];

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int wx = minX + lx, wz = minZ + lz;
                Arrays.fill(stack, null);
                Arrays.fill(deco, null);
                buildColumn(UnderworldTerrain.column(wx, wz), wx, wz, stack, deco);

                for (int y = minY; y <= maxY; y++) {
                    BlockState state = stack[y];
                    if (state == null || state == AIR) continue;
                    chunk.setBlockState(pos.set(lx, y, lz), state, false);
                    oceanFloor.update(lx, y, lz, state);
                    surface.update(lx, y, lz, state);
                }
            }
        }

        UnderworldStructures.Placer placer = (lx, y, lz, state) -> {
            chunk.setBlockState(pos.set(lx, y, lz), state, false);
            oceanFloor.update(lx, y, lz, state);
            surface.update(lx, y, lz, state);
        };

        
        UnderworldDecor.decorate(minX, minZ, minY, maxY, placer);

        
        UnderworldStructures.stamp(minX, minZ, minY, maxY, placer);

        return CompletableFuture.completedFuture(chunk);
    }

    

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState random, BiomeManager biomeManager,
                             StructureManager structures, ChunkAccess chunk, GenerationStep.Carving step) {
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState random, ChunkAccess chunk) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
    }

    

    @Override
    public int getGenDepth() {
        return UnderworldTerrain.HEIGHT;
    }

    @Override
    public int getSeaLevel() {
        return UnderworldTerrain.SEA_Y;
    }

    @Override
    public int getMinY() {
        return UnderworldTerrain.MIN_Y;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        UnderworldTerrain.Column col = UnderworldTerrain.column(x, z);
        boolean floor = type == Heightmap.Types.OCEAN_FLOOR || type == Heightmap.Types.OCEAN_FLOOR_WG;
        int h = Math.max(col.top(), col.deckTop());
        if (!floor) h = Math.max(h, col.water());
        return Math.min(level.getMaxBuildHeight(), h + 1); 
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        BlockState[] stack = new BlockState[UnderworldTerrain.HEIGHT];
        buildColumn(UnderworldTerrain.column(x, z), x, z, stack, new BlockState[UnderworldTerrain.HEIGHT]);
        int minY = level.getMinBuildHeight();
        BlockState[] states = new BlockState[level.getHeight()];
        for (int i = 0; i < states.length; i++) {
            int y = minY + i;
            BlockState s = (y >= 0 && y < stack.length) ? stack[y] : null;
            states[i] = s == null ? AIR : s;
        }
        return new NoiseColumn(minY, states);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        UnderworldTerrain.Column col = UnderworldTerrain.column(pos.getX(), pos.getZ());
        info.add("Underworld zone: " + col.zone() + " pale: " + col.pale() + " top: " + col.top()
                + " world: " + UnderworldTerrain.worldIndex(pos.getX(), pos.getZ()));
    }
}
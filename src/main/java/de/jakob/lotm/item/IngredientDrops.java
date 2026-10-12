package de.jakob.lotm.item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.jakob.lotm.LOTMCraft;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class IngredientDrops {

    private static ItemStack stack(String name) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, name));
        return new ItemStack(item);
    }

    private static void give(Player p, ItemStack out) {
        if (out.isEmpty()) return;
        if (!p.getInventory().add(out)) p.drop(out, false);
    }

    private static boolean isNight(Level l) { long t = l.getDayTime() % 24000L; return t >= 13000L && t < 23000L; }

    @FunctionalInterface
    private interface MobCheck { boolean test(LivingEntity victim, Player killer); }

    private record MobDrop(EntityType<?> type, String result, float chance, boolean bottle, MobCheck check) {}

    private static MobDrop m(EntityType<?> t, String res, float ch) { return new MobDrop(t, res, ch, false, (v, k) -> true); }
    private static MobDrop mc(EntityType<?> t, String res, float ch, MobCheck c) { return new MobDrop(t, res, ch, false, c); }
    private static MobDrop mb(EntityType<?> t, String res, float ch, MobCheck c) { return new MobDrop(t, res, ch, true, c); }

    private static boolean inMineshaft(LivingEntity v) {
        return v.level() instanceof ServerLevel sl
                && sl.structureManager().getStructureWithPieceAt(v.blockPosition(), StructureTags.MINESHAFT).isValid();
    }

    private static final List<MobDrop> MOB_DROPS = List.of(
        mb(EntityType.WITHER_SKELETON, "imprisoned_soul",                   0.12f, (v, k) -> true),
        m (EntityType.WITHER_SKELETON, "cloth_wrapped_persons_crystal",     0.025f),
        mc(EntityType.SKELETON,        "trapped_ghost_residue_powder",      0.02f,
                (v, k) -> v.level().getBiome(v.blockPosition()).is(Biomes.SOUL_SAND_VALLEY)),
        mb(EntityType.WOLF,            "shadowless_demonic_wolf_blood",     0.35f, (v, k) -> isNight(v.level())),
        m (EntityType.EVOKER,          "death_calling_crows_eyeball",       0.20f),
        m (EntityType.VEX,             "cloaked_specter_attachment",        0.07f),
        m (EntityType.PHANTOM,         "black_feather_of_monster_bird",     0.05f),
        mc(EntityType.CAVE_SPIDER,     "black_widow_spider_silk_gland",     0.05f, (v, k) -> inMineshaft(v)),
        m (EntityType.RAVAGER,         "spirit_eater_stomach_pouch",        0.20f),
        m (EntityType.ENDERMITE,       "crystal_threadworm",                0.12f),
        m (EntityType.PIGLIN_BRUTE,    "heart_of_a_magma_titan",            0.20f),
        m (EntityType.HOGLIN,          "land_rhinoceros_core_horn_crystal", 0.10f),
        m (EntityType.AXOLOTL,         "rainbow_salamander_pituitary_gland",0.15f),
        mc(EntityType.MAGMA_CUBE,      "fire_salamander_gland",             0.08f, (v, k) -> v instanceof Slime s && s.getSize() >= 2),
        m (EntityType.STRIDER,         "magma_elf_core",                    0.10f),
        m (EntityType.BLAZE,           "candle_devourer_core",              0.04f),
        m (EntityType.PILLAGER,        "dream_eating_raven_heart",          0.08f),
        m (EntityType.VINDICATOR,      "dream_eating_raven_heart",          0.08f),
        m (EntityType.SILVERFISH,      "dream_eating_rat_heart",            0.04f),
        mc(EntityType.SLIME,           "mutated_pituitary_gland_of_a_thousand_faced_hunter", 0.08f,
                (v, k) -> v instanceof Slime s && s.getSize() >= 4),
        m (EntityType.ZOMBIE_VILLAGER, "human_skined_shadow_characteristic",0.08f),
        m (EntityType.POLAR_BEAR,      "silver_war_bear_right_palm",        0.25f),
        m (EntityType.BEE,             "soul_bewitching_insect_colony_adult_insect", 0.08f)
    );

    @SubscribeEvent
    public static void onMobDrops(LivingDropsEvent e) {
        LivingEntity victim = e.getEntity();
        Level level = victim.level();
        if (level.isClientSide()) return;
        if (!(e.getSource().getEntity() instanceof Player killer)) return;

        for (MobDrop d : MOB_DROPS) {
            if (victim.getType() != d.type()) continue;
            if (d.bottle() && !killer.getOffhandItem().is(Items.GLASS_BOTTLE)) continue;
            if (!d.check().test(victim, killer)) continue;
            if (level.random.nextFloat() >= d.chance()) continue;
            ItemStack out = stack(d.result());
            if (out.isEmpty()) continue;
            if (d.bottle() && !killer.getAbilities().instabuild) killer.getOffhandItem().shrink(1);
            e.getDrops().add(new ItemEntity(level, victim.getX(), victim.getY(), victim.getZ(), out));
        }
    }

    private static final int ANY = 0, SHOVEL = 1, SHEARS = 2;

    @FunctionalInterface
    private interface BlockCheck { boolean test(ServerLevel l, BlockPos p, BlockState s); }

    private static final BlockCheck NONE = (l, p, s) -> true;

    private record BlockDrop(String result, float chance, int tool, boolean natural, BlockCheck extra, Set<Block> blocks) {}

    private static BlockDrop bd(String res, float ch, int tool, boolean natural, BlockCheck extra, Block... bs) {
        return new BlockDrop(res, ch, tool, natural, extra, Set.of(bs));
    }

    private static BlockCheck and(BlockCheck a, BlockCheck b) { return (l, p, s) -> a.test(l, p, s) && b.test(l, p, s); }
    @SafeVarargs
    private static BlockCheck biome(ResourceKey<Biome>... keys) {
        return (l, p, s) -> { for (ResourceKey<Biome> k : keys) if (l.getBiome(p).is(k)) return true; return false; };
    }
    private static BlockCheck biomeTag(TagKey<Biome> t) { return (l, p, s) -> l.getBiome(p).is(t); }
    private static BlockCheck inStructure(ResourceKey<Structure> key) {
        return (l, p, s) -> {
            Structure structure = l.registryAccess().registryOrThrow(Registries.STRUCTURE).get(key);
            return structure != null && l.structureManager().getStructureWithPieceAt(p, structure).isValid();
        };
    }

    private static final List<BlockDrop> BLOCK_DROPS = List.of(
        bd("ancient_wraith_dust",              0.08f, ANY,    true,  NONE, Blocks.ANCIENT_DEBRIS),
        bd("spirit_world_contaminated_soil",   0.10f, SHOVEL, true,  NONE, Blocks.SOUL_SOIL),
        bd("midnight_beauty_flower",           0.35f, ANY,    true,  NONE, Blocks.SCULK_CATALYST),
        bd("feynapotter_fly_powder",           0.20f, ANY,    true,  NONE, Blocks.BEE_NEST),
        bd("wild_rose",                        0.30f, SHEARS, false, biomeTag(BiomeTags.IS_FOREST), Blocks.ROSE_BUSH),
        bd("blasphemous_priest_fungal_shroud", 0.008f, SHEARS, true, biome(Biomes.DARK_FOREST),
                Blocks.RED_MUSHROOM_BLOCK, Blocks.BROWN_MUSHROOM_BLOCK),
        bd("siskun_demon_worm_nest",           0.08f, SHOVEL, true,  biome(Biomes.SWAMP, Biomes.MANGROVE_SWAMP),
                Blocks.MUD, Blocks.ROOTED_DIRT),
        bd("gem_devouring_worm",               0.06f, ANY,    true,  NONE, Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE),
        bd("siskun_demon_worm",                0.15f, ANY,    false, inStructure(BuiltinStructures.STRONGHOLD),
                Blocks.INFESTED_STONE_BRICKS, Blocks.INFESTED_MOSSY_STONE_BRICKS,
                Blocks.INFESTED_CRACKED_STONE_BRICKS, Blocks.INFESTED_CHISELED_STONE_BRICKS),
        bd("siren_rock",                       0.05f, ANY,    true,  inStructure(BuiltinStructures.OCEAN_MONUMENT),
                Blocks.SEA_LANTERN, Blocks.DARK_PRISMARINE),
        bd("pure_white_elf_flowers",           0.25f, SHEARS, false, NONE, Blocks.LILY_OF_THE_VALLEY),
        bd("celestine",                        0.12f, ANY,    true,  NONE, Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE),
        bd("true_root_of_mist_treant",         0.05f, ANY,    true,  (l, p, s) -> l.isRaining(), Blocks.MANGROVE_ROOTS),
        bd("strange_faced_cannabis_crystal",   0.20f, ANY,    true,  NONE, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE),
        bd("dust_of_a_lake_spirit",            0.12f, SHOVEL, true,  NONE, Blocks.CLAY),
        bd("moonstone",                        0.12f, ANY,    true,  NONE, Blocks.NETHER_QUARTZ_ORE),
        bd("amethyst",                         0.20f, ANY,    true,  NONE, Blocks.AMETHYST_CLUSTER),
        bd("wandering_asteroid_core",          0.03f, ANY,    true,  NONE, Blocks.OBSIDIAN),
        bd("worm_of_spirit",                   0.12f, SHOVEL, true,  NONE, Blocks.MYCELIUM),
        bd("soul_bewitching_insect_colony_larvae", 0.30f, ANY, false, NONE, Blocks.FROGSPAWN)
    );

    private static final Set<Block> TRACKED = new HashSet<>();
    static {
        for (BlockDrop d : BLOCK_DROPS) if (d.natural()) TRACKED.addAll(d.blocks());
    }

    public static class PlacedBlockTracker extends SavedData {
        private static final String NAME = LOTMCraft.MOD_ID + "_placed_blocks";
        private final LongOpenHashSet positions = new LongOpenHashSet();

        public static PlacedBlockTracker get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(
                    new SavedData.Factory<>(PlacedBlockTracker::new, PlacedBlockTracker::load, null), NAME);
        }

        public static PlacedBlockTracker load(CompoundTag tag, HolderLookup.Provider provider) {
            PlacedBlockTracker t = new PlacedBlockTracker();
            for (long l : tag.getLongArray("positions")) t.positions.add(l);
            return t;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
            tag.putLongArray("positions", positions.toLongArray());
            return tag;
        }

        public void add(BlockPos pos) { if (positions.add(pos.asLong())) setDirty(); }
        public boolean remove(BlockPos pos) {
            boolean r = positions.remove(pos.asLong());
            if (r) setDirty();
            return r;
        }
    }

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent e) {
        if (!(e.getLevel() instanceof ServerLevel level)) return;
        if (!TRACKED.contains(e.getPlacedBlock().getBlock())) return;
        PlacedBlockTracker.get(level).add(e.getPos());
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent e) {
        if (!(e.getLevel() instanceof ServerLevel level)) return;
        Player player = e.getPlayer();
        BlockPos pos = e.getPos();
        BlockState state = e.getState();
        Block block = state.getBlock();

        boolean placed = TRACKED.contains(block) && PlacedBlockTracker.get(level).remove(pos);
        if (player.getAbilities().instabuild) return;
        ItemStack tool = player.getMainHandItem();

        for (BlockDrop d : BLOCK_DROPS) {
            if (!d.blocks().contains(block)) continue;
            if (d.natural() && placed) continue;
            if (d.tool() == SHOVEL && !tool.canPerformAction(ItemAbilities.SHOVEL_DIG)) continue;
            if (d.tool() == SHEARS && !tool.canPerformAction(ItemAbilities.SHEARS_HARVEST)) continue;
            if (!d.extra().test(level, pos, state)) continue;
            if (level.random.nextFloat() >= d.chance()) continue;
            ItemStack out = stack(d.result());
            if (!out.isEmpty()) Block.popResource(level, pos, out);
        }
    }

    private record LootDrop(String result, float chance, List<ResourceLocation> tables) {}

    private static LootDrop ld(String res, float ch, String... tables) {
        List<ResourceLocation> ids = new ArrayList<>();
        for (String t : tables) ids.add(ResourceLocation.withDefaultNamespace(t));
        return new LootDrop(res, ch, ids);
    }

    private static final List<LootDrop> LOOT_DROPS = List.of(
        // chests (chance is per chest)
        ld("cursed_artifacts_of_an_ancient_wraith", 0.25f, "chests/woodland_mansion"),
        ld("tree_of_elders_fruit",                  0.20f, "chests/woodland_mansion"),
        ld("hornacis_gray_mountain_goat_horn",      0.12f, "chests/pillager_outpost"),
        ld("divine_blessed_crystal",                0.20f, "chests/ancient_city", "chests/ancient_city_ice_box"),
        ld("sphinx_brain",                          0.25f, "chests/desert_pyramid"),
        // suspicious sand / gravel (chance is per brushed block)
        ld("ancient_wraith_artifact",               0.10f, "archaeology/trail_ruins_common"),
        ld("ancient_wraith_artifact",               0.25f, "archaeology/trail_ruins_rare"),
        ld("mummy_ashes",                           0.20f, "archaeology/desert_pyramid", "archaeology/desert_well"),
        ld("illusory_chime_tree_fruit",             0.15f, "archaeology/ocean_ruin_warm")
    );

    @SubscribeEvent
    public static void onLootLoad(LootTableLoadEvent e) {
        ResourceLocation id = e.getName();
        for (LootDrop d : LOOT_DROPS) {
            if (!d.tables().contains(id)) continue;
            ItemStack out = stack(d.result());
            if (out.isEmpty()) continue;
            e.getTable().addPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(d.chance()))
                    .add(LootItem.lootTableItem(out.getItem()))
                    .build());
        }
    }
}
package de.jakob.lotm.item;

import java.util.List;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class IngredientSources {
    private static ItemStack stack(String name) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, name));
        return new ItemStack(item);
    }

    private static boolean isWaterBottle(ItemStack s) {
        if (!s.is(Items.POTION)) return false;
        PotionContents c = s.get(DataComponents.POTION_CONTENTS);
        return c != null && c.is(Potions.WATER);
    }

    private static void give(Player p, ItemStack out) {
        if (out.isEmpty()) return;
        if (!p.getInventory().add(out)) p.drop(out, false);
    }

    @FunctionalInterface
    private interface Check { boolean test(Level l, BlockPos p, BlockState s); }

    private record Rule(String result, float chance, int cooldown, boolean fluid, boolean waterBottle, Check check) {}

    private static Rule r(String res, float ch, int cd, Check c)  { return new Rule(res, ch, cd, false, false, c); }
    private static Rule rf(String res, float ch, int cd, Check c) { return new Rule(res, ch, cd, true, false, c); }
    private static Rule rw(String res, float ch, int cd, Check c) { return new Rule(res, ch, cd, false, true, c); }

    private static Check blocks(Block... bs) {
        return (l, p, s) -> { for (Block b : bs) if (s.is(b)) return true; return false; };
    }
    private static Check and(Check a, Check b) { return (l, p, s) -> a.test(l, p, s) && b.test(l, p, s); }
    @SafeVarargs
    private static Check biome(ResourceKey<Biome>... keys) {
        return (l, p, s) -> { for (ResourceKey<Biome> k : keys) if (l.getBiome(p).is(k)) return true; return false; };
    }
    private static Check biomeTag(TagKey<Biome> t) { return (l, p, s) -> l.getBiome(p).is(t); }
    private static Check water() {
        return (l, p, s) -> s.getFluidState().is(FluidTags.WATER) && s.getFluidState().isSource();
    }
    private static boolean isNight(Level l) { long t = l.getDayTime() % 24000L; return t >= 13000L && t < 23000L; }

    private static final List<Rule> RULES = List.of(
        r("another_tears_shed_due_to_shattered_ideals", 1.0f, 200, blocks(Blocks.CRYING_OBSIDIAN)),
        r("golden_jimsonweed_juice",   0.50f, 20, blocks(Blocks.DANDELION, Blocks.SUNFLOWER)),
        r("tornapple_jimsonweed_juice",0.50f, 20, blocks(Blocks.LILAC)),
        r("black_jimsonweed_juice",    0.60f, 20, blocks(Blocks.WITHER_ROSE)),
        r("lavender_hydrosol",         0.60f, 20, blocks(Blocks.ALLIUM)),
        r("water_violet_hydrosol",     0.25f, 40, and(blocks(Blocks.BLUE_ORCHID), biome(Biomes.SWAMP, Biomes.MANGROVE_SWAMP))),
        r("mist_treant_juice",         0.50f, 40, and(
                and((l, p, s) -> s.is(BlockTags.LOGS) || s.is(BlockTags.LEAVES),
                    biome(Biomes.SWAMP, Biomes.MANGROVE_SWAMP, Biomes.DARK_FOREST)),
                (l, p, s) -> l.isRaining())),
        r("hornbeam_essential_oils",    0.50f, 20, and(blocks(Blocks.OAK_LOG), biome(Biomes.BIRCH_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST))),
        r("larch_essential_oil",       0.60f, 20, and(blocks(Blocks.SPRUCE_LOG), biomeTag(BiomeTags.IS_TAIGA))),
        r("radiance_spirit_pact_tree_juice", 0.40f, 20, blocks(Blocks.CAVE_VINES, Blocks.CAVE_VINES_PLANT, Blocks.GLOW_LICHEN)),
        r("aqua_fern_juice",           0.50f, 20, blocks(Blocks.KELP, Blocks.KELP_PLANT, Blocks.SEAGRASS, Blocks.TALL_SEAGRASS)),

        rf("ancient_well_water", 1.0f, 10, and(water(),
            (l, p, s) -> l instanceof ServerLevel sl
                && sl.structureManager().getStructureWithPieceAt(p, StructureTags.VILLAGE).isValid())),
        rf("spring_water_of_golden_spring", 1.0f, 10, and(water(), biome(Biomes.LUSH_CAVES, Biomes.DRIPSTONE_CAVES))),
        rf("lake_water_freshly_reflecting_the_cosmos", 0.80f, 20, and(water(),
            (l, p, s) -> isNight(l) && !l.isRaining() && l.canSeeSky(p.above())
                && !l.getBiome(p).is(BiomeTags.IS_OCEAN))),

        rw("soaking_poplar_bark_extracted", 1.0f, 10, blocks(Blocks.BIRCH_LOG))
    );

    @SubscribeEvent
    public static void onBottleUse(PlayerInteractEvent.RightClickItem e) {
        Level level = e.getLevel();
        if (level.isClientSide() || e.getHand() != InteractionHand.MAIN_HAND) return;
        Player player = e.getEntity();
        ItemStack held = e.getItemStack();
        boolean empty = held.is(Items.GLASS_BOTTLE);
        boolean water = isWaterBottle(held);
        if (!empty && !water) return;
        if (player.getCooldowns().isOnCooldown(held.getItem())) return;

        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0f).scale(player.blockInteractionRange()));
        BlockHitResult solid = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        BlockHitResult liquid = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, player));

        for (Rule rule : RULES) {
            if (rule.waterBottle() ? !water : !empty) continue;
            BlockHitResult hit = rule.fluid() ? liquid : solid;
            if (hit.getType() != HitResult.Type.BLOCK) continue;
            BlockPos pos = hit.getBlockPos();
            if (!rule.check().test(level, pos, level.getBlockState(pos))) continue;

            player.getCooldowns().addCooldown(held.getItem(), rule.cooldown());
            if (level.random.nextFloat() >= rule.chance()) return;

            if (!player.getAbilities().instabuild) held.shrink(1);
            give(player, stack(rule.result()));
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0f, 1.0f);
            e.setCancellationResult(InteractionResult.SUCCESS);
            e.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent
    public static void onGoatMilk(PlayerInteractEvent.EntityInteract e) {
        Level level = e.getLevel();
        if (level.isClientSide() || e.getHand() != InteractionHand.MAIN_HAND) return;
        if (!(e.getTarget() instanceof Goat goat)) return;
        ItemStack held = e.getItemStack();
        if (!held.is(Items.GLASS_BOTTLE)) return;
        if (!level.getBiome(goat.blockPosition()).is(BiomeTags.IS_MOUNTAIN)) return;
        Player player = e.getEntity();
        if (player.getCooldowns().isOnCooldown(held.getItem())) return;

        player.getCooldowns().addCooldown(held.getItem(), 200);
        if (level.random.nextFloat() >= 0.5f) return;
        if (!player.getAbilities().instabuild) held.shrink(1);
        give(player, stack("goat_beard_hydrosol"));
        level.playSound(null, goat.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0f, 1.0f);
        e.setCancellationResult(InteractionResult.SUCCESS);
        e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onStrip(BlockEvent.BlockToolModificationEvent e) {
        if (e.isSimulated() || e.getItemAbility() != ItemAbilities.AXE_STRIP) return;
        if (!(e.getLevel() instanceof ServerLevel level)) return;
        BlockState s = e.getState();
        String id; float chance;
        if (s.is(Blocks.JUNGLE_LOG) || s.is(Blocks.JUNGLE_WOOD))      { id = "drago_bark";          chance = 0.10f; }
        else if (s.is(Blocks.BIRCH_LOG) || s.is(Blocks.BIRCH_WOOD))   { id = "red_hair_birch_bark"; chance = 0.08f; }
        else return;
        if (level.random.nextFloat() < chance) Block.popResource(level, e.getPos(), stack(id));
    }

    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() != VillagerProfession.FARMER) return;
        ItemStack wine = stack("red_wine");
        if (wine.isEmpty()) return;
        e.getTrades().get(5).add(new BasicItemListing(24, wine, 2, 30, 0.05f));
    }
}
package de.jakob.lotm.item;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.phys.AABB;
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
import net.neoforged.neoforge.event.village.WandererTradesEvent;

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

    private record Rule(String result, float chance, int cooldown, boolean fluid, boolean waterBottle, Check check, Consumer<Player> after) {}

    private static Rule r(String res, float ch, int cd, Check c)  { return new Rule(res, ch, cd, false, false, c, null); }
    private static Rule rf(String res, float ch, int cd, Check c) { return new Rule(res, ch, cd, true, false, c, null); }
    private static Rule rw(String res, float ch, int cd, Check c) { return new Rule(res, ch, cd, false, true, c, null); }
    private static Rule rfh(String res, float ch, int cd, Check c) { return new Rule(res, ch, cd, true, false, c, BURN); }

    private static final Consumer<Player> BURN = p -> {
        p.igniteForSeconds(4.0f);
        p.hurt(p.level().damageSources().lava(), 2.0f);
    };

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
    private static Check lava() {
        return (l, p, s) -> s.getFluidState().is(FluidTags.LAVA) && s.getFluidState().isSource();
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

            r("daffodil_juice",            0.50f, 20, blocks(Blocks.ORANGE_TULIP)),
            r("de_spiritualized_depraved_breath", 0.50f, 40, and(blocks(Blocks.SOUL_FIRE), biome(Biomes.SOUL_SAND_VALLEY))),

            rf("ancient_well_water", 1.0f, 10, and(water(),
                    (l, p, s) -> l instanceof ServerLevel sl
                            && sl.structureManager().getStructureWithPieceAt(p, StructureTags.VILLAGE).isValid())),
            rf("spring_water_of_golden_spring", 1.0f, 10, and(water(), biome(Biomes.LUSH_CAVES, Biomes.DRIPSTONE_CAVES))),
            rf("lake_water_freshly_reflecting_the_cosmos", 0.80f, 20, and(water(),
                    (l, p, s) -> isNight(l) && !l.isRaining() && l.canSeeSky(p.above())
                            && !l.getBiome(p).is(BiomeTags.IS_OCEAN))),
            rfh("fire_salamander_blood", 0.50f, 40, lava()),

            rw("soaking_poplar_bark_extracted", 1.0f, 10, blocks(Blocks.BIRCH_LOG))
    );


    private record EntityRule(EntityType<?> type, String result, float chance, int cooldown,
                              boolean sneak, float damage, Predicate<LivingEntity> check) {}

    private static EntityRule er(EntityType<?> t, String res, float ch, int cd) {
        return new EntityRule(t, res, ch, cd, false, 0f, e -> true);
    }

    private static boolean inBiome(LivingEntity e, TagKey<Biome> tag) {
        return e.level().getBiome(e.blockPosition()).is(tag);
    }

    private static final List<EntityRule> ENTITY_RULES = List.of(
            new EntityRule(EntityType.GOAT, "goat_beard_hydrosol", 0.50f, 200, false, 0f, e -> inBiome(e, BiomeTags.IS_MOUNTAIN)),
            er(EntityType.ZOMBIFIED_PIGLIN, "soul_snatcher_blood", 0.35f, 100),
            er(EntityType.BAT, "dream_eating_rat_blood", 0.40f, 100),
            er(EntityType.GLOW_SQUID, "mirror_dragon_blood", 0.40f, 100),
            er(EntityType.CAMEL, "sphinx_blood", 0.40f, 200),
            er(EntityType.WITCH, "swamp_giant_blood", 0.25f, 100),
            new EntityRule(EntityType.OCELOT, "dark_patterned_black_panther_spinal_fluid", 0.30f, 1200, false, 0f,
                    e -> inBiome(e, BiomeTags.IS_JUNGLE)),
            er(EntityType.FROG, "black_hunting_giant_lizard_spinal_fluid", 0.30f, 1200),
            new EntityRule(EntityType.SNIFFER, "mind_dragon_blood_adult", 0.30f, 600, false, 0f, e -> !e.isBaby()),
            new EntityRule(EntityType.VILLAGER, "thousand_faced_hunter_blood", 0.35f, 200, true, 1.0f, e -> !e.isBaby())
    );


    private static AreaEffectCloud findHarmingCloud(Level level, Vec3 from, Vec3 to) {
        for (AreaEffectCloud c : level.getEntitiesOfClass(AreaEffectCloud.class, new AABB(from, to).inflate(2.0))) {
            String nbt = c.saveWithoutId(new net.minecraft.nbt.CompoundTag()).toString();
            if (!nbt.contains("harming")) continue;
            if (c.getBoundingBox().inflate(0.25).clip(from, to).isPresent()) return c;
        }
        return null;
    }

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

        if (empty) {
            AreaEffectCloud cloud = findHarmingCloud(level, eye, end);
            if (cloud != null) {
                player.getCooldowns().addCooldown(held.getItem(), 40);
                cloud.setRadius(cloud.getRadius() - 0.5f);
                if (cloud.getRadius() < 0.5f) cloud.discard();
                if (level.random.nextFloat() < 0.30f) {
                    if (!player.getAbilities().instabuild) held.shrink(1);
                    give(player, stack("spirit_of_depraved_breath"));
                    level.playSound(null, player.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0f, 1.0f);
                }
                e.setCancellationResult(InteractionResult.SUCCESS);
                e.setCanceled(true);
                return;
            }
        }

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
            if (rule.after() != null) rule.after().accept(player);
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0f, 1.0f);
            e.setCancellationResult(InteractionResult.SUCCESS);
            e.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent
    public static void onBottleEntity(PlayerInteractEvent.EntityInteract e) {
        Level level = e.getLevel();
        if (level.isClientSide() || e.getHand() != InteractionHand.MAIN_HAND) return;
        if (!(e.getTarget() instanceof LivingEntity target)) return;
        ItemStack held = e.getItemStack();
        if (!held.is(Items.GLASS_BOTTLE)) return;
        Player player = e.getEntity();

        for (EntityRule rule : ENTITY_RULES) {
            if (target.getType() != rule.type()) continue;
            if (rule.sneak() && !player.isShiftKeyDown()) return;
            if (!rule.check().test(target)) return;

            // swallow the vanilla interaction (riding the camel etc.) while holding a bottle
            e.setCancellationResult(InteractionResult.CONSUME);
            e.setCanceled(true);

            if (player.getCooldowns().isOnCooldown(held.getItem())) return;
            player.getCooldowns().addCooldown(held.getItem(), rule.cooldown());
            if (level.random.nextFloat() >= rule.chance()) return;

            if (!player.getAbilities().instabuild) held.shrink(1);
            give(player, stack(rule.result()));
            if (rule.damage() > 0f) target.hurt(level.damageSources().generic(), rule.damage());
            level.playSound(null, target.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0f, 1.0f);
            e.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }
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


    private static void addTrade(VillagerTradesEvent e, int level, String name, int emeralds, int maxUses, int xp) {
        ItemStack s = stack(name);
        if (s.isEmpty()) return;
        e.getTrades().get(level).add(new BasicItemListing(emeralds, s, maxUses, xp, 0.05f));
    }

    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent e) {
        if (e.getType() == VillagerProfession.FARMER) {
            addTrade(e, 5, "red_wine", 24, 2, 30);
        } else if (e.getType() == VillagerProfession.CLERIC) {
            addTrade(e, 5, "spring_of_the_elves_marrow_crystal", 40, 2, 30);
        } else if (e.getType() == VillagerProfession.LIBRARIAN) {
            addTrade(e, 3, "diary_pages_over_22_years_old", 20, 4, 15);
        }
    }

    @SubscribeEvent
    public static void onWandererTrades(WandererTradesEvent e) {
        ItemStack horn = stack("flying_unicorn_horn");
        if (horn.isEmpty()) return;
        e.getRareTrades().add(new BasicItemListing(24, horn, 1, 0, 0.05f));
    }
}
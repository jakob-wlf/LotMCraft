package de.jakob.lotm.acting;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.SleepInBed;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.PlayerBrewedPotionEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ActingEventHandler {

    private static void fire(Player player, String id) {
        ActingHandler.onActingEvent(player, id);

        float hp = player.getHealth() / player.getMaxHealth();
        String healthSuffix = hp >= 1.0f ? "_while_full_health"
                : hp < 0.25f ? "_while_low_health"
                : hp < 0.5f  ? "_while_hurt"
                : null;

        if (healthSuffix != null) {
            ActingHandler.onActingEvent(player, id + healthSuffix);
            if (player.level().isNight())
                ActingHandler.onActingEvent(player, id + healthSuffix + "_at_night");
        }

        if (player.level().isNight())
            ActingHandler.onActingEvent(player, id + "_at_night");
    }

    private static final Map<Item, String> ITEM_USE_EVENTS = Map.of(
            Items.BONE_MEAL,       "use_bonemeal",
            Items.FIREWORK_ROCKET, "use_firework",
            Items.SPYGLASS,        "use_spyglass",
            Items.CLOCK,           "use_clock",
            Items.COMPASS,         "use_compass",
            Items.FLINT_AND_STEEL, "set_fire"
    );

    private static final Map<Block, String> BLOCK_INTERACT_EVENTS = Map.ofEntries(
            Map.entry(Blocks.ENDER_CHEST,        "open_ender_chest"),
            Map.entry(Blocks.CHEST,              "open_chest"),
            Map.entry(Blocks.TRAPPED_CHEST,      "open_chest"),
            Map.entry(Blocks.NOTE_BLOCK,         "use_note_block"),
            Map.entry(Blocks.LECTERN,            "use_lectern"),
            Map.entry(Blocks.ENCHANTING_TABLE,   "use_enchanting_table"),
            Map.entry(Blocks.CARTOGRAPHY_TABLE,  "use_cartography_table"),
            Map.entry(Blocks.CHISELED_BOOKSHELF, "open_chiseled_bookshelf"),
            Map.entry(Blocks.ANVIL,              "use_anvil"),
            Map.entry(Blocks.CHIPPED_ANVIL,      "use_anvil"),
            Map.entry(Blocks.DAMAGED_ANVIL,      "use_anvil"),
            Map.entry(Blocks.GRINDSTONE,         "use_grindstone"),
            Map.entry(Blocks.LOOM,               "use_loom"),
            Map.entry(Blocks.STONECUTTER,        "use_stonecutter"),
            Map.entry(Blocks.SMITHING_TABLE,     "use_smithing_table"),
            Map.entry(Blocks.FLETCHING_TABLE,    "use_fletching_table"),
            Map.entry(Blocks.JUKEBOX,            "use_jukebox"),
            Map.entry(Blocks.BELL,               "use_bell"),
            Map.entry(Blocks.BOOKSHELF,          "interact_bookshelf"),
            Map.entry(Blocks.CAULDRON,           "use_cauldron"),
            Map.entry(Blocks.WATER_CAULDRON,     "use_cauldron"),
            Map.entry(Blocks.LAVA_CAULDRON,      "use_lava_cauldron"),
            Map.entry(Blocks.BREWING_STAND,      "use_brewing_stand"),
            Map.entry(Blocks.BEACON,             "use_beacon"),
            Map.entry(Blocks.CONDUIT,            "interact_conduit"),
            Map.entry(Blocks.COMPOSTER,          "use_composter"),
            Map.entry(Blocks.RESPAWN_ANCHOR,     "use_respawn_anchor"),
            Map.entry(Blocks.LODESTONE,          "use_lodestone"),
            Map.entry(Blocks.OBSERVER,           "place_observer"),
            Map.entry(Blocks.DAYLIGHT_DETECTOR,  "use_daylight_detector"),
            Map.entry(Blocks.SUSPICIOUS_SAND,    "brush_suspicious_block"),
            Map.entry(Blocks.SUSPICIOUS_GRAVEL,  "brush_suspicious_block"),
            Map.entry(Blocks.DECORATED_POT,      "interact_decorated_pot"),
            Map.entry(Blocks.FLOWER_POT,         "interact_flower_pot"),
            Map.entry(Blocks.CAKE,               "eat_cake"),
            Map.entry(Blocks.CANDLE_CAKE,        "eat_cake"),
            Map.entry(Blocks.CRAFTING_TABLE,     "use_crafting_table")
    );

    private static final Set<Block> SAPLINGS = Set.of(
            Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.BIRCH_SAPLING,
            Blocks.JUNGLE_SAPLING, Blocks.ACACIA_SAPLING, Blocks.DARK_OAK_SAPLING,
            Blocks.CHERRY_SAPLING, Blocks.MANGROVE_PROPAGULE
    );

    private static final Set<Block> MOB_HEADS = Set.of(
            Blocks.SKELETON_SKULL, Blocks.WITHER_SKELETON_SKULL, Blocks.ZOMBIE_HEAD,
            Blocks.CREEPER_HEAD, Blocks.DRAGON_HEAD, Blocks.PIGLIN_HEAD, Blocks.PLAYER_HEAD
    );

    private static final Set<Block> FLOWERS = Set.of(
            Blocks.DANDELION, Blocks.POPPY, Blocks.BLUE_ORCHID, Blocks.ALLIUM,
            Blocks.AZURE_BLUET, Blocks.RED_TULIP, Blocks.ORANGE_TULIP,
            Blocks.WHITE_TULIP, Blocks.PINK_TULIP, Blocks.OXEYE_DAISY,
            Blocks.CORNFLOWER, Blocks.LILY_OF_THE_VALLEY, Blocks.WITHER_ROSE,
            Blocks.SUNFLOWER, Blocks.LILAC, Blocks.ROSE_BUSH, Blocks.PEONY
    );

    private static final Set<Block> STONE_BRICK_TYPES = Set.of(
            Blocks.STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS,
            Blocks.CHISELED_STONE_BRICKS
    );

    private static final Set<Block> CANDLES = Set.of(
            Blocks.CANDLE, Blocks.WHITE_CANDLE, Blocks.ORANGE_CANDLE, Blocks.MAGENTA_CANDLE,
            Blocks.LIGHT_BLUE_CANDLE, Blocks.YELLOW_CANDLE, Blocks.LIME_CANDLE,
            Blocks.PINK_CANDLE, Blocks.GRAY_CANDLE, Blocks.LIGHT_GRAY_CANDLE,
            Blocks.CYAN_CANDLE, Blocks.PURPLE_CANDLE, Blocks.BLUE_CANDLE,
            Blocks.BROWN_CANDLE, Blocks.GREEN_CANDLE, Blocks.RED_CANDLE, Blocks.BLACK_CANDLE
    );

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack item = event.getItemStack();
        BlockState clickedBlock = event.getLevel().getBlockState(event.getPos());
        Block block = clickedBlock.getBlock();

        String blockEvent = BLOCK_INTERACT_EVENTS.get(block);
        if (blockEvent != null) fire(player, blockEvent);

        String itemEvent = ITEM_USE_EVENTS.get(item.getItem());
        if (itemEvent != null) fire(player, itemEvent);

        if (item.getItem() instanceof BlockItem blockItem) {
            Block b = blockItem.getBlock();

            if (b == Blocks.TNT)                                          fire(player, "place_tnt");
            if (clickedBlock.is(Blocks.FARMLAND) && b instanceof CropBlock) fire(player, "plant_crop");
            if (SAPLINGS.contains(b))                                     fire(player, "plant_sapling");
            if (MOB_HEADS.contains(b))                                    fire(player, "place_mob_head");
            if (b == Blocks.SOUL_TORCH || b == Blocks.SOUL_WALL_TORCH)   fire(player, "place_soul_torch");
            if (b == Blocks.SOUL_LANTERN)                                 fire(player, "place_soul_lantern");
            if (b instanceof LadderBlock || b instanceof ScaffoldingBlock || b == Blocks.VINE)
                fire(player, "use_environment");

            if (CANDLES.contains(b))
                fire(player, "place_candle");

            if (FLOWERS.contains(b))
                fire(player, "place_flower");

            if (b == Blocks.OAK_SIGN || b == Blocks.BIRCH_SIGN || b == Blocks.SPRUCE_SIGN
                    || b == Blocks.JUNGLE_SIGN || b == Blocks.ACACIA_SIGN || b == Blocks.DARK_OAK_SIGN
                    || b == Blocks.MANGROVE_SIGN || b == Blocks.BAMBOO_SIGN || b == Blocks.CHERRY_SIGN)
                fire(player, "place_sign");

            if (b == Blocks.WHITE_BANNER || b == Blocks.ORANGE_BANNER || b == Blocks.MAGENTA_BANNER
                    || b == Blocks.LIGHT_BLUE_BANNER || b == Blocks.YELLOW_BANNER || b == Blocks.LIME_BANNER
                    || b == Blocks.PINK_BANNER || b == Blocks.GRAY_BANNER || b == Blocks.LIGHT_GRAY_BANNER
                    || b == Blocks.CYAN_BANNER || b == Blocks.PURPLE_BANNER || b == Blocks.BLUE_BANNER
                    || b == Blocks.BROWN_BANNER || b == Blocks.GREEN_BANNER || b == Blocks.RED_BANNER
                    || b == Blocks.BLACK_BANNER)
                fire(player, "place_banner");

            if (b == Blocks.CHISELED_STONE_BRICKS || b == Blocks.CHISELED_NETHER_BRICKS
                    || b == Blocks.CHISELED_POLISHED_BLACKSTONE)
                fire(player, "place_chiseled_block");

            if ((b == Blocks.TORCH || b == Blocks.WALL_TORCH) && player.level().isNight())
                fire(player, "place_torch_at_night");
        }

        if (item.is(Items.BRUSH) && (block == Blocks.SUSPICIOUS_SAND || block == Blocks.SUSPICIOUS_GRAVEL))
            fire(player, "brush_suspicious_block");

        if (item.is(Items.WRITABLE_BOOK) || item.is(Items.WRITTEN_BOOK))
            fire(player, "interact_with_book");

        if (item.is(Items.MAP) || item.is(Items.FILLED_MAP))
            fire(player, "use_map");
    }

    @SubscribeEvent
    public static void onAbilityUse(AbilityUsedEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getAbility() == null) return;
        fire(player, "use_" + event.getAbility().getId());
    }

    @SubscribeEvent
    public static void onMount(EntityMountEvent event) {
        if (!(event.getEntityMounting() instanceof Player player)) return;

        if (event.getEntityBeingMounted() instanceof Boat)             ActingHandler.onActingEvent(player, "ride_boat");
        if (event.getEntityBeingMounted() instanceof AbstractMinecart) ActingHandler.onActingEvent(player, "ride_minecart");
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        ItemStack item = event.getCrafting();

        if (item.is(Items.MAP)) ActingHandler.onActingEvent(player, "craft_map");

        if (item.getItem() instanceof TieredItem tiered) {
            Tier tier = tiered.getTier();
            if (tier == Tiers.IRON)    ActingHandler.onActingEvent(player, "craft_iron_tool");
            if (tier == Tiers.GOLD)    ActingHandler.onActingEvent(player, "craft_golden_tool");
            if (tier == Tiers.DIAMOND) ActingHandler.onActingEvent(player, "craft_diamond_tool");
            if (tier == Tiers.NETHERITE) ActingHandler.onActingEvent(player, "craft_netherite_tool");
        }

        if (item.is(Items.COMPASS))       ActingHandler.onActingEvent(player, "craft_compass");
        if (item.is(Items.CLOCK))         ActingHandler.onActingEvent(player, "craft_clock");
        if (item.is(Items.BOOK))          ActingHandler.onActingEvent(player, "craft_book");
        if (item.is(Items.BOOKSHELF))     ActingHandler.onActingEvent(player, "craft_bookshelf");
        if (item.is(Items.SPYGLASS))      ActingHandler.onActingEvent(player, "craft_spyglass");
        if (item.is(Items.PAPER))         ActingHandler.onActingEvent(player, "craft_paper");
        if (item.is(Items.LANTERN) || item.is(Items.SOUL_LANTERN))
            ActingHandler.onActingEvent(player, "craft_lantern");
        if (item.is(Items.LEAD))          ActingHandler.onActingEvent(player, "craft_lead");
        if (item.is(Items.FISHING_ROD))   ActingHandler.onActingEvent(player, "craft_fishing_rod");
        if (item.is(Items.BOW))           ActingHandler.onActingEvent(player, "craft_bow");
        if (item.is(Items.CROSSBOW))      ActingHandler.onActingEvent(player, "craft_crossbow");
        if (item.is(Items.SHIELD))        ActingHandler.onActingEvent(player, "craft_shield");
        if (item.is(Items.SADDLE))        ActingHandler.onActingEvent(player, "craft_saddle");
        if (item.is(Items.NAME_TAG))      ActingHandler.onActingEvent(player, "craft_name_tag");
        if (item.is(Items.FIRE_CHARGE))   ActingHandler.onActingEvent(player, "craft_fire_charge");
        if (item.is(Items.FLINT_AND_STEEL)) ActingHandler.onActingEvent(player, "craft_flint_and_steel");
        if (item.is(Items.TNT))           ActingHandler.onActingEvent(player, "craft_tnt");
        if (item.is(Items.BONE_MEAL))     ActingHandler.onActingEvent(player, "craft_bonemeal");
        if (item.is(Items.ENDER_EYE))     ActingHandler.onActingEvent(player, "craft_ender_eye");
        if (item.is(Items.ENDER_CHEST))   ActingHandler.onActingEvent(player, "craft_ender_chest");

        if (item.getItem() instanceof ArmorItem armor) {
            if (armor.getMaterial() == ArmorMaterials.IRON)
                ActingHandler.onActingEvent(player, "craft_iron_armor");
            if (armor.getMaterial() == ArmorMaterials.DIAMOND)
                ActingHandler.onActingEvent(player, "craft_diamond_armor");
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();
        if (level.isClientSide()) return;

        if (level.isRaining() && level.canSeeSky(player.blockPosition()))
            ActingHandler.onActingEvent(player, "stand_in_rain");

        BlockPos pos = player.blockPosition();
        boolean nearLava = false;
        for (BlockPos neighbor : List.of(pos.north(), pos.south(), pos.east(), pos.west(), pos.below())) {
            if (level.getBlockState(neighbor).is(Blocks.LAVA)) { nearLava = true; break; }
        }
        if (nearLava) ActingHandler.onActingEvent(player, "stand_near_lava");

        if (player.isUnderWater()) ActingHandler.onActingEvent(player, "swim_underwater");

        if (player.isSprinting() && !player.isSwimming() && !player.isUnderWater())
            ActingHandler.onActingEvent(player, "sprint");

        if (player.isCrouching())
            ActingHandler.onActingEvent(player, "crouch");

        if (player.isOnFire())
            ActingHandler.onActingEvent(player, "player_on_fire");

        if (player.getY() > 200)
            ActingHandler.onActingEvent(player, "stand_at_high_altitude");

        if (player.getY() < 10)
            ActingHandler.onActingEvent(player, "stand_at_bedrock_level");

        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = level.getBrightness(LightLayer.SKY, pos);
        if (blockLight == 0 && skyLight == 0)
            ActingHandler.onActingEvent(player, "stand_in_complete_darkness");

        long dayTime = level.getDayTime() % 24000;
        if (dayTime >= 23800 || dayTime <= 200)
            ActingHandler.onActingEvent(player, "witness_dawn");

        if (player.getHealth() >= player.getMaxHealth())
            ActingHandler.onActingEvent(player, "maintain_full_health");
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        ItemStack item = event.getItemStack();

        if (item.is(Items.NAME_TAG))  ActingHandler.onActingEvent(player, "use_name_tag");
        if (item.is(Items.LEAD))      ActingHandler.onActingEvent(player, "use_lead");

        if (event.getTarget() instanceof Villager)
            ActingHandler.onActingEvent(player, "interact_with_villager");

        if (!(event.getTarget() instanceof Animal animal)) return;

        if (animal.isFood(item)) {
            ActingHandler.onActingEvent(player, "feed_animal");
            if (animal.getAge() == 0 && !animal.isInLove())
                ActingHandler.onActingEvent(player, "breed_animals");
        }
    }

    @SubscribeEvent
    public static void onPlayerTradeWithVillager(TradeWithVillagerEvent event) {
        ActingHandler.onActingEvent(event.getEntity(), "trade_with_villager");
    }

    @SubscribeEvent
    public static void onAnimalTame(AnimalTameEvent event) {
        ActingHandler.onActingEvent(event.getTamer(), "tame_animal");
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack item = event.getItemStack();
        String itemEvent = ITEM_USE_EVENTS.get(item.getItem());
        if (itemEvent != null) fire(player, itemEvent);

        if (item.is(Items.WRITTEN_BOOK))
            fire(player, "read_written_book");
    }

    @SubscribeEvent
    public static void onEnderPearlTeleport(EntityTeleportEvent.EnderPearl event) {
        if (event.getEntity() instanceof Player player)
            ActingHandler.onActingEvent(player, "use_ender_pearl");
    }

    @SubscribeEvent
    public static void onPlayerBrewedPotion(PlayerBrewedPotionEvent event) {
        ActingHandler.onActingEvent(event.getEntity(), "brew_potion");
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        BlockState state = event.getState();
        Block block = state.getBlock();

        if (state.getBlock() instanceof CropBlock) fire(player, "harvest_crop");
        if (state.is(Blocks.BARREL))               fire(player, "break_barrel");
        if (state.is(Blocks.SWEET_BERRY_BUSH))     fire(player, "harvest_sweet_berries");

        if (state.is(BlockTags.COAL_ORES) || state.is(BlockTags.IRON_ORES)    || state.is(BlockTags.COPPER_ORES)
                || state.is(BlockTags.GOLD_ORES) || state.is(BlockTags.REDSTONE_ORES) || state.is(BlockTags.EMERALD_ORES)
                || state.is(BlockTags.LAPIS_ORES)|| state.is(BlockTags.DIAMOND_ORES))
            fire(player, "mine_ore");

        if (state.is(Blocks.ANCIENT_DEBRIS))
            fire(player, "mine_ancient_debris");

        if (state.is(BlockTags.LOGS))
            fire(player, "chop_wood");

        if (state.is(Blocks.ICE) || state.is(Blocks.PACKED_ICE) || state.is(Blocks.BLUE_ICE))
            fire(player, "break_ice");

        if (FLOWERS.contains(block))
            fire(player, "harvest_flower");

        if (state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST))
            fire(player, "break_chest");

        if (state.is(Blocks.NETHER_QUARTZ_ORE))
            fire(player, "mine_nether_quartz");

        if (state.is(Blocks.SPAWNER) || state.is(Blocks.TRIAL_SPAWNER))
            fire(player, "destroy_spawner");
    }

    @SubscribeEvent
    public static void onFish(ItemFishedEvent event) {
        ActingHandler.onActingEvent(event.getEntity(), "catch_fish");

        for (ItemStack drop : event.getDrops()) {
            if (drop.is(Items.ENCHANTED_BOOK) || drop.is(Items.BOW) || drop.is(Items.FISHING_ROD)
                    || drop.is(Items.NAME_TAG) || drop.is(Items.SADDLE) || drop.is(Items.NAUTILUS_SHELL))
                ActingHandler.onActingEvent(event.getEntity(), "fish_up_treasure");
        }
    }

    @SubscribeEvent
    public static void onSmelt(PlayerEvent.ItemSmeltedEvent event) {
        ActingHandler.onActingEvent(event.getEntity(), "smelt_item");
    }

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Post event) {
        ActingHandler.onActingEvent(event.getPlayer(), "pickup_item");

        if (event.getItemEntity().getItem().is(Items.GOLD_INGOT)
                || event.getItemEntity().getItem().is(Items.GOLD_NUGGET)
                || event.getItemEntity().getItem().is(Items.GOLDEN_APPLE))
            ActingHandler.onActingEvent(event.getPlayer(), "pickup_gold");

        if (event.getItemEntity().getItem().is(Items.BONE)
                || event.getItemEntity().getItem().is(Items.BONE_MEAL))
            ActingHandler.onActingEvent(event.getPlayer(), "pickup_bone");

        if (event.getItemEntity().getItem().is(Items.ROTTEN_FLESH))
            ActingHandler.onActingEvent(event.getPlayer(), "pickup_rotten_flesh");
    }

    @SubscribeEvent
    public static void onMobKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        if (event.getEntity().getMaxHealth() >= player.getMaxHealth()) fire(player, "kill_strong_mobs");
        if (event.getEntity().isOnFire())                              fire(player, "kill_burning_mob");

        if (event.getEntity() instanceof Mob mob) {
            if (mob.getTarget() == null || !mob.getTarget().getUUID().equals(player.getUUID()))
                fire(player, "kill_untargeted_mob");

            List<Mob> targeting = player.level().getEntitiesOfClass(Mob.class,
                    player.getBoundingBox().inflate(24),
                    m -> m != mob && m.getTarget() != null && m.getTarget().getUUID().equals(player.getUUID()));
            if (targeting.size() >= 2) fire(player, "outnumbered_kill");
        }

        if (player.isCrouching()) fire(player, "sneak_kill");

        if (event.getEntity() instanceof Mob) {
            int light = player.level().getBrightness(LightLayer.BLOCK, player.blockPosition());
            if (light <= 2) fire(player, "kill_in_darkness");
        }

        if (event.getSource().getDirectEntity() instanceof AbstractArrow arrow
                && arrow.getOwner() instanceof Player p && p.equals(player))
            fire(player, "kill_with_bow");

        if (player.getMainHandItem().isEmpty())
            fire(player, "kill_unarmed");

        if (event.getEntity().getType().is(net.minecraft.tags.EntityTypeTags.UNDEAD))
            fire(player, "kill_undead");

        if (event.getEntity() instanceof net.minecraft.world.entity.boss.wither.WitherBoss
                || event.getEntity() instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon)
            fire(player, "kill_boss");

        if (player.getHealth() >= player.getMaxHealth())
            fire(player, "kill_while_full_health");

        if (player.getY() > 100)
            fire(player, "kill_at_high_altitude");

        if (player.level().isRaining())
            fire(player, "kill_in_rain");

        if (event.getEntity().isInWater())
            fire(player, "kill_in_water");
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof ThrownPotion potion)) return;
        if (!(potion.getOwner() instanceof Player player)) return;
        if (!(event.getRayTraceResult() instanceof EntityHitResult hit)) return;
        if (!(hit.getEntity() instanceof Mob)) return;

        Item item = potion.getItem().getItem();
        if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem)
            fire(player, "throw_splash_potion");
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getTo().equals(Level.NETHER))
            ActingHandler.onActingEvent(event.getEntity(), "enter_nether");
        if (event.getTo().equals(Level.END))
            ActingHandler.onActingEvent(event.getEntity(), "enter_end");
        if (event.getFrom().equals(Level.NETHER) || event.getFrom().equals(Level.END))
            ActingHandler.onActingEvent(event.getEntity(), "return_from_dimension");
    }

    @SubscribeEvent
    public static void onItemFinishedUsing(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack item = event.getItem();

        if (item.is(Items.GOLDEN_APPLE) || item.is(Items.ENCHANTED_GOLDEN_APPLE))
            ActingHandler.onActingEvent(player, "eat_golden_apple");
        if (item.is(Items.SUSPICIOUS_STEW))
            ActingHandler.onActingEvent(player, "eat_suspicious_stew");

        if (item.getItem() instanceof PotionItem) {
            fire(player, "drink_potion");

            PotionContents contents = item.get(DataComponents.POTION_CONTENTS);
            if (contents != null) {
                if (contents.is(Potions.INVISIBILITY))  ActingHandler.onActingEvent(player, "drink_invisibility_potion");
                if (contents.is(Potions.STRENGTH))      ActingHandler.onActingEvent(player, "drink_strength_potion");
                if (contents.is(Potions.HEALING))       ActingHandler.onActingEvent(player, "drink_healing_potion");
                if (contents.is(Potions.SWIFTNESS))     ActingHandler.onActingEvent(player, "drink_swiftness_potion");
                if (contents.is(Potions.NIGHT_VISION))  ActingHandler.onActingEvent(player, "drink_night_vision_potion");
                if (contents.is(Potions.POISON))        ActingHandler.onActingEvent(player, "drink_poison_potion");
                if (contents.is(Potions.SLOW_FALLING))  ActingHandler.onActingEvent(player, "drink_slow_falling_potion");
                if (contents.is(Potions.WATER_BREATHING)) ActingHandler.onActingEvent(player, "drink_water_breathing_potion");
            }
        }

        if (item.is(Items.CHORUS_FRUIT))
            ActingHandler.onActingEvent(player, "eat_chorus_fruit");

        if (item.is(Items.ROTTEN_FLESH))
            ActingHandler.onActingEvent(player, "eat_rotten_flesh");

        if (item.is(Items.PUFFERFISH))
            ActingHandler.onActingEvent(player, "eat_pufferfish");
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!(event.getTarget() instanceof Mob mob)) return;
        if (mob.getMaxHealth() >= player.getMaxHealth())
            fire(player, "attack_stronger_mob");
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        fire(player, "take_damage");

        float hp = player.getHealth() / player.getMaxHealth();
        if (hp < 0.25f)
            fire(player, "survive_while_critical");

        if (event.getSource().equals(player.level().damageSources().onFire())
                || event.getSource().equals(player.level().damageSources().inFire())
                || event.getSource().equals(player.level().damageSources().lava()))
            fire(player, "take_fire_damage");

        List<Mob> targeting = player.level().getEntitiesOfClass(Mob.class,
                player.getBoundingBox().inflate(16),
                m -> m.getTarget() != null && m.getTarget().getUUID().equals(player.getUUID()));
        if (targeting.size() >= 3)
            fire(player, "take_damage_outnumbered");
    }

    @SubscribeEvent
    public static void onExperiencePickup(PlayerXpEvent.LevelChange event) {
        if (event.getLevels() > 0)
            ActingHandler.onActingEvent(event.getEntity(), "gain_xp_level");
    }
}
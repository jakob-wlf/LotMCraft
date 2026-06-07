package de.jakob.lotm.acting;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
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
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ActingEventHandler {

    private static final Map<UUID, Integer> nightWatchTicks = new HashMap<>();
    private static final Map<UUID, Integer> darknessTicks = new HashMap<>();
    private static final Map<UUID, Integer> sunlightTicks = new HashMap<>();
    private static final Map<UUID, Integer> rainTicks = new HashMap<>();
    private static final Map<UUID, Integer> underwaterTicks = new HashMap<>();
    private static final Map<UUID, Integer> highAltitudeTicks = new HashMap<>();
    private static final Map<UUID, Long> lastNightAwakeReward = new HashMap<>();
    private static final Map<UUID, Long> lastDarknessTick = new HashMap<>();
    private static final Map<UUID, Long> lastSunlightTick = new HashMap<>();
    private static final Map<UUID, Long> lastRainTick = new HashMap<>();
    private static final Map<UUID, Long> lastUnderwaterTick = new HashMap<>();
    private static final Map<UUID, Long> lastHighAltitudeTick = new HashMap<>();

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
            Map.entry(Blocks.LOOM,               "use_loom"),
            Map.entry(Blocks.GRINDSTONE,         "use_grindstone"),
            Map.entry(Blocks.SMITHING_TABLE,     "use_smithing_table"),
            Map.entry(Blocks.STONECUTTER,        "use_stonecutter"),
            Map.entry(Blocks.CAMPFIRE,           "use_campfire"),
            Map.entry(Blocks.SOUL_CAMPFIRE,      "use_soul_campfire"),
            Map.entry(Blocks.BEEHIVE,            "collect_honey"),
            Map.entry(Blocks.BEE_NEST,           "collect_honey"),
            Map.entry(Blocks.BARREL,             "open_barrel"),
            Map.entry(Blocks.BOOKSHELF,          "interact_bookshelf"),
            Map.entry(Blocks.FLETCHING_TABLE,    "use_fletching_table"),
            Map.entry(Blocks.COMPOSTER,          "use_composter"),
            Map.entry(Blocks.CAULDRON,           "use_cauldron"),
            Map.entry(Blocks.WATER_CAULDRON,     "use_cauldron"),
            Map.entry(Blocks.LAVA_CAULDRON,      "use_lava_cauldron"),
            Map.entry(Blocks.RESPAWN_ANCHOR,     "use_respawn_anchor"),
            Map.entry(Blocks.LODESTONE,          "use_lodestone")
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
            Blocks.AZURE_BLUET, Blocks.RED_TULIP, Blocks.ORANGE_TULIP, Blocks.WHITE_TULIP,
            Blocks.PINK_TULIP, Blocks.OXEYE_DAISY, Blocks.CORNFLOWER, Blocks.LILY_OF_THE_VALLEY,
            Blocks.WITHER_ROSE, Blocks.SUNFLOWER, Blocks.LILAC, Blocks.ROSE_BUSH, Blocks.PEONY,
            Blocks.PINK_PETALS, Blocks.TORCHFLOWER, Blocks.CHERRY_LEAVES
    );

    private static final Set<Block> CAMPFIRES = Set.of(Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE);

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack item = event.getItemStack();
        BlockState clickedBlock = event.getLevel().getBlockState(event.getPos());

        String blockEvent = BLOCK_INTERACT_EVENTS.get(clickedBlock.getBlock());
        if (blockEvent != null) fire(player, blockEvent);

        String itemEvent = ITEM_USE_EVENTS.get(item.getItem());
        if (itemEvent != null) fire(player, itemEvent);

        if (item.getItem() instanceof BlockItem blockItem) {
            Block b = blockItem.getBlock();

            if (b == Blocks.TNT)                                            fire(player, "place_tnt");
            if (clickedBlock.is(Blocks.FARMLAND) && b instanceof CropBlock) fire(player, "plant_crop");
            if (SAPLINGS.contains(b))                                       fire(player, "plant_sapling");
            if (MOB_HEADS.contains(b))                                      fire(player, "place_mob_head");
            if (b == Blocks.SOUL_TORCH || b == Blocks.SOUL_WALL_TORCH)     fire(player, "place_soul_torch");
            if (b == Blocks.SOUL_LANTERN)                                   fire(player, "place_soul_lantern");
            if (b instanceof LadderBlock || b instanceof ScaffoldingBlock || b == Blocks.VINE)
                fire(player, "use_environment");
            if (b == Blocks.TORCH || b == Blocks.WALL_TORCH)               fire(player, "place_torch");
            if (FLOWERS.contains(b))                                        fire(player, "place_flower");
            if (b == Blocks.FLOWER_POT)                                     fire(player, "place_flower_pot");
            if (CAMPFIRES.contains(b))                                      fire(player, "place_campfire");
            if (b == Blocks.CANDLE || b.getDescriptionId().contains("candle"))
                fire(player, "place_candle");
        }

        if (item.is(Items.WRITTEN_BOOK))   fire(player, "read_written_book");
        if (item.is(Items.WRITABLE_BOOK))  fire(player, "write_in_book");
        if (item.is(Items.TRIDENT))        fire(player, "use_trident");
        if (item.is(Items.SHIELD))         fire(player, "use_shield");
        if (item.is(Items.GLASS_BOTTLE) && clickedBlock.is(Blocks.WATER_CAULDRON))
            fire(player, "fill_bottle_from_cauldron");
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

        if (item.is(Items.MAP))             ActingHandler.onActingEvent(player, "craft_map");
        if (item.is(Items.PAPER))           ActingHandler.onActingEvent(player, "craft_paper");
        if (item.is(Items.BOOK))            ActingHandler.onActingEvent(player, "craft_book");
        if (item.is(Items.WRITABLE_BOOK))   ActingHandler.onActingEvent(player, "craft_book");
        if (item.is(Items.COMPASS))         ActingHandler.onActingEvent(player, "craft_compass");
        if (item.is(Items.CLOCK))           ActingHandler.onActingEvent(player, "craft_clock");
        if (item.is(Items.TORCH))           ActingHandler.onActingEvent(player, "craft_torch");
        if (item.is(Items.SOUL_TORCH))      ActingHandler.onActingEvent(player, "craft_soul_torch");
        if (item.is(Items.LANTERN))         ActingHandler.onActingEvent(player, "craft_lantern");
        if (item.is(Items.SOUL_LANTERN))    ActingHandler.onActingEvent(player, "craft_soul_lantern");
        if (item.is(Items.ENDER_EYE))       ActingHandler.onActingEvent(player, "craft_ender_eye");
        if (item.is(Items.LEAD))            ActingHandler.onActingEvent(player, "craft_lead");
        if (item.is(Items.PAINTING))        ActingHandler.onActingEvent(player, "craft_painting");

        if (item.getItem() instanceof TieredItem tiered) {
            Tier tier = tiered.getTier();
            if (tier == Tiers.IRON)    ActingHandler.onActingEvent(player, "craft_iron_tool");
            if (tier == Tiers.GOLD)    ActingHandler.onActingEvent(player, "craft_golden_tool");
            if (tier == Tiers.DIAMOND) ActingHandler.onActingEvent(player, "craft_diamond_tool");
            if (tier == Tiers.NETHERITE) ActingHandler.onActingEvent(player, "craft_netherite_tool");
        }

        if (item.getItem() instanceof ArmorItem armor) {
            if (armor.getMaterial() == ArmorMaterials.IRON)      ActingHandler.onActingEvent(player, "craft_iron_armor");
            if (armor.getMaterial() == ArmorMaterials.GOLD)      ActingHandler.onActingEvent(player, "craft_golden_armor");
            if (armor.getMaterial() == ArmorMaterials.DIAMOND)   ActingHandler.onActingEvent(player, "craft_diamond_armor");
            if (armor.getMaterial() == ArmorMaterials.NETHERITE) ActingHandler.onActingEvent(player, "craft_netherite_armor");
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();
        if (level.isClientSide()) return;

        UUID id = player.getUUID();
        long gameTime = level.getGameTime();

        if (level.isNight() && !player.isSleeping()) {
            nightWatchTicks.merge(id, 1, Integer::sum);
            if (nightWatchTicks.get(id) >= 20 * 60) {
                long last = lastNightAwakeReward.getOrDefault(id, -99999L);
                if (gameTime - last >= 20 * 60) {
                    ActingHandler.onActingEvent(player, "stay_awake_through_night");
                    lastNightAwakeReward.put(id, gameTime);
                }
                nightWatchTicks.put(id, 0);
            }
        } else {
            nightWatchTicks.put(id, 0);
        }

        int blockLight = level.getBrightness(LightLayer.BLOCK, player.blockPosition());
        int skyLight   = level.getBrightness(LightLayer.SKY,   player.blockPosition());
        boolean inDarkness = blockLight <= 2 && skyLight <= 2;

        if (inDarkness) {
            darknessTicks.merge(id, 1, Integer::sum);
            if (darknessTicks.get(id) >= 20 * 30) {
                long last = lastDarknessTick.getOrDefault(id, -99999L);
                if (gameTime - last >= 20 * 30) {
                    ActingHandler.onActingEvent(player, "linger_in_darkness");
                    lastDarknessTick.put(id, gameTime);
                }
                darknessTicks.put(id, 0);
            }
        } else {
            darknessTicks.put(id, 0);
        }

        boolean hasSkyAccess = level.canSeeSky(player.blockPosition());
        boolean isDay = !level.isNight();
        boolean inSunlight = hasSkyAccess && isDay && !level.isRaining() && skyLight >= 15;

        if (inSunlight) {
            sunlightTicks.merge(id, 1, Integer::sum);
            if (sunlightTicks.get(id) >= 20 * 30) {
                long last = lastSunlightTick.getOrDefault(id, -99999L);
                if (gameTime - last >= 20 * 30) {
                    ActingHandler.onActingEvent(player, "stand_in_sunlight");
                    lastSunlightTick.put(id, gameTime);
                }
                sunlightTicks.put(id, 0);
            }
        } else {
            sunlightTicks.put(id, 0);
        }

        boolean inRain = level.isRaining() && hasSkyAccess;
        if (inRain) {
            rainTicks.merge(id, 1, Integer::sum);
            if (rainTicks.get(id) >= 20 * 20) {
                long last = lastRainTick.getOrDefault(id, -99999L);
                if (gameTime - last >= 20 * 20) {
                    ActingHandler.onActingEvent(player, "stand_in_rain");
                    lastRainTick.put(id, gameTime);
                }
                rainTicks.put(id, 0);
            }
        } else {
            rainTicks.put(id, 0);
        }

        boolean underwater = player.isUnderWater();
        if (underwater) {
            underwaterTicks.merge(id, 1, Integer::sum);
            if (underwaterTicks.get(id) >= 20 * 15) {
                long last = lastUnderwaterTick.getOrDefault(id, -99999L);
                if (gameTime - last >= 20 * 15) {
                    ActingHandler.onActingEvent(player, "submerge_in_water");
                    lastUnderwaterTick.put(id, gameTime);
                }
                underwaterTicks.put(id, 0);
            }
        } else {
            underwaterTicks.put(id, 0);
        }

        boolean atHeight = player.getY() >= 128 && hasSkyAccess;
        if (atHeight) {
            highAltitudeTicks.merge(id, 1, Integer::sum);
            if (highAltitudeTicks.get(id) >= 20 * 20) {
                long last = lastHighAltitudeTick.getOrDefault(id, -99999L);
                if (gameTime - last >= 20 * 20) {
                    ActingHandler.onActingEvent(player, "stand_at_height");
                    lastHighAltitudeTick.put(id, gameTime);
                }
                highAltitudeTicks.put(id, 0);
            }
        } else {
            highAltitudeTicks.put(id, 0);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        ItemStack item = event.getItemStack();

        if (item.is(Items.NAME_TAG)) ActingHandler.onActingEvent(player, "use_name_tag");
        if (item.is(Items.LEAD))     ActingHandler.onActingEvent(player, "use_lead");

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
        String itemEvent = ITEM_USE_EVENTS.get(event.getItemStack().getItem());
        if (itemEvent != null) fire(player, itemEvent);
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

        if (state.getBlock() instanceof CropBlock)      fire(player, "harvest_crop");
        if (state.is(Blocks.BARREL))                    fire(player, "break_barrel");
        if (state.is(Blocks.SWEET_BERRY_BUSH))          fire(player, "harvest_sweet_berries");
        if (FLOWERS.contains(state.getBlock()))         fire(player, "pick_flower");
        if (state.is(Blocks.SPAWNER))                   fire(player, "destroy_spawner");
        if (state.is(BlockTags.SIGNS) || state.is(BlockTags.WALL_SIGNS))
            fire(player, "break_sign");

        if (state.is(BlockTags.COAL_ORES) || state.is(BlockTags.IRON_ORES)    || state.is(BlockTags.COPPER_ORES)
                || state.is(BlockTags.GOLD_ORES) || state.is(BlockTags.REDSTONE_ORES) || state.is(BlockTags.EMERALD_ORES)
                || state.is(BlockTags.LAPIS_ORES)|| state.is(BlockTags.DIAMOND_ORES))
            fire(player, "mine_ore");

        if (state.is(BlockTags.GOLD_ORES))    fire(player, "mine_gold_ore");
        if (state.is(BlockTags.DIAMOND_ORES)) fire(player, "mine_diamond_ore");
        if (state.is(BlockTags.EMERALD_ORES)) fire(player, "mine_emerald_ore");
    }

    @SubscribeEvent
    public static void onFish(ItemFishedEvent event) {
        ActingHandler.onActingEvent(event.getEntity(), "catch_fish");
    }

    @SubscribeEvent
    public static void onSmelt(PlayerEvent.ItemSmeltedEvent event) {
        ActingHandler.onActingEvent(event.getEntity(), "smelt_item");
    }

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Post event) {
        Player player = event.getPlayer();
        Item item = event.getItemEntity().getItem().getItem();

        ActingHandler.onActingEvent(player, "pickup_item");

        if (item == Items.DIAMOND || item == Items.EMERALD || item == Items.GOLD_INGOT
                || item == Items.NETHERITE_INGOT || item == Items.ANCIENT_DEBRIS)
            ActingHandler.onActingEvent(player, "pickup_rare_item");

        if (item == Items.WRITTEN_BOOK || item == Items.WRITABLE_BOOK || item == Items.BOOK)
            ActingHandler.onActingEvent(player, "pickup_book");

        if (item == Items.ROTTEN_FLESH || item == Items.BONE || item == Items.SKULL_BANNER_PATTERN)
            ActingHandler.onActingEvent(player, "pickup_undead_drop");

        if (item == Items.NAUTILUS_SHELL || item == Items.HEART_OF_THE_SEA || item == Items.PRISMARINE_SHARD)
            ActingHandler.onActingEvent(player, "pickup_ocean_treasure");
    }

    @SubscribeEvent
    public static void onMobKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        if (event.getEntity().getMaxHealth() >= player.getMaxHealth()) fire(player, "kill_strong_mobs");
        if (event.getEntity().isOnFire())                              fire(player, "kill_burning_mob");
        if (player.isOnFire())                                         fire(player, "kill_while_burning");

        if (event.getEntity() instanceof Mob mob) {
            if (mob.getTarget() == null || !mob.getTarget().getUUID().equals(player.getUUID()))
                fire(player, "kill_untargeted_mob");

            List<Mob> targeting = player.level().getEntitiesOfClass(Mob.class,
                    player.getBoundingBox().inflate(24),
                    m -> m != mob && m.getTarget() != null && m.getTarget().getUUID().equals(player.getUUID()));
            if (targeting.size() >= 2) fire(player, "outnumbered_kill");

            if (mob instanceof Zombie || mob instanceof AbstractSkeleton)
                fire(player, "kill_undead");

            if (mob instanceof Monster && !(mob instanceof Zombie) && !(mob instanceof AbstractSkeleton))
                fire(player, "kill_monster");
        }

        if (event.getEntity() instanceof Animal) fire(player, "kill_passive_mob");

        if (player.isCrouching()) fire(player, "sneak_kill");

        if (event.getEntity() instanceof Mob) {
            int light = player.level().getBrightness(LightLayer.BLOCK, player.blockPosition());
            if (light <= 2) fire(player, "kill_in_darkness");
        }

        if (event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE))
            fire(player, "kill_with_ranged");

        double heightDiff = player.getY() - event.getEntity().getY();
        if (heightDiff >= 5) fire(player, "kill_from_above");

        Level level = player.level();
        if (level.isThundering()) fire(player, "kill_during_storm");
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
    public static void onArrowKill(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow arrow)) return;
        if (!(arrow.getOwner() instanceof Player player)) return;
        if (!(event.getRayTraceResult() instanceof EntityHitResult hit)) return;
        if (!(hit.getEntity() instanceof Mob)) return;
        ActingHandler.onActingEvent(player, "hit_with_arrow");
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getTo().equals(Level.NETHER))
            ActingHandler.onActingEvent(event.getEntity(), "enter_nether");
        if (event.getTo().equals(Level.END))
            ActingHandler.onActingEvent(event.getEntity(), "enter_end");
    }

    @SubscribeEvent
    public static void onItemFinishedUsing(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack item = event.getItem();

        if (item.is(Items.GOLDEN_APPLE) || item.is(Items.ENCHANTED_GOLDEN_APPLE))
            ActingHandler.onActingEvent(player, "eat_golden_apple");
        if (item.is(Items.SUSPICIOUS_STEW))
            ActingHandler.onActingEvent(player, "eat_suspicious_stew");
        if (item.is(Items.ROTTEN_FLESH))
            ActingHandler.onActingEvent(player, "eat_rotten_flesh");
        if (item.is(Items.MILK_BUCKET))
            ActingHandler.onActingEvent(player, "drink_milk");
        if (item.is(Items.HONEY_BOTTLE))
            ActingHandler.onActingEvent(player, "drink_honey");
        if (item.is(Items.MUSHROOM_STEW) || item.is(Items.RABBIT_STEW) || item.is(Items.BEETROOT_SOUP))
            ActingHandler.onActingEvent(player, "eat_stew");

        if (item.getItem() instanceof PotionItem) {
            fire(player, "drink_potion");

            PotionContents contents = item.get(DataComponents.POTION_CONTENTS);
            if (contents != null) {
                if (contents.is(Potions.INVISIBILITY))   ActingHandler.onActingEvent(player, "drink_invisibility_potion");
                if (contents.is(Potions.NIGHT_VISION))   ActingHandler.onActingEvent(player, "drink_night_vision_potion");
                if (contents.is(Potions.STRENGTH))       ActingHandler.onActingEvent(player, "drink_strength_potion");
                if (contents.is(Potions.HEALING))        ActingHandler.onActingEvent(player, "drink_healing_potion");
                if (contents.is(Potions.FIRE_RESISTANCE)) ActingHandler.onActingEvent(player, "drink_fire_resistance_potion");
                if (contents.is(Potions.WATER_BREATHING)) ActingHandler.onActingEvent(player, "drink_water_breathing_potion");
                if (contents.is(Potions.SWIFTNESS))      ActingHandler.onActingEvent(player, "drink_swiftness_potion");
            }
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!(event.getTarget() instanceof Mob mob)) return;
        if (mob.getMaxHealth() >= player.getMaxHealth())
            fire(player, "attack_stronger_mob");
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        ActingHandler.onActingEvent(event.getEntity(), "player_login");
    }

    @SubscribeEvent
    public static void onXpPickup(PlayerXpEvent.PickupXp event) {
        ActingHandler.onActingEvent(event.getEntity(), "pickup_xp");
    }

    @SubscribeEvent
    public static void onXpLevelChange(PlayerXpEvent.LevelChange event) {
        if (event.getLevels() > 0)
            ActingHandler.onActingEvent(event.getEntity(), "level_up");
    }

    @SubscribeEvent
    public static void onItemEnchanted(PlayerEnchantItemEvent event) {
        ActingHandler.onActingEvent(event.getEntity(), "enchant_item");
    }

    @SubscribeEvent
    public static void onDropItem(ItemTossEvent event) {
        Player player = event.getPlayer();
        Item item = event.getEntity().getItem().getItem();

        if (item == Items.WRITTEN_BOOK || item == Items.WRITABLE_BOOK)
            ActingHandler.onActingEvent(player, "drop_book");
    }
}
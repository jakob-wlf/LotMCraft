package de.jakob.lotm.beyonders.acting;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.AbilityUsedEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.PlayerBrewedPotionEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ActingEventHandler {

    private static void act(Player player, String id) {
        if (player == null || player.level().isClientSide()) return;
        ActingHandler.onActingEvent(player, id);
    }

    private static void fire(Player player, String id) {
        if (player == null || player.level().isClientSide()) return;
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

    private static final int DARK_LIGHT_LEVEL = 4;

    private static boolean isDark(Player player) {
        return player.level().getMaxLocalRawBrightness(player.blockPosition()) <= DARK_LIGHT_LEVEL;
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
            Map.entry(Blocks.DAYLIGHT_DETECTOR,  "use_daylight_detector"),
            Map.entry(Blocks.DECORATED_POT,      "interact_decorated_pot"),
            Map.entry(Blocks.CAKE,               "eat_cake"),
            Map.entry(Blocks.CANDLE_CAKE,        "eat_cake"),
            Map.entry(Blocks.CRAFTING_TABLE,     "use_crafting_table")
    );

    private static final Set<Block> FLOWERS = Set.of(
            Blocks.DANDELION, Blocks.POPPY, Blocks.BLUE_ORCHID, Blocks.ALLIUM,
            Blocks.AZURE_BLUET, Blocks.RED_TULIP, Blocks.ORANGE_TULIP,
            Blocks.WHITE_TULIP, Blocks.PINK_TULIP, Blocks.OXEYE_DAISY,
            Blocks.CORNFLOWER, Blocks.LILY_OF_THE_VALLEY, Blocks.WITHER_ROSE,
            Blocks.SUNFLOWER, Blocks.LILAC, Blocks.ROSE_BUSH, Blocks.PEONY,
            Blocks.TORCHFLOWER, Blocks.PINK_PETALS
    );

    private static final Map<Holder<MobEffect>, String> POTION_EFFECT_EVENTS = Map.of(
            MobEffects.INVISIBILITY,   "drink_invisibility_potion",
            MobEffects.DAMAGE_BOOST,   "drink_strength_potion",
            MobEffects.HEAL,           "drink_healing_potion",
            MobEffects.MOVEMENT_SPEED, "drink_swiftness_potion",
            MobEffects.NIGHT_VISION,   "drink_night_vision_potion",
            MobEffects.POISON,         "drink_poison_potion",
            MobEffects.SLOW_FALLING,   "drink_slow_falling_potion",
            MobEffects.WATER_BREATHING,"drink_water_breathing_potion"
    );

    private static final Set<UUID> WATCHED_DUSK = new HashSet<>();
    private static final Set<UUID> SLEPT_TONIGHT = new HashSet<>();


    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;   // fires on both sides
        Player player = event.getEntity();
        ItemStack item = event.getItemStack();
        BlockState clickedBlock = event.getLevel().getBlockState(event.getPos());
        Block block = clickedBlock.getBlock();

        if (event.getHand() == InteractionHand.MAIN_HAND) {
            String blockEvent = BLOCK_INTERACT_EVENTS.get(block);
            if (blockEvent != null) fire(player, blockEvent);

            if (block instanceof FlowerPotBlock) fire(player, "interact_flower_pot");

            if (clickedBlock.is(Blocks.SWEET_BERRY_BUSH)
                    && clickedBlock.getValue(SweetBerryBushBlock.AGE) > 1
                    && !item.is(Items.BONE_MEAL))
                fire(player, "harvest_sweet_berries");
        }

        String itemEvent = ITEM_USE_EVENTS.get(item.getItem());
        if (itemEvent != null) fire(player, itemEvent);

        if (item.is(Items.BRUSH) && (block == Blocks.SUSPICIOUS_SAND || block == Blocks.SUSPICIOUS_GRAVEL))
            fire(player, "brush_suspicious_block");

        if (item.is(Items.WRITABLE_BOOK) || item.is(Items.WRITTEN_BOOK))
            fire(player, "interact_with_book");

        if (item.is(Items.MAP) || item.is(Items.FILLED_MAP))
            fire(player, "use_map");
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide()) return;
        Player player = event.getEntity();
        ItemStack item = event.getItemStack();

        String itemEvent = ITEM_USE_EVENTS.get(item.getItem());
        if (itemEvent != null) fire(player, itemEvent);

        if (item.is(Items.WRITTEN_BOOK)) fire(player, "read_written_book");

        if (item.is(Items.WRITABLE_BOOK) || item.is(Items.WRITTEN_BOOK))
            fire(player, "interact_with_book");
        if (item.is(Items.MAP) || item.is(Items.FILLED_MAP))
            fire(player, "use_map");
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) return;   // fires on both sides
        Player player = event.getEntity();
        ItemStack item = event.getItemStack();   // item of the hand this event is for

        if (item.is(Items.NAME_TAG)) act(player, "use_name_tag");
        if (item.is(Items.LEAD))     act(player, "use_lead");

        if (event.getHand() != InteractionHand.MAIN_HAND) {
            if (event.getTarget() instanceof Animal animal && animal.isFood(item))
                handleFeed(player, animal, item);
            return;
        }

        if (event.getTarget() instanceof Villager)
            act(player, "interact_with_villager");

        if (event.getTarget() instanceof Animal animal && animal.isFood(item))
            handleFeed(player, animal, item);
    }

    private static void handleFeed(Player player, Animal animal, ItemStack item) {
        act(player, "feed_animal");
        if (animal.getAge() == 0 && animal.canFallInLove())
            act(player, "breed_animals");
    }


    @SubscribeEvent
    public static void onAbilityUse(AbilityUsedEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getAbility() == null) return;

        String id = String.valueOf(event.getAbility().getId()).toLowerCase();
        if (id.startsWith("use_")) id = id.substring(4);

        fire(player, "use_" + id);
        if (!id.endsWith("_ability"))
            fire(player, "use_" + id + "_ability");
    }


    @SubscribeEvent
    public static void onMount(EntityMountEvent event) {
        if (!event.isMounting()) return;   // was also firing on dismount
        if (!(event.getEntityMounting() instanceof Player player)) return;

        if (event.getEntityBeingMounted() instanceof Boat)             act(player, "ride_boat");
        if (event.getEntityBeingMounted() instanceof AbstractMinecart) act(player, "ride_minecart");
    }


    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        ItemStack item = event.getCrafting();

        if (item.is(Items.MAP)) act(player, "craft_map");

        if (item.getItem() instanceof TieredItem tiered) {
            Tier tier = tiered.getTier();
            if (tier == Tiers.IRON)      act(player, "craft_iron_tool");
            if (tier == Tiers.GOLD)      act(player, "craft_golden_tool");
            if (tier == Tiers.DIAMOND)   act(player, "craft_diamond_tool");
            if (tier == Tiers.NETHERITE) act(player, "craft_netherite_tool");
        }

        if (item.is(Items.COMPASS))         act(player, "craft_compass");
        if (item.is(Items.CLOCK))           act(player, "craft_clock");
        if (item.is(Items.BOOK))            act(player, "craft_book");
        if (item.is(Items.BOOKSHELF))       act(player, "craft_bookshelf");
        if (item.is(Items.SPYGLASS))        act(player, "craft_spyglass");
        if (item.is(Items.PAPER))           act(player, "craft_paper");
        if (item.is(Items.LANTERN) || item.is(Items.SOUL_LANTERN))
            act(player, "craft_lantern");
        if (item.is(Items.LEAD))            act(player, "craft_lead");
        if (item.is(Items.FISHING_ROD))     act(player, "craft_fishing_rod");
        if (item.is(Items.BOW))             act(player, "craft_bow");
        if (item.is(Items.CROSSBOW))        act(player, "craft_crossbow");
        if (item.is(Items.SHIELD))          act(player, "craft_shield");
        if (item.is(Items.SADDLE))          act(player, "craft_saddle");
        if (item.is(Items.NAME_TAG))        act(player, "craft_name_tag");
        if (item.is(Items.FIRE_CHARGE))     act(player, "craft_fire_charge");
        if (item.is(Items.FLINT_AND_STEEL)) act(player, "craft_flint_and_steel");
        if (item.is(Items.TNT))             act(player, "craft_tnt");
        if (item.is(Items.BONE_MEAL))       act(player, "craft_bonemeal");
        if (item.is(Items.ENDER_EYE))       act(player, "craft_ender_eye");
        if (item.is(Items.ENDER_CHEST))     act(player, "craft_ender_chest");

        if (item.getItem() instanceof ArmorItem armor) {
            if (armor.getMaterial() == ArmorMaterials.IRON)
                act(player, "craft_iron_armor");
            if (armor.getMaterial() == ArmorMaterials.DIAMOND)
                act(player, "craft_diamond_armor");
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Player player)) return;

        BlockState placed = event.getPlacedBlock();
        Block b = placed.getBlock();
        BlockPos pos = event.getPos();

        if (b == Blocks.TNT)       fire(player, "place_tnt");
        if (b == Blocks.OBSERVER)  fire(player, "place_observer");

        if (event.getLevel().getBlockState(pos.below()).is(Blocks.FARMLAND)
                && (b instanceof CropBlock || b instanceof StemBlock))
            fire(player, "plant_crop");

        if (placed.is(BlockTags.SAPLINGS))            fire(player, "plant_sapling");
        if (b instanceof AbstractSkullBlock)          fire(player, "place_mob_head");
        if (b == Blocks.SOUL_TORCH || b == Blocks.SOUL_WALL_TORCH) fire(player, "place_soul_torch");
        if (b == Blocks.SOUL_LANTERN)                 fire(player, "place_soul_lantern");

        if (b instanceof LadderBlock || b instanceof ScaffoldingBlock || b == Blocks.VINE)
            fire(player, "use_environment");

        if (placed.is(BlockTags.CANDLES))             fire(player, "place_candle");
        if (FLOWERS.contains(b))                      fire(player, "place_flower");
        if (placed.is(BlockTags.ALL_SIGNS))           fire(player, "place_sign");
        if (placed.is(BlockTags.BANNERS))             fire(player, "place_banner");

        if (b == Blocks.CHISELED_STONE_BRICKS || b == Blocks.CHISELED_NETHER_BRICKS
                || b == Blocks.CHISELED_POLISHED_BLACKSTONE)
            fire(player, "place_chiseled_block");

        if ((b == Blocks.TORCH || b == Blocks.WALL_TORCH) && player.level().isNight())
            fire(player, "place_torch_at_night");
    }


    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();
        if (level.isClientSide()) return;

        BlockPos pos = player.blockPosition();

        if (level.isRaining() && level.canSeeSky(pos))
            act(player, "stand_in_rain");

        boolean nearLava = false;
        for (BlockPos neighbor : List.of(pos.north(), pos.south(), pos.east(), pos.west(), pos.below())) {
            if (level.getBlockState(neighbor).is(Blocks.LAVA)) { nearLava = true; break; }
        }
        if (nearLava) act(player, "stand_near_lava");

        if (player.isUnderWater()) act(player, "swim_underwater");

        if (player.isSprinting() && !player.isSwimming() && !player.isUnderWater())
            act(player, "sprint");

        if (player.isCrouching())   act(player, "crouch");
        if (player.isOnFire())      act(player, "player_on_fire");

        if (player.getY() > 200)    act(player, "stand_at_high_altitude");

        if (player.getY() < level.getMinBuildHeight() + 6)
            act(player, "stand_at_bedrock_level");

        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = level.getBrightness(LightLayer.SKY, pos);
        if (blockLight == 0 && skyLight == 0)
            act(player, "stand_in_complete_darkness");

        long dayTime = level.getDayTime() % 24000;
        if (dayTime >= 23800 || dayTime <= 200)
            act(player, "witness_dawn");

        if (player.getHealth() >= player.getMaxHealth())
            act(player, "maintain_full_health");

        UUID id = player.getUUID();
        if (player.isSleeping()) SLEPT_TONIGHT.add(id);

        if (dayTime >= 13000 && dayTime < 13200) {
            WATCHED_DUSK.add(id);
            SLEPT_TONIGHT.remove(id);
        }
        if (dayTime < 1200 && WATCHED_DUSK.remove(id)) {
            if (!SLEPT_TONIGHT.remove(id))
                act(player, "stay_awake_through_night");
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        WATCHED_DUSK.remove(event.getEntity().getUUID());
        SLEPT_TONIGHT.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTradeWithVillager(TradeWithVillagerEvent event) {
        act(event.getEntity(), "trade_with_villager");
    }

    @SubscribeEvent
    public static void onAnimalTame(AnimalTameEvent event) {
        act(event.getTamer(), "tame_animal");
    }

    @SubscribeEvent
    public static void onEnderPearlTeleport(EntityTeleportEvent.EnderPearl event) {
        if (event.getEntity() instanceof Player player)
            act(player, "use_ender_pearl");
    }

    @SubscribeEvent
    public static void onPlayerBrewedPotion(PlayerBrewedPotionEvent event) {
        act(event.getEntity(), "brew_potion");
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        BlockState state = event.getState();
        Block block = state.getBlock();

        if (block instanceof CropBlock crop && crop.isMaxAge(state)) fire(player, "harvest_crop");
        if (state.is(Blocks.BARREL))                                 fire(player, "break_barrel");
        if (state.is(Blocks.SWEET_BERRY_BUSH) && state.getValue(SweetBerryBushBlock.AGE) > 1)
            fire(player, "harvest_sweet_berries");

        if (state.is(BlockTags.COAL_ORES) || state.is(BlockTags.IRON_ORES) || state.is(BlockTags.COPPER_ORES)
                || state.is(BlockTags.GOLD_ORES) || state.is(BlockTags.REDSTONE_ORES) || state.is(BlockTags.EMERALD_ORES)
                || state.is(BlockTags.LAPIS_ORES) || state.is(BlockTags.DIAMOND_ORES))
            fire(player, "mine_ore");

        if (state.is(Blocks.ANCIENT_DEBRIS)) fire(player, "mine_ancient_debris");
        if (state.is(BlockTags.LOGS))        fire(player, "chop_wood");

        if (state.is(Blocks.ICE) || state.is(Blocks.PACKED_ICE) || state.is(Blocks.BLUE_ICE))
            fire(player, "break_ice");

        if (FLOWERS.contains(block))         fire(player, "harvest_flower");

        if (state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST))
            fire(player, "break_chest");

        if (state.is(Blocks.NETHER_QUARTZ_ORE)) fire(player, "mine_nether_quartz");

        if (state.is(Blocks.SPAWNER) || state.is(Blocks.TRIAL_SPAWNER))
            fire(player, "destroy_spawner");
    }

    @SubscribeEvent
    public static void onFish(ItemFishedEvent event) {
        Player player = event.getEntity();
        act(player, "catch_fish");

        for (ItemStack drop : event.getDrops()) {
            if (drop.is(Items.ENCHANTED_BOOK) || drop.is(Items.BOW) || drop.is(Items.FISHING_ROD)
                    || drop.is(Items.NAME_TAG) || drop.is(Items.SADDLE) || drop.is(Items.NAUTILUS_SHELL)) {
                act(player, "fish_up_treasure");
                break;   // once per catch
            }
        }
    }

    @SubscribeEvent
    public static void onSmelt(PlayerEvent.ItemSmeltedEvent event) {
        act(event.getEntity(), "smelt_item");
    }

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Post event) {
        Player player = event.getPlayer();
        ItemStack stack = event.getItemEntity().getItem();

        act(player, "pickup_item");

        if (stack.is(Items.GOLD_INGOT) || stack.is(Items.GOLD_NUGGET) || stack.is(Items.GOLDEN_APPLE))
            act(player, "pickup_gold");

        if (stack.is(Items.BONE) || stack.is(Items.BONE_MEAL))
            act(player, "pickup_bone");

        if (stack.is(Items.ROTTEN_FLESH))
            act(player, "pickup_rotten_flesh");
    }


    @SubscribeEvent
    public static void onMobKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        if (event.getEntity().getMaxHealth() >= player.getMaxHealth()) fire(player, "kill_strong_mobs");
        if (event.getEntity().isOnFire())                              fire(player, "kill_burning_mob");

        if (event.getEntity() instanceof Mob mob) {
            if (mob.getTarget() == null || !mob.getTarget().getUUID().equals(player.getUUID()))
                fire(player, "kill_untargeted_mob");

            List<Mob> targeting = player.level().getEntitiesOfClass(Mob.class,
                    player.getBoundingBox().inflate(24),
                    m -> m != mob && m.getTarget() != null && m.getTarget().getUUID().equals(player.getUUID()));
            if (targeting.size() >= 2) fire(player, "outnumbered_kill");

            if (isDark(player)) fire(player, "kill_in_darkness");
        }

        if (player.isCrouching()) fire(player, "sneak_kill");

        if (event.getSource().getDirectEntity() instanceof AbstractArrow arrow
                && arrow.getOwner() instanceof Player p && p.equals(player))
            fire(player, "kill_with_bow");

        if (player.getMainHandItem().isEmpty())
            fire(player, "kill_unarmed");

        if (event.getEntity().getType().is(EntityTypeTags.UNDEAD))
            fire(player, "kill_undead");

        if (event.getEntity() instanceof WitherBoss || event.getEntity() instanceof EnderDragon)
            fire(player, "kill_boss");

        float hp = player.getHealth() / player.getMaxHealth();
        if (hp >= 1.0f) act(player, "kill_while_full_health");
        if (hp < 0.25f) act(player, "kill_while_low_health");

        if (player.getY() > 100)
            fire(player, "kill_at_high_altitude");

        if (player.level().isRainingAt(player.blockPosition()))
            fire(player, "kill_in_rain");

        if (event.getEntity().isInWater())
            fire(player, "kill_in_water");
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!(event.getTarget() instanceof Mob mob)) return;
        if (mob.getMaxHealth() >= player.getMaxHealth())
            fire(player, "attack_stronger_mob");
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide() || !player.isAlive()) return;

        fire(player, "take_damage");

        float hp = player.getHealth() / player.getMaxHealth();
        if (hp < 0.25f)
            fire(player, "survive_while_critical");

        if (event.getSource().is(DamageTypeTags.IS_FIRE))
            fire(player, "take_fire_damage");

        List<Mob> targeting = player.level().getEntitiesOfClass(Mob.class,
                player.getBoundingBox().inflate(16),
                m -> m.getTarget() != null && m.getTarget().getUUID().equals(player.getUUID()));
        if (targeting.size() >= 3)
            fire(player, "take_damage_outnumbered");
    }

    @SubscribeEvent
    public static void onPotionThrown(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof ThrownPotion potion)) return;
        if (!(potion.getOwner() instanceof Player player)) return;

        Item item = potion.getItem().getItem();
        if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem)
            fire(player, "throw_splash_potion");
    }


    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (event.getTo().equals(Level.NETHER)) act(player, "enter_nether");
        if (event.getTo().equals(Level.END))    act(player, "enter_end");
        if (event.getFrom().equals(Level.NETHER) || event.getFrom().equals(Level.END))
            act(player, "return_from_dimension");
    }

    @SubscribeEvent
    public static void onExperiencePickup(PlayerXpEvent.LevelChange event) {
        if (event.getLevels() > 0)
            act(event.getEntity(), "gain_xp_level");
    }

    @SubscribeEvent
    public static void onItemFinishedUsing(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack item = event.getItem();

        if (item.is(Items.GOLDEN_APPLE) || item.is(Items.ENCHANTED_GOLDEN_APPLE))
            act(player, "eat_golden_apple");
        if (item.is(Items.SUSPICIOUS_STEW)) act(player, "eat_suspicious_stew");
        if (item.is(Items.CHORUS_FRUIT))    act(player, "eat_chorus_fruit");
        if (item.is(Items.ROTTEN_FLESH))    act(player, "eat_rotten_flesh");
        if (item.is(Items.PUFFERFISH))      act(player, "eat_pufferfish");

        if (item.getItem() instanceof PotionItem) {
            fire(player, "drink_potion");

            PotionContents contents = item.get(DataComponents.POTION_CONTENTS);
            if (contents != null) {
                Set<String> fired = new HashSet<>();
                for (MobEffectInstance effect : contents.getAllEffects()) {
                    String id = POTION_EFFECT_EVENTS.get(effect.getEffect());
                    if (id != null && fired.add(id)) act(player, id);
                }
            }
        }
    }
}
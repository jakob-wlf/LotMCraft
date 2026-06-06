package de.jakob.lotm.acting;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
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
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;
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
            Map.entry(Blocks.DAMAGED_ANVIL,      "use_anvil")
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

            if (b == Blocks.TNT)                                          fire(player, "place_tnt");
            if (clickedBlock.is(Blocks.FARMLAND) && b instanceof CropBlock) fire(player, "plant_crop");
            if (SAPLINGS.contains(b))                                     fire(player, "plant_sapling");
            if (MOB_HEADS.contains(b))                                    fire(player, "place_mob_head");
            if (b == Blocks.SOUL_TORCH || b == Blocks.SOUL_WALL_TORCH)   fire(player, "place_soul_torch");
            if (b == Blocks.SOUL_LANTERN)                                 fire(player, "place_soul_lantern");
            if (b instanceof LadderBlock || b instanceof ScaffoldingBlock || b == Blocks.VINE)
                fire(player, "use_environment");
        }
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
            if (tier == Tiers.IRON) ActingHandler.onActingEvent(player, "craft_iron_tool");
            if (tier == Tiers.GOLD) ActingHandler.onActingEvent(player, "craft_golden_tool");
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();
        if (level.isClientSide()) return;
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

        if (state.getBlock() instanceof CropBlock) fire(player, "harvest_crop");
        if (state.is(Blocks.BARREL))               fire(player, "break_barrel");
        if (state.is(Blocks.SWEET_BERRY_BUSH))     fire(player, "harvest_sweet_berries");

        if (state.is(BlockTags.COAL_ORES) || state.is(BlockTags.IRON_ORES)    || state.is(BlockTags.COPPER_ORES)
                || state.is(BlockTags.GOLD_ORES) || state.is(BlockTags.REDSTONE_ORES) || state.is(BlockTags.EMERALD_ORES)
                || state.is(BlockTags.LAPIS_ORES)|| state.is(BlockTags.DIAMOND_ORES))
            fire(player, "mine_ore");
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
        ActingHandler.onActingEvent(event.getPlayer(), "pickup_item");
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
            if (contents != null && contents.is(Potions.INVISIBILITY))
                ActingHandler.onActingEvent(player, "drink_invisibility_potion");
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!(event.getTarget() instanceof Mob mob)) return;
        if (mob.getMaxHealth() >= player.getMaxHealth())
            fire(player, "attack_stronger_mob");
    }
}
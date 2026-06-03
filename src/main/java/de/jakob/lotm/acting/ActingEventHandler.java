package de.jakob.lotm.acting;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.*;
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
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.List;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ActingEventHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack item = event.getItemStack();
        BlockPos pos = event.getPos();
        Level level = event.getLevel();

        BlockState clickedBlock = level.getBlockState(pos);

        // Crop Planting
        if (clickedBlock.is(Blocks.FARMLAND) && item.getItem() instanceof BlockItem blockItem) {
            Block placedBlock = blockItem.getBlock();
            if (placedBlock instanceof CropBlock) {
                ActingHandler.onActingEvent(player, "plant_crop");
            }
        }

        // place traversal utility blocks
        if (item.getItem() instanceof BlockItem blockItem) {
            Block b = blockItem.getBlock();
            if (b instanceof LadderBlock || b instanceof ScaffoldingBlock || b == Blocks.VINE) {
                ActingHandler.onActingEvent(player, "use_environment");
            }
        }

        // Set Fire
        if (item.is(Items.FLINT_AND_STEEL)) {
            ActingHandler.onActingEvent(player, "set_fire");
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        ItemStack item = event.getItemStack();

        if (!(event.getTarget() instanceof Animal animal)) return;

        // feed or tend to an animal
        if (animal.isFood(item)) {
            ActingHandler.onActingEvent(player, "feed_animal");
        }

        // breed animals
        if (animal.isFood(item) && animal.getAge() == 0 && !animal.isInLove()) {
            ActingHandler.onActingEvent(player, "breed_animals");
        }
    }

    @SubscribeEvent
    public static void onAnimalTame(AnimalTameEvent event) {
        Player player = event.getTamer();

        // tame a wild animal
        ActingHandler.onActingEvent(player, "tame_animal");
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack item = event.getItemStack();

        // use bonemeal to nurture plant growth
        if (item.is(Items.BONE_MEAL)) {
            ActingHandler.onActingEvent(player, "use_bonemeal");
        }
    }

    @SubscribeEvent
    public static void onPlayerBrewedPotion(PlayerBrewedPotionEvent event) {
        Player player = event.getEntity();

        // brew a potion
        ActingHandler.onActingEvent(player, "brew_potion");
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        BlockState brokenBlock = event.getState();

        // Crop Harvesting
        if (brokenBlock.getBlock() instanceof CropBlock) {
            ActingHandler.onActingEvent(player, "harvest_crop");
        }
    }

    @SubscribeEvent
    public static void onMobKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        // Kill strong mobs
        if (event.getEntity().getMaxHealth() >= player.getMaxHealth()) {
            ActingHandler.onActingEvent(player, "kill_strong_mobs");
        }

        // kill a mob that is currently on fire
        if (event.getEntity().isOnFire()) {
            ActingHandler.onActingEvent(player, "kill_burning_mob");
        }

        if (event.getEntity() instanceof Mob mob) {
            // kill a mob that was not targeting the player
            if (mob.getTarget() == null || !mob.getTarget().getUUID().equals(player.getUUID())) {
                ActingHandler.onActingEvent(player, "kill_untargeted_mob");
            }

            // kill while being targeted by 3+ mobs simultaneously
            Level level = player.level();
            List<Mob> targeting = level.getEntitiesOfClass(Mob.class,
                    player.getBoundingBox().inflate(24),
                    m -> m != mob && m.getTarget() != null && m.getTarget().getUUID().equals(player.getUUID()));
            if (targeting.size() >= 2) {
                ActingHandler.onActingEvent(player, "outnumbered_kill");
            }
        }

        // kill while sneaking
        if (player.isCrouching()) {
            ActingHandler.onActingEvent(player, "sneak_kill");
        }

        // kill from darkness
        if (event.getEntity() instanceof Mob) {
            int lightLevel = player.level().getBrightness(LightLayer.BLOCK, player.blockPosition());
            if (lightLevel <= 2) {
                ActingHandler.onActingEvent(player, "kill_in_darkness");
            }
        }

        // kill while wounded
        if (player.getHealth() / player.getMaxHealth() < 0.5f) {
            ActingHandler.onActingEvent(player, "kill_while_hurt");
        }

        // kill while in despair
        if (player.getHealth() / player.getMaxHealth() < 0.25f) {
            ActingHandler.onActingEvent(player, "kill_while_low_health");
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof ThrownPotion potion)) return;
        if (!(potion.getOwner() instanceof Player player)) return;
        if (!(event.getRayTraceResult() instanceof EntityHitResult entityHit)) return;
        if (!(entityHit.getEntity() instanceof Mob)) return;

        ItemStack potionStack = potion.getItem();
        if (potionStack.getItem() instanceof SplashPotionItem || potionStack.getItem() instanceof LingeringPotionItem) {
            // Throw a splash/lingering potion at a mob to
            ActingHandler.onActingEvent(player, "throw_splash_potion");
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();

        // Enter the Nether
        if (event.getTo().equals(Level.NETHER)) {
            ActingHandler.onActingEvent(player, "enter_nether");
        }
    }

    @SubscribeEvent
    public static void onItemFinishedUsing(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack item = event.getItem();

        // consume a golden apple
        if (item.is(Items.GOLDEN_APPLE) || item.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            ActingHandler.onActingEvent(player, "eat_golden_apple");
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!(event.getTarget() instanceof Mob mob)) return;

        // strike a mob that is stronger than the player
        if (mob.getMaxHealth() >= player.getMaxHealth()) {
            ActingHandler.onActingEvent(player, "attack_stronger_mob");
        }
    }

}
package de.jakob.lotm.beyonders.abilities.twilight_giant.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class StrengthOfGiantsAbility extends PassiveAbility {

    private static final double SCALE_BONUS = 2.5D / 1.8D - 1.0D;
    private static final int COMBAT_WINDOW = 20 * 20;
    private static final float HUNGER_FACTOR = 0.5f;
    private static final ResourceLocation SCALE_ID = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "strength_of_giants_scale");

    private static final Map<UUID, Long> lastCombatTick = new HashMap<>();
    private static final Map<UUID, Float> lastExhaustion = new HashMap<>();

    private static StrengthOfGiantsAbility instance;

    public StrengthOfGiantsAbility(String id) {
        super(id);
        instance = this;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 6));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!level.isClientSide()) setScale(entity, true);
    }

    @Override
    public void onPassiveAbilityRemoved(LivingEntity entity, ServerLevel serverLevel) {
        setScale(entity, false);
        lastCombatTick.remove(entity.getUUID());
        lastExhaustion.remove(entity.getUUID());
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;
        long now = victim.level().getGameTime();
        if (applies(victim)) lastCombatTick.put(victim.getUUID(), now);
        if (event.getSource().getEntity() instanceof LivingEntity attacker && applies(attacker)) {
            lastCombatTick.put(attacker.getUUID(), now);
        }
    }

    @SubscribeEvent
    public static void onKnockBack(LivingKnockBackEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || !applies(entity)) return;
        LivingEntity attacker = entity.getLastHurtByMob();
        if (attacker == null || BeyonderData.getSequence(attacker) < BeyonderData.getSequence(entity)) return;
        if (!hasKnockback(attacker.getMainHandItem())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!applies(player)) return;
        ignoreCobwebs(player);
        if (!player.level().isClientSide()) slowHunger(player);
    }

    private static void ignoreCobwebs(Player player) {
        AABB box = player.getBoundingBox();
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
            if (player.level().getBlockState(pos).getBlock() instanceof WebBlock) {
                player.makeStuckInBlock(Blocks.AIR.defaultBlockState(), Vec3.ZERO);
                return;
            }
        }
    }

    private static void slowHunger(Player player) {
        FoodData food = player.getFoodData();
        float current = food.getExhaustionLevel();
        Float previous = lastExhaustion.put(player.getUUID(), current);
        if (previous == null || current <= previous || !inCombat(player)) return;
        float reduced = current - (current - previous) * HUNGER_FACTOR;
        food.setExhaustion(reduced);
        lastExhaustion.put(player.getUUID(), reduced);
    }

    private static boolean inCombat(LivingEntity entity) {
        Long last = lastCombatTick.get(entity.getUUID());
        return last != null && entity.level().getGameTime() - last <= COMBAT_WINDOW;
    }

    private static boolean hasKnockback(ItemStack stack) {
        return stack.getEnchantments().keySet().stream().anyMatch(enchantment -> enchantment.is(Enchantments.KNOCKBACK));
    }

    private static void setScale(LivingEntity entity, boolean giant) {
        AttributeInstance scale = entity.getAttribute(Attributes.SCALE);
        if (scale == null || scale.hasModifier(SCALE_ID) == giant) return;
        if (giant) {
            scale.addTransientModifier(new AttributeModifier(SCALE_ID, SCALE_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        } else {
            scale.removeModifier(SCALE_ID);
        }
    }

    private static boolean applies(LivingEntity entity) {
        return instance != null && instance.shouldApplyTo(entity);
    }
}

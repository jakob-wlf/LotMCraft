package de.jakob.lotm.beyonders.abilities.twilight_giant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.twilight_giant.handlers.TwilightAging;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.twilight_giant.TwilightSlashEntity;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class TwilightSwordAbility extends ToggleAbility {

    private static final ResourceLocation DAMAGE_ID = ResourceLocation.withDefaultNamespace("base_attack_damage");
    private static final ResourceLocation SPEED_ID = ResourceLocation.withDefaultNamespace("base_attack_speed");
    private static final ResourceLocation REACH_ID = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "twilight_sword_reach");
    private static final double SWORD_REACH_BONUS = 12.0D;
    private static final double SWORD_DAMAGE = 14.0D;
    private static final double SWORD_SPEED = -2.8D;
    private static final int BLINK_RANGE = 20^5;
    private static final double BLINK_OFFSET = 1.5D;
    private static final int BLINK_COOLDOWN = 20 * 8;
    private static final float BLINK_COST = 600f;
    private static final float STRIKE_COST = 2500f;
    private static final int HOLD_GAP = 8;
    private static final int CHARGE_TICKS = 30;
    private static final double STRIKE_LENGTH = 30.0D;
    private static final double STRIKE_WIDTH = 8.0D;
    private static final double STRIKE_HIT_RADIUS = 2.5D;
    private static final int STRIKE_TICKS = 12;
    private static final int STRIKE_DAMAGE = 30;
    private static final float UPKEEP = 80f;

    private static final Map<UUID, Charge> charges = new HashMap<>();
    private static final Map<UUID, Long> lastBlink = new HashMap<>();

    public TwilightSwordAbility(String id) {
        super(id);
        canBeUsedByNPC = false;
        tickRate = 20;
        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(2000f, 1200f, 800f));
        baseDamage = STRIKE_DAMAGE;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 800;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof Player player)) return;
        ItemStack sword = createSword();
        if (player.getMainHandItem().isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, sword);
        } else if (!player.getInventory().add(sword)) {
            return;
        }
        serverLevel.sendParticles(TwilightAging.TWILIGHT_DUST, player.getX(), player.getY(1.0), player.getZ(), 40, 0.5, 0.7, 0.5, 0.05);
        serverLevel.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY(1.0), player.getZ(), 20, 0.4, 0.6, 0.4, 0.03);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, player.getSoundSource(), 1f, 0.6f);
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (!(entity instanceof Player player)) return;
        charges.remove(player.getUUID());
        ArsenalOfDawnAbility.removeAll(player, ArsenalOfDawnAbility.TWILIGHT);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, player.getSoundSource(), 1f, 1.2f);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
    }

    @Override
    public void prepareTick(Level level, LivingEntity entity) {
        if (!(entity instanceof Player player) || !ArsenalOfDawnAbility.hasAny(player, ArsenalOfDawnAbility.TWILIGHT)) {
            if (level instanceof ServerLevel serverLevel) cancel(serverLevel, entity);
            return;
        }
        if (!level.isClientSide && shouldConsumeSpirituality(entity)) {
            if (BeyonderData.getSpirituality(entity) < UPKEEP) {
                cancel((ServerLevel) level, entity);
                return;
            }
            BeyonderData.reduceSpirituality(entity, UPKEEP);
        }
        tick(level, entity);
    }

    private static ItemStack createSword() {
        ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
                .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(REACH_ID, SWORD_REACH_BONUS, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(DAMAGE_ID, SWORD_DAMAGE, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(SPEED_ID, SWORD_SPEED, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
        ItemStack sword = ArsenalOfDawnAbility.create(ModItems.TWILIGHT_SWORD.get(), ArsenalOfDawnAbility.TWILIGHT_SWORD, modifiers);
        sword.set(DataComponents.ITEM_NAME, Component.translatable("item.lotmcraft.twilight_sword").withColor(TwilightAging.TWILIGHT_TEXT));
        sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return sword;
    }

    private static boolean holds(LivingEntity entity) {
        return ArsenalOfDawnAbility.is(entity.getMainHandItem(), ArsenalOfDawnAbility.TWILIGHT_SWORD);
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (!holds(event.getEntity()) || !(event.getTarget() instanceof LivingEntity target)) return;
        if (!AbilityUtil.mayDamage(event.getEntity(), target)) {
            event.setCanceled(true);
            return;
        }
        target.invulnerableTime = 0;
        if (event.getEntity() instanceof ServerPlayer player) TwilightAuthorityAbility.onSwordLeftClick(player, target);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!event.isCanceled() || !(event.getSource().getEntity() instanceof LivingEntity attacker) || !holds(attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity target) || !AbilityUtil.mayDamage(attacker, target)) return;
        event.setCanceled(false);
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getHand() != InteractionHand.MAIN_HAND || !holds(player)) return;
        ServerLevel level = player.serverLevel();
        long now = level.getGameTime();
        Charge charge = charges.get(player.getUUID());
        if (charge == null || now - charge.last > HOLD_GAP) {
            charges.put(player.getUUID(), new Charge(now));
            blinkSlash(level, player);
            return;
        }
        charge.last = now;
        level.sendParticles(TwilightAging.TWILIGHT_DUST, player.getX(), player.getY(1.0), player.getZ(), 10, 0.6, 0.6, 0.6, 0.05);
        if (now - charge.start < CHARGE_TICKS) return;
        if (!pay(player, STRIKE_COST)) return;
        charges.remove(player.getUUID());
        chargedStrike(level, player);
    }

    private static void blinkSlash(ServerLevel level, ServerPlayer player) {
        Long last = lastBlink.get(player.getUUID());
        if (last != null && level.getGameTime() - last < BLINK_COOLDOWN) return;
        LivingEntity target = AbilityUtil.getTargetEntity(player, BLINK_RANGE, 1.5f);
        if (target == null || !AbilityUtil.mayDamage(player, target)) return;
        if (!pay(player, BLINK_COST)) return;
        lastBlink.put(player.getUUID(), level.getGameTime());
        Vec3 toTarget = target.position().subtract(player.position());
        Vec3 direction = new Vec3(toTarget.x, 0, toTarget.z);
        direction = direction.lengthSqr() < 1.0E-4 ? new Vec3(1, 0, 0) : direction.normalize();
        Vec3 destination = target.position().subtract(direction.scale(BLINK_OFFSET + target.getBbWidth() * 0.5));
        level.sendParticles(TwilightAging.TWILIGHT_DUST, player.getX(), player.getY(1.0), player.getZ(), 30, 0.4, 0.8, 0.4, 0.05);
        float yaw = (float) (Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90);
        player.connection.teleport(destination.x, target.getY(), destination.z, yaw, player.getXRot());
        player.fallDistance = 0;
        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        pierce(level, player, target, damage);
        player.swing(InteractionHand.MAIN_HAND, true);
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY(0.6), target.getZ(), 3, 0.4, 0.3, 0.4, 0);
        level.sendParticles(TwilightAging.TWILIGHT_DUST, target.getX(), target.getY(0.6), target.getZ(), 30, 0.5, 0.6, 0.5, 0.1);
        level.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.5f, 0.7f);
    }

    private static void chargedStrike(ServerLevel level, ServerPlayer player) {
        Vec3 start = player.getEyePosition().add(0, -0.3, 0);
        Vec3 forward = player.getLookAngle().normalize();
        int damage = STRIKE_DAMAGE;
        Set<UUID> struck = new HashSet<>();
        int[] step = {0};
        Vec3 travel = new Vec3(forward.x, 0, forward.z);
        travel = travel.lengthSqr() < 1.0E-4 ? new Vec3(1, 0, 0) : travel.normalize();
        Vec3 end = player.position().add(travel.scale(STRIKE_LENGTH));
        TwilightSlashEntity slash = new TwilightSlashEntity(ModEntities.TWILIGHT_SLASH.get(), level);
        slash.setPos(end.x, player.getY(), end.z);
        slash.setOwner(player);
        level.addFreshEntity(slash);
        level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, player.getSoundSource(), 3f, 0.4f);
        level.playSound(null, player.blockPosition(), SoundEvents.WITHER_SHOOT, player.getSoundSource(), 2f, 0.6f);
        ServerScheduler.scheduleForDuration(0, 1, STRIKE_TICKS, () -> {
            double distance = STRIKE_LENGTH * (++step[0]) / STRIKE_TICKS;
            Vec3 middle = start.add(forward.scale(distance));
            for (LivingEntity target : AbilityUtil.getNearbyEntities(player, level, middle, STRIKE_WIDTH / 2 + STRIKE_HIT_RADIUS)) {
                if (!struck.add(target.getUUID()) || !AbilityUtil.mayDamage(player, target)) continue;
                pierce(level, player, target, damage);
                level.sendParticles(TwilightAging.TWILIGHT_DUST, target.getX(), target.getY(0.6), target.getZ(), 30, 0.5, 0.6, 0.5, 0.1);
            }
        }, level);
    }

    private static void pierce(ServerLevel level, LivingEntity attacker, LivingEntity target, float damage) {
        if (!AbilityUtil.mayDamage(attacker, target)) return;
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) return;
        float before = target.getHealth();
        target.invulnerableTime = 0;
        DamageSource source = ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, attacker);
        target.hurt(source, damage);
        if (!target.isAlive() || target.getHealth() < before) return;
        float health = before - damage;
        if (health > 0) {
            target.setHealth(health);
        } else {
            target.setHealth(0f);
            target.die(source);
        }
    }

    private static boolean pay(ServerPlayer player, float cost) {
        if (player.hasInfiniteMaterials()) return true;
        if (BeyonderData.getSpirituality(player) < cost) return false;
        BeyonderData.reduceSpirituality(player, cost);
        return true;
    }

    private static final class Charge {
        private final long start;
        private long last;

        private Charge(long start) {
            this.start = start;
            this.last = start;
        }
    }
}

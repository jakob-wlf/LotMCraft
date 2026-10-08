package de.jakob.lotm.beyonders.abilities.twilight_giant.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class WeaponMasteryAbility extends PassiveAbility {

    private static final float WEAPON_DAMAGE_MULTIPLIER = 1.15f;
    private static final double ATTACK_SPEED_BONUS = 0.25D;
    private static final float THROWN_MULTIPLIER = 1.25f;
    private static final float SHIELD_KNOCKBACK_STRENGTH = 1.4f;
    private static final float ARMOR_SAVE_CHANCE = 0.15f;
    private static final ResourceLocation SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "weapon_mastery_attack_speed");

    private static WeaponMasteryAbility instance;

    public WeaponMasteryAbility(String id) {
        super(id);
        instance = this;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 7));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!level.isClientSide()) syncAttackSpeed(entity, isWeapon(entity.getMainHandItem()));
    }

    @Override
    public void onPassiveAbilityRemoved(LivingEntity entity, ServerLevel serverLevel) {
        syncAttackSpeed(entity, false);
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getSlot() != EquipmentSlot.MAINHAND) return;
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || !applies(entity)) return;
        syncAttackSpeed(entity, isWeapon(event.getTo()));
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof LivingEntity attacker) || attacker.level().isClientSide()) return;
        boolean melee = source.getDirectEntity() == attacker && isWeapon(attacker.getMainHandItem());
        boolean thrown = source.getDirectEntity() instanceof ThrownTrident;
        if ((melee || thrown) && applies(attacker)) {
            event.setAmount(event.getAmount() * (melee ? WEAPON_DAMAGE_MULTIPLIER : THROWN_MULTIPLIER));
        }
    }

    @SubscribeEvent
    public static void onThrow(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || event.loadedFromDisk()) return;
        if (!(event.getEntity() instanceof ThrownTrident trident) || !(trident.getOwner() instanceof LivingEntity owner)) return;
        if (applies(owner)) trident.setDeltaMovement(trident.getDeltaMovement().scale(THROWN_MULTIPLIER));
    }

    @SubscribeEvent
    public static void onUseTick(LivingEntityUseItemEvent.Tick event) {
        LivingEntity entity = event.getEntity();
        if (entity.tickCount % 2 != 0) return;
        if (!(event.getItem().getItem() instanceof BowItem) && !(event.getItem().getItem() instanceof CrossbowItem)) return;
        if (applies(entity)) event.setDuration(event.getDuration() - 1);
    }

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        LivingEntity blocker = event.getEntity();
        if (!event.getBlocked() || blocker.level().isClientSide()) return;
        if (!(event.getDamageSource().getDirectEntity() instanceof LivingEntity attacker) || !applies(blocker)) return;
        attacker.knockback(SHIELD_KNOCKBACK_STRENGTH, blocker.getX() - attacker.getX(), blocker.getZ() - attacker.getZ());
        attacker.hurtMarked = true;
    }

    @SubscribeEvent
    public static void onArmorHurt(ArmorHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || !applies(entity)) return;
        for (EquipmentSlot slot : event.getArmorMap().keySet()) {
            if (entity.getRandom().nextFloat() < ARMOR_SAVE_CHANCE) event.setNewDamage(slot, 0);
        }
    }

    public static boolean isWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers().stream()
                .anyMatch(entry -> entry.attribute().value() == Attributes.ATTACK_DAMAGE.value() && entry.slot().test(EquipmentSlot.MAINHAND));
    }

    private static void syncAttackSpeed(LivingEntity entity, boolean active) {
        AttributeInstance attackSpeed = entity.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed == null || attackSpeed.hasModifier(SPEED_MODIFIER_ID) == active) return;
        if (active) {
            attackSpeed.addTransientModifier(new AttributeModifier(SPEED_MODIFIER_ID, ATTACK_SPEED_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else {
            attackSpeed.removeModifier(SPEED_MODIFIER_ID);
        }
    }

    private static boolean applies(LivingEntity entity) {
        return instance != null && instance.shouldApplyTo(entity);
    }
}

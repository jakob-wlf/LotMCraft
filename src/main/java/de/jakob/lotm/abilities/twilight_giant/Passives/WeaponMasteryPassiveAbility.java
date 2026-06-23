package de.jakob.lotm.abilities.twilight_giant.Passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class WeaponMasteryPassiveAbility extends PassiveAbilityItem {

    private static final ResourceLocation ATTACK_DAMAGE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(
        LOTMCraft.MOD_ID,
        "weapon_mastery_attack_damage"
    );
    private static final ResourceLocation ATTACK_SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(
        LOTMCraft.MOD_ID,
        "weapon_mastery_attack_speed"
    );
    private static final ResourceLocation ATTACK_COOLDOWN_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(
        LOTMCraft.MOD_ID,
        "weapon_mastery_attack_cooldown"
    );

    private static final double ATTACK_DAMAGE_BONUS = 2.0;
    private static final double ATTACK_SPEED_BONUS = 0.2;
    private static final double ATTACK_COOLDOWN_MULTIPLIER = 0.15;
    private static final double CHARGE_TIME_MULTIPLIER = 0.75;

    public WeaponMasteryPassiveAbility(Properties properties) {
        super(properties);
    }


    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 7));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        ItemStack weapon = getHeldWeapon(entity);
        if (weapon.isEmpty()) {
            removeModifier(entity, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_MODIFIER_ID);
            removeModifier(entity, Attributes.ATTACK_SPEED, ATTACK_SPEED_MODIFIER_ID);
            removeModifier(entity, Attributes.ATTACK_SPEED, ATTACK_COOLDOWN_MODIFIER_ID);
            return;
        }

        applyModifier(entity, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_MODIFIER_ID,
                ATTACK_DAMAGE_BONUS, AttributeModifier.Operation.ADD_VALUE);
        applyModifier(entity, Attributes.ATTACK_SPEED, ATTACK_SPEED_MODIFIER_ID,
                ATTACK_SPEED_BONUS, AttributeModifier.Operation.ADD_VALUE);
        applyModifier(entity, Attributes.ATTACK_SPEED, ATTACK_COOLDOWN_MODIFIER_ID,
                ATTACK_COOLDOWN_MULTIPLIER, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    private static ItemStack getHeldWeapon(LivingEntity entity) {
        ItemStack mainHand = entity.getMainHandItem();
        if (isWeapon(mainHand)) {
            return mainHand;
        }

        ItemStack offHand = entity.getOffhandItem();
        if (isWeapon(offHand)) {
            return offHand;
        }

        return ItemStack.EMPTY;
    }

    private static boolean isWeapon(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        Item item = stack.getItem();
        return item instanceof SwordItem
                || item instanceof AxeItem
                || item instanceof TridentItem
                || item instanceof BowItem
                || item instanceof CrossbowItem;
    }

    private static boolean isChargeWeapon(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        Item item = stack.getItem();
        return item instanceof BowItem
                || item instanceof CrossbowItem
                || item instanceof TridentItem;
    }

    private static void applyModifier(LivingEntity entity,
                                      net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                                      ResourceLocation modifierId,
                                      double value,
                                      AttributeModifier.Operation operation) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }

        instance.removeModifier(modifierId);
        instance.addTransientModifier(new AttributeModifier(modifierId, value, operation));
    }

    private static void removeModifier(LivingEntity entity,
                                       net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                                       ResourceLocation modifierId) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(modifierId);
        }
    }

    @SubscribeEvent
    public static void onItemUseStart(LivingEntityUseItemEvent.Start event) {
        ItemStack stack = event.getItem();
        if (!isChargeWeapon(stack)) {
            return;
        }

        PassiveAbilityItem ability = (PassiveAbilityItem) PassiveAbilityHandler.WEAPON_MASTERY_PASSIVE.get();
        if (!ability.shouldApplyTo(event.getEntity())) {
            return;
        }

        int duration = event.getDuration();
        int reducedDuration = Math.max(1, (int) Math.ceil(duration * CHARGE_TIME_MULTIPLIER));
        if (reducedDuration < duration) {
            event.setDuration(reducedDuration);
        }
    }

}






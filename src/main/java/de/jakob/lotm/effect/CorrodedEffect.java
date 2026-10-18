package de.jakob.lotm.effect;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CorrodedEffect extends MobEffect {
    public CorrodedEffect() {
        super(MobEffectCategory.HARMFUL, 0x4A5D1A);
        addAttributeModifier(Attributes.ARMOR, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "corroded_armor"), -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {return duration % 20 == 0;}
    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && entity instanceof Player) {corrodeGear(entity, 1 + amplifier);}
        return true;
    }
    // Armor and held items lose durability
    public static void corrodeGear(LivingEntity entity, int amount) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.isDamageableItem()) stack.hurtAndBreak(amount, entity, slot);
        }
    }
}
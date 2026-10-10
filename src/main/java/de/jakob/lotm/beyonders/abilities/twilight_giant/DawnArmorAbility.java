package de.jakob.lotm.beyonders.abilities.twilight_giant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DawnArmorAbility extends ToggleAbility {

    public DawnArmorAbility(String id) {
        super(id);
        tickRate = 20;
        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(320f, 240f, 180f, 140f, 110f, 90f, 80f));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 80;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (!(entity instanceof Player player)) return;
        float reduction = reduction(BeyonderData.getSequence(player));
        equip(player, EquipmentSlot.HEAD, piece(Items.IRON_HELMET, ArsenalOfDawnAbility.HELMET, reduction, EquipmentSlotGroup.HEAD));
        equip(player, EquipmentSlot.CHEST, piece(Items.IRON_CHESTPLATE, ArsenalOfDawnAbility.CHESTPLATE, reduction, EquipmentSlotGroup.CHEST));
        equip(player, EquipmentSlot.LEGS, piece(Items.IRON_LEGGINGS, ArsenalOfDawnAbility.LEGGINGS, reduction, EquipmentSlotGroup.LEGS));
        equip(player, EquipmentSlot.FEET, piece(Items.IRON_BOOTS, ArsenalOfDawnAbility.BOOTS, reduction, EquipmentSlotGroup.FEET));
        if (level instanceof ServerLevel serverLevel) {
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.END_ROD, player.position().add(0, 1.2, 0), 40, 0.5, 0.8, 0.5, 0.05);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_NETHERITE.value(), player.getSoundSource(), 1, 1.2f);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, player.getSoundSource(), 0.6f, 1.6f);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (entity instanceof Player player) ArsenalOfDawnAbility.removeAll(player, ArsenalOfDawnAbility.ARMOR);
        level.playSound(null, entity.blockPosition(), SoundEvents.ARMOR_EQUIP_IRON.value(), entity.getSoundSource(), 1, 0.6f);
    }

    private static float reduction(int sequence) {
        return switch (sequence) {
            case 0 -> 0.30F;
            case 1, 2 -> 0.25F;
            default -> 0.20F;
        };
    }

    private static ItemStack piece(Item base, String kind, float reduction, EquipmentSlotGroup slot) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "dawn_" + kind + "_armor");
        ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(id, 0, AttributeModifier.Operation.ADD_VALUE), slot)
                .build()
                .withTooltip(false);
        ItemStack stack = ArsenalOfDawnAbility.create(base, kind, modifiers);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putFloat(ArsenalOfDawnAbility.REDUCTION_TAG, reduction));
        return stack;
    }

    private static void equip(Player player, EquipmentSlot slot, ItemStack piece) {
        ItemStack previous = player.getItemBySlot(slot);
        player.setItemSlot(slot, piece);
        if (!previous.isEmpty() && !player.getInventory().add(previous)) player.drop(previous, false);
    }
}

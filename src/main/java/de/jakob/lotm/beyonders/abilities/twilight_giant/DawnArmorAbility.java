package de.jakob.lotm.beyonders.abilities.twilight_giant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.ParticleUtil;
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
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class DawnArmorAbility extends Ability {

    private static final double TOUGHNESS = 1.0D;

    public DawnArmorAbility(String id) {
        super(id, 10);
        canBeUsedByNPC = false;
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
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof Player player)) return;

        if (ArsenalOfDawnAbility.hasAny(player, ArsenalOfDawnAbility.ARMOR)) {
            ArsenalOfDawnAbility.removeAll(player, ArsenalOfDawnAbility.ARMOR);
            BeyonderData.incrementSpirituality(player, getSpiritualityCost());
            level.playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_IRON.value(), player.getSoundSource(), 1, 0.6f);
            return;
        }

        equip(player, EquipmentSlot.HEAD, piece(Items.IRON_HELMET, ArsenalOfDawnAbility.HELMET, 3, EquipmentSlotGroup.HEAD));
        equip(player, EquipmentSlot.CHEST, piece(Items.IRON_CHESTPLATE, ArsenalOfDawnAbility.CHESTPLATE, 7, EquipmentSlotGroup.CHEST));
        equip(player, EquipmentSlot.LEGS, piece(Items.IRON_LEGGINGS, ArsenalOfDawnAbility.LEGGINGS, 6, EquipmentSlotGroup.LEGS));
        equip(player, EquipmentSlot.FEET, piece(Items.IRON_BOOTS, ArsenalOfDawnAbility.BOOTS, 6, EquipmentSlotGroup.FEET));

        ParticleUtil.spawnParticles(serverLevel, ParticleTypes.END_ROD, player.position().add(0, 1.2, 0), 40, 0.5, 0.8, 0.5, 0.05);
        level.playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_NETHERITE.value(), player.getSoundSource(), 1, 1.2f);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, player.getSoundSource(), 0.6f, 1.6f);
    }

    private static ItemStack piece(Item base, String kind, double armor, EquipmentSlotGroup slot) {
        ResourceLocation armorId = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "dawn_" + kind + "_armor");
        ResourceLocation toughnessId = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "dawn_" + kind + "_toughness");
        ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(armorId, armor, AttributeModifier.Operation.ADD_VALUE), slot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(toughnessId, TOUGHNESS, AttributeModifier.Operation.ADD_VALUE), slot)
                .build();
        return ArsenalOfDawnAbility.create(base, kind, modifiers);
    }

    private static void equip(Player player, EquipmentSlot slot, ItemStack piece) {
        ItemStack previous = player.getItemBySlot(slot);
        player.setItemSlot(slot, piece);
        if (!previous.isEmpty() && !player.getInventory().add(previous)) player.drop(previous, false);
    }
}

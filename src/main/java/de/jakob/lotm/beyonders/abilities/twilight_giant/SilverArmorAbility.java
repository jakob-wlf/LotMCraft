package de.jakob.lotm.beyonders.abilities.twilight_giant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class SilverArmorAbility extends ToggleAbility {



    private static final double TOUGHNESS = 3.0D;
    private static final double KNOCKBACK_RESISTANCE = 0.1D;
    private static final int HIGH_TIER_CHARGES = 3;
    private static final float HIGH_TIER_REDUCTION = 0.5f;

    private static final Map<UUID, Integer> charges = new HashMap<>();

    public SilverArmorAbility(String id) {
        super(id);
        tickRate = 20;
        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(400f, 250f, 160f, 100f));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 100;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (!(entity instanceof Player player)) return;
        charges.put(player.getUUID(), HIGH_TIER_CHARGES);
        equip(player, EquipmentSlot.HEAD, piece(Items.IRON_HELMET, ArsenalOfDawnAbility.SILVER_HELMET, 3, EquipmentSlotGroup.HEAD));
        equip(player, EquipmentSlot.CHEST, piece(Items.IRON_CHESTPLATE, ArsenalOfDawnAbility.SILVER_CHESTPLATE, 8, EquipmentSlotGroup.CHEST));
        equip(player, EquipmentSlot.LEGS, piece(Items.IRON_LEGGINGS, ArsenalOfDawnAbility.SILVER_LEGGINGS, 6, EquipmentSlotGroup.LEGS));
        equip(player, EquipmentSlot.FEET, piece(Items.IRON_BOOTS, ArsenalOfDawnAbility.SILVER_BOOTS, 3, EquipmentSlotGroup.FEET));
        if (level instanceof ServerLevel serverLevel) {
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.WHITE_ASH, player.position().add(0, 1, 0), 60, 0.5, 0.9, 0.5, 0.05);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_NETHERITE.value(), player.getSoundSource(), 1f, 1.4f);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        charges.remove(entity.getUUID());
        if (entity instanceof Player player) ArsenalOfDawnAbility.removeAll(player, ArsenalOfDawnAbility.SILVER_ARMOR);
        level.playSound(null, entity.blockPosition(), SoundEvents.ARMOR_EQUIP_IRON.value(), entity.getSoundSource(), 1f, 0.7f);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        Integer left = charges.get(target.getUUID());
        if (left == null || left <= 0 || !(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        if (BeyonderData.getSequence(attacker) >= BeyonderData.getSequence(target)) return;
        event.setAmount(event.getAmount() * (1 - HIGH_TIER_REDUCTION));
        charges.put(target.getUUID(), left - 1);
        target.level().playSound(null, target.blockPosition(), SoundEvents.ANVIL_LAND, target.getSoundSource(), 0.6f, 1.6f);
    }

    private static ItemStack piece(Item base, String kind, double armor, EquipmentSlotGroup slot) {
        ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(id(kind, "armor"), armor, AttributeModifier.Operation.ADD_VALUE), slot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(id(kind, "toughness"), TOUGHNESS, AttributeModifier.Operation.ADD_VALUE), slot)
                .add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(id(kind, "knockback"), KNOCKBACK_RESISTANCE, AttributeModifier.Operation.ADD_VALUE), slot)
                .build();
        return ArsenalOfDawnAbility.create(base, kind, modifiers);
    }

    private static ResourceLocation id(String kind, String attribute) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, kind + "_" + attribute);
    }

    private static void equip(Player player, EquipmentSlot slot, ItemStack piece) {
        ItemStack previous = player.getItemBySlot(slot);
        player.setItemSlot(slot, piece);
        if (!previous.isEmpty() && !player.getInventory().add(previous)) player.drop(previous, false);
    }
}

package de.jakob.lotm.entity;

import de.jakob.lotm.attachments.ModAttachments;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.*;

public class OriginalBodyEntity extends LivingEntity {
    private final Map<EquipmentSlot, ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);

    public final SimpleContainer inventory = new SimpleContainer(41);

    public OriginalBodyEntity(EntityType<? extends OriginalBodyEntity> type, Level level) {
        super(type, level);

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            equipment.put(slot, ItemStack.EMPTY);
        }
        this.noCulling = true;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return List.of(
                getItemBySlot(EquipmentSlot.FEET),
                getItemBySlot(EquipmentSlot.LEGS),
                getItemBySlot(EquipmentSlot.CHEST),
                getItemBySlot(EquipmentSlot.HEAD)
        );
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return equipment.getOrDefault(slot, ItemStack.EMPTY);
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        equipment.put(slot, stack.copy());
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0)
                .add(Attributes.ARMOR, 0.0)
                .add(Attributes.ARMOR_TOUGHNESS, 0.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }
}
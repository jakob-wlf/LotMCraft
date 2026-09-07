package de.jakob.lotm.attachments;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.ability_entities.ControlBodyDouble;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityControllingComponent implements INBTSerializable<CompoundTag> {

    public boolean isControlling = false;
    private Map<ResourceLocation, AttributeSnapshot> savedAttributes = new HashMap<>();
    private double savedHealth = 20.0;

    public String controlledEntityPathway = LOTMCraft.NON_BEYONDER_PATHWAY;
    public int controlledEntitySequence = LOTMCraft.NON_BEYONDER_SEQ;
    public LivingEntity controlledEntity = null;
    public ControlBodyDouble bodyDouble = null;
    public boolean canUseOwnAbilities = true;

    private ArrayList<String> abilityWheelAbilities = new ArrayList<>();
    private int selectedAbilityInWheel = 0;

    private ArrayList<String> abilityBarAbilities = new ArrayList<>();


    public boolean isControlling() {
        return isControlling;
    }

    public void setControlling(boolean controlling) {
        isControlling = controlling;
    }

    public String getControlledEntityPathway() {
        return controlledEntityPathway;
    }

    public void setControlledEntityPathway(@NonNull String controlledEntityPathway) {
        this.controlledEntityPathway = controlledEntityPathway;
    }

    public int getControlledEntitySequence() {
        return controlledEntitySequence;
    }

    public void setControlledEntitySequence(int controlledEntitySequence) {
        this.controlledEntitySequence = controlledEntitySequence;
    }

    public LivingEntity getControlledEntity() {
        return controlledEntity;
    }

    public void setControlledEntity(LivingEntity controlledEntity) {
        this.controlledEntity = controlledEntity;
    }

    public boolean isCanUseOwnAbilities() {
        return canUseOwnAbilities;
    }

    public void setCanUseOwnAbilities(boolean canUseOwnAbilities) {
        this.canUseOwnAbilities = canUseOwnAbilities;
    }

    public ControlBodyDouble getBodyDouble() {
        return bodyDouble;
    }

    public void setBodyDouble(ControlBodyDouble bodyDouble) {
        this.bodyDouble = bodyDouble;
    }

    public ArrayList<String> getAbilityWheelAbilities() {
        return abilityWheelAbilities;
    }

    public void setAbilityWheelAbilities(ArrayList<String> abilityWheelAbilities) {
        this.abilityWheelAbilities = new ArrayList<>(abilityWheelAbilities);
    }

    public int getSelectedAbilityInWheel() {
        return selectedAbilityInWheel;
    }

    public void setSelectedAbilityInWheel(int selectedAbilityInWheel) {
        this.selectedAbilityInWheel = selectedAbilityInWheel;
    }

    public ArrayList<String> getAbilityBarAbilities() {
        return abilityBarAbilities;
    }

    public void setAbilityBarAbilities(ArrayList<String> abilityBarAbilities) {
        this.abilityBarAbilities = new ArrayList<>(abilityBarAbilities);
    }

    public void captureAttributesFrom(LivingEntity entity) {
        savedAttributes.clear();
        for (Attribute attribute : BuiltInRegistries.ATTRIBUTE) {
            AttributeInstance instance = entity.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute));
            if(instance == null) continue;
            ResourceLocation id = BuiltInRegistries.ATTRIBUTE.getKey(attribute);
            if(id == null) continue;

            savedAttributes.put(id, new AttributeSnapshot(instance.getBaseValue(), instance.getModifiers()));
        }
        savedHealth = entity.getHealth();
    }

    public void restoreAttributesTo(LivingEntity entity) {
        for (Map.Entry<ResourceLocation, AttributeSnapshot> saved : savedAttributes.entrySet()) {
            Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(saved.getKey());
            if (attribute == null) continue;

            AttributeInstance instance = entity.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute));
            if (instance == null) continue;

            instance.setBaseValue(saved.getValue().baseValue);
            for (AttributeModifier modifier : saved.getValue().modifiers) {
                instance.addOrReplacePermanentModifier(modifier);
            }
        }

        entity.setHealth((float) Math.min(savedHealth, entity.getMaxHealth()));
        savedAttributes.clear();
    }

    public boolean hasSavedAttributes() {
        return !savedAttributes.isEmpty();
    }

    public void reset() {
        isControlling = false;
        savedAttributes = new HashMap<>();
        savedHealth = 20.0;

        controlledEntityPathway = LOTMCraft.NON_BEYONDER_PATHWAY;
        controlledEntitySequence = LOTMCraft.NON_BEYONDER_SEQ;
        controlledEntity = null;
        bodyDouble = null;
        canUseOwnAbilities = true;
    }

    private static class AttributeSnapshot {
        final double baseValue;
        final List<AttributeModifier> modifiers;

        AttributeSnapshot(double baseValue, Collection<AttributeModifier> modifiers) {
            this.baseValue = baseValue;
            this.modifiers = new ArrayList<>(modifiers);
        }
    }


    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        tag.putBoolean("IsControlling", isControlling);
        tag.putDouble("SavedHealth", savedHealth);
        tag.put("SavedAttributes", writeAttributeSnapshots());

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        isControlling = tag.getBoolean("IsControlling");
        savedHealth = tag.getDouble("SavedHealth");
        readAttributeSnapshots(tag.getList("SavedAttributes", CompoundTag.TAG_COMPOUND));
    }

    private ListTag writeAttributeSnapshots() {
        ListTag list = new ListTag();
        for (Map.Entry<ResourceLocation, AttributeSnapshot> entry : savedAttributes.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("Attribute", entry.getKey().toString());
            entryTag.putDouble("BaseValue", entry.getValue().baseValue);

            ListTag modifiersTag = new ListTag();
            for (AttributeModifier modifier : entry.getValue().modifiers) {
                CompoundTag modifierTag = new CompoundTag();
                modifierTag.putString("Id", modifier.id().toString());
                modifierTag.putDouble("Amount", modifier.amount());
                modifierTag.putString("Operation", modifier.operation().name());
                modifiersTag.add(modifierTag);
            }
            entryTag.put("Modifiers", modifiersTag);

            list.add(entryTag);
        }
        return list;
    }

    private void readAttributeSnapshots(ListTag list) {
        savedAttributes.clear();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            ResourceLocation attributeId = ResourceLocation.parse(entryTag.getString("Attribute"));
            double baseValue = entryTag.getDouble("BaseValue");

            List<AttributeModifier> modifiers = new ArrayList<>();
            ListTag modifiersTag = entryTag.getList("Modifiers", CompoundTag.TAG_COMPOUND);
            for (int j = 0; j < modifiersTag.size(); j++) {
                CompoundTag modifierTag = modifiersTag.getCompound(j);
                ResourceLocation modifierId = ResourceLocation.parse(modifierTag.getString("Id"));
                double amount = modifierTag.getDouble("Amount");
                AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(modifierTag.getString("Operation"));
                modifiers.add(new AttributeModifier(modifierId, amount, operation));
            }

            savedAttributes.put(attributeId, new AttributeSnapshot(baseValue, modifiers));
        }
    }
}
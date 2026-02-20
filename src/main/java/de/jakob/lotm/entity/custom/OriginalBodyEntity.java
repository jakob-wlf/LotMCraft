package de.jakob.lotm.entity.custom;

import de.jakob.lotm.attachments.ModAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.*;

public class OriginalBodyEntity extends LivingEntity {
    private final Map<EquipmentSlot, ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);
    private ChunkPos lastLockedChunk = null;


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

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {

            ChunkPos currentChunk = new ChunkPos(this.blockPosition());

            if (lastLockedChunk == null || !currentChunk.equals(lastLockedChunk)) {

                // release the old chunk so it can unload normally
                if (lastLockedChunk != null) {
                    serverLevel.setChunkForced(lastLockedChunk.x, lastLockedChunk.z, false);
                }
                // force the new chunk to stay loaded
                serverLevel.setChunkForced(currentChunk.x, currentChunk.z, true);
                this.lastLockedChunk = currentChunk;
            }
        }
    }
    @Override
    public Component getCustomName() {
        String ownerName = this.getData(ModAttachments.CONTROLLING_DATA).getOwnerName();
        if (ownerName != null && !ownerName.isEmpty()){
            return Component.literal(ownerName);
        } else {
            return Component.translatable("entity.lotmcraft.original_body");
        }
    }
}
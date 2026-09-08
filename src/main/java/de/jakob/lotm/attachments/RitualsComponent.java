package de.jakob.lotm.attachments;

import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class RitualsComponent {
    private boolean isCompleted = false;
    private int stage = 0;

    public boolean isCompleted(){return isCompleted;}

    public void setCompleted(boolean value){ isCompleted = value;}

    public int getStage() {return stage;}

    public void setStage(int value) {stage = value;}

    public static final IAttachmentSerializer<CompoundTag, RitualsComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public RitualsComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    var component = new RitualsComponent();

                    component.isCompleted = tag.getBoolean("is_completed");
                    component.stage = tag.getInt("stage");

                    return component;
                }

                @Override
                public CompoundTag write(RitualsComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();

                    tag.putBoolean("is_completed", component.isCompleted);
                    tag.putInt("stage", component.stage);

                    return tag;
                }
            };
}

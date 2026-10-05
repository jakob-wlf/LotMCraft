package de.jakob.lotm.attachments;

import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.*;

public class RitualsComponent {
    private boolean isCompleted = false;
    private int stage = 0;
    private Vec3 pos = null;
    private Set<UUID> targetUuids = new HashSet<>();
    private Set<Integer> visitedBorders = new HashSet<>();

    public boolean isCompleted(){return isCompleted;}
    public void setCompleted(boolean value){ isCompleted = value;}

    public int getStage() {return stage;}
    public void setStage(int value) {stage = value;}

    public Vec3 getPos() {
        return pos;
    }
    public void setPos(Vec3 pos) {
        this.pos = pos;
    }

    public Set<UUID> getTargetUuids() { return targetUuids; }
    public void addTargetUuid(UUID uuid) { this.targetUuids.add(uuid); }
    public void clearTargetUuids() { this.targetUuids.clear(); }

    public Set<Integer> getVisitedBorders() { return visitedBorders; }
    public void addVisitedBorder(int border) { this.visitedBorders.add(border); }
    public void clearVisitedBorders() { this.visitedBorders.clear(); }

    public static final IAttachmentSerializer<CompoundTag, RitualsComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public RitualsComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    var component = new RitualsComponent();

                    component.isCompleted = tag.getBoolean("is_completed");
                    component.stage = tag.getInt("stage");

                    if (tag.contains("pos", Tag.TAG_COMPOUND)) {
                        CompoundTag posTag = tag.getCompound("pos");
                        component.pos = new Vec3(
                                posTag.getDouble("x"),
                                posTag.getDouble("y"),
                                posTag.getDouble("z")
                        );
                    }

                    if (tag.contains("target_uuids", Tag.TAG_LIST)) {
                        ListTag listTag = tag.getList("target_uuids", Tag.TAG_COMPOUND);
                        for (int i = 0; i < listTag.size(); i++) {
                            CompoundTag uuidTag = listTag.getCompound(i);
                            component.targetUuids.add(uuidTag.getUUID("uuid"));
                        }
                    }

                    if (tag.contains("visited_borders", Tag.TAG_INT_ARRAY)) {
                        for (int border : tag.getIntArray("visited_borders")) {
                            component.visitedBorders.add(border);
                        }
                    }

                    return component;
                }

                @Override
                public CompoundTag write(RitualsComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();

                    tag.putBoolean("is_completed", component.isCompleted);
                    tag.putInt("stage", component.stage);

                    if (component.pos != null) {
                        CompoundTag posTag = new CompoundTag();
                        posTag.putDouble("x", component.pos.x);
                        posTag.putDouble("y", component.pos.y);
                        posTag.putDouble("z", component.pos.z);
                        tag.put("pos", posTag);
                    }

                    ListTag listTag = new ListTag();
                    for (UUID uuid : component.targetUuids) {
                        CompoundTag uuidTag = new CompoundTag();
                        uuidTag.putUUID("uuid", uuid);
                        listTag.add(uuidTag);
                    }
                    tag.put("target_uuids", listTag);

                    int[] bordersArray = component.visitedBorders.stream().mapToInt(Integer::intValue).toArray();
                    tag.putIntArray("visited_borders", bordersArray);

                    return tag;
                }
            };
}

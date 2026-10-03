package de.jakob.lotm.attachments;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncSanityPacket;
import de.jakob.lotm.network.packets.toClient.SyncSkillScalingPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class SkillScalingComponent {
    private boolean scaleToSkill = false;
    private String path = "none";
    private int seq = LOTMCraft.NON_BEYONDER_SEQ;

    public SkillScalingComponent(){
    }

    public boolean getScaleToSkill() {
        return scaleToSkill;
    }

    public String getPath(){
        return path;
    }

    public int getSeq(){
        return seq;
    }

    public SkillScalingComponent setScalingToSkill(Boolean value){
        scaleToSkill = value;
        return this;
    }

    public SkillScalingComponent setPath(String value){
        path = value;
        return this;
    }

    public SkillScalingComponent setSeq(int value){
        seq = value;
        return this;
    }

    public void Sync(LivingEntity entity){
        if(!(entity instanceof ServerPlayer player)) return;

        PacketHandler.sendToPlayer(player, new SyncSkillScalingPacket(scaleToSkill, seq, path, entity.getId()));
    }

    public static final IAttachmentSerializer<CompoundTag, SkillScalingComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public SkillScalingComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    SkillScalingComponent component = new SkillScalingComponent();
                    component.scaleToSkill = tag.getBoolean("scaleToSkill");
                    component.path = tag.getString("path");
                    component.seq = tag.getInt("seq");
                    return component;
                }

                @Override
                public CompoundTag write(SkillScalingComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putBoolean("scaleToSkill", component.scaleToSkill);
                    tag.putString("path", component.path);
                    tag.putInt("seq", component.seq);
                    return tag;
                }
            };
}

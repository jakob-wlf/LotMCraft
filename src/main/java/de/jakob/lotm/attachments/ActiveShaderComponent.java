package de.jakob.lotm.attachments;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.SyncShaderPacket;
import de.jakob.lotm.network.packets.SyncTransformationPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class ActiveShaderComponent {

    private int shaderIndex = 0;
    private long shaderStopTime = 0;

    public ActiveShaderComponent() {
    }

    public boolean isShaderActive() {
        return shaderStopTime > System.currentTimeMillis();
    }

    public void setShaderActive(boolean shaderActive) {
        if (shaderActive) {
            shaderStopTime = System.currentTimeMillis() + 2000;
        } else {
            shaderStopTime = 0;
        }
    }

    public void setShaderActiveAndSync(boolean transformed, LivingEntity entity) {
        setShaderActive(transformed);
        PacketHandler.sendToAllPlayers(new SyncShaderPacket(entity.getId(), isShaderActive(), shaderIndex));
    }

    public int getShaderIndex() {
        return shaderIndex;
    }

    public void setShaderIndex(int shaderIndex) {
        this.shaderIndex = shaderIndex;
    }

    public void setShaderIndex(SHADERTYPE type) {
        this.shaderIndex = type.getIndex();
    }

    public void setShaderIndexAndSync(int transformationIndex, LivingEntity entity) {
        this.shaderIndex = transformationIndex;
        PacketHandler.sendToAllPlayers(new SyncShaderPacket(entity.getId(), isShaderActive(), transformationIndex));
    }

    public void setShaderIndexAndSync(SHADERTYPE type, LivingEntity entity) {
        this.shaderIndex = type.getIndex();
        PacketHandler.sendToAllPlayers(new SyncShaderPacket(entity.getId(), isShaderActive(), shaderIndex));
    }



    public static final IAttachmentSerializer<CompoundTag, ActiveShaderComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public ActiveShaderComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    ActiveShaderComponent component = new ActiveShaderComponent();
                    component.shaderStopTime = tag.getLong("shaderStopTime");
                    component.shaderIndex = tag.getInt("index");
                    return component;
                }

                @Override
                public CompoundTag write(ActiveShaderComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putLong("shaderStopTime", component.shaderStopTime);
                    tag.putInt("index", component.shaderIndex);
                    return tag;
                }
            };


    public enum SHADERTYPE {
        DROUGHT(0),
        BLIZZARD(1);

        private final int index;
        SHADERTYPE(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
}

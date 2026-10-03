package de.jakob.lotm.attachments;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncApotheosisPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class ApotheosisComponent implements INBTSerializable<CompoundTag> {
    private int apotheosisTicksLeft;
    private String pathway;

    public String getPathway() {
        return pathway;
    }

    public void setPathway(String pathway) {
        this.pathway = pathway;
    }

    public int getApotheosisTicksLeft() {
        return apotheosisTicksLeft;
    }

    public void setApotheosisTicksLeftAndSync(int apotheosisTicksLeft, ServerLevel level, Player player) {
        this.apotheosisTicksLeft = apotheosisTicksLeft;

        if(player == null) return;
        if(pathway == null) return;

        if(level != null) PacketHandler.sendToAllPlayersInSameLevel(new SyncApotheosisPacket(player.getId(), apotheosisTicksLeft, pathway), level);
    }

    public void setApotheosisTicksLeft(int apotheosisTicksLeft) {
        this.apotheosisTicksLeft = apotheosisTicksLeft;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        tag.putInt("apotheosisTicksLeft", apotheosisTicksLeft);
        tag.putString("pathway", pathway == null ? "" : pathway);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        this.apotheosisTicksLeft = tag.getInt("apotheosisTicksLeft");
        this.pathway = tag.getString("pathway");
    }
}

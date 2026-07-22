package de.jakob.lotm.attachments;

import com.mojang.datafixers.util.Pair;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncDiscernmentDataPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.LinkedList;
import java.util.List;

public class DiscernmentComponent {
    private boolean isDiscerning = false;
    private int previosSeq = 2;
    private int seq = LOTMCraft.NON_BEYONDER_SEQ;
    private String pathway = "none";

    private AbilityWheelComponent previousWheel ;
    private AbilityBarComponent previousBar;

    private LinkedList<Pair<
            Pair<String, Integer>,
            Pair<AbilityWheelComponent, AbilityBarComponent>
            >> saved = new LinkedList<>();

    public static final String NBT_IS_DISCERNING = "is_discerning";
    public static final String NBT_PREVIOS_SEQ = "previous_seq";
    public static final String NBT_SEQ = "seq";
    public static final String NBT_PATH = "path";
    public static final String NBT_SAVED = "saved_paths_seqs";

    public Pair<AbilityWheelComponent, AbilityBarComponent> getAbilitiesInBars(String path, int seq){
        return saved.stream().filter(o ->
                o.getFirst().getFirst().equals(path) && o.getFirst().getSecond().equals(seq)).toList().getFirst().getSecond();
    }

    public List<Pair<String, Integer>> getSavedPathsAndSeqs(){
        return saved.stream().map(Pair::getFirst).toList();
    }

    public boolean hasSaved(String path, int seq){
        return saved.stream().anyMatch(o -> o.getFirst().getFirst().equals(path) && o.getFirst().getSecond().equals(seq));
    }

    public void updateAbilitiesForCurrent(AbilityWheelComponent wheel, AbilityBarComponent bar){
        for (int i = 0; i < saved.size(); i++) {
            var entry = saved.get(i);

            if (entry.getFirst().getFirst().equals(pathway)
                    && entry.getFirst().getSecond().equals(seq)) {

                saved.set(i, Pair.of(
                        entry.getFirst(),
                        Pair.of(wheel.copy(), bar.copy())
                ));

                return;
            }
        }
    }

    public void add(String path, int seq){
        if(hasSaved(path, seq)) return;

        saved.push(Pair.of(
                Pair.of(path, seq),
                Pair.of(new AbilityWheelComponent(), new AbilityBarComponent())
                )
        );
    }

    public void remove(String path, int seq){
        Integer buff = null;

        for (int i = 0; i < saved.size(); i++) {
            var entry = saved.get(i);

            if (entry.getFirst().getFirst().equals(pathway)
                    && entry.getFirst().getSecond().equals(seq)) {

               buff = i;

                break;
            }
        }

        if(buff == null) return;

        saved.remove(buff.intValue());
    }

    public void clearAll(){
        saved.clear();
    }

    public boolean isDiscerning() {
        return isDiscerning;
    }

    public void setDiscerning(boolean discerning) {
        isDiscerning = discerning;
    }

    public int getPreviosSeq() {
        return previosSeq;
    }

    public void setPreviosSeq(int previosSeq) {
        this.previosSeq = previosSeq;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }


    public String getPathway() {
        return pathway;
    }

    public void setPathway(String pathway) {
        this.pathway = pathway;
    }

    public void syncData(ServerPlayer player) {
        if (player != null) {
            PacketHandler.sendToPlayer(player, new SyncDiscernmentDataPacket(this.isDiscerning, player.getId()));
        }
    }

    public static final IAttachmentSerializer<CompoundTag, DiscernmentComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public DiscernmentComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    DiscernmentComponent component = new DiscernmentComponent();

                    if(tag.getBoolean(NBT_IS_DISCERNING)) component.isDiscerning = tag.getBoolean(NBT_IS_DISCERNING);

                    if(tag.contains(NBT_PATH)) component.pathway = tag.getString(NBT_PATH);
                    if(tag.contains(NBT_SEQ)) component.seq = tag.getInt(NBT_SEQ);
                    if(tag.contains(NBT_PREVIOS_SEQ)) component.previosSeq = tag.getInt(NBT_PREVIOS_SEQ);

                    ListTag entries = tag.getList(NBT_SAVED, Tag.TAG_COMPOUND);

                    for (int i = 0; i < entries.size(); i++) {
                        CompoundTag entryTag = entries.getCompound(i);

                        String path = entryTag.getString("path");
                        int seq = entryTag.getInt("seq");

                        CompoundTag wheelTag = entryTag.getCompound("wheel");
                        CompoundTag barTag = entryTag.getCompound("bar");

                        AbilityWheelComponent wheel = new AbilityWheelComponent();
                        wheel.deserializeNBT(lookup, wheelTag);

                        AbilityBarComponent bar = new AbilityBarComponent();
                        bar.deserializeNBT(lookup, barTag);

                        component.saved.add(Pair.of(
                                Pair.of(path, seq),
                                Pair.of(wheel, bar)
                        ));
                    }
                    return component;
                }

                @Override
                public CompoundTag write(DiscernmentComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();

                    tag.putBoolean(NBT_IS_DISCERNING, component.isDiscerning);
                    tag.putString(NBT_PATH, component.pathway);
                    tag.putInt(NBT_SEQ, component.seq);
                    tag.putInt(NBT_PREVIOS_SEQ, component.previosSeq);

                    ListTag entries = new ListTag();

                    for (var entry : component.saved) {

                        CompoundTag entryTag = new CompoundTag();

                        Pair<String, Integer> info = entry.getFirst();

                        entryTag.putString("path", info.getFirst());
                        entryTag.putInt("seq", info.getSecond());

                        Pair<AbilityWheelComponent, AbilityBarComponent> components = entry.getSecond();

                        CompoundTag wheelTag = components.getFirst().serializeNBT(lookup);
                        CompoundTag barTag = components.getSecond().serializeNBT(lookup);

                        entryTag.put("wheel", wheelTag);
                        entryTag.put("bar", barTag);

                        entries.add(entryTag);
                    }

                    tag.put(NBT_SAVED, entries);

                    return tag;
                }
            };

    public AbilityWheelComponent getPreviousWheel() {
        return previousWheel;
    }

    public void setPreviousWheel(AbilityWheelComponent previousWheel) {
        this.previousWheel = previousWheel;
    }

    public AbilityBarComponent getPreviousBar() {
        return previousBar;
    }

    public void setPreviousBar(AbilityBarComponent previousBar) {
        this.previousBar = previousBar;
    }
}

package de.jakob.lotm.util.playerMap;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.Prophecy;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;

public class StoredDataBuilder {
    private String pathway;
    private Integer sequence;
    private HonorificName honorificName;
    private String trueName;
    private Boolean modified;
    private Vec3 lastPosition;
    private ArrayList<Characteristic> charList;
    private String[] pathwayHistory;
    private LinkedList<Prophecy> prophecyList;
    private String uniqueness;
    private String sefirot;

    public StoredDataBuilder(){
        clean();
    }

    private void clean(){
        pathway = "none";
        sequence = LOTMCraft.NON_BEYONDER_SEQ;
        honorificName = HonorificName.EMPTY;
        trueName = "none";
        modified = false;
        lastPosition = new Vec3(0, 0, 0);
        charList = new ArrayList<Characteristic>();
        pathwayHistory = new String[10];
        prophecyList = new LinkedList<>();
        uniqueness = "none";
        sefirot = "";
    }

    public StoredDataBuilder copyFrom(@Nullable StoredData data){
        clean();

        if(data != null){
            pathway = data.pathway();
            sequence = data.sequence();
            honorificName = data.honorificName();
            trueName = data.trueName();
            modified = data.modified();
            lastPosition = data.lastPosition();
            charList = new ArrayList<>(data.chars());
            pathwayHistory = Arrays.copyOf(data.pathwayHistory(), 10);
            prophecyList = data.prophecies();
            sefirot = data.claimedSefirot();
        }

        return this;
    }

    public StoredDataBuilder pathway(String path){
        pathway = path;
        return this;
    }

    public StoredDataBuilder sequence(Integer seq){
        sequence = seq;
        return this;
    }

    public StoredDataBuilder honorificName(HonorificName name){
        honorificName = name;
        return this;
    }

    public StoredDataBuilder trueName(String name){
        trueName = name;
        return this;
    }


    public StoredDataBuilder modified(Boolean bool){
        modified = bool;
        return this;
    }

    public StoredDataBuilder lastPosition(Vec3 vec){
        lastPosition = vec;
        return this;
    }

    public StoredDataBuilder characteristic(int stack, int sequence, String pathway) {
        Characteristic target = null;
        for (Characteristic characteristic : charList) {
            if (Objects.equals(characteristic.pathway(), pathway) && characteristic.sequence() == sequence) {
                target = characteristic;
                break;
            }
        }

        if (target != null) {
            if (stack <= 0) {
                charList.remove(target);
            } else {
                target.setStack(stack);
            }
        } else if (stack > 0) {
            charList.add(new Characteristic(pathway, stack, sequence));
        }

        return this;
    }

    public StoredDataBuilder charList(ArrayList<Characteristic> charList){
        this.charList = charList == null ? new ArrayList<>() : new ArrayList<>(charList);
        return this;
    }

    public StoredDataBuilder clearCharList(){
        charList = new ArrayList<>();
        return this;
    }


    public StoredDataBuilder uniqueness(String uniqueness) {
        this.uniqueness = uniqueness;
        return this;
    }

    public StoredDataBuilder pathwayHistory(String[] history) {
        pathwayHistory = Arrays.copyOf(history, 10);
        return this;
    }

    public StoredDataBuilder prophecies(LinkedList<Prophecy> list){
        prophecyList = list;
        return this;
    }

    public StoredDataBuilder sefirot(String value){
        sefirot = value;
        return this;
    }

    public StoredData build(){
        if (!charList.isEmpty()) {
            int minSeq = charList.stream().mapToInt(Characteristic::sequence).min().orElse(LOTMCraft.NON_BEYONDER_SEQ);
            this.sequence = minSeq;

            boolean currentMatches = false;
            for (Characteristic c : charList) {
                if (c.sequence() == minSeq && c.pathway().equals(this.pathway)) {
                    currentMatches = true;
                    break;
                }
            }

            if (!currentMatches) {
                for (Characteristic c : charList) {
                    if (c.sequence() == minSeq) {
                        this.pathway = c.pathway();
                        break;
                    }
                }
            }
        } else {
            this.pathway = "none";
            this.sequence = LOTMCraft.NON_BEYONDER_SEQ;
        }

        StoredData buff = new StoredData(pathway, sequence,
                honorificName, trueName, modified,
                lastPosition, charList, pathwayHistory, uniqueness,
                prophecyList, sefirot);

        clean();

        return buff;
    }
}

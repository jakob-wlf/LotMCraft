package de.jakob.lotm.util.playerMap;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.visionary.prophecy.Prophecy;
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
    private int[] charStack;
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
        charStack = new int[11];
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
            charStack = Arrays.copyOf(data.charStack(), 10);
            charList = data.chars();
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

    public StoredDataBuilder charStack(int stack, int sequence, String pathway) {
        if(sequence >= 0 && sequence < 10)
            charStack[sequence] = stack;

        for (Characteristic characteristic : charList) {
            if (Objects.equals(characteristic.pathway(), pathway)) {
                characteristic.setStack(stack);
                return this;
            }
        }
        charList.add(new Characteristic(pathway, stack, sequence));

        return this;
    }

    public StoredDataBuilder charList(ArrayList<Characteristic> charList){
        this.charList = charList;
        return this;
    }

    public StoredDataBuilder clearCharList(){
        charList = new ArrayList<>();
        return this;
    }

    public StoredDataBuilder clearCharStack() {
        charStack = new int[10];
        charList = new ArrayList<>();
        return this;
    }

    public StoredDataBuilder uniqueness(String uniqueness) {
        this.uniqueness = uniqueness;
        return this;
    }

    public StoredDataBuilder charStackArray(int[] stack) {
        this.charStack = Arrays.copyOf(stack, 10);
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
        StoredData buff = new StoredData(pathway, sequence,
                honorificName, trueName, modified,
                lastPosition, charStack, charList, pathwayHistory, uniqueness,
                prophecyList, sefirot);

        clean();

        return buff;
    }
}

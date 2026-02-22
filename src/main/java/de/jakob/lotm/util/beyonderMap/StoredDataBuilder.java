package de.jakob.lotm.util.beyonderMap;

import de.jakob.lotm.LOTMCraft;

import javax.annotation.Nullable;
import java.util.LinkedList;

public class StoredDataBuilder {
    private String pathway;
    private Integer sequence;
    private HonorificName honorificName;
    private String trueName;
    private LinkedList<MessageType> msgs;
    private LinkedList<HonorificName> knownNames;
    private Boolean modified;

    public StoredDataBuilder(){
        clean();
    }

    private void clean(){
        pathway = "none";
        sequence = LOTMCraft.NON_BEYONDER_SEQ;
        honorificName = HonorificName.EMPTY;
        trueName = "none";
        msgs = new LinkedList<>();
        knownNames = new LinkedList<>();
        modified = false;
    }

    public StoredDataBuilder copyFrom(@Nullable StoredData data){
        clean();

        if(data != null){
            pathway = data.pathway();
            sequence = data.sequence();
            honorificName = data.honorificName();
            trueName = data.trueName();
            msgs = data.msgs();
            knownNames = data.knownNames();
            modified = data.modified();
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

    public StoredDataBuilder msgs(LinkedList<MessageType> list){
        msgs = list;
        return this;
    }

    public StoredDataBuilder knownNames(LinkedList<HonorificName> list){
        knownNames = list;
        return this;
    }

    public StoredDataBuilder modified(Boolean bool){
        modified = bool;
        return this;
    }

    public StoredData build(){
        StoredData buff = new StoredData(pathway, sequence,
                honorificName, trueName,
                msgs, knownNames, modified);

        clean();

        return buff;
    }
}

package de.jakob.lotm.util.playerMap;

public class Characteristic {
    private final String pathway;
    private int stack;
    private final int sequence;
    public Characteristic(String pathway, int stack, int sequence){
        this.pathway = pathway;
        this.stack = stack;
        this.sequence = sequence;
    }

    public String pathway(){
        return pathway;
    }

    public void setStack(int stack){
        this.stack = stack;
    }

    public int stack(){
        return stack;
    }

    public int sequence(){
        return sequence;
    }

}

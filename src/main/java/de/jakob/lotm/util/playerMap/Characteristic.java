package de.jakob.lotm.util.playerMap;

public class Characteristic {
    private String pathway;
    private int ammount;
    private int sequence;
    public Characteristic(String patheway, int ammount, int sequence){
        this.pathway = patheway;
        this.ammount = ammount;
        this.sequence = sequence;
    }

    public String Pathway(){
        return pathway;
    }

    public void setAmmount(int ammount){
        this.ammount = ammount;
    }

    public int Ammount(){
        return ammount;
    }

    public int Sequence(){
        return sequence;
    }

}

package de.jakob.lotm.beyonders.abilities.abyss.contract.builder;

public class BuilderToken {
    public final ContractTermKind kind;
    public String value;
    public BuilderToken(ContractTermKind kind, String value) {
        this.kind = kind;
        this.value = value == null ? "" : value;
    }
}
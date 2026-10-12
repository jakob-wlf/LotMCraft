package de.jakob.lotm.beyonders.abilities.abyss.contract.builder;

public enum ContractTermKind {
    PARTY_A("Party A", false),
    PARTY_B("Party B", false),
    GIVES("Gives", false),
    GETS("Gets", false),
    FOR("For", false),
    FROM("From", false),
    RETURNS("Returns", false),
    TO("To", false),
    KILLS("Kills", false),
    VICTIM("Victim", true),
    BIND_LIFE_DEATH("Bind Life & Death", false),
    SOUL("Soul", false),
    RESOURCE("Resource", true),
    AMOUNT("Amount", true),
    TIME_UNIT("Time Unit", true),
    UNTIL("Until", false),
    PASSES("Passes", false),
    DIGEST("Digest", false),
    ENTITY_TYPE("Entity Type", true);

    public final String label;
    public final boolean isInput;

    ContractTermKind(String label, boolean isInput) {
        this.label = label;
        this.isInput = isInput;
    }
}
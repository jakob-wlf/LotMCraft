package de.jakob.lotm.beyonders.abilities.abyss.contract;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class ContractData {
    // TERM TYPES
    public enum TermType {
        NONE,
        ITEM,
        SOUL,
        RELEASE_SOUL,
        DIGEST
    }
    // STORAGE
    private final CompoundTag tag;
    private ContractData(CompoundTag tag) {
        this.tag = tag;
    }
    public static ContractData empty() {
        return new ContractData(new CompoundTag());
    }
    public static ContractData of(CompoundTag tag) {
        return new ContractData(tag);
    }
    public CompoundTag toTag() {
        return tag;
    }
    // PARTIES

    public UUID getPartyA() {return tag.hasUUID("partyA") ? tag.getUUID("partyA") : null;}
    public void setPartyA(UUID uuid) {
        if (uuid == null) {tag.remove("partyA");}
        else {tag.putUUID("partyA", uuid);}
    }
    public UUID getPartyB() {return tag.hasUUID("partyB") ? tag.getUUID("partyB") : null;
    }
    public void setPartyB(UUID uuid) {
        if (uuid == null) {tag.remove("partyB");}
        else {tag.putUUID("partyB", uuid);}
    }
    public boolean hasBothParties() {
        return getPartyA() != null && getPartyB() != null;
    }
    // PARTY NAMES
    public String partyAName() {
        return tag.getString("partyAName");
    }
    public void setPartyAName(String name) {
        tag.putString("partyAName", name == null ? "" : name);
    }
    public String partyBName() {
        return tag.getString("partyBName");
    }
    public void setPartyBName(String name) {
        tag.putString("partyBName", name == null ? "" : name);
    }

    // TERM A ALWAYS MEANS:PARTY B -> PARTY A
    public TermType termAType() {
        String type = tag.getString("termAType");
        if (type.isEmpty()) {return TermType.NONE;}
        try {return TermType.valueOf(type);}
        catch (IllegalArgumentException ignored) {return TermType.NONE;}
    }
    public void setTermAType(TermType type) {tag.putString("termAType", type == null ? TermType.NONE.name() : type.name());}
    public String termAItem() {
        return tag.getString("termAItem");
    }
    public void setTermAItem(String id) {
        tag.putString("termAItem", id == null ? "" : id);
    }
    public int termACount() {
        return Math.max(1, tag.getInt("termACount"));
    }
    public void setTermACount(int count) {
        tag.putInt("termACount", Math.max(1, count));
    }
    public boolean termAFulfilled() {
        return tag.getBoolean("termAFulfilled");
    }
    public void setTermAFulfilled(boolean value) {
        tag.putBoolean("termAFulfilled", value);
    }

    //For SOUL terms:true=Party A owns the soul/false = Party B owns the soul
    public boolean termASoulOwnerIsPartyA() {return tag.getBoolean("termASoulOwnerIsPartyA");}
    public void setTermASoulOwnerIsPartyA(boolean value) {tag.putBoolean("termASoulOwnerIsPartyA", value);}
    // TERM B ALWAYS MEANS:PARTY A -> PARTY B
    public TermType termBType() {
        String type = tag.getString("termBType");
        if (type.isEmpty()) {return TermType.NONE;}
        try {return TermType.valueOf(type);}
        catch (IllegalArgumentException ignored) {return TermType.NONE;}
    }
    public void setTermBType(TermType type) {tag.putString("termBType", type == null ? TermType.NONE.name() : type.name());}
    public String termBItem() {return tag.getString("termBItem");}
    public void setTermBItem(String id) {tag.putString("termBItem", id == null ? "" : id);}
    public int termBCount() {return Math.max(1, tag.getInt("termBCount"));}
    public void setTermBCount(int count) {tag.putInt("termBCount", Math.max(1, count));}
    public boolean termBFulfilled() {return tag.getBoolean("termBFulfilled");}
    public void setTermBFulfilled(boolean value) {tag.putBoolean("termBFulfilled", value);}
    //For SOUL terms: true  = Party A owns the soul/false = Party B owns the soul
    public boolean termBSoulOwnerIsPartyA() {
        return tag.getBoolean("termBSoulOwnerIsPartyA");
    }
    public void setTermBSoulOwnerIsPartyA(boolean value) {
        tag.putBoolean("termBSoulOwnerIsPartyA", value);
    }
    // CONTRACT STATE

    public boolean isActive() {
        return tag.getBoolean("active");
    }
    public void setActive(boolean value) {
        tag.putBoolean("active", value);
    }
    public boolean isComplete() {
        if (!hasBothParties()) {return false;}
        return tag.getBoolean("complete");
    }
    public void setComplete(boolean value) {
        tag.putBoolean("complete", value);
    }

    public boolean isTermAFulfilled() {
        return termAType() == TermType.NONE || termAFulfilled();
    }
    public boolean isTermBFulfilled() {
        return termBType() == TermType.NONE || termBFulfilled();
    }
    public boolean areAllTermsFulfilled() {
        return isTermAFulfilled() && isTermBFulfilled();
    }
    // CONTRACT IDENTITY
    public UUID contractUUID() {return tag.hasUUID("contractUUID") ? tag.getUUID("contractUUID") : null;}
    public void setContractUUID(UUID uuid) {
        if (uuid == null) {tag.remove("contractUUID");}
        else {tag.putUUID("contractUUID", uuid);}
    }
    public String contractKind() {
        return tag.getString("contractKind");
    }
    public void setContractKind(String kind) {
        tag.putString("contractKind", kind == null ? "" : kind);
    }
    public int casterSeqAtCreation() {
        return tag.getInt("casterSeqAtCreation");
    }
    public void setCasterSeqAtCreation(int seq) {
        tag.putInt("casterSeqAtCreation", seq);
    }
    // KILL CONTRACT
    public boolean isKillContract() {
        return "KILL".equalsIgnoreCase(contractKind());
    }
    public boolean isKillerPartyA() {
        return tag.getBoolean("killerIsPartyA");
    }
    public void setKillerIsPartyA(boolean value) {
        tag.putBoolean("killerIsPartyA", value);
    }
    public String killerName() {
        return tag.getString("killerName");
    }
    public void setKillerName(String name) {
        tag.putString("killerName", name == null ? "" : name);
    }
    public String victimName() {
        return tag.getString("victimName");
    }
    public void setVictimName(String name) {
        tag.putString("victimName", name == null ? "" : name);
    }
    public boolean killPaidOut() {
        return tag.getBoolean("killPaidOut");
    }
    public void setKillPaidOut(boolean value) {
        tag.putBoolean("killPaidOut", value);
    }
    public boolean isKillPayerPartyA() {
        return tag.getBoolean("killPayerIsPartyA");
    }
    public void setKillPayerIsPartyA(boolean value) {
        tag.putBoolean("killPayerIsPartyA", value);
    }
    public String victimEntityType() {
        String type = tag.getString("victimEntityType");
        if (type == null || type.isBlank()) {
            return "ALL";
        }
        return type.toUpperCase(java.util.Locale.ROOT);
    }
    public void setVictimEntityType(String type) {
        if (type == null || type.isBlank()) {
            tag.putString("victimEntityType", "ALL");
            return;
        }
        String normalized = type.toUpperCase(java.util.Locale.ROOT);
        if (!normalized.equals("ALL") && !normalized.equals("MOB") && !normalized.equals("PLAYER")) {normalized = "ALL";}
        tag.putString("victimEntityType", normalized);
    }
    public String payerName() {
        return isKillPayerPartyA() ? partyAName() : partyBName();
    }
    // LIFE / DEATH CONTRACT
    public boolean isLifeDeathContract() {
        return "LIFE_DEATH".equalsIgnoreCase(contractKind());
    }
    //Duration in Minecraft ticks,-1 means no expiry.
    public long lifeDeathDurationTicks() {return tag.contains("lifeDeathDuration") ? tag.getLong("lifeDeathDuration") : -1;}
    public void setLifeDeathDurationTicks(long ticks) {tag.putLong("lifeDeathDuration", ticks);}
    public long lifeDeathStartGameTime() {return tag.contains("lifeDeathStart") ? tag.getLong("lifeDeathStart") : -1L;}
    public void setLifeDeathStartGameTime(long time) {tag.putLong("lifeDeathStart", time);}
    public boolean lifeDeathExpired() {return tag.getBoolean("lifeDeathExpired");}
    public void setLifeDeathExpired(boolean value) {tag.putBoolean("lifeDeathExpired", value);}
    // SOUL / IOU STATE
    public boolean isIou() {return tag.getBoolean("isIou");}
    public void setIou(boolean value) {tag.putBoolean("isIou", value);}
    public String releaseTargetName() {return tag.getString("releaseTargetName");}
    public void setReleaseTargetName(String name) {tag.putString("releaseTargetName", name == null ? "" : name);}
    // CURSE STATE
    public long curseLastUseGameTime() {return tag.getLong("curseLastUse");}
    public void setCurseLastUseGameTime(long time) {tag.putLong("curseLastUse", time);}
    @Deprecated
    public boolean termAFromPartyA() {
        return false;
    }
    @Deprecated
    public boolean termBFromPartyA() {
        return true;
    }
    @Deprecated
    public boolean termAOwnerIsPartyA() {
        return termASoulOwnerIsPartyA();
    }
    @Deprecated
    public boolean termBOwnerIsPartyA() {
        return termBSoulOwnerIsPartyA();
    }
    // SOUL HELPERS
    public UUID getSoulOwner(boolean termA) {
        boolean ownerIsPartyA = termA ? termASoulOwnerIsPartyA() : termBSoulOwnerIsPartyA();
        return ownerIsPartyA ? getPartyA() : getPartyB();
    }
    public String getSoulOwnerName(boolean termA) {
        boolean ownerIsPartyA = termA ? termASoulOwnerIsPartyA() : termBSoulOwnerIsPartyA();
        return ownerIsPartyA ? partyAName() : partyBName();
    }
    // TERM DIRECTION HELPERS
    public UUID getTermSender(boolean termA) {return termA ? getPartyB() : getPartyA();}
    public UUID getTermRecipient(boolean termA) {return termA ? getPartyA() : getPartyB();}

    // TERM DESCRIPTION DATA
    public boolean hasTermA() {return termAType() != TermType.NONE;}
    public boolean hasTermB() {return termBType() != TermType.NONE;}
    public boolean hasSoulTermA() {return termAType() == TermType.SOUL;}
    public boolean hasSoulTermB() {return termBType() == TermType.SOUL;}
    public boolean hasAnySoulTerm() {return hasSoulTermA() || hasSoulTermB();}

    public String describe() {
        if ("KILL".equals(contractKind())) {
            String status;
            if (isComplete()) {
                status = " (Fulfilled)";
            } else if (killPaidOut() && termAFulfilled()) {
                status = " (Kill Complete - Payment Complete)";
            } else if (killPaidOut()) {
                status = " (Kill Complete - Payment Pending)";
            } else if (termAFulfilled()) {
                status = " (Payment Complete - Kill Pending)";
            } else {
                status = " (Pending Kill and Payment)";
            }
            return killerName()
                    + " kills "
                    + victimName()
                    + status
                    + "\nPaid by "
                    + payerName()
                    + ": "
                    + describeTerm(termAType(), termAItem(), termACount())
                    + (termAFulfilled() ? " (Paid)" : " (Unpaid)");
        }
        StringBuilder sb = new StringBuilder();
        if (hasTermB()) {
            sb.append(partyAName())
                    .append(" → ")
                    .append(partyBName())
                    .append(": ")
                    .append(describeTerm(termBType(), termBItem(), termBCount()));
        }
        if (hasTermA()) {
            if (!sb.isEmpty()) {sb.append("\n");}
            sb.append(partyBName())
                    .append(" → ")
                    .append(partyAName())
                    .append(": ")
                    .append(describeTerm(termAType(), termAItem(), termACount()));
        }
        if (sb.isEmpty()) {return "No terms";}
        return sb.toString();
    }
    public static String describeTerm(TermType type, String item, int count) {
        return switch (type) {
            case SOUL -> "their soul";
            case RELEASE_SOUL -> "the soul of " + item;
            case ITEM -> count + "x " + prettify(item);
            case NONE -> "nothing";
            case DIGEST -> count + "% digestion progress";
        };
    }
    public static String prettify(String itemId) {
        if (itemId == null || itemId.isBlank()) {return "Unknown";}
        String path = itemId.contains(":") ? itemId.substring(itemId.indexOf(':') + 1) : itemId;
        String[] words = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {continue;}
            if (!sb.isEmpty()) {sb.append(' ');}
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }
}
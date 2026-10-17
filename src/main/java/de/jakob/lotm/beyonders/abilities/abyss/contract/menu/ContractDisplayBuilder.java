package de.jakob.lotm.beyonders.abilities.abyss.contract.menu;

import de.jakob.lotm.beyonders.abilities.abyss.contract.ContractData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class ContractDisplayBuilder {

    private ContractDisplayBuilder() {
    }

    public static ContractDisplay build(ContractData data) {
        String kind = data.contractKind().isEmpty() ? "STANDARD" : data.contractKind().toUpperCase();
        String status = getStatus(data);
        return switch (kind) {
            case "KILL" -> buildKill(data, status);
            case "LIFE_DEATH" -> buildLifeDeath(data, status);
            default -> buildStandard(data, status);
        };
    }
    // STANDARD
    private static ContractDisplay buildStandard(ContractData data, String status) {
        ContractDisplay.Builder builder = ContractDisplay.builder(Component.literal("[Contract] " + status).withStyle(ChatFormatting.GOLD));
        String termB = describeTransfer(data, false);
        String termA = describeTransfer(data, true);
        if (termB != null) {
            builder.line(Component.literal(termB));
            builder.line(statusLine(data.termBFulfilled()));
        }

        if (termA != null) {
            if (termB != null) {builder.blank();}
            builder.line(Component.literal(termA));
            builder.line(statusLine(data.termAFulfilled()));
        }
        if (termB == null && termA == null) {builder.line(Component.literal("No obligations").withStyle(ChatFormatting.GRAY));}
        return builder.build();
    }
    // KILL
    private static ContractDisplay buildKill(ContractData data, String status) {
        ContractDisplay.Builder builder = ContractDisplay.builder(Component.literal("[Killing Contract] " + status).withStyle(ChatFormatting.GOLD));
        String killer = data.isKillerPartyA() ? safeName(data.partyAName(), "Party A") : safeName(data.partyBName(), "Party B");
        String victim = safeName(data.victimName(), "Unknown");
        builder.line(Component.literal(killer + " kills " + victim));
        builder.line(Component.literal("Status: ").append(Component.literal(data.killPaidOut() ? "Kill Complete" : "Pending Kill").withStyle(data.killPaidOut() ? ChatFormatting.GREEN : ChatFormatting.YELLOW)));
        if (data.termAType() != ContractData.TermType.NONE) {
            builder.blank();
            String payer = data.isKillerPartyA() ? safeName(data.partyBName(), "Party B") : safeName(data.partyAName(), "Party A");
            String reward = describeKillReward(data);
            builder.line(Component.literal("Paid by: " + payer).withStyle(ChatFormatting.AQUA));
            builder.line(Component.literal("Reward: " + reward));
            builder.line(Component.literal("Payment: ").append(Component.literal(data.termAFulfilled() ? "Paid" : "Pending").withStyle(data.termAFulfilled() ? ChatFormatting.GREEN : ChatFormatting.YELLOW)));
        } else {
            builder.blank();
            builder.line(Component.literal("No payment").withStyle(ChatFormatting.GRAY));
        }
        return builder.build();
    }

    private static String describeKillReward(ContractData data) {
        String payer = data.isKillerPartyA() ? safeName(data.partyBName(), "Party B") : safeName(data.partyAName(), "Party A");
        String killer = data.isKillerPartyA() ? safeName(data.partyAName(), "Party A") : safeName(data.partyBName(), "Party B");
        String reward = describeTerm(data, true, payer, killer);
        return payer + " → " + killer + ": " + reward;
    }
    // LIFE / DEATH
    private static ContractDisplay buildLifeDeath(ContractData data, String status) {
        ContractDisplay.Builder builder = ContractDisplay.builder(Component.literal("[Life-or-Death Contract] " + status).withStyle(ChatFormatting.GOLD));
        String partyA = safeName(data.partyAName(), "Party A");
        String partyB = safeName(data.partyBName(), "Party B");
        builder.line(Component.literal(partyA + " ↔ " + partyB));
        builder.line(Component.literal("Linked lives").withStyle(ChatFormatting.LIGHT_PURPLE));

        String termB = describeLifeDeathTransfer(data, false);
        String termA = describeLifeDeathTransfer(data, true);

        if (termB != null) {
            builder.blank();
            builder.line(Component.literal(termB));
            builder.line(statusLine(data.termBFulfilled()));
        }

        if (termA != null) {
            if (termB == null) {builder.blank();}
            else {builder.blank();}
            builder.line(Component.literal(termA));
            builder.line(statusLine(data.termAFulfilled()));
        }
        if (termA == null && termB == null) {
            builder.blank();
            builder.line(Component.literal("No exchange").withStyle(ChatFormatting.GRAY));
        }
        builder.blank();
        builder.line(Component.literal(formatDuration(data.lifeDeathDurationTicks())).withStyle(ChatFormatting.GRAY));
        return builder.build();
    }

    // TRANSFERS
    private static String describeTransfer(ContractData data, boolean termA) {
        ContractData.TermType type = termA ? data.termAType() : data.termBType();
        if (type == ContractData.TermType.NONE) {return null;}
        String recipient = termA ? safeName(data.partyAName(), "Party A") : safeName(data.partyBName(), "Party B");
        String description;
        if (type == ContractData.TermType.SOUL) {
            boolean ownerIsPartyA = termA ? data.termASoulOwnerIsPartyA() : data.termBSoulOwnerIsPartyA();
            String owner = ownerIsPartyA ? safeName(data.partyAName(), "Party A") : safeName(data.partyBName(), "Party B");
            description = owner + "'s Soul";
            return owner + " → " + recipient + ": " + description;
        }
        String sender = termA ? safeName(data.partyBName(), "Party B") : safeName(data.partyAName(), "Party A");
        description = describeTerm(data, termA, sender, recipient);
        return sender + " → " + recipient + ": " + description;
    }

    private static String describeLifeDeathTransfer(ContractData data, boolean termA) {
        ContractData.TermType type = termA ? data.termAType() : data.termBType();
        if (type == ContractData.TermType.NONE) {
            return null;
        }
        if (type == ContractData.TermType.SOUL) {
            boolean ownerIsPartyA = termA ? data.termASoulOwnerIsPartyA() : data.termBSoulOwnerIsPartyA();
            String owner = ownerIsPartyA ? safeName(data.partyAName(), "Party A") : safeName(data.partyBName(), "Party B");
            String recipient = ownerIsPartyA ? safeName(data.partyBName(), "Party B") : safeName(data.partyAName(), "Party A");
            return owner + "'s Soul → " + recipient;
        }
        String sender = termA ? safeName(data.partyBName(), "Party B") : safeName(data.partyAName(), "Party A");
        String recipient = termA ? safeName(data.partyAName(), "Party A") : safeName(data.partyBName(), "Party B");
        return sender + " → " + recipient + ": " + describeTerm(data, termA, sender, recipient);
    }
    // TERM DESCRIPTION
    private static String describeTerm(ContractData data, boolean termA, String sender, String recipient) {
        ContractData.TermType type = termA ? data.termAType() : data.termBType();
        String item = termA ? data.termAItem() : data.termBItem();
        int count = termA ? data.termACount() : data.termBCount();
        return switch (type) {
            case ITEM -> count + "x " + ContractData.prettify(item);

            case DIGEST -> count + "% digestion progress";
            case SOUL -> {
                boolean ownerIsPartyA = termA ? data.termASoulOwnerIsPartyA() : data.termBSoulOwnerIsPartyA();
                String owner = ownerIsPartyA ? safeName(data.partyAName(), "Party A") : safeName(data.partyBName(), "Party B");
                yield owner + "'s Soul";
            }
            case RELEASE_SOUL -> "the soul of " + item;
            case NONE -> "nothing";
        };
    }
    // STATUS

    private static String getStatus(ContractData data) {
        if (!data.isActive() && data.isComplete()) {return "Fulfilled";}
        if (!data.isActive()) {return "Void";}
        return "Active";
    }

    private static Component statusLine(boolean fulfilled) {
        return Component.literal(fulfilled ? "Fulfilled" : "Pending").withStyle(fulfilled ? ChatFormatting.GREEN : ChatFormatting.YELLOW);
    }
    // DURATION
    private static String formatDuration(long ticks) {
        if (ticks < 0) {return "No expiry";}
        long seconds = ticks / 20;
        if (seconds % 86400 == 0) {
            long days = seconds / 86400;
            return "Expires in " + days + (days == 1 ? " day" : " days");
        }
        if (seconds % 3600 == 0) {
            long hours = seconds / 3600;
            return "Expires in " + hours + (hours == 1 ? " hour" : " hours");
        }
        if (seconds % 60 == 0) {
            long minutes = seconds / 60;
            return "Expires in " + minutes + (minutes == 1 ? " minute" : " minutes");
        }
        return "Expires in " + seconds + (seconds == 1 ? " second" : " seconds");
    }
    // UTIL
    private static String safeName(String name, String fallback) {
        return name == null || name.isBlank() ? fallback : name;
    }
}
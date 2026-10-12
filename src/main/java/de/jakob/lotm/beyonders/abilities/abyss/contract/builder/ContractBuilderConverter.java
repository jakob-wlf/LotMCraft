package de.jakob.lotm.beyonders.abilities.abyss.contract.builder;

import de.jakob.lotm.beyonders.abilities.abyss.contract.*;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;

public class ContractBuilderConverter {

    public static void submit(ServerPlayer caster, List<BuilderToken> tokens, int signMode) {
        if (tokens.isEmpty()) {
            AbilityUtil.sendActionBar(caster, Component.literal("The contract is empty.").withColor(0xFFff124d));
            return;
        }
        Result result = match(tokens);
        if (result == null) {
            AbilityUtil.sendActionBar(caster, Component.literal("The Abyss cannot find any shape in this.").withColor(0xFFff124d));
            return;
        }
        ContractData data = result.data;
        data.setContractUUID(UUID.randomUUID());
        data.setCasterSeqAtCreation(BeyonderData.getSequence(caster));
        data.setActive(true);
        if (signMode == 1) {
            data.setPartyA(caster.getUUID());
            data.setPartyAName(caster.getName().getString());
        } else if (signMode == 2) {
            data.setPartyB(caster.getUUID());
            data.setPartyBName(caster.getName().getString());
        }

        if (data.hasBothParties()) {
            if (!ContractFinalizer.activate(data)) {
                AbilityUtil.sendActionBar(caster, Component.literal("The contract could not be formed.").withColor(0xFF124D));
                return;
            }
        }
        var contractItem = ContractItem.create(data);
        if (!caster.getInventory().add(contractItem)) {
            caster.drop(contractItem, false);
        }
        AbilityUtil.sendActionBar(caster, Component.literal(data.hasBothParties() ? "Contract formed." : "Contract created — right-click players to assign the open roles.").withColor(0x9932cc)
        );
    }
    static Result match(List<BuilderToken> t) {
        int verbIdx = -1;
        ContractTermKind verb = null;
        for (int i = 0; i < t.size(); i++) {
            ContractTermKind k = t.get(i).kind;
            if (k == ContractTermKind.GIVES || k == ContractTermKind.GETS || k == ContractTermKind.RETURNS || k == ContractTermKind.KILLS || k == ContractTermKind.BIND_LIFE_DEATH) {
                verbIdx = i;
                verb = k;
                break;
            }
        }
        if (verb == null) {
            return null;
        }
        return switch (verb) {
            case GIVES -> matchGive(t, verbIdx);
            case GETS -> matchGet(t, verbIdx);
            case RETURNS -> matchReturn(t, verbIdx);
            case KILLS -> matchKill(t, verbIdx);
            case BIND_LIFE_DEATH -> matchLifeDeath(t, verbIdx);
            default -> null;
        };
    }
    private static Result matchGive(List<BuilderToken> t, int verbIdx) {
        int forIdx = findFirstIndex(t, verbIdx + 1, t.size(), ContractTermKind.FOR);
        int clause1End = forIdx >= 0 ? forIdx : t.size();
        ResourceSpec given = parseResourceLenient(t, verbIdx + 1, clause1End);
        ResourceSpec received = forIdx >= 0 ? parseResourceLenient(t, forIdx + 1, t.size()) : null;
        if (given == null && received == null) return null;
        Boolean giverIsPartyA = findPartyBefore(t, 0, verbIdx);
        if (giverIsPartyA == null) return null;
        ContractData data = ContractData.empty();
        data.setContractKind(given != null && given.type == ContractData.TermType.DIGEST ? "DIGEST" : "STANDARD");
        applyResourceOrNone(data, given, false);
        applyResourceOrNone(data, received, true);
        return new Result(data);
    }

    private static Result matchGet(List<BuilderToken> t, int verbIdx) {
        int forIdx = findFirstIndex(t, verbIdx + 1, t.size(), ContractTermKind.FOR);
        int clause1End = forIdx >= 0 ? forIdx : t.size();
        ResourceSpec received = parseResourceLenient(t, verbIdx + 1, clause1End);
        ResourceSpec given = forIdx >= 0 ? parseResourceLenient(t, forIdx + 1, t.size()) : null;
        if (given == null && received == null) return null;
        ContractData data = ContractData.empty();
        data.setContractKind("STANDARD");
        applyResourceOrNone(data, received, true);
        applyResourceOrNone(data, given, false);
        return new Result(data);
    }

    private static Result matchReturn(List<BuilderToken> t, int verbIdx) {
        int toIdx = findFirstIndex(t, verbIdx + 1, t.size(), ContractTermKind.TO);
        int forIdx = findFirstIndex(t, verbIdx + 1, t.size(), ContractTermKind.FOR);
        int clause1End = toIdx >= 0 ? toIdx : (forIdx >= 0 ? forIdx : t.size());
        ResourceSpec returned = parseResourceLenient(t, verbIdx + 1, clause1End);
        if (returned == null) return null;
        Boolean returnerIsPartyA = findPartyBefore(t, 0, verbIdx);
        if (returnerIsPartyA == null) return null;
        ContractData data = ContractData.empty();
        data.setContractKind("IOU");
        if (returned.type == ContractData.TermType.SOUL) {
            data.setTermBType(ContractData.TermType.RELEASE_SOUL);
            data.setTermBSoulOwnerIsPartyA(Boolean.TRUE.equals(returned.ownerIsPartyA));
            if (forIdx >= 0) {ResourceSpec payment = parseResourceLenient(t, forIdx + 1, t.size());
                if (payment == null) {return null;}
                applyResourceOrNone(data, payment, true);
            } else {
                data.setTermAType(ContractData.TermType.NONE);
                data.setTermAFulfilled(false);
            }
        } else {
            data.setIou(true);
            applyResourceOrNone(data, returned, false);
            data.setTermAType(ContractData.TermType.NONE);
            data.setTermAFulfilled(false);
        }
        return new Result(data);
    }

    private static Result matchKill(List<BuilderToken> t, int verbIdx) {
        Boolean killerIsPartyA = findPartyBefore(t, 0, verbIdx);
        if (killerIsPartyA == null) return null;
        int victimIdx = findFirstIndex(t, verbIdx + 1, t.size(), ContractTermKind.VICTIM);
        if (victimIdx < 0) return null;
        String victim = t.get(victimIdx).value;
        if (victim == null || victim.isBlank()) return null;
        int forIdx = findFirstIndex(t, victimIdx + 1, t.size(), ContractTermKind.FOR);
        int fromIdx = findFirstIndex(t, victimIdx + 1, t.size(), ContractTermKind.FROM);
        int rewardStart = forIdx >= 0 ? forIdx + 1 : victimIdx + 1;
        ResourceSpec reward = parseResourceLenient(t, rewardStart, t.size());
        if (reward == null && fromIdx >= 0) {
            Boolean payerIsPartyA = findPartyAfter(t, fromIdx + 1, t.size());
            if (payerIsPartyA != null) {
                for (int idx = rewardStart; idx < t.size(); idx++) {
                    if (t.get(idx).kind != ContractTermKind.RESOURCE) continue;
                    String itemName = t.get(idx).value;
                    if (itemName == null || itemName.isBlank()) continue;
                    var resolved = ItemNameResolver.resolve(itemName);
                    if (resolved == null) continue;
                    ResourceSpec spec = new ResourceSpec();
                    spec.type = ContractData.TermType.ITEM;
                    spec.ownerIsPartyA = payerIsPartyA;
                    spec.itemId = BuiltInRegistries.ITEM.getKey(resolved).toString();
                    int amount = findPrecedingAmount(t, rewardStart, idx);
                    spec.count = amount > 0 ? amount : 1;
                    reward = spec;
                    break;
                }
            }
        }
        String victimEntityType = "ALL";

        for (int i = 0; i < victimIdx; i++) {
            if (t.get(i).kind == ContractTermKind.ENTITY_TYPE) {
                String value = t.get(i).value;
                if (value != null) {
                    victimEntityType = switch (value.toLowerCase(java.util.Locale.ROOT)) {
                        case "mob" -> "MOB";
                        case "player" -> "PLAYER";
                        default -> "ALL";
                    };
                }
            }
        }

        if (reward != null && reward.type == ContractData.TermType.ITEM && reward.ownerIsPartyA == null && fromIdx >= 0) {
            Boolean payerIsPartyA = findPartyAfter(t, fromIdx + 1, t.size());
            if (payerIsPartyA == null) return null;
            reward.ownerIsPartyA = payerIsPartyA;
        }
        ContractData data = ContractData.empty();
        data.setContractKind("KILL");
        data.setVictimName(victim);
        data.setVictimEntityType(victimEntityType);
        data.setKillerIsPartyA(killerIsPartyA);
        data.setKillPaidOut(false);
        if (reward != null && reward.ownerIsPartyA != null) data.setKillPayerIsPartyA(reward.ownerIsPartyA);
        if (reward != null) applyResourceOrNone(data, reward, true);
        else {
            data.setTermAType(ContractData.TermType.NONE);
            data.setTermAFulfilled(true);
        }
        return new Result(data);
    }

    private static Result matchLifeDeath(List<BuilderToken> t, int verbIdx) {
        Boolean binderIsPartyA = findPartyBefore(t, 0, verbIdx);
        if (binderIsPartyA == null) return null;
        int forIdx = findFirstIndex(t, verbIdx + 1, t.size(), ContractTermKind.FOR);
        int untilIdx = findFirstIndex(t, verbIdx + 1, t.size(), ContractTermKind.UNTIL);
        int targetEnd = t.size();
        if (forIdx >= 0) targetEnd = Math.min(targetEnd, forIdx);
        if (untilIdx >= 0) targetEnd = Math.min(targetEnd, untilIdx);
        Boolean targetIsPartyA = findPartyAfter(t, verbIdx + 1, targetEnd);
        if (targetIsPartyA == null || targetIsPartyA.equals(binderIsPartyA)) return null;
        ContractData data = ContractData.empty();
        data.setContractKind("LIFE_DEATH");
        data.setLifeDeathDurationTicks(-1);
        if (forIdx >= 0) {
            int paymentEnd = (untilIdx > forIdx) ? untilIdx : t.size();
            ResourceSpec payment = parseResourceWithFrom(t, forIdx + 1, paymentEnd);
            if (payment == null) return null;
            boolean itemPaidByPartyB = payment.type == ContractData.TermType.ITEM && Boolean.FALSE.equals(payment.ownerIsPartyA);
            applyResourceOrNone(data, payment, itemPaidByPartyB);
        } else {
            data.setTermBType(ContractData.TermType.NONE);
            data.setTermBFulfilled(false);
        }
        if (untilIdx >= 0) {
            int amountIdx = findFirstIndex(t, untilIdx + 1, t.size(), ContractTermKind.AMOUNT);
            int unitIdx = findFirstIndex(t, untilIdx + 1, t.size(), ContractTermKind.TIME_UNIT);
            if (amountIdx < 0 || unitIdx < 0) return null;
            String amountStr = t.get(amountIdx).value;
            String unit = t.get(unitIdx).value;
            if (amountStr == null || !amountStr.matches("\\d+") || unit == null) return null;
            long ticksPerUnit = switch (unit) {
                case "days" -> 20L * 60 * 60 * 24;
                case "minutes" -> 20L * 60;
                case "seconds" -> 20L;
                default -> -1;
            };
            if (ticksPerUnit <= 0) return null;
            data.setLifeDeathDurationTicks(Long.parseLong(amountStr) * ticksPerUnit);
        }
        return new Result(data);
    }
    private static ResourceSpec parseResourceWithFrom(List<BuilderToken> t, int from, int to) {
        int fromIdx = findFirstIndex(t, from, to, ContractTermKind.FROM);
        int resourceEnd = fromIdx >= 0 ? fromIdx : to;
        ResourceSpec spec = parseResourceLenient(t, from, resourceEnd);
        if (spec != null || fromIdx < 0) return spec;
        Boolean payerIsPartyA = findPartyAfter(t, fromIdx + 1, to);
        if (payerIsPartyA == null) return null;

        // SOUL
        for (int idx = from; idx < resourceEnd; idx++) {
            if (t.get(idx).kind == ContractTermKind.SOUL) {
                ResourceSpec s = new ResourceSpec();
                s.type = ContractData.TermType.SOUL;
                s.ownerIsPartyA = payerIsPartyA;
                return s;
            }
        }

        // DIGEST
        for (int idx = from; idx < resourceEnd; idx++) {
            if (t.get(idx).kind == ContractTermKind.DIGEST) {
                ResourceSpec s = new ResourceSpec();
                s.type = ContractData.TermType.DIGEST;
                s.ownerIsPartyA = payerIsPartyA;
                int amount = findPrecedingAmount(t, from, idx);
                if (amount > 100) return null;
                s.count = amount > 0 ? amount : 1;
                return s;
            }
        }
        // ITEM
        for (int idx = from; idx < resourceEnd; idx++) {
            if (t.get(idx).kind != ContractTermKind.RESOURCE) continue;
            String itemName = t.get(idx).value;
            if (itemName == null || itemName.isBlank()) continue;
            var resolved = ItemNameResolver.resolve(itemName);
            if (resolved == null) continue;
            ResourceSpec s = new ResourceSpec();
            s.type = ContractData.TermType.ITEM;
            s.ownerIsPartyA = payerIsPartyA;
            s.itemId = BuiltInRegistries.ITEM.getKey(resolved).toString();
            int amount = findPrecedingAmount(t, from, idx);
            s.count = amount > 0 ? amount : 1;
            return s;
        }
        return null;
    }

    private static void applyResourceOrNone(ContractData data, ResourceSpec res, boolean isTermA) {
        if (res == null) {
            if (isTermA) {
                data.setTermAType(ContractData.TermType.NONE);
                data.setTermAFulfilled(false);
            } else {
                data.setTermBType(ContractData.TermType.NONE);
                data.setTermBFulfilled(false);
            }
            return;
        }
        if (isTermA) {
            data.setTermAType(res.type);
            data.setTermAFulfilled(false);
            if (res.type == ContractData.TermType.ITEM) {
                data.setTermAItem(res.itemId);
                data.setTermACount(res.count);
            } else if (res.type == ContractData.TermType.DIGEST) {
                data.setTermACount(res.count);
            } else if (res.type == ContractData.TermType.SOUL) {
                data.setTermASoulOwnerIsPartyA(Boolean.TRUE.equals(res.ownerIsPartyA));
            } else if (res.type == ContractData.TermType.RELEASE_SOUL) {
                data.setReleaseTargetName(res.itemId);
            }
        } else {
            data.setTermBType(res.type);
            data.setTermBFulfilled(false);
            if (res.type == ContractData.TermType.ITEM) {
                data.setTermBItem(res.itemId);
                data.setTermBCount(res.count);
            } else if (res.type == ContractData.TermType.DIGEST) {
                data.setTermBCount(res.count);
            } else if (res.type == ContractData.TermType.SOUL) {
                data.setTermBSoulOwnerIsPartyA(Boolean.TRUE.equals(res.ownerIsPartyA));
            } else if (res.type == ContractData.TermType.RELEASE_SOUL) {
                data.setReleaseTargetName(res.itemId);
            }
        }
    }

    private static ResourceSpec parseResourceLenient(List<BuilderToken> t, int from, int to) {
        from = Math.max(from, 0);
        to = Math.min(to, t.size());
        for (int idx = from; idx < to; idx++) {
            if (t.get(idx).kind == ContractTermKind.SOUL) {
                Boolean ownerIsA = findPrecedingParty(t, from, idx);
                if (ownerIsA == null) continue;
                ResourceSpec spec = new ResourceSpec();
                spec.type = ContractData.TermType.SOUL;
                spec.ownerIsPartyA = ownerIsA;
                return spec;
            }
        }
        for (int idx = from; idx < to; idx++) {
            if (t.get(idx).kind == ContractTermKind.DIGEST) {
                Boolean ownerIsA = findPrecedingParty(t, from, idx);
                if (ownerIsA == null) continue;
                ResourceSpec spec = new ResourceSpec();
                spec.type = ContractData.TermType.DIGEST;
                spec.ownerIsPartyA = ownerIsA;
                int amount = findPrecedingAmount(t, from, idx);
                if (amount > 100) return null;
                spec.count = amount > 0 ? amount : 1;
                return spec;
            }
        }

        for (int idx = from; idx < to; idx++) {
            if (t.get(idx).kind == ContractTermKind.RESOURCE) {
                String itemName = t.get(idx).value;
                if (itemName == null || itemName.isEmpty()) continue;
                var resolved = ItemNameResolver.resolve(itemName);
                if (resolved == null) continue;
                Boolean ownerIsA = findPrecedingParty(t, from, idx);
                if (ownerIsA == null) continue;
                ResourceSpec spec = new ResourceSpec();
                spec.type = ContractData.TermType.ITEM;
                spec.ownerIsPartyA = ownerIsA;
                spec.itemId = BuiltInRegistries.ITEM.getKey(resolved).toString();
                int amount = findPrecedingAmount(t, from, idx);
                spec.count = amount > 0 ? amount : 1;
                return spec;
            }
        }
        return null;
    }

    private static Boolean findPrecedingParty(List<BuilderToken> t, int from, int idx) {
        for (int j = idx - 1; j >= from; j--) {
            ContractTermKind kind = t.get(j).kind;
            if (kind == ContractTermKind.PARTY_A) return true;
            if (kind == ContractTermKind.PARTY_B) return false;
            if (kind == ContractTermKind.RESOURCE || kind == ContractTermKind.DIGEST || kind == ContractTermKind.SOUL) break;
        }
        return null;
    }

    private static int findPrecedingAmount(List<BuilderToken> t, int from, int idx) {
        for (int j = idx - 1; j >= from; j--) {
            ContractTermKind k = t.get(j).kind;
            if (k == ContractTermKind.AMOUNT) {
                String v = t.get(j).value;
                if (v != null && v.matches("\\d+")) return Integer.parseInt(v);
            }
            if (k == ContractTermKind.RESOURCE || k == ContractTermKind.DIGEST || k == ContractTermKind.SOUL) break;
        }
        return -1;
    }

    private static int findFirstIndex(List<BuilderToken> t, int from, int to, ContractTermKind kind) {
        from = Math.max(from, 0);
        to = Math.min(to, t.size());
        for (int i = from; i < to; i++) if (t.get(i).kind == kind) return i;
        return -1;
    }

    private static Boolean findPartyBefore(List<BuilderToken> t, int from, int to) {
        from = Math.max(0, from);
        to = Math.min(t.size(), to);
        for (int i = to - 1; i >= from; i--) {
            if (t.get(i).kind == ContractTermKind.PARTY_A) return true;
            if (t.get(i).kind == ContractTermKind.PARTY_B) return false;
        }
        return null;
    }
    private static Boolean findPartyAfter(List<BuilderToken> t, int from, int to) {
        from = Math.max(0, from);
        to = Math.min(t.size(), to);
        for (int i = from; i < to; i++) {
            if (t.get(i).kind == ContractTermKind.PARTY_A) return true;
            if (t.get(i).kind == ContractTermKind.PARTY_B) return false;
        }
        return null;
    }
    static class ResourceSpec {
        ContractData.TermType type;
        Boolean ownerIsPartyA;
        String itemId;
        int count;
    }
    public static class Result {
        public final ContractData data;
        Result(ContractData data) {
            this.data = data;
        }
    }
}
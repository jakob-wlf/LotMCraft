package de.jakob.lotm.beyonders.sefirah;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.SefirotData;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.sefirah.SefirotAuthorityAbility;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ProbabilityManipulationManager extends SavedData {
    private static final String dataName = "keyOfLightProbabilityManipulation";
    private static final Set<String> blacklistedAbilityIds = Set.of(
            "ally_ability",
            "cogitation_ability",
            "divination_ability",
            "mythical_creature_form_ability",
            "spirit_vision_ability"
    );
    private final Map<String, Integer> failureChances = new HashMap<>();

    public static ProbabilityManipulationManager get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(new Factory<>(
                ProbabilityManipulationManager::new,
                ProbabilityManipulationManager::load), dataName);
    }

    public Map<String, Integer> getFailureChances() {
        return Collections.unmodifiableMap(failureChances);
    }

    public Map<String, Integer> getEffectiveFailureChances(MinecraftServer server) {
        UUID ownerId = SefirotData.get(server).getHolderOf("key_of_light");
        if (ownerId == null) return Map.of();
        ServerPlayer onlineOwner = server.getPlayerList().getPlayer(ownerId);
        int sequence = onlineOwner != null ? BeyonderData.getSequence(onlineOwner)
                : BeyonderData.playerMap.get(ownerId).map(data -> data.sequence()).orElse(9);
        normalizeForSequence(sequence);
        return getFailureChances();
    }

    public static int getMaximumAbilities(int sequence) {
        return switch (sequence) {
            case 0 -> 5;
            case 1 -> 3;
            case 2, 3 -> 2;
            default -> 1;
        };
    }

    public static ChanceRange getChanceRange(int sequence) {
        return switch (sequence) {
            case 0 -> new ChanceRange(25, 75);
            case 1 -> new ChanceRange(15, 50);
            case 2 -> new ChanceRange(10, 35);
            case 3 -> new ChanceRange(5, 25);
            case 4 -> new ChanceRange(3, 15);
            case 5 -> new ChanceRange(2, 10);
            default -> new ChanceRange(1, 5);
        };
    }

    public static boolean isEligibleAbility(Ability ability, int ownerSequence) {
        if (ability == null || ability.getShouldBeHidden()) return false;
        String abilityId = ability.getId();
        if (ability instanceof SefirotAuthorityAbility
                || abilityId.contains("authority")
                || blacklistedAbilityIds.contains(abilityId)) {
            return false;
        }
        return ability.lowestSequenceUsable() != 0 || ownerSequence <= 0;
    }

    public boolean updateRule(ServerPlayer owner, String abilityId, int requestedChance) {
        if (!"key_of_light".equals(SefirotData.get(owner.server).getClaimedSefirot(owner.getUUID()))) {
            return false;
        }

        if (requestedChance <= 0) {
            if (failureChances.remove(abilityId) != null) setDirty();
            return true;
        }

        int sequence = BeyonderData.getSequence(owner);
        var ability = LOTMCraft.abilityHandler.getById(abilityId);
        if (!isEligibleAbility(ability, sequence)) return false;
        if (!failureChances.containsKey(abilityId)
                && failureChances.size() >= getMaximumAbilities(sequence)) {
            return false;
        }
        ChanceRange range = getChanceRange(sequence);
        failureChances.put(abilityId, Math.clamp(requestedChance, range.minimum(), range.maximum()));
        trimToLimit(getMaximumAbilities(sequence));
        setDirty();
        return true;
    }

    private void trimToLimit(int limit) {
        while (failureChances.size() > limit) {
            String abilityId = failureChances.keySet().stream().sorted().reduce((first, second) -> second).orElse(null);
            if (abilityId == null) return;
            failureChances.remove(abilityId);
        }
    }

    private void normalizeForSequence(int sequence) {
        int previousSize = failureChances.size();
        failureChances.keySet().removeIf(abilityId ->
            !isEligibleAbility(LOTMCraft.abilityHandler.getById(abilityId), sequence));
        trimToLimit(getMaximumAbilities(sequence));
        ChanceRange range = getChanceRange(sequence);
        boolean changed = previousSize != failureChances.size();
        for (Map.Entry<String, Integer> entry : failureChances.entrySet()) {
            int clamped = Math.clamp(entry.getValue(), range.minimum(), range.maximum());
            if (clamped != entry.getValue()) {
                entry.setValue(clamped);
                changed = true;
            }
        }
        if (changed) setDirty();
    }

    public static boolean shouldFail(ServerLevel level, net.minecraft.world.entity.LivingEntity entity,
                                     Ability ability) {
        if (ability instanceof SefirotAuthorityAbility) return false;
        int chance = get(level.getServer()).getEffectiveFailureChances(level.getServer())
                .getOrDefault(ability.getId(), 0);
        if (chance <= 0 || entity.getRandom().nextInt(100) >= chance) return false;

        if (entity instanceof ServerPlayer player) {
            player.displayClientMessage(Component.literal("Probability rejected " )
                    .append(ability.getName())
                    .withStyle(ChatFormatting.RED), true);
        }
        return true;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag rules = new ListTag();
        failureChances.forEach((abilityId, chance) -> {
            CompoundTag rule = new CompoundTag();
            rule.putString("ability", abilityId);
            rule.putInt("chance", chance);
            rules.add(rule);
        });
        tag.put("rules", rules);
        return tag;
    }

    private static ProbabilityManipulationManager load(CompoundTag tag, HolderLookup.Provider provider) {
        ProbabilityManipulationManager data = new ProbabilityManipulationManager();
        ListTag rules = tag.getList("rules", Tag.TAG_COMPOUND);
        for (int index = 0; index < rules.size(); index++) {
            CompoundTag rule = rules.getCompound(index);
            data.failureChances.put(rule.getString("ability"), Math.clamp(rule.getInt("chance"), 1, 100));
        }
        return data;
    }

    public record ChanceRange(int minimum, int maximum) {}
}
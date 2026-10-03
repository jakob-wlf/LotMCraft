package de.jakob.lotm.attachments;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.DisableAbilityUsageForTimePacket;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DisabledAbilitiesComponent implements INBTSerializable<CompoundTag> {

    private final HashMap<String, Integer> hasAllAbilitiesDisabled = new HashMap<>();
    private final HashMap<String, List<DisabledAbility>> disabledAbilities = new HashMap<>();

    public List<DisabledAbility> getAllDisabledAbilities() {
        List<DisabledAbility> all = new ArrayList<>();
        for (List<DisabledAbility> list : disabledAbilities.values()) {
            all.addAll(list);
        }
        return all;
    }

    public void disableAbilityUsage(String cause) {
        hasAllAbilitiesDisabled.put(cause, 1);
    }

    public void enableAbilityUsage(String cause) {
        hasAllAbilitiesDisabled.remove(cause);
    }

    public void disableAbilityUsageForTime(String cause, int ticks, LivingEntity entity) {
        hasAllAbilitiesDisabled.put(cause, hasAllAbilitiesDisabled.getOrDefault(cause, 0) + 1);

        ServerScheduler.scheduleDelayed(ticks, () -> {
            hasAllAbilitiesDisabled.put(cause, hasAllAbilitiesDisabled.getOrDefault(cause, 1) - 1);
            if (hasAllAbilitiesDisabled.getOrDefault(cause, 0) <= 0) {
                hasAllAbilitiesDisabled.remove(cause);
            }
        });

        if(entity instanceof ServerPlayer player) {
            PacketHandler.sendToPlayer(player, new DisableAbilityUsageForTimePacket(entity.getId(), cause, ticks));
        }
    }

    public void disableSpecificAbility(String ability, String cause) {
        disabledAbilities.computeIfAbsent(cause, k -> new ArrayList<>()).add(new DisabledAbility(ability, 1));
    }

    public void enableSpecificAbility(String ability, String cause) {
        List<DisabledAbility> abilities = disabledAbilities.get(cause);
        if (abilities != null) {
            abilities.removeIf(da -> da.ability.equals(ability));
            if (abilities.isEmpty()) {
                disabledAbilities.remove(cause);
            }
        }
    }

    public void disableSpecificAbilityForTime(String ability, String cause, int ticks) {
        disabledAbilities.computeIfAbsent(cause, k -> new ArrayList<>());

        if(disabledAbilities.get(cause).stream().noneMatch(da -> da.ability.equals(ability))) {
            disabledAbilities.get(cause).add(new DisabledAbility(ability, 1));
        } else {
            DisabledAbility da = disabledAbilities.get(cause).stream().filter(d -> d.ability.equals(ability)).findFirst().orElseThrow();
            disabledAbilities.get(cause).remove(da);
            disabledAbilities.get(cause).add(new DisabledAbility(ability, da.amountDisabled + 1));
        }

        ServerScheduler.scheduleDelayed(ticks, () -> {
            List<DisabledAbility> abilities = disabledAbilities.get(cause);
            if (abilities != null) {
                DisabledAbility da = abilities.stream().filter(d -> d.ability.equals(ability)).findFirst().orElseThrow();
                abilities.remove(da);
                if (da.amountDisabled > 1) {
                    abilities.add(new DisabledAbility(ability, da.amountDisabled - 1));
                }
                if (abilities.isEmpty()) {
                    disabledAbilities.remove(cause);
                }
            }
        });
    }

    public boolean isAbilityUsageDisabled() {
        return !hasAllAbilitiesDisabled.isEmpty();
    }

    public boolean isSpecificAbilityDisabled(String ability) {
        return disabledAbilities.values().stream().anyMatch(list -> list.stream().anyMatch(da -> da.ability.equals(ability)));
    }

    public void enableAllAbilities() {
        hasAllAbilitiesDisabled.clear();
        disabledAbilities.clear();
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        ListTag allDisabledList = new ListTag();
        hasAllAbilitiesDisabled.forEach((cause, amount) -> {
            CompoundTag causeTag = new CompoundTag();
            causeTag.putString("cause", cause);
            causeTag.putInt("amount", amount);
            allDisabledList.add(causeTag);
        });
        tag.put("allDisabled", allDisabledList);

        ListTag specificDisabledList = new ListTag();
        disabledAbilities.forEach((cause, abilities) -> {
            abilities.forEach(da -> {
                CompoundTag abilityTag = new CompoundTag();
                abilityTag.putString("cause", cause);
                abilityTag.putString("ability", da.ability);
                abilityTag.putInt("amount", da.amountDisabled);
                specificDisabledList.add(abilityTag);
            });
        });
        tag.put("specificDisabled", specificDisabledList);

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        hasAllAbilitiesDisabled.clear();
        disabledAbilities.clear();

        if (compoundTag.contains("allDisabled", Tag.TAG_LIST)) {
            ListTag allDisabledList = compoundTag.getList("allDisabled", Tag.TAG_COMPOUND);
            for (int i = 0; i < allDisabledList.size(); i++) {
                CompoundTag causeTag = allDisabledList.getCompound(i);
                String cause = causeTag.getString("cause");
                int amount = causeTag.getInt("amount");
                hasAllAbilitiesDisabled.put(cause, amount);
            }
        }

        if (compoundTag.contains("specificDisabled", Tag.TAG_LIST)) {
            ListTag specificDisabledList = compoundTag.getList("specificDisabled", Tag.TAG_COMPOUND);
            for (int i = 0; i < specificDisabledList.size(); i++) {
                CompoundTag abilityTag = specificDisabledList.getCompound(i);
                String cause = abilityTag.getString("cause");
                String ability = abilityTag.getString("ability");
                int amount = abilityTag.getInt("amount");
                disabledAbilities.computeIfAbsent(cause, k -> new ArrayList<>()).add(new DisabledAbility(ability, amount));
            }
        }
    }

    public record DisabledAbility(String ability, int amountDisabled) {

    }
}

package de.jakob.lotm.beyonders.abilities.door;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.CopiedAbilityComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.util.helper.CopiedAbilityHelper;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Door Beyonders gain copied abilities from other pathways as they advance:
 * <ul>
 *     <li>Sequence 5, 4 and 3: {@link #RECORDED_PER_SEQUENCE} abilities with {@link #RECORDED_USES} uses each.</li>
 *     <li>Sequence 2: every copied ability they hold becomes unlimited.</li>
 *     <li>Sequence 2, 1 and 0: {@link #REPLICATED_PER_SEQUENCE} replicated (unlimited) abilities.</li>
 * </ul>
 */
public class DoorAdvancementCopies {

    public static final String COPY_TYPE = "door_advancement";
    private static final String REPLICATED_COPY_TYPE = "replicated";

    private static final int RECORDED_PER_SEQUENCE = 3;
    private static final int RECORDED_USES = 5;
    private static final int FIRST_GRANTING_SEQUENCE = 5;

    private static final int REPLICATED_PER_SEQUENCE = 2;
    private static final int UNLIMITED_SEQUENCE = 2;

    /** Called from BeyonderData.setBeyonder after a sequence-up has been applied. */
    public static void onSequenceUp(ServerPlayer player, String pathway, int sequence) {
        if (!pathway.equals("door") || sequence > FIRST_GRANTING_SEQUENCE) return;

        ServerLevel level = player.serverLevel();
        // Deferred so the grant happens after setBeyonder (which may clear copied abilities) has finished
        ServerScheduler.scheduleDelayed(3, () -> {
            if (player.isRemoved()) return;
            if (sequence > UNLIMITED_SEQUENCE) {
                grantCopies(player, sequence, RECORDED_PER_SEQUENCE, COPY_TYPE, RECORDED_USES);
            } else {
                makeCopiesUnlimited(player);
                grantCopies(player, sequence, REPLICATED_PER_SEQUENCE, REPLICATED_COPY_TYPE, -1);
            }
        }, level);
    }

    private static void makeCopiesUnlimited(ServerPlayer player) {
        CopiedAbilityComponent component = player.getData(ModAttachments.COPIED_ABILITY_COMPONENT);
        List<CopiedAbilityComponent.CopiedAbilityData> abilities = component.getAbilities();
        boolean changed = false;
        for (int i = 0; i < abilities.size(); i++) {
            if (abilities.get(i).remainingUses() != -1) {
                abilities.set(i, abilities.get(i).withRemainingUses(-1));
                changed = true;
            }
        }
        if (!changed) return;

        CopiedAbilityHelper.syncToClient(player);
        player.sendSystemMessage(Component.translatable("ability.lotmcraft.door.advancement_copies_unlimited").withColor(0x8ff4ff));
    }

    private static void grantCopies(ServerPlayer player, int sequence, int count, String copyType, int uses) {
        List<Ability> pool = buildPool(player, sequence);
        List<Component> names = new ArrayList<>();
        for (int i = 0; i < count && !pool.isEmpty(); i++) {
            Ability ability = pool.remove(player.getRandom().nextInt(pool.size()));
            CopiedAbilityHelper.addAbility(player, new CopiedAbilityComponent.CopiedAbilityData(
                    ability.getId(), copyType, uses, null));
            names.add(ability.getName());
        }
        if (names.isEmpty()) return;

        Component list = names.stream()
                .reduce((a, b) -> Component.empty().append(a).append(", ").append(b))
                .orElse(Component.empty());
        Component message = uses == -1
                ? Component.translatable("ability.lotmcraft.door.advancement_replicated", sequence, list)
                : Component.translatable("ability.lotmcraft.door.advancement_copies", sequence, list, uses);
        player.sendSystemMessage(message.copy().withColor(0x8ff4ff));
    }

    /**
     * Copyable abilities from other pathways that some pathway gets at exactly the new sequence,
     * excluding ones the player already has or has already copied.
     */
    private static List<Ability> buildPool(ServerPlayer player, int sequence) {
        List<Ability> pool = new ArrayList<>();
        for (Ability ability : LOTMCraft.abilityHandler.getAllAbilitiesOrdered()) {
            if (!ability.canBeCopied) continue;
            if (LOTMCraft.abilityHandler.isDisabled(ability)) continue;
            if (ability.getRequirements().containsKey("door")) continue;
            if (!ability.getRequirements().containsValue(sequence)) continue;
            if (ability.hasAbility(player, true) || CopiedAbilityHelper.hasCopiedAbility(player, ability.getId())) continue;
            pool.add(ability);
        }
        return pool;
    }
}

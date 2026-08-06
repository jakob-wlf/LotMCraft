package de.jakob.lotm.util.helper;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.CopiedAbilityComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.AbilityHandler;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncCopiedAbilitiesPacket;
import de.jakob.lotm.util.data.ClientData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;


import java.util.ArrayList;
import java.util.List;

public class CopiedAbilityHelper {

    public static void addAbility(ServerPlayer player, CopiedAbilityComponent.CopiedAbilityData data) {
        CopiedAbilityComponent component = player.getData(ModAttachments.COPIED_ABILITY_COMPONENT);
        component.addAbility(data);
        syncToClient(player);
    }

    public static void removeAbility(ServerPlayer player, int index) {
        CopiedAbilityComponent component = player.getData(ModAttachments.COPIED_ABILITY_COMPONENT);
        component.removeAbility(index);
        syncToClient(player);
    }


    public static void clearAbilities(ServerPlayer player) {
        CopiedAbilityComponent component = player.getData(ModAttachments.COPIED_ABILITY_COMPONENT);
        component.getAbilities().clear();
        syncToClient(player);
    }

    public static void decrementUses(LivingEntity entity, String abilityId) {
        CopiedAbilityComponent component = entity.getData(ModAttachments.COPIED_ABILITY_COMPONENT);

        int index = component.getAbilities().indexOf(component.getAbilities().stream().filter(data -> data.abilityId().equals(abilityId) && shouldReduceUsesForType(data.copyType())).findFirst().orElse(null));
        if (index < 0 || index >= component.getAbilities().size()) return;
        CopiedAbilityComponent.CopiedAbilityData data = component.getAbilities().get(index);
        if (data.remainingUses() == -1) return;
        int newUses = data.remainingUses() - 1;
        if (newUses <= 0) {
            component.getAbilities().remove(index);
        } else {
            component.getAbilities().set(index, data.withRemainingUses(newUses));
        }

        if (!entity.level().isClientSide) {
            if(entity instanceof ServerPlayer player)
                AbilityWheelHelper.removeUnusableAbilities(player);
            syncToClient((ServerPlayer) entity);
        }
    }

    private static boolean shouldReduceUsesForType(String copyType) {
        return switch (copyType) {
            default -> true;
            case "replicated" -> false;
        };
    }

    public static void syncToClient(ServerPlayer player) {
        CopiedAbilityComponent component = player.getData(ModAttachments.COPIED_ABILITY_COMPONENT);
        List<CopiedAbilityComponent.CopiedAbilityData> abilities = component.getAbilities();

        ArrayList<String> abilityIds = new ArrayList<>();
        ArrayList<String> copyTypes = new ArrayList<>();
        ArrayList<Integer> remainingUses = new ArrayList<>();

        for (CopiedAbilityComponent.CopiedAbilityData data : abilities) {
            abilityIds.add(data.abilityId());
            copyTypes.add(data.copyType());
            remainingUses.add(data.remainingUses());
        }

        PacketHandler.sendToPlayer(player, new SyncCopiedAbilitiesPacket(abilityIds, copyTypes, remainingUses));
    }
}

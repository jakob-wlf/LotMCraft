package de.jakob.lotm.util.helper;

import de.jakob.lotm.attachments.CopiedAbilityComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.gui.custom.copied_ability_wheel.CopiedAbilityWheelMenu;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncCopiedAbilitiesPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;


import java.util.ArrayList;
import java.util.List;

public class CopiedAbilityHelper {

    public static void addAbility(LivingEntity player, CopiedAbilityComponent.CopiedAbilityData data) {
        CopiedAbilityComponent component = player.getData(ModAttachments.COPIED_ABILITY_COMPONENT);
        component.addAbility(data);

        if(player instanceof ServerPlayer serverPlayer)
            syncToClient(serverPlayer);
    }

    public static void removeAbilityIndex(LivingEntity player, int index) {
        CopiedAbilityComponent component = player.getData(ModAttachments.COPIED_ABILITY_COMPONENT);
        component.removeAbility(index);

        if(player instanceof ServerPlayer serverPlayer)
            syncToClient(serverPlayer);
    }

    public static void removeAbilityID(LivingEntity entity, String abilityId) {
        CopiedAbilityComponent component = entity.getData(ModAttachments.COPIED_ABILITY_COMPONENT);
        component.removeAbility(abilityId);

        if (entity instanceof ServerPlayer serverPlayer) {
            syncToClient(serverPlayer);
        }
    }

    public static void openCopiedAbilityWheel(ServerPlayer player) {
        System.out.println("[SERVER] Opening wheel for: " + player.getName().getString());
        syncToClient(player);
        player.openMenu(new SimpleMenuProvider(
                (id, inventory, p) -> new CopiedAbilityWheelMenu(id, inventory),
                Component.translatable("lotm.copied_ability_wheel.title")
        ));
    }


    public static void clearAbilities(LivingEntity player) {
        CopiedAbilityComponent component = player.getData(ModAttachments.COPIED_ABILITY_COMPONENT);
        component.getAbilities().clear();

        if(player instanceof ServerPlayer serverPlayer)
            syncToClient(serverPlayer);
    }

    public static void decrementUses(LivingEntity entity, String abilityId) {
        CopiedAbilityComponent component = entity.getData(ModAttachments.COPIED_ABILITY_COMPONENT);

        int index = component.getAbilities()
                .indexOf(component.getAbilities()
                        .stream()
                        .filter(data ->
                                data.abilityId().equals(abilityId)
                                        && shouldReduceUsesForType(data.copyType()))
                        .findFirst().orElse(null));

        if (index < 0 || index >= component.getAbilities().size()) return;
        CopiedAbilityComponent.CopiedAbilityData data = component.getAbilities().get(index);

        if (data.remainingUses() == -1) return;

        int newUses = data.remainingUses() - 1;

        if (newUses <= 0) {
            removeAbilityIndex(entity, index);
        } else {
            component.getAbilities().set(index, data.withRemainingUses(newUses));
        }

        if (!entity.level().isClientSide) {
            if(entity instanceof ServerPlayer player)
                AbilityWheelHelper.removeUnusableAbilities(player);
            syncToClient((ServerPlayer) entity);
        }
    }

    public static int getRemainingUses(LivingEntity entity, String abilityId) {
        CopiedAbilityComponent component = entity.getData(ModAttachments.COPIED_ABILITY_COMPONENT);
        return component.getAbilities().stream()
                .filter(data -> data.abilityId().equals(abilityId))
                .map(CopiedAbilityComponent.CopiedAbilityData::remainingUses)
                .findFirst()
                .orElse(-1);
    }

    public static boolean hasCopiedAbility(LivingEntity entity, String abilityId) {
        CopiedAbilityComponent component = entity.getData(ModAttachments.COPIED_ABILITY_COMPONENT);
        return component.getAbilities().stream()
                .anyMatch(data -> data.abilityId().equals(abilityId));
    }

    private static boolean shouldReduceUsesForType(String copyType) {
        return switch (copyType) {
            case "replicated" -> false;
            default -> true;
        };
    }

    public static void syncToClient(ServerPlayer player) {
        CopiedAbilityComponent component = player.getData(ModAttachments.COPIED_ABILITY_COMPONENT);
        List<CopiedAbilityComponent.CopiedAbilityData> abilities = component.getAbilities();

        System.out.println("[SERVER] Syncing " + abilities.size() + " abilities to client: " + player.getName().getString());
        for (CopiedAbilityComponent.CopiedAbilityData data : abilities) {
            System.out.println("  - Ability ID: " + data.abilityId() + ", Uses: " + data.remainingUses());
        }

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

package de.jakob.lotm.gui.custom.ArtifactWheel;

import de.jakob.lotm.artifacts.SealedArtifactData;
import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.gui.ModMenuTypes;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.SyncArtifactAbilityWheel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ArtifactWheelMenu extends AbstractContainerMenu {

    private final ItemStack stack;
    private final Inventory playerInventory;

    public ArtifactWheelMenu(int containerId, Inventory playerInventory, ItemStack stack) {
        super(ModMenuTypes.ARTIFACT_WHEEL_MENU.get(), containerId);
        this.playerInventory = playerInventory;
        this.stack = stack;
    }

    public List<String> getAbilities() {
        SealedArtifactData data = stack.get(ModDataComponents.SEALED_ARTIFACT_DATA);
        if (data == null || data.abilities().isEmpty()) {
            return new ArrayList<>();
        }
        List<String> abilityNames = data.abilities().stream()
                .map(ability -> ability.getId())
                .toList();
        if (abilityNames.isEmpty()) {
            return new ArrayList<>();
        }
        return abilityNames;
    }

    public int getSelectedAbilityIndex() {
        return stack.getOrDefault(ModDataComponents.SEALED_ARTIFACT_SELECTED, 0);
    }

    public void setSelectedAbilityIndex(int index) {
        ItemStack stackInHand = playerInventory.player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!stackInHand.has(ModDataComponents.SEALED_ARTIFACT_DATA)) {
            stackInHand = playerInventory.player.getItemInHand(InteractionHand.OFF_HAND);
            if (!stackInHand.has(ModDataComponents.SEALED_ARTIFACT_DATA)) {
                return;
            }
        }
        stackInHand.set(ModDataComponents.SEALED_ARTIFACT_SELECTED, index);
        PacketHandler.sendToServer(
                new SyncArtifactAbilityWheel(index)
        );
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
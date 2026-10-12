package de.jakob.lotm.beyonders.abilities.abyss.contract.handler;

import de.jakob.lotm.beyonders.abilities.abyss.contract.builder.ContractBuilderScreen;
import de.jakob.lotm.beyonders.abilities.abyss.contract.menu.ContractAbilityMenus;
import net.minecraft.client.Minecraft;

public final class ContractScreenHandler {
    private ContractScreenHandler() {}
    public static void open(boolean showHelp) {
        Minecraft mc = Minecraft.getInstance();
        if (showHelp) mc.setScreen(ContractAbilityMenus.createContractAbilityMenu(mc.screen));
        else mc.setScreen(new ContractBuilderScreen());
    }
}
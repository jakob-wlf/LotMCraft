package de.jakob.lotm.beyonders.abilities.abyss.contract.menu;

import de.jakob.lotm.gui.custom.text_display.AbilityMenuBuilder;
import de.jakob.lotm.gui.custom.text_display.ColoredTextDisplayScreen;
import de.jakob.lotm.gui.custom.text_display.TextColorHelper;
import net.minecraft.client.gui.screens.Screen;

public class ContractAbilityMenus {

    public static ColoredTextDisplayScreen createContractAbilityMenu(Screen previousScreen) {
        return new AbilityMenuBuilder("Outdated Contract", previousScreen)
                .header("Overview", TextColorHelper.DARK_RED)
                .line("Create binding contracts with other players using the contract builder.", TextColorHelper.DARK_GREEN)
                .line("Contracts can exchange items, souls, digestion, life and death, or", TextColorHelper.DARK_GREEN)
                .line("even require one player to kill another. Choose your terms carefully.", TextColorHelper.DARK_GREEN)

                .spacing()
                .header("How to Use", TextColorHelper.DARK_RED)
                .line("1. Activate the ability", TextColorHelper.DARK_GREEN)
                .line("2. Use the Contract Builder to choose the contract terms", TextColorHelper.DARK_GREEN)
                .line("3. Assign Party A and Party B when required", TextColorHelper.DARK_GREEN)
                .line("4. The assigned target receives the contract proposal", TextColorHelper.DARK_GREEN)
                .line("5. They type 'accept' or 'decline' in chat", TextColorHelper.DARK_GREEN)
                .line("6. Once accepted, a Contract Item records the agreement", TextColorHelper.DARK_GREEN)

                .spacing()
                .header("Contract Forms", TextColorHelper.DARK_RED)
                .ability("<Name> gives <Name> <resource> for <Name> <resource>", "Standard exchange", TextColorHelper.DARK_RED, TextColorHelper.DARK_GREEN)
                .ability("<Name> gets <resource> for <Name> <resource>", "Same as GIVE, reordered", TextColorHelper.DARK_RED, TextColorHelper.DARK_GREEN)
                .ability("<Name> returns <Name>'s soul to <Name> [for <resource>]", "Releases a soul you hold", TextColorHelper.DARK_RED, TextColorHelper.DARK_GREEN)
                .ability("<Name> returns <item> to <Name>", "An IOU using your soul as collateral", TextColorHelper.DARK_RED, TextColorHelper.DARK_GREEN)
                .ability("<Name> kills <Name> for <resource> from <Name>", "Killing Contract", TextColorHelper.DARK_RED, TextColorHelper.DARK_GREEN)
                .ability("<Name> binds life and death with <Name> for <resource> from <Name>", "Link your lives", TextColorHelper.DARK_RED, TextColorHelper.DARK_GREEN)
                .ability("... until <amount> days|minutes|seconds passes", "Optional expiry on a life-link", TextColorHelper.DARK_RED, TextColorHelper.DARK_GREEN)
                .ability("<Name> gives <Name> <amount> of digest for <resource>", "Trade digestion progress for a 50% tax", TextColorHelper.DARK_RED, TextColorHelper.DARK_GREEN)

                .spacing()
                .header("Resources", TextColorHelper.DARK_RED)
                .line("An item resource: '10 diamonds', '1 iron ingot'", TextColorHelper.DARK_GREEN)
                .line("A soul must name its owner: 'Alice's soul'", TextColorHelper.DARK_GREEN)
                .line("Souls cannot be offered without identifying their owner.", TextColorHelper.DARK_GREEN)

                .spacing()
                .header("How Souls Work", TextColorHelper.DARK_RED)
                .line("A soul can only be bound to one contract at a time.", TextColorHelper.DARK_GREEN)
                .line("Once bound, the soul remains tied to the contract until fulfilled", TextColorHelper.DARK_GREEN)
                .line("or the contract is voided. Consuming a soul begins a drain on its owner.", TextColorHelper.DARK_GREEN)
                .line("The soul's owner does not immediately die when their soul is consumed.", TextColorHelper.DARK_GREEN)
                .line("Right-click the Contract Item to perform an available soul action.", TextColorHelper.DARK_GREEN)

                .spacing()
                .header("Completing & Voiding", TextColorHelper.DARK_RED)
                .line("Item terms are fulfilled when the required item is present with", TextColorHelper.DARK_GREEN)
                .line("the party who is supposed to receive it.", TextColorHelper.DARK_GREEN)
                .line("Some contracts complete automatically once all obligations are met.", TextColorHelper.DARK_GREEN)
                .line("A sufficiently powerful player may forcibly void a contract they do not own.", TextColorHelper.DARK_GREEN)
                .line("Voiding releases pledged souls or transfers them to a pending", TextColorHelper.DARK_GREEN)
                .line("release contract when applicable.", TextColorHelper.DARK_GREEN)

                .spacing()
                .separator()
                .spacing()
                .line("Press ESC to close this menu", TextColorHelper.DARK_GRAY)

                .build();
    }
}
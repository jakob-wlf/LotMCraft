package de.jakob.lotm.abilities;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.abyss.*;
import de.jakob.lotm.abilities.common.*;
import de.jakob.lotm.abilities.darkness.*;
import de.jakob.lotm.abilities.demoness.*;
import de.jakob.lotm.abilities.door.*;
import de.jakob.lotm.abilities.error.*;
import de.jakob.lotm.abilities.fool.*;
import de.jakob.lotm.abilities.mother.*;
import de.jakob.lotm.abilities.red_priest.*;
import de.jakob.lotm.abilities.sun.*;
import de.jakob.lotm.abilities.tyrant.*;
import de.jakob.lotm.abilities.visionary.*;
import de.jakob.lotm.abilities.wheel_of_fortune.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AbilityItemHandler {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LOTMCraft.MOD_ID);

    public static DeferredItem<Item> ABILITY_NOT_IMPLEMENTED = ITEMS.registerItem("ability_not_implemented", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static void registerAbilities(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

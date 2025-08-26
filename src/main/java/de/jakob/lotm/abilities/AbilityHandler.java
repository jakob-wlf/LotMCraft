package de.jakob.lotm.abilities;

import de.jakob.lotm.abilities.common.CogitationAbility;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.common.DivinationAbility;
import de.jakob.lotm.abilities.common.SpiritVisionAbility;
import de.jakob.lotm.abilities.abyss.PoisonousFlameAbility;
import de.jakob.lotm.abilities.abyss.ToxicSmokeAbility;
import de.jakob.lotm.abilities.darkness.MidnightPoemAbility;
import de.jakob.lotm.abilities.demoness.*;
import de.jakob.lotm.abilities.door.DoorOpeningAbility;
import de.jakob.lotm.abilities.door.SpellsAbility;
import de.jakob.lotm.abilities.fool.*;
import de.jakob.lotm.abilities.mother.CleansingAbility;
import de.jakob.lotm.abilities.mother.HealingAbility;
import de.jakob.lotm.abilities.mother.PlantNurturingAbility;
import de.jakob.lotm.abilities.red_priest.ProvokingAbility;
import de.jakob.lotm.abilities.red_priest.PyrokinesisAbility;
import de.jakob.lotm.abilities.red_priest.TrapAbility;
import de.jakob.lotm.abilities.tyrant.WaterManipulationAbility;
import de.jakob.lotm.abilities.sun.*;
import de.jakob.lotm.abilities.tyrant.RagingBlowsAbility;
import de.jakob.lotm.abilities.tyrant.WindManipulationAbility;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AbilityHandler {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LOTMCraft.MOD_ID);
    public static final DeferredItem<Item> COGITATION = ITEMS.registerItem("cogitation_ability", CogitationAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static final DeferredItem<Item> DIVINATION = ITEMS.registerItem("divination_ability", DivinationAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static final DeferredItem<Item> SPIRIT_VISION = ITEMS.registerItem("spirit_vision_ability", SpiritVisionAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static final DeferredItem<Item> HOLY_SONG = ITEMS.registerItem("holy_song_ability", HolySongAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static final DeferredItem<Item> ILLUMINATE = ITEMS.registerItem("illuminate_ability", IlluminateAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> HOLY_LIGHT = ITEMS.registerItem("holy_light_ability",  HolyLightAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> FIRE_OF_LIGHT = ITEMS.registerItem("fire_of_light_ability",  FireOfLightAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> HOLY_LIGHT_SUMMONING = ITEMS.registerItem("holy_light_summoning_ability",  HolyLightSummoningAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> CLEAVE_OF_PURIFICATION = ITEMS.registerItem("cleave_of_purification_ability",  CleaveOfPurificationAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> HOLY_OATH = ITEMS.registerItem("holy_oath_ability",  HolyOathAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> NOTARY_BUFF = ITEMS.registerItem("notary_buff_ability",  GodSaysItsEffectiveAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> NOTARY_DEBUFF = ITEMS.registerItem("notary_debuff_ability",  GodSaysItsNotEffectiveAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> LIGHT_OF_HOLINESS = ITEMS.registerItem("light_of_holiness_ability",  LightOfHolinessAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> PURIFICATION_HALO = ITEMS.registerItem("purification_halo_ability",  PurificationHaloAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> TRAP = ITEMS.registerItem("trap_ability", TrapAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> PROVOKING = ITEMS.registerItem("provoking_ability", ProvokingAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> PYROKINESIS = ITEMS.registerItem("pyrokinesis_ability", PyrokinesisAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> RAGING_BLOWS = ITEMS.registerItem("raging_blows_ability", RagingBlowsAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> WATER_MANIPULATION = ITEMS.registerItem("water_manipulation_ability", WaterManipulationAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> WIND_MANIPULATION = ITEMS.registerItem("wind_manipulation_ability", WindManipulationAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> TOXIC_SMOKE = ITEMS.registerItem("toxic_smoke_ability", ToxicSmokeAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> POISONOUS_FLAME = ITEMS.registerItem("poisonous_flame_ability", PoisonousFlameAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> AIR_BULLET = ITEMS.registerItem("air_bullet_ability", AirBulletAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> FLAME_CONTROLLING = ITEMS.registerItem("flame_controlling_ability", FlameControllingAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> PAPER_FIGURINE_SUBSTITUTE = ITEMS.registerItem("paper_figurine_substitute_ability", PaperFigurineSubstituteAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> FLAMING_JUMP = ITEMS.registerItem("flaming_jump_ability", FlamingJumpAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> UNDERWATER_BREATHING = ITEMS.registerItem("underwater_breathing_ability", UnderWaterBreathingAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> MIDNIGHT_POEM = ITEMS.registerItem("midnight_poem_ability", MidnightPoemAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> SHADOW_CONCEALMENT = ITEMS.registerItem("shadow_concealment_ability", ShadowConcealmentAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> MIGHTY_BLOW = ITEMS.registerItem("mighty_blow_ability", MightyBlowAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> BLACK_FLAME = ITEMS.registerItem("black_flame_ability", BlackFlameAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> FROST = ITEMS.registerItem("frost_ability", FrostAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> INVISIBILITY = ITEMS.registerItem("invisibility_ability", InvisibilityAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> MIRROR_SUBSTITUTION = ITEMS.registerItem("mirror_substitution_ability", MirrorSubstituteAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> PLANT_NURTURING = ITEMS.registerItem("plant_nurturing_ability", PlantNurturingAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> HEALING = ITEMS.registerItem("healing_ability", HealingAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> CLEANSING = ITEMS.registerItem("cleanse_ability", CleansingAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> DOOR_OPENING = ITEMS.registerItem("door_opening_ability", DoorOpeningAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static DeferredItem<Item> SPELLS = ITEMS.registerItem("spells_ability", SpellsAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static void registerAbilities(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

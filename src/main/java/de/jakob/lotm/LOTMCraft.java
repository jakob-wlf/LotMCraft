package de.jakob.lotm;

import com.mojang.logging.LogUtils;
import de.jakob.lotm.abilities.AbilityItemHandler;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.block.ModBlockEntities;
import de.jakob.lotm.block.ModBlocks;
import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.client.*;
import de.jakob.lotm.entity.quests.QuestRegistry;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.gui.ModMenuTypes;
import de.jakob.lotm.gui.custom.AbilitySelection.AbilitySelectionScreen;
import de.jakob.lotm.gui.custom.BrewingCauldron.BrewingCauldronScreen;
import de.jakob.lotm.gui.custom.Introspect.IntrospectScreen;
import de.jakob.lotm.item.ModCreativeModTabs;
import de.jakob.lotm.item.ModIngredients;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.loottables.ModLootModifiers;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.particle.*;
import de.jakob.lotm.potions.BeyonderCharacteristicItemHandler;
import de.jakob.lotm.potions.PotionItemHandler;
import de.jakob.lotm.potions.PotionRecipeItemHandler;
import de.jakob.lotm.potions.PotionRecipes;
import de.jakob.lotm.rendering.GuidingBookRenderer;
import de.jakob.lotm.sound.ModSounds;
import de.jakob.lotm.structure.ModStructures;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.Config;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import de.jakob.lotm.villager.ModVillagers;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(LOTMCraft.MOD_ID)
public class LOTMCraft
{
    public static final String MOD_ID = "lotmcraft";

    public static KeyMapping pathwayInfosKey;
    public static KeyMapping openAbilitySelectionKey;
    public static KeyMapping toggleGriefingKey;
    public static KeyMapping showPassiveAbilitiesKey;
    public static KeyMapping nextAbilityKey;
    public static KeyMapping previousAbilityKey;
    public static KeyMapping toggleAbilityHotbarKey;
    public static KeyMapping cycleAbilityHotbarKey;
    public static KeyMapping enterSefirotKey;


    public static final Logger LOGGER = LogUtils.getLogger();

    public LOTMCraft(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        BeyonderData.initPathwayInfos();

        // Register ourselves for server and other game events we are interested in
        NeoForge.EVENT_BUS.register(this);

        ModCreativeModTabs.register(modEventBus);

        ModItems.register(modEventBus);
        ModIngredients.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModParticles.register(modEventBus);
        ModEntities.register(modEventBus);
        ModEffects.register(modEventBus);
        ModSounds.register(modEventBus);
        ModLootModifiers.register(modEventBus);
        ModVillagers.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModStructures.register(modEventBus);
        ModDataComponents.register(modEventBus);
        PotionRecipeItemHandler.registerRecipes(modEventBus);
        BeyonderCharacteristicItemHandler.registerCharacteristics(modEventBus);
        ModAttachments.register(modEventBus);
        ModDimensions.register(modEventBus);
        ModGameRules.register();

        AbilityItemHandler.registerAbilities(modEventBus);
        PassiveAbilityHandler.registerAbilities(modEventBus);
        PotionItemHandler.registerPotions(modEventBus);

        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(PacketHandler::register);

        ServerScheduler.initialize();

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntities.FLAMING_SPEAR.get(), FlamingSpearProjectileRenderer::new);
            EntityRenderers.register(ModEntities.UNSHADOWED_SPEAR.get(), UnshadowedSpearProjectileRenderer::new);
            EntityRenderers.register(ModEntities.WIND_BLADE.get(), WindBladeRenderer::new);
            EntityRenderers.register(ModEntities.FIREBALL.get(), FireballRenderer::new);
            EntityRenderers.register(ModEntities.PAPER_DAGGER.get(), PaperDaggerProjectileRenderer::new);
            EntityRenderers.register(ModEntities.FIRE_RAVEN.get(), FireRavenRenderer::new);
            EntityRenderers.register(ModEntities.APPRENTICE_DOOR.get(), ApprenticeDoorRenderer::new);
            EntityRenderers.register(ModEntities.FROST_SPEAR.get(), FrostSpearProjectileRenderer::new);
            EntityRenderers.register(ModEntities.ELECTRIC_SHOCK.get(), ElectricShockRenderer::new);
            EntityRenderers.register(ModEntities.LIGHTNING.get(), LightningRenderer::new);
            EntityRenderers.register(ModEntities.TRAVELERS_DOOR.get(), TravelersDoorRenderer::new);
            EntityRenderers.register(ModEntities.APPRENTICE_BOOK.get(), ApprenticeBookRenderer::new);
            EntityRenderers.register(ModEntities.BEYONDER_NPC.get(), BeyonderNPCRenderer::new);
            EntityRenderers.register(ModEntities.TSUNAMI.get(), TsunamiRenderer::new);
            EntityRenderers.register(ModEntities.TORNADO.get(), TornadoRenderer::new);
            EntityRenderers.register(ModEntities.LIGHTNING_BRANCH.get(), LightningBranchRenderer::new);
            EntityRenderers.register(ModEntities.EXILE_DOORS.get(), ExileDoorsRenderer::new);
            EntityRenderers.register(ModEntities.SPACE_RIFT.get(), SpaceRiftRenderer::new);
            EntityRenderers.register(ModEntities.SPACE_COLLAPSE_LEGACY.get(), SpaceCollapseRendererLegacy::new);
            EntityRenderers.register(ModEntities.SPACE_COLLAPSE.get(), SpaceCollapseRenderer::new);
            EntityRenderers.register(ModEntities.BLACK_HOLE.get(), BlackHoleRenderer::new);
            EntityRenderers.register(ModEntities.WAR_BANNER.get(), WarBannerRenderer::new);
            EntityRenderers.register(ModEntities.Meteor.get(), MeteorRenderer::new);
            EntityRenderers.register(ModEntities.JUSTICE_SWORD.get(), JusticeSwordRenderer::new);
            EntityRenderers.register(ModEntities.SUN.get(), SunRenderer::new);
            EntityRenderers.register(ModEntities.SPEAR_OF_LIGHT.get(), SpearOfLightProjectileRenderer::new);
            EntityRenderers.register(ModEntities.VOLCANO.get(), VolcanoRenderer::new);
            EntityRenderers.register(ModEntities.GIANT_LIGHTNING.get(), GiantLightningRenderer::new);
            EntityRenderers.register(ModEntities.ELECTROMAGNETIC_TORNADO.get(), ElectromagneticTornadoRenderer::new);
            EntityRenderers.register(ModEntities.SUN_KINGDOM.get(), SunKingdomEntityRenderer::new);
            EntityRenderers.register(ModEntities.DISTORTION_FIELD.get(), DistortionFieldRenderer::new);
            EntityRenderers.register(ModEntities.SPEAR_OF_DESTRUCTION.get(), SpearOfDestructionProjectileRenderer::new);
            EntityRenderers.register(ModEntities.RETURN_PORTAL.get(), ReturnPortalRenderer::new);

            GuidingBookRenderer.loadPages(LOTMCraft.MOD_ID);
        }

        @SubscribeEvent
        public static void onCommonSetup(FMLCommonSetupEvent event) {
            QuestRegistry.registerQuests();
            event.enqueueWork(PotionRecipes::initPotionRecipes);
            event.enqueueWork(PotionRecipeItemHandler::initializeRecipes);
        }

        @SubscribeEvent
        public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticles.HOLY_FLAME.get(), HolyFlameParticle.Provider::new);
            event.registerSpriteSet(ModParticles.DARKER_FLAME.get(), DarkerFlameParticle.Provider::new);
            event.registerSpriteSet(ModParticles.CRIMSON_LEAF.get(), CrimsonLeafParticle.Provider::new);
            event.registerSpriteSet(ModParticles.TOXIC_SMOKE.get(), ToxicSmokeParticle.Provider::new);
            event.registerSpriteSet(ModParticles.GREEN_FLAME.get(), GreenFlameParticle.Provider::new);
            event.registerSpriteSet(ModParticles.BLACK_FLAME.get(), BlackFlameParticle.Provider::new);
            event.registerSpriteSet(ModParticles.HEALING.get(), HealingParticle.Provider::new);
            event.registerSpriteSet(ModParticles.BLACK_NOTE.get(), BlackNoteParticle.Provider::new);
            event.registerSpriteSet(ModParticles.GOLDEN_NOTE.get(), GoldenNoteParticle.Provider::new);
            event.registerSpriteSet(ModParticles.DISEASE.get(), DiseaseParticle.Provider::new);
            event.registerSpriteSet(ModParticles.EARTHQUAKE.get(), EarthquakeParticle.Provider::new);
            event.registerSpriteSet(ModParticles.LIGHTNING.get(), LightningParticle.Provider::new);
            event.registerSpriteSet(ModParticles.STAR.get(), StarParticle.Provider::new);
            event.registerSpriteSet(ModParticles.FOG_OF_WAR.get(), FogOfWarParticle.Provider::new);
            event.registerSpriteSet(ModParticles.PURPLE_FLAME.get(), PurpleFlameParticle.Provider::new);
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.ABILITY_SELECTION_MENU.get(), AbilitySelectionScreen::new);
            event.register(ModMenuTypes.INTROSPECT_MENU.get(), IntrospectScreen::new);
            event.register(ModMenuTypes.BREWING_CAULDRON_MENU.get(), BrewingCauldronScreen::new);
        }
    }

}

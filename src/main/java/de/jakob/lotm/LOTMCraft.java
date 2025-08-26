package de.jakob.lotm;

import com.mojang.logging.LogUtils;
import de.jakob.lotm.block.ModBlockEntities;
import de.jakob.lotm.block.ModBlocks;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.client.*;
import de.jakob.lotm.gui.ModMenuTypes;
import de.jakob.lotm.gui.custom.AbilitySelectionScreen;
import de.jakob.lotm.gui.custom.BrewingCauldronScreen;
import de.jakob.lotm.item.ModCreativeModTabs;
import de.jakob.lotm.item.ModIngredients;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.loottables.ModLootModifiers;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.particle.*;
import de.jakob.lotm.potions.PotionItemHandler;
import de.jakob.lotm.potions.PotionRecipeItemHandler;
import de.jakob.lotm.potions.PotionRecipes;
import de.jakob.lotm.sound.ModSounds;
import de.jakob.lotm.structure.ModProcessorTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.abilities.AbilityHandler;
import de.jakob.lotm.util.Config;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.util.scheduling.ClientScheduler;
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

    public static KeyMapping switchBeyonderKey;
    public static KeyMapping pathwayInfosKey;
    public static KeyMapping clearBeyonderKey;
    public static KeyMapping openAbilitySelectionKey;
    public static KeyMapping toggleGriefingKey;
    public static KeyMapping showPassiveAbilitiesKey;
    public static KeyMapping nextAbilityKey;
    public static KeyMapping previousAbilityKey;
    public static KeyMapping increaseSequenceKey;
    public static KeyMapping decreaseSequenceKey;


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
        ModProcessorTypes.register(modEventBus);
        PotionRecipeItemHandler.registerRecipes(modEventBus);

        AbilityHandler.registerAbilities(modEventBus);
        PassiveAbilityHandler.registerAbilities(modEventBus);
        PotionItemHandler.registerPotions(modEventBus);

        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(PacketHandler::register);

        ServerScheduler.initialize();
        ClientScheduler.initialize();

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

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntities.FLAMING_SPEAR.get(), FlamingSpearProjectileRenderer::new);
            EntityRenderers.register(ModEntities.WIND_BLADE.get(), WindBladeRenderer::new);
            EntityRenderers.register(ModEntities.FIREBALL.get(), FireballRenderer::new);
            EntityRenderers.register(ModEntities.PAPER_DAGGER.get(), PaperDaggerProjectileRenderer::new);
            EntityRenderers.register(ModEntities.FIRE_RAVEN.get(), FireRavenRenderer::new);
            EntityRenderers.register(ModEntities.APPRENTICE_DOOR.get(), ApprenticeDoorRenderer::new);
            EntityRenderers.register(ModEntities.FROST_SPEAR.get(), FrostSpearProjectileRenderer::new);
            EntityRenderers.register(ModEntities.ELECTRIC_SHOCK.get(), ElectricShockRenderer::new);
        }

        @SubscribeEvent
        public static void onCommonSetup(FMLCommonSetupEvent event) {
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
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.ABILITY_SELECTION_MENU.get(), AbilitySelectionScreen::new);
            event.register(ModMenuTypes.BREWING_CAULDRON_MENU.get(), BrewingCauldronScreen::new);
        }
    }

}

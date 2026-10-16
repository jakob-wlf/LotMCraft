package de.jakob.lotm.dimension.abyss.Flith;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class ModFilthClient {
    private static final ResourceLocation STILL = ResourceLocation.withDefaultNamespace("block/water_still");
    private static final ResourceLocation FLOWING = ResourceLocation.withDefaultNamespace("block/water_flow");
    private static final ResourceLocation OVERLAY = ResourceLocation.withDefaultNamespace("block/water_overlay");
    private static final int TINT = 0xFF1B0F24;
    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public @NotNull ResourceLocation getStillTexture() {return STILL;}
            @Override
            public @NotNull ResourceLocation getFlowingTexture() {return FLOWING;}
            @Override
            public ResourceLocation getOverlayTexture() {return OVERLAY;}
            @Override
            public int getTintColor() {return TINT;}
        }, ModFilth.LIQUID_FILTH_TYPE.get());
    }
}

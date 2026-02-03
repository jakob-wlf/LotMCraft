package de.jakob.lotm.rendering;

import com.mojang.blaze3d.shaders.FogShape;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.FogComponent;
import de.jakob.lotm.attachments.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class FogRenderer {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Player player = Minecraft.getInstance().player;
        if(player == null) {
            return;
        }

        FogComponent component = player.getData(ModAttachments.FOG_COMPONENT);
        if(!component.isActive()) {
            return;
        }

        FogComponent.FOG_TYPE fogType = component.getFogType();
        if(fogType == null) {
            return;
        }

        event.setFogShape(FogShape.SPHERE);
        event.setNearPlaneDistance(fogType.getNearPlaneDistance());
        event.setFarPlaneDistance(fogType.getFarPlaneDistance());
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRenderFog(ViewportEvent.ComputeFogColor event) {
        Player player = Minecraft.getInstance().player;
        if(player == null) {
            return;
        }

        FogComponent component = player.getData(ModAttachments.FOG_COMPONENT);
        if(!component.isActive()) {
            return;
        }

        FogComponent.FOG_TYPE fogType = component.getFogType();
        if(fogType == null) {
            return;
        }

        event.setRed(component.getColor().x());
        event.setGreen(component.getColor().y());
        event.setBlue(component.getColor().z());
    }

}

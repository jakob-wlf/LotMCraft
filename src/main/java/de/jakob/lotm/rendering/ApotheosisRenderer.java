package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.FogComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

import java.util.Comparator;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class ApotheosisRenderer {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRenderFogColor(ViewportEvent.ComputeFogColor event) {
        Player player = Minecraft.getInstance().player;
        if(player == null) {
            return;
        }

        Player apotheosisPlayer = player.level().players().stream()
                .filter(p -> p.getData(ModAttachments.APOTHEOSIS_COMPONENT).getApotheosisTicksLeft() > 0)
                .max(Comparator.comparing(p -> p.getData(ModAttachments.APOTHEOSIS_COMPONENT).getApotheosisTicksLeft()))
                .orElse(null);

        if(apotheosisPlayer == null) {
            return;
        }

        int color = apotheosisPlayer.getData(ModAttachments.APOTHEOSIS_COMPONENT).getPathway() == null ?
                0xFFFFFF :
                BeyonderData.pathwayInfos.get(apotheosisPlayer.getData(ModAttachments.APOTHEOSIS_COMPONENT).getPathway()).color();

        event.setRed(((color >> 16) & 0xFF) / 255f);
        event.setGreen(((color >> 8) & 0xFF) / 255f);
        event.setBlue((color & 0xFF) / 255f);

    }

}

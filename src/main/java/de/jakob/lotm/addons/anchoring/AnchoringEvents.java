package de.jakob.lotm.addons.anchoring;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber
public class AnchoringEvents {
    private static long tickCounter = 0;

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.isBeyonder(player)) return;

        int seq = BeyonderData.getSequence(player);

        boolean hasSwitched = BeyonderData.hasSwitchedPathway(player);
        boolean hasUndigestedStack = BeyonderData.getCurrentCharStack(player) > 0
                && BeyonderData.getDigestionProgress(player) < 1.0f;

        String name = player.getName().getString();

        if(player.tickCount % 40 == 0){
            if(player.level() instanceof ServerLevel level)
                BeyonderData.anchoringStorage.recalculateAnchoring(name, level);
        }

        float drain = 0.0f;
        if(hasSwitched)
            drain += 0.0025f;
        if(hasUndigestedStack)
            drain += 0.0025f;

        drain += AnchoringCore.getSanityLossBase(seq);

        float anchors = (float) BeyonderData.anchoringStorage.getAnchoring(name)
                .getAnchoring() / 100;


        drain -= anchors;
        if(drain <= 0.0f) {
            return;
        }

        var component = player.getData(ModAttachments.SANITY_COMPONENT.get());
        component.decreaseSanityAndSync(drain, player);
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if(player.level() instanceof ServerLevel level)
                BeyonderData.anchoringStorage.recalculateAnchoring(player.getName().getString(), level);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if(player.level() instanceof ServerLevel level)
                BeyonderData.anchoringStorage.recalculateAnchoring(player.getName().getString(), level);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;

        if(tickCounter % 20 == 0){
            BeyonderData.anchoringStorage.recalculateAvatars(event.getServer().overworld());
        }
    }
}

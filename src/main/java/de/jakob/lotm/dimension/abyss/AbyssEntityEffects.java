package de.jakob.lotm.dimension.abyss;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class AbyssEntityEffects {

    private static final int ABYSS_SAFE_SEQUENCE = 5;
    private static final int OTHER_SAFE_SEQUENCE = 2;
    private static final float SANITY_DAMAGE = 0.005f;
    
    //The Abyss itself counts as a sequence 0 source.
    private static final int ABYSS_SOURCE_SEQUENCE = 0;
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.level().dimension().equals(ModDimensions.ABYSS_DIMENSION_KEY)) return;
        if (player.isCreative() || player.isSpectator()) return;
        if (!player.isAlive()) return;
        if (isProtected(player)) return;
        player.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityWithSequenceDifference(SANITY_DAMAGE,player,ABYSS_SOURCE_SEQUENCE,BeyonderData.getSequence(player));
    }

    private static boolean isProtected(ServerPlayer player) {
        String pathway = BeyonderData.getPathway(player);
        if (pathway == null || pathway.isEmpty() || pathway.equalsIgnoreCase("none")) {return false;}
        int sequence = BeyonderData.getSequence(player);
        int needed = "abyss".equals(pathway) ? ABYSS_SAFE_SEQUENCE : OTHER_SAFE_SEQUENCE;
        return sequence <= needed;
    }
}
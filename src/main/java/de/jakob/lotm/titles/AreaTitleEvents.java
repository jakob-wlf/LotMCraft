package de.jakob.lotm.titles;

import de.jakob.lotm.attachments.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = "yourmod")
public class AreaTitleEvents {
    private static final int STRUCTURE_CHECK_INTERVAL = 10; // ticks

    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent e) {
        e.addListener(new AreaTitleManager());
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        sp.getData(ModAttachments.INSIDE_NOW).clear(); // structure state is per-level

        var entry = AreaTitleManager.forDimension(e.getTo().location());
        if (entry != null) tryShow(sp, entry.getKey(), entry.getValue());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        if (sp.tickCount % STRUCTURE_CHECK_INTERVAL != 0) return;

        Map<ResourceLocation, AreaTitle> defs = AreaTitleManager.structureTitles();
        if (defs.isEmpty()) return;

        ServerLevel level = sp.serverLevel();
        BlockPos pos = sp.blockPosition();
        Set<ResourceLocation> inside = sp.getData(ModAttachments.INSIDE_NOW);

        for (var entry : defs.entrySet()) {
            ResourceLocation titleId = entry.getKey();
            AreaTitle def = entry.getValue();
            ResourceKey<Structure> key = ResourceKey.create(Registries.STRUCTURE, def.target());

            // Piece-accurate check (not just the bounding box)
            boolean now = level.structureManager()
                    .getStructureWithPieceAt(pos, holder -> holder.is(key))
                    .isValid();
            boolean was = inside.contains(titleId);

            if (now && !was) { inside.add(titleId); tryShow(sp, titleId, def); }
            else if (!now && was) { inside.remove(titleId); }
        }
    }

    private static void tryShow(ServerPlayer sp, ResourceLocation id, AreaTitle def) {
        if (def.frequency() == AreaTitle.Frequency.ONCE) {
            if (!sp.getData(ModAttachments.SEEN_TITLES).add(id)) return;
        }
        sp.connection.send(new ClientboundSetTitlesAnimationPacket(def.fadeIn(), def.stay(), def.fadeOut()));
        sp.connection.send(new ClientboundSetSubtitleTextPacket(def.subtitle()));
        sp.connection.send(new ClientboundSetTitleTextPacket(def.title()));
    }
}

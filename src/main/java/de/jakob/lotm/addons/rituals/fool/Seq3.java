package de.jakob.lotm.addons.rituals.fool;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq3 {
    private static final int DISTANCE = 150;
    private static final int SECONDS_PER_STAGE = 320;
    private static final int STAGES = 50;
    private static final int MAX_LEVEL_IN_FACTION = 5;

    @SubscribeEvent
    private static void onFoolTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!BeyonderData.getPathway(player).equals("fool") ||
                BeyonderData.getSequence(player) != 4) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if (component.isCompleted()) return;

        if (level.dimension() != ServerLevel.OVERWORLD) {
            component.setStage(0);
            return;
        }

        String name = player.getName().getString();
        var factions = BeyonderData.factionStorage.getPartOfFaction(name);
        for (var obj : factions) {
            if (obj.getPlayerLevel(name) > MAX_LEVEL_IN_FACTION)
                return;
        }

        for (var obj : level.getServer().getPlayerList().getPlayers()) {
            if (obj.equals(player)) continue;
            if (obj.level() != player.level()) continue;

            if (obj.distanceTo(player) <= DISTANCE)
                component.setStage(0);
        }

        if (player.tickCount % (20 * SECONDS_PER_STAGE) == 0) {
            component.setStage(component.getStage() + 1);
        }

        if (component.getStage() >= STAGES) {
            component.setCompleted(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    private static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();

        if (!(player.level() instanceof ServerLevel level)) return;

        for (var obj : level.getServer().getPlayerList().getPlayers()) {
            if (BeyonderData.getPathway(obj).equals("fool") && BeyonderData.getSequence(obj) == 4) {
                if (obj.level() != player.level()) continue;
                if (player.distanceTo(obj) <= DISTANCE) {
                    var component = obj.getData(ModAttachments.RITUALS.get());
                    component.setStage(0);
                }
            }
        }
    }
}

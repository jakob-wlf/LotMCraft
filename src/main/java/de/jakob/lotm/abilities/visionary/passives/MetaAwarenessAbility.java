package de.jakob.lotm.abilities.visionary.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.events.HonorificNamesEventHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.playerMap.PendingPrayer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class MetaAwarenessAbility extends PassiveAbilityItem {
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
    private static final long COOLDOWN_MS = 5000; // 5s cooldown

    public static final Set<String> COMMON_WORDS = Set.of(
            "a","about","above","after","again","against","all","am","an","and","any","are",
            "as","at","be","because","been","before","being","below","between","both","but",
            "by","can","could","did","do","does","doing","down","during","each","few","for",
            "from","further","had","has","have","having","he","her","here","hers","herself",
            "him","himself","his","how","i","if","in","into","is","it","its","itself",
            "just","me","more","most","my","myself","no","nor","not","now","of","off","on",
            "once","only","or","other","our","ours","ourselves","out","over","own","same",
            "she","should","so","some","such","than","that","the","their","theirs","them",
            "themselves","then","there","these","they","this","those","through","to","too",
            "under","until","up","very","was","we","were","what","when","where","which",
            "while","who","whom","why","will","with","you","your","yours","yourself","yourselves"
    );

    public MetaAwarenessAbility(Item.Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 1));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onChat(ServerChatEvent event) {
        String msg = event.getRawText();
        ServerPlayer sender = event.getPlayer();

        if (!(sender.level() instanceof ServerLevel serverLevel)) return;

        // Check all online players who have this passive
        for (ServerPlayer candidate : serverLevel.getServer().getPlayerList().getPlayers()) {
            // Don't trigger if the sender is saying their own name
            if (candidate.getUUID().equals(sender.getUUID())) continue;

            // Check if this player has the MetaAwareness passive
            if (!hasMetaAwareness(candidate)) continue;

            String username = candidate.getName().getString();

            boolean match = false;

            for (int i = 0; i < username.length(); i++) {
                for (int j = i + 3; j <= username.length(); j++) {
                    String part = username.substring(i, j);

                    if (COMMON_WORDS.contains(part)) continue;

                    if (msg.contains(part)) {
                        match = true;
                        break;
                    }
                }
                if (match) break;
            }

            if (!match) {
                if (!msg.toLowerCase().contains(username.toLowerCase())
                        && !BeyonderData.playerMap.containsHonorificNameWithLine(msg)) continue;
            }

            triggerAutoPrayer(sender, candidate, msg);
        }
    }


    // Called from PlayerDivinationSelectedPacket.handle when a divination succeeds. If the divined target has MetaAwareness, auto-pray  to the diviner.
    public static void onDivined(ServerPlayer diviner, ServerPlayer target) {
        if (!hasMetaAwareness(target)) return;
        triggerAutoPrayer(diviner, target, "");
    }


    public static void triggerAutoPrayer(ServerPlayer sender, ServerPlayer target, String msg) {
        // Cooldown check on the target (prevent spam if their name is said repeatedly)
        long now = System.currentTimeMillis();
        Long lastTrigger = COOLDOWNS.get(target.getUUID());
        if (lastTrigger != null && now - lastTrigger < COOLDOWN_MS) return;
        COOLDOWNS.put(target.getUUID(), now);

        // Store pending prayer so target can respond via Honorific Names menu
        PendingPrayer prayer = new PendingPrayer(
                sender.getUUID(),
                sender.getName().getString(),
                BeyonderData.getPathway(sender),
                BeyonderData.getSequence(sender),
                sender.getX(), sender.getY(), sender.getZ()
        );

        HonorificNamesEventHandler.addPendingPrayer(target.getUUID(), prayer);

        // Add to pending prayers via HonorificNamesEventHandler
        HonorificNamesEventHandler.answerState.add(
                new com.mojang.datafixers.util.Pair<>(target.getUUID(), sender.getUUID()));

        // Use reflection-free approach: directly notify target
        target.sendSystemMessage(
                Component.empty()
                        .append(Component.literal("[Meta Awareness] ")
                                .withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(msg.isEmpty()? "" : "Message: " + msg + "\n")
                        .append(HonorificNamesEventHandler.formNotification(sender))
        );
    }

    private static boolean hasMetaAwareness(ServerPlayer player) {
        // Check if the player has this passive item in their inventory
        return BeyonderData.getSequence(player) <= 1 && BeyonderData.getPathway(player).equals("visionary");
    }
}
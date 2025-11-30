package de.jakob.lotm.sefirah;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class SefirahCastleEventHandler {

    public static HashMap<UUID, Integer> luckRitualProgress = new HashMap<>();
    public static HashMap<UUID, Long> timeoutForRitual = new HashMap<>();


    // Check for ritual -------------------------------------------------------------------------------
    @SubscribeEvent
    public static void onChatMessageSent(ServerChatEvent event) {
        UUID playerUUID = event.getPlayer().getUUID();

        // Check for timeout
        if(timeoutForRitual.containsKey(playerUUID) && timeoutForRitual.get(playerUUID) <= System.currentTimeMillis()) {
            timeoutForRitual.remove(playerUUID);
            luckRitualProgress.remove(playerUUID);
        }

        String rawMessage = event.getRawText();

        // Check chants
        if(!checkIfChantIsCompleted(rawMessage, playerUUID, event.getPlayer().position(), event.getPlayer().serverLevel())) {
            return;
        }

        timeoutForRitual.remove(playerUUID);
        luckRitualProgress.remove(playerUUID);

        // Check for pathway requirement
        if(!BeyonderData.isBeyonder(event.getPlayer()) ||
                (!BeyonderData.getPathway(event.getPlayer()).equalsIgnoreCase("fool") &&
                        !BeyonderData.getPathway(event.getPlayer()).equalsIgnoreCase("door") &&
                        !BeyonderData.getPathway(event.getPlayer()).equalsIgnoreCase("error"))) {
            AbilityUtil.sendActionBar(event.getPlayer(), Component.translatable("lotm.sefirot.wrong_pathway").withColor(0x942de3));
            return;
        }

        // Claim Sefirah Castle
        if (!SefirahHandler.claimSefirot(event.getPlayer(), "sefirah_castle") && !SefirahHandler.getClaimedSefirot(event.getPlayer()).equalsIgnoreCase("sefirah_castle")) {
            AbilityUtil.sendActionBar(event.getPlayer(), Component.translatable("lotm.sefirot.sefirah_castle_already_occupied").withColor(0x942de3));
            return;
        }

        SefirahHandler.teleportToSefirot(event.getPlayer(), true);
    }

    private static boolean checkIfChantIsCompleted(String rawMessage, UUID playerUUID, Vec3 pos, ServerLevel serverLevel) {
        // Check for first chant
        if(!luckRitualProgress.containsKey(playerUUID)) {
            if(!rawMessage.equalsIgnoreCase("The Immortal Lord of Heaven and Earth for Blessings")) {
                return false;
            }
            luckRitualProgress.put(playerUUID, 0);
            timeoutForRitual.put(playerUUID, System.currentTimeMillis() + (60 * 1000));

            EffectManager.playEffect(EffectManager.Effect.SEFIRAH_CASTLE_PARTICLES, pos.x, pos.y, pos.z, serverLevel);
            return false;
        }

        // Check for subsequent chants
        int ritualProgress = luckRitualProgress.get(playerUUID);

        if(!rawMessage.equalsIgnoreCase(getNextIncantationForProgressIndex(ritualProgress))) {
            luckRitualProgress.remove(playerUUID);
            timeoutForRitual.remove(playerUUID);
            return false;
        }

        EffectManager.playEffect(EffectManager.Effect.SEFIRAH_CASTLE_PARTICLES, pos.x, pos.y, pos.z, serverLevel);

        luckRitualProgress.replace(playerUUID, ritualProgress + 1);

        return ritualProgress == 2;
    }



    // Disable abilities inside the castle and disable griefing inside completely --------------------
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if(!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if(!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (!entity.level().dimension().equals(ModDimensions.SEFIRAH_CASTLE_DIMENSION_KEY)) {
            return;
        }

        // Disable griefing
        if (entity instanceof Player player) {
            BeyonderData.setGriefingEnabled(player, false);
        }

        // Disable ability use
        if(!(entity instanceof ServerPlayer player) || !SefirahHandler.getClaimedSefirot(player).equalsIgnoreCase("sefirah_castle")) {
            BeyonderData.disableAbilityUseWithTimeLimit(entity, "sefirah_castle", 2000);
        }
    }

    private static String getNextIncantationForProgressIndex(int progress) {
        return switch (progress) {
            case 0 -> "The Sky Lord of Heaven and Earth for Blessings";
            case 1 -> "The Exalted Thearch of Heaven and Earth for Blessings";
            case 2 -> "The Celestial Worthy of Heaven and Earth for Blessings";
            default -> "";
        };
    }

}

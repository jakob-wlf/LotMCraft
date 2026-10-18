package de.jakob.lotm.beyonders.abilities.death;

import com.mojang.logging.LogUtils;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Each time a Death Beyonder advances into Sequence 5, 4, 3, 2, 1 or 0, {@link #SOULS_PER_SEQUENCE}
 * Beyonder souls of random pathways, one sequence below theirs, are stored in their Internal Underworld.
 */
public class DeathAdvancementSouls {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int SOULS_PER_SEQUENCE = 3;
    /** Souls start being granted once the Beyonder gains Internal Underworld. */
    private static final int FIRST_GRANTING_SEQUENCE = 5;

    /** Called from BeyonderData.setBeyonder after a sequence-up has been applied. */
    public static void onSequenceUp(ServerPlayer player, String pathway, int sequence) {
        if (!pathway.equals("death") || sequence > FIRST_GRANTING_SEQUENCE) return;

        ServerLevel level = player.serverLevel();
        // Deferred so the soul cap is read from the new sequence after setBeyonder has finished
        ServerScheduler.scheduleDelayed(3, () -> grantSouls(player, sequence + 1), level);
    }

    private static void grantSouls(ServerPlayer player, int soulSequence) {
        if (player.isRemoved() || !(player.level() instanceof ServerLevel level)) return;

        List<String> names = new ArrayList<>();
        for (int i = 0; i < SOULS_PER_SEQUENCE; i++) {
            String soulPathway = pickPathway(player, soulSequence);
            if (soulPathway == null) {
                LOGGER.warn("No pathway available for a Sequence {} advancement soul for {}", soulSequence, player.getName().getString());
                return;
            }

            // Never added to the world: it only exists long enough to be serialized into the Internal Underworld
            BeyonderNPCEntity npc = new BeyonderNPCEntity(ModEntities.BEYONDER_NPC.get(), level, false, soulPathway, soulSequence);
            InternalUnderworldAbility.storeSoul(player, npc);
            npc.discard();
            names.add(BeyonderData.getSequenceName(soulPathway, soulSequence));
        }

        player.sendSystemMessage(Component.translatable("ability.lotmcraft.death.advancement_souls",
                soulSequence, String.join(", ", names)).withColor(0x7ECFCF));
    }

    /** Random pathway that is implemented down to the requested sequence. */
    private static String pickPathway(ServerPlayer player, int sequence) {
        List<String> eligible = BeyonderData.implementedPathways.stream()
                .filter(p -> BeyonderData.getHighestImplementedSequence(p) <= sequence)
                .toList();
        if (eligible.isEmpty()) return null;
        return eligible.get(player.getRandom().nextInt(eligible.size()));
    }
}

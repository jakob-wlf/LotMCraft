package de.jakob.lotm.beyonders.abilities.fool.marionettes;

import com.mojang.logging.LogUtils;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.List;

/**
 * Each time a Fool advances into Sequence 5, 4, 3, 2, 1 or 0, they gain one marionette:
 * a Beyonder of a random pathway at their new sequence. It ignores Puppeteering's marionette cap.
 */
public class FoolAdvancementMarionettes {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Marionettes start being granted once the Fool becomes a Marionettist. */
    private static final int FIRST_GRANTING_SEQUENCE = 5;

    /** Called from BeyonderData.setBeyonder after a sequence-up has been applied. */
    public static void onSequenceUp(ServerPlayer player, String pathway, int sequence) {
        if (!pathway.equals("fool") || sequence > FIRST_GRANTING_SEQUENCE) return;

        ServerLevel level = player.serverLevel();
        // Deferred so the spawn happens after setBeyonder and its syncs have finished
        ServerScheduler.scheduleDelayed(3, () -> grantMarionette(player, sequence), level);
    }

    private static void grantMarionette(ServerPlayer player, int sequence) {
        if (player.isRemoved() || !(player.level() instanceof ServerLevel level)) return;

        String marionettePathway = pickPathway(player, sequence);
        if (marionettePathway == null) {
            LOGGER.warn("No pathway available for a Sequence {} advancement marionette for {}", sequence, player.getName().getString());
            return;
        }

        BeyonderNPCEntity npc = new BeyonderNPCEntity(ModEntities.BEYONDER_NPC.get(), level, false, marionettePathway, sequence);
        Vec3 look = player.getLookAngle().multiply(1, 0, 1);
        Vec3 offset = look.lengthSqr() < 1.0E-4 ? new Vec3(2, 0, 0) : look.normalize().scale(2);
        Vec3 pos = player.position().add(offset);
        npc.setPos(pos);
        level.addFreshEntity(npc);

        npc.setHealth(npc.getMaxHealth());
        npc.setTarget(null);
        if (!MarionetteUtils.turnEntityIntoMarionette(npc, player)) {
            LOGGER.warn("Failed to turn advancement marionette into a marionette for {}", player.getName().getString());
            return;
        }

        for (int i = 0; i < 40; i++) {
            double ox = (player.getRandom().nextDouble() - 0.5) * 2;
            double oy = player.getRandom().nextDouble() * npc.getBbHeight();
            double oz = (player.getRandom().nextDouble() - 0.5) * 2;
            level.sendParticles(ParticleTypes.SOUL, pos.x + ox, pos.y + oy, pos.z + oz, 1, 0, 0.05, 0, 0.05);
        }

        player.sendSystemMessage(Component.translatable("ability.lotmcraft.fool.advancement_marionette",
                sequence, BeyonderData.getSequenceName(marionettePathway, sequence)).withColor(0xa26fc9));
    }

    /**
     * Random pathway that is implemented down to the requested sequence. No pathway lists Sequence 0 as
     * implemented, so for Sequence 0 any fully implemented pathway (down to Sequence 1) is allowed.
     */
    private static String pickPathway(ServerPlayer player, int sequence) {
        int required = Math.max(sequence, 1);
        List<String> eligible = BeyonderData.implementedPathways.stream()
                .filter(p -> BeyonderData.getHighestImplementedSequence(p) <= required)
                .toList();
        if (eligible.isEmpty()) return null;
        return eligible.get(player.getRandom().nextInt(eligible.size()));
    }
}

package de.jakob.lotm.abilities.sefirah;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.OpenAboveSeqAuthorityScreenPacket;
import de.jakob.lotm.sefirah.GreatOldOneManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Map;

/**
 * Above the Sequence Authority — granted exclusively to players who have reached
 * Sequence 0 (the peak of the beyonder sequence).  When activated it opens the
 * "Above the Sequence Authority" screen (currently shown as "Coming Soon").
 */
public class AboveTheSequenceAuthorityAbility extends Ability {

    public AboveTheSequenceAuthorityAbility(String id) {
        super(id, 0f);
        this.canBeCopied = false;
        this.canBeShared = false;
        this.canBeReplicated = false;
        this.canBeUsedInArtifact = false;
        this.cannotBeStolen = true;
        this.canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        // Not pathway-specific — hasAbility() is used instead.
        return Map.of();
    }

    @Override
    protected float getSpiritualityCost() {
        return 0f;
    }

    /** Only Great Old Ones (Sequence -1). */
    @Override
    public boolean hasAbility(LivingEntity entity) {
        if (entity.level().isClientSide()) return false;
        if (!(entity instanceof ServerPlayer player)) return false;
        return GreatOldOneManager.isGreatOldOne(player);
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide()) return;
        if (!(entity instanceof ServerPlayer player)) return;
        PacketHandler.sendToPlayer(player, new OpenAboveSeqAuthorityScreenPacket());
    }
}

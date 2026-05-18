package de.jakob.lotm.abilities.error;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.AvatarEntity;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class AvatarCreationAbility extends Ability {
    public AvatarCreationAbility(String id) {
        super(id, 5);

        canBeUsedByNPC = false;
        canBeCopied = false;
        canBeUsedInArtifact =false;
        canBeShared = false;
        cannotBeStolen = true;
        canBeReplicated = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 1500;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if(!BeyonderData.isBeyonder(entity)) {
            return;
        }

        if(entity instanceof AvatarEntity previousAvatar) {
            return;
        }

        var stacks = BeyonderData.playerMap.get(entity).get().charStack();
        int sequence = LOTMCraft.NON_BEYONDER_SEQ;
        int entitySeq = BeyonderData.getSequence(entity);

        for (int i = 1; i < LOTMCraft.NON_BEYONDER_SEQ; i++){
            if(entitySeq >= i) continue;

            if(stacks[i] > 1){
                sequence = i;
                break;
            }
        }

        AvatarEntity avatar = new AvatarEntity(ModEntities.ERROR_AVATAR.get(), level, entity.getUUID(), "error", sequence);
        avatar.setPos(entity.getX(), entity.getY(), entity.getZ());
        level.addFreshEntity(avatar);

        if(sequence != LOTMCraft.NON_BEYONDER_SEQ)
            BeyonderData.setCharStack(entity, sequence, stacks[sequence] - 1, true, BeyonderData.getPathway(entity));
    }
}

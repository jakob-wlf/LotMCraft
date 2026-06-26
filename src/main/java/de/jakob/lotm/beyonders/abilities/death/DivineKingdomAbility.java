package de.jakob.lotm.beyonders.abilities.death;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.death_pathway.DeathDivineKingdomEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class DivineKingdomAbility extends Ability {

    public DivineKingdomAbility(String id) {
        super(id, 300f, "death");
        canBeCopied = false;
        canBeReplicated = false;
        cannotBeStolen = true;
        canBeUsedByNPC = false;
        canBeUsedInArtifact = false;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("death", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 30000;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity caster) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        level.playSound(null, caster.blockPosition(),
                SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 4.0f, 0.3f);
        level.playSound(null, caster.blockPosition(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 3.0f, 0.5f);

        DeathDivineKingdomEntity domain = new DeathDivineKingdomEntity(
                ModEntities.DEATH_DIVINE_KINGDOM.get(), level, caster);
        domain.setPos(caster.getX(), caster.getY(), caster.getZ());
        serverLevel.addFreshEntity(domain);
    }
}

package de.jakob.lotm.beyonders.abilities.common;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.sefirah.SefirahHandler;
import de.jakob.lotm.beyonders.sefirah.SefrotInvasionManager;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class SefrotInvasionAbility extends Ability {
    public SefrotInvasionAbility(String id) {
        super(id, 1);
        canBeUsedByNPC = false;
        canBeCopied = false;
        canBeReplicated = false;
        cannotBeStolen = true;
        canBeUsedInArtifact = false;
        canBeShared = false;
        doesNotIncreaseDigestion = true;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player
                && BeyonderData.isBeyonder(player)
                && BeyonderData.getSequence(player) <= 9
                && !SefirahHandler.hasSefirot(player)) {
            SefrotInvasionManager.tryInvade(player);
        }
    }

    @Override
    public boolean hasAbility(LivingEntity entity) {
        return (!(entity instanceof ServerPlayer player) || !SefirahHandler.hasSefirot(player))
                && super.hasAbility(entity);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        Map<String, Integer> requirements = new HashMap<>();
        BeyonderData.pathwayInfos.keySet().stream()
                .filter(pathway -> !pathway.equals("none") && !pathway.equals("placeholder"))
                .forEach(pathway -> requirements.put(pathway, 9));
        return requirements;
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    @Override
    public ResourceLocation getTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID,
                "textures/abilities/sefirot_authority_ability.png");
    }
}
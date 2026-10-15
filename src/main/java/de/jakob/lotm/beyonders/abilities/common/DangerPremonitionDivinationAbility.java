package de.jakob.lotm.beyonders.abilities.common;

import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class DangerPremonitionDivinationAbility extends ToggleAbility {
    public DangerPremonitionDivinationAbility(String id, String... interactionFlags) {
        super(id, interactionFlags);
        this.canBeUsedByNPC = false;

        this.shouldBeHidden = true;
        this.canBeCopied = false;
        this.cannotBeStolen = true;
        this.canBeReplicated = false;
        this.canBeUsedInArtifact = false;
        this.doesNotIncreaseDigestion = true;
    }

    @Override
    public void tick(Level level, LivingEntity entity) {

    }

    @Override
    public void start(Level level, LivingEntity entity) {

    }

    @Override
    public void stop(Level level, LivingEntity entity) {

    }

    @Override
    public Map<String, Integer> getRequirements() {
        Map<String, Integer> reqs = new HashMap(
                Map.of(
                        "fool", 9,
                        "door", 7,
                        "hermit", 9,
                        "demoness", 7,
                        "wheel_of_fortune", 8,
                        "abyss", 4,
                        "darkness", 4
                ));

        for(String pathway : BeyonderData.pathways) {
            if (!reqs.containsKey(pathway) && !pathway.equalsIgnoreCase("death"))
                reqs.put(pathway, 4);
        }
        return reqs;
    }


    @Override
    protected float getSpiritualityCost() {
        return 0;
    }
}

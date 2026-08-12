package de.jakob.lotm.beyonders.abilities.wheel_of_fortune;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.attachments.LuckAccumulationComponent;
import de.jakob.lotm.attachments.LuckComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.data.EntityLocation;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LuckReleaseAbility extends Ability {
    public LuckReleaseAbility(String id) {
        super(id, 120);
        canBeUsedInArtifact = false;
        canBeShared = false;

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(70000f, 24000f, 11600f, 5800f, 4550f, 2200f));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 5));
    }

    @Override
    public float getSpiritualityCost() {
        return 100;
    }

    private static final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(192 / 255f, 246 / 255f, 252 / 255f),
            1.5f
    );


    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel)) {
            return;
        }

        LuckAccumulationComponent component = entity.getData(ModAttachments.LUCK_ACCUMULATION_COMPONENT.get());
        long ticks = component.getTicksAccumulated();
        component.setTicksAccumulated(0);

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        int additionalLuck = getAdditionalLuckByTicks(ticks, entitySeq);

        LuckComponent luckComponent = entity.getData(ModAttachments.LUCK_COMPONENT.get());
        luckComponent.addLuckWithMax(additionalLuck, 3000);

        EntityLocation loc = new EntityLocation(entity);
        ParticleUtil.createParticleSpirals(dust, loc, 1.75, 1.75, 2.25, .35, 5, 20 * 35, 15, 8);
    }

    private int getAdditionalLuckByTicks(long ticks, int seq) {
        long mins = ticks % (20 * 60);
        if(mins <= 1) return 0;

        mins *= getLuckPerSeq(seq);

        return Math.clamp(mins, 1, getMaxPerSeq(seq));
    }

    private int getLuckPerSeq(int seq){
        return switch (seq){
            case 5 -> 50;
            case 4 -> 60;
            case 3 -> 75;
            case 2 -> 100;
            case 1 -> 120;
            case 0 -> 150;
            default -> 0;
        };
    }

    private int getMaxPerSeq(int seq){
        return switch (seq){
            case 5 -> 500;
            case 4 -> 750;
            case 3 -> 1000;
            case 2 -> 1500;
            case 1 -> 2000;
            case 0 -> 3000;
            default -> 0;
        };
    }
}

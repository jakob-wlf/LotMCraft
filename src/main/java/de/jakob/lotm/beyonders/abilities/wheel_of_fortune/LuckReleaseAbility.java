package de.jakob.lotm.beyonders.abilities.wheel_of_fortune;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.attachments.LuckAccumulationComponent;
import de.jakob.lotm.attachments.LuckComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.data.EntityLocation;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class LuckReleaseAbility extends Ability {
    public LuckReleaseAbility(String id) {
        super(id, 120);
        canBeUsedInArtifact = false;
        canBeShared = false;
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

        int additionalLuck = getAdditionalLuckByTicks(ticks);

        LuckComponent luckComponent = entity.getData(ModAttachments.LUCK_COMPONENT.get());
        luckComponent.addLuck(-additionalLuck);

        EntityLocation loc = new EntityLocation(entity);
        ParticleUtil.createParticleSpirals(dust, loc, 1.75, 1.75, 2.25, .35, 5, 20 * 35, 15, 8);
    }

    private int getAdditionalLuckByTicks(long ticks) {
        int additionalLuck = Math.round(ticks / (20 * 60 * 2f)) * 120;
        return Math.clamp(additionalLuck, 1, 8);
    }
}

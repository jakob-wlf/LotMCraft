package de.jakob.lotm.beyonders.abilities.visionary.passives;

import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityItem;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class PureIdealism extends PassiveAbilityItem {
    public PureIdealism(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 2));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        var sanity = entity.getData(ModAttachments.SANITY_COMPONENT.get());

        BeyonderData.addModifier(entity, "pure_idealism",
                calculatemultiplier(sanity.getSanity(),
                        getPerfectMultWithSeq(BeyonderData.getSequence(entity))));
    }

    private float calculatemultiplier(float sanity, float mult) {
        return 1.0f + (mult - 1.0f) * sanity;
    }

    private float getPerfectMultWithSeq(int seq){
        return switch (seq){
            case 2 -> 1.05f;
            case 1 -> 1.15f;
            case 0 -> 1.25f;
            default -> 1.0f;
        };
    }
}

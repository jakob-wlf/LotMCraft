package de.jakob.lotm.beyonders.abilities.black_emperor.passives;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;


import java.util.Map;

public class MentalResistanceEmperor extends PassiveAbility {
    public MentalResistanceEmperor(String id) {
        super(id);
    }
    private static float LAST_SANITY = 0f;
    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("black_emperor", 8);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        SanityComponent sanity = entity.getData(ModAttachments.SANITY_COMPONENT.get());
        float currentSanity = sanity.getSanity();
        if(LAST_SANITY == 0f) LAST_SANITY = currentSanity;
        if(currentSanity < LAST_SANITY) {
            float difference = LAST_SANITY - currentSanity;
            float resistance = difference*0.3f;
            sanity.increaseSanityAndSyncIgnoreSequence(resistance, entity);
            entity.setData(ModAttachments.SANITY_COMPONENT.get(), sanity);
        }
        LAST_SANITY = entity.getData(ModAttachments.SANITY_COMPONENT.get()).getSanity();
    }


}

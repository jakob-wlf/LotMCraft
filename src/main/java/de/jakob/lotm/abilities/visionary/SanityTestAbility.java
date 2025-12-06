package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class SanityTestAbility extends AbilityItem {
    public SanityTestAbility(Properties properties) {
        super(properties, .2f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 5));
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel)) return;

        entity.getData(ModAttachments.SANITY_COMPONENT).setSanity(entity.getData(ModAttachments.SANITY_COMPONENT).getSanity() - 0.1f);
    }
}

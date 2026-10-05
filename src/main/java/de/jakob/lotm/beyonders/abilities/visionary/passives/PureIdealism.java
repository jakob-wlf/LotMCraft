package de.jakob.lotm.beyonders.abilities.visionary.passives;

import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.MultiplierModifierComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class PureIdealism extends PassiveAbility {
    public PureIdealism(String id) {
        super(id);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 2));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        var sanity = entity.getData(ModAttachments.SANITY_COMPONENT.get());

        MultiplierModifierComponent component = entity.getData(ModAttachments.MULTIPLIER_MODIFIER_COMPONENT);

        if (!component.hasMultiplier("pure_idealism"))
            BeyonderData.addModifier(entity, "pure_idealism", calculatemultiplier(sanity.getSanity(),
                    getPerfectMultWithSeq(BeyonderData.getSequence(entity))));
        else{
            component.replaceMultiplier("pure_idealism", calculatemultiplier(sanity.getSanity(),
                    getPerfectMultWithSeq(BeyonderData.getSequence(entity))));
        }

    }

    private float calculatemultiplier(float sanity, float mult) {
        return 1.0f + (mult - 1.0f) * sanity;
    }

    private float getPerfectMultWithSeq(int seq) {
        return switch (seq) {
            case 2 -> 1.05f;
            case 1 -> 1.075f;
            case 0 -> 1.10f;
            default -> 1.0f;
        };
    }

    @SubscribeEvent
    public static void multiplierClean(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity entity) {

            MultiplierModifierComponent component = entity.getData(ModAttachments.MULTIPLIER_MODIFIER_COMPONENT);

            String path = BeyonderData.getPathway(entity);
            int seq = BeyonderData.getSequence(entity);
            if (component.hasMultiplier("pure_idealism") && (!path.equals("visionary") || seq > 2)) {
                component.removeMultiplier("pure_idealism");
            }
        }
    }
}

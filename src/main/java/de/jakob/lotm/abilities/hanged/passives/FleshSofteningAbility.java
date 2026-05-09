package de.jakob.lotm.abilities.hanged.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.abilities.hanged.HangedPathwayConstants;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class FleshSofteningAbility extends PassiveAbilityItem {
    private static final float GENERIC_MULTIPLIER = 0.72f;
    private static final float PROJECTILE_MULTIPLIER = 0.4f;
    private static final float BEYONDER_MULTIPLIER = 0.82f;
    private static final float MAX_SPIKE_DAMAGE = 10.0f;

    public FleshSofteningAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of(HangedPathwayConstants.PATHWAY_ID, HangedPathwayConstants.SEQUENCE_ROSE_BISHOP);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (!((FleshSofteningAbility) PassiveAbilityHandler.FLESH_SOFTENING.get()).shouldApplyTo(target)) {
            return;
        }

        if (event.getAmount() <= 0 || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }

        float multiplier = GENERIC_MULTIPLIER;
        if (event.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
            multiplier = PROJECTILE_MULTIPLIER;
        } else if (event.getSource().getEntity() instanceof LivingEntity attacker && BeyonderData.isBeyonder(attacker)) {
            multiplier = BEYONDER_MULTIPLIER;
        }

        float softened = event.getAmount() * multiplier;
        if (!event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)) {
            softened = Math.min(softened, MAX_SPIKE_DAMAGE + (event.getAmount() * 0.18f));
        }

        event.setAmount(Math.max(0.5f, softened));
    }
}

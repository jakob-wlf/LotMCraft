package de.jakob.lotm.beyonders.abilities.death.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityItem;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class UndeadIgnoranceAbility extends PassiveAbilityItem {

    public static final HashSet<LivingEntity> ignoredByUndead = new HashSet<>();

    public UndeadIgnoranceAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("death", 9));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        ignoredByUndead.removeIf(e -> !this.shouldApplyTo(e));
        ignoredByUndead.add(entity);
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity target = event.getNewAboutToBeSetTarget();
        if (target == null) return;
        if (!ignoredByUndead.contains(target)) return;

        LivingEntity attacker = event.getEntity();
        if (attacker.getType().is(EntityTypeTags.UNDEAD)) {
            event.setCanceled(true);
        }
    }
}

package de.jakob.lotm.abilities.door.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class VoidImmunityAbility extends PassiveAbilityItem {
    public static final HashSet<LivingEntity> IMMUNE_ENTITIES = new HashSet<>();

    public VoidImmunityAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 3));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        IMMUNE_ENTITIES.removeIf(e -> !this.shouldApplyTo(e));
        IMMUNE_ENTITIES.add(entity);
    }

    @SubscribeEvent
    public static void onLivingTakeVoidDamage(LivingIncomingDamageEvent event) {
        if(!IMMUNE_ENTITIES.contains(event.getEntity()))
            return;

        if(event.getEntity().position().y < -1000)
            return;

        // Get the damage source
        var source = event.getSource();

        var damageTypeKey = source.typeHolder().unwrapKey();

        if (damageTypeKey.isPresent() && damageTypeKey.get().equals(ResourceKey.create(Registries.DAMAGE_TYPE, DamageTypes.FELL_OUT_OF_WORLD.location()))) {
            event.setCanceled(true);
        }
    }
}

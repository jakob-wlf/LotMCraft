package de.jakob.lotm.abilities.error;

import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class HostControllingAbility extends SelectableAbility {
    public HostControllingAbility(String id) {
        super(id, .5f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.host_controlling.drain_health", "ability.lotmcraft.host_controlling.kill"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity host = ParasitationAbility.getHostForEntity(serverLevel, entity);
        if(host == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.host_controlling.no_host").withColor(0x3240bf));
            return;
        }

        switch (abilityIndex) {
            case 0 -> {
                float healthToDrain = (float) (DamageLookup.lookupDamage(4, .75f) * multiplier(entity));

                host.hurt(entity.damageSources().magic(), healthToDrain);
                entity.heal(healthToDrain);
            }
            case 1 -> {
                host.setHealth(0.5f);
                host.hurt(entity.damageSources().magic(), 1000);
            }
        }
    }
}

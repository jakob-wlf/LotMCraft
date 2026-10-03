package de.jakob.lotm.beyonders.abilities.twilight_giant.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class SupernaturalResistanceAbility extends PassiveAbility {

    private static final float BASE_RESISTANCE = 0.30f;
    private static final float RESISTANCE_PER_SEQUENCE = 0.15f;

    private static SupernaturalResistanceAbility instance;

    public SupernaturalResistanceAbility(String id) {
        super(id);
        instance = this;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 8));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
    }

    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (instance == null || target.level().isClientSide()) return;
        DamageSource source = event.getSource();
        if (!isSupernatural(source) || !instance.shouldApplyTo(target)) return;
        float resistance = BASE_RESISTANCE;
        if (source.getEntity() instanceof LivingEntity attacker && attacker != target) {
            resistance += (BeyonderData.getSequence(target) <= 0 ?RESISTANCE_PER_SEQUENCE *2.5f : RESISTANCE_PER_SEQUENCE ) * (BeyonderData.getSequence(attacker) - BeyonderData.getSequence(target));
        }
        resistance = Mth.clamp(resistance, 0f, 1f);
        if (resistance > 0f) event.setAmount(event.getAmount() * (1f - resistance));
    }

    public static boolean isSupernatural(DamageSource source) {
        if (source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC)) return true;
        return source.typeHolder().unwrapKey()
                .map(key -> key.location().getNamespace().equals(LOTMCraft.MOD_ID) && !key.equals(ModDamageTypes.LOOSING_CONTROL))
                .orElse(false);
    }
}

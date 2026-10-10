package de.jakob.lotm.beyonders.abilities.twilight_giant.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class SupernaturalResistanceAbility extends PassiveAbility {

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
        float resistance = reduction(BeyonderData.getSequence(target));
        if (source.getEntity() instanceof LivingEntity attacker && attacker != target) {
            resistance += 0.15F * (BeyonderData.getSequence(attacker) - BeyonderData.getSequence(target));
        }
        resistance = Mth.clamp(resistance, 0F, 1F);
        if (resistance > 0f) event.setAmount(event.getAmount() * (1f - resistance));
    }

    private static float reduction(int sequence) {
        return switch (sequence) {
            case 0 -> 0.85F;
            case 1 -> 0.75F;
            case 2 -> 0.70F;
            case 3 -> 0.65F;
            case 4 -> 0.55F;
            case 5 -> 0.45F;
            case 6 -> 0.40F;
            case 7 -> 0.35F;
            default -> 0.30F;
        };
    }

    public static boolean isSupernatural(DamageSource source) {
        return source.typeHolder().unwrapKey()
                .map(key -> key.location().getNamespace().equals(LOTMCraft.MOD_ID) && !key.equals(ModDamageTypes.SPACE_DESTRUCTION))
                .orElse(false);
    }
}

package de.jakob.lotm.beyonders.abilities.tyrant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class PerfectBalanceAbility extends ToggleAbility{
    private static final Map<UUID, UUID> currentHitAttacker = new HashMap<>();
    public PerfectBalanceAbility(String id) {super(id);}
    @Override
    public Map<String, Integer> getRequirements() {return new HashMap<>(Map.of("tyrant", 9));}
    protected float getSpiritualityCost() {return 0;}
    @Override
    public void tick(Level level, LivingEntity entity) {if (level.isClientSide) return;}
    @Override
    public void start(Level level, LivingEntity entity) {if (level.isClientSide) return;}
    @Override
    public void stop(Level level, LivingEntity entity) {if (level.isClientSide) return;}
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        Entity source = event.getSource().getEntity();
        if (source instanceof LivingEntity attacker) {currentHitAttacker.put(event.getEntity().getUUID(), attacker.getUUID());}
    }
    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(((ToggleAbility) LOTMCraft.abilityHandler.getById("perfect_balance_ability")).isActiveForEntity(victim))) {return;}
        UUID attackerUUID = currentHitAttacker.get(victim.getUUID());
        if (attackerUUID == null) return;
        Entity attackerEntity = victim.level().getServer().getPlayerList().getPlayer(attackerUUID);
        if (attackerEntity == null && victim.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {attackerEntity = serverLevel.getEntity(attackerUUID);}
        if (!(attackerEntity instanceof LivingEntity attacker)) return;
        int attackerSeq = BeyonderData.getSequence(attacker);
        int victimSeq = BeyonderData.getSequence(victim);
        if (attackerSeq > victimSeq) {event.setStrength(0);}
    }
    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();
        if (!(((ToggleAbility) LOTMCraft.abilityHandler.getById("perfect_balance_ability")).isActiveForEntity(entity))) {return;}
        if (event.getEffectInstance().getEffect() == MobEffects.CONFUSION && event.getEffectInstance().getAmplifier() <= 1) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);}
    }
}

package de.jakob.lotm.beyonders.abilities.abyss.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityHandler;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class DomainOfBodyAbility extends PassiveAbility {

    private static final float REGEN_AMOUNT = 2.0F;
    private static final float CORROSION_RADIUS = 16.0F;
    private static final float CORROSION_DAMAGE = 3.0F;
    private static final float SANITY_DAMAGE = 1.5F;
    private static final long REGEN_BLOCK_TICKS = 20L * 10L;
    private static final Map<UUID, Long> regenBlockedUntil = new ConcurrentHashMap<>();
    private static final long RETALIATION_COOLDOWN_TICKS = 20L * 10L;
    private static final Map<UUID, Long> retaliationCooldowns = new ConcurrentHashMap<>();
    private static boolean retaliating = false;

    public DomainOfBodyAbility(String id) {super(id);}

    @Override
    public Map<String, Integer> getRequirements() {return new HashMap<>(Map.of("abyss", 2));}

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide()) return;
        if (entity.tickCount % 20 != 0) return;

        // Purification damage cancels the regen
        Long blockedUntil = regenBlockedUntil.get(entity.getUUID());
        if (blockedUntil != null) {
            System.out.println("Healing Blocked");
            if (level.getGameTime() < blockedUntil) return;
            regenBlockedUntil.remove(entity.getUUID());
        }

        if (entity.getHealth() < entity.getMaxHealth()) {
            entity.setHealth(Math.min(entity.getMaxHealth(), entity.getHealth() + REGEN_AMOUNT));
        }
    }

    @SubscribeEvent
    public static void onPurificationDamage(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        if (level.isClientSide()) return;
        if (event.getNewDamage() <= 0) return;
        if (!event.getSource().is(ModDamageTypes.PURIFICATION)) return;
        System.out.println("PURIFICATION");
        PassiveAbility ability = PassiveAbilityHandler.getById("domain_of_body_ability");
        if (ability == null || !ability.shouldApplyTo(entity)) return;
        System.out.println("Healing disabled");
        regenBlockedUntil.put(entity.getUUID(), level.getGameTime() + REGEN_BLOCK_TICKS);
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        if (level.isClientSide()) return;
        if (event.getNewDamage() <= 0) return;
        PassiveAbility ability = PassiveAbilityHandler.getById("domain_of_body_ability");
        if (ability == null || !ability.shouldApplyTo(entity)) return;
        // Purification blocks regen
        if (event.getSource().is(ModDamageTypes.PURIFICATION)) {
            regenBlockedUntil.put(entity.getUUID(), level.getGameTime() + REGEN_BLOCK_TICKS);
        }
        // Prevent recursive retaliation
        if (retaliating) return;
        // Check retaliation cooldown
        UUID entityId = entity.getUUID();
        long currentTime = level.getGameTime();
        Long cooldownUntil = retaliationCooldowns.get(entityId);
        if (cooldownUntil != null && currentTime < cooldownUntil) return;
        // Start cooldown
        retaliationCooldowns.put(entityId, currentTime + RETALIATION_COOLDOWN_TICKS);
        // Radius retaliation
        retaliating = true;
        try {
            int entitySeq = BeyonderData.getSequence(entity);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(CORROSION_RADIUS), t -> t != entity && t.isAlive())) {
                target.hurt(level.damageSources().generic(), CORROSION_DAMAGE);
                target.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityWithSequenceDifference(SANITY_DAMAGE, target, entitySeq, BeyonderData.getSequence(target));
            }
        } finally {
            retaliating = false;
        }
    }
}
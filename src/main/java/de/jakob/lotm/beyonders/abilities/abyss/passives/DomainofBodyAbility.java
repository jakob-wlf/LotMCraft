package de.jakob.lotm.beyonders.abilities.abyss.passives;

import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.attachments.ModAttachments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.HashMap;
import java.util.Map;

public class DomainofBodyAbility extends PassiveAbility {

    private static final float REGEN_AMOUNT = 1.0F;
    private static final float CORROSION_RADIUS = 5.0F;
    private static final float CORROSION_DAMAGE = 5.0F;
    private static final float SANITY_DAMAGE = 1.0F;

    public DomainofBodyAbility(String id) {super(id);}
    @Override
    public Map<String, Integer> getRequirements() {return new HashMap<>(Map.of("abyss", 2));}
    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide()) return;
        if (entity.tickCount % 20 != 0) return;
        if (entity.getHealth() < entity.getMaxHealth()) {entity.setHealth(Math.min(entity.getMaxHealth(), entity.getHealth() + REGEN_AMOUNT));
        }
    }
    @SubscribeEvent
    public static void onEntityDamage(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        int EntitySeq = BeyonderData.getSequence(entity);
        if (level.isClientSide()) {return;}
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(CORROSION_RADIUS), target -> target != entity && target.isAlive())) {
            target.hurt(level.damageSources().generic(), CORROSION_DAMAGE);
            target.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityWithSequenceDifference(SANITY_DAMAGE, target, EntitySeq, BeyonderData.getSequence(target));
        }
    }
}

package de.jakob.lotm.dimension;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.AbilityHandler;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityHandler;
import de.jakob.lotm.beyonders.abilities.door.passives.VoidImmunityAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.dimension.ModDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Random;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class SpaceTimeLabyrinthEffect {

    private static final int DAMAGE_INTERVAL_TICKS = 25;

    private static final float DAMAGE_PER_PULSE = 8f;

    private static final double NAUSEA_CHANCE = 0.10;

    private static final int NAUSEA_DURATION_TICKS = 160;

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if(!(event.getEntity().level() instanceof ServerLevel level)) return;
        if(!(event.getEntity() instanceof LivingEntity entity)) return;


        if (!level.dimension().equals(ModDimensions.SPACE_TIME_LABYRINTH_LEVEL_KEY)) return;

        if (entity.tickCount % DAMAGE_INTERVAL_TICKS != 0) return;

        if (((VoidImmunityAbility) PassiveAbilityHandler.VOID_IMMUNITY.get()).shouldApplyTo(entity)) return;

        DamageSource source = ModDamageTypes.source(level, ModDamageTypes.DOOR_SPACE);
        entity.hurt(source, DAMAGE_PER_PULSE);

        if (RANDOM.nextDouble() < NAUSEA_CHANCE) {
            entity.addEffect(new MobEffectInstance(
                    MobEffects.CONFUSION,
                    NAUSEA_DURATION_TICKS,
                    1,
                    false,
                    true,
                    true));
        }
    }
}

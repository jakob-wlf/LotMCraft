package de.jakob.lotm.dimension;

import de.jakob.lotm.dimension.ModDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Random;

public class SpaceTimeLabyrinthEffect {

    private static final int DAMAGE_INTERVAL_TICKS = 40; // every 2 s

    private static final float DAMAGE_PER_PULSE = 0.5f;

    private static final double NAUSEA_CHANCE = 0.10;

    private static final int NAUSEA_DURATION_TICKS = 160;

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ServerLevel level = player.serverLevel();

        if (!level.dimension().equals(ModDimensions.SPACE_TIME_LABYRINTH_LEVEL_KEY)) return;

        if (player.tickCount % DAMAGE_INTERVAL_TICKS != 0) return;

        DamageSource source = level.damageSources().magic();
        player.hurt(source, DAMAGE_PER_PULSE);

        if (RANDOM.nextDouble() < NAUSEA_CHANCE) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.CONFUSION,
                    NAUSEA_DURATION_TICKS,
                    1,
                    false,
                    true,
                    true));
        }
    }
}

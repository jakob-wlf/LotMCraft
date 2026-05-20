package de.jakob.lotm.abilities.black_emperor.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public final class HeroOfTheVillagePassive {

    private static final int REFRESH_INTERVAL = 20;

    private HeroOfTheVillagePassive() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()) return;
        if (!(player.level() instanceof ServerLevel)) return;
        if (!BeyonderData.isBeyonder(player)) return;
        if (!"black_emperor".equals(BeyonderData.getPathway(player))) return;

        int seq = BeyonderData.getSequence(player);
        if (seq > 9) return;

        if (player.level().getGameTime() % REFRESH_INTERVAL != 0) return;

        int amplifier = getAmplifierForSequence(seq);

        player.addEffect(new MobEffectInstance(
                MobEffects.HERO_OF_THE_VILLAGE,
                60,
                amplifier,
                false,
                false,
                true
        ));
    }

    // Lower sequence = stronger effect.
    private static int getAmplifierForSequence(int seq) {
        if (seq <= 0) return 9;
        if (seq == 1) return 8;
        if (seq == 2) return 7;
        if (seq == 3) return 6;
        if (seq == 4) return 5;
        if (seq == 5) return 4;
        if (seq == 6) return 3;
        if (seq == 7) return 2;
        if (seq == 8) return 1;
        return 0;
    }
}
package de.jakob.lotm.beyonders.sefirah;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class SefirahEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(player.tickCount % 10 != 0) return;

        if(!SefirahHandler.hasSefirot(player) && !SefirahHandler.isInSefirot(player)) return;

        if(!SefirahHandler.isInSefirot(player)) {
            player.addEffect(new MobEffectInstance(ModEffects.CONCEALMENT, 80, 5 + 3 * SefirahHandler.getSefirotProgress(player), false, false));
        }
        else {
            player.addEffect(new MobEffectInstance(ModEffects.CONCEALMENT, 80, 20 + 5 * SefirahHandler.getSefirotProgress(player), false, false));
        }

        ArrayList<String> perks = SefirahHandler.getPerksForProgressAndSefirot(SefirahHandler.getSefirotProgress(player), SefirahHandler.getClaimedSefirot(player));
        if(perks.contains("lotm.sefirot.luck")) {
            int maxLuck = switch(SefirahHandler.getSefirotProgress(player)) {
                case 1 -> 100;
                case 2 -> 200;
                case 3 -> 400;
                case 4 -> 800;
                default -> 0;
            };

            if(player.getData(ModAttachments.LUCK_COMPONENT).getLuck() < maxLuck) {
                player.getData(ModAttachments.LUCK_COMPONENT).addLuck(SefirahHandler.getSefirotProgress(player) * 2);
            }
        }
        if(perks.contains("lotm.sefirot.regeneration")) {
            int regen = switch(SefirahHandler.getSefirotProgress(player)) {
                case 1 -> 0;
                case 2, 3 -> 1;
                case 4 -> 3;
                default -> 0;
            };

            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, regen, false, false, false));
        }
        if(perks.contains("lotm.sefirot.damage_boost")) {
            float multiplier = switch(SefirahHandler.getSefirotProgress(player)) {
                case 1 -> 1.05f;
                case 2 -> 1.15f;
                case 3 -> 1.225f;
                case 4 -> 1.35f;
                default -> 0;
            };

            BeyonderData.addModifierWithTimeLimit(player, "sefirot_damage_boost", multiplier, 2000);
        }
    }

}

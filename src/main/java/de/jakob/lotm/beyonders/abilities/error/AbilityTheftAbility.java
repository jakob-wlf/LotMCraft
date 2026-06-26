package de.jakob.lotm.beyonders.abilities.error;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.error.handler.TheftHandler;
import de.jakob.lotm.events.ProhibitionHandler;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.CopiedAbilityHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;


public class AbilityTheftAbility extends Ability {
    public AbilityTheftAbility(String id) {
        super(id, 3f);
        canBeCopied = false;
        canBeReplicated = false;
        canBeShared = false;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel)) {
            if (entity instanceof Player player) {
                player.playSound(SoundEvents.BELL_RESONATE, 1, 1);
            }
            return;
        }
        if (ProhibitionHandler.IsInTheftZone(entity.position(), (ServerLevel) level, AbilityUtil.getSeqWithArt(entity, this))) return;
        LivingEntity target = AbilityUtil.getTargetEntity(entity, (int) (15 * (multiplier(entity) * multiplier(entity))), 2);
        if (target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.ability_theft.no_target").withColor(0x6d32a8));
            return;
        }

        TheftHandler.performAbilityTheft(level, entity, target, random, false, this);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 6));
    }

    @Override
    public float getSpiritualityCost() {
        return 200;
    }

}

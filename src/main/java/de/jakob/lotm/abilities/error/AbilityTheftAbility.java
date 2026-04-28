package de.jakob.lotm.abilities.error;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.error.handler.TheftHandler;
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


public class AbilityTheftAbility extends SelectableAbility {
    public AbilityTheftAbility(String id) {
        super(id, 3f);
        canBeCopied = false;
        canBeReplicated = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 6));
    }

    @Override
    public float getSpiritualityCost() {
        return 200;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.ability_theft.steal",
                "ability.lotmcraft.ability_theft.use_copied"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if (selectedAbility == 0) {
            performTheft(level, entity);
        } else if (selectedAbility == 1) {
            openCopiedAbilityWheel(level, entity);
        }
    }

    private void openCopiedAbilityWheel(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel) || !(entity instanceof ServerPlayer player)) return;
        CopiedAbilityHelper.openCopiedAbilityWheel(player);
    }

    private void performTheft(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel)) {
            if (entity instanceof Player player) {
                player.playSound(SoundEvents.BELL_RESONATE, 1, 1);
            }
            return;
        }
        if (ProhibitionHandler.IsInTheftZone(entity.position(), (ServerLevel) level)) return;
        LivingEntity target = AbilityUtil.getTargetEntity(entity, (int) (15 * (multiplier(entity) * multiplier(entity))), 2);
        if (target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.ability_theft.no_target").withColor(0x6d32a8));
            return;
        }

        TheftHandler.performAbilityTheft(level, entity, target, random, false, this);
    }

}

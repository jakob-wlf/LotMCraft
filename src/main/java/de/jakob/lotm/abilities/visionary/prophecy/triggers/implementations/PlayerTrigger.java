package de.jakob.lotm.abilities.visionary.prophecy.triggers.implementations;

import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.TriggerBase;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.TriggerEnum;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.context.TriggerContextBase;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.context.TriggerContextEnum;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.context.implementations.TriggerPlayerContext;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.LinkedList;
import java.util.UUID;

public class PlayerTrigger extends TriggerBase {
    public PlayerTrigger(ActionBase action, TriggerContextBase context) {
        super(action, context);
    }

    @Override
    public TriggerEnum getType() {
        return TriggerEnum.PLAYER;
    }

    @Override
    public int getRequiredSeq() {
        return 6;
    }

    @Override
    public int checkTrigger(Level level, LivingEntity entity, UUID casterId) {
        if (!(context instanceof TriggerPlayerContext players)) return -1;
        if (!(level instanceof ServerLevel serverLevel)) return -1;

        if (players.names.isEmpty()) return -1;

        LinkedList<ServerPlayer> playersEntities = new LinkedList<>();

        for (var name : players.names) {
            var id = BeyonderData.playerMap.getKeyByName(name);

            if (id != null) {
                var target = serverLevel.getPlayerByUUID(id);

                if (target != null)
                    playersEntities.add((ServerPlayer) target);
            }

        }

        int range = players.range;

        var target = AbilityUtil.getTargetEntity(entity, range > 0 ? range : 100, 2f, true, true);

        for (var e : playersEntities) {
            if (e.equals(target)) {
                action.action(level, entity, casterId);
                return 1;
            }
        }

        return 0;
    }

    public static PlayerTrigger load(CompoundTag tag,
                                     ActionsEnum actionType,
                                     TriggerContextEnum contextType,
                                     HolderLookup.Provider provider) {
        return new PlayerTrigger(ActionBase.load(actionType, tag, provider),
                TriggerContextBase.load(contextType, tag, provider));
    }
}

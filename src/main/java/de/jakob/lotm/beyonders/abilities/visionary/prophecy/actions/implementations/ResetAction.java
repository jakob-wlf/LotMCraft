package de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.Prophecy;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class ResetAction extends ActionBase {
    public ResetAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
    return ActionsEnum.RESET;
    }

    @Override
    public int getRequiredSeq() {
        return 0;
    }

    @Override
    public void action(Level level, LivingEntity entity, UUID casterId) {
        if(!(level instanceof ServerLevel serverLevel)) return;
        if(!(entity instanceof ServerPlayer targetPlayer)) return;

        var dataOp = BeyonderData.playerMap.get(casterId);
        if(dataOp.isEmpty()) return;
        var data = dataOp.get();
        int entitySeq = data.sequence();

        var targetOp = BeyonderData.playerMap.get(targetPlayer.getUUID());
        if(targetOp.isEmpty()) return;

        List<Prophecy> all = new LinkedList<>(
                BeyonderData.playerMap.get(targetPlayer).get().prophecies()
        );

        List<Prophecy> matching = all.stream()
                .filter(obj -> {
                    int seq = BeyonderData.playerMap.get(obj.casterId()).get().sequence();
                    return entitySeq <= obj.trigger().getRequiredSeq()
                            && entitySeq <= obj.trigger().getActionRequiredSeq()
                            && entitySeq <= seq;
                })
                .toList();

        all.removeAll(matching);

        BeyonderData.playerMap.setProphecies(targetPlayer.getUUID(), all);
    }

    public static ResetAction load(CompoundTag tag, HolderLookup.Provider provider) {
            return new ResetAction(ActionContextBase.load(ActionContextEnum.EMPTY, tag, provider));
    }
}

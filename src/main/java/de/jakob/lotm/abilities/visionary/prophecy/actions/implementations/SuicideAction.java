package de.jakob.lotm.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class SuicideAction extends ActionBase {
    public SuicideAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
        return ActionsEnum.SUICIDE;
    }

    @Override
    public int getRequiredSeq() {
        return 4;
    }

    @Override
    public void action(Level level, LivingEntity entity, UUID casterId) {
        var data = BeyonderData.playerMap.get(casterId).get();
        int seq = data.sequence();
        int targetSeq = BeyonderData.getSequence(entity);

        if(AbilityUtil.isTargetSignificantlyWeaker(seq, targetSeq))
            entity.kill();
    }

    public static SuicideAction load(CompoundTag tag, HolderLookup.Provider provider) {
        return new SuicideAction(ActionContextBase.load(ActionContextEnum.EMPTY, tag, provider));
    }
}

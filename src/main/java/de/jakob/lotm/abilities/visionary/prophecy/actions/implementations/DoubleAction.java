package de.jakob.lotm.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.visionary.prophecy.TokenStream;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionsHelper;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.implementations.ActionStringContext;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import javax.swing.text.StringContent;
import java.util.UUID;

public class DoubleAction extends ActionBase {
    public DoubleAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
        return null;
    }

    @Override
    public int getRequiredSeq() {
        return 4;
    }

    @Override
    public void action(Level level, LivingEntity entity, UUID casterId) {
        if(!(entity instanceof ServerPlayer player)) return;
        if(!(context instanceof ActionStringContext string)) return;

        if(string.string.isEmpty()) return;

        String[] parts = string.string.split("\\sand\\s", 2);
        if(parts.length < 2) return;

        var stream1 = new TokenStream("and " + parts[0]);
        var stream2 = new TokenStream("and " + parts[1]);

        var casterData = BeyonderData.playerMap.get(casterId).get();

        var action1 = ActionsHelper.deduceActionWithContextSkipNick(stream1, casterData.sequence(), entity.getUUID());
        var action2 = ActionsHelper.deduceActionWithContextSkipNick(stream2, casterData.sequence(), entity.getUUID());

        if(action1 != null && !(action1 instanceof DoubleAction))
            action1.action(level, entity, casterId);

        if(action2 != null && !(action2 instanceof DoubleAction))
            action2.action(level, entity, casterId);
    }

    public static DoubleAction load(CompoundTag tag, HolderLookup.Provider provider) {
        return new DoubleAction(ActionContextBase.load(ActionContextEnum.STRING, tag, provider));
    }
}

package de.jakob.lotm.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.AbilityHandler;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.visionary.prophecy.TokenStream;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.implementations.ActionStringContext;
import de.jakob.lotm.artifacts.SealedArtifactItem;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class UseSkillAction extends ActionBase {
    public UseSkillAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
        return ActionsEnum.SKILL;
    }

    @Override
    public int getRequiredSeq() {
        return 4;
    }

    @Override
    public void action(Level level, LivingEntity entity) {
        if(!(context instanceof ActionStringContext string)) return;
        if(!(level instanceof ServerLevel serverLevel)) return;

        TokenStream stream = new TokenStream(string.string);

        if(stream.match("skill"))
            stream.next();

        var ability = LOTMCraft.abilityHandler.getById(stream.peek());

        if(ability == null) return;

        stream.next();
        AbilityUtil.ignoreAllies.put(entity.getUUID(), false);

        if(ability instanceof SelectableAbility selectableAbility){
            int option = 0;

            try{
                option = Integer.parseInt(stream.peek());
            }catch (NumberFormatException ignored) {}

            selectableAbility.setSelectedAbility((ServerPlayer) entity, option);
            selectableAbility.onAbilityUse(serverLevel, entity);
            return;
        }

        ability.useAbility(serverLevel, entity);
    }

    public static UseSkillAction load(CompoundTag tag, HolderLookup.Provider provider) {
        return new UseSkillAction(ActionContextBase.load(ActionContextEnum.STRING, tag, provider));
    }
}

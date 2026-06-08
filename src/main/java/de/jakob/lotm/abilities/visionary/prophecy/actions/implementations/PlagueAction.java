package de.jakob.lotm.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.abilities.visionary.prophecy.TokenStream;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.implementations.ActionStringContext;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.TimeChangeEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class PlagueAction extends ActionBase {
    public PlagueAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
        return ActionsEnum.PLAGUE;
    }

    @Override
    public int getRequiredSeq() {
        return 4;
    }

    @Override
    public void action(Level level, LivingEntity entity, UUID casterId) {
        if(!(context instanceof ActionStringContext string)) return;
        if(!(level instanceof ServerLevel serverLevel)) return;

        var stream = new TokenStream(string.string);

        var component = entity.getData(ModAttachments.MENTAL_PLAGUE.get());
        var casterData = BeyonderData.playerMap.get(casterId).get();

        switch (stream.peek()){
           case "place" ->{
               component.place(casterData.trueName(), casterData.sequence());
           }

           case "activate" ->{
               if(component.isOwner(casterId) && component.hasMentalPlague()){
                   component.activate();
               }
           }
        }
    }

    public static PlagueAction load(CompoundTag tag, HolderLookup.Provider provider) {
        return new PlagueAction(ActionContextBase.load(ActionContextEnum.STRING, tag, provider));
    }

}

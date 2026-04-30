package de.jakob.lotm.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.implementations.ActionStringContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ServerChatEvent;

public class WhispersAction extends ActionBase {
    public WhispersAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
        return ActionsEnum.WHISPERS;
    }

    @Override
    public int getRequiredSeq() {
        return 7;
    }

    @Override
    public void action(Level level, LivingEntity entity) {
        if(!(context instanceof ActionStringContext string)) return;
        if(!(level instanceof ServerLevel serverLevel)) return;
        if(!(entity instanceof ServerPlayer player)) return;

        String msg = string.string;
        if(msg.isEmpty()) return;

        player.sendSystemMessage(Component.literal(msg).withStyle(ChatFormatting.GREEN));
    }

    public static WhispersAction load(CompoundTag tag, HolderLookup.Provider provider) {
        return new WhispersAction(ActionContextBase.load(ActionContextEnum.STRING, tag, provider));
    }
}

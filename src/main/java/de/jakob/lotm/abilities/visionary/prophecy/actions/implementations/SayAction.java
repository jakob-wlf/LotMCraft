package de.jakob.lotm.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.implementations.ActionStringContext;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ServerChatEvent;
import org.checkerframework.checker.units.qual.C;

import java.util.UUID;

public class SayAction extends ActionBase {
    public SayAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
        return ActionsEnum.SAY;
    }

    @Override
    public int getRequiredSeq() {
        return 6;
    }

    @Override
    public void action(Level level, LivingEntity entity, UUID casterId) {
        if(!(context instanceof ActionStringContext string)) return;
        if(!(entity instanceof ServerPlayer player)) return;

        Component component = Component.literal(string.string);

        ServerChatEvent event = new ServerChatEvent(player, string.string, component);

        NeoForge.EVENT_BUS.post(event);

        if (!event.isCanceled()) {
            PlayerChatMessage msg = PlayerChatMessage.system(string.string);

            player.getServer().getPlayerList().broadcastChatMessage(msg, player, ChatType.bind(ChatType.CHAT, player));
        }
    }

    public static SayAction load(CompoundTag tag, HolderLookup.Provider provider) {
        return new SayAction(ActionContextBase.load(ActionContextEnum.STRING, tag, provider));
    }
}

package de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.visionary.passives.MetaAwarenessAbility;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.TokenStream;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionsHelper;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.implementations.EmptyAction;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.TriggerContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.TriggerContextEnum;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class TriggerHelper {
    private static @Nullable TriggerEnum getType(String str){
        return switch (str){
            case "on" -> TriggerEnum.POSITION;
            case "has" -> TriggerEnum.PICK_UP;
            case "instant" -> TriggerEnum.INSTANT;
            case "health" -> TriggerEnum.HEALTH;
            case "sanity" -> TriggerEnum.SANITY;
            case "player" -> TriggerEnum.PLAYER;
            case "sealed" -> TriggerEnum.SEALED;
            case "hunger" -> TriggerEnum.HUNGER;
            case "riding" -> TriggerEnum.RIDING;
            case "spirituality" -> TriggerEnum.SPIRITUALITY;
            case "sequence" -> TriggerEnum.SEQUENCE;
            case "pathway" -> TriggerEnum.PATHWAY;
            case "light" -> TriggerEnum.LIGHT;
            case "asleep" -> TriggerEnum.ASLEEP;
            case "chain" -> TriggerEnum.CHAIN;
            default -> null;
        };
    }

    private static TriggerContextEnum getContextType(TriggerEnum value){
        return switch (value){
            case POSITION -> TriggerContextEnum.POSITION;
            case PICK_UP -> TriggerContextEnum.ITEM;
            case INSTANT -> TriggerContextEnum.EMPTY;
            case HEALTH -> TriggerContextEnum.NUMBER;
            case SANITY -> TriggerContextEnum.NUMBER;
            case PLAYER -> TriggerContextEnum.PLAYER;
            case SEALED -> TriggerContextEnum.EMPTY;
            case HUNGER -> TriggerContextEnum.NUMBER;
            case RIDING -> TriggerContextEnum.EMPTY;
            case SPIRITUALITY -> TriggerContextEnum.NUMBER;
            case SEQUENCE -> TriggerContextEnum.NUMBER;
            case PATHWAY -> TriggerContextEnum.STRING;
            case LIGHT -> TriggerContextEnum.NUMBER;
            case ASLEEP -> TriggerContextEnum.EMPTY;
            case CHAIN -> TriggerContextEnum.CHAIN;
        };
    }

    public static @Nullable Integer getDistanceToTarget(LivingEntity caster, UUID targetId){
        if(!(caster instanceof ServerPlayer player)) return null;

        var targetEntity = caster.level().getPlayerByUUID(targetId);

        if(targetEntity == null) return null;

        return (int) caster.distanceTo(targetEntity);
    }

    public static @Nullable UUID getUUIDFromNick(String str){
        TokenStream stream = new TokenStream(str);

        stream.next();

        String nick = stream.peek();
        return BeyonderData.playerMap.getKeyByName(nick);
    }

    public static @Nullable TriggerBase deduceWithoutAction(String str, int casterSeq){
        TokenStream stream = new TokenStream(str);

        LOTMCraft.LOGGER.info("In construct - str: {}, stream: {}", str, stream.toString());

        String nick = stream.peek();
        UUID id = BeyonderData.playerMap.getKeyByName(nick);

        LOTMCraft.LOGGER.info("In construct - nick: {}", nick);

        if(id == null) return null;

        var data = BeyonderData.playerMap.get(id).get();
        if(casterSeq > data.sequence() && data.pathway().equals("visionary")){
            return null;
        }

        stream.next();
        var type = getType(Objects.requireNonNull(stream.peek()));

        if(type == null) return null;

        LOTMCraft.LOGGER.info("In construct - type: {}", type.toString());

        var contextType = getContextType(type);
        if(contextType == null) return null;

        var context = TriggerContextBase.create(contextType, id);
        context.fillFromStream(stream);

        LOTMCraft.LOGGER.info("In construct - context: {}", context.toString());

        EmptyAction emptyAction = (EmptyAction) ActionBase.create(ActionsEnum.EMPTY,
                ActionContextBase.create(ActionContextEnum.EMPTY, id));

        return TriggerBase.create(type, emptyAction, context);
    }

    public static @Nullable TriggerBase deduceWithContext(String str, int casterSeq, ServerPlayer caster){
        TokenStream stream = new TokenStream(str);

        stream.next();

        String nick = stream.peek();
        UUID id = BeyonderData.playerMap.getKeyByName(nick);

        if(id == null) return null;

        var data = BeyonderData.playerMap.get(id).get();
        if(casterSeq > data.sequence() && data.pathway().equals("visionary")){
            var target = caster.level().getPlayerByUUID(id);
            if(data.sequence() <= 1 && target != null){
                MetaAwarenessAbility.onDivined(caster, (ServerPlayer) target);
            }

            return null;
        }

        stream.next();
        var type = getType(Objects.requireNonNull(stream.peek()));

        if(type == null) return null;

        var contextType = getContextType(type);
        if(contextType == null) return null;

        var context = TriggerContextBase.create(contextType, id);
        context.fillFromStream(stream);

        var action = ActionsHelper.deduceActionWithContext(str, casterSeq);
        if(action == null) return null;

        var trigger = TriggerBase.create(type, action, context);

        if(trigger.getRequiredSeq() < casterSeq) return null;

        int amount = data.prophecies().stream().filter(obj -> obj.casterId().equals(caster.getUUID())).toList().size();
        int amountPerSeq = getAmountPerSeq(casterSeq);

        if(amount + 1 > amountPerSeq) return null;

        int count = data.prophecies().stream().filter(obj ->
                obj.trigger().getType().equals(trigger.getType())
                        && obj.trigger().getActionType().equals(action.getType())).toList().size();

        if(count + 1 > getCountPerSeq(casterSeq)) return null;

        return trigger;
    }

    public static int getAmountPerSeq(int seq){
        return switch (seq){
            case 7 -> 5;
            case 6,5 -> 10;
            case 4,3 -> 15;
            case 2 -> 25;
            case 1 -> 40;
            case 0 -> 80;
            case -1 -> 9999999;
            default -> 1;
        };
    }

    public static int getCountPerSeq(int seq){
        return switch (seq){
            case 7 -> 1;
            case 6,5 -> 2;
            case 4,3 -> 4;
            case 2 -> 5;
            case 1 -> 7;
            case 0 -> 9;
            case -1 -> 99999999;
            default -> 0;
        };
    }

    public static Item getItemFromString(String input) {
        return ActionsHelper.getItemFromString(input);
    }
}

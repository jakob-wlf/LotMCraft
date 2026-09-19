package de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers;

import de.jakob.lotm.beyonders.abilities.visionary.passives.MetaAwarenessAbility;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.TokenStream;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionsHelper;
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
    public record ParseResult(@Nullable TriggerBase trigger, @Nullable String failureReason) {
        public boolean succeeded() {
            return trigger != null;
        }
    }

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
            case "become" -> TriggerEnum.BECOME;
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
            case BECOME -> TriggerContextEnum.PATH_SEQ;
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

    public static @Nullable TriggerBase deduceWithContext(String str, int casterSeq, ServerPlayer caster){
        return deduceDetailed(str, casterSeq, caster).trigger();
    }

    public static ParseResult deduceDetailed(String str, int casterSeq, ServerPlayer caster){
        TokenStream stream = new TokenStream(str);

        if (!stream.match("if")) {
            return new ParseResult(null, "Start with: if <player> <trigger> then <action>");
        }

        stream.next();

        String nick = stream.peek();
        if (nick == null) {
            return new ParseResult(null, "Missing target player name.");
        }

        UUID id = nick != null && nick.equalsIgnoreCase("self")
            ? caster.getUUID()
            : BeyonderData.playerMap.getKeyByName(nick);

        if(id == null) return new ParseResult(null, "Unknown player '" + nick + "'. Use their exact name or 'self'.");

        var storedData = BeyonderData.playerMap.get(id);
        if (storedData.isEmpty()) {
            return new ParseResult(null, "No Beyonder data exists for '" + nick + "'.");
        }

        var data = storedData.get();
        if(casterSeq > data.sequence() && data.pathway().equals("visionary")){
            var target = caster.level().getPlayerByUUID(id);
            if(data.sequence() <= 1 && target != null){
                MetaAwarenessAbility.onDivined(caster, (ServerPlayer) target);
            }

            return new ParseResult(null, "The target is a stronger Visionary and resisted the attempt.");
        }

        stream.next();
        String triggerKeyword = stream.peek();
        if (triggerKeyword == null) {
            return new ParseResult(null, "Missing trigger after the target name.");
        }

        var type = getType(triggerKeyword.toLowerCase());

        if(type == null) return new ParseResult(null, "Unknown trigger '" + triggerKeyword + "'.");

        var contextType = getContextType(type);

        TriggerContextBase context;
        try {
            context = TriggerContextBase.create(contextType, id);
            context.fillFromStream(stream);
        } catch (RuntimeException exception) {
            return new ParseResult(null, "Invalid arguments for trigger '" + triggerKeyword + "'.");
        }

        ActionsHelper.ParseResult actionResult = ActionsHelper.deduceActionDetailed(str, casterSeq, id);
        if(!actionResult.succeeded()) return new ParseResult(null, actionResult.failureReason());

        var trigger = TriggerBase.create(type, actionResult.action(), context);

        if(trigger.getRequiredSeq() < casterSeq) {
            return new ParseResult(null, "Trigger '" + triggerKeyword + "' requires sequence "
                    + trigger.getRequiredSeq() + " or stronger.");
        }

        int amount = data.prophecies().stream().filter(obj -> obj.casterId().equals(caster.getUUID())).toList().size();
        int amountPerSeq = getAmountPerSeq(casterSeq);

        if(amount + 1 > amountPerSeq) {
            return new ParseResult(null, "You have reached your limit of " + amountPerSeq
                + " active prophecies on this target.");
        }

        int count = data.prophecies().stream().filter(obj ->
                obj.trigger().getType().equals(trigger.getType())
                && obj.trigger().getActionType().equals(trigger.getActionType())).toList().size();

        int typeLimit = getCountPerSeq(casterSeq);
        if(count + 1 > typeLimit) {
            return new ParseResult(null, "You have reached the limit of " + typeLimit
                + " matching trigger/action prophecies on this target.");
        }

        return new ParseResult(trigger, null);
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

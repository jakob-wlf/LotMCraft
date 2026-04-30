package de.jakob.lotm.abilities.visionary.prophecy.actions;

import de.jakob.lotm.abilities.visionary.prophecy.TokenStream;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.implementations.ActionStringContext;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class ActionsHelper {

    private static @Nullable ActionsEnum getType(String str){
        return switch (str){
            case "drop" -> ActionsEnum.DROP_ITEM;
            case "teleport" -> ActionsEnum.TELEPORT;
            case "digest" -> ActionsEnum.DIGESTION;
            case "health" -> ActionsEnum.HEALTH;
            case "sanity" -> ActionsEnum.SANITY;
            case "calamity" -> ActionsEnum.CALAMITY;
            case "stun" -> ActionsEnum.STUN;
            case "skill" -> ActionsEnum.SKILL;
            case "confusion" -> ActionsEnum.CONFUSION;
            case "seal" -> ActionsEnum.SEAL;
            case "unseal" -> ActionsEnum.UNSEAL;
            case "spawn" -> ActionsEnum.SPAWN;
            case "say" -> ActionsEnum.SAY;
            case "weather" -> ActionsEnum.WEATHER;
            case "time" -> ActionsEnum.TIME;
            case  "whispers" -> ActionsEnum.WHISPERS;
            default -> null;
        };
    }

    public static ActionContextEnum getContextType(ActionsEnum value){
        return switch (value){
            case ActionsEnum.DROP_ITEM -> ActionContextEnum.ITEM;
            case ActionsEnum.TELEPORT -> ActionContextEnum.POSITION;
            case ActionsEnum.DIGESTION -> ActionContextEnum.NUMBER;
            case ActionsEnum.HEALTH -> ActionContextEnum.NUMBER;
            case ActionsEnum.SANITY -> ActionContextEnum.NUMBER;
            case ActionsEnum.CALAMITY -> ActionContextEnum.STRING;
            case ActionsEnum.STUN -> ActionContextEnum.EMPTY;
            case ActionsEnum.SKILL -> ActionContextEnum.STRING;
            case ActionsEnum.CONFUSION -> ActionContextEnum.EMPTY;
            case ActionsEnum.SEAL -> ActionContextEnum.EMPTY;
            case ActionsEnum.UNSEAL -> ActionContextEnum.EMPTY;
            case ActionsEnum.SPAWN -> ActionContextEnum.STRING;
            case ActionsEnum.SAY -> ActionContextEnum.STRING;
            case ActionsEnum.WEATHER -> ActionContextEnum.STRING;
            case ActionsEnum.TIME -> ActionContextEnum.STRING;
            case ActionsEnum.WHISPERS -> ActionContextEnum.STRING;
        };
    }

    public static @Nullable ActionBase deduceActionWithContext(String str, int casterSeq){
        TokenStream stream = new TokenStream(str);

        stream.next();
        String nick = stream.peek();
        UUID id = BeyonderData.playerMap.getKeyByName(nick);

        if(id == null) return null;

        return deduceActionWithContextSkipNick(stream, casterSeq, id);
    }

    public static @Nullable ActionBase deduceActionWithContextSkipNick(TokenStream stream, int casterSeq, UUID id){
        stream = moveToThenOrAnd(stream);
        if(stream == null) return null;

        stream.next();
        var actionType = getType(Objects.requireNonNull(stream.peek()));
        if(actionType == null) return null;

        var contextType = getContextType(actionType);
        if(contextType == null) return null;

        var context = ActionContextBase.create(contextType, id);
        context.fillFromStream(stream);

        var action = ActionBase.create(actionType, context);
        if(action.getRequiredSeq() < casterSeq) return null;

        return action;
    }

    private static TokenStream moveToThenOrAnd(TokenStream stream){
        if(stream.match("then") || stream.match("and")) return stream;
        else if (stream.isEmpty()) return null;
        else {
            stream.next();
            return moveToThenOrAnd(stream);
        }
    }

    public static Item getItemFromString(String input) {
        ResourceLocation id = ResourceLocation.tryParse(input);

        if (id == null) {
            return null;
        }

        return BuiltInRegistries.ITEM.getOptional(id).orElse(null);
    }
}

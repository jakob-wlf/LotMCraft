package de.jakob.lotm.command;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.jakob.lotm.events.HonorificNamesEventHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.beyonderMap.HonorificName;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

import java.util.LinkedList;
import java.util.List;

public class HonorificNameCommand {

    private static LiteralArgumentBuilder<CommandSourceStack> sendMessage() {
        return Commands.literal("sendmessage")
                .then(Commands.argument("player", EntityArgument.entity())
                        .then(Commands.argument("target", EntityArgument.entity())
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();

                                    var playerEntity = EntityArgument.getEntity(context, "player");
                                    var targetEntity = EntityArgument.getEntity(context, "target");

                                    if(!(targetEntity instanceof LivingEntity target)
                                            || (!(playerEntity instanceof LivingEntity player))){
                                        source.sendFailure(Component.literal("Targets must be a living entities!"));
                                        return 0;
                                    }

                                    HonorificNamesEventHandler.isInTransferring.put(target.getUUID(), player.getUUID());
                                    return 1;
                                }))
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> ui() {
        return Commands.literal("ui")
                .then(sendMessage());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("honorificname")
                .then(ui())
                .then(set())
        );
    }

    public static LiteralArgumentBuilder<CommandSourceStack> set() {
        return Commands.literal("set")
                .then(Commands.argument("json", StringArgumentType.greedyString())
                        .executes(context ->{
                            CommandSourceStack source = context.getSource();
                            String rawJson = StringArgumentType.getString(context, "json");

                            try {
                                JsonObject obj = JsonParser.parseString(rawJson).getAsJsonObject();

                                String line1 = obj.get("line1").getAsString();
                                String line2 = obj.get("line2").getAsString();
                                String line3 = obj.get("line3").getAsString();
                                String line4 = null;

                                if(obj.has("line_4"))
                                    line4 = obj.get("line4").getAsString();

                                if(BeyonderData.getSequence(source.getPlayer()) >= 4){
                                    source.sendFailure(Component.literal("You must be sequence 3 or higher to utilize honorific name!"));
                                    return 0;
                                }

                                if(BeyonderData.getSequence(source.getPlayer()) == 3 && line4 == null){
                                    source.sendFailure(Component.literal("You must have 4 lines in honorific name as sequence 3"));
                                    return 0;
                                }

                                HonorificName name = new HonorificName((LinkedList<String>) List.of(line1, line2, line3));
                                if(line4 != null)
                                    name = name.addLine(line4);

                                BeyonderData.setHonorificName(source.getPlayer(), name);
                            }
                            catch (Exception e) {
                                source.sendFailure(Component.literal("Invalid json format!"));
                                return 0;
                            }

                            return 1;
                        })
                );
    }
}

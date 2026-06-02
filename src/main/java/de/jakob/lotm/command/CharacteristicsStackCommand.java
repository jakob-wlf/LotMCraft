package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public class CharacteristicsStackCommand {

    private static final SuggestionProvider<CommandSourceStack> PATHWAY_SUGGESTIONS =
            (context, builder) -> SharedSuggestionProvider.suggest(BeyonderData.implementedPathways, builder);

    private static LiteralArgumentBuilder<CommandSourceStack> set() {
        return Commands.literal("set")
                .then(Commands.argument("target", EntityArgument.entity())
                        .then(Commands.argument("seq", IntegerArgumentType.integer())
                                .then(Commands.argument("stack", IntegerArgumentType.integer())
                                        .executes(context -> {
                                                    CommandSourceStack source = context.getSource();
                                                    var targetEntity = EntityArgument.getEntity(context, "target");
                                                    var seq = IntegerArgumentType.getInteger(context, "seq");
                                                    var stack = IntegerArgumentType.getInteger(context, "stack");

                                                    if (!(targetEntity instanceof LivingEntity livingEntity)
                                                            || !(BeyonderData.isBeyonder(livingEntity))) {
                                                        source.sendFailure(Component.literal("Target must be a living beyonder entity!"));
                                                        return 0;
                                                    }

                                                    if(seq >= LOTMCraft.NON_BEYONDER_SEQ || seq < 0){
                                                        source.sendFailure(Component.literal("Invalid sequence!"));
                                                        return 0;
                                                    }

                                                    if(stack < 0){
                                                        source.sendFailure(Component.literal("Invalid stack value!"));
                                                        return 0;
                                                    }

                                                    BeyonderData.setCharacteristic(livingEntity, stack, seq, true, BeyonderData.getPathway(livingEntity));

                                                    return 1;
                                                }
                                        )
                                        .then(Commands.argument("pathway", StringArgumentType.string())
                                                .suggests(PATHWAY_SUGGESTIONS)
                                                .executes(context -> {
                                                    CommandSourceStack source = context.getSource();
                                                    var targetEntity = EntityArgument.getEntity(context, "target");
                                                    var seq = IntegerArgumentType.getInteger(context, "seq");
                                                    var stack = IntegerArgumentType.getInteger(context, "stack");
                                                    var pathway = StringArgumentType.getString(context, "pathway");

                                                    if (!(targetEntity instanceof LivingEntity livingEntity)
                                                            || !(BeyonderData.isBeyonder(livingEntity))) {
                                                        source.sendFailure(Component.literal("Target must be a living beyonder entity!"));
                                                        return 0;
                                                    }

                                                    if(seq >= LOTMCraft.NON_BEYONDER_SEQ || seq < 0){
                                                        source.sendFailure(Component.literal("Invalid sequence!"));
                                                        return 0;
                                                    }

                                                    if(stack < 0){
                                                        source.sendFailure(Component.literal("Invalid stack value!"));
                                                        return 0;
                                                    }

                                                    BeyonderData.setCharacteristic(livingEntity, stack, seq, true, pathway);

                                                    return 1;
                                                })
                                        )
                                )
                        )
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> delete() {
        return Commands.literal("delete")
                .then(Commands.argument("target", EntityArgument.entity())
                        .then(Commands.argument("seq", IntegerArgumentType.integer())
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    var targetEntity = EntityArgument.getEntity(context, "target");
                                    var seq = IntegerArgumentType.getInteger(context, "seq");

                                    if (!(targetEntity instanceof LivingEntity livingEntity)
                                            || !(BeyonderData.isBeyonder(livingEntity))) {
                                        source.sendFailure(Component.literal("Target must be a living beyonder entity!"));
                                        return 0;
                                    }

                                    if(seq >= LOTMCraft.NON_BEYONDER_SEQ || seq < 0){
                                        source.sendFailure(Component.literal("Invalid sequence!"));
                                        return 0;
                                    }

                                    BeyonderData.setCharacteristic(livingEntity, 0, seq, true, BeyonderData.getPathway(livingEntity));

                                    return 1;
                                })
                                .then(Commands.argument("pathway", StringArgumentType.string())
                                        .suggests(PATHWAY_SUGGESTIONS)
                                        .executes(context -> {
                                            CommandSourceStack source = context.getSource();
                                            var targetEntity = EntityArgument.getEntity(context, "target");
                                            var seq = IntegerArgumentType.getInteger(context, "seq");
                                            var pathway = StringArgumentType.getString(context, "pathway");

                                            if (!(targetEntity instanceof LivingEntity livingEntity)
                                                    || !(BeyonderData.isBeyonder(livingEntity))) {
                                                source.sendFailure(Component.literal("Target must be a living beyonder entity!"));
                                                return 0;
                                            }

                                            if(seq >= LOTMCraft.NON_BEYONDER_SEQ || seq < 0){
                                                source.sendFailure(Component.literal("Invalid sequence!"));
                                                return 0;
                                            }

                                            BeyonderData.setCharacteristic(livingEntity, 0, seq, true, pathway);

                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("all")
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    var targetEntity = EntityArgument.getEntity(context, "target");

                                    if (!(targetEntity instanceof LivingEntity livingEntity)
                                            || !(BeyonderData.isBeyonder(livingEntity))) {
                                        source.sendFailure(Component.literal("Target must be a living beyonder entity!"));
                                        return 0;
                                    }

                                    BeyonderData.clearCharacteristics(livingEntity);

                                    BeyonderData.recalculateCharStackModifiers(livingEntity);

                                    return 1;
                                })
                        )
                        .then(Commands.literal("modifiers")
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    var targetEntity = EntityArgument.getEntity(context, "target");

                                    if (!(targetEntity instanceof LivingEntity livingEntity)
                                            || !(BeyonderData.isBeyonder(livingEntity))) {
                                        source.sendFailure(Component.literal("Target must be a living beyonder entity!"));
                                        return 0;
                                    }

                                    for(int i = 9; i >= BeyonderData.getSequence(livingEntity); i--) {
                                        BeyonderData.removeModifier(livingEntity, BeyonderData.CHAR_STACK_BOOST_ID + "_" + i);
                                    }

                                    return 1;
                                })))
                ;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> recalculate() {
        return Commands.literal("recalculate")
                .then(Commands.argument("target" , EntityArgument.entity())
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            var targetEntity = EntityArgument.getEntity(context, "target");

                            if (!(targetEntity instanceof LivingEntity livingEntity)
                                    || !(BeyonderData.isBeyonder(livingEntity))) {
                                source.sendFailure(Component.literal("Target must be a living beyonder entity!"));
                                return 0;
                            }

                            BeyonderData.recalculateCharStackModifiers(livingEntity);

                            return 1;
                        }));
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("characteristicstack")
                .requires(source -> source.hasPermission(2))
                .then(set())
                .then(delete())
                .then(recalculate())
        );
    }
}
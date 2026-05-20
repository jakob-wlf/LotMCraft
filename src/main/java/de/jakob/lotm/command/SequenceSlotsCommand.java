package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.pathways.PathwayInfos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.util.Locale;

public class SequenceSlotsCommand {
    private static final ResourceLocation MONO_FONT = ResourceLocation.fromNamespaceAndPath("minecraft", "uniform");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("showavalibleseqslots")
                .requires(source -> source.hasPermission(2))
                .executes(context -> showAvailable(context.getSource()))
        );

        dispatcher.register(Commands.literal("showclaimedseqslots")
                .requires(source -> source.hasPermission(2))
                .executes(context -> showClaimed(context.getSource()))
        );
    }

    private static int showAvailable(CommandSourceStack source) {
        if (BeyonderData.playerMap == null) {
            source.sendFailure(Component.literal("BeyonderMap is not initialized."));
            return 0;
        }

        ServerLevel level = source.getLevel();
        int limit0 = level.getGameRules().getInt(ModGameRules.SEQ_0_AMOUNT);
        int limit1 = level.getGameRules().getInt(ModGameRules.SEQ_1_AMOUNT);
        int limit2 = level.getGameRules().getInt(ModGameRules.SEQ_2_AMOUNT);
        int limit3 = level.getGameRules().getInt(ModGameRules.SEQ_3_AMOUNT);
        int limit4 = level.getGameRules().getInt(ModGameRules.SEQ_4_AMOUNT);
        int limit5 = level.getGameRules().getInt(ModGameRules.SEQ_5_AMOUNT);
        int limit6 = level.getGameRules().getInt(ModGameRules.SEQ_6_AMOUNT);
        int limit7 = level.getGameRules().getInt(ModGameRules.SEQ_7_AMOUNT);
        int limit8 = level.getGameRules().getInt(ModGameRules.SEQ_8_AMOUNT);

        source.sendSystemMessage(Component.literal("---- Available sequence slots (seq 0-8) ----"));
        int nameWidth = getMaxPathwayNameWidth();
        source.sendSystemMessage(headerLineAvailable(nameWidth));

        for (String pathway : BeyonderData.pathways) {
            String displayName = getPathwayDisplayName(pathway);
            int seq0 = BeyonderData.countTotalSequence(level, pathway, 0);
            int seq1 = BeyonderData.countTotalSequence(level, pathway, 1);
            int seq4 = BeyonderData.countTotalSequence(level, pathway, 4);
            int seq5 = BeyonderData.countTotalSequence(level, pathway, 5);
            int seq6 = BeyonderData.countTotalSequence(level, pathway, 6);
            int seq7 = BeyonderData.countTotalSequence(level, pathway, 7);
            int seq8 = BeyonderData.countTotalSequence(level, pathway, 8);
            int seq2 = BeyonderData.countTotalSequence(level, pathway, 2);
            int seq3 = BeyonderData.countTotalSequence(level, pathway, 3);

                int open0 = Math.max(0, limit0 - seq0);
                int open1 = Math.max(0, limit1 - seq1);
            int open2 = Math.max(0, limit2 - (seq2 + seq1));
            int open3 = Math.max(0, limit3 - (seq3 + seq2));
            int open4 = Math.max(0, limit4 - (seq4 + seq3));
            int open5 = Math.max(0, limit5 - (seq5 + seq4));
            int open6 = Math.max(0, limit6 - (seq6 + seq5));
            int open7 = Math.max(0, limit7 - (seq7 + seq6));
            int open8 = Math.max(0, limit8 - (seq8 + seq7));

                    int padding = Math.max(0, nameWidth - displayName.length());
                    String numbers = String.format(Locale.ROOT,
                            " | %4d %4d %4d %4d %4d %4d %4d %4d %4d",
                            open0, open1, open2, open3, open4, open5, open6, open7, open8
                );
                    source.sendSystemMessage(coloredPathLine(pathway, displayName, padding, numbers));
        }

        return 1;
    }

    private static int showClaimed(CommandSourceStack source) {
        if (BeyonderData.playerMap == null) {
            source.sendFailure(Component.literal("BeyonderMap is not initialized."));
            return 0;
        }

        ServerLevel level = source.getLevel();
        source.sendSystemMessage(Component.literal("---- Claimed sequence slots (seq 0-8) ----"));
        int nameWidth = getMaxPathwayNameWidth();
        source.sendSystemMessage(headerLineClaimed(nameWidth));

        for (String pathway : BeyonderData.pathways) {
            String displayName = getPathwayDisplayName(pathway);
            int seq0 = BeyonderData.countTotalSequence(level, pathway, 0);
            int seq1 = BeyonderData.countTotalSequence(level, pathway, 1);
            int seq2 = BeyonderData.countTotalSequence(level, pathway, 2);
            int seq3 = BeyonderData.countTotalSequence(level, pathway, 3);
            int seq4 = BeyonderData.countTotalSequence(level, pathway, 4);
            int seq5 = BeyonderData.countTotalSequence(level, pathway, 5);
            int seq6 = BeyonderData.countTotalSequence(level, pathway, 6);
            int seq7 = BeyonderData.countTotalSequence(level, pathway, 7);
            int seq8 = BeyonderData.countTotalSequence(level, pathway, 8);

                    int padding = Math.max(0, nameWidth - displayName.length());
                    String numbers = String.format(Locale.ROOT,
                            " | %4d %4d %4d %4d %4d %4d %4d %4d %4d",
                    seq0, seq1, seq2, seq3, seq4, seq5, seq6, seq7, seq8
                );
                    source.sendSystemMessage(coloredPathLine(pathway, displayName, padding, numbers));
        }

        return 1;
    }

                private static MutableComponent coloredPathLine(String pathway, String name, int padding, String numbers) {
            int color = getPathwayColor(pathway);
                String pad = " ".repeat(Math.max(0, padding));
                return Component.literal(name)
                        .withStyle(style -> style.withColor(TextColor.fromRgb(color)).withFont(MONO_FONT))
                    .append(Component.literal(pad + numbers).withStyle(style -> style.withFont(MONO_FONT)));
            }

                    private static MutableComponent headerLineAvailable(int nameWidth) {
                    String header = "Pathway";
                    int padding = Math.max(0, nameWidth - header.length());
                    String pad = " ".repeat(padding);
                    String columns = String.format(Locale.ROOT,
                            " | %4s %4s %4s %4s %4s %4s %4s %4s %4s",
                            "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8"
                    );
                    return Component.literal(header + pad + columns)
                        .withStyle(style -> style.withFont(MONO_FONT));
                    }

                    private static MutableComponent headerLineClaimed(int nameWidth) {
                    String header = "Pathway";
                    int padding = Math.max(0, nameWidth - header.length());
                    String pad = " ".repeat(padding);
                    String columns = String.format(Locale.ROOT,
                        " | %4s %4s %4s %4s %4s %4s %4s %4s %4s",
                        "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8"
                    );
                    return Component.literal(header + pad + columns)
                        .withStyle(style -> style.withFont(MONO_FONT));
                }

            private static int getPathwayColor(String pathway) {
            PathwayInfos infos = BeyonderData.pathwayInfos.get(pathway);
            if (infos == null) return 0xFFFFFF;
            return infos.color() & 0xFFFFFF;
            }

            private static String getPathwayDisplayName(String pathway) {
                PathwayInfos infos = BeyonderData.pathwayInfos.get(pathway);
                if (infos == null) return pathway;
                String translated = infos.getName();
                return (translated == null || translated.isEmpty()) ? pathway : translated;
            }

            private static int getMaxPathwayNameWidth() {
                int max = "Pathway".length();
                for (String pathway : BeyonderData.pathways) {
                    String name = getPathwayDisplayName(pathway);
                    if (name.length() > max) {
                        max = name.length();
                    }
                }
                return max;
            }
}

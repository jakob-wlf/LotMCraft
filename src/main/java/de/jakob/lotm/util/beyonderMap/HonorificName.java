package de.jakob.lotm.util.beyonderMap;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.LinkedList;
import java.util.List;

public record HonorificName(LinkedList<String> lines) {
    static public String NBT_LINES = "honorific_name_lines";

    static public final HonorificName EMPTY = new HonorificName(new LinkedList<>());

    static public LinkedList<String> getMustHaveWords(String path){
        return new LinkedList<String>(
                switch (path){
                    case "fool" -> List.of("history", "mystery", "bizarreness", "change", "wishes", "miracles", "drama", "performances", "fools");
                    case "error" -> List.of("time", "deceit", "trickery", "loopholes", "tampering", "exploitation", "bugs", "theft", "errors");
                    case "door" -> List.of("space", "stars", "cosmos", "worlds", "concealment", "seals", "travels", "chronicles", "doors");
                    case "visionary" -> List.of("mind", "dreams", "dream", "visions", "fantasies", "fantasy" ,"imagination");
                    case "sun" -> List.of("light", "order", "holiness", "contracts", "justice", "energy", "sun");
                    case "tyrant" -> List.of("storms", "calamities", "seas", "skies", "tsunamis", "undersea", "creatures", "tyranny");
                    case "white_tower" -> List.of("Knowledge", "Observation", "Reasoning", "Cognition", "Wisdom", "Reason", "Omniscience");
                    case "hanged_man" -> List.of("Degeneration", "Corruption", "Mutation", "Shadows", "Darkness", "Whispers", "Sacrifice");
                    case "darkness" -> List.of("Darkness", "Dreams", "Silence", "Horror", "Repose", "Misfortune", "Concealment", "Stars");
                    case "death" -> List.of("Death", "Undeath", "Spirits", "Underworld", "Pallor", "Styx", "Dead", "Eternal", "Rest");
                    case "twilight_light" -> List.of("Twilight", "Decay", "Glory", "Passage","Time", "Dawn", "Dusk", "Power", "Combat");
                    case "demoness" -> List.of("Chaos", "Catastrophes","Mirror", "Disasters", "Apocalypse", "Strife", "Plagues", "Primordium");
                    case "red_priest" -> List.of("War", "Calamity", "Iron","Blood", "Battlefield", "Strife", "Chaos", "Destruction");
                    case "hermit" -> List.of("Knowledge", "Information", "Symbols", "Magic", "Data", "Numbers");
                    case "paragon" -> List.of("Technology", "Logic", "Machinery", "Physics", "Civilizations", "Structure", "Essence");
                    case "wheel_of_fortune" -> List.of("Fate", "Luck", "Lucky", "Misfortune", "Fortune", "River", "Calamities", "Probability", "Madness", "Chaos");
                    case "mother" -> List.of("Earth", "Fertility", "Reproduction", "Desolation", "Nature", "Origin", "Life");
                    case "moon" -> List.of("Moon", "Fertility", "Reproduction", "Spirituality", "Beauty", "Proliferation", "Moonlight");
                    case "abyss" -> List.of("Devils", "Evil", "Desires", "Corruption", "Blood", "Malice", "Blathers", "Corrosion", "Filth");
                    case "chained" -> List.of("Chains", "Temperance", "Deviants", "Restraint", "Indulgence", "Unity", "Binding", "Curses");
                    case "black_emperor" -> List.of("Disorder", "Distortion", "Exploitation", "Entropy", "Abolition", "Frenzy", "Domination", "Black");
                    case "justiciar" -> List.of("Laws", "Rules", "Balance", "Discipline", "Justice", "Judgement", "Order", "White");
                    default -> List.of();
        });
    }

    public static int MAX_LENGTH = 200;

    public boolean contains(String str) {return lines.contains(str);}

    public static boolean validate(String path, LinkedList<String> list){
        var mustHave = getMustHaveWords(path);

        for (var str : list){
            var words = List.of(str.split(" "));
            if(!words.stream().
                    anyMatch(input -> mustHave.stream().
                            anyMatch(target -> target.equalsIgnoreCase(input))))
                return false;
        }

        return true;
    }

    public boolean isEmpty(){
        return lines.isEmpty();
    }

    public HonorificName addLine(String str){
        LinkedList<String> result = new LinkedList<>(lines);
        result.add(str);

        return new HonorificName(result);
    }

    public String getAllInfo(){
        return !lines.isEmpty()?
                ("\n  Line 1: " + lines.get(0)
                + "\n  Line 2: " + lines.get(1)
                + "\n  Line 3: " + lines.get(2)
                + ((lines.size() >= 4) ? ("\n  Line 4: " + lines.get(3)) : "")
                + ((lines.size() == 5) ? ("\n  Line 5: " + lines.get(4)) : ""))
                : "None";
    }

    public CompoundTag toNBT(){
        CompoundTag tag = new CompoundTag();

        ListTag list = new ListTag();
        for (var value : lines) {
            list.add(StringTag.valueOf(value));
        }

        tag.put(NBT_LINES, list);

        return tag;
    }

    static public HonorificName fromNBT(CompoundTag tag){
        LinkedList<String> list = new LinkedList<>();

        if (tag.contains(NBT_LINES, Tag.TAG_LIST)) {
            ListTag listTag = tag.getList(NBT_LINES, Tag.TAG_STRING);

            for (var obj : listTag) {
                list.add(obj.getAsString());
            }
        }

        return new HonorificName(list);
    }

    public static HonorificName fromNetwork(FriendlyByteBuf buf) {
//        return new HonorificName(
//                buf.readUtf(MAX_LENGTH),
//                buf.readUtf(MAX_LENGTH),
//                buf.readUtf(MAX_LENGTH),
//                buf.readUtf(MAX_LENGTH)
//        );

        return HonorificName.EMPTY;
    }

    public void toNetwork(FriendlyByteBuf buf) {
//        buf.writeUtf(first, MAX_LENGTH);
//        buf.writeUtf(second, MAX_LENGTH);
//        buf.writeUtf(third, MAX_LENGTH);
//        buf.writeUtf(trueName, MAX_LENGTH);
    }
}

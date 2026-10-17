package de.jakob.lotm.titles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

/** One title definition, loaded from data/<namespace>/area_titles/<name>.json */
public record AreaTitle(
        Trigger trigger,
        ResourceLocation target,
        Component title,
        Component subtitle,
        Frequency frequency,
        int fadeIn, int stay, int fadeOut) {

    public enum Trigger implements StringRepresentable {
        DIMENSION("dimension"), STRUCTURE("structure");
        public static final Codec<Trigger> CODEC = StringRepresentable.fromEnum(Trigger::values);
        private final String id;
        Trigger(String id) { this.id = id; }
        @Override public String getSerializedName() { return id; }
    }

    public enum Frequency implements StringRepresentable {
        ONCE("once"),
        EVERY_TIME("every_time");
        public static final Codec<Frequency> CODEC = StringRepresentable.fromEnum(Frequency::values);
        private final String id;
        Frequency(String id) { this.id = id; }
        @Override public String getSerializedName() { return id; }
    }

    public static final Codec<AreaTitle> CODEC = RecordCodecBuilder.create(i -> i.group(
            Trigger.CODEC.fieldOf("trigger").forGetter(AreaTitle::trigger),
            ResourceLocation.CODEC.fieldOf("target").forGetter(AreaTitle::target),
            ComponentSerialization.CODEC.fieldOf("title").forGetter(AreaTitle::title),
            ComponentSerialization.CODEC.optionalFieldOf("subtitle", Component.empty()).forGetter(AreaTitle::subtitle),
            Frequency.CODEC.optionalFieldOf("frequency", Frequency.ONCE).forGetter(AreaTitle::frequency),
            Codec.INT.optionalFieldOf("fade_in", 10).forGetter(AreaTitle::fadeIn),
            Codec.INT.optionalFieldOf("stay", 70).forGetter(AreaTitle::stay),
            Codec.INT.optionalFieldOf("fade_out", 20).forGetter(AreaTitle::fadeOut)
    ).apply(i, AreaTitle::new));
}

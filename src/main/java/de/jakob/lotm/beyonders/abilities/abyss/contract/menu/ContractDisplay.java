package de.jakob.lotm.beyonders.abilities.abyss.contract.menu;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class ContractDisplay {

    private final Component title;
    private final List<Component> lines;

    public ContractDisplay(Component title, List<Component> lines) {
        this.title = title;
        this.lines = List.copyOf(lines);
    }
    public Component title() {
        return title;
    }
    public List<Component> lines() {
        return lines;
    }
    public static Builder builder(Component title) {
        return new Builder(title);
    }
    public static final class Builder {
        private final Component title;
        private final List<Component> lines = new ArrayList<>();
        private Builder(Component title) {
            this.title = title;
        }
        public Builder line(Component line) {
            lines.add(line);
            return this;
        }
        public Builder blank() {
            lines.add(Component.empty());
            return this;
        }
        public ContractDisplay build() {
            return new ContractDisplay(title, lines);
        }
    }
}
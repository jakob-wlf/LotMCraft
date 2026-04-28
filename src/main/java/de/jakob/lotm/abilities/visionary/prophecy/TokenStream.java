package de.jakob.lotm.abilities.visionary.prophecy;

import javax.annotation.Nullable;

public class TokenStream {
    private final String[] tokens;
    private int index = 0;

    public TokenStream(String input) {
        this.tokens = input.trim().split("\\s+");
    }

    public @Nullable String peek() {
        return index < tokens.length ? tokens[index] : null;
    }

    public void next() {
        index++;
    }

    public boolean match(String expected) {
        return expected.equalsIgnoreCase(peek());
    }

    public boolean isEmpty(){
        return index >= tokens.length;
    }

    public int getTotalSize() { return tokens.length; }

}

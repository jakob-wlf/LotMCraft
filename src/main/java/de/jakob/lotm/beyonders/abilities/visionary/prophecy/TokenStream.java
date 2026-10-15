package de.jakob.lotm.beyonders.abilities.visionary.prophecy;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;

public class TokenStream {
    private final String[] tokens;
    private int index = 0;

    public TokenStream(String input) {
        this.tokens = input.trim().split("\\s+");
    }

    public TokenStream(ArrayList<String> list){
        this.tokens = list.toArray(new String[0]);
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

    public TokenStream add(int indexV, String value){
        var list = new ArrayList<String>(Arrays.stream(tokens).toList());
        list.add(indexV, value);

        return new TokenStream(list);
    }

    @Override
    public String toString(){
        var resultBuilder = new StringBuilder();
        for(var obj : tokens){
            resultBuilder.append(obj).append(" ");
        }

        return resultBuilder.toString();
    }

}

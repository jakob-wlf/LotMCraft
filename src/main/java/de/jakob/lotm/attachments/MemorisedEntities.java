package de.jakob.lotm.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

public class MemorisedEntities {
    private final List<String> memorisedEntityTypes;

    public static final Codec<MemorisedEntities> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(Codec.STRING).fieldOf("storedEntityTypes").forGetter(m -> m.memorisedEntityTypes)
            ).apply(instance, MemorisedEntities::new)
    );

    public MemorisedEntities() {
        this.memorisedEntityTypes = new ArrayList<>();
    }

    public MemorisedEntities(List<String> types) {
        this.memorisedEntityTypes = new ArrayList<>(types);
    }

    public List<String> getMemorisedEntityTypes() {
        return memorisedEntityTypes;
    }

    public void addMemorisedEntity(String entityType) {
        if (!memorisedEntityTypes.contains(entityType)) {
            memorisedEntityTypes.add(entityType);
        }
    }
}
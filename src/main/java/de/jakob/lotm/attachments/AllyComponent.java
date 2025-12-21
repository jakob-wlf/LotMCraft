package de.jakob.lotm.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Component that stores ally relationships for an entity.
 * Allies cannot target or damage each other.
 */
public record AllyComponent(Set<String> allies) {

    public static final Codec<AllyComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.listOf().xmap(
                            list -> new HashSet<>(list),
                            set -> set.stream().toList()
                    ).fieldOf("allies").forGetter(comp -> new HashSet<>(comp.allies()))
            ).apply(instance, AllyComponent::new)
    );

    public static final StreamCodec<ByteBuf, AllyComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8),
            AllyComponent::allies,
            AllyComponent::new
    );

    public AllyComponent() {
        this(new HashSet<>());
    }

    /**
     * Add an ally by UUID string
     */
    public AllyComponent addAlly(String allyUUID) {
        Set<String> newAllies = new HashSet<>(this.allies);
        newAllies.add(allyUUID);
        return new AllyComponent(newAllies);
    }

    /**
     * Add an ally by UUID
     */
    public AllyComponent addAlly(UUID allyUUID) {
        return addAlly(allyUUID.toString());
    }

    /**
     * Remove an ally by UUID string
     */
    public AllyComponent removeAlly(String allyUUID) {
        Set<String> newAllies = new HashSet<>(this.allies);
        newAllies.remove(allyUUID);
        return new AllyComponent(newAllies);
    }

    /**
     * Remove an ally by UUID
     */
    public AllyComponent removeAlly(UUID allyUUID) {
        return removeAlly(allyUUID.toString());
    }

    /**
     * Check if an entity is an ally
     */
    public boolean isAlly(String uuid) {
        return allies.contains(uuid);
    }

    /**
     * Check if an entity is an ally
     */
    public boolean isAlly(UUID uuid) {
        return isAlly(uuid.toString());
    }

    /**
     * Clear all allies
     */
    public AllyComponent clearAllies() {
        return new AllyComponent(new HashSet<>());
    }

    /**
     * Get the number of allies
     */
    public int allyCount() {
        return allies.size();
    }

    /**
     * Check if has any allies
     */
    public boolean hasAllies() {
        return !allies.isEmpty();
    }
}
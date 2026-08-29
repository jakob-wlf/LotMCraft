package de.jakob.lotm.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


public record AllyComponent(Set<AllyInfo> allies, Set<AllyInfo> requests) {

    public AllyComponent() {
        this(new HashSet<>(), new HashSet<>());
    }

    public AllyComponent addAlly(UUID allyUUID, String playerName, boolean isPlayer) {
        Set<AllyInfo> newAllies = new HashSet<>(this.allies);
        newAllies.add(new AllyInfo(allyUUID, playerName, isPlayer));
        return new AllyComponent(newAllies, this.requests);
    }

    public AllyComponent removeAlly(UUID allyUUID) {
        Set<AllyInfo> newAllies = new HashSet<>(this.allies);
        newAllies.removeIf(info -> info.uuid().equals(allyUUID));
        return new AllyComponent(newAllies, this.requests);
    }

    public boolean isAlly(UUID uuid) {
        return allies.stream().anyMatch(info -> info.uuid().equals(uuid));
    }

    public boolean hasAllies() {
        return !allies.isEmpty();
    }

    public AllyComponent addRequest(UUID allyUUID, String playerName, boolean isPlayer) {
        Set<AllyInfo> newRequests = new HashSet<>(this.requests);
        newRequests.add(new AllyInfo(allyUUID, playerName, isPlayer));
        return new AllyComponent(this.allies, newRequests);
    }

    public  AllyComponent removeRequest(UUID allyUUID) {
        Set<AllyInfo> newRequests = new HashSet<>(this.requests);
        newRequests.removeIf(info -> info.uuid().equals(allyUUID));
        return new AllyComponent(this.allies, newRequests);
    }

    public static final Codec<AllyComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    AllyInfo.CODEC.listOf().<Set<AllyInfo>>xmap(
                            HashSet::new,
                            set -> set.stream().toList()
                    ).fieldOf("allies").forGetter(AllyComponent::allies),
                    AllyInfo.CODEC.listOf().<Set<AllyInfo>>xmap(
                            HashSet::new,
                            set -> set.stream().toList()
                    ).fieldOf("requests").forGetter(AllyComponent::requests)
            ).apply(instance, AllyComponent::new)
    );

    public static final StreamCodec<ByteBuf, AllyComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(HashSet::new, AllyInfo.STREAM_CODEC),
            AllyComponent::allies,
            ByteBufCodecs.collection(HashSet::new, AllyInfo.STREAM_CODEC),
            AllyComponent::requests,
            AllyComponent::new
    );

    public record AllyInfo(UUID uuid, String playerName, boolean isPlayer) {

        public static final Codec<AllyInfo> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.xmap(UUID::fromString, UUID::toString)
                                .fieldOf("uuid").forGetter(AllyInfo::uuid),
                        Codec.STRING.fieldOf("playerName").forGetter(AllyInfo::playerName),
                        Codec.BOOL.fieldOf("isPlayer").forGetter(AllyInfo::isPlayer)
                ).apply(instance, AllyInfo::new)
        );

        public static final StreamCodec<ByteBuf, AllyInfo> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString),
                AllyInfo::uuid,
                ByteBufCodecs.STRING_UTF8,
                AllyInfo::playerName,
                ByteBufCodecs.BOOL,
                AllyInfo::isPlayer,
                AllyInfo::new
        );

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AllyInfo other)) return false;
            return uuid.equals(other.uuid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid);
        }
    }
}
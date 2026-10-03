package de.jakob.lotm.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

/**
 * Component stored on a Red Priest player that tracks team membership.
 * If the player is the leader, memberUUIDs contains the UUIDs of their members and leaderUUID is empty.
 * If the player is a member, leaderUUID contains the UUID of their leader and memberUUIDs is unused.
 */
public record TeamComponent(List<String> memberUUIDs, String leaderUUID) {

    public static final Codec<TeamComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.listOf().xmap(
                            ArrayList::new,
                            list -> list
                    ).fieldOf("memberUUIDs").forGetter(comp -> new ArrayList<>(comp.memberUUIDs())),
                    Codec.STRING.fieldOf("leaderUUID").forGetter(TeamComponent::leaderUUID)
            ).apply(instance, TeamComponent::new)
    );

    public static final StreamCodec<ByteBuf, TeamComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
            TeamComponent::memberUUIDs,
            ByteBufCodecs.STRING_UTF8,
            TeamComponent::leaderUUID,
            TeamComponent::new
    );

    public TeamComponent() {
        this(new ArrayList<>(), "");
    }

    /**
     * Add a member by UUID string. Returns a new instance.
     */
    public TeamComponent addMember(String uuid) {
        List<String> newMembers = new ArrayList<>(this.memberUUIDs);
        if (!newMembers.contains(uuid)) {
            newMembers.add(uuid);
        }
        return new TeamComponent(newMembers, this.leaderUUID);
    }

    /**
     * Remove a member by UUID string. Returns a new instance.
     */
    public TeamComponent removeMember(String uuid) {
        List<String> newMembers = new ArrayList<>(this.memberUUIDs);
        newMembers.remove(uuid);
        return new TeamComponent(newMembers, this.leaderUUID);
    }

    /**
     * Check if a UUID string is a member of this team.
     */
    public boolean hasMember(String uuid) {
        return memberUUIDs.contains(uuid);
    }

    /**
     * Get the number of members (not counting the leader).
     */
    public int memberCount() {
        return memberUUIDs.size();
    }

    /**
     * Returns a new instance with the given leaderUUID set.
     */
    public TeamComponent withLeader(String uuid) {
        return new TeamComponent(new ArrayList<>(this.memberUUIDs), uuid);
    }

    /**
     * Returns a new instance with leaderUUID cleared (empty string).
     */
    public TeamComponent clearLeader() {
        return new TeamComponent(new ArrayList<>(this.memberUUIDs), "");
    }

    /**
     * Returns true if this player is currently a member of a team (i.e. has a leader).
     */
    public boolean isInTeam() {
        return !leaderUUID.isEmpty();
    }
}

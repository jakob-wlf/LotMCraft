package de.jakob.lotm.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Component stored on a Red Priest player that tracks team membership.
 * If the player is the leader, memberUUIDs contains the UUIDs of their members and leaderUUID is empty.
 * If the player is a member, leaderUUID contains the UUID of their leader and memberUUIDs is unused.
 */
public record TeamComponent(List<String> memberUUIDs, String leaderUUID, boolean damageFocus, boolean damageAlleviation) {

    public static final Codec<TeamComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.listOf().xmap(
                            ArrayList::new,
                            list -> list
                    ).fieldOf("memberUUIDs").forGetter(comp -> new ArrayList<>(comp.memberUUIDs())),
                    Codec.STRING.fieldOf("leaderUUID").forGetter(TeamComponent::leaderUUID),
                    Codec.BOOL.fieldOf("damageFocus").forGetter(TeamComponent::damageFocus),
                    Codec.BOOL.fieldOf("damageAlleviation").forGetter(TeamComponent::damageAlleviation)
            ).apply(instance, TeamComponent::new)

    );

    public static final StreamCodec<ByteBuf, TeamComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
            TeamComponent::memberUUIDs,
            ByteBufCodecs.STRING_UTF8,
            TeamComponent::leaderUUID,
            ByteBufCodecs.BOOL,
            TeamComponent::damageFocus,
            ByteBufCodecs.BOOL,
            TeamComponent::damageAlleviation,
            TeamComponent::new
    );

    public TeamComponent() {
        this(new ArrayList<>(), "", false, false);
    }

    /**
     * Add a member by UUID string. Returns a new instance.
     */
    public TeamComponent addMember(String uuid) {
        List<String> newMembers = new ArrayList<>(this.memberUUIDs);
        if (!newMembers.contains(uuid)) {
            newMembers.add(uuid);
        }
        return new TeamComponent(newMembers, this.leaderUUID, this.damageFocus, this.damageAlleviation);
    }

    /**
     * Remove a member by UUID string. Returns a new instance.
     */
    public TeamComponent removeMember(String uuid) {
        List<String> newMembers = new ArrayList<>(this.memberUUIDs);
        newMembers.remove(uuid);
        return new TeamComponent(newMembers, this.leaderUUID, this.damageFocus, this.damageAlleviation);
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
        return new TeamComponent(new ArrayList<>(this.memberUUIDs), uuid,  this.damageFocus, this.damageAlleviation);
    }

    public boolean isDamageFocus() {
        return this.damageFocus;
    }

    public boolean isDamageAlleviation() {
        return this.damageAlleviation;
    }

    public TeamComponent setDamageFocus(boolean bool) {
        return new TeamComponent(this.memberUUIDs, this.leaderUUID, bool, this.damageAlleviation);
    }

    public TeamComponent setDamageAlleviation(boolean bool) {
        return new TeamComponent(this.memberUUIDs, this.leaderUUID, this.damageFocus, bool);
    }
    /**
     * Returns a new instance with leaderUUID cleared (empty string).
     */
    public TeamComponent clearLeader() {
        return new TeamComponent(new ArrayList<>(this.memberUUIDs), "",  this.damageFocus, this.damageAlleviation);
    }

    public TeamComponent clearAll() {
        return new TeamComponent();
    }
    /**
     * Returns true if this player is currently a member of a team (i.e. has a leader).
     */
    public boolean isInTeam() {
        return !leaderUUID.isEmpty();
    }
    public boolean isLeader(ServerPlayer player) {
        return this.leaderUUID.equals(player.getStringUUID());
    }

}

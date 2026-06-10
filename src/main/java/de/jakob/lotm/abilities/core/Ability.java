package de.jakob.lotm.abilities.core;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.black_emperor.EntropySubAbility;
import de.jakob.lotm.abilities.error.ParasitationAbility;
import de.jakob.lotm.attachments.*;
import de.jakob.lotm.acting.ActingTaskRegistry;
import de.jakob.lotm.attachments.AbilityCooldownComponent;
import de.jakob.lotm.attachments.ControllingDataComponent;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.UseAbilityPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;
import de.jakob.lotm.abilities.black_emperor.MausoleumDomainAbility;
import de.jakob.lotm.util.helper.AbilityUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public abstract class Ability {

    // Identity
    private final String id;
    protected final int cooldown;

    // Interaction behaviour
    protected double interactionRadius = 1.4;
    protected int interactionCacheTicks = 10;
    protected final String[] interactionFlags;
    protected boolean postsUsedAbilityEventManually = false;

    // Optimal distance
    public boolean hasOptimalDistance = true;
    public float optimalDistance = 5f;

    // Permissions
    public boolean canBeUsedByNPC = true;
    public boolean canBeCopied = true;
    public boolean cannotBeStolen = false;
    public boolean canBeUsedInArtifact = true;
    public boolean canBeReplicated = true;
    public boolean canBeShared = true;

    public boolean canAlwaysBeUsed = false;

    // Misc
    public boolean doesNotIncreaseDigestion = false;
    protected boolean shouldBeHidden = false;

    // Utility
    protected final Random random = new Random();

    //Scaling
    public HashMap<UUID, Integer> artifactScalingMap;
    protected boolean autoClear = true;

    public Ability(String id, float cooldown, String... interactionFlags) {
        this.id = id;
        this.cooldown = Math.round(cooldown * 20);
        this.interactionFlags = interactionFlags;
        this.artifactScalingMap = new HashMap<>(60);
    }

    protected int getColorForPathway(String pathway) {
        return BeyonderData.pathwayInfos.containsKey(pathway) ? BeyonderData.pathwayInfos.get(pathway).color() : 0xFFFFFF;
    }

    public void useAbility(ServerLevel serverLevel, LivingEntity entity, boolean consumeSpirituality, boolean hasToHaveAbility, boolean hasToMeetRequirements) {
        if(LOTMCraft.abilityHandler.isDisabled(this)) {
            return;
        }

        if(!canUse(entity, hasToHaveAbility, consumeSpirituality) && hasToMeetRequirements) {
            return;
        }

        // Fire event
        AbilityUseEvent event = new AbilityUseEvent(entity, this);
        NeoForge.EVENT_BUS.post(event);

        if(event.isCanceled()) {
            return;
        }

        LivingEntity newUser = event.getEntity();
        if(!canUse(newUser, false, consumeSpirituality)) {
            return;
        }

        // Consume spirituality
        if(shouldConsumeSpirituality(newUser) && consumeSpirituality) {
            BeyonderData.reduceSpirituality(newUser, getInflatedSpiritualityCost(newUser, serverLevel));
        }

        // Digest potion
        if(!doesNotIncreaseDigestion && newUser instanceof Player player) {
            if(ActingTaskRegistry.getTasksFor(BeyonderData.getPathway(player), BeyonderData.getSequence(player)).isEmpty())
                BeyonderData.digest(player, getDigestionProgressForUse(newUser), true);
        }

        // Handle Cooldown
        AbilityCooldownComponent component = newUser.getData(ModAttachments.COOLDOWN_COMPONENT);
        int inflatedCooldown = cooldown;
        var pdata = newUser.getPersistentData();
        if (pdata.contains(EntropySubAbility.SENSORY_DECAY_COOLDOWN_MULT_KEY)) {
            if (pdata.getLong(EntropySubAbility.SENSORY_DECAY_COOLDOWN_UNTIL_KEY) > serverLevel.getGameTime()) {
                inflatedCooldown = (int)(cooldown * pdata.getFloat(EntropySubAbility.SENSORY_DECAY_COOLDOWN_MULT_KEY));
            } else {
                pdata.remove(EntropySubAbility.SENSORY_DECAY_COOLDOWN_MULT_KEY);
                pdata.remove(EntropySubAbility.SENSORY_DECAY_COOLDOWN_UNTIL_KEY);
            }
        }
        component.setCooldown(id, inflatedCooldown);

        if(AbilityUtil.hasArtifactScaling(entity)){
            artifactScalingMap.put(entity.getUUID(), AbilityUtil.getArtifactScalingSeq(entity));
            AbilityUtil.removeArtifactScaling(entity);
        }

        // Use ability client and server sided
        onAbilityUse(serverLevel, newUser);
        if(entity instanceof ServerPlayer player) PacketHandler.sendToPlayer(player, new UseAbilityPacket(getId(), newUser.getId()));

        if(this.autoClear){
            clearArtifactScaling(entity);
        }

        if(AbilityUtil.ignoreAllies.containsKey(entity.getUUID()) && !AbilityUtil.ignoreAllies.get(entity.getUUID())){
            AbilityUtil.ignoreAllies.remove(entity.getUUID());
        }

        // Track ability use for Recording/Replicating detection
        AbilityUseTracker.trackUse(newUser, this, newUser.position(), serverLevel);

        if(!postsUsedAbilityEventManually && !(this instanceof ToggleAbility)) {
            NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, newUser.position(), newUser, this, interactionFlags, interactionRadius, interactionCacheTicks));
        }
    }

    public void useAbility(ServerLevel serverLevel, LivingEntity entity) {
        useAbility(serverLevel, entity, true, true, true);
    }

    public void clearArtifactScaling(LivingEntity entity){
        artifactScalingMap.remove(entity.getUUID());
    }

    public abstract void onAbilityUse(Level level, LivingEntity entity);

    public abstract Map<String, Integer> getRequirements();

    protected abstract float getSpiritualityCost();

    public float getInflatedSpiritualityCost(LivingEntity entity, ServerLevel level) {
        float base = getSpiritualityCost();
        var pdata = entity.getPersistentData();
        if (pdata.contains(EntropySubAbility.ENTROPY_DRAIN_SPIRIT_MULT_KEY)) {
            if (pdata.getLong(EntropySubAbility.ENTROPY_DRAIN_SPIRIT_UNTIL_KEY) > level.getGameTime()) {
                return base * pdata.getFloat(EntropySubAbility.ENTROPY_DRAIN_SPIRIT_MULT_KEY);
            } else {
                pdata.remove(EntropySubAbility.ENTROPY_DRAIN_SPIRIT_MULT_KEY);
                pdata.remove(EntropySubAbility.ENTROPY_DRAIN_SPIRIT_UNTIL_KEY);
            }
        }
        return base;
    }

    public float multiplier(LivingEntity entity) {
        return (float) AbilityUtil.getMultiplierWithArt(entity, this);
    }

    public void onHold(Level level, LivingEntity entity) {

    }

    public boolean shouldUseAbility(LivingEntity entity) {
        return true;
    }

    public boolean hasAbility(LivingEntity entity) {
        if(!BeyonderData.isBeyonder(entity)) return false;

        String pathway = BeyonderData.getPathway(entity);
        int sequence = BeyonderData.getSequence(entity);

        // Creative + OP players can use any ability up to their sequence
        if(entity instanceof Player player && player.isCreative() && player.hasPermissions(2)) {
            return getRequirements().values().stream().anyMatch(reqSeq -> reqSeq >= sequence);
        }

        // use the old system in case of controlling - will change once worms get added
        ControllingDataComponent controllingDataComponent = entity.getData(ModAttachments.CONTROLLING_DATA);
        if (controllingDataComponent.isControlling()) {
            if(getRequirements().containsKey(pathway) && getRequirements().get(pathway) >= sequence) {
                return true;
            }
        }

        DiscernmentComponent discernmentComponent = entity.getData(ModAttachments.DISCERNMENT_DATA.get());
        if(discernmentComponent.isDiscerning()){
            if(getRequirements().containsKey(pathway) && getRequirements().get(pathway) >= sequence)
                return true;
        }

        // Check pathway
        for(int i = sequence; i < BeyonderData.getPathwayHistory(entity).length; i++) {
            if(BeyonderData.getPathwayHistory(entity)[i] == null) continue;
            String userPath = BeyonderData.getPathwayHistory(entity)[i];
            if(getRequirements().containsKey(userPath) && getRequirements().get(userPath) == i) {
                return true;
            }
        }

        return false;
    }

    public boolean canUse(LivingEntity entity) {
        return canUse(entity, true, true);
    }

    public boolean canUse(LivingEntity entity, boolean hasToHaveAbility, boolean doesConsumeSpirituality) {
        if(!hasAbility(entity) && hasToHaveAbility) return false;

        if (MausoleumDomainAbility.isInsideMausoleumDomain(entity.getUUID())) {
            if (entity instanceof ServerPlayer player) {
                AbilityUtil.sendActionBar(player,
                        Component.literal("Your abilities are sealed.").withColor(0xFF5555));
            }
            return false;
        }

        AbilityCooldownComponent component = entity.getData(ModAttachments.COOLDOWN_COMPONENT);
        if(component.isOnCooldown(id)) return false;

        if(shouldConsumeSpirituality(entity) && doesConsumeSpirituality && BeyonderData.getSpirituality(entity) <= getSpiritualityCost()) return false;

        if(!(entity instanceof Player) && !canBeUsedByNPC) return false;

        if(entity instanceof Player player && player.isSpectator() && !ParasitationAbility.isConcealed(player.getUUID())) return false;

        DisabledAbilitiesComponent disabledComponent = entity.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
        if((disabledComponent.isAbilityUsageDisabled() || disabledComponent.isSpecificAbilityDisabled(this.getId())) && !this.canAlwaysBeUsed) return false;

        if(LOTMCraft.abilityHandler.isDisabled(this)) return false;

        return true;
    }

    protected boolean shouldConsumeSpirituality(LivingEntity entity) {
        return !entity.hasInfiniteMaterials() && entity instanceof Player;
    }

    public int lowestSequenceUsable() {
        return getRequirements().values().stream()
                .max(Integer::compareTo)
                .orElse(-1);
    }

    private float getDigestionProgressForUse(LivingEntity entity) {
        int sequence = BeyonderData.getSequence(entity);

        if (!getRequirements().containsKey(BeyonderData.getPathway(entity))) {
            return 0f;
        }

        int requiredSequence = getRequirements().get(BeyonderData.getPathway(entity));

        if (sequence > requiredSequence) {
            return 0f;
        }

        float cooldownMultiplier = Math.clamp(((float) cooldown) / (20 * 7), .2f, 2.25f);

        return (1f / (80f * Math.max(.5f, ((10 - requiredSequence) * .5f)))) * cooldownMultiplier;
    }

    public ResourceLocation getTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/abilities/" + id + ".png");
    }

    public MutableComponent getName() {
        return Component.translatable("lotmcraft." + getId());
    }

    public Component getNameFormatted() {
        if(getRequirements().isEmpty()) {
            return Component.translatable("lotmcraft." + getId()).withStyle(ChatFormatting.BOLD);
        }

        String pathway = getRequirements().keySet()
                .stream()
                .sorted()
                .findFirst()
                .orElse(null);

        int color = BeyonderData.pathwayInfos.get(pathway).color();
        return getName().withStyle(ChatFormatting.BOLD).withColor(color);
    }

    public Component getNameFormatted(LivingEntity entity) {
        if(getRequirements().isEmpty()) {
            return Component.translatable("lotmcraft." + getId()).withStyle(ChatFormatting.BOLD);
        }

        String pathway = BeyonderData.getPathway(entity);

        int color = BeyonderData.pathwayInfos.containsKey(pathway) ? BeyonderData.pathwayInfos.get(pathway).color() : 0xFFFFFF;
        return getName().withStyle(ChatFormatting.BOLD).withColor(color);
    }


    @Nullable
    public Component getDescription() {
        MutableComponent description = Component.translatable("lotmcraft." + getId() + ".description");
        if(description.getString().equals("lotmcraft." + getId() + ".description")) {
            return null;
        }
        return description.withStyle(ChatFormatting.DARK_GRAY);
    }

    public String getId() {
        return id;
    }

    public String[] getInteractionFlags() {
        return interactionFlags;
    }

    public double getInteractionRadius() {
        return interactionRadius;
    }

    public int getInteractionCacheTicks() {
        return interactionCacheTicks;
    }

    public boolean getShouldBeHidden(){
        return shouldBeHidden;
    }

    public int getCooldown() {
        return cooldown;
    }

    public float spiritualityCost() {
        return getSpiritualityCost();
    }
}

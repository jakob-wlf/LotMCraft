package de.jakob.lotm.beyonders.abilities.core;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.error.ParasitationAbility;
import de.jakob.lotm.attachments.*;
import de.jakob.lotm.beyonders.acting.ActingTaskRegistry;
import de.jakob.lotm.attachments.AbilityCooldownComponent;
import de.jakob.lotm.attachments.ControllingDataComponent;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.UseAbilityPacket;
import de.jakob.lotm.util.AuthorityResistanceManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.ClientData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.CopiedAbilityHelper;
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

import java.util.*;

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

    //dynamic spirituality
    protected boolean hasDynamicSpirituality = false;
    protected List<Float> dynamicSpirituality = new LinkedList<>(); // must be for every seq, if enabled

    //dynamic cooldown
    protected boolean hasDynamicCooldown = false;
    protected List<Integer> dynamicCooldown = new LinkedList<>(); // must be for every seq, if enabled

    public float baseDamage = 0f;

    protected boolean hasManualDistance = false;
    public int baseDistance = 0;

    public Ability(String id, float cooldown, String... interactionFlags) {
        this.id = id;
        this.cooldown = Math.round(cooldown * 20);
        this.interactionFlags = interactionFlags;
        this.artifactScalingMap = new HashMap<>(60);
    }

    protected int getColorForPathway(String pathway) {
        return BeyonderData.pathwayInfos.containsKey(pathway) ? BeyonderData.pathwayInfos.get(pathway).color() : 0xFFFFFF;
    }

    public void useAbility(ServerLevel serverLevel, LivingEntity entity, boolean consumeSpirituality, boolean hasToHaveAbility, boolean hasToMeetRequirements, boolean isCopied) {
        if(LOTMCraft.abilityHandler.isDisabled(this)) {
            return;
        }

        if(!canUse(entity, hasToHaveAbility, consumeSpirituality, isCopied) && hasToMeetRequirements) {
            return;
        }

        // Fire event
        AbilityUseEvent event = new AbilityUseEvent(entity, this);
        NeoForge.EVENT_BUS.post(event);

        if(event.isCanceled()) {
            return;
        }

        LivingEntity newUser = event.getEntity();
        if(!canUse(newUser, false, consumeSpirituality, isCopied)) {
            return;
        }

        if(AbilityUtil.hasArtifactScaling(entity)){
            artifactScalingMap.put(entity.getUUID(), AbilityUtil.getArtifactScalingSeq(entity));
            AbilityUtil.removeArtifactScaling(entity);
        }

        //Sequence for dynamic cooldown and spirituality
        int seq = AbilityUtil.getSeqWithArt(newUser, this);

        // Consume spirituality
        if(shouldConsumeSpirituality(newUser) && consumeSpirituality) {
            float cost = getInflatedSpiritualityCost(newUser, serverLevel, seq);
            float current = BeyonderData.getSpirituality(newUser);

            // A spirituality shortfall (up to 30%) is paid in sanity: deeper deficits and pricier abilities cost more,
            // capped at 0.1. Applied directly (bypassing the per-sequence resistance) so it bites at any sequence.
            if(cost > 0 && current < cost && newUser instanceof Player) {
                float deficitScale = Math.min(0.3f, (cost - current) / cost) / 0.3f;
                float costScale = (float) Math.log10(Math.max(10f, cost));
                float sanityCost = Math.min(0.1f, deficitScale * costScale * 0.025f);
                SanityComponent sanityComp = newUser.getData(ModAttachments.SANITY_COMPONENT);
                sanityComp.setSanityAndSync(sanityComp.getSanity() - sanityCost, newUser);
            }

            BeyonderData.reduceSpirituality(newUser, cost);
        }

        // Digest potion
        if(!doesNotIncreaseDigestion && newUser instanceof Player player) {
            if(ActingTaskRegistry.getTasksFor(BeyonderData.getPathway(player), BeyonderData.getSequence(player)).isEmpty())
                BeyonderData.digest(player, getDigestionProgressForUse(newUser), true);
        }

        // Decrement ability if it was copied
        if(isCopied) {
            CopiedAbilityHelper.decrementUses(entity, getId());
        }

        // Handle Cooldown
        AbilityCooldownComponent component = newUser.getData(ModAttachments.COOLDOWN_COMPONENT);
        int trueCooldown = getCooldown(seq);

        int inflatedCooldown = trueCooldown;
        component.setCooldown(id, inflatedCooldown);

        // Use ability client and server sided
        final float damageBackup = baseDamage;
        baseDamage *= multiplier(newUser);

        if(!hasManualDistance)
            baseDistance = (int) (Math.max(Math.max((1 << (9 - seq)), 15), 125) * multiplier(newUser));
        AuthorityResistanceManager.addToBuffer(entity, seq);

        onAbilityUse(serverLevel, newUser);

        baseDamage = damageBackup;

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
        useAbility(serverLevel, entity, true, true, true, false);
    }

    public void clearArtifactScaling(LivingEntity entity){
        artifactScalingMap.remove(entity.getUUID());
    }

    public abstract void onAbilityUse(Level level, LivingEntity entity);

    public abstract Map<String, Integer> getRequirements();

    protected abstract float getSpiritualityCost();

    public float getInflatedSpiritualityCost(LivingEntity entity, ServerLevel level, int seq) {
        float base = spiritualityCost(seq);
        return base;
    }

    public float multiplier(LivingEntity entity) {
        return entity != null ? (float) AbilityUtil.getMultiplierWithArt(entity, this) : 1f;
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
        return canUse(entity, true, true, false);
    }

    public boolean canUse(LivingEntity entity, boolean hasToHaveAbility, boolean doesConsumeSpirituality, boolean isCopied) {
        if(!hasAbility(entity) && hasToHaveAbility && !isCopied) return false;

        AbilityCooldownComponent component = entity.getData(ModAttachments.COOLDOWN_COMPONENT);
        if(component.isOnCooldown(id)) return false;

        // Allow use down to a 30% spirituality deficit; the shortfall is paid in sanity on use
        if(shouldConsumeSpirituality(entity) && doesConsumeSpirituality && BeyonderData.getSpirituality(entity) < getSpiritualityCost() * 0.7f) return false;

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

    public int getCooldown(int seq) {
        if(seq + 1 > dynamicCooldown.size()) return cooldown;

        return hasDynamicCooldown ? 20 * dynamicCooldown.get(seq) : cooldown;
    }

    public float spiritualityCost(int seq) {
        if(seq + 1 > dynamicSpirituality.size()) return getSpiritualityCost();

        return hasDynamicSpirituality ? dynamicSpirituality.get(seq) : getSpiritualityCost();
    }
}

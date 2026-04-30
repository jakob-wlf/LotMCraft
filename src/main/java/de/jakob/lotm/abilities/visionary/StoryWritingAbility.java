package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.abilities.visionary.prophecy.Prophecy;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.TriggerHelper;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.*;

//if <trigger> then <action>

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class StoryWritingAbility extends ToggleAbility {
    public static final HashMap<UUID, Integer> writingMap = new HashMap<>();

    public StoryWritingAbility(String id) {
        super(id);
        canBeUsedByNPC = false;
        canBeCopied = false;
        canBeReplicated = false;
        cannotBeStolen = true;
        autoClear = false;
        canBeShared = false;
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        writingMap.put(entity.getUUID(), AbilityUtil.getSeqWithArt(entity, this));
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            if(entity.isShiftKeyDown())
                ClientHandler.openStoryWritingExplanation();
            return;
        }

        if(entity.isShiftKeyDown()) {
            cancel((ServerLevel) level, entity);
            return;
        }

        if(PsychologicalCueAbility.map.containsKey(entity.getUUID())){
            cancel((ServerLevel) level, entity);
        }

        writingMap.put(entity.getUUID(), AbilityUtil.getSeqWithArt(entity, this));
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        writingMap.remove(entity.getUUID());
        clearArtifactScaling(entity);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 50;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onChatMessageSent(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();

        if (!writingMap.containsKey(player.getUUID())) return;

        event.setCanceled(true);

        String rawMessage = event.getRawText();

        var trigger = TriggerHelper.deduceWithContext(rawMessage, writingMap.get(player.getUUID()), player);
        if(trigger == null){
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.story_writing.failed"));
            return;
        }

        BeyonderData.playerMap.addProphecy(trigger.getTarget(), new Prophecy(trigger.getTarget(), trigger, trigger.getType(), player.getUUID()));
    }

    @SubscribeEvent
    public static void onEntityTickPre(EntityTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;

        if (player.level().isClientSide()) return;

        var op = BeyonderData.playerMap.get(player);
        if(op.isPresent()){
            var prophecies = op.get().prophecies();

            LinkedList<Prophecy> buff1 = new LinkedList<>();
            LinkedList<Prophecy> buff2 = new LinkedList<>(prophecies);

            for(var obj : buff2){
                if (obj.checkAndPerform(player.level(), player)){
                    buff1.add(obj);
                }
            }

            prophecies.removeAll(buff1);

            BeyonderData.playerMap.setProphecies(player.getUUID(), prophecies);
        }
    }

//    private static final Map<UUID, UUID> AUTHOR_TARGET = new HashMap<>();
//    private static final Map<UUID, GuidanceData> ACTIVE_GUIDANCE = new HashMap<>();
//    private static final Map<UUID, UUID> TARGET_TO_AUTHOR = new HashMap<>();
//
//    private static final float BASE_SANITY_DRAIN = -0.01f;
//    private static final int RAMP_INTERVAL_TICKS = 20 * 30;
//    private static final int GUIDANCE_RADIUS = 2; //2 blocks = 5x5x5 region
//
//    private static class GuidanceData {
//        final UUID authorUUID;
//        final Vec3 targetPos;
//        float currentDrain;
//        int ticksSinceLastRamp;
//        int rampCount;
//
//        GuidanceData(UUID authorUUID, Vec3 targetPos) {
//            this.authorUUID = authorUUID;
//            this.targetPos = targetPos;
//            this.currentDrain = BASE_SANITY_DRAIN;
//            this.ticksSinceLastRamp = 0;
//            this.rampCount = 0;
//        }
//    }
//
//    public StoryWritingAbility(String id) {
//        super(id, 60f);
//        canBeUsedByNPC = false;
//    }
//
//    @Override
//    public Map<String, Integer> getRequirements() {
//        return new HashMap<>(Map.of("visionary", 1));
//    }
//
//    @Override
//    public float getSpiritualityCost() {
//        return 1500;
//    }
//
//    @Override
//    protected String[] getAbilityNames() {
//        return new String[]{
//                "ability.lotmcraft.story_writing.player",
//                "ability.lotmcraft.story_writing.target",
//                "ability.lotmcraft.story_writing.assault",
//                "ability.lotmcraft.story_writing.guidance",
//        };
//    }
//
//    @Override
//    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
//        if (level.isClientSide) return;
//        if (!(level instanceof ServerLevel serverLevel)) return;
//        if (!(entity instanceof ServerPlayer author)) return;
//
//        switch (abilityIndex) {
//            case 0 -> castPlayer(serverLevel, author);
//            case 1 -> castTarget(serverLevel, author);
//            case 2 -> castAssault(serverLevel, author);
//            case 3 -> castGuidance(serverLevel, author);
//        }
//    }
//
//    // Opens shapeshifting screen to select a player
//    private void castPlayer(ServerLevel level, ServerPlayer author) {
//        PacketDistributor.sendToPlayer(author,
//                new OpenShapeShiftingScreenPacket(Collections.emptyList()));
//        // Wire screen confirm callback to onTargetSelected(author, target)
//    }
//
//    // Looks at the nearest mob/player and creates a book for them directly
//    private void castTarget(ServerLevel level, ServerPlayer author) {
//        LivingEntity target = AbilityUtil.getTargetEntity(author, 20, 2);
//        if (target == null) {
//            AbilityUtil.sendActionBar(author,
//                    Component.translatable("ability.lotmcraft.frenzy.no_target").withColor(0xFFff124d));
//            return;
//        }
//
//        assignTarget(author, target, level);
//    }
//
//
//    public static void onTargetSelected(ServerPlayer author, ServerPlayer target) {
//        if (DivinationUtil.getConcealmentPower(target) >= 13) {
//            AbilityUtil.sendActionBar(author,
//                    Component.literal("Target cannot be guided.").withColor(0xFFff124d));
//            return;
//        }
//        assignTarget(author, target, author.serverLevel());
//    }
//
//    private static void assignTarget(ServerPlayer author, LivingEntity target, ServerLevel level) {
//        // Remove existing book
//        removeBookFromAuthor(author);
//
//        // Clear old guidance if switching targets
//        UUID oldTarget = AUTHOR_TARGET.get(author.getUUID());
//        if (oldTarget != null) {
//            stopGuidance(oldTarget);
//            TARGET_TO_AUTHOR.remove(oldTarget);
//        }
//
//        AUTHOR_TARGET.put(author.getUUID(), target.getUUID());
//        TARGET_TO_AUTHOR.put(target.getUUID(), author.getUUID());
//
//        // Create book — use player name if player, otherwise entity name
//        String targetName = target.getName().getString();
//
//        // Build book item manually since target may not be a ServerPlayer
//        ItemStack book;
//        if (target instanceof ServerPlayer targetPlayer) {
//            book = StoryBookItem.create(author, targetPlayer);
//        } else {
//            // Create book with mob as target — reuse create() logic manually
//            net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
//            tag.putUUID(StoryBookItem.KEY_AUTHOR, author.getUUID());
//            tag.putUUID(StoryBookItem.KEY_TARGET, target.getUUID());
//            tag.putString(StoryBookItem.KEY_TARGET_NAME, targetName);
//            tag.putInt(StoryBookItem.KEY_USES, 3);
//            book = new ItemStack(ModItems.STORY_BOOK.get());
//            book.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
//                    net.minecraft.world.item.component.CustomData.of(tag));
//            book.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
//                    Component.literal("Story of " + targetName)
//                            .withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
//        }
//
//        author.addItem(book);
//
//        AbilityUtil.sendActionBar(author,
//                Component.literal("Story of " + targetName + " created.").withColor(0xFFe3ffff));
//    }
//
//
//    private int getAuthorDisasterIndex(ServerPlayer author) {
//        DisasterFantasiaAbility fantasia = (DisasterFantasiaAbility)
//                LOTMCraft.abilityHandler.getById("disaster_fantasia_ability");
//        if (fantasia == null) return 0;
//        return fantasia.getSelectedAbilityIndex(author.getUUID());
//    }
//
//    private void castAssault(ServerLevel level, ServerPlayer author) {
//        UUID targetUUID = AUTHOR_TARGET.get(author.getUUID());
//        if (targetUUID == null) {
//            AbilityUtil.sendActionBar(author,
//                    Component.translatable("ability.lotmcraft.story_writing.no_target").withColor(0xFFff124d));
//            return;
//        }
//
//        LivingEntity target = level.getEntity(targetUUID) instanceof LivingEntity le ? le : null;
//        if (target == null || !target.isAlive()) {
//            AbilityUtil.sendActionBar(author,
//                    Component.translatable("ability.lotmcraft.story_writing.target_unavailable").withColor(0xFFff124d));
//            return;
//        }
//
//        ManipulationAbility manipulation = (ManipulationAbility)
//                LOTMCraft.abilityHandler.getById("manipulation_ability");
//        if (manipulation != null) {
//            manipulation.triggerGroupIncite(level, author, target);
//        }
//        consumeBookUse(author);
//    }
//
//
//    private void castGuidance(ServerLevel level, ServerPlayer author) {
//        UUID targetUUID = AUTHOR_TARGET.get(author.getUUID());
//        if (targetUUID == null) {
//            AbilityUtil.sendActionBar(author,
//                    Component.translatable("ability.lotmcraft.story_writing.no_target").withColor(0xFFff124d));
//            return;
//        }
//
//        // Guidance only works on players since we need to send them messages
//        ServerPlayer target = level.getServer().getPlayerList().getPlayer(targetUUID);
//        if (target == null || !target.isAlive()) {
//            AbilityUtil.sendActionBar(author,
//                    Component.translatable("ability.lotmcraft.story_writing.target_unavailable").withColor(0xFFff124d));
//            return;
//        }
//
//        Vec3 guidancePos = AbilityUtil.getTargetLocation(author, 85, 3);
//
//        stopGuidance(targetUUID);
//
//        GuidanceData data = new GuidanceData(author.getUUID(), guidancePos);
//        ACTIVE_GUIDANCE.put(targetUUID, data);
//
//        sendGuidanceMessage(target, guidancePos);
//        consumeBookUse(author);
//    }
//
//    private static void sendGuidanceMessage(ServerPlayer target, Vec3 pos) {
//        AbilityUtil.sendActionBar(target,
//                Component.literal(String.format("You feel like moving to X:%.0f Y:%.0f Z:%.0f",
//                        pos.x, pos.y, pos.z)).withColor(0xFFffad33));
//    }
//
//    private static void stopGuidance(UUID targetUUID) {
//        ACTIVE_GUIDANCE.remove(targetUUID);
//    }
//
//    @SubscribeEvent
//    public static void onEntityTick(EntityTickEvent.Post event) {
//        if (!(event.getEntity() instanceof ServerPlayer target)) return;
//        if (target.level().isClientSide) return;
//        if (!(target.level() instanceof ServerLevel serverLevel)) return;
//
//        UUID targetUUID = target.getUUID();
//        GuidanceData data = ACTIVE_GUIDANCE.get(targetUUID);
//        if (data == null) return;
//
//        // Target died — clean up everything
//        if (!target.isAlive()) {
//            stopGuidanceAndCleanup(targetUUID, serverLevel);
//            return;
//        }
//
//        // Check if target reached the guidance position (5x5x5 = ±2 blocks)
//        Vec3 pos = target.position();
//        if (Math.abs(pos.x - data.targetPos.x) <= GUIDANCE_RADIUS
//                && Math.abs(pos.y - data.targetPos.y) <= GUIDANCE_RADIUS
//                && Math.abs(pos.z - data.targetPos.z) <= GUIDANCE_RADIUS) {
//            stopGuidance(targetUUID);
//            AbilityUtil.sendActionBar(target,
//                    Component.literal("You have arrived.").withColor(0xFFe3ffff));
//            return;
//        }
//
//        // Sanity drain once per second after the first ramp (ie after 30s)
//        //the sanity drain needs to be tested cuz i dont know how to create a server in my ide so idk how strong this sanity drain is. I want it to be heavy for those below s4
//        if (target.tickCount % 20 == 0 && data.rampCount >= 1) {
//            SanityComponent sanity = target.getData(ModAttachments.SANITY_COMPONENT);
//            sanity.increaseSanityAndSync(data.currentDrain, target);
//        }
//
//        // Ramp up drain every 30s and resend coordinates until the 4th
//        data.ticksSinceLastRamp++;
//        if (data.ticksSinceLastRamp >= RAMP_INTERVAL_TICKS) {
//            data.ticksSinceLastRamp = 0;
//            data.rampCount++;
//
//            if (data.rampCount >= 4) {
//                // 4th ramp — remove guidance entirely
//                stopGuidance(targetUUID);
//                AbilityUtil.sendActionBar(target,
//                        Component.literal("The coincidences fade.").withColor(0xFFe3ffff));
//                return;
//            }
//
//            // Ramps 1-3: multiply drain by 3x each interval
//            data.currentDrain *= 1.5f;
//            sendGuidanceMessage(target, data.targetPos);
//        }
//    }
//
//    private static void stopGuidanceAndCleanup(UUID targetUUID, ServerLevel level) {
//        ACTIVE_GUIDANCE.remove(targetUUID);
//        UUID authorUUID = TARGET_TO_AUTHOR.remove(targetUUID);
//        if (authorUUID != null) {
//            AUTHOR_TARGET.remove(authorUUID);
//            // Book already consumed on cast — no need to remove it again
//        }
//    }
//
//
//    private static void removeBookFromAuthor(ServerPlayer author) {
//        for (int i = 0; i < author.getInventory().getContainerSize(); i++) {
//            ItemStack stack = author.getInventory().getItem(i);
//            if (stack.getItem() instanceof StoryBookItem) {
//                author.getInventory().removeItem(i, 1);
//                break;
//            }
//        }
//    }
//    @SubscribeEvent
//    public static void onEntityDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
//        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;
//
//        UUID deadUUID = event.getEntity().getUUID();
//
//        // Check if dead entity is a tracked target
//        UUID authorUUID = TARGET_TO_AUTHOR.get(deadUUID);
//        if (authorUUID != null) {
//            stopGuidance(deadUUID);
//            TARGET_TO_AUTHOR.remove(deadUUID);
//            AUTHOR_TARGET.remove(authorUUID);
//
//            ServerPlayer author = serverLevel.getServer().getPlayerList().getPlayer(authorUUID);
//            if (author != null) removeBookFromAuthor(author);
//        }
//    }
//
//    public static UUID getTargetForAuthor(UUID authorUUID) {
//        return AUTHOR_TARGET.get(authorUUID);
//    }
//
//    private void consumeBookUse(ServerPlayer author) {
//        for (int i = 0; i < author.getInventory().getContainerSize(); i++) {
//            ItemStack stack = author.getInventory().getItem(i);
//            if (!(stack.getItem() instanceof StoryBookItem)) continue;
//            StoryBookItem.decrementUses(stack);
//            if (StoryBookItem.getUsesRemaining(stack) <= 0) {
//                author.getInventory().removeItem(i, 1);
//                AUTHOR_TARGET.remove(author.getUUID());
//                UUID targetUUID = TARGET_TO_AUTHOR.entrySet().stream()
//                        .filter(e -> e.getValue().equals(author.getUUID()))
//                        .map(Map.Entry::getKey)
//                        .findFirst().orElse(null);
//                if (targetUUID != null) TARGET_TO_AUTHOR.remove(targetUUID);
//            }
//            break;
//        }
//    }
}

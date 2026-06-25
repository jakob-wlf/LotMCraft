package de.jakob.lotm.beyonders.abilities.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryLoosingControlHandler;
import de.jakob.lotm.beyonders.abilities.visionary.passives.MetaAwarenessAbility;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.Prophecy;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.TriggerHelper;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class PsychologicalCueAbility extends ToggleAbility {
    public static final HashMap<UUID, Integer> map = new HashMap<>();

   public PsychologicalCueAbility(String id) {
        super(id);
        canBeUsedByNPC = false;
        canBeCopied = false;
        canBeReplicated = false;
        cannotBeStolen = true;
        autoClear = false;
        canBeShared = false;
        canBeUsedInArtifact = false;
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        map.put(entity.getUUID(), AbilityUtil.getSeqWithArt(entity, this));

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        if(VisionaryHandler.shouldBeAffectedWithMindWorldSeal(entitySeq)){
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.mind_world_authority_ability.is_sealed")
                            .withColor(0xFFff124d));
            cancel((ServerLevel) level, entity);
        }
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            if(entity.isShiftKeyDown())
                ClientHandler.openPsychologicalCueExplanation();
            return;
        }

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        if(VisionaryHandler.shouldBeAffectedWithMindWorldSeal(entitySeq)){
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.mind_world_authority_ability.is_sealed")
                            .withColor(0xFFff124d));
            return;
        }

        if(entity.isShiftKeyDown()) {
            cancel((ServerLevel) level, entity);
            return;
        }

        if(StoryWritingAbility.writingMap.containsKey(entity.getUUID())){
            cancel((ServerLevel) level, entity);
        }

        map.put(entity.getUUID(), AbilityUtil.getSeqWithArt(entity, this));
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        map.remove(entity.getUUID());
        clearArtifactScaling(entity);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 7));
    }

    @Override
    protected float getSpiritualityCost() {
        return 3.5f;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onChatMessageSent(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();

        if (!map.containsKey(player.getUUID())) return;

        event.setCanceled(true);

        String rawMessage = event.getRawText();

        var trigger = TriggerHelper.deduceWithContext(rawMessage, map.get(player.getUUID()), player);
        if(trigger == null){
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.story_writing.failed"));
            return;
        }

        var target = player.level().getPlayerByUUID(trigger.getTarget());
        if(target == null){
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.story_writing.failed"));
            return;
        }

        var virtualPersonaCaster = player.getData(ModAttachments.VIRTUAL_PERSONAS.get());

        Integer distance = TriggerHelper.getDistanceToTarget(player, trigger.getTarget());
        if (distance == null) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.story_writing.failed"));
            return;
        }

        if(!virtualPersonaCaster.affects(target.getName().getString())) {
            if (distance > getDistancePerSeq(map.get(player.getUUID()))) {
                AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.story_writing.failed"));
                return;
            }
        }

        if(target instanceof ServerPlayer playerTarget)
            MetaAwarenessAbility.sendWithMessage(player, playerTarget, "Tried to use cue");

        int targetSeq = BeyonderData.getSequence(target);
        if(AbilityUtil.isTargetSignificantlyStronger(map.get(player.getUUID()), targetSeq)){
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.story_writing.failed"));
            player.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 25, VisionaryLoosingControlHandler.getBasePerSeq(targetSeq), false, false, false));
            return;
        }

        BeyonderData.playerMap.addProphecy(trigger.getTarget(), new Prophecy(trigger.getTarget(), trigger, trigger.getType(), player.getUUID()));
    }

    private static int getDistancePerSeq(int seq){
        return switch(seq){
            case 7 -> 10;
            case 6 -> 20;
            case 5 -> 30;
            case 4 -> 50;
            case 3 -> 75;
            case 2 -> 125;
            case 1 -> 200;
            case 0 -> 300;
            default -> 5;
        };
    }

}

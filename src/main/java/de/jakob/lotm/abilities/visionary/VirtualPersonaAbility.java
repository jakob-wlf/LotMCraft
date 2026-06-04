package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.abilities.visionary.passives.MetaAwarenessAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.AvatarEntity;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.AbilitySelectionPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.RingEffectManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class VirtualPersonaAbility extends SelectableAbility {

    public VirtualPersonaAbility(String id) {
        super(id, 3f);
        canBeUsedByNPC = false;
        canBeCopied = false;
        canBeReplicated = false;
        canBeUsedInArtifact = false;
        cannotBeStolen = true;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 500;
    }

    @Override
    public String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.virtual_persona.self",
                "ability.lotmcraft.virtual_persona.move",
                "ability.lotmcraft.virtual_persona.check",
                "ability.lotmcraft.virtual_persona.create_avatar"
        };
    }

    @Override
    public void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {

        switch (abilityIndex) {
            case 0 -> virtualSelf(level, entity);
            case 1 -> move(level, entity);
            case 2 -> check(level, entity);
            case 3 -> createAvatar(level, entity);
        }
    }

    private void createAvatar(Level level, LivingEntity entity){
        if(level.isClientSide) return;

        int seq = BeyonderData.getSequence(entity);
        if(seq > 3) return;

        var component = entity.getData(ModAttachments.VIRTUAL_PERSONAS.get());
        if(component.createAvatar()){
            spawnAvatar((ServerLevel) level, entity);
        }
    }

    private void check(Level level, LivingEntity entity){
        if (level.isClientSide) return;

        int seq = BeyonderData.getSequence(entity);
        var target = AbilityUtil.getTargetEntity(entity, (int) (20 * multiplier(entity)), 1.2f);

        if(target == null){
            var component = entity.getData(ModAttachments.VIRTUAL_PERSONAS.get());

            var affects = component.getAffects();
            String info = component.getGeneralInfo(seq);
            var affectedBy = component.getAffectedBy(seq);

            StringBuilder affectsResultBuilder = new StringBuilder("Affects:");
            for(var obj : affects){
                var data = BeyonderData.playerMap.get(BeyonderData.playerMap.getKeyByName(obj)).get();

                String location = "";
                var targetLoop = level.getPlayerByUUID(BeyonderData.playerMap.getKeyByName(obj));
                if(targetLoop != null){
                    var pos = targetLoop.position();
                    location = " Location: x = " + (int) pos.x + " y = " + (int) pos.y + " z = " + (int) pos.z;
                }

                affectsResultBuilder.append("\n").append(obj)
                        .append(" --- Path: ").append(data.pathway())
                        .append(" Seq: ").append(data.sequence())
                        .append(location);
            }
            var affectsResult = affectsResultBuilder.toString();

            StringBuilder affectedByResultBuilder = new StringBuilder("Affected by:");
            for(var obj : affectedBy){
                affectedByResultBuilder.append("\n").append(obj);
            }
            var affectedByResult = affectedByResultBuilder.toString();

            entity.sendSystemMessage(Component.literal(info + "\n\n" + affectsResult + "\n\n" + affectedByResult + "\n\n")
                    .withColor(0xf5c56c));

            return;
        }

        int targetSeq = BeyonderData.getSequence(target);
        var component = target.getData(ModAttachments.VIRTUAL_PERSONAS.get());

        var affects = component.getAffects();
        String info = component.getGeneralInfo(targetSeq);
        var affectedBy = component.getAffectedBy(seq);

        StringBuilder affectsResultBuilder = new StringBuilder("Affects:");
        for(var obj : affects){
            affectsResultBuilder.append("\n").append(obj);
        }
        var affectsResult = affectsResultBuilder.toString();

        StringBuilder affectedByResultBuilder = new StringBuilder("Affected by:");
        for(var obj : affectedBy){
            affectedByResultBuilder.append("\n").append(obj);
        }
        var affectedByResult = affectedByResultBuilder.toString();

        entity.sendSystemMessage(Component.literal(info + "\n\n" + affectsResult + "\n\n" + affectedByResult + "\n\n")
                .withColor(0xf5c56c));
    }

    private void move(Level level, LivingEntity entity){
        if (level.isClientSide) return;

        var component = entity.getData(ModAttachments.VIRTUAL_PERSONAS.get());
        int seq = BeyonderData.getSequence(entity);

        if(!component.hasOnSelf()){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.dream_traversal.failed")
                    .withColor(0xFFff124d));
            return;
        }

        var target = AbilityUtil.getTargetEntity(entity, (int) (20 * multiplier(entity)), 1.2f);
        if(target == null){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.dream_traversal.failed")
                    .withColor(0xFFff124d));
            return;
        }

        if(VisionaryHandler.shouldFailAndTrigger(seq, entity, target, this)){
            return;
        }
        else if(VisionaryHandler.shouldStayInvisible(seq, target)){
            return;
        }

        if(!(entity instanceof ServerPlayer player)) return;
        if(!(target instanceof ServerPlayer targetPlayer)) return;

        var personas = target.getData(ModAttachments.VIRTUAL_PERSONAS.get());
        if(component.affects(target.getName().getString())){
            component.removeAffects(targetPlayer.getName().getString(), entity.getName().getString(),(ServerLevel) level);
        }
        else {
            personas.placeBy(player, targetPlayer);
        }
    }

    private void virtualSelf(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        var component = entity.getData(ModAttachments.VIRTUAL_PERSONAS.get());
        int seq = BeyonderData.getSequence(entity);

        if(component.outOfSlots(seq)){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.dream_traversal.failed")
                    .withColor(0xFFff124d));
            return;
        }

        level.playSound(null,
                entity.position().x, entity.position().y, entity.position().z,
                SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1, 1);

        component.create(seq);
    }

    @Override
    public void nextAbility(LivingEntity entity){
        if(getAbilityNames().length == 0)
            return;

        if(!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        int selectedAbility = selectedAbilities.get(entity.getUUID());
        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        selectedAbility++;
        if(selectedAbility >= getAbilityNames().length) {
            selectedAbility = 0;
        }

        if(entitySeq > 3 && selectedAbility >= 3){
            selectedAbility = 0;
        }

        selectedAbilities.put(entity.getUUID(), selectedAbility);
        PacketHandler.sendToServer(new AbilitySelectionPacket(getId(), selectedAbility));
    }

    @Override
    public void previousAbility(LivingEntity entity){
        if(getAbilityNames().length == 0)
            return;

        if(!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        int selectedAbility = selectedAbilities.get(entity.getUUID());
        selectedAbility--;
        if(selectedAbility <= -1) {
            selectedAbility = getAbilityNames().length - 1;
        }

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        if(entitySeq > 3 && selectedAbility >= 3){
            selectedAbility = 2;
        }

        selectedAbilities.put(entity.getUUID(), selectedAbility);
        PacketHandler.sendToServer(new AbilitySelectionPacket(getId(), selectedAbility));
    }

    private void spawnAvatar(ServerLevel serverLevel, LivingEntity entity) {
        if (!BeyonderData.isBeyonder(entity)) return;

        int casterSequence = BeyonderData.getSequence(entity);
        int avatarSequence = casterSequence + 2; // 2 sequences weaker

        AvatarEntity avatar = new AvatarEntity(
                ModEntities.ERROR_AVATAR.get(),
                serverLevel,
                entity.getUUID(),
                "visionary",
                avatarSequence
        );
        avatar.setPos(entity.getX(), entity.getY(), entity.getZ());
        serverLevel.addFreshEntity(avatar);

        AbilityUtil.sendActionBar(entity,
                Component.translatable("ability.lotmcraft.virtual_persona.avatar_spawned").withColor(0xFFe3ffff));
    }


    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        var entity = event.getEntity();

        if(!(entity instanceof ServerPlayer player)) return;

        if(event.getSource().is(ModDamageTypes.LOOSING_CONTROL)){
            var component = player.getData(ModAttachments.VIRTUAL_PERSONAS.get());

            float amount = event.getAmount();

            amount = component.block(amount);
            event.setAmount(amount);

            if(amount <= 0)
                event.setCanceled(true);
        }
    }
}

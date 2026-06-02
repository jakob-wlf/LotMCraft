package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.visionary.passives.MetaAwarenessAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.AvatarEntity;
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
        return new String[]{"ability.lotmcraft.virtual_persona.self"};
    }

    @Override
    public void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {

        switch (abilityIndex) {
            case 0 -> virtualSelf(level, entity);
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


//    private void virtualOthers(Level level, LivingEntity entity) {
//        if (level.isClientSide) return;
//        if (!(level instanceof ServerLevel serverLevel)) return;
//
//        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 2);
//
//        // S3 and below with no target: spawn an avatar instead
//        if (target == null) {
//            if (BeyonderData.getSequence(entity) <= 3) {
//                spawnAvatar(serverLevel, entity);
//            } else {
//                AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.frenzy.no_target").withColor(0xFFff124d));
//            }
//            return;
//        }
//
//        int targetSeq = BeyonderData.getSequence(target);
//        if(BeyonderData.getPathway(target).equals("visionary") && BeyonderData.getSequence(target) <
//                BeyonderData.getSequence(entity)){
//            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.dream_traversal.failed").withColor(0xFFff124d));
//
//            if(targetSeq <= 1 && target instanceof ServerPlayer targetPlayer && entity instanceof ServerPlayer entityPlayer){
//                MetaAwarenessAbility.onDivined(targetPlayer, entityPlayer);
//            }
//
//            return;
//        }
//
//        applyVirtualPersonaStack(level, target);
//    }


//    private void applyVirtualPersonaStack(Level level, LivingEntity entity) {
//        SanityComponent sanity = entity.getData(ModAttachments.SANITY_COMPONENT);
//        int current = sanity.getVirtualPersonaStacks();
//
//        if (current >= getMaxPersonasPerSeq(BeyonderData.getSequence(entity))) {
//            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.virtual_persona.max_stacks").withColor(0xFFffad33));
//            return;
//        }
//
//        sanity.addVirtualPersonaStack();
//
////        RingEffectManager.createRingForAll(
////                entity.getEyePosition().subtract(0, .4, 0),
////                2, 60,
////                250 / 255f, 201 / 255f, 102 / 255f,
////                1, .5f, .75f,
////                (ServerLevel) level
////        );
//
//
//
//        AbilityUtil.sendActionBar(entity,
//                Component.translatable("ability.lotmcraft.virtual_persona.stacks",
//                        sanity.getVirtualPersonaStacks()).withColor(0xFFe3ffff));
//    }


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

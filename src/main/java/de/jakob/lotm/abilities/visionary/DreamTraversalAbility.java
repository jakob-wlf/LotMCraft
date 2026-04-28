package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.visionary.passives.MetaAwarenessAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.ParasitationComponent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.OpenPlayerDivinationScreenPacket;
import de.jakob.lotm.network.packets.toServer.AbilitySelectionPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.PlayerSelectionWorkType;
import de.jakob.lotm.util.data.PlayerInfo;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class DreamTraversalAbility extends SelectableAbility {
    private static final HashMap<UUID, UUID> hideMap = new HashMap<>();
    private static final HashMap<UUID, Integer> hideSeqMap = new HashMap<>();
    //this is for artifact stuff
    //tbh wonkiest ability for atifact cuz it uses selectable (even though hide is a toggle style - cuz i didnt wanna make a whole other ability for it tbh

    public DreamTraversalAbility(String id) {
        super(id, 1f);
        this.autoClear = false;
        canBeUsedInArtifact = false;
        canBeCopied = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 5));
    }

    @Override
    public float getSpiritualityCost() {
        return 400;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.dream_traversal.jump",
                "ability.lotmcraft.dream_traversal.jump_range",
                "ability.lotmcraft.dream_traversal.hide"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        switch (abilityIndex) {
            case 0 -> jump(level, entity);
            case 1 -> jumpInRange(level, entity);
            case 2 -> hide(level, entity);
        }
    }

    private static final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(250 / 255f, 201 / 255f, 102 / 255f),
            1f
    );

    private void jumpInRange(Level level, LivingEntity entity){
        if (!(level instanceof ServerLevel serverLevel)) return;

        var server = entity.getServer();
        if (server == null) return;

        if(!(entity instanceof ServerPlayer player)) return;

        List<PlayerInfo> players = server.getPlayerList()
                .getPlayers()
                .stream()
                .filter(p -> p != player)
                .map(p -> new PlayerInfo(p.getUUID(), p.getGameProfile().getName()))
                .toList();

        PacketDistributor.sendToPlayer(
                player,
                new OpenPlayerDivinationScreenPacket(players, PlayerSelectionWorkType.DREAM_TRAVERSAL)
        );
    }

    private void jump(Level level, LivingEntity entity) {
        LivingEntity target = AbilityUtil.getTargetEntity(entity, (int) (20 * multiplier(entity)), 1.5f);

        if (target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.dream_traversal.no_target").withColor(0xFFff124d));
            return;
        }

        if(level.isClientSide) {
            ParticleUtil.spawnParticles((ClientLevel) level, dust, target.position().add(0, entity.getEyeHeight() / 2, 0), 100, .35, entity.getEyeHeight() / 2, .35, 0);
            return;
        }

        if (!(level instanceof ServerLevel serverLevel)) return;


        int targetSeq = BeyonderData.getSequence(target);
        if(BeyonderData.getPathway(target).equals("visionary") && BeyonderData.getSequence(target) <
                BeyonderData.getSequence(entity)){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.dream_traversal.failed").withColor(0xFFff124d));

            if(targetSeq <= 1 && target instanceof ServerPlayer targetPlayer && entity instanceof ServerPlayer entityPlayer){
                MetaAwarenessAbility.onDivined(entityPlayer, targetPlayer);
            }

            return;
        }

        if (requiresAsleep(entity) && !target.hasEffect(ModEffects.ASLEEP)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.dream_traversal.must_be_asleep").withColor(0xFFff124d));
            return;
        }

        // If currently hiding, jump to new host without cancelling hide
        if (hideMap.containsKey(entity.getUUID())) {
            Entity oldHostEntity = serverLevel.getEntity(hideMap.get(entity.getUUID()));
            if (oldHostEntity instanceof LivingEntity oldHost) {
                ParasitationComponent oldParasite = oldHost.getData(ModAttachments.PARASITE_COMPONENT);
                oldParasite.setParasited(false);
                oldParasite.setParasiteUUID(null);
            }

            hideMap.put(entity.getUUID(), target.getUUID());
            ParasitationComponent newParasite = target.getData(ModAttachments.PARASITE_COMPONENT);
            newParasite.setParasited(true);
            newParasite.setParasiteUUID(entity.getUUID());
        }

        performTeleport(entity, target);
    }

    private void hide(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(entity instanceof ServerPlayer player)) return;

        if (hideMap.containsKey(entity.getUUID())) {
            cancelHide(serverLevel, entity);
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 40 * (int) Math.max(multiplier(entity)/4,1), 1.5f);

        if (target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.dream_traversal.no_target").withColor(0xFFff124d));
            return;
        }

        int targetSeq = BeyonderData.getSequence(target);
        if(BeyonderData.getPathway(target).equals("visionary") && BeyonderData.getSequence(target) <
                BeyonderData.getSequence(player)){
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.dream_traversal.failed").withColor(0xFFff124d));

            if(targetSeq <= 1 && target instanceof ServerPlayer targetPlayer){
                MetaAwarenessAbility.onDivined(player, targetPlayer);
            }

            return;
        }

        if (requiresAsleep(entity) && !target.hasEffect(ModEffects.ASLEEP)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.dream_traversal.must_be_asleep").withColor(0xFFff124d));
            return;
        }

        hideMap.put(entity.getUUID(), target.getUUID());
        // Store sequence at cast time for artifact stuff
        hideSeqMap.put(entity.getUUID(), AbilityUtil.getSeqWithArt(entity, this));

        ParasitationComponent parasitationComponent = target.getData(ModAttachments.PARASITE_COMPONENT);
        parasitationComponent.setParasited(true);
        parasitationComponent.setParasiteUUID(entity.getUUID());

        player.setBoundingBox(new AABB(
                player.getX(), player.getY(), player.getZ(),
                player.getX(), player.getY(), player.getZ()
        ));
        player.onUpdateAbilities();
        player.hurtMarked = true;

        PsychologicalInvisibilityAbility.addInvisFromOtherSkills(entity, hideSeqMap.get(entity.getUUID()));
    }

    public static void cancelHide(ServerLevel serverLevel, LivingEntity entity) {
        if (!hideMap.containsKey(entity.getUUID())) return;

        Entity hostEntity = serverLevel.getEntity(hideMap.get(entity.getUUID()));
        if (hostEntity instanceof LivingEntity host) {
            ParasitationComponent parasitationComponent = host.getData(ModAttachments.PARASITE_COMPONENT);
            parasitationComponent.setParasited(false);
            parasitationComponent.setParasiteUUID(null);
        }

        hideMap.remove(entity.getUUID());
        hideSeqMap.remove(entity.getUUID());

        if (entity instanceof Player player) {
            player.setBoundingBox(player.getDimensions(player.getPose()).makeBoundingBox(
                    player.getX(), player.getY(), player.getZ()
            ));
            player.onUpdateAbilities();
            player.hurtMarked = true;
        }

        PsychologicalInvisibilityAbility.removeInvisFromOtherSkills(entity);
    }

    public static boolean requiresAsleep(LivingEntity entity) {
        return BeyonderData.getSequence(entity) > 3;
    }

    public static int getRangeBySeq(int seq){
        return switch (seq){
            case 5 -> 100;
            case 4 -> 250;
            case 3 -> 500;
            case 2 -> 1000;
            case 1 -> 2500;
            case 0 -> 10000;
            default -> 0;
        };
    }

    public static void performTeleport(LivingEntity entity, LivingEntity target){
        entity.teleportTo(target.getX(), target.getY(), target.getZ());
        ParticleUtil.spawnParticles((ServerLevel) entity.level(), dust, target.position().add(0, entity.getEyeHeight() / 2, 0), 100, .35, entity.getEyeHeight() / 2, .35, 0);
    }

    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        var entity = event.getEntity();
        if(!(entity.level() instanceof ServerLevel level)) return;

        if(isHiding(entity.getUUID())){
            if(event.getSource().is(ModDamageTypes.LOOSING_CONTROL)){
                cancelHide(level, entity);
            }
            else{
                event.setAmount(0.0f);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        if(!(event.getLevel() instanceof ServerLevel level)) return;

        LivingEntity entity = event.getEntity();

        if (hideMap.containsKey(entity.getUUID())) {
            DreamTraversalAbility.cancelHide(level, entity);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer entity)) return;
        if (entity.level().isClientSide) return;
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;
        if (!hideMap.containsKey(entity.getUUID())) return;

        Entity hostEntity = serverLevel.getEntity(hideMap.get(entity.getUUID()));

        // Use the sequence stored at cast time (ie the artifact if used)
        boolean seqUnlocked = hideSeqMap.getOrDefault(entity.getUUID(), 9) <= 3;

        if (hostEntity == null || hostEntity.isRemoved() || !(hostEntity instanceof LivingEntity host)
                || !host.isAlive() || (!seqUnlocked && !host.hasEffect(ModEffects.ASLEEP))) {

            cancelHide(serverLevel, entity);
            return;
        }

        Vec3 hostPos = host.position();
        Vec3 floatPos = hostPos.add(0, host.getBbHeight() + 0.3, 0);
        entity.teleportTo(floatPos.x, floatPos.y, floatPos.z);
        entity.setDeltaMovement(Vec3.ZERO);

        if (entity instanceof Player player) {
            player.setBoundingBox(new AABB(
                    player.getX(), player.getY(), player.getZ(),
                    player.getX(), player.getY(), player.getZ()
            ));
            player.hurtMarked = true;
        }

        if(host instanceof Mob mob){
            if(mob.getTarget() != null && mob.getTarget().equals(entity)){
                mob.setTarget(null);
            }
        }
    }

    public static boolean isHiding(UUID uuid) {
        return hideMap.containsKey(uuid);
    }
}

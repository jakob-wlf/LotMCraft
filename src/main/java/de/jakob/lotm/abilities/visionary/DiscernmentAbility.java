package de.jakob.lotm.abilities.visionary;

import com.mojang.datafixers.util.Pair;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.visionary.passives.MetaAwarenessAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.OpenDiscernmentScreenPacket;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.DiscernmentUtil;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class DiscernmentAbility extends SelectableAbility {
    public static HashMap<UUID, Pair<String, Integer>> preMeditation = new HashMap<>();
    public static HashSet<UUID> meditating = new HashSet<>();

    public DiscernmentAbility(String id) {
        super(id, 5f);

        canBeCopied = false;
        canBeUsedByNPC = false;
        cannotBeStolen = true;
        canBeReplicated = false;
        canBeUsedInArtifact = false;
        canBeShared = false;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.discernment_ability.gather_role",
                "ability.lotmcraft.discernment_ability.immerse_into_role",
                "ability.lotmcraft.discernment_ability.meditate"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        switch (selectedAbility) {
            case 0 -> gatherRole(level, entity);
            case 1 -> immerseIntoRole(level, entity);
            case 2 -> meditate(level, entity);
        }
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 350;
    }

    private void gatherRole(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(entity instanceof ServerPlayer player)) return;

        var target = AbilityUtil.getTargetEntity(player, 100, 1f);
        if (target == null) return;

        if (!BeyonderData.isBeyonder(target)) return;

        String path = BeyonderData.getPathway(target);
        int seq = BeyonderData.getSequence(target);

        int diff = seq - BeyonderData.getSequence(entity) ;

        if (random.nextInt(10 - diff) != 0) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.discernment_ability.failed").withColor(0xf5c56c));
            return;
        }

        preMeditation.put(entity.getUUID(), Pair.of(path, seq));
        player.sendSystemMessage(Component.literal("Discerned role: sequence " + seq + " of " + path + " pathway").withColor(0xf5c56c));

        if(target instanceof ServerPlayer playerTarget) {
            if (path.equals("visionary") && seq <= 1) {
                MetaAwarenessAbility.onDivined(player, playerTarget);
            }
        }
    }

    private void meditate(Level level, LivingEntity entity){
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if(meditating.contains(entity.getUUID())) return;;

        meditating.add(entity.getUUID());

        ServerScheduler.scheduleForDuration(0, 2, 20 *
                getMeditationDuration(BeyonderData.getSequence(entity), preMeditation.get(entity.getUUID()).getSecond().intValue()),
                ()-> {
                    if(!entity.isAlive() || entity.isRemoved()) {
                        preMeditation.remove(entity.getUUID());
                        meditating.remove(entity.getUUID());
                        return;
                    }

                    var pos = entity.position();
                    entity.teleportTo(pos.x, pos.y, pos.z);
                    entity.setDeltaMovement(Vec3.ZERO);

                    var component = entity.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT.get());
                    component.disableAbilityUsageForTime("visionary_meditation", 20, entity);

                    entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 20 * 2, 10, false, false, false));
                    entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 2, 10, false, false, false));
                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 2, 10, false, false, false));
                    },
                () -> {
                        var component = player.getData(ModAttachments.DISCERNMENT_DATA.get());
                        var pair = preMeditation.get(entity.getUUID());

                        component.add(pair.getFirst(), pair.getSecond());

                        preMeditation.remove(entity.getUUID());
                        meditating.remove(entity.getUUID());
                },
                serverLevel);
    }

    private void immerseIntoRole(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(entity instanceof ServerPlayer player)) return;

        var component = player.getData(ModAttachments.DISCERNMENT_DATA.get());
        if (component.isDiscerning()) DiscernmentUtil.stopDiscernment(player);

        PacketHandler.sendToPlayer(player, new OpenDiscernmentScreenPacket(component.getSavedPathsAndSeqs()));
    }

    private static int getMeditationDuration(int seq, int targetSeq){
        int diff = 10 - (targetSeq - seq);

        return 20 * diff;
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        meditating.remove(player.getUUID());
    }
}

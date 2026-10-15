package de.jakob.lotm.beyonders.abilities.red_priest;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TeamComponent;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.gui.custom.cohort.CohortMenuProvider;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.CohortUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.subordinates.SubordinateUtils;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ChainOfCommandAbility extends SelectableAbility {

    public static ChainOfCommandAbility instance;

    public ChainOfCommandAbility(String id) {
        super(id, 1);

        instance = this;

        canBeUsedByNPC = false;

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(3, 4, 4, 5, 5));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(4000f, 1600f, 890f, 570f, 624f));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(.2f, .1f, .1f), 2);

    private final int INVITE_COST = 30;
    private final int FOCUS_COST = 20;
    private final int ALLEVIATION_COST = 20;

    @Override
    protected String[] getAbilityNames() {
        return new String[] {
                "ability.lotmcraft.chain_of_command.invite_soldier",
                "ability.lotmcraft.chain_of_command.damage_focus",
                "ability.lotmcraft.chain_of_command.damage_alleviation",
                "ability.lotmcraft.chain_of_command.manage",
        };
    }


    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        switch (selectedAbility) {
            case 0 -> invite(level, entity);
            case 1 -> damageBoost(level, entity);
            case 2 -> damageAlleviation(level, entity);
            case 3 -> manage(level, entity);

        }
    }



    private void damageAlleviation(Level level, LivingEntity entity) {
        if(level.isClientSide || !(entity instanceof ServerPlayer player)) return;
        TeamComponent team = player.getData(ModAttachments.TEAM_COMPONENT.get());
        if(CohortUtil.getAllMembers(player).isEmpty()) {
            player.sendSystemMessage(Component.literal("You are not a part of a Red Priest Cohort").withStyle(ChatFormatting.RED));
            return;
        }
        player.sendSystemMessage(Component.literal(team.leaderUUID()));
        if(!team.leaderUUID().equals(player.getStringUUID())) {
            player.sendSystemMessage(Component.literal("you are not the leader of this team").withStyle(ChatFormatting.RED));
            return;
        }
        AtomicBoolean done = new AtomicBoolean(false);
        if(!CohortUtil.isAlleviation(player)) {
            CohortUtil.setAlleviation(player, true);
            player.sendSystemMessage(Component.literal("Damage Alleviation has been activated").withStyle(ChatFormatting.GREEN));
            done.set(false);
        } else {
            CohortUtil.setAlleviation(player, false);
            player.sendSystemMessage(Component.literal("Dammage Alleviation has been deactivated").withStyle(ChatFormatting.RED));
            done.set(true);
        }

        if(CohortUtil.isAlleviation(player)) {
            ServerScheduler.scheduleUntil((ServerLevel) level, () -> {
                if(!CohortUtil.isAlleviation(player)) done.set(true);
                BeyonderData.reduceSpirituality(entity, ALLEVIATION_COST);
                if(BeyonderData.getSpirituality(entity) < ALLEVIATION_COST) {
                    CohortUtil.setAlleviation(player, false);
                    done.set(true);
                }
            }, 5, null, done);
        }
    }

    private void damageBoost(Level level, LivingEntity entity) {
        if(level.isClientSide || !(entity instanceof ServerPlayer player)) return;
        TeamComponent team = player.getData(ModAttachments.TEAM_COMPONENT.get());
        if(CohortUtil.getALlPlayers(player).isEmpty()) {
            player.sendSystemMessage(Component.literal("You are not a part of a Red Priest Team").withStyle(ChatFormatting.RED));
            return;
        }
        if(!team.leaderUUID().equals(player.getStringUUID())) {
            player.sendSystemMessage(Component.literal("you are not the leader of this team").withStyle(ChatFormatting.RED));
            return;
        }
        AtomicBoolean done = new AtomicBoolean(false);
        if(!CohortUtil.isFocus(player)) {
            CohortUtil.setFocus(player, true);
            player.sendSystemMessage(Component.literal("Damage Focus has been activated").withStyle(ChatFormatting.GREEN));
            done.set(false);

        } else {
            CohortUtil.setFocus(player, false);
            player.sendSystemMessage(Component.literal("Damage Focus has been deactivated").withStyle(ChatFormatting.RED));
            done.set(true);
        }

        if(CohortUtil.isFocus(player)) ServerScheduler.scheduleUntil((ServerLevel) level, () -> {
            if(!CohortUtil.isFocus(player)) done.set(true);
            BeyonderData.reduceSpirituality(entity, FOCUS_COST);
            if(BeyonderData.getSpirituality(entity) < FOCUS_COST) {
                CohortUtil.setFocus(player, false);
                done.set(true);
            }
        }, 5, null, done);
    }

    private void manage(Level level, LivingEntity entity) {
        if(level.isClientSide || !(entity instanceof ServerPlayer player)) return;
        TeamComponent team = player.getData(ModAttachments.TEAM_COMPONENT.get());
        if(CohortUtil.getAllMembers(player).isEmpty()) {
            player.sendSystemMessage(Component.literal("You are not a part of a Red Priest Cohort").withStyle(ChatFormatting.RED));
            return;
        }
        else if(!CohortUtil.getALlPlayers(player).isEmpty() && !team.leaderUUID().equals(player.getStringUUID())) {
            player.sendSystemMessage(Component.literal("you are not the leader of this team").withStyle(ChatFormatting.RED));
            return;
        }
        HashMap<String, List<String[]>> members = CohortUtil.getAllMembersGrouped(entity);
        player.openMenu(new CohortMenuProvider(members));
    }

    public void invite(Level level, LivingEntity entity) {
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 3, 1.5f);
        if (target == null) {
            if (entity instanceof ServerPlayer serverPlayer) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.translatable("ability.lotmcraft.chain_of_command.no_entity_found").withColor(0xFFff124d));
                serverPlayer.connection.send(packet);
            }
            return;
        }

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        int targetSeq = BeyonderData.getSequence(target);
        if (target instanceof ServerPlayer serverPlayer) {
            TeamComponent team = player.getData(ModAttachments.TEAM_COMPONENT.get());
            if(!CohortUtil.getALlPlayers(player).isEmpty()) {
                if (!team.leaderUUID().equals(player.getStringUUID())) {
                    player.sendSystemMessage(Component.literal("you are not the leader of this team").withStyle(ChatFormatting.RED));
                    return;
                }
            }
            CohortUtil.addMember(player, serverPlayer);
        } else {
            if (!BeyonderData.isBeyonder(target) || targetSeq > entitySeq) {
                SubordinateUtils.turnEntityIntoSubordinate(target, player, false);
                ParticleUtil.spawnParticles(serverLevel, dust, target.position().add(0, target.getEyeHeight() / 2, 0), 95, .5, target.getEyeHeight() / 2, .5, 0);
            } else {
                ParticleUtil.spawnParticles(serverLevel, dust, target.position().add(0, target.getEyeHeight() / 2, 0), 95, .5, target.getEyeHeight() / 2, .5, 0);
                if (entity instanceof ServerPlayer serverPlayer) {
                    ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.translatable("ability.lotmcraft.chain_of_command.no_entity_found").withColor(0xFFff124d));
                    serverPlayer.connection.send(packet);
                }
            }

            SubordinateUtils.turnEntityIntoSubordinate(target, player);
            ParticleUtil.spawnParticles(serverLevel, dust, target.position().add(0, target.getEyeHeight() / 2, 0), 95, .5, target.getEyeHeight() / 2, .5, 0);
        }

        }
        BeyonderData.reduceSpirituality(entity, INVITE_COST);
    }





    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();

        if(!instance.canUse(entity)) {
            return;
        }

        if(!(entity.level() instanceof ServerLevel) || !(entity instanceof ServerPlayer player)) return;
        if(CohortUtil.isAlleviation(player)) {
            if(!player.getData(ModAttachments.TEAM_COMPONENT.get()).isLeader(player)) return;
            HashMap<String, List<String[]>> all = CohortUtil.getAllMembersGrouped(entity);
            List<String[]> members = all.get("players").stream().filter(e -> e.length > 2).toList();
            StringBuilder msg = new StringBuilder();
            msg.append("Original damage + ").append(event.getAmount());
            List<LivingEntity> subordinates = CohortUtil.getAllSubordinates(entity);
            if(members.isEmpty() && subordinates.isEmpty()) return;
            float count = (subordinates.size()+members.size() + 1);
            float damageForEach = event.getAmount() / count;
            event.setAmount(damageForEach);
            subordinates.stream().filter(e -> !instance.canUse(e)).forEach(e -> e.hurt(event.getSource(), damageForEach));
            msg.append(" distrubted into ").append(damageForEach);
            msg.append(" among ").append(count).append(" players");
            for(String[] member : members) {
                if(player.getServer().getPlayerList().getPlayer(UUID.fromString(member[0])) instanceof ServerPlayer serverPlayer) {
                    serverPlayer.hurt(event.getSource(), damageForEach);

                }
            }
//            player.sendSystemMessage(Component.literal(msg.toString()).withStyle(ChatFormatting.GREEN));
        }


    }

    @SubscribeEvent
    public static void onAttacking(LivingIncomingDamageEvent event) {
        if(!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if(!instance.canUse(player)) return;
        if(CohortUtil.isFocus(player)) {
            if(CohortUtil.getLeaderUUID(player) == null) return;
            if(Objects.equals(CohortUtil.getLeaderUUID(player), player.getUUID().toString())) {
                event.setAmount(event.getAmount()*2.0f);
            } else {
                if (!(event.getSource().getEntity() instanceof LivingEntity ent)) return;

                int count = CohortUtil.getALlPlayers(ent).size();
                if (count < 1) count = 1;

                float multiplier = 1.0f - 0.5f / count;
                event.setAmount(event.getAmount() * multiplier);
            }
        }
    }

}

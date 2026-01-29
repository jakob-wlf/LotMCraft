package de.jakob.lotm.abilities.red_priest;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.subordinates.SubordinateComponent;
import de.jakob.lotm.util.helper.subordinates.SubordinateUtils;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.joml.Vector3f;

import java.util.*;
import java.util.stream.StreamSupport;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ChainOfCommandAbility extends Ability {

    private static ChainOfCommandAbility instance;

    public ChainOfCommandAbility(String id) {
        super(id, 5);

        instance = this;

        canBeCopied = false;
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 900;
    }

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(.2f, .1f, .1f), 2);

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if(!(entity instanceof Player player)) {
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 3, 1.5f);
        if(target == null || target instanceof Player) {
            if(entity instanceof ServerPlayer serverPlayer) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.translatable("ability.lotmcraft.chain_of_command.no_entity_found").withColor(0xFFff124d));
                serverPlayer.connection.send(packet);
            }
            return;
        }

        if(!BeyonderData.isBeyonder(target) || BeyonderData.getSequence(target) > BeyonderData.getSequence(entity)) {
            SubordinateUtils.turnEntityIntoSubordinate(target, player);
            ParticleUtil.spawnParticles(serverLevel, dust, target.position().add(0, target.getEyeHeight() / 2, 0), 95, .5, target.getEyeHeight() / 2, .5, 0);
        }
        else {
            ParticleUtil.spawnParticles(serverLevel, dust, target.position().add(0, target.getEyeHeight() / 2, 0), 95, .5, target.getEyeHeight() / 2, .5, 0);
            if(entity instanceof ServerPlayer serverPlayer) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.translatable("ability.lotmcraft.chain_of_command.no_entity_found").withColor(0xFFff124d));
                serverPlayer.connection.send(packet);
            }
        }
    }

    private static ArrayList<LivingEntity> getSubordinatesOfPlayerInAllLevelsOrderedById(LivingEntity entity) {
        Level level = entity.level();

        if(level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return new ArrayList<>();
        }

        if(entity.getServer() == null) {
            return new ArrayList<>();
        }

        final ArrayList<LivingEntity> subordinates = new ArrayList<>(StreamSupport.stream(serverLevel.getAllEntities().spliterator(), false).filter(e -> e instanceof LivingEntity).map(e -> (LivingEntity) e).toList());

        for(ServerLevel l : entity.getServer().getAllLevels()) {
            if(l == level)
                continue;
            subordinates.addAll(StreamSupport.stream(l.getAllEntities().spliterator(), false).filter(e -> e instanceof LivingEntity).map(e -> (LivingEntity) e).toList());
        }

        subordinates.removeIf(e -> {
            if(e == entity)
                return true;
            SubordinateComponent component = e.getData(ModAttachments.SUBORDINATE_COMPONENT.get());
            if (!component.isSubordinate()) {
                return true;
            }

            if(!component.getControllerUUID().equals(entity.getStringUUID())) {
                return true;
            }

            return false;
        });

        subordinates.sort(Comparator.comparingInt(LivingEntity::getId));
        return subordinates;
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();

        if(!instance.canUse(entity)) {
            return;
        }

        List<LivingEntity> subordinates = getSubordinatesOfPlayerInAllLevelsOrderedById(entity);
        float damageForEach = event.getAmount() / (subordinates.size() + 1);
        event.setAmount(damageForEach);
        subordinates.stream().filter(e -> !instance.canUse(e)).forEach(e -> e.hurt(event.getSource(), damageForEach));
    }
}

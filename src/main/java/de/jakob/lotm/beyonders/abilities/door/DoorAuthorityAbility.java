package de.jakob.lotm.beyonders.abilities.door;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.attachments.DoorAuthorityData;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class DoorAuthorityAbility extends SelectableAbility {

    public DoorAuthorityAbility(String id) {
        super(id, 10);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 0));
    }

    @Override
    protected float getSpiritualityCost() {
        return 10000;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.door_authority.malfunction", "ability.lotmcraft.door_authority.strengthen"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if(level.isClientSide) {
            ClientHandler.applyCameraShakeToPlayersInRadius(3, 40, (ClientLevel) level, entity.position(), 20);
            return;
        }

        level.playSound(null,
                entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.AMETHYST_CLUSTER_BREAK,
                entity.getSoundSource(), 1.5f, 0.6f);

        level.playSound(null,
                entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.BEACON_ACTIVATE,
                entity.getSoundSource(), 2.5f, 1);

        Vec3 effectPos = entity.getEyePosition().subtract(0, .5, 0);

        ParticleUtil.spawnSphereParticles((ServerLevel) level, ParticleTypes.END_ROD, effectPos, 4, 450, .025);
        ParticleUtil.spawnSphereParticles((ServerLevel) level, ParticleTypes.PORTAL, effectPos, 3, 250, .025);

        for(int i = 3; i < 8; i++) {
            ParticleUtil.spawnCircleParticles((ServerLevel) level, ParticleTypes.ENCHANT, effectPos, i, i * 70);
        }

        ParticleUtil.spawnCircleParticles((ServerLevel) level, new DustParticleOptions(new Vector3f(131 / 255f, 225 / 255f, 235 / 255f), 4f), effectPos, 9, 300);

        String effectId = selectedAbility == 0 ? "malfunction" : "strengthen";
        DoorAuthorityData data = DoorAuthorityData.get((ServerLevel) level);
        data.activate(20 * 60 * 5, effectId);
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if(level.isClientSide()) {
            return;
        }

        DoorAuthorityData data = DoorAuthorityData.get((ServerLevel) level);
        if (!data.isActive()) {
            return;
        }
        String activeEffect = data.getEffectId();

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof DoorBlock) {
            if(activeEffect.equals("strengthen")) {
                event.setCanceled(true);
            }
        }
    }

}

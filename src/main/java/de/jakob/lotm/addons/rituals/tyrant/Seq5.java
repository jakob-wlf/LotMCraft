package de.jakob.lotm.addons.rituals.tyrant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq5 {
    private static final int SECONDS_PER_STAGE = 120;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("tyrant") ||
        BeyonderData.getSequence(player) != 6) return;
        if(!(player.level() instanceof ServerLevel level)) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        BlockPos pos = player.blockPosition();

        Structure monument = level.registryAccess()
                .lookupOrThrow(Registries.STRUCTURE)
                .getOrThrow(BuiltinStructures.OCEAN_MONUMENT)
                .value();

        boolean inMonument = level.structureManager()
                .getStructureWithPieceAt(pos, monument)
                .isValid();

        boolean targeted = !level.getEntitiesOfClass(
                Mob.class,
                player.getBoundingBox().inflate(32.0),
                mob -> mob instanceof Guardian && player.equals(mob.getTarget())
        ).isEmpty();

        if(inMonument && targeted){
            component.setCompleted(true);
        }
    }

}

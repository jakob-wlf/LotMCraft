package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import static com.sun.tools.attach.VirtualMachine.attach;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public final class IllusoryScalesRenderer {

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (EntityType<?> type : event.getEntityTypes()) {
            EntityRenderer<?> renderer = event.getRenderer(type);
            if (renderer instanceof LivingEntityRenderer<?, ?> living) {
                attach(living);
            }
        }

        // Players use separate renderers per skin model ("default" / "slim")
        for (var skin : event.getSkins()) {
            if (event.getSkin(skin) instanceof LivingEntityRenderer<?, ?> playerRenderer) {
                attach(playerRenderer);
            }
        }

    }

    private static <T extends LivingEntity> void addTo(EntityRenderersEvent.AddLayers event, EntityType<T> type) {
        EntityRenderer<?> raw = event.getRenderer(type);
        if (raw instanceof LivingEntityRenderer<?, ?> living) {
            @SuppressWarnings("unchecked")
            LivingEntityRenderer<T, EntityModel<T>> renderer = (LivingEntityRenderer<T, EntityModel<T>>) living;
            renderer.addLayer(new IllusoryScalesLayer<>(renderer));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void attach(LivingEntityRenderer<?, ?> renderer) {
        ((LivingEntityRenderer) renderer).addLayer(
                new IllusoryScalesLayer((LivingEntityRenderer) renderer, (java.util.function.Predicate<LivingEntity>) entity -> ((ToggleAbility) LOTMCraft.abilityHandler.getById("illusory_scales_ability")).isActiveForEntity(entity)));
    }
}
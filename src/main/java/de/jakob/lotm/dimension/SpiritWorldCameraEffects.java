package de.jakob.lotm.dimension;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class SpiritWorldCameraEffects {
    
    // Track per-player camera distortion data
    private static final Map<UUID, CameraDistortion> distortions = new HashMap<>();
    
    // Distortion parameters
    private static final float SHIFT_CHECK_CHANCE = 0.03f;
    private static final float FLIP_CHECK_CHANCE = 0.0075f;
    private static final int MIN_SHIFT_DURATION = 20; // 1 second
    private static final int MAX_SHIFT_DURATION = 120; // 5 seconds
    private static final int MIN_FLIP_DURATION = 40; // 2 seconds
    private static final int MAX_FLIP_DURATION = 120; // 6 seconds
    private static final float MAX_YAW_SHIFT = 45.0f; // degrees
    private static final float MAX_PITCH_SHIFT = 30.0f; // degrees
    
    private static class CameraDistortion {
        float targetYawShift = 0;
        float targetPitchShift = 0;
        float currentYawShift = 0;
        float currentPitchShift = 0;
        boolean isFlipped = false;
        long shiftEndTime = 0;
        long flipEndTime = 0;
        float flipProgress = 0; // 0 to 1 for smooth flipping
    }
    
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof LocalPlayer player)) {
            return;
        }
        
        // Only apply in Spirit World
        if (!player.level().dimension().equals(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY)) {
            // Clean up distortion if leaving dimension
            distortions.remove(player.getUUID());
            return;
        }
        
        UUID playerId = player.getUUID();
        CameraDistortion distortion = distortions.computeIfAbsent(playerId, k -> new CameraDistortion());
        long currentTime = player.level().getGameTime();
        
        // Check for new random shift
        if (currentTime >= distortion.shiftEndTime) {
            if (player.getRandom().nextFloat() < SHIFT_CHECK_CHANCE) {
                // Apply new random shift
                distortion.targetYawShift = (player.getRandom().nextFloat() - 0.5f) * 2 * MAX_YAW_SHIFT;
                distortion.targetPitchShift = (player.getRandom().nextFloat() - 0.5f) * 2 * MAX_PITCH_SHIFT;
                
                int duration = MIN_SHIFT_DURATION + player.getRandom().nextInt(MAX_SHIFT_DURATION - MIN_SHIFT_DURATION);
                distortion.shiftEndTime = currentTime + duration;
            } else {
                // Gradually return to normal
                distortion.targetYawShift = 0;
                distortion.targetPitchShift = 0;
            }
        }
        
        // Check for flip
        if (currentTime >= distortion.flipEndTime) {
            if (player.getRandom().nextFloat() < FLIP_CHECK_CHANCE) {
                // Flip the camera
                distortion.isFlipped = !distortion.isFlipped;
                
                int duration = MIN_FLIP_DURATION + player.getRandom().nextInt(MAX_FLIP_DURATION - MIN_FLIP_DURATION);
                distortion.flipEndTime = currentTime + duration;
            }
        }
        
        // Smooth interpolation for shifts
        float lerpSpeed = 0.1f;
        distortion.currentYawShift = Mth.lerp(lerpSpeed, distortion.currentYawShift, distortion.targetYawShift);
        distortion.currentPitchShift = Mth.lerp(lerpSpeed, distortion.currentPitchShift, distortion.targetPitchShift);
        
        // Smooth interpolation for flip
        float targetFlip = distortion.isFlipped ? 1.0f : 0.0f;
        distortion.flipProgress = Mth.lerp(0.05f, distortion.flipProgress, targetFlip);
    }
    
    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        // Only apply in Spirit World
        if (!mc.player.level().dimension().equals(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY)) {
            return;
        }
        
        CameraDistortion distortion = distortions.get(mc.player.getUUID());
        if (distortion == null) return;
        
        // Apply yaw and pitch shifts
        float newYaw = event.getYaw() + distortion.currentYawShift;
        float newPitch = event.getPitch() + distortion.currentPitchShift;
        
        // Apply flip rotation
        if (distortion.flipProgress > 0.01f) {
            // Roll the camera 180 degrees
            float rollAmount = distortion.flipProgress * 180.0f;
            newPitch += rollAmount; // This creates the upside-down effect
        }
        
        event.setYaw(newYaw);
        event.setPitch(newPitch);
    }
    
    @SubscribeEvent
    public static void onCameraSetup(net.neoforged.neoforge.client.event.ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        // Only apply in Spirit World
        if (!mc.player.level().dimension().equals(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY)) {
            return;
        }
        
        CameraDistortion distortion = distortions.get(mc.player.getUUID());
        if (distortion == null) return;
        
        // Apply camera roll for flipping
        if (distortion.flipProgress > 0.01f) {
            float roll = distortion.flipProgress * 180.0f;
            event.setRoll(roll);
        }
    }
}
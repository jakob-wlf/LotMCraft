package de.jakob.lotm.rituals.impl;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import de.jakob.lotm.rituals.RitualManager;
import de.jakob.lotm.rituals.RitualResultHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RitualMagicSummonEntity implements RitualResultHandler {

    @Override
    public void perform(Map<String, Object> params, ServerPlayer player) {
        SummonEntityResult result = deserializeParams(params, SummonEntityResult.class);
        if (result == null) return;

        for (SummonEntityResult.EntityEntry entry : result.entities) {
            for (int i = 0; i < entry.count(); i++) {
                summonEntity(entry.entityId(), player);
            }
        }
    }

    private void summonEntity(String id, ServerPlayer player) {
        Vec3 spawnPos = player.getEyePosition().add(0, .5, 0).add(new Vec3(player.getLookAngle().x, 0, player.getLookAngle().z).normalize().scale(2));

        ResourceLocation parsedId = ResourceLocation.parse(id);
        Optional<EntityType<?>> maybeType = BuiltInRegistries.ENTITY_TYPE.getOptional(parsedId);
        if(maybeType.isEmpty()) {
            return;
        };

        EntityType<?> entityType = maybeType.get();
        Entity entity = entityType.create(player.level());
        if(entity == null) {
            return;
        }

        entity.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        player.level().addFreshEntity(entity);
    }

    public record SummonEntityResult(List<EntityEntry> entities) {

        public record EntityEntry(
                @SerializedName("entity_id") String entityId,
                int count
        ) {}
    }

    public static <T> T deserializeParams(Map<String, Object> params, Class<T> type) {
        JsonElement json = RitualManager.GSON.toJsonTree(params);
        return RitualManager.GSON.fromJson(json, type);
    }
}

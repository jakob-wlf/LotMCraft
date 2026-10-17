package de.jakob.lotm.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class MiniatureCreatureRenderer extends BlockEntityWithoutLevelRenderer {
    private static MiniatureCreatureRenderer instance;
    private Entity cached;
    private int cachedHash;

    public MiniatureCreatureRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static MiniatureCreatureRenderer instance() {
        if (instance == null) instance = new MiniatureCreatureRenderer();
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        Entity creature = creature(stack);
        if (creature == null) return;

        boolean gui = context == ItemDisplayContext.GUI || context == ItemDisplayContext.FIXED;
        float yaw = gui ? 150f : 180f;
        creature.setYRot(yaw);
        creature.yRotO = yaw;
        if (creature instanceof LivingEntity living) {
            living.yBodyRot = yaw;
            living.yBodyRotO = yaw;
            living.yHeadRot = yaw;
            living.yHeadRotO = yaw;
        }

        float span = Math.max(creature.getBbWidth(), creature.getBbHeight());
        float scale = 0.75f / Math.max(span, 0.01f);
        pose.pushPose();
        pose.translate(0.5, 0.12, 0.5);
        pose.scale(scale, scale, scale);
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        draw(dispatcher.getRenderer(creature), creature, yaw, pose, buffer, gui ? LightTexture.FULL_BRIGHT : light);
        pose.popPose();
    }

    private Entity creature(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        CompoundTag stored = data.copyTag();
        int hash = stored.hashCode();
        Level level = Minecraft.getInstance().level;
        if (cached != null && hash == cachedHash && cached.level() == level) {
            if (level != null) cached.tickCount = (int) level.getGameTime();
            return cached;
        }
        if (level == null || !stored.contains("EntityNBT")) return null;
        var type = EntityType.byString(stored.getString("EntityType"));
        if (type.isEmpty()) return null;
        Entity created = type.get().create(level);
        if (created == null) return null;
        try {
            created.load(stored.getCompound("EntityNBT").copy());
        } catch (RuntimeException ignored) {
            return null;
        }
        created.setPos(0, 0, 0);
        created.xo = 0;
        created.yo = 0;
        created.zo = 0;
        created.tickCount = (int) level.getGameTime();
        cached = created;
        cachedHash = hash;
        return created;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity> void draw(EntityRenderer<?> renderer, Entity entity, float yaw, PoseStack pose, MultiBufferSource buffer, int light) {
        ((EntityRenderer<T>) renderer).render((T) entity, yaw, 0, pose, buffer, light);
    }
}

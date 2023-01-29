package io.github.thatrobin.client.entities;

import io.github.thatrobin.entities.SlimeFriendEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.SlimeEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.DyeableHorseArmorItem;

import java.awt.*;

@Environment(value= EnvType.CLIENT)
public class SlimeFriendOverlayFeatureRenderer<T extends LivingEntity>
        extends FeatureRenderer<T, SlimeFriendEntityModel<T>> {
    private final EntityModel<T> model;

    public SlimeFriendOverlayFeatureRenderer(FeatureRendererContext<T, SlimeFriendEntityModel<T>> context, EntityModelLoader loader) {
        super(context);
        this.model = new SlimeFriendEntityModel(loader.getModelPart(EntityModelLayers.SLIME_OUTER));
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
        boolean bl;
        if (livingEntity instanceof SlimeFriendEntity slimeFriendEntity) {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            boolean bl2 = bl = minecraftClient.hasOutline(livingEntity) && (livingEntity).isInvisible();
            if ((livingEntity).isInvisible() && !bl) {
                return;
            }
            VertexConsumer vertexConsumer = bl ? vertexConsumerProvider.getBuffer(RenderLayer.getOutline(this.getTexture(livingEntity))) : vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(this.getTexture(livingEntity)));
            (this.getContextModel()).copyStateTo(this.model);
            this.model.animateModel(livingEntity, f, g, h);
            this.model.setAngles(livingEntity, f, g, j, k, l);
            Color rgb = slimeFriendEntity.getColour();
            this.model.render(matrixStack, vertexConsumer, i, LivingEntityRenderer.getOverlay(livingEntity, 0.0f), rgb.getRed() / 255f, rgb.getGreen() / 255f, rgb.getBlue() / 255f, 1.0f);
        }
    }
}



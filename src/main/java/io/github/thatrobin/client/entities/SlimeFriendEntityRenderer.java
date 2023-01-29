package io.github.thatrobin.client.entities;

import io.github.thatrobin.GoopyGuysMain;
import io.github.thatrobin.entities.SlimeFriendEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.SlimeEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.SlimeOverlayFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SlimeEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class SlimeFriendEntityRenderer extends MobEntityRenderer<SlimeFriendEntity, SlimeFriendEntityModel<SlimeFriendEntity>> {

    private static final Identifier TEXTURE = GoopyGuysMain.identifier("textures/entity/slime/slimefriend.png");

    public SlimeFriendEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new SlimeFriendEntityModel<SlimeFriendEntity>(context.getPart(EntityModelLayers.SLIME)), 0.25f);
        this.addFeature(new SlimeFriendOverlayFeatureRenderer<SlimeFriendEntity>(this, context.getModelLoader()));
    }

    @Override
    public void render(SlimeFriendEntity slimeEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        this.shadowRadius = 0.25f * (float)slimeEntity.getSize();
        super.render(slimeEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    protected void scale(SlimeFriendEntity slimeEntity, MatrixStack matrixStack, float f) {
        float g = 0.999f;
        matrixStack.scale(0.999f, 0.999f, 0.999f);
        matrixStack.translate(0.0f, 0.001f, 0.0f);
        float h = slimeEntity.getSize();
        float i = MathHelper.lerp(f, slimeEntity.lastStretch, slimeEntity.stretch) / (h * 0.5f + 1.0f);
        float j = 1.0f / (i + 1.0f);
        matrixStack.scale(j * h, 1.0f / j * h, j * h);
    }

    @Override
    public Identifier getTexture(SlimeFriendEntity slimeEntity) {
        return TEXTURE;
    }
}
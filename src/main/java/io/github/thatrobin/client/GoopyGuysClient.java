package io.github.thatrobin.client;

import io.github.thatrobin.GoopyGuysMain;
import io.github.thatrobin.client.entities.SlimeFriendEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.VillagerEntity;

public class GoopyGuysClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        EntityRendererRegistry.register(GoopyGuysMain.SLIME_FRIEND, SlimeFriendEntityRenderer::new);
    }

}

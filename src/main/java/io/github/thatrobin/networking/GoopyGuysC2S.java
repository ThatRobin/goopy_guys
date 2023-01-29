package io.github.thatrobin.networking;

import io.github.thatrobin.entities.SlimeFriendEntity;
import io.github.thatrobin.mixin.LivingEntityAccessorMixin;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class GoopyGuysC2S {

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(GoopyGuysPackets.SET_JUMP_STRENGTH, GoopyGuysC2S::setJumpStrength);
    }

    private static void setJumpStrength(MinecraftServer minecraftServer, ServerPlayerEntity serverPlayerEntity, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        UUID uuid = packetByteBuf.readUuid();
        float jumpStrength = packetByteBuf.readFloat();
        Entity entity = serverPlayerEntity.world.getEntityLookup().get(uuid);
        if(entity instanceof SlimeFriendEntity slimeFriendEntity) {
            if(slimeFriendEntity.isOnGround()) {
                slimeFriendEntity.setJumpStrength(jumpStrength);
                Vec3d vec3d = slimeFriendEntity.getVelocity();
                slimeFriendEntity.setVelocity(vec3d.x, slimeFriendEntity.getJumpVelocity(), vec3d.z);
                slimeFriendEntity.setJumpStrength(0.0f);
            }
        }
    }

}

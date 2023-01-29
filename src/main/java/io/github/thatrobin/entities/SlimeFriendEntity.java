package io.github.thatrobin.entities;

import io.github.thatrobin.mixin.LivingEntityAccessorMixin;
import io.github.thatrobin.networking.GoopyGuysPackets;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.render.entity.feature.HorseArmorFeatureRenderer;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SlimeFriendEntity extends SlimeEntity implements Tameable, Mount, JumpingMount, Saddleable {

    private Color DEFAULT_COLOR = new Color(97, 155, 81);
    private Color color = DEFAULT_COLOR;
    protected float jumpStrength;
    protected boolean jumping;
    protected SimpleInventory items;
    private static final TrackedData<Byte> SLIME_FLAGS = DataTracker.registerData(SlimeFriendEntity.class, TrackedDataHandlerRegistry.BYTE);
    protected static final TrackedData<Optional<UUID>> OWNER_UUID = DataTracker.registerData(SlimeFriendEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    protected static final TrackedData<Byte> TAMEABLE_FLAGS = DataTracker.registerData(SlimeFriendEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final int SADDLED_FLAG = 4;

    public SlimeFriendEntity(EntityType<? extends SlimeEntity> entityType, World world) {
        super(entityType, world);
        this.onChestedStatusChanged();

    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimmingGoal(this));
        this.goalSelector.add(3, new RandomLookGoal(this));
        this.goalSelector.add(5, new MoveGoal(this));
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (this.hasPassengers() || this.isBaby()) {
            return super.interactMob(player, hand);
        }
        ItemStack itemStack = player.getStackInHand(hand);
        if (!itemStack.isEmpty()) {
            if(itemStack.getItem() instanceof DyeItem dyeItem) {
                float[] colour = dyeItem.getColor().getColorComponents();
                Color color = new Color(colour[0], colour[1], colour[2]);
                this.setColour(color);
            }
            ActionResult actionResult = itemStack.useOnEntity(player, this, hand);
            if (actionResult.isAccepted()) {
                return actionResult;
            }
        }
        if(this.isSaddled() && this.getSize() >= 1) {
            this.putPlayerOnBack(player);
            return ActionResult.success(this.world.isClient);
        }
        return ActionResult.FAIL;
    }

    @Override
    public double getMountedHeightOffset() {
        return (double)this.getHeight() * 0.80;
    }

    protected void putPlayerOnBack(PlayerEntity player) {
        if (!this.world.isClient) {
            player.setYaw(this.getYaw());
            player.setPitch(this.getPitch());
            player.startRiding(this);
        }
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        Random random = world.getRandom();
        int i = random.nextInt(3);
        if (i < 2 && random.nextFloat() < 0.5f * difficulty.getClampedLocalDifficulty()) {
            ++i;
        }
        int j = 1 << i;
        this.setSize(j, true);
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (this.getFirstPassenger() instanceof LivingEntity livingEntity) {
            if(livingEntity.forwardSpeed > 0.0f) {
                super.travel(new Vec3d(movementInput.x, movementInput.y, livingEntity.forwardSpeed));
            } else {
                super.travel(Vec3d.ZERO);
            }
        } else {
            super.travel(movementInput);
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {

        return damageSource != DamageSource.OUT_OF_WORLD;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(TAMEABLE_FLAGS, (byte)0);
        this.dataTracker.startTracking(SLIME_FLAGS, (byte)0);
        this.dataTracker.startTracking(OWNER_UUID, Optional.empty());
    }

    public void setJumpStrength(float jumpStrength) {
        this.jumpStrength = jumpStrength;
    }

    @Override
    public void setJumpStrength(int strength) {
        if (!this.isSaddled()) {
            return;
        }
        if (strength < 0) {
            strength = 0;
        } else {
            this.jumping = true;
        }
        this.jumpStrength = strength >= 90 ? 1.0f : 0.4f + 0.4f * (float)strength / 90.0f;
        PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());
        packetByteBuf.writeUuid(this.uuid);
        packetByteBuf.writeFloat(this.jumpStrength);
        ClientPlayNetworking.send(GoopyGuysPackets.SET_JUMP_STRENGTH, packetByteBuf);
    }

    @Override
    public float getJumpVelocity() {
        return (0.42f * this.getJumpVelocityMultiplier()) * ((this.jumpStrength * 2) + 1);
    }

    @Override
    public void jump() {
        Vec3d vec3d = this.getVelocity();
        if(!this.hasPassengers() || (this.getFirstPassenger() instanceof LivingEntity livingEntity && livingEntity.forwardSpeed > 0)) {
            this.setVelocity(vec3d.x, this.getJumpVelocity(), vec3d.z);
        }
        this.velocityDirty = true;
    }

    @Override
    public int getTicksUntilNextJump() {
        if (this.getFirstPassenger() instanceof LivingEntity) {
            return 0;
        } else {
            return this.random.nextInt(20) + 10;
        }
    }

    @Override
    public boolean canJump(PlayerEntity player) {
        return this.isSaddled();
    }

    @Override
    public void startJumping(int height) {
        this.jumping = true;
        if (this.makesJumpSound()) {
            this.playSound(this.getJumpSound(), this.getSoundVolume(), this.getJumpSoundPitch());
        }
    }

    @Override
    public void stopJumping() {
    }

    @Override
    public boolean canBeSaddled() {
        return this.isAlive() && this.getSize() >= 1 && this.isTame();
    }

    @Override
    public void saddle(@Nullable SoundCategory sound) {
        this.items.setStack(0, new ItemStack(Items.SADDLE));
        if (sound != null) {
            this.world.playSoundFromEntity(null, this, this.getSaddleSound(), sound, 0.5f, 1.0f);
        }
        this.updateSaddle();
    }

    @Override
    public boolean isSaddled() {
        return this.getSlimeFlag();
    }

    protected void updateSaddle() {
        if (this.world.isClient) {
            return;
        }
        this.setSlimeFlag(!this.items.getStack(0).isEmpty());
    }

    protected boolean getSlimeFlag() {
        return (this.dataTracker.get(SLIME_FLAGS) & SlimeFriendEntity.SADDLED_FLAG) != 0;
    }

    protected void setSlimeFlag(boolean flag) {
        byte b = this.dataTracker.get(SLIME_FLAGS);
        if (flag) {
            this.dataTracker.set(SLIME_FLAGS, (byte)(b | SlimeFriendEntity.SADDLED_FLAG));
        } else {
            this.dataTracker.set(SLIME_FLAGS, (byte)(b & ~SlimeFriendEntity.SADDLED_FLAG));
        }
    }

    public boolean isTame() {
        return (this.dataTracker.get(TAMEABLE_FLAGS) & 4) != 0;
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        UUID uUID;
        super.readCustomDataFromNbt(nbt);
        if (nbt.containsUuid("Owner")) {
            uUID = nbt.getUuid("Owner");
        } else {
            String string = nbt.getString("Owner");
            uUID = ServerConfigHandler.getPlayerUuidByName(this.getServer(), string);
        }
        if (uUID != null) {
            try {
                this.setOwnerUuid(uUID);
                this.setTamed(true);
            }
            catch (Throwable throwable) {
                this.setTamed(false);
            }
        }
        ItemStack itemStack;
        if (nbt.contains("SaddleItem", NbtElement.COMPOUND_TYPE) && (itemStack = ItemStack.fromNbt(nbt.getCompound("SaddleItem"))).isOf(Items.SADDLE)) {
            this.items.setStack(0, itemStack);
        }
        if(nbt.contains("Colour")) {
            this.color = new Color(nbt.getInt("Colour"), false);
        }
        this.updateSaddle();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.getOwnerUuid() != null) {
            nbt.putUuid("Owner", this.getOwnerUuid());
        }
        if (!this.items.getStack(0).isEmpty()) {
            nbt.put("SaddleItem", this.items.getStack(0).writeNbt(new NbtCompound()));
        }
        nbt.putInt("Colour", this.color.getRGB());
    }

    public void setColour(Color colour) {
        if(this.color == DEFAULT_COLOR) {
            this.color = new Color(255,255,255);
            this.addColour(colour);
        } else {
            this.addColour(colour);
        }
    }

    public void addColour(Color colour) {
        this.color = mixColors(this.color, colour, 0.5);
    }

    public Color mixColors(Color color1, Color color2, double percent) {
        double inverse_percent = 1.0 - percent;
        int redPart = (int) (color1.getRed()*percent + color2.getRed()*inverse_percent);
        int greenPart = (int) (color1.getGreen()*percent + color2.getGreen()*inverse_percent);
        int bluePart = (int) (color1.getBlue()*percent + color2.getBlue()*inverse_percent);
        return new Color(redPart, greenPart, bluePart);
    }

    public Color getColour() {
        return this.color;
    }

    @Override
    public boolean makesJumpSound() {
        return this.getSize() > 0;
    }

    @Override
    public float getSoundVolume() {
        return 0.4f * (float)this.getSize();
    }

    @Override
    public SoundEvent getJumpSound() {
        return this.isSmall() ? SoundEvents.ENTITY_SLIME_JUMP_SMALL : SoundEvents.ENTITY_SLIME_JUMP;
    }

    protected void onChestedStatusChanged() {
        this.items = new SimpleInventory(1);
        this.updateSaddle();
    }

    @Override
    @Nullable
    public UUID getOwnerUuid() {
        return this.dataTracker.get(OWNER_UUID).orElse(null);
    }

    public void setOwnerUuid(@Nullable UUID uuid) {
        this.dataTracker.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    public void setOwner(PlayerEntity player) {
        this.setTamed(true);
        this.setOwnerUuid(player.getUuid());
    }

    @Override
    @Nullable
    public LivingEntity getOwner() {
        try {
            UUID uUID = this.getOwnerUuid();
            if (uUID == null) {
                return null;
            }
            return this.world.getPlayerByUuid(uUID);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return null;
        }
    }

    public void setTamed(boolean tamed) {
        byte b = this.dataTracker.get(TAMEABLE_FLAGS);
        if (tamed) {
            this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b | 4));
        } else {
            this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b & 0xFFFFFFFB));
        }
        this.onTamedChanged();
    }

    protected void onTamedChanged() {
    }

    public float getJumpSoundPitch() {
        float f = this.isSmall() ? 1.4f : 0.8f;
        return ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f) * f;
    }

    static class SwimmingGoal
            extends Goal {
        private final SlimeEntity slime;

        public SwimmingGoal(SlimeEntity slime) {
            this.slime = slime;
            this.setControls(EnumSet.of(Goal.Control.JUMP, Goal.Control.MOVE));
            slime.getNavigation().setCanSwim(true);
        }

        @Override
        public boolean canStart() {
            return (this.slime.isTouchingWater() || this.slime.isInLava()) && this.slime.getMoveControl() instanceof SlimeMoveControl;
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if(((LivingEntityAccessorMixin)this.slime).getJumping()) {
                if (this.slime.getRandom().nextFloat() < 0.8f) {
                    this.slime.getJumpControl().setActive();
                }
                ((SlimeMoveControl) this.slime.getMoveControl()).move(1.2);
            }
        }
    }

    static class RandomLookGoal
            extends Goal {
        private final SlimeFriendEntity slime;
        private float targetYaw;
        private int timer;

        public RandomLookGoal(SlimeFriendEntity slime) {
            this.slime = slime;
            this.setControls(EnumSet.of(Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            return this.slime.getTarget() == null && (this.slime.isOnGround() || this.slime.isTouchingWater() || this.slime.isInLava() || this.slime.hasStatusEffect(StatusEffects.LEVITATION)) && this.slime.getMoveControl() instanceof SlimeMoveControl;
        }

        @Override
        public void tick() {
            if(this.slime.getFirstPassenger() instanceof LivingEntity livingEntity) {
                this.targetYaw = livingEntity.getHeadYaw();
            } else {
                if (--this.timer <= 0) {
                    this.timer = this.getTickCount(40 + this.slime.getRandom().nextInt(60));
                    this.targetYaw = this.slime.getRandom().nextInt(360);
                }
            }
            ((SlimeMoveControl)this.slime.getMoveControl()).look(this.targetYaw, false);
        }
    }

    static class MoveGoal
            extends Goal {
        private final SlimeEntity slime;

        public MoveGoal(SlimeEntity slime) {
            this.slime = slime;
            this.setControls(EnumSet.of(Goal.Control.JUMP, Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            return !this.slime.hasVehicle();
        }

        @Override
        public void tick() {
            ((SlimeMoveControl)this.slime.getMoveControl()).move(1.0);
        }
    }
}

package io.github.thatrobin.mixin;

import io.github.thatrobin.GoopyGuysMain;
import io.github.thatrobin.entities.SlimeFriendEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class SlimeEntityMixin {

    @Inject(method = "interactMob", at = @At("HEAD"))
    private void interactMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        MobEntity mobEntity = (MobEntity) (Object) this;
        if(player.getStackInHand(hand).isIn(ItemTags.SMALL_FLOWERS)) {
            if (mobEntity instanceof SlimeEntity slimeEntity) {
                SlimeFriendEntity friendEntity = slimeEntity.convertTo(GoopyGuysMain.SLIME_FRIEND, true);
                if (friendEntity != null) {
                    friendEntity.setSize(slimeEntity.getSize(), true);
                    friendEntity.setOwner(player);
                }
            }
        }
    }
}

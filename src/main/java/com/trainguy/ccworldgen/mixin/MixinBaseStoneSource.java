package com.trainguy.ccworldgen.mixin;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DepthBasedReplacingBaseStoneSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DepthBasedReplacingBaseStoneSource.class)
public class MixinBaseStoneSource {
    @Shadow @Final private BlockState normalBlock;

    @Inject(method = "getBaseBlock", at = @At("HEAD"), cancellable = true)
    private void replaceBaseStoneSource(int i, int j, int k, CallbackInfoReturnable<BlockState> cir){
        cir.setReturnValue(this.normalBlock);
    }
}

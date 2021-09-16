package com.trainguy.ccworldgen.mixin;

import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(OreFeature.class)
public class MixinOreFeature {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void removeAlternateStoneBlobs(FeaturePlaceContext<OreConfiguration> featurePlaceContext, CallbackInfoReturnable<Boolean> cir){
        OreConfiguration oreConfiguration = (OreConfiguration)featurePlaceContext.config();
        if(oreConfiguration.targetStates.get(0).state == Blocks.DIORITE.defaultBlockState() || oreConfiguration.targetStates.get(0).state == Blocks.ANDESITE.defaultBlockState() || oreConfiguration.targetStates.get(0).state == Blocks.TUFF.defaultBlockState() || oreConfiguration.targetStates.get(0).state == Blocks.GRANITE.defaultBlockState() || oreConfiguration.targetStates.get(0).state == Blocks.GRAVEL.defaultBlockState() || oreConfiguration.targetStates.get(0).state == Blocks.DIRT.defaultBlockState()){
            cir.setReturnValue(false);
        }
    }
}

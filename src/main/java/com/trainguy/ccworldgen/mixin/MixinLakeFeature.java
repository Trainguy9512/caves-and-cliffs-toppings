package com.trainguy.ccworldgen.mixin;

import com.trainguy.ccworldgen.util.FastNoiseLite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(LakeFeature.class)
public class MixinLakeFeature {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void overwriteLakeFeatures(FeaturePlaceContext<BlockStateConfiguration> featurePlaceContext, CallbackInfoReturnable<Boolean> cir){
        BlockPos blockPos = featurePlaceContext.origin();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        Random random = featurePlaceContext.random();

        // Get the center pos
        blockPos = new BlockPos(Mth.floor(blockPos.getX() / 16F) * 16 + 8, blockPos.getY(), Mth.floor(blockPos.getZ() / 16F) * 16 + 8);

        BlockStateConfiguration blockStateConfiguration;
        for(blockStateConfiguration = (BlockStateConfiguration)featurePlaceContext.config(); blockPos.getY() > worldGenLevel.getMinBuildHeight() + 5 && worldGenLevel.isEmptyBlock(blockPos); blockPos = blockPos.below()) {
        }

        FastNoiseLite shapeNoise = new FastNoiseLite(random.nextInt(400));
        shapeNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        shapeNoise.SetFrequency(0.08F);
        shapeNoise.SetFractalOctaves(1);

        for(int y = -8; y <= 8; y++){
            for(int x = -8; x <= 8; x++){
                for(int z = -8; z <= 8; z++){
                    BlockPos currentPos = blockPos.offset(x, y * 2F, z);
                    BlockPos finalPos = blockPos.offset(x, y, z);
                    float sampledShapeNoise = shapeNoise.GetNoise(x, y, z) * 0.5F + 0.5F;
                    float distanceFromCenterWater = (float) Math.sqrt(currentPos.distSqr(blockPos));
                    float distanceFromCenter = (float) Math.sqrt(finalPos.distSqr(blockPos));

                    if(distanceFromCenter <= 8 - sampledShapeNoise * 4){
                        boolean isConditional;
                        BlockState placeState;
                        if(y > 0){
                            placeState = Blocks.AIR.defaultBlockState();
                            isConditional = false;
                        } else if(distanceFromCenterWater < 5 - sampledShapeNoise * 4){
                            placeState = featurePlaceContext.config().state;
                            isConditional = false;
                        } else if(y != 0 || distanceFromCenter <= 11 - sampledShapeNoise * 4) {
                            placeState = Blocks.STONE.defaultBlockState();
                            isConditional = true;
                        } else {
                            placeState = Blocks.AIR.defaultBlockState();
                            isConditional = true;
                        }
                        conditionalSetBlock(worldGenLevel, finalPos, placeState, isConditional);
                    }
                }
            }
        }

        cir.setReturnValue(true);
    }

    void conditionalSetBlock(WorldGenLevel worldGenLevel, BlockPos blockPos, BlockState blockState, boolean onlyPlaceInAir){
        if(!onlyPlaceInAir || !worldGenLevel.getBlockState(blockPos).getMaterial().isSolid()){
            worldGenLevel.setBlock(blockPos, blockState, 2);
        }
    }
}

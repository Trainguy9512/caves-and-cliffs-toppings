package com.trainguy.ccworldgen.feature;

import com.mojang.serialization.Codec;
import com.trainguy.ccworldgen.util.FastNoiseLite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.Random;

public class MountainGlacierFeature extends Feature<NoneFeatureConfiguration> {
    public MountainGlacierFeature(Codec<NoneFeatureConfiguration> codec) { super(codec); }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
        Random random = featurePlaceContext.random();
        BlockPos originPos = featurePlaceContext.origin();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        System.out.println("Placed at " + originPos);

        originPos = new BlockPos(originPos.getX(), worldGenLevel.getHeight(Heightmap.Types.WORLD_SURFACE_WG, originPos.getX(), originPos.getZ()) - 4, originPos.getZ());

        FastNoiseLite offsetNoise = new FastNoiseLite();
        offsetNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        offsetNoise.SetFrequency(0.15F);

        int radius = 8;
        for(int x = -radius; x <= radius; x++){
            for(int y = -radius; y <= radius; y++){
                for(int z = -radius; z <= radius; z++){
                    BlockPos currentPos = originPos.offset(x, y, z);
                    double distanceToCenter = Math.sqrt(originPos.distSqr(currentPos));
                    float sampledOffsetNoise = offsetNoise.GetNoise(currentPos.getX(), currentPos.getY() / 3F, currentPos.getZ()) * 0.5F + 0.5F;

                    if(distanceToCenter <= radius - sampledOffsetNoise * 4){
                        setBlockReplaceAir(worldGenLevel, currentPos, Blocks.BLUE_ICE.defaultBlockState());
                    }
                }
            }
        }
        for(int x = -radius; x <= radius; x++){
            for(int z = -radius; z <= radius; z++){
                boolean hasHitIce = false;
                for(int y = radius; y >= -radius; y--){
                    BlockPos currentPos = originPos.offset(x, y, z);
                    if(worldGenLevel.getBlockState(currentPos) == Blocks.BLUE_ICE.defaultBlockState() && !hasHitIce){
                        int randomDepth = random.nextInt(3);
                        for(int y2 = 0; y2 >= -randomDepth; y2--){
                            worldGenLevel.setBlock(currentPos.offset(0, y2, 0), Blocks.ICE.defaultBlockState(), 2);
                        }
                        hasHitIce = true;
                    }
                }
            }
        }
        return true;
    }

    private void setBlockReplaceAir(WorldGenLevel worldGenLevel, BlockPos blockPos, BlockState blockState){
        if(worldGenLevel.getBlockState(blockPos).isAir()){
            worldGenLevel.setBlock(blockPos, blockState, 2);
        }
    }
}

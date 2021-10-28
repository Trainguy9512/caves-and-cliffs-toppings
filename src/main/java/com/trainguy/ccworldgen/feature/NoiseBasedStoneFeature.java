package com.trainguy.ccworldgen.feature;

import com.ibm.icu.impl.UCharacterUtility;
import com.mojang.serialization.Codec;
import com.trainguy.ccworldgen.util.FastNoiseLite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.lwjgl.system.CallbackI;
import org.spongepowered.asm.mixin.Unique;

import java.util.Random;

public class NoiseBasedStoneFeature extends Feature<NoneFeatureConfiguration> {

    private static final BlockState STONE = Blocks.STONE.defaultBlockState();
    private static final BlockState DEEPSLATE = Blocks.DEEPSLATE.defaultBlockState();
    private static final BlockState CLAY = Blocks.CLAY.defaultBlockState();
    private static final float BASE_FREQUENCY = 0.02F;
    private FastNoiseLite testRockNoise;
    private FastNoiseLite domainWarpNoise;

    public NoiseBasedStoneFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
        Random random = featurePlaceContext.random();
        BlockPos originPos = featurePlaceContext.origin();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        ChunkGenerator chunkGenerator = featurePlaceContext.chunkGenerator();

        testRockNoise = new FastNoiseLite(9512);
        testRockNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        testRockNoise.SetRotationType3D(FastNoiseLite.RotationType3D.ImproveXZPlanes);
        testRockNoise.SetFrequency(BASE_FREQUENCY);
        testRockNoise.SetFractalOctaves(2);
        testRockNoise.SetFractalType(FastNoiseLite.FractalType.Ridged);


        domainWarpNoise = new FastNoiseLite(95121);
        domainWarpNoise.SetDomainWarpType(FastNoiseLite.DomainWarpType.OpenSimplex2);
        domainWarpNoise.SetRotationType3D(FastNoiseLite.RotationType3D.ImproveXZPlanes);
        domainWarpNoise.SetFrequency(BASE_FREQUENCY / 3F);
        domainWarpNoise.SetDomainWarpAmp(1.0F);
        domainWarpNoise.SetFractalOctaves(1);

        FastNoiseLite.Vector3 warpCoord = new FastNoiseLite.Vector3(0, 0, 0);

        //Iterate over every block
        for(int x = (int) (Math.floor(originPos.getX() / 16F) * 16); x < (int) (Math.floor(originPos.getX() / 16F) * 16) + 16; x++){
            for(int z = (int) (Math.floor(originPos.getZ() / 16F) * 16); z < (int) (Math.floor(originPos.getZ() / 16F) * 16) + 16; z++){
                for(int y = chunkGenerator.getMinY(); y < chunkGenerator.getGenDepth() + chunkGenerator.getMinY(); y++){
                    BlockPos currentPos = new BlockPos(x, y, z);

                    warpCoord.x = x;
                    warpCoord.y = y;
                    warpCoord.z = z;
                    domainWarpNoise.DomainWarp(warpCoord);

                    float testGradient = Mth.clamp(y, -32, 32) / 32F;
                    float testNoiseValue = testRockNoise.GetNoise(warpCoord.x, warpCoord.y, warpCoord.z) + testGradient;
                    BlockState blockState = testNoiseValue > 0 ? STONE : DEEPSLATE;
                    blockState = worldGenLevel.getBiome(currentPos).getBiomeCategory() == Biome.BiomeCategory.UNDERGROUND ? CLAY : blockState;

                    setBlockReplace(worldGenLevel, currentPos, STONE, DEEPSLATE);
                    setBlockReplace(worldGenLevel, currentPos, blockState, STONE);
                    if(z < 0){
                        worldGenLevel.setBlock(currentPos, Blocks.BARRIER.defaultBlockState(), 2);
                    }
                }
            }
        }
        return false;
    }

    private void setBlockReplace(WorldGenLevel worldGenLevel, BlockPos blockPos, BlockState blockState, BlockState replaceState){
        if(worldGenLevel.getBlockState(blockPos) == replaceState){
            worldGenLevel.setBlock(blockPos, blockState, 2);
        }
    }
}

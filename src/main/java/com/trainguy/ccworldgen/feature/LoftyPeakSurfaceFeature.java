package com.trainguy.ccworldgen.feature;

import com.mojang.serialization.Codec;
import com.trainguy.ccworldgen.util.FastNoiseLite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.lwjgl.system.CallbackI;

import java.util.Random;

public class LoftyPeakSurfaceFeature extends Feature<NoneFeatureConfiguration> {
    public LoftyPeakSurfaceFeature(Codec<NoneFeatureConfiguration> codec) { super(codec); }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
        Random random = featurePlaceContext.random();
        BlockPos originPos = featurePlaceContext.origin();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        System.out.println("Placed at " + originPos);

        for(int x = (int) (Math.floor(originPos.getX() / 16F) * 16); x < (int) (Math.floor(originPos.getX() / 16F) * 16) + 16; x++){
            for(int z = (int) (Math.floor(originPos.getZ() / 16F) * 16); z < (int) (Math.floor(originPos.getZ() / 16F) * 16) + 16; z++){
                int heightAtColumn = worldGenLevel.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                if(heightAtColumn > 64){
                    boolean isSteepTerrain = isSteepTerrain(worldGenLevel, x, z, true, false, true, false);
                    if(!isSteepTerrain){
                        BlockPos currentPos = new BlockPos(x, heightAtColumn, z);
                        worldGenLevel.setBlock(currentPos.offset(0, -2, 0), Blocks.SNOW_BLOCK.defaultBlockState(), 2);
                        worldGenLevel.setBlock(currentPos.offset(0, -1, 0), Blocks.SNOW_BLOCK.defaultBlockState(), 2);
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

    public boolean isSteepTerrain(WorldGenLevel chunkAccess, int i, int j, boolean hasNorthSlopes, boolean hasSouthSlopes, boolean hasEastSlopes, boolean hasWestSlopes) {

        int m = i;
        int n = j;
        int t;
        int u;
        int v;
        int w;
        int x;
        if (hasNorthSlopes || hasSouthSlopes) {
            t = n - 1;
            u = n + 1;
            v = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, m, t);
            w = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, m, u);
            x = v - w;
            if (hasSouthSlopes && x > 3) {
                return true;
            }

            if (hasNorthSlopes && -x > 3) {
                return true;
            }
        }

        if (hasEastSlopes && hasWestSlopes) {
            return false;
        } else {
            t = m - 1;
            u = m + 1;
            v = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, t, n);
            w = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, u, n);
            x = v - w;
            if (hasEastSlopes && x > 3) {
                return true;
            } else {
                return hasWestSlopes && -x > 3;
            }
        }
    }
}

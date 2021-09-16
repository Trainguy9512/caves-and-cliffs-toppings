package com.trainguy.ccworldgen.mixin;

import com.trainguy.ccworldgen.util.FastNoiseLite;
import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.SurfaceBuilders;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.surfacebuilders.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.Random;

@Mixin(ConfiguredSurfaceBuilder.class)
public class MixinConfiguredSurfaceBuilder<SC extends SurfaceBuilderConfiguration> {
    @Shadow @Final public SurfaceBuilder<SC> surfaceBuilder;
    @Shadow @Final public SC config;

    private long fastNoiseSeed;
    private FastNoiseLite undergroundStoneRidgedNoise;
    private FastNoiseLite undergroundStoneCellNoiseDist;
    private FastNoiseLite undergroundStoneCellNoiseValue;
    private FastNoiseLite deepslateLayerOffsetNoise;

    @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
    private void addNoiseBasedStone(Random random, ChunkAccess chunkAccess, Biome biome, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, int l, int m, long n, CallbackInfo ci){
        initFastNoise(n);
        this.surfaceBuilder.apply(random, chunkAccess, biome, i, j, k, d, blockState, blockState2, l, m, n, this.config);

        // Get the surface height and use it in the deepslate layer, but make sure it can go no lower than 64- if you don't do this, then it kinda breaks in huge cave entrances where skylight goes down to y0
        int surfaceHeight = Math.max(chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, i & 15, j & 15), 64);
        int deepslateHeight = (int) (surfaceHeight * 0.5 - 32);

        // Ieterate over every single block in the column
        for(int y = k; y > chunkAccess.getMinBuildHeight(); y--){
            BlockPos blockPos = new BlockPos(i, y, j);

            // Get whether the block is in deepslate or the deepslate transition using two layers of noise
            boolean isInDeepslate = y < deepslateHeight + deepslateLayerOffsetNoise.GetNoise(i, j) * 3;
            boolean isInDeepslateTransition = y < (deepslateHeight + deepslateLayerOffsetNoise.GetNoise(i, j) * 3) + 3 + (deepslateLayerOffsetNoise.GetNoise(j, i) * 2);

            if(chunkAccess.getBlockState(blockPos) == Blocks.TUFF.defaultBlockState() && !isInDeepslateTransition){
                chunkAccess.setBlockState(blockPos, Blocks.ANDESITE.defaultBlockState(), false);
            }
            if(chunkAccess.getBlockState(blockPos) == Blocks.DEEPSLATE_IRON_ORE.defaultBlockState() && !isInDeepslateTransition){
                chunkAccess.setBlockState(blockPos, Blocks.IRON_ORE.defaultBlockState(), false);
            }
            if(chunkAccess.getBlockState(blockPos) == Blocks.GRANITE.defaultBlockState() && isInDeepslateTransition){
                chunkAccess.setBlockState(blockPos, Blocks.TUFF.defaultBlockState(), false);
            }
            if(chunkAccess.getBlockState(blockPos) == Blocks.COPPER_ORE.defaultBlockState() && isInDeepslateTransition){
                chunkAccess.setBlockState(blockPos, Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState(), false);
            }

            if(biome.getBiomeCategory() == Biome.BiomeCategory.MOUNTAIN && chunkAccess.getBlockState(blockPos) == Blocks.STONE.defaultBlockState() && config.getTopMaterial() == Blocks.STONE.defaultBlockState() && y > surfaceHeight - 8){
                float noiseValue = undergroundStoneRidgedNoise.GetNoise(i * 0.9F, 0, j * 0.9F);
                BlockState stripeStoneState;

                if(noiseValue > 0.6){
                    stripeStoneState = Blocks.CALCITE.defaultBlockState();
                } else {
                    stripeStoneState = Blocks.STONE.defaultBlockState();
                }
                chunkAccess.setBlockState(blockPos, stripeStoneState, false);
            }

            // Only apply this to stone blocks 16 blocks below the surface, to keep it underground
            if((chunkAccess.getBlockState(blockPos) == Blocks.STONE.defaultBlockState() || chunkAccess.getBlockState(blockPos) == Blocks.BARRIER.defaultBlockState()) && y < surfaceHeight - 16){
                float noiseValue = (Math.max(undergroundStoneRidgedNoise.GetNoise(i, y * (isInDeepslate ? 3 : 5), j) + 0.05F, undergroundStoneRidgedNoise.GetNoise(i, (y + 1) * (isInDeepslate ? 3 : 5), j)) * 0.5F + 0.5F);
                // If this block is in the stone layer, add a space between different stone types to give areas of uniform stone inbetween
                noiseValue *= isInDeepslate ? 1 : 1 - (undergroundStoneCellNoiseDist.GetNoise(i, y, j) * 0.5F + 0.4F);
                // Get cell noise of the same seed/frequency as the noiseStripMultiplier, so each spaced out region has a different material
                float cellNoiseValue = undergroundStoneCellNoiseValue.GetNoise(i, y, j);

                // Initialize the blockstate variable
                BlockState alternateStoneState;
                if(isInDeepslate){
                    // Use uniform stripes of tuff in deepslate
                    if(noiseValue > 0.9){
                        alternateStoneState = Blocks.TUFF.defaultBlockState();
                    } else {
                        alternateStoneState = chunkAccess.getBlockState(blockPos) == Blocks.BARRIER.defaultBlockState() ? Blocks.BARRIER.defaultBlockState() : Blocks.DEEPSLATE.defaultBlockState();
                    }
                } else if(isInDeepslateTransition){
                    alternateStoneState = Blocks.TUFF.defaultBlockState();
                } else {
                    // In stone, make andesite, diorite, and granite have an equal chance of occuring + a chance of it just being stone with no alternate
                    noiseValue += noiseValue < 0.75 ? random.nextFloat() * 0.05 : 0;
                    if(noiseValue > 0.85){
                        if(cellNoiseValue < -0.5F){
                            alternateStoneState = Blocks.ANDESITE.defaultBlockState();
                        } else if(cellNoiseValue < 0){
                            alternateStoneState = Blocks.DIORITE.defaultBlockState();
                        } else if(cellNoiseValue < 0.5){
                            alternateStoneState = Blocks.GRANITE.defaultBlockState();
                        } else {
                            alternateStoneState = Blocks.STONE.defaultBlockState();
                        }
                    } else {
                        // if it's a barrier block, place barriers, else just place stone normally (this doesn't need to be removed for the final mod)
                        alternateStoneState = chunkAccess.getBlockState(blockPos) == Blocks.BARRIER.defaultBlockState() ? Blocks.BARRIER.defaultBlockState() : Blocks.STONE.defaultBlockState();
                    }
                }

                // Gets multi ridged noise for underground sediments by taking two of the same noise and adjusting the parameters of each and multiplying them together, giving a nice web-like effect similar to geodes- this prevents striping in sediments
                float sedimentNoiseValue = (float) ((undergroundStoneRidgedNoise.GetNoise(-i * 1.1F, -y * 1.1F, -j * 1.1F) * 0.5F + 0.5F) * Math.pow(undergroundStoneRidgedNoise.GetNoise(i * 1.5F, y * 1.5F, j * 1.5F) * 0.5F + 0.5F, 3));
                if(sedimentNoiseValue > 0.7){
                    float sedimentTypeCellValue = undergroundStoneCellNoiseValue.GetNoise(i * 1.5F, y * 1.5F, j * 1.5F) * 0.5F + 0.5F;
                    // Place sediments as dirt when 16 blocks below the surface
                    if(y > surfaceHeight - 16){
                        alternateStoneState = Blocks.DIRT.defaultBlockState();
                    } else {
                        // Based off the cell noise value, place gravel, dirt, or coarse dirt above deepslate or just gravel below deepslate. Also whenever placing gravel, check that there is a solid block below it or else don't place gravel
                        if(sedimentTypeCellValue < 0.3 || (isInDeepslateTransition && sedimentTypeCellValue < 0.5)){
                            if(chunkAccess.getBlockState(blockPos.offset(0, -1, 0)).getMaterial().isSolid()){
                                alternateStoneState = Blocks.GRAVEL.defaultBlockState();
                            }
                        } else if(sedimentTypeCellValue < 0.5 && !isInDeepslateTransition){
                            alternateStoneState = Blocks.DIRT.defaultBlockState();
                        } else if(sedimentTypeCellValue < 0.8 && !isInDeepslateTransition){
                            alternateStoneState = Blocks.COARSE_DIRT.defaultBlockState();
                        }
                    }
                }
                chunkAccess.setBlockState(blockPos, alternateStoneState, false);
            }
            // Easy way to cut the world in half to see what it looks like on the side, obviously get rid of this for the final mod
            //if(j > -1){
                //chunkAccess.setBlockState(blockPos, Blocks.BARRIER.defaultBlockState(), false);
            //}
            // Adds buffer to ore veins
            if(chunkAccess.getBlockState(blockPos) == Blocks.DIAMOND_BLOCK.defaultBlockState()){
                BlockState oreVeinBufferState = isInDeepslateTransition ? isInDeepslate ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.TUFF.defaultBlockState() : Blocks.STONE.defaultBlockState();
                if(random.nextFloat() < 0.05F){
                    if(y > 0){
                        oreVeinBufferState = isInDeepslateTransition ? Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState() : Blocks.COPPER_ORE.defaultBlockState();
                    } else {
                        oreVeinBufferState = isInDeepslateTransition ? Blocks.DEEPSLATE_IRON_ORE.defaultBlockState() : Blocks.IRON_ORE.defaultBlockState();
                    }
                }
                chunkAccess.setBlockState(blockPos, oreVeinBufferState, false);
            }
        }
        ci.cancel();
    }

    public void initFastNoise(long l) {
        if (this.fastNoiseSeed != l) {
            WorldgenRandom worldgenRandom = new WorldgenRandom(l);
            this.undergroundStoneRidgedNoise = new FastNoiseLite((int) l);
            undergroundStoneRidgedNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            undergroundStoneRidgedNoise.SetFrequency(0.02F);
            undergroundStoneRidgedNoise.SetFractalType(FastNoiseLite.FractalType.Ridged);
            undergroundStoneRidgedNoise.SetFractalOctaves(1);
            this.deepslateLayerOffsetNoise = new FastNoiseLite((int) l);
            deepslateLayerOffsetNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            deepslateLayerOffsetNoise.SetFrequency(0.02F);
            deepslateLayerOffsetNoise.SetFractalOctaves(1);
            this.undergroundStoneCellNoiseDist = new FastNoiseLite((int) l);
            undergroundStoneCellNoiseDist.SetNoiseType(FastNoiseLite.NoiseType.Cellular);
            undergroundStoneCellNoiseDist.SetCellularReturnType(FastNoiseLite.CellularReturnType.Distance2Div);
            undergroundStoneCellNoiseDist.SetFrequency(0.003F);
            undergroundStoneCellNoiseDist.SetFractalOctaves(1);
            this.undergroundStoneCellNoiseValue = new FastNoiseLite((int) l);
            undergroundStoneCellNoiseValue.SetNoiseType(FastNoiseLite.NoiseType.Cellular);
            undergroundStoneCellNoiseValue.SetCellularReturnType(FastNoiseLite.CellularReturnType.CellValue);
            undergroundStoneCellNoiseValue.SetFrequency(0.003F);
            undergroundStoneCellNoiseValue.SetFractalOctaves(1);
        }

        this.fastNoiseSeed = l;
    }
}

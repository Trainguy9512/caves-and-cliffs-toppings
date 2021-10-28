package com.trainguy.ccworldgen.mixin;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.MineShaftPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import org.lwjgl.system.CallbackI;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;
import java.util.Set;

@Mixin(MineShaftPieces.MineShaftCorridor.class)
public abstract class MixinMineshaftCorridor extends StructurePiece {

    private static final Set<Block> surfaceLedgeBlocks = ImmutableSet.of(
            Blocks.STONE,
            Blocks.GRANITE,
            Blocks.DIORITE,
            Blocks.ANDESITE,
            Blocks.DIRT,
            Blocks.COARSE_DIRT,
            Blocks.PODZOL,
            Blocks.GRASS_BLOCK,
            Blocks.TERRACOTTA,
            Blocks.WHITE_TERRACOTTA,
            Blocks.ORANGE_TERRACOTTA,
            Blocks.MAGENTA_TERRACOTTA,
            Blocks.LIGHT_BLUE_TERRACOTTA,
            Blocks.YELLOW_TERRACOTTA,
            Blocks.LIME_TERRACOTTA,
            Blocks.PINK_TERRACOTTA,
            Blocks.GRAY_TERRACOTTA,
            Blocks.LIGHT_GRAY_TERRACOTTA,
            Blocks.CYAN_TERRACOTTA,
            Blocks.PURPLE_TERRACOTTA,
            Blocks.BLUE_TERRACOTTA,
            Blocks.BROWN_TERRACOTTA,
            Blocks.GREEN_TERRACOTTA,
            Blocks.RED_TERRACOTTA,
            Blocks.BLACK_TERRACOTTA,
            Blocks.SANDSTONE,
            Blocks.RED_SANDSTONE,
            Blocks.MYCELIUM,
            Blocks.SNOW,
            Blocks.PACKED_ICE,
            Blocks.DEEPSLATE,
            Blocks.CALCITE,
            Blocks.SAND,
            Blocks.RED_SAND,
            Blocks.GRAVEL,
            Blocks.TUFF,
            Blocks.GRANITE,
            Blocks.IRON_ORE,
            Blocks.DEEPSLATE_IRON_ORE,
            Blocks.RAW_IRON_BLOCK,
            Blocks.COPPER_ORE,
            Blocks.DEEPSLATE_COPPER_ORE,
            Blocks.RAW_COPPER_BLOCK);

    @Shadow @Final private int numSections;
    protected MixinMineshaftCorridor(StructurePieceType structurePieceType, int i, BoundingBox boundingBox) {
        super(structurePieceType, i, boundingBox);
    }

    private void setBlock(WorldGenLevel worldGenLevel, BoundingBox boundingBox, BlockState blockState, int x, int y, int z, boolean shouldUpdate, boolean placeOnlyInAir){
        BlockPos worldPos = this.getWorldPos(x, y, z);
        if(!placeOnlyInAir || worldGenLevel.getBlockState(worldPos).isAir()){
            this.placeBlock(worldGenLevel, blockState, x, y, z, boundingBox);
            if(shouldUpdate){
                worldGenLevel.getChunk(worldPos).markPosForPostprocessing(worldPos);
            }
        }
    }

    private void setFilledBox(WorldGenLevel worldGenLevel, BoundingBox boundingBox, BlockState blockState, int x1, int y1, int z1, int x2, int y2, int z2, boolean shouldUpdate, boolean placeOnlyInAir){
        for(int x = x1; x <= x2; x++){
            for(int y = y1; y <= y2; y++){
                for(int z = z1; z <= z2; z++){
                    BlockPos currentPos = new BlockPos(x, y, z);
                    setBlock(worldGenLevel, boundingBox, blockState, x, y, z, shouldUpdate, placeOnlyInAir);
                }
            }
        }
    }

    private void setFilledBoxUnderHeightmap(WorldGenLevel worldGenLevel, BoundingBox boundingBox, BlockState blockState, int x1, int y1, int z1, int x2, int y2, int z2, boolean shouldUpdate, boolean placeOnlyInAir, int blocksAboveHeightmap){
        for(int x = x1; x <= x2; x++){
            for(int z = z1; z <= z2; z++){
                BlockPos columnWorldPos = this.getWorldPos(x, 0, z);
                int columnHeight = worldGenLevel.getHeight(Heightmap.Types.WORLD_SURFACE_WG, columnWorldPos.getX(), columnWorldPos.getZ());
                for(int y = y1; y <= y2; y++){
                    BlockPos currentPos = this.getWorldPos(x, y, z);
                    if(currentPos.getY() - blocksAboveHeightmap <= columnHeight){
                        setBlock(worldGenLevel, boundingBox, blockState, x, y, z, shouldUpdate, placeOnlyInAir);
                    }
                }
            }
        }
    }

    private void setFilledBoxOverLedge(WorldGenLevel worldGenLevel, BoundingBox boundingBox, BlockState blockState, int x1, int y1, int z1, int x2, int y2, int z2, boolean shouldUpdate, boolean placeOnlyInAir, int blocksOverEdge){
        for(int x = x1; x <= x2; x++){
            for(int y = y1; y <= y2; y++){
                for(int z = z1; z <= z2; z++){
                    boolean isNearEdgeBlock = false;
                    BlockPos edgeCheckPos = this.getWorldPos(x, y, z);
                    if(surfaceLedgeBlocks.contains(worldGenLevel.getBlockState(edgeCheckPos).getBlock())){
                        isNearEdgeBlock = true;
                    } else {
                        for(int k = -blocksOverEdge; k <= blocksOverEdge; k++){
                            edgeCheckPos = this.getWorldPos(x, y, z + k);
                            if(surfaceLedgeBlocks.contains(worldGenLevel.getBlockState(edgeCheckPos).getBlock())){
                                isNearEdgeBlock = true;
                            }
                        }
                    }
                    if(isNearEdgeBlock){
                        setBlock(worldGenLevel, boundingBox, blockState, x, y, z, shouldUpdate, placeOnlyInAir);
                    }
                }
            }
        }
    }

    /**
     * @author Trainguy
     */
    @Overwrite
    public void postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
        BlockState airState = Blocks.AIR.defaultBlockState();
        BlockState plankState = Blocks.OAK_PLANKS.defaultBlockState();

        int corridorLength = this.numSections * 5 - 1;
        setFilledBox(worldGenLevel, boundingBox, airState, 0, 0, 0, 2, 2, corridorLength, false, false);
        setFilledBoxOverLedge(worldGenLevel, boundingBox, plankState, 0, -1, 0, 2, -1, corridorLength, false, true, 2);
    }
}

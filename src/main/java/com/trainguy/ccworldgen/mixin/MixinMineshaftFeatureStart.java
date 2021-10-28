package com.trainguy.ccworldgen.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.IglooFeature;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.MineShaftPieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.Inject;

import java.util.function.Predicate;

@Mixin(MineshaftFeature.class)
public class MixinMineshaftFeatureStart extends StructureFeature<MineshaftConfiguration> {


    public MixinMineshaftFeatureStart(Codec<MineshaftConfiguration> codec, PieceGenerator<MineshaftConfiguration> pieceGenerator) {
        super(codec, pieceGenerator);
    }

    /**
     * @author James Pelter (Trainguy)
     */
    @Overwrite
    private static void generatePieces(StructurePiecesBuilder structurePiecesBuilder, MineshaftConfiguration mineshaftConfiguration, PieceGenerator.Context context) {
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        ChunkPos chunkPos = context.chunkPos();
        Predicate<Biome> predicate = context.validBiome();
        if (predicate.test(chunkGenerator.getNoiseBiome(QuartPos.fromBlock(chunkPos.getMiddleBlockX()), QuartPos.fromBlock(50), QuartPos.fromBlock(chunkPos.getMiddleBlockZ())))) {
            MineShaftPieces.MineShaftRoom mineShaftRoom = new MineShaftPieces.MineShaftRoom(0, context.random(), chunkPos.getBlockX(2), chunkPos.getBlockZ(2), mineshaftConfiguration.type);
            structurePiecesBuilder.addPiece(mineShaftRoom);
            mineShaftRoom.addChildren(mineShaftRoom, structurePiecesBuilder, context.random());
            if (mineshaftConfiguration.type == MineshaftFeature.Type.MESA) {
                BoundingBox boundingBox = structurePiecesBuilder.getBoundingBox();
                int j = chunkGenerator.getSeaLevel() - boundingBox.maxY() + boundingBox.getYSpan() / 2 - -5;
                structurePiecesBuilder.offsetPiecesVertically(0);
            } else {
                boolean isMountainMineshaft;
                int sampledHeight = chunkGenerator.getBaseHeight((int) Mth.lerp(0.5F, structurePiecesBuilder.getBoundingBox().minX(), structurePiecesBuilder.getBoundingBox().maxX()), (int) Mth.lerp(0.5F, structurePiecesBuilder.getBoundingBox().minZ(), structurePiecesBuilder.getBoundingBox().maxZ()), Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor());
                if(sampledHeight > 140){
                    isMountainMineshaft = true;
                    System.out.println("Structure height: " + structurePiecesBuilder.getBoundingBox().minY() + ", Surface height: " + sampledHeight + ", /tp @p " + Mth.lerp(0.5F, structurePiecesBuilder.getBoundingBox().minX(), structurePiecesBuilder.getBoundingBox().maxX()) + " ~ " + Mth.lerp(0.5F, structurePiecesBuilder.getBoundingBox().minZ(), structurePiecesBuilder.getBoundingBox().maxZ()));
                    structurePiecesBuilder.offsetPiecesVertically(sampledHeight - structurePiecesBuilder.getBoundingBox().maxY());
                } else {
                    structurePiecesBuilder.moveBelowSeaLevel(chunkGenerator.getSeaLevel(), chunkGenerator.getMinY(), context.random(), 10);
                }
            }
        }
    }
}

package com.trainguy.ccworldgen.mixin;

import com.trainguy.ccworldgen.TrainguysCCWorldgen;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.placement.CavePlacements;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BiomeDefaultFeatures.class)

public class MixinBiomeDefaultFeatures {
    @Inject(method = "addDefaultUndergroundVariety", at = @At("HEAD"), cancellable = true)
    private static void overwriteUndergroundVariety(BiomeGenerationSettings.Builder builder, CallbackInfo ci){
        builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.GLOW_LICHEN);
        builder.addFeature(GenerationStep.Decoration.RAW_GENERATION, TrainguysCCWorldgen.PLACED_TEST_NOISE_STONE);
        builder.addFeature(GenerationStep.Decoration.RAW_GENERATION, TrainguysCCWorldgen.PLACED_TEST_LUSH_NOISE_STONE);
        ci.cancel();
    }

    @Inject(method = "addLushCavesSpecialOres", at = @At("HEAD"), cancellable = true)
    private static void overwriteLushCavesClay(BiomeGenerationSettings.Builder builder, CallbackInfo ci){
        ci.cancel();
    }
}

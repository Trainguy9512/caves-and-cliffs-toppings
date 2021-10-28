package com.trainguy.ccworldgen;

import com.trainguy.ccworldgen.feature.LoftyPeakSurfaceFeature;
import com.trainguy.ccworldgen.feature.MountainGlacierFeature;
import com.trainguy.ccworldgen.feature.NoiseBasedStoneFeature;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.impl.biome.modification.BuiltInRegistryKeys;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.Features;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringDecomposer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.HeightmapConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import org.apache.http.config.RegistryBuilder;
import org.intellij.lang.annotations.Identifier;

public class TrainguysCCWorldgen implements ModInitializer {

	public static final String MOD_ID = "cavetweaks";

	public static final Feature<NoneFeatureConfiguration> NOISE_BASED_STONE = new NoiseBasedStoneFeature(NoneFeatureConfiguration.CODEC);
	public static final ConfiguredFeature<?, ?> CONFIGURED_NOISE_BASED_STONE = NOISE_BASED_STONE.configured(new NoneFeatureConfiguration()).squared();

	@Override
	public void onInitialize() {
		// Register features
		Registry.register(Registry.FEATURE, new ResourceLocation(MOD_ID, "noise_based_stone"), NOISE_BASED_STONE);

		// Register configured features
		ResourceKey<ConfiguredFeature<?, ?>> configuredNoiseBasedStone = ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY, new ResourceLocation(MOD_ID, "configured_noise_based_stone"));
		Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, configuredNoiseBasedStone.location(), CONFIGURED_NOISE_BASED_STONE);
	}
}

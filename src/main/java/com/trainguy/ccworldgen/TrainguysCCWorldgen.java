package com.trainguy.ccworldgen;

import com.trainguy.ccworldgen.feature.NoiseBasedStoneFeature;
import com.trainguy.ccworldgen.util.StoneEntry;
import com.trainguy.ccworldgen.util.StonePattern;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.CavePlacements;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

public class TrainguysCCWorldgen implements ModInitializer {

	public static final String MOD_ID = "cavetweaks";
	public static final Feature<NoneFeatureConfiguration> TEST_NOISE_STONE = NoiseBasedStoneFeature.featureWithDeepslateUnfiltered(NoneFeatureConfiguration.CODEC,
			List.of(
					new StoneEntry(Blocks.GRANITE, Blocks.STONE, StonePattern.sedimentaryStone()),
					new StoneEntry(Blocks.STONE, Blocks.DIORITE, StonePattern.sedimentaryStone()),
					new StoneEntry(Blocks.ANDESITE, Blocks.STONE, StonePattern.sedimentaryStone())
			),
			List.of(
					new StoneEntry(Blocks.TUFF, Blocks.DEEPSLATE, StonePattern.sedimentaryStone()),
					new StoneEntry(Blocks.TUFF, Blocks.DEEPSLATE, StonePattern.sedimentaryStone()),
					new StoneEntry(Blocks.TUFF, Blocks.DEEPSLATE, StonePattern.sedimentaryStone())
			),
			0.25F
	);
	public static final Feature<NoneFeatureConfiguration> TEST_LUSH_NOISE_STONE = NoiseBasedStoneFeature.featureWithDeepslateFiltered(NoneFeatureConfiguration.CODEC,
			List.of(
					new StoneEntry(Blocks.CLAY, Blocks.CALCITE, StonePattern.dottedClay())
			),
			List.of(
					new StoneEntry(Blocks.TUFF, Blocks.CLAY, StonePattern.dottedClay())
			),
			List.of(Biomes.LUSH_CAVES),
			0F
	);
	public static final PlacedFeature PLACED_TEST_NOISE_STONE = TEST_NOISE_STONE.configured(FeatureConfiguration.NONE).placed(InSquarePlacement.spread());
	public static final PlacedFeature PLACED_TEST_LUSH_NOISE_STONE = TEST_LUSH_NOISE_STONE.configured(FeatureConfiguration.NONE).placed(InSquarePlacement.spread());

	@Override
	public void onInitialize() {
		registerFeatures();
	}

	public void registerFeatures(){
		Registry.register(Registry.FEATURE, new ResourceLocation(MOD_ID, "noise_based_stone_test"), TEST_NOISE_STONE);
		Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new ResourceLocation(MOD_ID, "noise_based_stone_test"), TEST_NOISE_STONE.configured(FeatureConfiguration.NONE));
		Registry.register(BuiltinRegistries.PLACED_FEATURE, new ResourceLocation(MOD_ID, "noise_based_stone_test"), PLACED_TEST_NOISE_STONE);

		Registry.register(Registry.FEATURE, new ResourceLocation(MOD_ID, "test_lush_noise_stone"), TEST_LUSH_NOISE_STONE);
		Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new ResourceLocation(MOD_ID, "test_lush_noise_stone"), TEST_LUSH_NOISE_STONE.configured(FeatureConfiguration.NONE));
		Registry.register(BuiltinRegistries.PLACED_FEATURE, new ResourceLocation(MOD_ID, "test_lush_noise_stone"), PLACED_TEST_LUSH_NOISE_STONE);
	}
}

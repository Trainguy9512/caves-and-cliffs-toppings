package com.trainguy.ccworldgen.mixin;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(WorldCarver.class)
public class MixinWorldCarver<C extends CarverConfiguration> {
    @Shadow protected Set<Block> replaceableBlocks;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addCarvableBlocks(Codec<C> codec, CallbackInfo ci){
        replaceableBlocks = ImmutableSet.<Block>builder().addAll(replaceableBlocks).add(Blocks.CALCITE, Blocks.GRAVEL, Blocks.SAND, Blocks.RED_SAND, Blocks.SNOW_BLOCK, Blocks.POWDER_SNOW).build();
    }
}

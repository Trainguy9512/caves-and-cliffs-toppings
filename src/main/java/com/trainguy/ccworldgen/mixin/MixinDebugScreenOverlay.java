package com.trainguy.ccworldgen.mixin;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

import static net.minecraft.client.gui.GuiComponent.fill;

@Mixin(DebugScreenOverlay.class)
public abstract class MixinDebugScreenOverlay {
    @Shadow @Final private Minecraft minecraft;

    @Shadow @Final private Font font;

    @Shadow @Nullable protected abstract ServerLevel getServerLevel();

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void customDebug(PoseStack poseStack, CallbackInfo ci){
        if (this.minecraft.options.renderFpsChart) {
            List<String> list = getAlternateDebugInformation();
            boolean bl = this.minecraft.getSingleplayerServer() != null;

            for(int i = 0; i < list.size(); ++i) {
                String string = (String)list.get(i);
                if (!Strings.isNullOrEmpty(string)) {
                    Objects.requireNonNull(this.font);
                    int j = 9;
                    int k = this.font.width(string);
                    int m = 2 + j * i;
                    fill(poseStack, 1, m - 1, 2 + k + 1, m + j - 1, -1873784752);
                    this.font.draw(poseStack, string, 2.0F, (float)m, 14737632);
                }
            }
            ci.cancel();
        }
    }

    private List<String> getAlternateDebugInformation(){
        List<String> list = Lists.newArrayList();
        ServerLevel serverLevel = this.getServerLevel();
        if (serverLevel != null) {
            ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
            ChunkGenerator chunkGenerator = serverChunkCache.getGenerator();
            Climate.Sampler sampler = chunkGenerator.climateSampler();
            BiomeSource biomeSource = chunkGenerator.getBiomeSource();
            BlockPos blockPos = Objects.requireNonNull(this.minecraft.getCameraEntity()).blockPosition();


            int x = QuartPos.fromBlock(blockPos.getX());
            int y = QuartPos.fromBlock(blockPos.getY());
            int z = QuartPos.fromBlock(blockPos.getZ());
            Climate.TargetPoint targetPoint = sampler.sample(x, y, z);

            DecimalFormat decimalFormat = new DecimalFormat("0.000");
            DecimalFormat decimalFormat2 = new DecimalFormat("0.00");
            list.add("XYZ: " + decimalFormat2.format(this.minecraft.getCameraEntity().getX()) + " " + decimalFormat2.format(this.minecraft.getCameraEntity().getY()) + " " + decimalFormat2.format(this.minecraft.getCameraEntity().getZ()));
            list.add("");
            list.add("Continentalness: " + decimalFormat.format((double) Climate.unquantizeCoord(targetPoint.continentalness())));
            list.add("Erosion: " + decimalFormat.format((double) Climate.unquantizeCoord(targetPoint.erosion())));
            list.add("Weirdness: " + decimalFormat.format((double) Climate.unquantizeCoord(targetPoint.weirdness())));
        }
        return list;
    }
}

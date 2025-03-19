package com.zhenzi.zhenzigtqt.client.render.texture.custom;

import codechicken.lib.colour.Colour;
import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.resources.TextTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.custom.QuantumStorageRenderer;
import gregtech.client.utils.RenderUtil;
import gregtech.common.ConfigHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.aspects.Aspect;

import java.awt.*;

public class AspectStorageRenderer extends QuantumStorageRenderer {

    private static final TextTexture textRenderer = (new TextTexture()).setWidth(32);

    @SideOnly(Side.CLIENT)
    public static void renderTankAspect(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Aspect aspect, int maxCap, int amount, IBlockAccess world, BlockPos pos, EnumFacing frontFacing) {

        if (aspect != null && amount != 0 && ConfigHolder.client.enableFancyChestRender)
        {
            Color co = new Color(0);
            if (aspect != null) {
                co = new Color(aspect.getColor());
            }
            if (world != null) {
                renderState.setBrightness(world, pos);
            }

            Cuboid6 partialFluidBox = new Cuboid6(0.06640625, 0.12890625, 0.06640625, 0.93359375, 0.93359375, 0.93359375);
            double fillFraction = (double)amount / (double)maxCap;

            partialFluidBox.max.y = Math.min(11.875 * fillFraction + 2.0625, 14.0) / 16.0;

            renderState.baseColour = aspect.getColor() << 8 | 255;
//            renderState.setColour(new ColourRGBA(255, 255, 126, 255));
            TextureAtlasSprite icon = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("thaumcraft:blocks/animatedglow");

            Textures.renderFace(renderState, translation, pipeline, frontFacing, partialFluidBox, icon, BlockRenderLayer.CUTOUT_MIPPED);
            Textures.renderFace(renderState, translation, pipeline, EnumFacing.UP, partialFluidBox, icon, BlockRenderLayer.CUTOUT_MIPPED);
            GlStateManager.resetColor();
            renderState.reset();
        }
//        Textures.renderFace(renderState, translation, pipeline, frontFacing, partialFluidBox, fluidStillSprite, BlockRenderLayer.CUTOUT_MIPPED);
//        Textures.renderFace(renderState, translation, pipeline, gas ? EnumFacing.DOWN : EnumFacing.UP, partialFluidBox, fluidStillSprite, BlockRenderLayer.CUTOUT_MIPPED);
//        GlStateManager.resetColor();
//        renderState.reset();
    }

    public static void renderAspectAmount(double x, double y, double z, EnumFacing frontFacing, long amount) {
        float lastBrightnessX = OpenGlHelper.lastBrightnessX;
        float lastBrightnessY = OpenGlHelper.lastBrightnessY;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        renderAspectAmountText(x, y, z, amount, frontFacing);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
    }

    public static void renderAspectAmountText(double x, double y, double z, long amount, EnumFacing frontFacing) {
        if (ConfigHolder.client.enableFancyChestRender && canRender(x, y, z, 64.0) && amount != 0) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.translate((float)(frontFacing.getXOffset() * -1) / 16.0F, (float)(frontFacing.getYOffset() * -1) / 16.0F, (float)(frontFacing.getZOffset() * -1) / 16.0F);
            RenderUtil.moveToFace(0.0, 0.0, 0.0, frontFacing);
            if (frontFacing.getAxis() == EnumFacing.Axis.Y) {
                RenderUtil.rotateToFace(frontFacing, EnumFacing.SOUTH);
            } else {
                RenderUtil.rotateToFace(frontFacing, (EnumFacing)null);
            }

            String amountText = TextFormattingUtil.formatLongToCompactString(amount, 4);
            GlStateManager.scale(0.015625F, 0.015625F, 0.0F);
            GlStateManager.translate(-32.0F, -32.0F, 0.0F);
            GlStateManager.disableLighting();
            textRenderer.setText(amountText);
            textRenderer.draw(0.0, 24.0, 64, 28);
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }
    }

    public static void renderAspect(double x, double y, double z, EnumFacing frontFacing, Aspect aspect, Aspect filter) {
        float lastBrightnessX = OpenGlHelper.lastBrightnessX;
        float lastBrightnessY = OpenGlHelper.lastBrightnessY;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        renderAspectImage(x, y, z, frontFacing, aspect, filter);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
    }

    public static void renderAspectImage(double x, double y, double z, EnumFacing frontFacing, Aspect aspect, Aspect filter) {
        if (ConfigHolder.client.enableFancyChestRender && canRender(x, y, z, 64.0)) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.translate((float)(frontFacing.getXOffset() * -1) / 16.0F, (float)(frontFacing.getYOffset() * -1) / 16.0F, (float)(frontFacing.getZOffset() * -1) / 16.0F);
            RenderUtil.moveToFace(0.0, 0.0, 0.0, frontFacing);
            if (frontFacing.getAxis() == EnumFacing.Axis.Y) {
                RenderUtil.rotateToFace(frontFacing, EnumFacing.SOUTH);
            } else {
                RenderUtil.rotateToFace(frontFacing, (EnumFacing)null);
            }
            TextureArea textureFilterArea = null;
            if (filter != null)
            {
                textureFilterArea = new TextureArea(filter.getImage(), 0,0,1.0,1.0);
                Color co = new Color(0);
                co = new Color(filter.getColor());
                GlStateManager.color((float)co.getRed() / 255.0F, (float)co.getGreen() / 255.0F, (float)co.getBlue() / 255.0F);
                GlStateManager.scale(0.002625F, 0.002625F, 0.0F);
                GlStateManager.translate(-32.0F, -32.0F, 0.0F);
                GlStateManager.disableLighting();
                textureFilterArea.draw(-80, 80, 64, 64);
//            textRenderer.draw(0.0, 24.0, 64, 28);
                GlStateManager.enableLighting();
                GlStateManager.popMatrix();
            }   else if (aspect != null)
            {
                TextureArea textureArea = new TextureArea(aspect.getImage(), 0,0,1.0,1.0);
                GlStateManager.scale(0.002625F, 0.002625F, 0.0F);
                GlStateManager.translate(-32.0F, -32.0F, 0.0F);
                GlStateManager.disableLighting();
                textureArea.draw(-80, 80, 64, 64);
//            textRenderer.draw(0.0, 24.0, 64, 28);
                GlStateManager.enableLighting();
                GlStateManager.popMatrix();
            }

//            String amountText = TextFormattingUtil.formatLongToCompactString(amount, 4);

        }
    }

}

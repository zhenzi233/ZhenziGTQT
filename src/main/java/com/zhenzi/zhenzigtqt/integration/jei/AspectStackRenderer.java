package com.zhenzi.zhenzigtqt.integration.jei;

import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectStack;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.plugins.vanilla.ingredients.fluid.FluidStackRenderer;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import thaumcraft.api.aspects.Aspect;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AspectStackRenderer implements IIngredientRenderer<AspectStack> {
    private static final int TEX_WIDTH = 16;
    private static final int TEX_HEIGHT = 16;
    private static final int MIN_FLUID_HEIGHT = 1;
    private final int capacityMb;
    private final AspectStackRenderer.TooltipMode tooltipMode;
    private final int width;
    private final int height;
    @Nullable
    private final IDrawable overlay;

    public AspectStackRenderer() {
        this(1000, AspectStackRenderer.TooltipMode.ITEM_LIST, 16, 16, (IDrawable)null);
    }

    public AspectStackRenderer(int capacityMb, boolean showCapacity, int width, int height, @Nullable IDrawable overlay) {
        this(capacityMb, showCapacity ? AspectStackRenderer.TooltipMode.SHOW_AMOUNT_AND_CAPACITY : AspectStackRenderer.TooltipMode.SHOW_AMOUNT, width, height, overlay);
    }

    public AspectStackRenderer(int capacityMb, AspectStackRenderer.TooltipMode tooltipMode, int width, int height, @Nullable IDrawable overlay) {
        this.capacityMb = capacityMb;
        this.tooltipMode = tooltipMode;
        this.width = width;
        this.height = height;
        this.overlay = overlay;
    }

    public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable AspectStack fluidStack) {
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        this.drawAspect(minecraft, xPosition, yPosition, fluidStack);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.overlay != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 0.0F, 200.0F);
            this.overlay.draw(minecraft, xPosition, yPosition);
            GlStateManager.popMatrix();
        }

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
    }

    public void drawAspect(Minecraft minecraft, int xPosition, int yPosition, @Nullable AspectStack fluidStack) {
        if (fluidStack != null) {
            IGuiTexture area = new TextureArea(fluidStack.getAspect().getImage(), 0, 0, 1.0, 1.0);
            Color co = new Color(0);
            co = new Color(fluidStack.getAspect().getColor());

            GlStateManager.color((float)co.getRed() / 255.0F, (float)co.getGreen() / 255.0F, (float)co.getBlue() / 255.0F);

            area.draw((double)xPosition, (double)yPosition, this.width, this.height);
        }
    }

    private static void setGLColorFromInt(int color) {
        float red = (float)(color >> 16 & 255) / 255.0F;
        float green = (float)(color >> 8 & 255) / 255.0F;
        float blue = (float)(color & 255) / 255.0F;
        GlStateManager.color(red, green, blue, 1.0F);
    }

    private static void drawTextureWithMasking(double xCoord, double yCoord, TextureAtlasSprite textureSprite, int maskTop, int maskRight, double zLevel) {
        double uMin = (double)textureSprite.getMinU();
        double uMax = (double)textureSprite.getMaxU();
        double vMin = (double)textureSprite.getMinV();
        double vMax = (double)textureSprite.getMaxV();
        uMax -= (double)maskRight / 16.0 * (uMax - uMin);
        vMax -= (double)maskTop / 16.0 * (vMax - vMin);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferBuilder.pos(xCoord, yCoord + 16.0, zLevel).tex(uMin, vMax).endVertex();
        bufferBuilder.pos(xCoord + 16.0 - (double)maskRight, yCoord + 16.0, zLevel).tex(uMax, vMax).endVertex();
        bufferBuilder.pos(xCoord + 16.0 - (double)maskRight, yCoord + (double)maskTop, zLevel).tex(uMax, vMin).endVertex();
        bufferBuilder.pos(xCoord, yCoord + (double)maskTop, zLevel).tex(uMin, vMin).endVertex();
        tessellator.draw();
    }

    public List<String> getTooltip(Minecraft minecraft, AspectStack fluidStack, ITooltipFlag tooltipFlag) {
        List<String> tooltip = new ArrayList();
        Aspect fluidType = fluidStack.getAspect();
        if (fluidType == null) {
            return tooltip;
        } else {
            String fluidName = fluidType.getLocalizedDescription();
            tooltip.add(fluidName);
            String amount;
            if (this.tooltipMode == AspectStackRenderer.TooltipMode.SHOW_AMOUNT_AND_CAPACITY) {
                amount = Translator.translateToLocalFormatted("jei.tooltip.liquid.amount.with.capacity", new Object[]{fluidStack.amount, this.capacityMb});
                tooltip.add(TextFormatting.GRAY + amount);
            } else if (this.tooltipMode == AspectStackRenderer.TooltipMode.SHOW_AMOUNT) {
                amount = Translator.translateToLocalFormatted("jei.tooltip.liquid.amount", new Object[]{fluidStack.amount});
                tooltip.add(TextFormatting.GRAY + amount);
            }

            return tooltip;
        }
    }

    static enum TooltipMode {
        SHOW_AMOUNT,
        SHOW_AMOUNT_AND_CAPACITY,
        ITEM_LIST;

        private TooltipMode() {
        }
    }
}

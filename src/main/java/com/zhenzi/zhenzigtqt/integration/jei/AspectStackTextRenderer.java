package com.zhenzi.zhenzigtqt.integration.jei;

import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectStack;
import gregtech.api.recipes.chance.boost.BoostableChanceEntry;
import gregtech.api.recipes.chance.output.ChancedOutputLogic;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.utils.RenderUtil;
import gregtech.integration.jei.utils.render.FluidStackTextRenderer;
import mezz.jei.api.gui.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AspectStackTextRenderer extends AspectStackRenderer{
    private boolean notConsumed;
    private int chanceBase = -1;
    private int chanceBoost = -1;
    private ChancedOutputLogic chanceLogic;

    public AspectStackTextRenderer(int capacityMb, boolean showCapacity, int width, int height, @Nullable IDrawable overlay) {
        super(capacityMb, showCapacity, width, height, overlay);
        this.notConsumed = false;
    }

    public AspectStackTextRenderer setNotConsumed(boolean notConsumed) {
        this.notConsumed = notConsumed;
        return this;
    }

    public AspectStackTextRenderer(int capacityMb, boolean showCapacity, int width, int height, @Nullable IDrawable overlay, BoostableChanceEntry<FluidStack> chance, ChancedOutputLogic chanceLogic) {
        if (chance != null) {
            this.chanceBase = chance.getChance();
            this.chanceBoost = chance.getChanceBoost();
            this.chanceLogic = chanceLogic;
        }

        this.notConsumed = false;
    }

    public void render(@NotNull Minecraft minecraft, int xPosition, int yPosition, @Nullable AspectStack fluidStack) {
        if (fluidStack != null) {
            GlStateManager.disableBlend();
            super.drawAspect(minecraft, xPosition, yPosition, fluidStack);
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5, 0.5, 1.0);
            String s = TextFormattingUtil.formatLongToCompactString((long)fluidStack.amount, 4);
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            fontRenderer.drawStringWithShadow(s, (float)((xPosition + 6) * 2 - fontRenderer.getStringWidth(s) + 19), (float)((yPosition + 11) * 2), 16777215);
            GlStateManager.popMatrix();
            if (this.chanceBase >= 0) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.5, 0.5, 1.0);
                GlStateManager.translate(0.0F, 0.0F, 160.0F);
                String s2 = this.chanceBase / 100 + "%";
                if (this.chanceLogic != null && this.chanceLogic != ChancedOutputLogic.NONE && this.chanceLogic != ChancedOutputLogic.OR) {
                    s2 = s2 + "*";
                } else if (this.chanceBoost > 0) {
                    s2 = s2 + "+";
                }

                fontRenderer.drawStringWithShadow(s2, (float)((xPosition + 6) * 2 - fontRenderer.getStringWidth(s2) + 19), (float)((yPosition + 1) * 2), 16776960);
                GlStateManager.popMatrix();
            } else if (this.notConsumed) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.5, 0.5, 1.0);
                fontRenderer.drawStringWithShadow("NC", (float)((xPosition + 6) * 2 - fontRenderer.getStringWidth("NC") + 19), (float)((yPosition + 1) * 2), 16776960);
                GlStateManager.popMatrix();
            }

            GlStateManager.enableBlend();
        }
    }
}

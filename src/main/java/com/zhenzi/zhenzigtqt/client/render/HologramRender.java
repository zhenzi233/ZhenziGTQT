package com.zhenzi.zhenzigtqt.client.render;

import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class HologramRender {

    public HologramRender(){}

    public static class HologramConfig {
        // 位置偏移（以方块中心为原点）
        public float posX = 0.0f;
        public float posY = 4.5f;
        public float posZ = 0.0f;

        // 缩放比例
        public float scale = 1.0f;

        // 旋转参数（角度制）
        public float rotationYaw = 0.0f;   // Y轴旋转
        public float rotationPitch = 0.0f; // X轴旋转
        public float rotationRoll = 0.0f;   // Z轴旋转

        // 背景尺寸
        public float width = 4.0f;
        public float height = 3.0f;

        // 背景颜色
        public float bgRed = 0.0f;
        public float bgGreen = 0.0f;
        public float bgBlue = 1.0f;
        public float bgAlpha = 0.5f;

        // 文本配置
        public float textScale = 0.02f;
        public int maxLineWidth = 200; // 像素单位
    }

    private final HologramConfig hologramConfig = new HologramConfig();

    // 渲染方法实现
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks, EnumFacing facing) {

//        if (!isStructureFormed()) return;

        GlStateManager.pushMatrix();
        try {
            prepareTransform(x + 0.5, y, z + 0.5, facing);
            renderBackground();
            renderText();
        } finally {
            GlStateManager.popMatrix();
        }
    }

    private void prepareTransform(double baseX, double baseY, double baseZ, EnumFacing facing) {
        // 应用基础位移
        GlStateManager.translate(
                baseX + hologramConfig.posX,
                baseY + hologramConfig.posY,
                baseZ + hologramConfig.posZ
        );

        // 应用面向方向旋转
        applyFrontFacingRotation(facing);

        // 应用自定义旋转（顺序：Yaw -> Pitch -> Roll）
        GlStateManager.rotate(hologramConfig.rotationYaw, 0, 1, 0);
        GlStateManager.rotate(hologramConfig.rotationPitch, 1, 0, 0);
        GlStateManager.rotate(hologramConfig.rotationRoll, 0, 0, 1);

        // 应用缩放
        GlStateManager.scale(hologramConfig.scale, hologramConfig.scale, hologramConfig.scale);
    }

    private void applyFrontFacingRotation(EnumFacing facing) {
        switch (facing) {
            case SOUTH:
                GlStateManager.rotate(180, 0, 1, 0);
                break;
            case WEST:
                GlStateManager.rotate(90, 0, 1, 0);
                break;
            case EAST:
                GlStateManager.rotate(-90, 0, 1, 0);
                break;
            case NORTH:
            default:
                break;
        }
    }

    private void renderBackground() {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(
                hologramConfig.bgRed,
                hologramConfig.bgGreen,
                hologramConfig.bgBlue,
                hologramConfig.bgAlpha
        );

        float halfWidth = hologramConfig.width / 2;
        float halfHeight = hologramConfig.height / 2;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        buffer.pos(-halfWidth, -halfHeight, 0.1).endVertex();
        buffer.pos(-halfWidth, halfHeight, 0.1).endVertex();
        buffer.pos(halfWidth, halfHeight, 0.1).endVertex();
        buffer.pos(halfWidth, -halfHeight, 0.1).endVertex();
        tessellator.draw();

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }


    private void renderText() {
        GlStateManager.translate(0, 0, 0.1);
        GlStateManager.scale(hologramConfig.textScale, hologramConfig.textScale, hologramConfig.textScale);

        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        float scaleFactor = 1/hologramConfig.textScale; // 计算实际缩放系数

        int x=0;
        // 渲染标题
        List<String> titleLines = wrapText(fontRenderer,(String.format("gtqtcore.multiblock.kqn")),
                (int)(hologramConfig.maxLineWidth * scaleFactor));
        for (int i = 0; i < titleLines.size(); i++) {
            renderTextLine(fontRenderer, titleLines.get(i),  hologramConfig.width*25-5, hologramConfig.height*25-5 -i*15, 0xFFFFFF, false);
            x++;
        }

        // 渲染正文
        List<String> wrappedLines = wrapText(fontRenderer, (String.format("gtqtcore.multiblock.kqn")),
                (int)(hologramConfig.maxLineWidth * scaleFactor));
        for (int i = 0; i < wrappedLines.size(); i++) {
            renderTextLine(fontRenderer, wrappedLines.get(i), hologramConfig.width*25-5, hologramConfig.height*25-5 -(i+x)*15, 0xFFFFFF, false);
        }

        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
    }

    private List<String> wrapText(FontRenderer fr, String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        for (String s : text.split(" ")) {
            if (lines.isEmpty()) {
                lines.add(s);
                continue;
            }

            String last = lines.get(lines.size()-1);
            if (fr.getStringWidth(last + " " + s) <= maxWidth) {
                lines.set(lines.size()-1, last + " " + s);
            } else {
                lines.add(s);
            }
        }
        return lines;
    }

    private void renderTextLine(FontRenderer fr, String text, float x, float y, int color, boolean center) {
        GlStateManager.pushMatrix();
        try {
            if (center) {
                int textWidth = fr.getStringWidth(text);
                GlStateManager.translate(-textWidth/2.0, 0, 0);
            }

            GlStateManager.translate(x, y, 0);
            GlStateManager.rotate(180, 0, 1, 0);
            GlStateManager.rotate(180, 1, 0, 0);

            fr.drawString(text, 0, 0, color);
        } finally {
            GlStateManager.popMatrix();
        }
    }

//    public AxisAlignedBB getRenderBoundingBox() {
//        //这个影响模型的可视范围，正常方块都是 1 1 1，长宽高各为1，当这个方块离线玩家视线后，obj模型渲染会停止，所以可以适当放大这个大小能让模型有更多角度的可视
//        return new AxisAlignedBB(getPos(), getPos().add(5, 5, 5));
//    }
}

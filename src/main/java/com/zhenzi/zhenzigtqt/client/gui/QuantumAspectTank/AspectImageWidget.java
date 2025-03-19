package com.zhenzi.zhenzigtqt.client.gui.QuantumAspectTank;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.util.LocalizationUtils;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import thaumcraft.api.aspects.Aspect;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class AspectImageWidget extends ClickButtonWidget {
    private boolean enableColor;
    private String tooltipText;
    protected final Consumer<AspectImageWidget.ClickAspectData> onPressCallbackA;
    private Aspect aspect = null;
    public AspectImageWidget(int xPosition, int yPosition, int width, int height, Aspect aspect, Consumer<ClickAspectData> onPressed) {
        super(xPosition, yPosition, width, height, "", null);
        this.aspect = aspect;
        this.onPressCallbackA = onPressed;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        AspectImage image = null;
        if (this.aspect == null)
        {
            image = AspectImage.EMPTY;
        }   else
        {
            image = AspectImage.aspectImages.get(this.aspect.getTag());
        }
        IGuiTexture area = image.getArea();

        if (super.isVisible() && area != null) {
            Color co = new Color(0);
            co = new Color(image.getColor());
            if (this.enableColor)
            {
                GlStateManager.color((float)co.getRed() / 255.0F, (float)co.getGreen() / 255.0F, (float)co.getBlue() / 255.0F);
            }   else
            {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }
            Position position = this.getPosition();
            Size size = this.getSize();
            area.draw((double)position.x, (double)position.y, size.width, size.height);
        }

        if (this.isActive())
        {
            if (this.isMouseOverElement(mouseX, mouseY)) {
                GlStateManager.disableDepth();
                GlStateManager.colorMask(true, true, true, false);
                drawSolidRect(this.getPosition().x, this.getPosition().y, 16, 16, -2130706433);
                GlStateManager.colorMask(true, true, true, true);
                GlStateManager.enableDepth();
                GlStateManager.enableBlend();
            }
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        Aspect aspect = null;
        if (this.aspect != null)
        {
            AspectImage image = AspectImage.aspectImages.get(this.aspect.getTag());
            IGuiTexture area = image.getArea();
            if (this.isVisible() && this.tooltipText != null && area != null && this.isMouseOverElement(mouseX, mouseY)) {
                List<String> hoverList = Arrays.asList(LocalizationUtils.formatLines(this.tooltipText, new Object[0]));
                this.drawHoveringText(ItemStack.EMPTY, hoverList, 300, mouseX, mouseY);
            }
        }   else
        {
            AspectImage image = AspectImage.EMPTY;
            IGuiTexture area = image.getArea();
            if (this.isVisible() && this.tooltipText != null && area != null && this.isMouseOverElement(mouseX, mouseY)) {
                List<String> hoverList = Arrays.asList(LocalizationUtils.formatLines(this.tooltipText, new Object[0]));
                this.drawHoveringText(ItemStack.EMPTY, hoverList, 300, mouseX, mouseY);
            }
        }
    }

    public AspectImageWidget enableColor(Boolean flag)
    {
        this.enableColor = flag;
        return this;
    }
    protected Aspect getAspect()
    {
        return this.aspect;
    }

    public void setAspect(Aspect aspect)
    {
        this.aspect = aspect;
    }

    public AspectImageWidget setTooltip(String tooltipText) {
        this.tooltipText = tooltipText;
        return this;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!(Boolean)this.shouldDisplay.get()) {
            return false;
        } else if (this.isMouseOverElement(mouseX, mouseY)) {
            this.triggerButton();
            return true;
        } else {
            return false;
        }
    }

    protected void triggerButton() {
        AspectImageWidget.ClickAspectData clickData = new AspectImageWidget.ClickAspectData(Mouse.getEventButton(), isShiftDown(), isCtrlDown(), this.aspect);
        Objects.requireNonNull(clickData);
        this.writeClientAction(1, clickData::writeToBuf);
        if (this.shouldClientCallback) {
            this.onPressCallbackA.accept(new AspectImageWidget.ClickAspectData(Mouse.getEventButton(), isShiftDown(), isCtrlDown(), this.aspect));
        }

        playButtonClickSound();
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 1) {
            AspectImageWidget.ClickAspectData clickData = ClickAspectData.readFromBuf(buffer);
            this.onPressCallbackA.accept(clickData);
        }

    }

    public static final class ClickAspectData {
        public final int button;
        public final boolean isShiftClick;
        public final boolean isCtrlClick;
        public final boolean isClient;
        public Aspect aspect = null;

        public ClickAspectData(int button, boolean isShiftClick, boolean isCtrlClick, Aspect aspect) {
            this.button = button;
            this.isShiftClick = isShiftClick;
            this.isCtrlClick = isCtrlClick;
            this.isClient = false;
            this.aspect = aspect;
        }

        public ClickAspectData(int button, boolean isShiftClick, boolean isCtrlClick, boolean isClient, Aspect aspect) {
            this.button = button;
            this.isShiftClick = isShiftClick;
            this.isCtrlClick = isCtrlClick;
            this.isClient = isClient;
            this.aspect = aspect;
        }

        public void writeToBuf(PacketBuffer buf) {
            buf.writeVarInt(this.button);
            buf.writeBoolean(this.isShiftClick);
            buf.writeBoolean(this.isCtrlClick);
            buf.writeBoolean(this.isClient);
            buf.writeString(this.aspect == null ? "" : this.aspect.getTag());
        }

        public static AspectImageWidget.ClickAspectData readFromBuf(PacketBuffer buf) {
            int button = buf.readVarInt();
            boolean shiftClick = buf.readBoolean();
            boolean ctrlClick = buf.readBoolean();
            boolean isClient = buf.readBoolean();
            String aspectS = buf.readString(100);
            Aspect aspect = Aspect.getAspect(aspectS);
            return new AspectImageWidget.ClickAspectData(button, shiftClick, ctrlClick, isClient, aspect);
        }
    }
}

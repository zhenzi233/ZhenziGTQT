package com.zhenzi.zhenzigtqt.client.gui.QuantumAspectTank;

import com.zhenzi.zhenzigtqt.client.gui.MultiQuantumTankMainWidget;
import com.zhenzi.zhenzigtqt.common.metatileentity.MetaTileEntityAspectTank;
import com.zhenzi.zhenzigtqt.common.metatileentity.MetaTileEntityMultiQuantumTank;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.*;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import thaumcraft.api.aspects.Aspect;

import java.util.Iterator;

public class QuantumAspectTankWidget extends WidgetGroup {
    private MetaTileEntityAspectTank te;
    private final QuantumAspectTankMainWidget main;

    private AspectImageWidget imageWidget;
    public QuantumAspectTankWidget(QuantumAspectTankMainWidget main, int xPos, int yPos, MetaTileEntityAspectTank te, int slot) {
        super(new Position(xPos, yPos), new Size(176, 166));
        this.main = main;
        this.te = te;
        this.imageWidget = new AspectImageWidget(69, 43, 16, 16, null, null).enableColor(true).setTooltip("zhenzigtqt.machine.aspect_tank.help");
        this.createUI();
    }

    public void createUI()
    {

        this.addWidget(new ImageWidget(7, 16, 81, 46, GuiTextures.DISPLAY));
        this.addWidget(new LabelWidget(11, 20, "zhenzigtqt.gui.aspect_amount", 16777215));

        this.addWidget(imageWidget);

        this.addWidget(new AdvancedTextWidget(11, 30, te.getAspectAmountText(), 16777215));
        this.addWidget(new AdvancedTextWidget(11, 40, te.getAspectText(), 16777215));
        this.addWidget((new SlotWidget(te.getImportItems(), 0, 90, 17, true, true)).setBackgroundTexture(new IGuiTexture[]{GuiTextures.SLOT, GuiTextures.IN_SLOT_OVERLAY}));
        this.addWidget((new SlotWidget(te.getExportItems(), 0, 90, 44, true, false)).setBackgroundTexture(new IGuiTexture[]{GuiTextures.SLOT, GuiTextures.OUT_SLOT_OVERLAY}));

    }

    public void setAspectImage(Aspect aspect)
    {
        this.imageWidget.setAspect(aspect);
    }

    @Override
    public void updateScreen() {
        this.setAspectImage(this.te.aspect);
        Iterator var1 = this.widgets.iterator();

        while(var1.hasNext()) {
            Widget widget = (Widget)var1.next();
            if (widget.isActive()) {
                widget.updateScreen();
            }
        }



        if (this.waitToRemoved != null) {
            this.waitToRemoved.forEach(this::removeWidget);
            this.waitToRemoved = null;
        }

    }
}

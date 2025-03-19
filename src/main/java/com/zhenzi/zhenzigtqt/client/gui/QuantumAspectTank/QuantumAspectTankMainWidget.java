package com.zhenzi.zhenzigtqt.client.gui.QuantumAspectTank;

import com.zhenzi.zhenzigtqt.common.metatileentity.MetaTileEntityAspectTank;
import com.zhenzi.zhenzigtqt.common.metatileentity.MetaTileEntityMultiQuantumTank;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.*;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import thaumcraft.api.aspects.Aspect;

import java.util.Iterator;


public class QuantumAspectTankMainWidget  extends AbstractWidgetGroup {
    private MetaTileEntityAspectTank te;
    private int page;
    private boolean LockGui;
    private final QuantumAspectTankWidget widget;
    private final QuantumAspectTankLockWidget lockWidget;


    public QuantumAspectTankMainWidget(int xPos, int yPos, MetaTileEntityAspectTank te, int slot) {
        super(new Position(xPos, yPos), new Size(176, 166));
        this.te = te;

        this.widget = new QuantumAspectTankWidget(this, xPos, yPos, te, slot);
        this.lockWidget = new QuantumAspectTankLockWidget(this, xPos, yPos, te, slot);
        this.addWidget(widget);
        this.addWidget(lockWidget);

        this.lockWidget.setActive(false);
        this.lockWidget.setVisible(false);

        this.createUI();
    }

    public void createUI()
    {
        this.addWidget(new ToggleButtonWidget(43, 64, 18, 18, GuiTextures.BUTTON_LOCK, this::isLockGui, this::setLockGui).shouldUseBaseBackground().setTooltipText("zhenzigtqt.gui.changelock"));
        this.addWidget(this.te.createOUTPUTui(25, 64));
        this.addWidget(this.te.createVoidUi(7, 64));
    }

    public void setAspectImage(Aspect aspect)
    {
        this.writeUpdateInfo(204, packetBuffer ->
        {
            packetBuffer.writeString(aspect.getTag());
        });

    }



    protected boolean isLockGui() {
        return this.LockGui;
    }
    protected void setLockGui(boolean LockGui) {
        this.LockGui = LockGui;
        if (this.LockGui)
        {
            this.writeUpdateInfo(201, packetBuffer -> {});
        }   else
        {
            this.writeUpdateInfo(202, packetBuffer -> {});
        }

    }
    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 201) {
            this.widget.setActive(false);
            this.widget.setVisible(false);
            this.lockWidget.setActive(true);
            this.lockWidget.setVisible(true);
        } else if (id == 202) {
            this.lockWidget.setVisible(false);
            this.lockWidget.setActive(false);
            this.widget.setActive(true);
            this.widget.setVisible(true);
        } else if (id == 204) {
            Aspect aspect = Aspect.getAspect(buffer.readString(100));
            this.widget.setAspectImage(aspect);
        }
        else {
            super.readUpdateInfo(id, buffer);
        }
    }
}

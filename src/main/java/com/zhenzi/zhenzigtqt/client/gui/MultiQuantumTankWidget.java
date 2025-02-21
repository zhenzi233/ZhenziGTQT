package com.zhenzi.zhenzigtqt.client.gui;

import com.zhenzi.zhenzigtqt.common.metatileentity.MetaTileEntityMultiQuantumTank;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.*;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.network.PacketBuffer;


public class MultiQuantumTankWidget extends WidgetGroup {

    private MetaTileEntityMultiQuantumTank te;
    private final MultiQuantumTankMainWidget main;
    private int slot;
    public MultiQuantumTankWidget(MultiQuantumTankMainWidget main, int xPos, int yPos, MetaTileEntityMultiQuantumTank te, int slot) {
        super(new Position(xPos, yPos), new Size(176, 166));
        this.main = main;
        this.te = te;
        this.slot = slot;

        if (slot == 4)
        {
            this.create4SlotUI();
        }   else if (slot == 9)
        {
            this.create9SlotUI();
        }   else if (slot == 16)
        {
            this.create16SlotUI();
        }   else if (slot == 25)
        {
            this.create25SlotUI();
        }
    }

    public void create4SlotUI()
    {
        for (int i = 0; i < this.te.getFluidTanks().length; i++) {
            this.addWidget(this.te.createTankWidget(this.te.getTankAt(i), i, 10 + i * 18, 43, 18, 18,  GuiTextures.FLUID_SLOT));
        }
    }

    public void create9SlotUI()
    {
        for (int i = 0; i < this.te.getFluidTanks().length; i++) {
            this.addWidget(this.te.createTankWidget(this.te.getTankAt(i), i, 7 + i * 18, 28, 18, 18,  GuiTextures.FLUID_SLOT));
        }
    }

    public void create16SlotUI()
    {
        for (int i = 0; i < this.te.getFluidTanks().length; i++) {
            int row1 = i / 4;
            int y1 = 28 + row1 * 18;
            int col1 = i % 4;
            int x1 = 7 + col1 * 18;
            this.addWidget(this.te.createTankWidget(this.te.getTankAt(i), i, x1, y1, 18, 18,  GuiTextures.FLUID_SLOT));
        }
    }

    public void create25SlotUI(){
        for (int i = 0; i < this.te.getFluidTanks().length; i++) {
            int row1 = i / 5;
            int y1 = 28 + row1 * 18;
            int col1 = i % 5;
            int x1 = 7 + col1 * 18;
            this.addWidget(this.te.createTankWidget(this.te.getTankAt(i), i, x1, y1, 18, 18,  GuiTextures.FLUID_SLOT));
        }
    }
    public void hideTank() {
        this.setActive(false);
        this.setVisible(false);
    }
    public void showTank() {
        this.setActive(true);
        this.setVisible(true);
    }

}

package com.zhenzi.zhenzigtqt.client.gui;

import com.zhenzi.zhenzigtqt.common.metatileentity.MetaTileEntityMultiQuantumTank;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import java.util.Objects;

public class MultiQuantumTankMainWidget extends AbstractWidgetGroup {
    private final MultiQuantumTankWidget tank;
    private FluidStack[] stacks;

    private boolean LockGui;
    private PhantomTankWidget[] phantomTankWidgets;
    private ImageWidget[] lockSlotImage;
    private int slot;
    private MetaTileEntityMultiQuantumTank te;
    public MultiQuantumTankMainWidget(int xPos, int yPos, MetaTileEntityMultiQuantumTank te, int slot) {
        super(new Position(xPos, yPos), new Size(176, 166));
        this.te = te;
        this.tank = new MultiQuantumTankWidget(this, xPos, yPos, te, slot);
        this.slot = slot;

        if (slot == 4)
        {
            this.create4SlotUI();
        } else if (slot == 9)
        {
            this.create9SlotUI();
        } else if (slot == 16)
        {
            this.create16SlotUI();
        } else if (slot == 25)
        {
            this.create25SlotUI();
        }
    }

    public void create4SlotUI()
    {
        this.phantomTankWidgets = new PhantomTankWidget[te.getFluidTanks().length];
        this.lockSlotImage = new ImageWidget[te.getFluidTanks().length];

        this.addWidget(new ImageWidget(7, 16, 81, 46, GuiTextures.DISPLAY));
        this.addWidget(this.te.createOUTPUTui(7, 64));
        this.addWidget(this.te.createVoidUi(25, 64));
        this.addWidget(new LabelFluidLockedTooltipWidget(11, 20, "Locked Fluid", te.getLockedFluids()));
        for (int i = 0; i < this.te.getFluidTanks().length; i++) {
            this.addWidget(this.te.createContainerSlot(i, 92  + i * 18, 17));
            this.addWidget(this.te.createSlot(i, 92  + i * 18, 44));

            MetaTileEntityMultiQuantumTank.MuitlQuantumFluidTank tank = this.te.getTankAt(i);
            int finalI = i;
            this.phantomTankWidgets[i] = new PhantomTankWidget(this.te.getTankAt(i).getLockedTank(), 10 + i * 18, 43, 18, 18, () -> {
                if (tank != null && tank.getLockedFluid() != null)
                {
                    return tank.getLockedFluid();
                }   else return null;
            }, f -> {
                if (f == null)
                {
                    tank.setIsLocked(false);
                    tank.setLockedFluid(null);
                    this.te.writeLockedFluids(finalI);
                    this.te.markDirty();

                }   else
                {
                    tank.setIsLocked(true);
                    tank.setLockedFluid(f.copy());
                    tank.getLockedFluid().amount = 1;
                    this.te.writeLockedFluids(finalI);
                    this.te.markDirty();
                }
            });

            this.phantomTankWidgets[i].setClient();
            this.lockSlotImage[i] = new ImageWidget(10 + i * 18, 43, 18, 18, GuiTextures.SLOT);
            this.addWidget(this.lockSlotImage[i]);
            this.addWidget(this.phantomTankWidgets[i]);
        }
        this.addWidget(this.tank);

        this.setLockGui(false);

        this.addWidget(new ToggleButtonWidget(43, 64, 18, 18, GuiTextures.BUTTON_LOCK, this::isLockGui, this::setLockGui).shouldUseBaseBackground().setTooltipText("zhenzigtqt.gui.changelock"));
    }
    public void create9SlotUI()
    {
        super.setSize(new Size(176, 166 + 8));

        this.phantomTankWidgets = new PhantomTankWidget[te.getFluidTanks().length];
        this.lockSlotImage = new ImageWidget[te.getFluidTanks().length];

        this.addWidget(new ImageWidget(7, 8, 81, 16, GuiTextures.DISPLAY));
        this.addWidget(this.te.createOUTPUTui(151, 8));
        this.addWidget(this.te.createVoidUi(133, 8));
        this.addWidget(new LabelFluidLockedTooltipWidget(11, 10, "Locked Fluid", te.getLockedFluids()));

        for (int i = 0; i < this.te.getFluidTanks().length; i++) {
            this.addWidget(this.te.createContainerSlot(i, 7  + i * 18, 48));
            this.addWidget(this.te.createSlot(i, 7  + i * 18, 68));

            MetaTileEntityMultiQuantumTank.MuitlQuantumFluidTank tank = this.te.getTankAt(i);
            int finalI = i;
            this.phantomTankWidgets[i] = new PhantomTankWidget(this.te.getTankAt(i).getLockedTank(), 7 + i * 18, 28, 18, 18, () -> {
                if (tank != null && tank.getLockedFluid() != null)
                {
                    return tank.getLockedFluid();
                }   else return null;
            }, f -> {
                if (f == null)
                {
                    tank.setIsLocked(false);
                    tank.setLockedFluid(null);
                    this.te.writeLockedFluids(finalI);
                    this.te.markDirty();

                }   else
                {
                    tank.setIsLocked(true);
                    tank.setLockedFluid(f.copy());
                    tank.getLockedFluid().amount = 1;
                    this.te.writeLockedFluids(finalI);
                    this.te.markDirty();
                }
            });

            this.phantomTankWidgets[i].setClient();
            this.lockSlotImage[i] = new ImageWidget(7 + i * 18, 28, 18, 18, GuiTextures.SLOT);
            this.addWidget(this.lockSlotImage[i]);
            this.addWidget(this.phantomTankWidgets[i]);
        }
        this.addWidget(this.tank);

        this.setLockGui(false);

        this.addWidget(new ToggleButtonWidget(115, 8, 18, 18, GuiTextures.BUTTON_LOCK, this::isLockGui, this::setLockGui).shouldUseBaseBackground().setTooltipText("zhenzigtqt.gui.changelock"));
    }

    public void create16SlotUI()
    {
        super.setSize(new Size(176, 166 + 8 + 18));

        this.phantomTankWidgets = new PhantomTankWidget[te.getFluidTanks().length];
        this.lockSlotImage = new ImageWidget[te.getFluidTanks().length];

        this.addWidget(new ImageWidget(7, 8, 72, 16, GuiTextures.DISPLAY));
        this.addWidget(this.te.createOUTPUTui(151, 8));
        this.addWidget(this.te.createVoidUi(133, 8));
        this.addWidget(new LabelFluidLockedTooltipWidget(11, 10, "Locked Fluid", te.getLockedFluids()));

        for (int i = 0; i < 8; i++) {
            int row = i / 2;
            int y = 28 + row * 18;
            int col = i % 2;
            int x = 7 + col * 18;
            this.addWidget(this.te.createContainerSlot(i, 90 + x, y));
            this.addWidget(this.te.createSlot(i, 90 + 36 + x, y));
        }
        for (int i = 0; i < this.te.getFluidTanks().length; i++) {

            MetaTileEntityMultiQuantumTank.MuitlQuantumFluidTank tank = this.te.getTankAt(i);
            int finalI = i;
            int row1 = i / 4;
            int y1 = 28 + row1 * 18;
            int col1 = i % 4;
            int x1 = 7 + col1 * 18;
            this.phantomTankWidgets[i] = new PhantomTankWidget(this.te.getTankAt(i).getLockedTank(), x1, y1, 18, 18, () -> {
                if (tank != null && tank.getLockedFluid() != null)
                {
                    return tank.getLockedFluid();
                }   else return null;
            }, f -> {
                if (f == null)
                {
                    tank.setIsLocked(false);
                    tank.setLockedFluid(null);
                    this.te.writeLockedFluids(finalI);
                    this.te.markDirty();

                }   else
                {
                    tank.setIsLocked(true);
                    tank.setLockedFluid(f.copy());
                    tank.getLockedFluid().amount = 1;
                    this.te.writeLockedFluids(finalI);
                    this.te.markDirty();
                }
            });

            this.phantomTankWidgets[i].setClient();
            this.lockSlotImage[i] = new ImageWidget(x1, y1, 18, 18, GuiTextures.SLOT);
            this.addWidget(this.lockSlotImage[i]);
            this.addWidget(this.phantomTankWidgets[i]);
        }
        this.addWidget(this.tank);

        this.setLockGui(false);

        this.addWidget(new ToggleButtonWidget(115, 8, 18, 18, GuiTextures.BUTTON_LOCK, this::isLockGui, this::setLockGui).shouldUseBaseBackground().setTooltipText("zhenzigtqt.gui.changelock"));

    }

    public void create25SlotUI()
    {
        super.setSize(new Size(176, 166 + 8 + 18 + 18));

        this.phantomTankWidgets = new PhantomTankWidget[te.getFluidTanks().length];
        this.lockSlotImage = new ImageWidget[te.getFluidTanks().length];

        this.addWidget(new ImageWidget(7, 8, 72, 16, GuiTextures.DISPLAY));
        this.addWidget(this.te.createOUTPUTui(151, 8));
        this.addWidget(this.te.createVoidUi(133, 8));
        this.addWidget(new LabelFluidLockedTooltipWidget(11, 10, "Locked Fluid", te.getLockedFluids()));

        for (int i = 0; i < 10; i++) {
            int row = i / 2;
            int y = 28 + row * 18;
            int col = i % 2;
            int x = 7 + col * 18;
            this.addWidget(this.te.createContainerSlot(i, 90 + x, y));
            this.addWidget(this.te.createSlot(i, 90 + 36 + x, y));
        }
        for (int i = 0; i < this.te.getFluidTanks().length; i++) {

            MetaTileEntityMultiQuantumTank.MuitlQuantumFluidTank tank = this.te.getTankAt(i);
            int finalI = i;
            int row1 = i / 5;
            int y1 = 28 + row1 * 18;
            int col1 = i % 5;
            int x1 = 7 + col1 * 18;
            this.phantomTankWidgets[i] = new PhantomTankWidget(this.te.getTankAt(i).getLockedTank(), x1, y1, 18, 18, () -> {
                if (tank != null && tank.getLockedFluid() != null)
                {
                    return tank.getLockedFluid();
                }   else return null;
            }, f -> {
                if (f == null)
                {
                    tank.setIsLocked(false);
                    tank.setLockedFluid(null);
                    this.te.writeLockedFluids(finalI);
                    this.te.markDirty();

                }   else
                {
                    tank.setIsLocked(true);
                    tank.setLockedFluid(f.copy());
                    tank.getLockedFluid().amount = 1;
                    this.te.writeLockedFluids(finalI);
                    this.te.markDirty();
                }
            });

            this.phantomTankWidgets[i].setClient();
            this.lockSlotImage[i] = new ImageWidget(x1, y1, 18, 18, GuiTextures.SLOT);
            this.addWidget(this.lockSlotImage[i]);
            this.addWidget(this.phantomTankWidgets[i]);
        }
        this.addWidget(this.tank);

        this.setLockGui(false);

        this.addWidget(new ToggleButtonWidget(115, 8, 18, 18, GuiTextures.BUTTON_LOCK, this::isLockGui, this::setLockGui).shouldUseBaseBackground().setTooltipText("zhenzigtqt.gui.changelock"));

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
            this.tank.hideTank();

            for (int i = 0; i < this.phantomTankWidgets.length; i++)
            {
                this.lockSlotImage[i].setActive(true);
                this.lockSlotImage[i].setVisible(true);
                this.phantomTankWidgets[i].setActive(true);
                this.phantomTankWidgets[i].setVisible(true);
            }

        } else if (id == 202) {
            for (int i = 0; i < this.phantomTankWidgets.length; i++)
            {
                this.lockSlotImage[i].setActive(false);
                this.lockSlotImage[i].setVisible(false);
                this.phantomTankWidgets[i].setActive(false);
                this.phantomTankWidgets[i].setVisible(false);
            }
            this.tank.showTank();
        }else {
            super.readUpdateInfo(id, buffer);
        }
    }
}

package com.zhenzi.zhenzigtqt.client.gui.QuantumAspectTank;

import com.zhenzi.zhenzigtqt.common.metatileentity.MetaTileEntityAspectTank;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.network.PacketBuffer;
import thaumcraft.api.aspects.Aspect;

import java.util.*;

public class QuantumAspectTankLockWidget extends WidgetGroup {
    private MetaTileEntityAspectTank te;
    private final QuantumAspectTankMainWidget main;
    private int page = 0;
    private int pages = 0;
    private List<List<AspectImageWidget>> aspectImagePageWidgets = new ArrayList<>();
    public QuantumAspectTankLockWidget(QuantumAspectTankMainWidget main, int xPos, int yPos, MetaTileEntityAspectTank te, int slot) {
        super(new Position(xPos, yPos), new Size(176, 166));
        this.main = main;
        this.te = te;

        this.createUI();
    }

    public void createUI(){
        this.addWidget(new ImageWidget(7, 16, 162, 46, GuiTextures.DISPLAY));
        LinkedHashMap<String, Aspect> aspects = Aspect.aspects;
        int x = 0;
        int y = 0;
        int a = 0;
        List<Aspect> aspectList = new ArrayList<>();
        List<List<AspectImageWidget>> aspectListPage = new ArrayList<>();

        for (Map.Entry<String, Aspect> entry : aspects.entrySet())
        {
            aspectList.add(entry.getValue());
        }
        this.pages = aspectList.size() / 16 + 1;
        for (int i = 0; i < this.pages; i++)
        {
            List<AspectImageWidget> tempList = new ArrayList<>();
            for (int j = i * 16; j < 16 * (i + 1); j++)
            {
                x = j % 8;
                a = j / 8;
                y = a % 2 == 0 ? 0 : 1;
                if (j >= aspectList.size()) break;
                AspectImageWidget imageWidget = new AspectImageWidget(10 + x * 18, 25 + y * 18, 16, 16, aspectList.get(j), this::setAspectFilter);

                imageWidget.setTooltip(aspectList.get(j).getLocalizedDescription());
                imageWidget.enableColor(false);

                if (this.te.aspectFilter != null && Objects.equals(this.te.aspectFilter.getTag(), aspectList.get(j).getTag()))
                {
                    imageWidget.enableColor(true);
                }

                this.addWidget(imageWidget);

                tempList.add(imageWidget);
            }
            aspectListPage.add(tempList);
        }

        this.aspectImagePageWidgets = aspectListPage;

        this.changePage(0);

        this.addWidget(new ClickButtonWidget(133, 64, 18, 18, "", this::changePageLeft).setButtonTexture(GuiTextures.BUTTON_LEFT));
        this.addWidget(new ClickButtonWidget(151, 64, 18, 18, "",this::changePageRight).setButtonTexture(GuiTextures.BUTTON_RIGHT));

    }

    public void setAspectFilter(AspectImageWidget.ClickAspectData clickData)
    {
        if (clickData.isShiftClick)
        {
            this.te.aspectFilter = null;
            this.te.writeAspectFilter(null);
            this.writeUpdateInfo(203, packetBuffer -> {
                packetBuffer.writeString("");
            });
        }   else
        {
            this.te.aspectFilter = Aspect.getAspect(clickData.aspect != null ? clickData.aspect.getTag() : "");
            this.te.writeAspectFilter(Aspect.getAspect(clickData.aspect != null ? clickData.aspect.getTag() : ""));
            this.writeUpdateInfo(203, packetBuffer -> {
                packetBuffer.writeString(clickData.aspect != null ? clickData.aspect.getTag() : "");
            });
        }
    }

    public void changePage(int page)
    {
        for (List<AspectImageWidget> aspectImagePageWidget : this.aspectImagePageWidgets) {
            for (AspectImageWidget imageWidget : aspectImagePageWidget) {
                imageWidget.setActive(false);
                imageWidget.setVisible(false);
            }
        }

        for (AspectImageWidget imageWidget : this.aspectImagePageWidgets.get(page)) {
            imageWidget.setActive(true);
            imageWidget.setVisible(true);
        }
    }

    public void changePageLeft(ClickData clickData)
    {
        if (this.page > 0)
        {
            this.writeUpdateInfo(202, packetBuffer -> {
                packetBuffer.writeInt(--this.page);
            });
        }
    }

    public void changePageRight(ClickData clickData)
    {
        if (this.page < this.pages - 1)
        {
            this.writeUpdateInfo(202, packetBuffer -> {
                packetBuffer.writeInt(++this.page);
            });
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 202) {
            int newPage = buffer.readInt();
            this.changePage(newPage);
        }   else if (id == 203)
        {
            String aspectS = buffer.readString(100);
            for (List<AspectImageWidget> aspectImagePageWidget : this.aspectImagePageWidgets) {
                for (AspectImageWidget imageWidget : aspectImagePageWidget) {
                    if (Objects.equals(imageWidget.getAspect().getTag(), aspectS))
                    {
                        imageWidget.enableColor(true);
                    }   else
                    {
                        imageWidget.enableColor(false);
                    }
                }
            }
        }
        else {
            super.readUpdateInfo(id, buffer);
        }
    }
}

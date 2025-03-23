package com.zhenzi.zhenzigtqt.common.lib.aspect;

import gregtech.api.capability.INotifiableHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraftforge.fluids.FluidTank;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotifiableAspectTank extends AspectTank implements INotifiableHandler {
    List<MetaTileEntity> notifiableEntities = new ArrayList();
    private final boolean isExport;

    public NotifiableAspectTank(int capacity, MetaTileEntity entityToNotify, boolean isExport) {
        super(capacity);
        this.notifiableEntities.add(entityToNotify);
        this.isExport = isExport;
    }

    protected void onContentsChanged() {
        super.onContentsChanged();

        for (MetaTileEntity metaTileEntity : this.notifiableEntities) {
            if (metaTileEntity != null && metaTileEntity.isValid()) {
                this.addToNotifiedList(metaTileEntity, this, this.isExport);
            }
        }

    }

    public void addNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        this.notifiableEntities.add(metaTileEntity);
    }

    public void removeNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        this.notifiableEntities.remove(metaTileEntity);
    }
}

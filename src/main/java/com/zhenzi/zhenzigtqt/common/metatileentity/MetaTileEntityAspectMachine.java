package com.zhenzi.zhenzigtqt.common.metatileentity;

import com.zhenzi.zhenzigtqt.common.lib.aspect.*;
import gregtech.api.capability.IActiveOutputSide;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTTransferUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

public class MetaTileEntityAspectMachine extends MetaTileEntity implements ITieredMetaTileEntity, IActiveOutputSide, IFastRenderMetaTileEntity {
    private final int tier;
    protected IAspectHandler aspectInventory;
    protected AspectTankList importAspects;
    protected AspectTankList exportAspects;

    public MetaTileEntityAspectMachine(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId);
        this.tier = tier;
        this.initializeInventory();
    }

    protected void initializeInventory() {
        super.initializeInventory();
        this.importAspects = this.createImportAspectHandler();
        this.exportAspects = this.createExportAspectHandler();
        this.aspectInventory = new AspectHandlerProxy(this.importAspects, this.exportAspects);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return null;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    public void fillContainerFromInternalTank() {
        this.fillContainerFromInternalTank(this.exportAspects);
    }

    public void fillContainerFromInternalTank(IAspectHandler fluidHandler) {
        for(int i = 0; i < this.importItems.getSlots(); ++i) {
            ItemStack emptyContainer = this.importItems.extractItem(i, 1, true);
            AspectActionResult result = AspectUtil.tryFillContainer(emptyContainer, fluidHandler, Integer.MAX_VALUE, (EntityPlayer)null, false);
            if (result.isSuccess()) {
                ItemStack remainingItem = result.getResult();
                if (remainingItem.isEmpty() || GTTransferUtils.insertItem(this.exportItems, remainingItem, true).isEmpty()) {
                    AspectUtil.tryFillContainer(emptyContainer, fluidHandler, Integer.MAX_VALUE, (EntityPlayer)null, true);
                    this.importItems.extractItem(i, 1, false);
                    GTTransferUtils.insertItem(this.exportItems, remainingItem, false);
                }
            }
        }

    }

    public void fillInternalTankFromAspectContainer() {
        this.fillInternalTankFromAspectContainer(this.importAspects);
    }

    public void fillInternalTankFromAspectContainer(IAspectHandler fluidHandler) {
        for(int i = 0; i < this.importItems.getSlots(); ++i) {
            ItemStack inputContainerStack = this.importItems.extractItem(i, 1, true);
            AspectActionResult result = AspectUtil.tryEmptyContainer(inputContainerStack, fluidHandler, Integer.MAX_VALUE, (EntityPlayer)null, false);
            if (result.isSuccess()) {
                ItemStack remainingItem = result.getResult();
                if (!ItemStack.areItemStacksEqual(inputContainerStack, remainingItem) && (remainingItem.isEmpty() || GTTransferUtils.insertItem(this.exportItems, remainingItem, true).isEmpty())) {
                    AspectUtil.tryEmptyContainer(inputContainerStack, fluidHandler, Integer.MAX_VALUE, (EntityPlayer)null, true);
                    this.importItems.extractItem(i, 1, false);
                    GTTransferUtils.insertItem(this.exportItems, remainingItem, false);
                }
            }
        }

    }

    public void pushAspectsIntoNearbyHandlers(EnumFacing... allowedFaces) {
        this.transferToNearby(CapabilityAspectHandler.ASPECT_HANDLER_CAPABILITY, AspectUtil::transferAspects, allowedFaces);
    }

    private <T> void transferToNearby(Capability<T> capability, BiConsumer<T, T> transfer, EnumFacing... allowedFaces) {
        EnumFacing[] var4 = allowedFaces;
        int var5 = allowedFaces.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            EnumFacing nearbyFacing = var4[var6];
            TileEntity tileEntity = this.getNeighbor(nearbyFacing);
            if (tileEntity != null) {
                T otherCap = tileEntity.getCapability(capability, nearbyFacing.getOpposite());
                T thisCap = this.getCoverCapability(capability, nearbyFacing);
                if (otherCap != null && thisCap != null) {
                    transfer.accept(thisCap, otherCap);
                }
            }
        }

    }

    protected AspectTankList createImportAspectHandler() {
        return new AspectTankList(false);
    }

    protected AspectTankList createExportAspectHandler() {
        return new AspectTankList(false);
    }

    @Override
    public boolean isAutoOutputItems() {
        return false;
    }

    @Override
    public boolean isAutoOutputFluids() {
        return false;
    }

    @Override
    public boolean isAllowInputFromOutputSideItems() {
        return false;
    }

    @Override
    public boolean isAllowInputFromOutputSideFluids() {
        return false;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return null;
    }

    @Override
    public int getTier() {
        return this.tier;
    }
}

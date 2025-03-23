package com.zhenzi.zhenzigtqt.common.metatileentity;

import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Matrix4;
import com.zhenzi.zhenzigtqt.common.lib.aspect.*;
import com.zhenzi.zhenzigtqt.loaders.AbstractAspectRecipeLogic;
import com.zhenzi.zhenzigtqt.loaders.IVoidableAspect;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.*;
import gregtech.api.cover.CoverSaveHandler;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Mods;
import gregtech.integration.jei.multiblock.MultiblockInfoCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;

public class AspectMetaTileEntity extends MetaTileEntity implements IVoidableAspect {
    protected AspectTankList importAspects;
    protected AspectTankList exportAspects;
    protected IAspectHandler aspectInventory;
    protected List<IAspectHandler> notifiedAspectInputList;
    protected List<IAspectHandler> notifiedAspectOutputList;

    public AspectMetaTileEntity(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.notifiedAspectInputList = new ArrayList();
        this.notifiedAspectOutputList = new ArrayList();
    }

    protected void initializeInventory() {
        this.importItems = this.createImportItemHandler();
        this.exportItems = this.createExportItemHandler();
        this.itemInventory = new ItemHandlerProxy(this.importItems, this.exportItems);
        this.importFluids = this.createImportFluidHandler();
        this.exportFluids = this.createExportFluidHandler();
        this.fluidInventory = new FluidHandlerProxy(this.importFluids, this.exportFluids);
        this.importAspects = this.createImportAspectHandler();
        this.exportAspects = this.createExportAspectHandler();
        this.aspectInventory = new AspectHandlerProxy(this.importAspects, this.exportAspects);
    }

    public static <T extends AspectMetaTileEntity> T registerMetaTileEntityA(int id, T sampleMetaTileEntity) {
        GregTechAPI.MTE_REGISTRY.register(id, sampleMetaTileEntity.metaTileEntityId, sampleMetaTileEntity);
        return sampleMetaTileEntity;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return null;
    }

    public boolean canVoidRecipeAspectOutputs() {
        return false;
    }

    @Override
    public int getAspectOutputLimit() {
        return IVoidableAspect.super.getAspectOutputLimit();
    }

    public <T> void addNotifiedInput(T input) {
        if (input instanceof IItemHandlerModifiable) {
            if (!this.notifiedItemInputList.contains(input)) {
                this.notifiedItemInputList.add((IItemHandlerModifiable)input);
            }
        } else if (input instanceof IFluidHandler && !this.notifiedFluidInputList.contains(input)) {
            this.notifiedFluidInputList.add((IFluidHandler)input);
        } else if (input instanceof IAspectHandler && !this.notifiedAspectInputList.contains(input))
        {
            this.notifiedAspectInputList.add((IAspectHandler) input);
        }

    }

    public <T> void addNotifiedOutput(T output) {
        if (output instanceof IItemHandlerModifiable) {
            if (!this.notifiedItemOutputList.contains(output)) {
                this.notifiedItemOutputList.add((IItemHandlerModifiable)output);
            }
        } else if (output instanceof NotifiableFluidTank && !this.notifiedFluidOutputList.contains(output)) {
            this.notifiedFluidOutputList.add((NotifiableFluidTank)output);
        } else if (output instanceof NotifiableAspectTank && !this.notifiedAspectOutputList.contains(output)) {
            this.notifiedAspectOutputList.add((NotifiableAspectTank)output);
        }
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    protected AspectTankList createImportAspectHandler() {
        return new AspectTankList(false);
    }

    protected AspectTankList createExportAspectHandler() {
        return new AspectTankList(false);
    }

    public IAspectHandler getAspectInventory() {
        return this.aspectInventory;
    }

    public AspectTankList getImportAspects() {
        return this.importAspects;
    }

    public List<IAspectHandler> getNotifiedAspectInputList() {
        return this.notifiedAspectInputList;
    }

    public List<IAspectHandler> getNotifiedAspectOutputList() {
        return this.notifiedAspectOutputList;
    }

    public AspectTankList getExportAspects() {
        return this.exportAspects;
    }

    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityAspectHandler.ASPECT_HANDLER_CAPABILITY && this.getAspectInventory().getTankProperties().length > 0) {
            return CapabilityAspectHandler.ASPECT_HANDLER_CAPABILITY.cast(this.getAspectInventory());
        }
        return super.getCapability(capability, side);
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

    public void fillContainerFromInternalAspectTank() {
        this.fillContainerFromInternalAspectTank(this.exportAspects);
    }

    public void fillContainerFromInternalAspectTank(IAspectHandler fluidHandler) {
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

    public void pushAspectsIntoNearbyHandlers(EnumFacing... allowedFaces) {
        this.transferToNearby(CapabilityAspectHandler.ASPECT_HANDLER_CAPABILITY, AspectUtil::transferAspects, allowedFaces);
    }

    public void pullAspectsFromNearbyHandlers(EnumFacing... allowedFaces) {
        this.transferToNearby(CapabilityAspectHandler.ASPECT_HANDLER_CAPABILITY, (thisCap, otherCap) -> {
            AspectUtil.transferAspects(otherCap, thisCap);
        }, allowedFaces);
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

    public NBTTagCompound writeToNBT(NBTTagCompound data) {

        if (this.shouldSerializeInventories()) {
            data.setTag("ImportAspectInventory", this.importAspects.serializeNBT());
            data.setTag("ExportAspectInventory", this.exportAspects.serializeNBT());
        }

        return super.writeToNBT(data);
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (this.shouldSerializeInventories()) {
            this.importAspects.deserializeNBT(data.getCompoundTag("ImportAspectInventory"));
            this.exportAspects.deserializeNBT(data.getCompoundTag("ExportAspectInventory"));
        }
    }

    public final @Nullable AbstractAspectRecipeLogic getAspectRecipeLogic() {
        MTETrait trait = this.getMTETrait("AspectRecipeMapWorkable");
        if (trait instanceof AbstractAspectRecipeLogic) {
            return (AbstractAspectRecipeLogic)trait;
        } else if (trait != null) {
            throw new IllegalStateException("MTE Trait " + trait.getName() + " has name " + "AspectRecipeMapWorkable" + " but is not instanceof AbstractRecipeLogic");
        } else {
            return null;
        }
    }

    @Override
    public void dropAllCovers() {
        super.dropAllCovers();
    }

    @Override
    public void dropCover(@NotNull EnumFacing side) {
        super.dropCover(side);
    }

    @Override
    public void updateCovers() {
        super.updateCovers();
    }

    @Override
    public void renderCovers(@NotNull CCRenderState renderState, @NotNull Matrix4 translation, @NotNull BlockRenderLayer layer) {
        super.renderCovers(renderState, translation, layer);
    }

    @Override
    public void addCoverCollisionBoundingBox(@NotNull List<? super IndexedCuboid6> collisionList) {
        super.addCoverCollisionBoundingBox(collisionList);
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        return super.hasCapability(capability, facing);
    }

    @Override
    public boolean hasCover(@NotNull EnumFacing side) {
        return super.hasCover(side);
    }

    @Override
    public int getItemOutputLimit() {
        return super.getItemOutputLimit();
    }

    @Override
    public int getFluidOutputLimit() {
        return super.getFluidOutputLimit();
    }
}

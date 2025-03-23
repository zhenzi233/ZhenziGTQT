package com.zhenzi.zhenzigtqt.common.metatileentity;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectTank;
import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectTankList;
import com.zhenzi.zhenzigtqt.common.lib.aspect.NotifiableAspectTank;
import com.zhenzi.zhenzigtqt.loaders.AbstractAspectRecipeLogic;
import com.zhenzi.zhenzigtqt.loaders.AspectRecipeLogicEnergy;
import com.zhenzi.zhenzigtqt.loaders.AspectRecipeMap;
import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.*;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.multiblock.ICleanroomProvider;
import gregtech.api.metatileentity.multiblock.ICleanroomReceiver;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class AspectWorkableTieredMetaTileEntity extends AspectTieredMetaTileEntity implements IDataInfoProvider, ICleanroomReceiver {
    protected final AbstractAspectRecipeLogic workable;
    protected final AspectRecipeMap<?> recipeMap;
    protected final ICubeRenderer renderer;
    private final Function<Integer, Integer> tankScalingFunction;
    public final boolean handlesRecipeOutputs;
    private ICleanroomProvider cleanroom;
//    protected IEnergyContainer energyContainer;

    public AspectWorkableTieredMetaTileEntity(ResourceLocation metaTileEntityId, AspectRecipeMap<?> recipeMap, ICubeRenderer renderer, int tier, Function<Integer, Integer> tankScalingFunction) {
        this(metaTileEntityId, recipeMap, renderer, tier, tankScalingFunction, true);
    }

    public AspectWorkableTieredMetaTileEntity(ResourceLocation metaTileEntityId, AspectRecipeMap<?> recipeMap, ICubeRenderer renderer, int tier, Function<Integer, Integer> tankScalingFunction, boolean handlesRecipeOutputs) {
        super(metaTileEntityId, tier);
        this.renderer = renderer;
        this.handlesRecipeOutputs = handlesRecipeOutputs;
        this.workable = this.createWorkable(recipeMap);
        this.recipeMap = recipeMap;
        this.tankScalingFunction = tankScalingFunction;
        this.initializeInventory();
        this.reinitializeEnergyContainer();
    }

    protected AbstractAspectRecipeLogic createWorkable(AspectRecipeMap<?> recipeMap) {
        return new AspectRecipeLogicEnergy(this, recipeMap, () -> {
            return this.energyContainer;
        });
    }

    protected void reinitializeEnergyContainer() {
        long tierVoltage = GTValues.V[this.getTier()];
        if (this.isEnergyEmitter()) {
            this.energyContainer = EnergyContainerHandler.emitterContainer(this, tierVoltage * 64L, tierVoltage, this.getMaxInputOutputAmperage());
        } else {
            this.energyContainer = new EnergyContainerHandler(this, tierVoltage * 64L, tierVoltage, 2L, 0L, 0L) {
                public long getInputAmperage() {
                    return this.getEnergyCapacity() / 2L > this.getEnergyStored() && AspectWorkableTieredMetaTileEntity.this.workable.isActive() ? 2L : 1L;
                }
            };
        }

    }

    protected long getMaxInputOutputAmperage() {
        return 2L;
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.renderer.renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(), this.workable.isActive(), this.workable.isWorkingEnabled());
    }

    protected IItemHandlerModifiable createImportItemHandler() {
        return (IItemHandlerModifiable)(this.workable == null ? new GTItemStackHandler(this, 0) : new NotifiableItemStackHandler(this, this.workable.getRecipeMap().getMaxInputs(), this, false));
    }

    protected IItemHandlerModifiable createExportItemHandler() {
        return (IItemHandlerModifiable)(this.workable == null ? new GTItemStackHandler(this, 0) : new NotifiableItemStackHandler(this, this.workable.getRecipeMap().getMaxOutputs(), this, true));
    }

    protected FluidTankList createImportFluidHandler() {
        if (this.workable == null) {
            return new FluidTankList(false);
        } else {
            NotifiableFluidTank[] fluidImports = new NotifiableFluidTank[this.workable.getRecipeMap().getMaxFluidInputs()];

            for(int i = 0; i < fluidImports.length; ++i) {
                NotifiableFluidTank filteredFluidHandler = new NotifiableFluidTank((Integer)this.tankScalingFunction.apply(this.getTier()), this, false);
                fluidImports[i] = filteredFluidHandler;
            }

            return new FluidTankList(false, fluidImports);
        }
    }

    protected FluidTankList createExportFluidHandler() {
        if (this.workable == null) {
            return new FluidTankList(false, new IFluidTank[0]);
        } else {
            FluidTank[] fluidExports = new FluidTank[this.workable.getRecipeMap().getMaxFluidOutputs()];

            for(int i = 0; i < fluidExports.length; ++i) {
                fluidExports[i] = new NotifiableFluidTank((Integer)this.tankScalingFunction.apply(this.getTier()), this, true);
            }

            return new FluidTankList(false, fluidExports);
        }
    }

    protected AspectTankList createImportAspectHandler() {
        if (this.workable == null) {
            return new AspectTankList(false);
        } else {
            NotifiableAspectTank[] fluidImports = new NotifiableAspectTank[this.workable.getRecipeMap().getMaxAspectInputs()];

            for(int i = 0; i < fluidImports.length; ++i) {
                NotifiableAspectTank filteredFluidHandler = new NotifiableAspectTank((Integer)this.tankScalingFunction.apply(this.getTier()), this, false);
                fluidImports[i] = filteredFluidHandler;
            }

            return new AspectTankList(false, fluidImports);
        }
    }

    protected AspectTankList createExportAspectHandler() {
        if (this.workable == null) {
            return new AspectTankList(false);
        } else {
            AspectTank[] fluidExports = new AspectTank[this.workable.getRecipeMap().getMaxAspectOutputs()];

            for(int i = 0; i < fluidExports.length; ++i) {
                fluidExports[i] = new NotifiableAspectTank((Integer)this.tankScalingFunction.apply(this.getTier()), this, true);
            }

            return new AspectTankList(false, fluidExports);
        }
    }

    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", this.energyContainer.getInputVoltage(), GTValues.VNF[this.getTier()]));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", this.energyContainer.getEnergyCapacity()));
        if (this.workable.getRecipeMap().getMaxFluidInputs() != 0) {
            tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity", this.tankScalingFunction.apply(this.getTier())));
        }

    }

    public Function<Integer, Integer> getTankScalingFunction() {
        return this.tankScalingFunction;
    }

    public boolean isActive() {
        return this.workable.isActive() && this.workable.isWorkingEnabled();
    }

    public SoundEvent getSound() {
        return this.workable.getRecipeMap().getSound();
    }

    public @NotNull List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = new ArrayList();
        if (this.workable != null) {
            list.add(new TextComponentTranslation("behavior.tricorder.workable_progress", (new TextComponentTranslation(TextFormattingUtil.formatNumbers((long)(this.workable.getProgress() / 20))))
                    .setStyle((new Style())
                            .setColor(TextFormatting.GREEN)), (new TextComponentTranslation(TextFormattingUtil.formatNumbers((long)(this.workable.getMaxProgress() / 20)), new Object[0]))
                    .setStyle((new Style()).setColor(TextFormatting.YELLOW))));
            if (this.energyContainer != null) {
                list.add(new TextComponentTranslation("behavior.tricorder.workable_stored_energy", (new TextComponentTranslation(TextFormattingUtil.formatNumbers(this.energyContainer.getEnergyStored()))).setStyle((new Style()).setColor(TextFormatting.GREEN)), (new TextComponentTranslation(TextFormattingUtil.formatNumbers(this.energyContainer.getEnergyCapacity()), new Object[0])).setStyle((new Style()).setColor(TextFormatting.YELLOW))));
            }

            if (this.workable.consumesEnergy()) {
                list.add(new TextComponentTranslation("behavior.tricorder.workable_consumption", (new TextComponentTranslation(TextFormattingUtil.formatNumbers((long)this.workable.getInfoProviderEUt()))).setStyle((new Style()).setColor(TextFormatting.RED)), (new TextComponentTranslation(TextFormattingUtil.formatNumbers(this.workable.getInfoProviderEUt() == 0 ? 0L : 1L), new Object[0])).setStyle((new Style()).setColor(TextFormatting.RED))));
            } else {
                list.add(new TextComponentTranslation("behavior.tricorder.workable_production", (new TextComponentTranslation(TextFormattingUtil.formatNumbers((long)this.workable.getInfoProviderEUt()))).setStyle((new Style()).setColor(TextFormatting.RED)), (new TextComponentTranslation(TextFormattingUtil.formatNumbers(this.workable.getInfoProviderEUt() == 0 ? 0L : 1L), new Object[0])).setStyle((new Style()).setColor(TextFormatting.RED))));
            }
        }

        return list;
    }

    public @Nullable ICleanroomProvider getCleanroom() {
        return this.cleanroom;
    }

    public void setCleanroom(ICleanroomProvider provider) {
        this.cleanroom = provider;
    }
}


package com.zhenzi.zhenzigtqt.common.metatileentity;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.zhenzi.zhenzigtqt.client.gui.QuantumAspectTank.AspectTankWidget;
import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectTankList;
import com.zhenzi.zhenzigtqt.common.lib.aspect.CapabilityAspectHandler;
import com.zhenzi.zhenzigtqt.common.lib.aspect.IAspectHandler;
import com.zhenzi.zhenzigtqt.loaders.AbstractAspectRecipeLogic;
import com.zhenzi.zhenzigtqt.loaders.AspectFuelRecipeLogic;
import com.zhenzi.zhenzigtqt.loaders.AspectRecipeLogicEnergy;
import com.zhenzi.zhenzigtqt.loaders.AspectRecipeMap;
import gregtech.api.GTValues;
import gregtech.api.capability.IActiveOutputSide;
import gregtech.api.capability.impl.*;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleGeneratorMetaTileEntity;
import gregtech.api.metatileentity.WorkableTieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.PipelineUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class MetaTileEntityAspectGenerator extends AspectWorkableTieredMetaTileEntity implements IActiveOutputSide {
    private static final int FONT_HEIGHT = 9;

    public MetaTileEntityAspectGenerator(ResourceLocation metaTileEntityId, AspectRecipeMap<?> recipeMap, ICubeRenderer renderer, int tier, Function<Integer, Integer> tankScalingFunction) {
        this(metaTileEntityId, recipeMap, renderer, tier, tankScalingFunction, false);
    }

    public MetaTileEntityAspectGenerator(ResourceLocation metaTileEntityId, AspectRecipeMap<?> recipeMap, ICubeRenderer renderer, int tier, Function<Integer, Integer> tankScalingFunction, boolean handlesRecipeOutputs) {
        super(metaTileEntityId, recipeMap, renderer, tier, tankScalingFunction, handlesRecipeOutputs);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityAspectGenerator(super.metaTileEntityId, super.workable.getRecipeMap(), super.renderer, super.getTier(), super.getTankScalingFunction(), super.handlesRecipeOutputs);
    }

    protected AspectRecipeLogicEnergy createWorkable(AspectRecipeMap<?> recipeMap) {
        return new AspectFuelRecipeLogic(this, recipeMap, () -> {
            return super.energyContainer;
        });
    }

    protected FluidTankList createExportFluidHandler() {
        return super.handlesRecipeOutputs ? super.createExportFluidHandler() : new FluidTankList(false, new IFluidTank[0]);
    }

    protected void reinitializeEnergyContainer() {
        super.reinitializeEnergyContainer();
        ((EnergyContainerHandler)super.energyContainer).setSideOutputCondition((side) -> {
            return side == super.getFrontFacing();
        });
    }

    public boolean hasFrontFacing() {
        return true;
    }

    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return super.fluidInventory.getTankProperties().length > 0 ? CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(super.fluidInventory) : null;
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return super.itemInventory.getSlots() > 0 ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(super.itemInventory) : null;
        } else if (capability == CapabilityAspectHandler.ASPECT_HANDLER_CAPABILITY) {
            return super.aspectInventory.getTankProperties().length > 0 ? CapabilityAspectHandler.ASPECT_HANDLER_CAPABILITY.cast(super.aspectInventory) : null;
        }else {
            return super.getCapability(capability, side);
        }
    }

    protected ModularUI.Builder createGuiTemplate(EntityPlayer player) {
        AspectRecipeMap<?> workableRecipeMap = super.workable.getRecipeMap();
        int yOffset = 0;
        if (workableRecipeMap.getMaxInputs() >= 6 || workableRecipeMap.getMaxFluidInputs() >= 6 || workableRecipeMap.getMaxOutputs() >= 6 || workableRecipeMap.getMaxFluidOutputs() >= 6) {
            yOffset = 9;
        }

        AbstractAspectRecipeLogic var10001;
        ModularUI.Builder builder;
        if (super.handlesRecipeOutputs) {
            var10001 = super.workable;
            Objects.requireNonNull(var10001);
            builder = workableRecipeMap.createUITemplate(var10001::getProgressPercent, super.importItems, super.exportItems, super.importFluids, super.exportFluids, super.importAspects, super.exportAspects, yOffset);
        } else {
            var10001 = super.workable;
            Objects.requireNonNull(var10001);
            builder = workableRecipeMap.createUITemplateNoOutputs(var10001::getProgressPercent, super.importItems, super.exportItems, super.importFluids, super.exportFluids, super.importAspects, super.exportAspects, yOffset);
        }

        builder.widget(new LabelWidget(6, 6, super.getMetaFullName())).bindPlayerInventory(player.inventory, GuiTextures.SLOT, yOffset);
        return builder;
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        renderOverlays(renderState, translation, pipeline);
        Textures.ENERGY_OUT.renderSided(super.getFrontFacing(), renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[super.getTier()]));
    }

    protected void renderOverlays(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderer.renderOrientedState(renderState, translation, pipeline, super.getFrontFacing(), super.workable.isActive(), super.workable.isWorkingEnabled());
    }

    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createGuiTemplate(entityPlayer).build(super.getHolder(), entityPlayer);
    }

    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip, boolean advanced) {
        String key = super.metaTileEntityId.getPath().split("\\.")[0];
        String mainKey = String.format("gregtech.machine.%s.tooltip", key);
        if (I18n.hasKey(mainKey)) {
            tooltip.add(1, I18n.format(mainKey, new Object[0]));
        }

        tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_out", super.energyContainer.getOutputVoltage(), GTValues.VNF[super.getTier()]));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", super.energyContainer.getEnergyCapacity()));
        if (super.recipeMap.getMaxFluidInputs() > 0 || super.recipeMap.getMaxFluidOutputs() > 0) {
            tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity", super.getTankScalingFunction().apply(super.getTier())));
        }

    }

    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing", new Object[0]));
        tooltip.add(I18n.format("gregtech.tool_action.soft_mallet.reset", new Object[0]));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    public boolean isAutoOutputItems() {
        return false;
    }

    public boolean isAutoOutputFluids() {
        return false;
    }

    public boolean isAllowInputFromOutputSideItems() {
        return false;
    }

    public boolean isAllowInputFromOutputSideFluids() {
        return false;
    }

    protected long getMaxInputOutputAmperage() {
        return 1L;
    }

    protected boolean isEnergyEmitter() {
        return true;
    }

    public boolean canVoidRecipeItemOutputs() {
        return !super.handlesRecipeOutputs;
    }

    public boolean canVoidRecipeFluidOutputs() {
        return !super.handlesRecipeOutputs;
    }
}

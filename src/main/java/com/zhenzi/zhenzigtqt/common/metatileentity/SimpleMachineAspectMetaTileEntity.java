package com.zhenzi.zhenzigtqt.common.metatileentity;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.zhenzi.zhenzigtqt.client.render.texture.ZZTextures;
import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectHandlerProxy;
import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectTankList;
import com.zhenzi.zhenzigtqt.common.lib.aspect.CapabilityAspectHandler;
import com.zhenzi.zhenzigtqt.common.lib.aspect.IAspectHandler;
import com.zhenzi.zhenzigtqt.loaders.AbstractAspectRecipeLogic;
import com.zhenzi.zhenzigtqt.loaders.AspectRecipeMap;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IActiveOutputSide;
import gregtech.api.capability.IGhostSlotConfigurable;
import gregtech.api.capability.impl.*;
import gregtech.api.cover.Cover;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.*;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.WorkableTieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.client.particle.IMachineParticleEffect;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.RenderUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntSupplier;

import static gregtech.api.metatileentity.MetaTileEntity.clearInventory;

public class SimpleMachineAspectMetaTileEntity extends AspectWorkableTieredMetaTileEntity implements IActiveOutputSide, IGhostSlotConfigurable {
    private final boolean hasFrontFacing;
    protected final GTItemStackHandler chargerInventory;
    protected @Nullable GhostCircuitItemStackHandler circuitInventory;
    private EnumFacing outputFacingItems;
    private EnumFacing outputFacingFluids;
    private EnumFacing outputFacingAspects;
    private boolean autoOutputItems;
    private boolean autoOutputFluids;
    private boolean autoOutputAspects;
    private boolean allowInputFromOutputSideItems;
    private boolean allowInputFromOutputSideFluids;
    private boolean allowInputFromOutputSideAspects;
    protected IItemHandler outputItemInventory;
    protected IFluidHandler outputFluidInventory;

    protected IAspectHandler outputAspectInventory;
    private IItemHandlerModifiable actualImportItems;
    private static final int FONT_HEIGHT = 9;
    protected final @Nullable IMachineParticleEffect tickingParticle;
    protected final @Nullable IMachineParticleEffect randomParticle;

    public SimpleMachineAspectMetaTileEntity(ResourceLocation metaTileEntityId, AspectRecipeMap<?> recipeMap, ICubeRenderer renderer, int tier, boolean hasFrontFacing) {
        this(metaTileEntityId, recipeMap, renderer, tier, hasFrontFacing, GTUtility.defaultTankSizeFunction);
    }

    public SimpleMachineAspectMetaTileEntity(ResourceLocation metaTileEntityId, AspectRecipeMap<?> recipeMap, ICubeRenderer renderer, int tier, boolean hasFrontFacing, Function<Integer, Integer> tankScalingFunction) {
        this(metaTileEntityId, recipeMap, renderer, tier, hasFrontFacing, tankScalingFunction, (IMachineParticleEffect)null, (IMachineParticleEffect)null);
    }

    public SimpleMachineAspectMetaTileEntity(ResourceLocation metaTileEntityId, AspectRecipeMap<?> recipeMap, ICubeRenderer renderer, int tier, boolean hasFrontFacing, Function<Integer, Integer> tankScalingFunction, @Nullable IMachineParticleEffect tickingParticle, @Nullable IMachineParticleEffect randomParticle) {
        super(metaTileEntityId, recipeMap, renderer, tier, tankScalingFunction);
        this.allowInputFromOutputSideItems = false;
        this.allowInputFromOutputSideFluids = false;
        this.hasFrontFacing = hasFrontFacing;
        this.chargerInventory = new GTItemStackHandler(this, 1);
        this.tickingParticle = tickingParticle;
        this.randomParticle = randomParticle;
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new SimpleMachineAspectMetaTileEntity(super.metaTileEntityId, super.workable.getRecipeMap(), super.renderer, super.getTier(), this.hasFrontFacing, super.getTankScalingFunction(), this.tickingParticle, this.randomParticle);
    }

    protected void initializeInventory() {
        super.initializeInventory();
        this.outputItemInventory = new ItemHandlerProxy(new GTItemStackHandler(this, 0), super.exportItems);
        this.outputFluidInventory = new FluidHandlerProxy(new FluidTankList(false), super.exportFluids);
        this.outputAspectInventory = new AspectHandlerProxy(new AspectTankList(false), super.exportAspects);
        if (hasGhostCircuitInventory()) {
            this.circuitInventory = new GhostCircuitItemStackHandler(this);
            this.circuitInventory.addNotifiableMetaTileEntity(this);
        }

        this.actualImportItems = null;

    }

    public IItemHandlerModifiable getImportItems() {
        if (this.actualImportItems == null) {
            this.actualImportItems = (IItemHandlerModifiable)(this.circuitInventory == null ? super.getImportItems() : new ItemHandlerList(Arrays.asList(super.getImportItems(), this.circuitInventory)));
        }

        return this.actualImportItems;
    }

    public boolean hasFrontFacing() {
        return this.hasFrontFacing;
    }

    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            if (getOutputFacing() == facing) {
                return false;
            } else if (hasFrontFacing() && facing == super.getFrontFacing()) {
                return false;
            } else {
                if (!super.getWorld().isRemote) {
                    setOutputFacing(facing);
                }

                return true;
            }
        } else {
            return super.onWrenchClick(playerIn, hand, facing, hitResult);
        }
    }

    public void addCover(@NotNull EnumFacing side, @NotNull Cover cover) {
        super.addCover(side, cover);
        if (cover.canInteractWithOutputSide()) {
            if (getOutputFacingItems() == side) {
                setAllowInputFromOutputSideItems(true);
            }

            if (getOutputFacingFluids() == side) {
                setAllowInputFromOutputSideFluids(true);
            }

            if (getOutputFacingAspects() == side) {
                setAllowInputFromOutputSideAspects(true);
            }
        }

    }

    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.outputAspectInventory != null && super.getExportAspects().getTanks() > 0) {
            Textures.PIPE_OUT_OVERLAY.renderSided(this.outputFacingAspects, renderState, RenderUtil.adjustTrans(translation, this.outputFacingAspects, 2), pipeline);
        }

        if (this.outputFacingFluids != null && super.getExportFluids().getTanks() > 0) {
            Textures.PIPE_OUT_OVERLAY.renderSided(this.outputFacingFluids, renderState, RenderUtil.adjustTrans(translation, this.outputFacingFluids, 2), pipeline);
        }

        if (this.outputFacingItems != null && super.getExportItems().getSlots() > 0) {
            Textures.PIPE_OUT_OVERLAY.renderSided(this.outputFacingItems, renderState, RenderUtil.adjustTrans(translation, this.outputFacingItems, 2), pipeline);
        }

        if (isAutoOutputItems() && this.outputFacingItems != null) {
            Textures.ITEM_OUTPUT_OVERLAY.renderSided(this.outputFacingItems, renderState, RenderUtil.adjustTrans(translation, this.outputFacingItems, 2), pipeline);
        }

        if (isAutoOutputFluids() && this.outputFacingFluids != null) {
            Textures.FLUID_OUTPUT_OVERLAY.renderSided(this.outputFacingFluids, renderState, RenderUtil.adjustTrans(translation, this.outputFacingFluids, 2), pipeline);
        }

        if (isAutoOutputAspects() && this.outputFacingAspects != null) {
            ZZTextures.PIPE_ASPECT_OUT_OVERLAY.renderSided(this.outputFacingAspects, renderState, RenderUtil.adjustTrans(translation, this.outputFacingAspects, 2), pipeline);
        }

    }

    public void update() {
        super.update();
        if (!super.getWorld().isRemote) {
            ((EnergyContainerHandler)super.energyContainer).dischargeOrRechargeEnergyContainers(this.chargerInventory, 0);
            if (super.getOffsetTimer() % 5L == 0L) {
                if (isAutoOutputAspects()) {
                    super.pushAspectsIntoNearbyHandlers(getOutputFacingAspects());
                }

                if (isAutoOutputFluids()) {
                    super.pushFluidsIntoNearbyHandlers(getOutputFacingFluids());
                }

                if (isAutoOutputItems()) {
                    super.pushItemsIntoNearbyHandlers(getOutputFacingItems());
                }
            }
        } else if (this.tickingParticle != null && super.isActive()) {
            this.tickingParticle.runEffect(this);
        }

    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick() {
        if (this.randomParticle != null && super.isActive()) {
            this.randomParticle.runEffect(this);
        }

    }

    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!super.getWorld().isRemote) {
            if (isAllowInputFromOutputSideItems()) {
                setAllowInputFromOutputSideItems(false);
                setAllowInputFromOutputSideFluids(false);
                setAllowInputFromOutputSideAspects(false);
                playerIn.sendStatusMessage(new TextComponentTranslation("gregtech.machine.basic.input_from_output_side.disallow", new Object[0]), true);
            } else {
                setAllowInputFromOutputSideItems(true);
                setAllowInputFromOutputSideFluids(true);
                setAllowInputFromOutputSideAspects(true);
                playerIn.sendStatusMessage(new TextComponentTranslation("gregtech.machine.basic.input_from_output_side.allow", new Object[0]), true);
            }
        }

        return true;
    }

    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            IFluidHandler fluidHandler = side == getOutputFacingFluids() && !isAllowInputFromOutputSideFluids() ? this.outputFluidInventory : super.fluidInventory;
            return fluidHandler.getTankProperties().length > 0 ? CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler) : null;
        } else if (capability == CapabilityAspectHandler.ASPECT_HANDLER_CAPABILITY)
        {
            IAspectHandler fluidHandler = side == getOutputFacingAspects() && !isAllowInputFromOutputSideAspects() ? this.outputAspectInventory : super.aspectInventory;
            return fluidHandler.getTankProperties().length > 0 ? CapabilityAspectHandler.ASPECT_HANDLER_CAPABILITY.cast(fluidHandler) : null;
        }
        else if (capability != CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (capability == GregtechTileCapabilities.CAPABILITY_ACTIVE_OUTPUT_SIDE) {
                return side != getOutputFacingItems() && side != getOutputFacingFluids() ? null : GregtechTileCapabilities.CAPABILITY_ACTIVE_OUTPUT_SIDE.cast(this);
            } else {
                return super.getCapability(capability, side);
            }
        } else {
            IItemHandler itemHandler = side == getOutputFacingItems() && !isAllowInputFromOutputSideFluids() ? this.outputItemInventory : super.itemInventory;
            return itemHandler.getSlots() > 0 ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler) : null;
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("ChargerInventory", this.chargerInventory.serializeNBT());
        if (this.circuitInventory != null) {
            this.circuitInventory.write(data);
        }

        data.setInteger("OutputFacing", getOutputFacingItems().getIndex());
        data.setInteger("OutputFacingF", getOutputFacingFluids().getIndex());
        data.setInteger("OutputFacingA", getOutputFacingAspects().getIndex());
        data.setBoolean("AutoOutputItems", this.autoOutputItems);
        data.setBoolean("AutoOutputFluids", this.autoOutputFluids);
        data.setBoolean("AutoOutputAspects", this.autoOutputAspects);
        data.setBoolean("AllowInputFromOutputSide", this.allowInputFromOutputSideItems);
        data.setBoolean("AllowInputFromOutputSideF", this.allowInputFromOutputSideFluids);
        data.setBoolean("AllowInputFromOutputSideA", this.allowInputFromOutputSideAspects);
        return data;
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.chargerInventory.deserializeNBT(data.getCompoundTag("ChargerInventory"));
        if (this.circuitInventory != null) {
            if (data.hasKey("CircuitInventory", 10)) {
                ItemStackHandler legacyCircuitInventory = new ItemStackHandler();
                legacyCircuitInventory.deserializeNBT(data.getCompoundTag("CircuitInventory"));

                for(int i = 0; i < legacyCircuitInventory.getSlots(); ++i) {
                    ItemStack stack = legacyCircuitInventory.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        stack = GTTransferUtils.insertItem(super.importItems, stack, false);
                        this.circuitInventory.setCircuitValueFromStack(stack);
                    }
                }
            } else {
                this.circuitInventory.read(data);
            }
        }

        this.outputFacingItems = EnumFacing.VALUES[data.getInteger("OutputFacing")];
        this.outputFacingFluids = EnumFacing.VALUES[data.getInteger("OutputFacingF")];
        this.outputFacingAspects = EnumFacing.VALUES[data.getInteger("OutputFacingA")];
        this.autoOutputItems = data.getBoolean("AutoOutputItems");
        this.autoOutputFluids = data.getBoolean("AutoOutputFluids");
        this.autoOutputAspects = data.getBoolean("AutoOutputAspects");
        this.allowInputFromOutputSideItems = data.getBoolean("AllowInputFromOutputSide");
        this.allowInputFromOutputSideFluids = data.getBoolean("AllowInputFromOutputSideF");
        this.allowInputFromOutputSideAspects = data.getBoolean("AllowInputFromOutputSideA");
    }

    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(getOutputFacingItems().getIndex());
        buf.writeByte(getOutputFacingFluids().getIndex());
        buf.writeByte(getOutputFacingAspects().getIndex());
        buf.writeBoolean(this.autoOutputItems);
        buf.writeBoolean(this.autoOutputFluids);
        buf.writeBoolean(this.autoOutputAspects);
    }

    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.outputFacingItems = EnumFacing.VALUES[buf.readByte()];
        this.outputFacingFluids = EnumFacing.VALUES[buf.readByte()];
        this.outputFacingAspects = EnumFacing.VALUES[buf.readByte()];
        this.autoOutputItems = buf.readBoolean();
        this.autoOutputFluids = buf.readBoolean();
        this.autoOutputAspects = buf.readBoolean();
    }

    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_OUTPUT_FACING) {
            this.outputFacingItems = EnumFacing.VALUES[buf.readByte()];
            this.outputFacingFluids = EnumFacing.VALUES[buf.readByte()];
            this.outputFacingAspects = EnumFacing.VALUES[buf.readByte()];
            super.scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.UPDATE_AUTO_OUTPUT_ITEMS) {
            this.autoOutputItems = buf.readBoolean();
            super.scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.UPDATE_AUTO_OUTPUT_FLUIDS) {
            this.autoOutputFluids = buf.readBoolean();
            super.scheduleRenderUpdate();
        } else if (dataId == 200) {
            this.autoOutputAspects = buf.readBoolean();
            super.scheduleRenderUpdate();
        }

    }

    public boolean isValidFrontFacing(EnumFacing facing) {
        return super.isValidFrontFacing(facing) && facing != this.outputFacingItems && facing != this.outputFacingFluids && facing != this.outputFacingAspects;
    }

    /** @deprecated */
    @Deprecated
    public void setOutputFacing(EnumFacing outputFacing) {
        this.outputFacingItems = outputFacing;
        this.outputFacingFluids = outputFacing;
        this.outputFacingAspects = outputFacing;
        if (!super.getWorld().isRemote) {
            super.notifyBlockUpdate();
            super.writeCustomData(GregtechDataCodes.UPDATE_OUTPUT_FACING, (buf) -> {
                buf.writeByte(this.outputFacingItems.getIndex());
                buf.writeByte(this.outputFacingFluids.getIndex());
                buf.writeByte(this.outputFacingAspects.getIndex());
            });
            super.markDirty();
        }

    }

    public void setOutputFacingItems(EnumFacing outputFacing) {
        this.outputFacingItems = outputFacing;
        if (!super.getWorld().isRemote) {
            super.notifyBlockUpdate();
            super.writeCustomData(GregtechDataCodes.UPDATE_OUTPUT_FACING, (buf) -> {
                buf.writeByte(this.outputFacingItems.getIndex());
                buf.writeByte(this.outputFacingFluids.getIndex());
                buf.writeByte(this.outputFacingAspects.getIndex());
            });
            super.markDirty();
        }

    }

    public void setOutputFacingFluids(EnumFacing outputFacing) {
        this.outputFacingFluids = outputFacing;
        if (!super.getWorld().isRemote) {
            super.notifyBlockUpdate();
            super.writeCustomData(GregtechDataCodes.UPDATE_OUTPUT_FACING, (buf) -> {
                buf.writeByte(this.outputFacingItems.getIndex());
                buf.writeByte(this.outputFacingFluids.getIndex());
                buf.writeByte(this.outputFacingAspects.getIndex());
            });
            super.markDirty();
        }

    }

    public void setAutoOutputItems(boolean autoOutputItems) {
        this.autoOutputItems = autoOutputItems;
        if (!super.getWorld().isRemote) {
            super.writeCustomData(GregtechDataCodes.UPDATE_AUTO_OUTPUT_ITEMS, (buf) -> {
                buf.writeBoolean(autoOutputItems);
            });
            super.markDirty();
        }

    }

    public void setAutoOutputFluids(boolean autoOutputFluids) {
        this.autoOutputFluids = autoOutputFluids;
        if (!super.getWorld().isRemote) {
            super.writeCustomData(GregtechDataCodes.UPDATE_AUTO_OUTPUT_FLUIDS, (buf) -> {
                buf.writeBoolean(autoOutputFluids);
            });
            super.markDirty();
        }

    }

    public void setAutoOutputAspects(boolean autoOutputAspects) {
        this.autoOutputAspects = autoOutputAspects;
        if (!super.getWorld().isRemote) {
            super.writeCustomData(200, (buf) -> {
                buf.writeBoolean(autoOutputAspects);
            });
            super.markDirty();
        }

    }

    public void setAllowInputFromOutputSideItems(boolean allowInputFromOutputSide) {
        this.allowInputFromOutputSideItems = allowInputFromOutputSide;
        if (!super.getWorld().isRemote) {
            super.markDirty();
        }

    }

    public void setAllowInputFromOutputSideFluids(boolean allowInputFromOutputSide) {
        this.allowInputFromOutputSideFluids = allowInputFromOutputSide;
        if (!super.getWorld().isRemote) {
            super.markDirty();
        }

    }

    public void setAllowInputFromOutputSideAspects(boolean allowInputFromOutputSide) {
        this.allowInputFromOutputSideAspects = allowInputFromOutputSide;
        if (!super.getWorld().isRemote) {
            super.markDirty();
        }

    }

    public void setGhostCircuitConfig(int config) {
        if (this.circuitInventory != null && this.circuitInventory.getCircuitValue() != config) {
            this.circuitInventory.setCircuitValue(config);
            if (!super.getWorld().isRemote) {
                super.markDirty();
            }

        }
    }

    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(frontFacing);
        if (this.outputFacingItems == null || this.outputFacingFluids == null) {
            setOutputFacing(frontFacing.getOpposite());
        }

    }

    /** @deprecated */
    @Deprecated
    public EnumFacing getOutputFacing() {
        return getOutputFacingItems();
    }

    public EnumFacing getOutputFacingItems() {
        return this.outputFacingItems == null ? EnumFacing.SOUTH : this.outputFacingItems;
    }

    public EnumFacing getOutputFacingFluids() {
        return this.outputFacingFluids == null ? EnumFacing.SOUTH : this.outputFacingFluids;
    }

    public EnumFacing getOutputFacingAspects() {
        return this.outputFacingAspects == null ? EnumFacing.SOUTH : this.outputFacingAspects;
    }

    public boolean isAutoOutputItems() {
        return this.autoOutputItems;
    }

    public boolean isAutoOutputFluids() {
        return this.autoOutputFluids;
    }
    public boolean isAutoOutputAspects() {
        return this.autoOutputAspects;
    }

    public boolean isAllowInputFromOutputSideItems() {
        return this.allowInputFromOutputSideItems;
    }

    public boolean isAllowInputFromOutputSideFluids() {
        return this.allowInputFromOutputSideFluids;
    }
    public boolean isAllowInputFromOutputSideAspects() {
        return this.allowInputFromOutputSideAspects;
    }

    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        clearInventory(itemBuffer, this.chargerInventory);
    }

    protected ModularUI.Builder createGuiTemplate(EntityPlayer player) {
        AspectRecipeMap<?> workableRecipeMap = super.workable.getRecipeMap();
        int yOffset = 0;
        if (workableRecipeMap.getMaxInputs() >= 6 || workableRecipeMap.getMaxFluidInputs() >= 6 || workableRecipeMap.getMaxOutputs() >= 6 || workableRecipeMap.getMaxFluidOutputs() >= 6) {
            yOffset = 9;
        }

        AbstractAspectRecipeLogic var10001 = super.workable;
        Objects.requireNonNull(var10001);
        ModularUI.Builder var10000 = workableRecipeMap.createUITemplate(var10001::getProgressPercent, super.importItems, super.exportItems, super.importFluids, super.exportFluids, super.importAspects, super.exportAspects, yOffset).widget(new LabelWidget(5, 5, super.getMetaFullName(), new Object[0])).widget((new SlotWidget(this.chargerInventory, 0, 79, 62 + yOffset, true, true, false)).setBackgroundTexture(new IGuiTexture[]{GuiTextures.SLOT, GuiTextures.CHARGER_OVERLAY}).setTooltipText("gregtech.gui.charger_slot.tooltip", new Object[]{GTValues.VNF[super.getTier()], GTValues.VNF[super.getTier()]}));
        ImageWidget var8 = (new ImageWidget(79, 42 + yOffset, 18, 18, GuiTextures.INDICATOR_NO_ENERGY)).setIgnoreColor(true);
        AbstractAspectRecipeLogic var10002 = super.workable;
        Objects.requireNonNull(var10002);
        ModularUI.Builder builder = var10000.widget(var8.setPredicate(var10002::isHasNotEnoughEnergy)).bindPlayerInventory(player.inventory, GuiTextures.SLOT, yOffset);
        int leftButtonStartX = 7;
        if (super.exportItems.getSlots() > 0) {
            builder.widget((new ToggleButtonWidget(leftButtonStartX, 62 + yOffset, 18, 18, GuiTextures.BUTTON_ITEM_OUTPUT, this::isAutoOutputItems, this::setAutoOutputItems)).setTooltipText("gregtech.gui.item_auto_output.tooltip", new Object[0]).shouldUseBaseBackground());
            leftButtonStartX += 18;
        }

        if (super.exportFluids.getTanks() > 0) {
            builder.widget((new ToggleButtonWidget(leftButtonStartX, 62 + yOffset, 18, 18, GuiTextures.BUTTON_FLUID_OUTPUT, this::isAutoOutputFluids, this::setAutoOutputFluids)).setTooltipText("gregtech.gui.fluid_auto_output.tooltip", new Object[0]).shouldUseBaseBackground());
            leftButtonStartX += 18;
        }

        if (super.exportAspects.getTanks() > 0) {
            builder.widget((new ToggleButtonWidget(leftButtonStartX, 62 + yOffset, 18, 18, ZZTextures.ASPECT_SLOT, this::isAutoOutputAspects, this::setAutoOutputAspects)).setTooltipText("gregtech.gui.aspect_auto_output.tooltip", new Object[0]).shouldUseBaseBackground());
            leftButtonStartX += 18;
        }

        int var10004 = 62 + yOffset;
        String[] var10007 = super.workable.getAvailableOverclockingTiers();
        AbstractAspectRecipeLogic var10008 = super.workable;
        Objects.requireNonNull(var10008);
        IntSupplier var9 = var10008::getOverclockTier;
        AbstractAspectRecipeLogic var10009 = super.workable;
        Objects.requireNonNull(var10009);
        builder.widget((new CycleButtonWidget(leftButtonStartX, var10004, 18, 18, var10007, var9, var10009::setOverclockTier)).setTooltipHoverString("gregtech.gui.overclock.description").setButtonTexture(GuiTextures.BUTTON_OVERCLOCK));
        if (super.exportItems.getSlots() + super.exportFluids.getTanks() + super.exportAspects.getTanks() <= 9) {
            ImageWidget logo = (new ImageWidget(152, 63 + yOffset, 17, 17, (Boolean)GTValues.XMAS.get() ? GuiTextures.GREGTECH_LOGO_XMAS : GuiTextures.GREGTECH_LOGO)).setIgnoreColor(true);
            if (this.circuitInventory != null) {
                SlotWidget circuitSlot = (new GhostCircuitSlotWidget(this.circuitInventory, 0, 124, 62 + yOffset)).setBackgroundTexture(GuiTextures.SLOT, getCircuitSlotOverlay());
                builder.widget(circuitSlot.setConsumer(this::getCircuitSlotTooltip)).widget(logo);
            }
        }

        return builder;
    }

    public boolean hasGhostCircuitInventory() {
        return true;
    }

    protected TextureArea getCircuitSlotOverlay() {
        return GuiTextures.INT_CIRCUIT_OVERLAY;
    }

    protected void getCircuitSlotTooltip(SlotWidget widget) {
        String configString;
        if (this.circuitInventory != null && this.circuitInventory.getCircuitValue() != -1) {
            configString = String.valueOf(this.circuitInventory.getCircuitValue());
        } else {
            configString = (new TextComponentTranslation("gregtech.gui.configurator_slot.no_value", new Object[0])).getFormattedText();
        }

        widget.setTooltipText("gregtech.gui.configurator_slot.tooltip", configString);
    }

    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createGuiTemplate(entityPlayer).build(super.getHolder(), entityPlayer);
    }

    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        String key = super.metaTileEntityId.getPath().split("\\.")[0];
        String mainKey = String.format("gregtech.machine.%s.tooltip", key);
        if (I18n.hasKey(mainKey)) {
            tooltip.add(1, mainKey);
        }

    }

    public boolean needsSneakToRotate() {
        return true;
    }

    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.auto_output_covers", new Object[0]));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing", new Object[0]));
        tooltip.add(I18n.format("gregtech.tool_action.soft_mallet.reset", new Object[0]));
        super.addToolUsages(stack, world, tooltip, advanced);
    }
}

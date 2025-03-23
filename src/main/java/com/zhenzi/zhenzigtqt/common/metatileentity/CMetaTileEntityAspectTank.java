package com.zhenzi.zhenzigtqt.common.metatileentity;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.zhenzi.zhenzigtqt.client.gui.QuantumAspectTank.AspectPhantomTankWidget;
import com.zhenzi.zhenzigtqt.client.gui.QuantumAspectTank.AspectTankWidget;
import com.zhenzi.zhenzigtqt.client.render.texture.ZZTextures;
import com.zhenzi.zhenzigtqt.client.render.texture.custom.AspectStorageRenderer;
import com.zhenzi.zhenzigtqt.common.lib.GTEssentiaHandler;
import com.zhenzi.zhenzigtqt.common.lib.aspect.*;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.FilteredItemHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.cover.CoverRayTracer;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.*;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.custom.QuantumStorageRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import thaumcraft.api.aspects.Aspect;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CMetaTileEntityAspectTank extends MetaTileEntity implements ITieredMetaTileEntity, IActiveOutputSide, IFastRenderMetaTileEntity {
    private final int tier;
    private final int maxAspectCapacity;
    protected AspectTank aspectTank;
    private boolean autoOutputFluids;
    private @Nullable EnumFacing outputFacing;
    private boolean allowInputFromOutputSide = false;
    protected IAspectHandler outputFluidInventory;
    protected @Nullable AspectStack previousFluid;
    protected boolean locked;
    protected boolean voiding;
    private @Nullable AspectStack lockedFluid;

    protected IAspectHandler aspectInventory;
    protected AspectTankList importAspects;
    protected AspectTankList exportAspects;

    public CMetaTileEntityAspectTank(ResourceLocation metaTileEntityId, int tier, int maxAspectCapacity) {
        super(metaTileEntityId);
        this.tier = tier;
        this.maxAspectCapacity = maxAspectCapacity;
        this.initializeInventory();
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    protected void initializeInventory() {
        super.initializeInventory();
        this.aspectTank = new QuantumAspectTank(this.maxAspectCapacity);
        this.aspectInventory = this.aspectTank;
        this.importAspects = new AspectTankList(false, this.aspectTank);
        this.exportAspects = new AspectTankList(false, this.aspectTank);
        this.outputFluidInventory = new AspectHandlerProxy(new AspectTankList(false), this.exportAspects);
    }

    public void update() {
        super.update();
        EnumFacing currentOutputFacing = this.getOutputFacing();
        if (!this.getWorld().isRemote) {
            this.fillContainerFromInternalTank();
            this.fillInternalTankFromAspectContainer();
            if (this.isAutoOutputFluids()) {
                this.pushAspectsIntoNearbyHandlers(currentOutputFacing);
            }

            this.takeAspectsIntoNearByHandler(EnumFacing.UP);

            AspectStack currentFluid = this.aspectTank.getAspectStack();
            if (this.previousFluid == null) {
                if (currentFluid != null) {
                    this.updatePreviousFluid(currentFluid);
                }
            } else if (currentFluid == null) {
                this.updatePreviousFluid(null);
            } else if (this.previousFluid.isAspectEqual(currentFluid) && this.previousFluid.amount != currentFluid.amount) {
                int currentFill = MathHelper.floor(16.0F * (float)currentFluid.amount / (float)this.aspectTank.getCapacity());
                int previousFill = MathHelper.floor(16.0F * (float)this.previousFluid.amount / (float)this.aspectTank.getCapacity());
                this.previousFluid.amount = currentFluid.amount;
                this.writeCustomData(GregtechDataCodes.UPDATE_FLUID_AMOUNT, (buf) -> {
                    buf.writeInt(currentFluid.amount);
                    buf.writeBoolean(currentFill != previousFill);
                });
            } else if (!this.previousFluid.equals(currentFluid)) {
                this.updatePreviousFluid(currentFluid);
            }
        }

    }

//    public fillContainerFromOtherTank(EnumFacing)
//    {
//
//    }


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

    public void takeAspectsIntoNearByHandler(EnumFacing... enumFacings)
    {
        AspectStack aspectStack = GTEssentiaHandler.takeEssentia(this, enumFacings[0], false, 5);
        if (aspectStack != null)
        {
            this.aspectTank.fill(aspectStack, true);
        }
    }
    public void pushAspectsIntoNearbyHandlers(EnumFacing... allowedFaces) {
        this.transferToNearby(CapabilityAspectHandler.ASPECT_HANDLER_CAPABILITY, AspectUtil::transferAspects, allowedFaces);

        for (EnumFacing facing : allowedFaces)
        {

            if (this.aspectTank.getAspectStack() != null && GTEssentiaHandler.addEssentiaToTile(this, this.aspectTank.getAspectStack().getAspect(), facing, false, 5))
            {
                this.aspectTank.drain(1, true);
//                this.takeFromContainer(this.aspectTank.getAspectStack().getAspect(), 1);
            }
        }
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

    protected void updatePreviousFluid(AspectStack currentFluid) {
        this.previousFluid = currentFluid == null ? null : currentFluid.copy();
        this.writeCustomData(GregtechDataCodes.UPDATE_FLUID, (buf) -> {
            buf.writeCompoundTag(currentFluid == null ? null : currentFluid.writeToNBT(new NBTTagCompound()));
        });
    }

    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("FluidInventory", this.aspectTank.writeToNBT(new NBTTagCompound()));
        data.setBoolean("AutoOutputFluids", this.autoOutputFluids);
        data.setInteger("OutputFacing", this.getOutputFacing().getIndex());
        data.setBoolean("IsVoiding", this.voiding);
        data.setBoolean("IsLocked", this.locked);
        if (this.locked && this.lockedFluid != null) {
            data.setTag("LockedAspect", this.lockedFluid.writeToNBT(new NBTTagCompound()));
        }

        data.setBoolean("AllowInputFromOutputSideF", this.allowInputFromOutputSide);
        return data;
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("ContainerInventory")) {
            legacyTankItemHandlerNBTReading(this, data.getCompoundTag("ContainerInventory"), 0, 1);
        }

        this.aspectTank.readFromNBT(data.getCompoundTag("FluidInventory"));
        this.autoOutputFluids = data.getBoolean("AutoOutputFluids");
        this.outputFacing = EnumFacing.VALUES[data.getInteger("OutputFacing")];
        this.voiding = data.getBoolean("IsVoiding") || data.getBoolean("IsPartiallyVoiding");
        this.locked = data.getBoolean("IsLocked");
        this.lockedFluid = this.locked ? AspectStack.loadAspectStackFromNBT(data.getCompoundTag("LockedAspect")) : null;
        this.allowInputFromOutputSide = data.getBoolean("AllowInputFromOutputSideF");
    }

    public static void legacyTankItemHandlerNBTReading(MetaTileEntity mte, NBTTagCompound nbt, int inputSlot, int outputSlot) {
        if (mte != null && nbt != null) {
            NBTTagList items = nbt.getTagList("Items", 10);
            if (mte.getExportItems().getSlots() >= 1 && mte.getImportItems().getSlots() >= 1 && inputSlot >= 0 && outputSlot >= 0 && inputSlot != outputSlot) {
                for(int i = 0; i < items.tagCount(); ++i) {
                    NBTTagCompound itemTags = items.getCompoundTagAt(i);
                    int slot = itemTags.getInteger("Slot");
                    if (slot == inputSlot) {
                        mte.getImportItems().setStackInSlot(0, new ItemStack(itemTags));
                    } else if (slot == outputSlot) {
                        mte.getExportItems().setStackInSlot(0, new ItemStack(itemTags));
                    }
                }

            }
        }
    }

    public void initFromItemStackData(NBTTagCompound tag) {
        super.initFromItemStackData(tag);
        if (tag.hasKey("Fluid", 10)) {
            this.aspectTank.setAspectStack(AspectStack.loadAspectStackFromNBT(tag.getCompoundTag("Aspect")));
        }

        if (tag.getBoolean("IsVoiding") || tag.getBoolean("IsPartialVoiding")) {
            this.setVoiding(true);
        }

        this.lockedFluid = AspectStack.loadAspectStackFromNBT(tag.getCompoundTag("LockedAspect"));
        this.locked = this.lockedFluid != null;
    }

    public void writeItemStackData(NBTTagCompound tag) {
        super.writeItemStackData(tag);
        AspectStack stack = this.aspectTank.getAspectStack();
        if (stack != null && stack.amount > 0) {
            tag.setTag("Aspect", stack.writeToNBT(new NBTTagCompound()));
        }

        if (this.voiding) {
            tag.setBoolean("IsVoiding", true);
        }

        if (this.locked && this.lockedFluid != null) {
            tag.setTag("LockedAspect", this.lockedFluid.writeToNBT(new NBTTagCompound()));
        }

    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new CMetaTileEntityAspectTank(this.metaTileEntityId, this.tier, this.maxAspectCapacity);
    }

    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(false);
    }

    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false);
    }

    protected IItemHandlerModifiable createImportItemHandler() {
        return (new FilteredItemHandler(this, 1)).setFillPredicate(FilteredItemHandler.getCapabilityFilter(CapabilityAspectHandler.ASPECT_HANDLER_ITEM_CAPABILITY));
    }

    protected IItemHandlerModifiable createExportItemHandler() {
        return new GTItemStackHandler(this, 1);
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Textures.QUANTUM_STORAGE_RENDERER.renderMachine(renderState, translation, (IVertexOperation[]) ArrayUtils.add(pipeline, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(this.getPaintingColorForRendering()))), this);
        ZZTextures.QUANTUM_ASPECT_TANK_OVERLAY.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        if (this.outputFacing != null) {
            ZZTextures.PIPE_ASPECT_OUT_OVERLAY.renderSided(this.outputFacing, renderState, translation, pipeline);
            if (this.isAutoOutputFluids()) {
                ZZTextures.ASPECT_OUTPUT_OVERLAY.renderSided(this.outputFacing, renderState, translation, pipeline);
            }
        }

        AspectStorageRenderer.renderTankAspect(renderState, translation, pipeline, this.aspectTank, this.getWorld(), this.getPos(), this.getFrontFacing());
    }

    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        if (this.aspectTank.getAspectStack() != null && this.aspectTank.getAspectAmount() != 0) {
            QuantumStorageRenderer.renderTankAmount(x, y, z, this.getFrontFacing(), (long)this.aspectTank.getAspectAmount());
        }
    }

    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.VOLTAGE_CASINGS[this.tier].getParticleSprite(), this.getPaintingColorForRendering());
    }

    public void addInformation(ItemStack stack, @org.jetbrains.annotations.Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.quantum_tank.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity", this.maxAspectCapacity));
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null) {
            if (tag.hasKey("Aspect", 10)) {
                AspectStack aspectStack = AspectStack.loadAspectStackFromNBT(tag.getCompoundTag("Aspect"));
                if (aspectStack != null) {
                    tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_stored", aspectStack.getLocalizedName(), aspectStack.amount));
                }
            }

            if (tag.getBoolean("IsVoiding") || tag.getBoolean("IsPartialVoiding")) {
                tooltip.add(I18n.format("gregtech.machine.quantum_tank.tooltip.voiding_enabled"));
            }
        }

    }

    public void addToolUsages(ItemStack stack, @org.jetbrains.annotations.Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.auto_output_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    protected ModularUI createUI(EntityPlayer entityPlayer) {
        AspectTankWidget tankWidget = (new AspectPhantomTankWidget(this.aspectTank, 69, 43, 18, 18, () -> {
            return this.lockedFluid;
        }, (f) -> {
            if (this.aspectTank.getAspectAmount() == 0) {
                if (f == null) {
                    this.setLocked(false);
                    this.lockedFluid = null;
                } else {
                    this.setLocked(true);
                    this.lockedFluid = f.copy();
                    this.lockedFluid.amount = 1;
                }

            }
        })).setAlwaysShowFull(true).setDrawHoveringText(false);
        return ModularUI.defaultBuilder()
                .widget(new ImageWidget(7, 16, 81, 46, GuiTextures.DISPLAY))
                .widget(new LabelWidget(11, 20, "gregtech.gui.fluid_amount", 16777215))
                .widget(tankWidget).widget(new AdvancedTextWidget(11, 30, this.getAspectAmountText(tankWidget), 16777215))
                .widget(new AdvancedTextWidget(11, 40, this.getAspectNameText(tankWidget), 16777215)).label(6, 6, this.getMetaFullName())
                .widget((new AspectContainerSlotWidget(this.importItems, 0, 90, 17, false)).setBackgroundTexture(GuiTextures.SLOT, GuiTextures.IN_SLOT_OVERLAY))
                .widget((new SlotWidget(this.exportItems, 0, 90, 44, true, false)).setBackgroundTexture(GuiTextures.SLOT, GuiTextures.OUT_SLOT_OVERLAY))
                .widget((new ToggleButtonWidget(7, 64, 18, 18, GuiTextures.BUTTON_FLUID_OUTPUT, this::isAutoOutputFluids, this::setAutoOutputFluids)).setTooltipText("gregtech.gui.fluid_auto_output.tooltip").shouldUseBaseBackground())
                .widget((new ToggleButtonWidget(25, 64, 18, 18, GuiTextures.BUTTON_LOCK, this::isLocked, this::setLocked)).setTooltipText("gregtech.gui.fluid_lock.tooltip").shouldUseBaseBackground())
                .widget((new ToggleButtonWidget(43, 64, 18, 18, GuiTextures.BUTTON_FLUID_VOID, this::isVoiding, this::setVoiding)).setTooltipText("gregtech.gui.fluid_voiding.tooltip").shouldUseBaseBackground())
                .bindPlayerInventory(entityPlayer.inventory)
                .build(this.getHolder(), entityPlayer);
    }

    private Consumer<List<ITextComponent>> getAspectNameText(AspectTankWidget tankWidget) {
        return (list) -> {
            TextComponentTranslation translation = tankWidget.getFluidTextComponent();
            if (translation == null) {
                translation = AspectUtil.getFluidTranslation(this.lockedFluid);
            }

            if (translation != null) {
                list.add(translation);
            }

        };
    }

    private Consumer<List<ITextComponent>> getAspectAmountText(AspectTankWidget tankWidget) {
        return (list) -> {
            String fluidAmount = "";
            if (tankWidget.getFormattedFluidAmount().equals("0")) {
                if (this.lockedFluid != null) {
                    fluidAmount = "0";
                }
            } else {
                fluidAmount = tankWidget.getFormattedFluidAmount();
            }

            if (!fluidAmount.isEmpty()) {
                list.add(new TextComponentString(fluidAmount));
            }

        };
    }

    public EnumFacing getOutputFacing() {
        return this.outputFacing == null ? this.frontFacing.getOpposite() : this.outputFacing;
    }

    public void setFrontFacing(EnumFacing frontFacing) {
        if (frontFacing == EnumFacing.UP) {
            if (this.outputFacing != null && this.outputFacing != EnumFacing.DOWN) {
                super.setFrontFacing(this.outputFacing.getOpposite());
            } else {
                super.setFrontFacing(EnumFacing.NORTH);
            }
        } else {
            super.setFrontFacing(frontFacing);
        }

        if (this.outputFacing == null) {
            this.setOutputFacing(frontFacing.getOpposite());
        }

    }

    @Override
    public boolean isAutoOutputItems() {
        return false;
    }

    @Override
    public boolean isAutoOutputFluids() {
        return this.autoOutputFluids;
    }

    @Override
    public boolean isAllowInputFromOutputSideItems() {
        return false;
    }

    @Override
    public boolean isAllowInputFromOutputSideFluids() {
        return this.allowInputFromOutputSide;
    }

    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_OUTPUT_FACING) {
            this.outputFacing = EnumFacing.VALUES[buf.readByte()];
            this.scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.UPDATE_AUTO_OUTPUT_FLUIDS) {
            this.autoOutputFluids = buf.readBoolean();
            this.scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.UPDATE_FLUID) {
            try {
                this.aspectTank.setAspectStack(AspectStack.loadAspectStackFromNBT(buf.readCompoundTag()));
            } catch (IOException var6) {
                GTLog.logger.warn("Failed to load fluid from NBT in a quantum tank at {} on a routine fluid update", this.getPos());
            }

            this.scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.UPDATE_FLUID_AMOUNT) {
            int amount = buf.readInt();
            boolean updateRendering = buf.readBoolean();
            AspectStack stack = this.aspectTank.getAspectStack();
            if (stack != null) {
                stack.amount = Math.min(amount, this.aspectTank.getCapacity());
                if (updateRendering) {
                    this.scheduleRenderUpdate();
                }
            }
        } else if (dataId == GregtechDataCodes.UPDATE_IS_VOIDING) {
            this.setVoiding(buf.readBoolean());
        }

    }

    public boolean isValidFrontFacing(EnumFacing facing) {
        return super.isValidFrontFacing(facing) && facing != this.outputFacing;
    }

    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(this.getOutputFacing().getIndex());
        buf.writeBoolean(this.autoOutputFluids);
        buf.writeBoolean(this.locked);
        buf.writeCompoundTag(this.aspectTank.getAspectStack() == null ? null : this.aspectTank.getAspectStack().writeToNBT(new NBTTagCompound()));
        buf.writeBoolean(this.voiding);
    }

    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.outputFacing = EnumFacing.VALUES[buf.readByte()];
        if (this.frontFacing == EnumFacing.UP) {
            if (this.outputFacing != EnumFacing.DOWN) {
                this.frontFacing = this.outputFacing.getOpposite();
            } else {
                this.frontFacing = EnumFacing.NORTH;
            }
        }

        this.autoOutputFluids = buf.readBoolean();
        this.locked = buf.readBoolean();

        try {
            this.aspectTank.setAspectStack(AspectStack.loadAspectStackFromNBT(buf.readCompoundTag()));
        } catch (IOException var3) {
            GTLog.logger.warn("Failed to load fluid from NBT in a quantum tank at " + this.getPos() + " on initial server/client sync");
        }

        this.voiding = buf.readBoolean();
    }

    public void setOutputFacing(EnumFacing outputFacing) {
        this.outputFacing = outputFacing;
        if (!this.getWorld().isRemote) {
            this.notifyBlockUpdate();
            this.writeCustomData(GregtechDataCodes.UPDATE_OUTPUT_FACING, (buf) -> {
                buf.writeByte(outputFacing.getIndex());
            });
            this.markDirty();
        }

    }

    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_ACTIVE_OUTPUT_SIDE) {
            return side == this.getOutputFacing() ? GregtechTileCapabilities.CAPABILITY_ACTIVE_OUTPUT_SIDE.cast(this) : null;
        } else if (capability != CapabilityAspectHandler.ASPECT_HANDLER_CAPABILITY) {
            return super.getCapability(capability, side);
        } else {
            IAspectHandler fluidHandler = side == this.getOutputFacing() && !this.isAllowInputFromOutputSideFluids() ? this.outputFluidInventory : this.aspectInventory;
            return fluidHandler.getTankProperties().length > 0 ? CapabilityAspectHandler.ASPECT_HANDLER_CAPABILITY.cast(fluidHandler) : null;
        }
    }

    public ICapabilityProvider initItemStackCapabilities(ItemStack itemStack) {
        return new AspectHandlerItemStack(itemStack, this.maxAspectCapacity);
    }

    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            if (this.getOutputFacing() != facing && this.getFrontFacing() != facing) {
                if (!this.getWorld().isRemote) {
                    this.setOutputFacing(facing);
                }

                return true;
            } else {
                return false;
            }
        } else {
            return super.onWrenchClick(playerIn, hand, facing, hitResult);
        }
    }

    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        EnumFacing hitFacing = CoverRayTracer.determineGridSideHit(hitResult);
        if (facing == this.getOutputFacing() || hitFacing == this.getOutputFacing() && playerIn.isSneaking()) {
            if (!this.getWorld().isRemote) {
                if (this.isAllowInputFromOutputSideFluids()) {
                    this.setAllowInputFromOutputSide(false);
                    playerIn.sendStatusMessage(new TextComponentTranslation("gregtech.machine.basic.input_from_output_side.disallow", new Object[0]), true);
                } else {
                    this.setAllowInputFromOutputSide(true);
                    playerIn.sendStatusMessage(new TextComponentTranslation("gregtech.machine.basic.input_from_output_side.allow", new Object[0]), true);
                }
            }

            return true;
        } else {
            return super.onScrewdriverClick(playerIn, hand, facing, hitResult);
        }
    }

    public void setAllowInputFromOutputSide(boolean allowInputFromOutputSide) {
        if (this.allowInputFromOutputSide != allowInputFromOutputSide) {
            this.allowInputFromOutputSide = allowInputFromOutputSide;
            if (!this.getWorld().isRemote) {
                this.markDirty();
            }

        }
    }

    public void setAutoOutputFluids(boolean autoOutputFluids) {
        if (this.autoOutputFluids != autoOutputFluids) {
            this.autoOutputFluids = autoOutputFluids;
            if (!this.getWorld().isRemote) {
                this.writeCustomData(GregtechDataCodes.UPDATE_AUTO_OUTPUT_FLUIDS, (buf) -> {
                    buf.writeBoolean(autoOutputFluids);
                });
                this.markDirty();
            }

        }
    }

    protected boolean isLocked() {
        return this.locked;
    }

    protected void setLocked(boolean locked) {
        if (this.locked != locked) {
            this.locked = locked;
            if (!this.getWorld().isRemote) {
                this.markDirty();
            }

            if (locked && this.aspectTank.getAspectStack() != null) {
                this.lockedFluid = this.aspectTank.getAspectStack().copy();
                this.lockedFluid.amount = 1;
            } else {
                this.lockedFluid = null;
            }
        }
    }

    protected boolean isVoiding() {
        return this.voiding;
    }

    protected void setVoiding(boolean isPartialVoid) {
        this.voiding = isPartialVoid;
        if (!this.getWorld().isRemote) {
            this.writeCustomData(GregtechDataCodes.UPDATE_IS_VOIDING, (buf) -> {
                buf.writeBoolean(this.voiding);
            });
            this.markDirty();
        }

    }

    public ItemStack getPickItem(EntityPlayer player) {
        if (!player.isCreative()) {
            return super.getPickItem(player);
        } else {
            ItemStack baseItemStack = this.getStackForm();
            NBTTagCompound tag = new NBTTagCompound();
            this.writeItemStackData(tag);
            if (!tag.isEmpty()) {
                baseItemStack.setTagCompound(tag);
            }

            return baseItemStack;
        }
    }

    public boolean needsSneakToRotate() {
        return true;
    }

    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.getPos());
    }

    public boolean isOpaqueCube() {
        return false;
    }

    public int getLightOpacity() {
        return 0;
    }

    private class QuantumAspectTank extends AspectTank implements IFilteredAspectContainer, IFilter<AspectStack> {
        public QuantumAspectTank(int capacity) {
            super(capacity);
        }

        public int fillInternal(AspectStack resource, boolean doFill) {
            int accepted = super.fillInternal(resource, doFill);
            if (accepted == 0 && !resource.isAspectEqual(this.getAspectStack())) {
                return 0;
            } else {
                if (doFill && CMetaTileEntityAspectTank.this.locked && CMetaTileEntityAspectTank.this.lockedFluid == null) {
                    CMetaTileEntityAspectTank.this.lockedFluid = resource.copy();
                    CMetaTileEntityAspectTank.this.lockedFluid.amount = 1;
                }

                return CMetaTileEntityAspectTank.this.voiding ? resource.amount : accepted;
            }
        }

        public boolean canFillFluidType(AspectStack aspectStack) {
            return this.test(aspectStack);
        }

        public IFilter<AspectStack> getFilter() {
            return this;
        }

        public boolean test(AspectStack aspectStack) {
            return !CMetaTileEntityAspectTank.this.locked || CMetaTileEntityAspectTank.this.lockedFluid == null || aspectStack.isAspectEqual(CMetaTileEntityAspectTank.this.lockedFluid);
        }

        public int getPriority() {
            return CMetaTileEntityAspectTank.this.locked && CMetaTileEntityAspectTank.this.lockedFluid != null ? IFilter.whitelistPriority(1) : IFilter.noPriority();
        }
    }
}

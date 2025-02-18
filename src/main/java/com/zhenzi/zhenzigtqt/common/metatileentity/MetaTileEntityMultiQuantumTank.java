package com.zhenzi.zhenzigtqt.common.metatileentity;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.zhenzi.zhenzigtqt.client.gui.LabelFluidLockedTooltipWidget;
import com.zhenzi.zhenzigtqt.client.render.texture.custom.MultiQuantumStorageRenderer;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.FilteredItemHandler;
import gregtech.api.capability.impl.FluidHandlerProxy;
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
import gregtech.api.util.GTUtility;
import gregtech.api.util.function.BooleanConsumer;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.function.BooleanSupplier;


//四重超级缸，超级屎山

public class MetaTileEntityMultiQuantumTank extends MetaTileEntity implements ITieredMetaTileEntity, IActiveOutputSide, IFastRenderMetaTileEntity{
    private final int tier;
    private final int maxFluidCapacity;
    //占位，防止初始化时候崩溃
    protected FluidTank fluidTankA;
    //直接堆四个tank
    protected MuitlQuantumFluidTank[] fluidTanks;
    private boolean autoOutputFluids;
    private @Nullable EnumFacing outputFacing;
    private boolean allowInputFromOutputSide = false;
    protected IFluidHandler outputFluidInventory;
    //用于更新客户端的渲染状态
    protected @Nullable FluidStack[] previousFluids;
    //储存被锁定的液体
    protected FluidStack[] lockedFluids;
//    protected boolean locked;
    protected boolean voiding;
    protected IFluidHandler[] fluidInventorys;
    private String lockedFluidtext;
    private int tankAmount;

    public MetaTileEntityMultiQuantumTank(ResourceLocation metaTileEntityId, int tier, int maxFluidCapacity, int tankAmount) {
        super(metaTileEntityId);
        this.tier = tier;
        this.maxFluidCapacity = maxFluidCapacity;
        this.tankAmount = tankAmount;
        this.initializeInventory();

    }

    public int getTier() {
        return tier;
    }

    //初始化
    @Override
    protected void initializeInventory() {
        super.initializeInventory();

        this.fluidTanks = new MuitlQuantumFluidTank[this.tankAmount];

        for (int i = 0; i < this.fluidTanks.length; i++)
        {
            this.fluidTanks[i] = new MuitlQuantumFluidTank(maxFluidCapacity, i);
        }

        this.fluidInventorys = this.fluidTanks;

        this.importFluids = new FluidTankList(false, this.fluidTanks);
        this.exportFluids = new FluidTankList(false, this.fluidTanks);
        this.outputFluidInventory = new FluidHandlerProxy(new FluidTankList(false), exportFluids);

        this.previousFluids = new FluidStack[this.fluidTanks.length];
        this.lockedFluids = new FluidStack[this.fluidTanks.length];
        this.writeCustomData(200, (buf) -> {
            buf.writeInt(this.tankAmount);
        });
    }

    @Override
    public void update() {
        super.update();
        EnumFacing currentOutputFacing = this.getOutputFacing();
        if (!this.getWorld().isRemote) {
            //与其他液体储罐交互以及处理容器输入
            this.fillContainerFromInternalTank();
            this.fillInternalTankFromFluidContainer();
            if (this.isAutoOutputFluids()) {
                this.pushFluidsIntoNearbyHandlers(new EnumFacing[]{currentOutputFacing});
            }

            for (int i = 0; i < this.fluidTanks.length; i++)
            {
                this.updateFluidMethod(this.fluidTanks, i);
            }
        }

    }

    //流体渲染更新
    private void updateFluidMethod(FluidTank[] tanks, int index)
    {
        FluidStack currentFluidA = tanks[index].getFluid();
        if(this.previousFluids[index] == null)
        {
            if(currentFluidA != null)
            {
                this.updatePreviousFluid(currentFluidA, index);
            }
        } else if (currentFluidA == null)
        {
            this.updatePreviousFluid(null, index);
        } else if (this.previousFluids[index].getFluid().equals(currentFluidA.getFluid()) && this.previousFluids[index].amount != currentFluidA.amount)
        {
            int currentFill = MathHelper.floor(16.0F * (float)currentFluidA.amount / (float)tanks[0].getCapacity());
            int previousFill = MathHelper.floor(16.0F * (float)this.previousFluids[index].amount / (float)tanks[0].getCapacity());
            this.previousFluids[index].amount = currentFluidA.amount;
            this.writeCustomData(GregtechDataCodes.UPDATE_FLUID_AMOUNT, (buf) -> {

                buf.writeInt(index);
                buf.writeCompoundTag(currentFluidA.writeToNBT(new NBTTagCompound()));
                buf.writeBoolean(currentFill != previousFill);
            });
        }
    }
    protected void updatePreviousFluid(FluidStack currentFluid, int index) {
        this.previousFluids[index] = currentFluid == null ? null : currentFluid.copy();
        this.writeCustomData(GregtechDataCodes.UPDATE_FLUID, (buf) -> {
            buf.writeInt(index);
            buf.writeCompoundTag(currentFluid == null ? null : currentFluid.writeToNBT(new NBTTagCompound()));
        });
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        for (int i = 0; i < this.fluidTanks.length; i++)
        {
            data.setTag("FluidInventory" + i, this.fluidTanks[i].writeToNBT(new NBTTagCompound()));
            data.setBoolean("IsLocked" + i, this.fluidTanks[i].getIsLocked());
            if (this.fluidTanks[i].getIsLocked() && this.fluidTanks[i].getLockedFluid() != null) {
                data.setTag("LockedFluid" + i, this.fluidTanks[i].getLockedFluid().writeToNBT(new NBTTagCompound()));
            }
        }

        data.setBoolean("AutoOutputFluids", this.autoOutputFluids);
        data.setInteger("OutputFacing", this.getOutputFacing().getIndex());
        data.setBoolean("IsVoiding", this.voiding);

        data.setBoolean("AllowInputFromOutputSideF", this.allowInputFromOutputSide);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("ContainerInventory")) {
            legacyTankItemHandlerNBTReading(this, data.getCompoundTag("ContainerInventory"), 0, 1);
        }

        for (int i = 0; i < this.fluidTanks.length; i++)
        {
            this.fluidTanks[i].readFromNBT(data.getCompoundTag("FluidInventory" + i));
            this.fluidTanks[i].setIsLocked(data.getBoolean("IsLocked" + i));
            this.fluidTanks[i].setLockedFluid(this.fluidTanks[i].getIsLocked() ? FluidStack.loadFluidStackFromNBT(data.getCompoundTag("LockedFluid" + i)) : null);
        }

        this.autoOutputFluids = data.getBoolean("AutoOutputFluids");
        this.outputFacing = EnumFacing.VALUES[data.getInteger("OutputFacing")];
        this.voiding = data.getBoolean("IsVoiding") || data.getBoolean("IsPartiallyVoiding");

        this.allowInputFromOutputSide = data.getBoolean("AllowInputFromOutputSideF");

    }

    //疑似处理超级缸内部的物品存储
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

    //物品状态的超级缸的处理
    @Override
    public void initFromItemStackData(NBTTagCompound tag) {
        super.initFromItemStackData(tag);
        for (int i = 0; i < this.fluidTanks.length; i++){
            this.readItemStackMethod(tag, i);
        }

        if (tag.getBoolean("IsVoiding") || tag.getBoolean("IsPartialVoiding")) {
            this.setVoiding(true);
        }
    }

    private void readItemStackMethod(NBTTagCompound tag, int index)
    {
        if (tag.hasKey("Fluid" + index, 10)) {
            this.fluidTanks[index].setFluid(FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("Fluid" + index)));
        }

        this.fluidTanks[index].setIsLocked(tag.getBoolean("Locked" + index));
        this.fluidTanks[index].setLockedFluid(FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("LockedFluid" + index)));
    }

    @Override
    public void writeItemStackData(NBTTagCompound tag) {
        super.writeItemStackData(tag);
        for (int i = 0; i < this.fluidTanks.length; i++)
        {
            this.writeItemStackMethod(this.fluidTanks[i].getFluid(), tag, i);
        }

        if (this.voiding) {
            tag.setBoolean("IsVoiding", true);
        }
    }

    private void writeItemStackMethod(FluidStack stack, NBTTagCompound tag, int index)
    {
        if (stack != null && stack.amount > 0) {
            tag.setTag("Fluid" + index, stack.writeToNBT(new NBTTagCompound()));
        }

        if (this.fluidTanks[index].getIsLocked() && this.fluidTanks[index].getLockedFluid() != null) {
            tag.setBoolean("Locked" + index, this.fluidTanks[index].getIsLocked());
            tag.setTag("LockedFluid" + index, this.fluidTanks[index].getLockedFluid().writeToNBT(new NBTTagCompound()));
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMultiQuantumTank(this.metaTileEntityId, this.tier, this.maxFluidCapacity, this.tankAmount);
    }

    //创建接口，先传入占位符，后传入tanks，防止初始化崩溃
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, this.fluidTankA);
    }

    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false, this.fluidTankA);
    }

    //超级缸的物品输入槽逻辑，但无法实现根据槽的位置来输出特定的液体
    protected IItemHandlerModifiable createImportItemHandler() {
        return new FilteredItemHandler(this, this.tankAmount).setFillPredicate(FilteredItemHandler.getCapabilityFilter(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY));
    }



    protected IItemHandlerModifiable createExportItemHandler() {
        return new GTItemStackHandler(this, this.tankAmount);
    }

    //超级缸的客户端液体渲染
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Textures.QUANTUM_STORAGE_RENDERER.renderMachine(renderState, translation, (IVertexOperation[]) ArrayUtils.add(pipeline, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(this.getPaintingColorForRendering()))), this);
        Textures.QUANTUM_TANK_OVERLAY.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        if (this.outputFacing != null) {
            Textures.PIPE_OUT_OVERLAY.renderSided(this.outputFacing, renderState, translation, pipeline);
            if (this.isAutoOutputFluids()) {
                Textures.FLUID_OUTPUT_OVERLAY.renderSided(this.outputFacing, renderState, translation, pipeline);
            }
        }

        Cuboid6 partialFluidBox = new Cuboid6();
        partialFluidBox = new Cuboid6(0.06640625, 0.12890625, 0.06640625, 0.93359375, 0.93359375, 0.93359375);
        double high = 0;
        Cuboid6 gasPartialFluidBox = new Cuboid6();
        gasPartialFluidBox = new Cuboid6(0.06640625, 0.12890625, 0.06640625, 0.93359375, 0.93359375, 0.93359375);
        double gasHigh = 0;

        MultiQuantumStorageRenderer.renderMultiTankFluid(renderState, translation, pipeline, this.fluidTanks, this.getWorld(), this.getPos(), this.getFrontFacing(), partialFluidBox, high, gasPartialFluidBox, gasHigh, this.tankAmount);
    }

    //超级缸的客户端数字渲染
    @Override
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        for (int i = 0; i < this.fluidTanks.length; i++)
        {
            if (!this.TankIsEmpty(i)) {
                MultiQuantumStorageRenderer.renderTankAmount(x, y - 0.15 + i * 0.15, z, this.getFrontFacing(), (long)this.fluidTanks[i].getFluid().amount);
            }
        }
    }

    //检测超级缸的tank是否为空
    private boolean TankIsEmpty(int index)
    {
        if (this.fluidTanks[index].getFluid() == null) {
            return true;
        }
        if (this.fluidTanks[index] != null && this.fluidTanks[index].getFluid().amount == 0) {
            return true;
        }
        return false;
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.VOLTAGE_CASINGS[this.tier].getParticleSprite(), this.getPaintingColorForRendering());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.quantum_tank.tooltip", new Object[0]));
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity", new Object[]{this.maxFluidCapacity}));
        NBTTagCompound tag = stack.getTagCompound();

        if (tag != null) {
            this.informationFluidShow(tag, tooltip);

            if (tag.getBoolean("IsVoiding") || tag.getBoolean("IsPartialVoiding")) {
                tooltip.add(I18n.format("gregtech.machine.quantum_tank.tooltip.voiding_enabled", new Object[0]));
            }
        }

    }

    private void informationFluidShow(NBTTagCompound tag, List<String> tooltip)
    {
            for (int i = 0; i < this.fluidTanks.length; i++) {
                if (tag.hasKey("Fluid" + i, 10)) {
                    FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("Fluid" + i));
                    if (fluidStack != null) {
                        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_stored", new Object[]{fluidStack.getLocalizedName(), fluidStack.amount}));
                    }
                }
            }
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.auto_output_covers", new Object[0]));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing", new Object[0]));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    //究极堆叠
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.defaultBuilder();
        if (this.tankAmount == 4)
        {
            builder = this.create4SlotUI(entityPlayer);
        }   else if (this.tankAmount == 9)
        {
            builder = this.create9SlotUI(entityPlayer);
        }
        return builder.build(this.getHolder(), entityPlayer);
    }

    private ModularUI.Builder create4SlotUI(EntityPlayer entityPlayer)
    {
        ModularUI.Builder builder = ModularUI.defaultBuilder();

        builder.widget(new ImageWidget(7, 16, 81, 46, GuiTextures.DISPLAY))
                .widget((new ToggleButtonWidget(7, 64, 18, 18, GuiTextures.BUTTON_FLUID_OUTPUT, this::isAutoOutputFluids, this::setAutoOutputFluids)).setTooltipText("gregtech.gui.fluid_auto_output.tooltip", new Object[0]).shouldUseBaseBackground())
                .widget((new ToggleButtonWidget(25, 64, 18, 18, GuiTextures.BUTTON_FLUID_VOID, this::isVoiding, this::setVoiding)).setTooltipText("gregtech.gui.fluid_voiding.tooltip", new Object[0]).shouldUseBaseBackground()).bindPlayerInventory(entityPlayer.inventory);
        for (int i = 0; i < this.fluidTanks.length; i++) {
            builder.widget(this.createTankWidget(this.fluidTanks[i], i, 10 + i * 18, 43, 18, 18));
            builder.widget(this.createContainerSlot(i, 92  + i * 18, 17));
            builder.widget(this.createSlot(i, 92  + i * 18, 44));
            int finalI = i;
            builder.widget(this.createLockedButton(92  + i * 18, 64, () -> this.fluidTanks[finalI].isLocked,
                    (flag) -> {
                        this.setIsLocked(flag, this.fluidTanks[finalI]);
                        this.writeLockedFluids(finalI);
                        this.markDirty();
                    }));
        }
        builder.widget(new LabelFluidLockedTooltipWidget(11, 20, "Locked Fluid", this.lockedFluids));
        return builder;
    }

    private ModularUI.Builder create9SlotUI(EntityPlayer entityPlayer)
    {
        ModularUI.Builder builder = ModularUI.defaultBuilder(28);

        builder.widget(new ImageWidget(7, 8, 81, 16, GuiTextures.DISPLAY))
                .widget((new ToggleButtonWidget(151, 8, 18, 18, GuiTextures.BUTTON_FLUID_OUTPUT, this::isAutoOutputFluids, this::setAutoOutputFluids)).setTooltipText("gregtech.gui.fluid_auto_output.tooltip", new Object[0]).shouldUseBaseBackground())
                .widget((new ToggleButtonWidget(133, 8, 18, 18, GuiTextures.BUTTON_FLUID_VOID, this::isVoiding, this::setVoiding)).setTooltipText("gregtech.gui.fluid_voiding.tooltip", new Object[0]).shouldUseBaseBackground());
        for (int i = 0; i < this.fluidTanks.length; i++) {
            builder.widget(this.createTankWidget(this.fluidTanks[i], i, 7 + i * 18, 28, 18, 18));
            builder.widget(this.createContainerSlot(i, 7  + i * 18, 48));
            builder.widget(this.createSlot(i, 7  + i * 18, 68));
            int finalI = i;
            builder.widget(this.createLockedButton(7  + i * 18, 88, () -> this.fluidTanks[finalI].isLocked,
                    (flag) -> {
                        this.setIsLocked(flag, this.fluidTanks[finalI]);
                        this.writeLockedFluids(finalI);
                        this.markDirty();
                    }));
        }
        builder.widget(new LabelFluidLockedTooltipWidget(11, 10, "Locked Fluid", this.lockedFluids));
        builder.bindPlayerInventory(entityPlayer.inventory, 110);
        return builder;
    }

    //ui的液体渲染
    private TankWidget createTankWidget(FluidTank tank, int lockedFluidIndex, int x, int y, int width, int height)
    {
        TankWidget tankWidget = new TankWidget(tank, x, y, width, height).setBackgroundTexture(GuiTextures.FLUID_SLOT).setAlwaysShowFull(true).setDrawHoveringText(false).setContainerClicking(true, true);
        return tankWidget;
    }

    //ui的物品输入槽渲染
    private FluidContainerSlotWidget createContainerSlot(int slot, int x, int y)
    {
        FluidContainerSlotWidget containerSlotWidget = new FluidContainerSlotWidget(this.importItems, slot, x, y, false);
        containerSlotWidget.setBackgroundTexture(new IGuiTexture[]{GuiTextures.SLOT, GuiTextures.IN_SLOT_OVERLAY});
        return containerSlotWidget;
    }

    //ui的物品输出槽渲染
    private SlotWidget createSlot(int slot, int x, int y)
    {
        SlotWidget slotWidget = new SlotWidget(this.exportItems, slot, x, y, true, false);
        slotWidget.setBackgroundTexture(new IGuiTexture[]{GuiTextures.SLOT, GuiTextures.OUT_SLOT_OVERLAY});
        return slotWidget;
    }

    private ToggleButtonWidget createLockedButton(int x, int y, BooleanSupplier isPressedCondition, BooleanConsumer setPressedExecutor)
    {
        ToggleButtonWidget Lock = new ToggleButtonWidget(x, y, 18, 18, GuiTextures.BUTTON_LOCK, isPressedCondition, setPressedExecutor)
                .setTooltipText("gregtech.gui.fluid_lock.tooltip", new Object[0]).shouldUseBaseBackground();
        return Lock;
    }


    public EnumFacing getOutputFacing() {
        return this.outputFacing == null ? this.frontFacing.getOpposite() : this.outputFacing;
    }

    @Override
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

    //客户端接收包，更新渲染状态
    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_OUTPUT_FACING) {
            this.outputFacing = EnumFacing.VALUES[buf.readByte()];
            this.scheduleRenderUpdate();
        } else if (dataId == 200) {
            this.tankAmount = buf.readInt();
            this.scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.UPDATE_AUTO_OUTPUT_FLUIDS) {
            this.autoOutputFluids = buf.readBoolean();
            this.scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.UPDATE_FLUID) {
            try {
                int index = buf.readInt();
                this.fluidTanks[index].setFluid(FluidStack.loadFluidStackFromNBT(buf.readCompoundTag()));
            } catch (IOException var6) {
                GTLog.logger.warn("Failed to load fluid from NBT in a quantum tank at {} on a routine fluid update", this.getPos());
            }
            this.scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.UPDATE_FLUID_AMOUNT) {
            int index = buf.readInt();
            FluidStack stack = null;
            try {
                stack = FluidStack.loadFluidStackFromNBT(buf.readCompoundTag());
            } catch (IOException var6) {
                GTLog.logger.warn("Failed to load fluid from NBT in a quantum tank at {} on a routine fluid update", this.getPos());
            }
            boolean updateRendering = buf.readBoolean();
            if (stack != null) {
                this.fluidTanks[index].setFluid(stack);
                if (updateRendering) {
                    this.scheduleRenderUpdate();
                }
            }
        } else if (dataId == GregtechDataCodes.UPDATE_IS_VOIDING) {
            this.setVoiding(buf.readBoolean());
        } else if (dataId == GregtechDataCodes.UPDATE_LOCKED_STATE) {
            try {
                int index = buf.readInt();
                this.lockedFluids[index] = FluidStack.loadFluidStackFromNBT(buf.readCompoundTag());
            } catch (IOException var6) {
                GTLog.logger.warn("Failed to load fluid from NBT in a quantum tank at {} on a routine fluid update", this.getPos());
            }
        }
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return super.isValidFrontFacing(facing) && facing != this.outputFacing;
    }

    //可能是初始化时候发包
    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(this.tankAmount);
        buf.writeByte(this.getOutputFacing().getIndex());
        buf.writeBoolean(this.autoOutputFluids);
        for (int i = 0; i < this.fluidTanks.length; i++) {
            buf.writeCompoundTag(this.fluidTanks[i].getFluid() == null ? null : this.fluidTanks[i].getFluid().writeToNBT(new NBTTagCompound()));
            buf.writeCompoundTag(this.fluidTanks[i].getLockedFluid() == null ? null : this.fluidTanks[i].getLockedFluid().writeToNBT(new NBTTagCompound()));
        }
        buf.writeBoolean(this.voiding);

    }

    //可能是初始化时候接受包
    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.tankAmount = buf.readByte();
        this.outputFacing = EnumFacing.VALUES[buf.readByte()];
        if (this.frontFacing == EnumFacing.UP) {
            if (this.outputFacing != EnumFacing.DOWN) {
                this.frontFacing = this.outputFacing.getOpposite();
            } else {
                this.frontFacing = EnumFacing.NORTH;
            }
        }

        this.autoOutputFluids = buf.readBoolean();

        try {
            for (int i = 0; i < this.fluidTanks.length; i++) {
                this.fluidTanks[i].setFluid(FluidStack.loadFluidStackFromNBT(buf.readCompoundTag()));
                this.lockedFluids[i] = FluidStack.loadFluidStackFromNBT(buf.readCompoundTag());
            }
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

    //handler能传入tankList！
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_ACTIVE_OUTPUT_SIDE) {
            return side == this.getOutputFacing() ? GregtechTileCapabilities.CAPABILITY_ACTIVE_OUTPUT_SIDE.cast(this) : null;
        } else if (capability != CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return super.getCapability(capability, side);
        } else {
            IFluidHandler fluidTank = new FluidTankList(false, this.fluidTanks);
            IFluidHandler fluidHandler = side == this.getOutputFacing() && !this.isAllowInputFromOutputSideFluids() ? this.outputFluidInventory : fluidTank;
            if (fluidHandler.getTankProperties().length > 0){
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);
            }
            return null;
        }
    }

    @Override
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

    @Override
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

    //发包
    private void writeLockedFluids(int index)
    {
        this.writeCustomData(GregtechDataCodes.UPDATE_LOCKED_STATE, (buf ->
        {
            buf.writeInt(index);
            buf.writeCompoundTag(this.fluidTanks[index].getLockedFluid() == null ? null : this.fluidTanks[index].getLockedFluid().writeToNBT(new NBTTagCompound()));
        }));
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

    @Override
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

    @Override
    public boolean needsSneakToRotate() {
        return true;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.getPos());
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getLightOpacity() {
        return 0;
    }

    //设置某个tank的锁定状态
    public void setIsLocked(boolean locked, MuitlQuantumFluidTank tank)
    {
        if (tank.getIsLocked() == locked) return;
        tank.setIsLocked(locked);
        if (!getWorld().isRemote) {
            markDirty();
        }
        if (tank.getIsLocked() && tank.getFluid() != null) {
            tank.setLockedFluid(tank.getFluid().copy());
            tank.getLockedFluid().amount = 1;
            return;
        }
        tank.setLockedFluid(null);
    }

    //专门写了处理锁定逻辑的tank
    public class MuitlQuantumFluidTank extends FluidTank implements IFilteredFluidContainer, IFilter<FluidStack> {

        public int lockId;
        public boolean isLocked;
        public FluidStack lockedFluid;
        public MuitlQuantumFluidTank(int capacity, int lockId) {
            super(capacity);
            this.lockId = lockId;
        }

        @Override
        public int fillInternal(FluidStack resource, boolean doFill) {
            int accepted = super.fillInternal(resource, doFill);

            // if we couldn't accept "resource", and "resource" is not the same as the stored fluid.
            if (accepted == 0 && !resource.isFluidEqual(getFluid())) {
                return 0;
            }

            if (doFill && this.isLocked && this.lockedFluid == null) {
                this.lockedFluid = resource.copy();
                this.lockedFluid.amount = 1;
            }
            return voiding ? resource.amount : accepted;
        }

        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return test(fluid);
        }

        @Override
        public IFilter<FluidStack> getFilter() {
            return this;
        }

        @Override
        public boolean test(FluidStack fluidStack) {
            return !this.isLocked || this.lockedFluid == null || fluidStack.isFluidEqual(this.lockedFluid);
        }

        @Override
        public int getPriority() {
            return !this.isLocked || this.lockedFluid == null ? IFilter.noPriority() : IFilter.whitelistPriority(1);
        }

        public int getLockId()
        {
            return this.lockId;
        }

        public boolean getIsLocked()
        {
            return this.isLocked;
        }

        public void setIsLocked(boolean locked)
        {
            this.isLocked = locked;
        }

        public FluidStack getLockedFluid()
        {
            return this.lockedFluid;
        }

        public void setLockedFluid(FluidStack stack)
        {
            this.lockedFluid = stack;
        }
    }

}

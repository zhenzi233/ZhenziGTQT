package com.zhenzi.zhenzigtqt.common.metatileentity;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.zhenzi.zhenzigtqt.client.gui.MultiQuantumTankMainWidget;
import com.zhenzi.zhenzigtqt.client.gui.QuantumAspectTank.QuantumAspectTankMainWidget;
import com.zhenzi.zhenzigtqt.client.render.texture.ZZTextures;
import com.zhenzi.zhenzigtqt.client.render.texture.custom.AspectStorageRenderer;
import com.zhenzi.zhenzigtqt.common.lib.GTEssentiaHandler;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IActiveOutputSide;
import gregtech.api.capability.IFilter;
import gregtech.api.capability.IFilteredFluidContainer;
import gregtech.api.capability.impl.FilteredItemHandler;
import gregtech.api.capability.impl.FluidHandlerProxy;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.GTFluidHandlerItemStack;
import gregtech.api.cover.CoverRayTracer;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.TextureArea;
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
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
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
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.common.blocks.essentia.BlockJar;
import thaumcraft.common.blocks.essentia.BlockJarItem;
import thaumcraft.common.items.consumables.ItemPhial;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumcraft.common.tiles.essentia.TileJarFillable;

import javax.annotation.Nullable;

import static com.zhenzi.zhenzigtqt.common.metatileentity.ZhenziGTQTMetaTileEntity.ASPECT_TANK;

public class MetaTileEntityAspectTank extends MetaTileEntity implements ITieredMetaTileEntity, IActiveOutputSide, IFastRenderMetaTileEntity, IAspectSource, IEssentiaTransport {
    private final int tier;
    private final int maxAspectCapacity;
    private boolean autoOutputFluids;
    private @Nullable EnumFacing outputFacing;
    private boolean allowInputFromOutputSide = false;
    protected boolean voiding;

    public Aspect aspect;
    private Aspect previousAspect;
    public Aspect aspectFilter;
    public int amount = 0;
    public int previousAmount = 0;
    public int facing = 2;
    public boolean blocked = false;

    public MetaTileEntityAspectTank(ResourceLocation metaTileEntityId, int tier, int maxAspectCapacity) {
        super(metaTileEntityId);
        this.tier = tier;
        this.maxAspectCapacity = maxAspectCapacity;
        this.initializeInventory();

    }

    public int getTier() {
        return this.tier;
    }

    public int getMaxAspectCapacity()
    {
        return this.maxAspectCapacity;
    }

    protected void initializeInventory() {
        super.initializeInventory();
        this.aspect = null;
        this.aspectFilter = null;
    }

    public void update() {
        super.update();
        EnumFacing currentOutputFacing = this.getOutputFacing();
        if (!this.getWorld().isRemote) {
            this.fillInternalTankFromAspectContainer();
            this.takeInternalTankToAspectContainer();
        }
            if (this.isAutoOutputFluids()) {
                this.pushAspectIntoNearbyHandlers(new EnumFacing[]{currentOutputFacing});
            }
            Aspect currentAspect = this.aspect;
            if (this.previousAspect == null)
            {
                if (currentAspect != null)
                {
                    this.updatePreviousAspect(currentAspect);
                }
            } else if (currentAspect == null) {
                this.updatePreviousAspect(null);
            } else if (this.previousAmount == 1 && this.amount == 0)
            {
                this.writeCustomData(201, (buf) -> {
                    buf.writeString("");
                    buf.writeInt(0);

                    buf.writeBoolean(true);
                });
            }
            else if (this.previousAspect.equals(currentAspect) && this.previousAmount != this.amount) {
                this.previousAmount = this.amount;
                this.writeCustomData(201, (buf) -> {
                    buf.writeString(this.amount == 0 ? "" : this.aspect.getTag());
                    buf.writeInt(this.amount);

                    buf.writeBoolean(true);
                });
            }
            else if (!this.previousAspect.equals(currentAspect)) {
                this.updatePreviousAspect(currentAspect);
            }
    }

    public ItemStack checkItemIsMTE(Item item)
    {
        for (MetaTileEntity metaTileEntity : ASPECT_TANK) {
            if (metaTileEntity != null && !metaTileEntity.getStackForm().isEmpty())
            {
                if (item == metaTileEntity.getStackForm().getItem())
                {
                    return metaTileEntity.getStackForm();
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public void fillInternalTankFromAspectContainer()
    {
        for(int i = 0; i < this.importItems.getSlots(); ++i) {
            ItemStack inputContainerStack = this.importItems.extractItem(i, 1, true);
            if (inputContainerStack.hasTagCompound() && inputContainerStack.getTagCompound().hasKey("Aspects"))
            {
                NBTTagCompound nbtTagCompound = inputContainerStack.getTagCompound();
                NBTTagList nbta = nbtTagCompound.getTagList("Aspects", 10);
                NBTTagCompound a = null;
                a = (NBTTagCompound) nbta.get(0);
                int amount = a.getInteger("amount");
                String key = a.getString("key");
                Aspect aspect = Aspect.getAspect(key);

                    Item item = inputContainerStack.getItem();
                    if (item instanceof BlockJarItem && GTTransferUtils.insertItem(this.exportItems, new ItemStack(BlocksTC.jarNormal, 1), true).isEmpty() && this.addToContainer(aspect, amount) <= 0)
                    {
                        this.importItems.extractItem(i, 1, false);
                        GTTransferUtils.insertItem(this.exportItems, new ItemStack(BlocksTC.jarNormal, 1), false);
                    }   else if (item instanceof ItemPhial && GTTransferUtils.insertItem(this.exportItems, new ItemStack(ItemsTC.phial, 1), true).isEmpty() && this.addToContainer(aspect, amount) <= 0)
                    {
                        this.importItems.extractItem(i, 1, false);
                        GTTransferUtils.insertItem(this.exportItems, new ItemStack(ItemsTC.phial, 1), false);
                    }   else if (!this.checkItemIsMTE(item).isEmpty() && GTTransferUtils.insertItem(this.exportItems, this.checkItemIsMTE(item), true).isEmpty() && this.addToContainer(aspect, amount) <= 0)
                    {

                        this.importItems.extractItem(i, 1, false);
                        GTTransferUtils.insertItem(this.exportItems, this.checkItemIsMTE(item), false);
                    }
            }
        }
    }

    public void takeInternalTankToAspectContainer()
    {
        if (this.amount == 0) return;
        for(int i = 0; i < this.importItems.getSlots(); ++i) {
            ItemStack inputContainerStack = this.importItems.extractItem(i, 1, true);
            ItemStack exportContainerStack = this.takeFromContainer(inputContainerStack, true);
            if (exportContainerStack != ItemStack.EMPTY)
            {
                if (exportContainerStack.getItem() instanceof BlockJarItem && GTTransferUtils.insertItem(this.exportItems, exportContainerStack, true).isEmpty())
                {
                    this.importItems.extractItem(i, 1, false);
                    this.takeFromContainer(inputContainerStack, false);
                    GTTransferUtils.insertItem(this.exportItems, exportContainerStack, false);
                }   else if (exportContainerStack.getItem() instanceof ItemPhial && GTTransferUtils.insertItem(this.exportItems, exportContainerStack, true).isEmpty())
                {
                    this.importItems.extractItem(i, 1, false);
                    this.takeFromContainer(inputContainerStack, false);
                    GTTransferUtils.insertItem(this.exportItems, exportContainerStack,  false);
                }   else if (!this.checkItemIsMTE(exportContainerStack.getItem()).isEmpty() && GTTransferUtils.insertItem(this.exportItems, exportContainerStack, true).isEmpty())
                {
                    this.importItems.extractItem(i, 1, false);
                    this.takeFromContainer(inputContainerStack, false);
                    GTTransferUtils.insertItem(this.exportItems, exportContainerStack,  false);
                }
            }
        }
    }


    protected void updatePreviousAspect(Aspect currentAspect)
    {
        this.previousAspect = currentAspect;
        this.writeCustomData(200, (buf) -> {
            buf.writeString(currentAspect == null ? "" : currentAspect.getTag());
        });

    }

    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        if (this.aspect != null)
        {
            data.setString("Aspect", this.aspect.getTag());
        }
        if (this.aspectFilter != null)
        {
            data.setString("AspectFilter", this.aspectFilter.getTag());
        }
        data.setShort("Amount", (short)this.amount);
        data.setByte("facing", (byte)this.facing);
        data.setBoolean("blocked", this.blocked);
        data.setBoolean("AutoOutputFluids", this.autoOutputFluids);
        data.setInteger("OutputFacing", this.getOutputFacing().getIndex());
        data.setBoolean("IsVoiding", this.voiding);
        return data;
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        this.aspect = Aspect.getAspect(data.getString("Aspect"));
        this.aspectFilter = Aspect.getAspect(data.getString("AspectFilter"));
        this.amount = data.getShort("Amount");
        this.facing = data.getByte("facing");
        this.blocked = data.getBoolean("blocked");
        if (data.hasKey("ContainerInventory")) {
            legacyTankItemHandlerNBTReading(this, data.getCompoundTag("ContainerInventory"), 0, 1);
        }
        this.autoOutputFluids = data.getBoolean("AutoOutputFluids");
        this.outputFacing = EnumFacing.VALUES[data.getInteger("OutputFacing")];
        this.voiding = data.getBoolean("IsVoiding") || data.getBoolean("IsPartiallyVoiding");
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

        if (tag.getBoolean("IsVoiding") || tag.getBoolean("IsPartialVoiding")) {
            this.setVoiding(true);
        }
        boolean a = tag.hasKey("AspectFilter");
        if (a)
        {
            this.aspectFilter = Aspect.getAspect(tag.getString("AspectFilter"));
            this.writeAspectFilter(this.aspectFilter);
        }

        NBTTagList nbta = tag.getTagList("Aspects", 10);
        if (!nbta.isEmpty())
        {
            NBTTagCompound b = (NBTTagCompound) nbta.get(0);
            this.amount = b.getInteger("amount");
            String key = b.getString("key");
            this.aspect = Aspect.getAspect(key);
        }

    }

    public void writeItemStackData(NBTTagCompound tag) {
        super.writeItemStackData(tag);
        if (this.voiding) {
            tag.setBoolean("IsVoiding", true);
        }

        if (this.aspectFilter != null)
        {
            tag.setString("AspectFilter", this.aspectFilter.getTag());
        }

        if (this.aspect != null)
        {
            NBTTagList tlist = new NBTTagList();
            tag.setTag("Aspects", tlist);
            AspectList list = this.getAspects();
            int size = list.size();

            for(int i = 0; i < size; ++i) {
                Aspect aspect1 = list.getAspects()[i];
                if (aspect1 != null) {
                    NBTTagCompound f = new NBTTagCompound();
                    f.setString("key", aspect1.getTag());
                    f.setInteger("amount", this.amount);
                    tlist.appendTag(f);
                }
            }
        }
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityAspectTank(this.metaTileEntityId, this.tier, this.maxAspectCapacity);
    }

    protected IItemHandlerModifiable createImportItemHandler() {
        return (new FilteredItemHandler(this, 1));
    }

    protected IItemHandlerModifiable createExportItemHandler() {
        return new GTItemStackHandler(this, 1);
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Textures.QUANTUM_STORAGE_RENDERER.renderMachine(renderState, translation, (IVertexOperation[])ArrayUtils.add(pipeline, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(this.getPaintingColorForRendering()))), this);
        ZZTextures.QUANTUM_ASPECT_TANK_OVERLAY.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        if (this.outputFacing != null) {
            ZZTextures.PIPE_ASPECT_OUT_OVERLAY.renderSided(this.outputFacing, renderState, translation, pipeline);
            if (this.isAutoOutputFluids()) {
                ZZTextures.ASPECT_OUTPUT_OVERLAY.renderSided(this.outputFacing, renderState, translation, pipeline);
            }
        }

        AspectStorageRenderer.renderTankAspect(renderState, translation, pipeline, this.aspect, this.maxAspectCapacity, this.amount, this.getWorld(), this.getPos(), this.getFrontFacing());
    }

    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        boolean flag = this.aspect != null || this.aspectFilter != null;
        if (flag)
        {
            AspectStorageRenderer.renderAspect(x, y, z, this.getFrontFacing(), this.aspect, this.aspectFilter);
            AspectStorageRenderer.renderAspectAmount(x, y, z, this.getFrontFacing(), this.amount);
        }
    }

    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.VOLTAGE_CASINGS[this.tier].getParticleSprite(), this.getPaintingColorForRendering());
    }

    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("zhenzigtqt.universal.tooltip.aspect_storage_capacity", new Object[]{this.maxAspectCapacity}));
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null) {
            NBTTagList nbta = tag.getTagList("Aspects", 10);
            if (!nbta.isEmpty())
            {
                NBTTagCompound b = (NBTTagCompound) nbta.get(0);
                String key = b.getString("key");
                Aspect aspect = Aspect.getAspect(key);
                if (aspect != null) {
                    tooltip.add(I18n.format("zhenzigtqt.universal.tooltip.aspect_storage", aspect.getLocalizedDescription(), b.getInteger("amount")));
                }
            }
            if (tag.hasKey("AspectFilter"))
            {
                Aspect aspectFilter = Aspect.getAspect(tag.getString("AspectFilter"));
                if (aspectFilter != null) {
                    tooltip.add(I18n.format("zhenzigtqt.universal.tooltip.aspect_locked", aspectFilter.getLocalizedDescription()));
                }
            }
            if (tag.getBoolean("IsVoiding") || tag.getBoolean("IsPartialVoiding")) {
                tooltip.add(I18n.format("gregtech.machine.quantum_tank.tooltip.voiding_enabled"));
            }
        }
    }

    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.auto_output_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    protected ModularUI createUI(EntityPlayer entityPlayer) {

        ModularUI.Builder builder = ModularUI.defaultBuilder();

        QuantumAspectTankMainWidget mainWidget = new QuantumAspectTankMainWidget(0, 0, this, 4);

        builder.widget(mainWidget).bindPlayerInventory(entityPlayer.inventory);

        return builder.build(this.getHolder(), entityPlayer);
    }

    public ToggleButtonWidget createOUTPUTui(int x, int y){
        return new ToggleButtonWidget(x, y, 18, 18, GuiTextures.BUTTON_FLUID_OUTPUT, this::isAutoOutputFluids, this::setAutoOutputFluids).setTooltipText("gregtech.gui.fluid_auto_output.tooltip", new Object[0]).shouldUseBaseBackground();
    }

    public ToggleButtonWidget createVoidUi(int x, int y){
        return new ToggleButtonWidget(x, y, 18, 18, GuiTextures.BUTTON_FLUID_VOID, this::isVoiding, this::setVoiding).setTooltipText("gregtech.gui.fluid_voiding.tooltip", new Object[0]).shouldUseBaseBackground();
    }

    public Consumer<List<ITextComponent>> getAspectText() {
        return (list) -> {
            String aspectText = "";
            if (this.aspect == null) {
                if (this.aspectFilter != null) {
                    aspectText = this.aspectFilter.getLocalizedDescription();
                }
            } else {
                aspectText = this.aspect.getLocalizedDescription();
            }

            if (!aspectText.isEmpty()) {
                list.add(new TextComponentString(aspectText));
            }

        };
    }

    public Consumer<List<ITextComponent>> getAspectAmountText() {
        return (list) -> {
            String fluidAmount = "";
            if (this.amount == 0) {
                if (this.aspectFilter != null) {
                    fluidAmount = "0";
                }
            } else {
                fluidAmount = String.valueOf(this.amount);
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

    public boolean isAutoOutputItems() {
        return false;
    }

    public boolean isAutoOutputFluids() {
        return this.autoOutputFluids;
    }

    public boolean isAllowInputFromOutputSideItems() {
        return false;
    }

    public boolean isAllowInputFromOutputSideFluids() {
        return this.allowInputFromOutputSide;
    }

    public void writeAspectFilter(Aspect aspect)
    {
        this.writeCustomData(GregtechDataCodes.UPDATE_LOCKED_STATE, (buf ->
        {
            buf.writeString(aspect != null ? aspect.getTag() : "");
        }));
    }

    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 200)
        {
            String a = buf.readString(100);
            if (!a.isEmpty())
            {
                this.aspect = Aspect.getAspect(a);
                this.scheduleRenderUpdate();
            }
        }   else if (dataId == 201)
        {
            String aspect = buf.readString(100);
            int amount = buf.readInt();
            boolean updateRendering = buf.readBoolean();
            this.amount = Math.min(amount, this.maxAspectCapacity);
            this.aspect = Aspect.getAspect(aspect);
            if (updateRendering)
            {
                this.scheduleRenderUpdate();
            }
        }   else if (dataId == GregtechDataCodes.UPDATE_LOCKED_STATE)
        {
            String aspect = buf.readString(100);

            this.aspectFilter = Aspect.getAspect(aspect);
            this.scheduleRenderUpdate();

        }
        if (dataId == GregtechDataCodes.UPDATE_OUTPUT_FACING) {
            this.outputFacing = EnumFacing.VALUES[buf.readByte()];
            this.scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.UPDATE_AUTO_OUTPUT_FLUIDS) {
            this.autoOutputFluids = buf.readBoolean();
            this.scheduleRenderUpdate();
        }  else if (dataId == GregtechDataCodes.UPDATE_IS_VOIDING) {
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
        buf.writeBoolean(this.voiding);
        buf.writeString(this.aspect == null ? "" : this.aspect.getTag());
        buf.writeString(this.aspectFilter == null ? "" : this.aspectFilter.getTag());
        buf.writeInt(this.amount);
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
        this.voiding = buf.readBoolean();

        String aspect = buf.readString(100);
        String aspectFilter = buf.readString(100);
        if (!aspect.isEmpty())
        {
            this.aspect = Aspect.getAspect(aspect);
        }
        if (!aspectFilter.isEmpty())
        {
            this.aspectFilter = Aspect.getAspect(aspectFilter);
        }
        this.amount = buf.readInt();

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
        return super.getCapability(capability, side);
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
        return super.onScrewdriverClick(playerIn, hand, facing, hitResult);
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

    public void pushAspectIntoNearbyHandlers(EnumFacing[] facings)
    {
        for (EnumFacing facing : facings)
        {
            if (GTEssentiaHandler.addEssentiaToTile(this, this.aspect, facing, false, 5))
            {
                this.takeFromContainer(this.aspect, 1);
            }   else if (GTEssentiaHandler.addEssentiaToMTE(this, this.aspect, facing, false, 5))
            {
                this.takeFromContainer(this.aspect, 1);
            }
        }

    }

    public void setAspects(ItemStack itemstack, AspectList aspects) {
        if (!itemstack.hasTagCompound()) {
            itemstack.setTagCompound(new NBTTagCompound());
        }

        aspects.writeToNBT(itemstack.getTagCompound());
    }

    @Override
    public boolean isBlocked() {
        return false;
    }

    @Override
    public AspectList getAspects() {
        AspectList al = new AspectList();
        if (this.aspect != null && this.amount > 0) {
            al.add(this.aspect, this.amount);
        }

        return al;
    }

    @Override
    public void setAspects(AspectList aspectList) {
        if (aspectList != null && aspectList.size() > 0) {
            this.aspect = aspectList.getAspectsSortedByAmount()[0];
            this.amount = aspectList.getAmount(aspectList.getAspectsSortedByAmount()[0]);
        }
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return this.aspectFilter == null || aspect.equals(this.aspectFilter);
    }

    @Override
    public int addToContainer(Aspect aspect, int i) {
        if (aspectFilter != null && aspect != aspectFilter)
        {
            return i;
        }
        if (i != 0) {
            int testAmount = this.amount;
            if (testAmount == this.maxAspectCapacity && this.isVoiding() && aspect == this.aspect)
            {
                i = 0;
            }   else
            if (testAmount < this.maxAspectCapacity && aspect == this.aspect || testAmount == 0) {
                this.aspect = aspect;
                int added = Math.min(i, this.maxAspectCapacity - testAmount);
                this.amount += added;
                i -= added;
            }

            IBlockState state = this.getWorld().getBlockState(this.getPos());
//            this.getWorld().notifyBlockUpdate(this.getPos(), state, state, 2 + (rerender ? 4 : 0));
            this.markDirty();
        }
        return i;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int i) {
        if (this.amount >= i && aspect == this.aspect) {
            this.amount -= i;
            if (this.amount <= 0) {
                this.aspect = null;
                this.amount = 0;
            }

//            this.syncTile(false);
            this.markDirty();
            return true;
        } else {
            return false;
        }
    }

    public ItemStack takeFromContainer(ItemStack itemStack, boolean similar) {
        Item item = itemStack.getItem();
        if (item instanceof BlockJarItem) {
            if (!itemStack.hasTagCompound()) {
                int currentAmount = this.amount;
                int fillAmount = 0;
                currentAmount -= 250;
                if (currentAmount <= 0) {
                    fillAmount = this.amount;
                    ItemStack newItem = itemStack.copy();

                    ((BlockJarItem) newItem.getItem()).setAspects(newItem, (new AspectList()).add(this.aspect, fillAmount));
                    if (!similar)
                    {
                        this.amount = 0;
                        this.aspect = null;
                        this.markDirty();
                    }

                    return newItem;
                } else {
                    fillAmount = 250;
                    ItemStack newItem = itemStack.copy();
                    ((BlockJarItem) newItem.getItem()).setAspects(newItem, (new AspectList()).add(this.aspect, fillAmount));
                    if (!similar)
                    {
                        this.amount = currentAmount;
                        this.markDirty();
                    }
                    return newItem;
                }
            }
        } else if (item instanceof ItemPhial) {
            if (!itemStack.hasTagCompound()) {
                int currentAmount = this.amount;
                currentAmount -= 10;
                if (currentAmount >= 0) {
                    ItemStack newItem = itemStack.copy();
                    newItem = ItemPhial.makePhial(this.aspect, 10);
                    if(!similar)
                    {
                        this.amount = currentAmount;
                        this.markDirty();
                    }
                    return newItem;
                }
            }
        } else if (!this.checkItemIsMTE(item).isEmpty()) {
            MetaTileEntityAspectTank metaTileEntity = (MetaTileEntityAspectTank) GTUtility.getMetaTileEntity(itemStack);
            if (!itemStack.hasTagCompound()) {
                int cap = metaTileEntity.getMaxAspectCapacity();
                int currentAmount = this.amount;
                int fillAmount = 0;
                currentAmount -= cap;
                if (currentAmount <= 0) {
                    fillAmount = this.amount;
                    ItemStack newItem = itemStack.copy();
                    this.setAspects(newItem, (new AspectList()).add(this.aspect, fillAmount));
                    if (!similar)
                    {
                        this.amount = 0;
                        this.aspect = null;
                        this.markDirty();
                    }
                    this.writeCustomData(201, (buf) -> {
                        buf.writeString("");
                        buf.writeInt(0);
                        buf.writeBoolean(true);
                    });
                    return newItem;
                } else {
                    fillAmount = metaTileEntity.getMaxAspectCapacity();
                    ItemStack newItem = itemStack.copy();
                    this.setAspects(newItem, (new AspectList()).add(this.aspect, fillAmount));
                    if (!similar)
                    {
                        this.amount = currentAmount;
                        this.markDirty();
                    }
                    return newItem;
                }
            }
            return ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }


    @Override
    public boolean takeFromContainer(AspectList aspectList) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int i) {
        return this.amount >= i && aspect == this.aspect;
    }

    @Override
    public boolean doesContainerContain(AspectList aspectList) {
        Aspect[] al = aspectList.getAspects();
        int al_length = al.length;

        for(int i = 0; i < al_length; ++i) {
            Aspect tt = al[i];
            if (this.amount > 0 && tt == this.aspect) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int containerContains(Aspect aspect) {
        return aspect == this.aspect ? this.amount : 0;
    }

    @Override
    public boolean isConnectable(EnumFacing enumFacing) {
        return enumFacing == EnumFacing.UP;
    }

    @Override
    public boolean canInputFrom(EnumFacing enumFacing) {
        return enumFacing == EnumFacing.UP;
    }

    @Override
    public boolean canOutputTo(EnumFacing enumFacing) {
        return enumFacing == EnumFacing.UP;
    }

    @Override
    public void setSuction(Aspect aspect, int i) {

    }

    @Override
    public Aspect getSuctionType(EnumFacing enumFacing) {
        return this.aspectFilter != null ? this.aspectFilter : this.aspect;
    }

    @Override
    public int getSuctionAmount(EnumFacing enumFacing) {
        if (this.amount < 250) {
            return this.aspectFilter != null ? 64 : 32;
        } else {
            return 0;
        }
    }

    @Override
    public int takeEssentia(Aspect aspect, int i, EnumFacing enumFacing) {
        return this.canOutputTo(enumFacing) && this.takeFromContainer(aspect, i) ? i : 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int i, EnumFacing enumFacing) {
        return this.canInputFrom(enumFacing) ? i - this.addToContainer(aspect, i) : 0;
    }

    @Override
    public Aspect getEssentiaType(EnumFacing enumFacing) {
        return this.aspect;
    }

    @Override
    public int getEssentiaAmount(EnumFacing enumFacing) {
        return this.amount;
    }

    @Override
    public int getMinimumSuction() {
        return this.aspectFilter != null ? 64 : 32;
    }
}

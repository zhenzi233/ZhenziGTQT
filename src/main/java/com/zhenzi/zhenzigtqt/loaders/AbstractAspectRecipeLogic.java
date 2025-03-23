package com.zhenzi.zhenzigtqt.loaders;

import com.zhenzi.zhenzigtqt.common.lib.aspect.*;
import com.zhenzi.zhenzigtqt.common.metatileentity.AspectMetaTileEntity;
import gregtech.api.GTValues;
import gregtech.api.capability.*;
import gregtech.api.metatileentity.IVoidable;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.metatileentity.multiblock.ICleanroomProvider;
import gregtech.api.metatileentity.multiblock.ICleanroomReceiver;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.recipes.FluidKey;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.IParallelableRecipeLogic;
import gregtech.api.recipes.logic.OverclockingLogic;
import gregtech.api.recipes.logic.ParallelLogic;
import gregtech.api.recipes.recipeproperties.CleanroomProperty;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;
import gregtech.api.util.GTHashMaps;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static gregtech.api.recipes.logic.ParallelLogic.getMaxRecipeMultiplier;

public abstract class AbstractAspectRecipeLogic extends MTETrait implements IWorkable, IParallelableAspectRecipeLogic {
    private static final String ALLOW_OVERCLOCKING = "AllowOverclocking";
    private static final String OVERCLOCK_VOLTAGE = "OverclockVoltage";
    private final AspectRecipeMap<?> recipeMap;
    private double euDiscount = -1.0;
    private double speedBonus = -1.0;
    protected AspectRecipe previousRecipe;
    private boolean allowOverclocking = true;
    protected int parallelRecipesPerformed;
    private long overclockVoltage = 0L;
    protected int[] overclockResults;
    protected boolean canRecipeProgress = true;
    protected int progressTime;
    protected int maxProgressTime;
    protected int recipeEUt;
    protected List<FluidStack> fluidOutputs;
    protected List<AspectStack> aspectOutputs;
    protected NonNullList<ItemStack> itemOutputs;
    protected boolean isActive;
    protected boolean workingEnabled = true;
    protected boolean hasNotEnoughEnergy;
    protected boolean wasActiveAndNeedsUpdate;
    protected boolean isOutputsFull;
    protected boolean invalidInputsForRecipes;
    protected boolean hasPerfectOC = false;
    private int parallelLimit = 1;
    protected final AspectMetaTileEntity AmetaTileEntity;

    public AbstractAspectRecipeLogic(AspectMetaTileEntity tileEntity, AspectRecipeMap<?> recipeMap) {
        super(tileEntity);
        this.recipeMap = recipeMap;
        this.AmetaTileEntity = tileEntity;
    }

    public AbstractAspectRecipeLogic(AspectMetaTileEntity tileEntity, AspectRecipeMap<?> recipeMap, boolean hasPerfectOC) {
        super(tileEntity);
        this.recipeMap = recipeMap;
        this.hasPerfectOC = hasPerfectOC;
        this.AmetaTileEntity = tileEntity;
    }

    protected abstract long getEnergyInputPerSecond();

    protected abstract long getEnergyStored();

    protected abstract long getEnergyCapacity();

    protected abstract boolean drawEnergy(int var1, boolean var2);

    public abstract long getMaxVoltage();

    protected long getMaxParallelVoltage() {
        return this.getMaxVoltage();
    }

    protected IItemHandlerModifiable getInputInventory() {
        return this.AmetaTileEntity.getImportItems();
    }

    protected IItemHandlerModifiable getOutputInventory() {
        return this.AmetaTileEntity.getExportItems();
    }

    protected IMultipleTankHandler getInputTank() {
        return this.AmetaTileEntity.getImportFluids();
    }

    protected IMultipleTankHandler getOutputTank() {
        return this.AmetaTileEntity.getExportFluids();
    }

    protected IMultipleAspectTankHandler getInputAspectTank() {
        return this.AmetaTileEntity.getImportAspects();
    }

    protected IMultipleAspectTankHandler getOutputAspectTank() {
        return this.AmetaTileEntity.getImportAspects();
    }

    public boolean consumesEnergy() {
        return true;
    }

    public final @NotNull String getName() {
        return "RecipeMapWorkable";
    }

    public <T> T getCapability(Capability<T> capability) {
        if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE) {
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        } else if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        } else {
            return capability == CapabilityAspectHandler.CAPABILITY_ASPECT_RECIPE_LOGIC ? CapabilityAspectHandler.CAPABILITY_ASPECT_RECIPE_LOGIC.cast(this) : null;
        }
    }

    public void update() {
        World world = this.getMetaTileEntity().getWorld();
        if (world != null && !world.isRemote) {
            if (this.workingEnabled) {
                if (this.getMetaTileEntity().getOffsetTimer() % 20L == 0L) {
                    this.canRecipeProgress = this.canProgressRecipe();
                }

                if (this.progressTime > 0) {
                    this.updateRecipeProgress();
                }

                if (this.progressTime == 0 && this.shouldSearchForRecipes()) {
                    this.trySearchNewRecipe();
                }
            }

            if (this.wasActiveAndNeedsUpdate) {
                this.wasActiveAndNeedsUpdate = false;
                this.setActive(false);
            }
        }

    }

    @Override
    public void applyParallelBonus(@NotNull AspectRecipeBuilder<?> builder) {
        IParallelableAspectRecipeLogic.super.applyParallelBonus(builder);
    }

    @Override
    public AspectRecipeBuilder<?> findMultipliedParallelRecipe(@NotNull AspectRecipeMap<?> recipeMap, @NotNull AspectRecipe currentRecipe, @NotNull IItemHandlerModifiable inputs, @NotNull IMultipleTankHandler fluidInputs, @NotNull IMultipleAspectTankHandler aspectInputs, @NotNull IItemHandlerModifiable outputs, @NotNull IMultipleTankHandler fluidOutputs, @NotNull IMultipleAspectTankHandler aspectOutputs, int parallelLimit, long maxVoltage, @NotNull IVoidableAspect voidable) {
        return IParallelableAspectRecipeLogic.super.findMultipliedParallelRecipe(recipeMap, currentRecipe, inputs, fluidInputs, aspectInputs, outputs, fluidOutputs, aspectOutputs, parallelLimit, maxVoltage, voidable);
    }

    @Override
    public AspectRecipeBuilder<?> findAppendedParallelItemRecipe(@NotNull AspectRecipeMap<?> recipeMap, @NotNull IItemHandlerModifiable inputs, @NotNull IItemHandlerModifiable outputs, int parallelLimit, long maxVoltage, @NotNull IVoidable voidable) {
        return IParallelableAspectRecipeLogic.super.findAppendedParallelItemRecipe(recipeMap, inputs, outputs, parallelLimit, maxVoltage, voidable);
    }

    @Override
    public AspectRecipe findParallelRecipe(@NotNull AspectRecipe currentRecipe, @NotNull IItemHandlerModifiable inputs, @NotNull IMultipleTankHandler fluidInputs, @NotNull IMultipleAspectTankHandler aspectInputs, @NotNull IItemHandlerModifiable outputs, @NotNull IMultipleTankHandler fluidOutputs, @NotNull IMultipleAspectTankHandler aspectOutputs, long maxVoltage, int parallelLimit) {
        return IParallelableAspectRecipeLogic.super.findParallelRecipe(currentRecipe, inputs, fluidInputs, aspectInputs, outputs, fluidOutputs, aspectOutputs, maxVoltage, parallelLimit);
    }

    public @Nullable AspectRecipeMap<?> getRecipeMap() {
        return this.recipeMap;
    }

    public @Nullable AspectRecipe getPreviousRecipe() {
        return this.previousRecipe;
    }

    protected boolean shouldSearchForRecipes() {
        return this.canWorkWithInputs() && this.canFitNewOutputs();
    }

    protected boolean hasNotifiedInputs() {
        return this.AmetaTileEntity.getNotifiedItemInputList().size() > 0 || this.AmetaTileEntity.getNotifiedFluidInputList().size() > 0 || this.AmetaTileEntity.getNotifiedAspectInputList().size() > 0;
    }

    protected boolean hasNotifiedOutputs() {
        return this.AmetaTileEntity.getNotifiedItemOutputList().size() > 0 || this.AmetaTileEntity.getNotifiedFluidOutputList().size() > 0 || this.AmetaTileEntity.getNotifiedAspectOutputList().size() > 0;
    }

    protected boolean canFitNewOutputs() {
        if (this.isOutputsFull && !this.hasNotifiedOutputs()) {
            return false;
        } else {
            this.isOutputsFull = false;
            this.AmetaTileEntity.getNotifiedItemOutputList().clear();
            this.AmetaTileEntity.getNotifiedFluidOutputList().clear();
            this.AmetaTileEntity.getNotifiedAspectOutputList().clear();
            return true;
        }
    }

    protected boolean canWorkWithInputs() {
        if (this.invalidInputsForRecipes && !this.hasNotifiedInputs()) {
            return false;
        } else {
            this.isOutputsFull = false;
            this.invalidInputsForRecipes = false;
            this.AmetaTileEntity.getNotifiedItemInputList().clear();
            this.AmetaTileEntity.getNotifiedFluidInputList().clear();
            this.AmetaTileEntity.getNotifiedAspectInputList().clear();
            return true;
        }
    }

    public void invalidateInputs() {
        this.invalidInputsForRecipes = true;
    }

    public void invalidateOutputs() {
        this.isOutputsFull = true;
    }

    public void setParallelRecipesPerformed(int amount) {
        this.parallelRecipesPerformed = amount;
    }

    protected void updateRecipeProgress() {
        if (this.canRecipeProgress && this.drawEnergy(this.recipeEUt, true)) {
            this.drawEnergy(this.recipeEUt, false);
            if (++this.progressTime > this.maxProgressTime) {
                this.completeRecipe();
            }

            if (this.hasNotEnoughEnergy && this.getEnergyInputPerSecond() > 19L * (long)this.recipeEUt) {
                this.hasNotEnoughEnergy = false;
            }
        } else if (this.recipeEUt > 0) {
            this.hasNotEnoughEnergy = true;
            this.decreaseProgress();
        }

    }

    protected void decreaseProgress() {
        if (this.progressTime >= 2) {
            if (ConfigHolder.machines.recipeProgressLowEnergy) {
                this.progressTime = 1;
            } else {
                this.progressTime = Math.max(1, this.progressTime - 2);
            }
        }

    }

    protected boolean canProgressRecipe() {
        return this.previousRecipe == null ? true : this.checkCleanroomRequirement(this.previousRecipe);
    }

    public void forceRecipeRecheck() {
        this.previousRecipe = null;
        this.trySearchNewRecipe();
    }

    protected void trySearchNewRecipe() {
        long maxVoltage = this.getMaxVoltage();
        IItemHandlerModifiable importInventory = this.getInputInventory();
        IMultipleTankHandler importFluids = this.getInputTank();
        IMultipleAspectTankHandler importAspects = this.getInputAspectTank();
        AspectRecipe currentRecipe;
        if (this.checkPreviousRecipe()) {
            currentRecipe = this.previousRecipe;
        } else {
            currentRecipe = this.findRecipe(maxVoltage, importInventory, importFluids, importAspects);
        }

        if (currentRecipe != null) {
            this.previousRecipe = currentRecipe;
        }

        this.invalidInputsForRecipes = currentRecipe == null;
        if (currentRecipe != null && this.checkRecipe(currentRecipe)) {
            this.prepareRecipe(currentRecipe);
        }

    }

    protected boolean checkPreviousRecipe() {
        if (this.previousRecipe == null) {
            return false;
        } else {
            return (long)this.previousRecipe.getEUt() > this.getMaxVoltage() ? false : this.previousRecipe.matches(false, this.getInputInventory(), this.getInputTank(), this.getInputAspectTank());
        }
    }

    public boolean checkRecipe(@NotNull AspectRecipe recipe) {
        return this.checkCleanroomRequirement(recipe);
    }

    public @NotNull AspectMetaTileEntity getMetaTileEntity() {
        return this.AmetaTileEntity;
    }
    public @NotNull AspectMetaTileEntity getAMetaTileEntity() {
        return this.AmetaTileEntity;
    }
    protected boolean checkCleanroomRequirement(@NotNull AspectRecipe recipe) {
        CleanroomType requiredType = (CleanroomType)recipe.getProperty(CleanroomProperty.getInstance(), null);
        if (requiredType == null) {
            return true;
        } else {
            AspectMetaTileEntity mte = this.getAMetaTileEntity();
            if (!(mte instanceof ICleanroomReceiver)) {
                return false;
            } else {
                ICleanroomReceiver receiver = (ICleanroomReceiver)mte;
                if (ConfigHolder.machines.cleanMultiblocks && mte instanceof IMultiblockController) {
                    return true;
                } else {
                    ICleanroomProvider cleanroomProvider = receiver.getCleanroom();
                    if (cleanroomProvider == null) {
                        return false;
                    } else {
                        return cleanroomProvider.isClean() && cleanroomProvider.checkCleanroomType(requiredType);
                    }
                }
            }
        }
    }

    public boolean prepareRecipe(AspectRecipe recipe, IItemHandlerModifiable inputInventory, IMultipleTankHandler inputFluidInventory, IMultipleAspectTankHandler inputAspectInventory) {
        recipe = AspectRecipe.trimRecipeOutputs(recipe, this.getRecipeMap(), this.AmetaTileEntity.getItemOutputLimit(), this.AmetaTileEntity.getFluidOutputLimit(), -1);
        if (this.euDiscount > 0.0 || this.speedBonus > 0.0) {
            AspectRecipeBuilder<?> builder = new AspectRecipeBuilder(recipe, this.recipeMap);
            int duration;
            if (this.euDiscount > 0.0) {
                duration = (int)Math.round((double)recipe.getEUt() * this.euDiscount);
                if (duration <= 0) {
                    duration = 1;
                }

                builder.EUt(duration);
            }

            if (this.speedBonus > 0.0) {
                duration = recipe.getDuration();
                int newDuration = (int)Math.round((double)duration * this.speedBonus);
                if (newDuration <= 0) {
                    newDuration = 1;
                }

                builder.duration(newDuration);
            }

            recipe = (AspectRecipe)builder.build().getResult();
        }

        recipe = (AspectRecipe) this.findParallelRecipe(recipe,
                inputInventory, inputFluidInventory, inputAspectInventory,
                this.getOutputInventory(), this.getOutputTank(), this.getOutputAspectTank(),
                this.getMaxParallelVoltage(), this.getParallelLimit());
        if (recipe != null && this.setupAndConsumeRecipeInputs(recipe, inputInventory, inputFluidInventory, inputAspectInventory)) {
            this.setupRecipe(recipe);
            return true;
        } else {
            return false;
        }
    }

    public boolean prepareRecipe(AspectRecipe recipe) {
        return this.prepareRecipe(recipe, this.getInputInventory(), this.getInputTank(), this.getInputAspectTank());
    }

    public int getParallelLimit() {
        return this.parallelLimit;
    }

    public void setParallelLimit(int amount) {
        this.parallelLimit = amount;
    }

    public void setEUDiscount(double discount) {
        if (discount <= 0.0) {
            GTLog.logger.warn("Cannot set EU discount for recipe logic to {}, discount must be > 0", discount);
        } else {
            this.euDiscount = discount;
        }
    }

    public double getEUtDiscount() {
        return this.euDiscount;
    }

    public void setSpeedBonus(double bonus) {
        if (bonus <= 0.0) {
            GTLog.logger.warn("Cannot set speed bonus for recipe logic to {}, bonus must be > 0", bonus);
        } else {
            this.speedBonus = bonus;
        }
    }

    public double getSpeedBonus() {
        return this.speedBonus;
    }

    public @NotNull ParallelLogicType getParallelLogicType() {
        return ParallelLogicType.MULTIPLY;
    }

    protected static int getMinTankCapacity(@NotNull IMultipleTankHandler tanks) {
        if (tanks.getTanks() == 0) {
            return 0;
        } else {
            int result = Integer.MAX_VALUE;

            IFluidTank fluidTank;
            for(Iterator var2 = tanks.getFluidTanks().iterator(); var2.hasNext(); result = Math.min(fluidTank.getCapacity(), result)) {
                fluidTank = (IFluidTank)var2.next();
            }

            return result;
        }
    }

    protected static int getMinAspectTankCapacity(@NotNull IMultipleAspectTankHandler tanks) {
        if (tanks.getTanks() == 0) {
            return 0;
        } else {
            int result = Integer.MAX_VALUE;

            IAspectTank fluidTank;
            for(Iterator var2 = tanks.getFluidTanks().iterator(); var2.hasNext(); result = Math.min(fluidTank.getCapacity(), result)) {
                fluidTank = (IAspectTank)var2.next();
            }

            return result;
        }
    }

    protected @Nullable AspectRecipe findRecipe(long maxVoltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, IMultipleAspectTankHandler aspectInputs) {
        AspectRecipeMap<?> map = this.getRecipeMap();
        return map != null && this.isRecipeMapValid(map) ? map.findRecipe(maxVoltage, inputs, fluidInputs, aspectInputs) : null;
    }

    public boolean isRecipeMapValid(@NotNull AspectRecipeMap<?> recipeMap) {
        return true;
    }

    protected static boolean areItemStacksEqual(@NotNull ItemStack stackA, @NotNull ItemStack stackB) {
        return stackA.isEmpty() && stackB.isEmpty() || ItemStack.areItemsEqual(stackA, stackB) && ItemStack.areItemStackTagsEqual(stackA, stackB);
    }

    protected boolean setupAndConsumeRecipeInputs(@NotNull AspectRecipe recipe, @NotNull IItemHandlerModifiable importInventory, @NotNull IMultipleTankHandler importFluids, @NotNull IMultipleAspectTankHandler importAspects) {
        this.overclockResults = this.calculateOverclock(recipe);
        this.modifyOverclockPost(this.overclockResults, recipe.getRecipePropertyStorage());
        if (!this.hasEnoughPower(this.overclockResults)) {
            return false;
        } else {
            IItemHandlerModifiable exportInventory = this.getOutputInventory();
            IMultipleTankHandler exportFluids = this.getOutputTank();
            IMultipleAspectTankHandler exportAspects = this.getOutputAspectTank();
            if (!this.AmetaTileEntity.canVoidRecipeItemOutputs() && !GTTransferUtils.addItemsToItemHandler(exportInventory, true, recipe.getAllItemOutputs())) {
                this.isOutputsFull = true;
                return false;
            } else if (!this.AmetaTileEntity.canVoidRecipeFluidOutputs() && !GTTransferUtils.addFluidsToFluidHandler(exportFluids, true, recipe.getAllFluidOutputs())) {
                this.isOutputsFull = true;
                return false;
            } else if (!this.AmetaTileEntity.canVoidRecipeAspectOutputs() && !AspectUtil.addAspectsToAspectHandler(exportAspects, true, recipe.getAllAspectOutputs())) {
                this.isOutputsFull = true;
                return false;
            }else {
                this.isOutputsFull = false;
                if (recipe.matches(true, importInventory, importFluids, importAspects)) {
                    this.AmetaTileEntity.addNotifiedInput(importInventory);
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    protected boolean setupAndConsumeRecipeInputs(@NotNull AspectRecipe recipe, @NotNull IItemHandlerModifiable importInventory) {
        return this.setupAndConsumeRecipeInputs(recipe, importInventory, this.getInputTank(), this.getInputAspectTank());
    }

    protected boolean hasEnoughPower(int @NotNull [] resultOverclock) {
        int recipeEUt = resultOverclock[0];
        if (recipeEUt >= 0) {
            long stored = this.getEnergyStored();
            long eu = (long)recipeEUt << 3;
            return stored >= eu;
        } else {
            return this.getEnergyStored() - (long)recipeEUt <= this.getEnergyCapacity();
        }
    }

    protected void modifyOverclockPost(int[] overclockResults, @NotNull IRecipePropertyStorage storage) {
    }

    protected @NotNull int[] calculateOverclock(@NotNull AspectRecipe recipe) {
        return this.performOverclocking(recipe);
    }

    protected int @NotNull [] performOverclocking(@NotNull AspectRecipe recipe) {
        int[] values = new int[]{recipe.getEUt(), recipe.getDuration(), this.getNumberOfOCs(recipe.getEUt())};
        this.modifyOverclockPre(values, recipe.getRecipePropertyStorage());
        return values[2] <= 0 ? new int[]{values[0], values[1]} : this.runOverclockingLogic(recipe.getRecipePropertyStorage(), values[0], this.getMaximumOverclockVoltage(), values[1], values[2]);
    }

    protected int getNumberOfOCs(int recipeEUt) {
        if (!this.isAllowOverclocking()) {
            return 0;
        } else {
            int recipeTier = GTUtility.getTierByVoltage((long)recipeEUt);
            int maximumTier = this.getOverclockForTier(this.getMaximumOverclockVoltage());
            if (maximumTier <= 1) {
                return 0;
            } else {
                int numberOfOCs = maximumTier - recipeTier;
                if (recipeTier == 0) {
                    --numberOfOCs;
                }

                return numberOfOCs;
            }
        }
    }

    protected void modifyOverclockPre(@NotNull int[] values, @NotNull IRecipePropertyStorage storage) {
    }

    protected @NotNull int[] runOverclockingLogic(@NotNull IRecipePropertyStorage propertyStorage, int recipeEUt, long maxVoltage, int duration, int amountOC) {
        return OverclockingLogic.standardOverclockingLogic(Math.abs(recipeEUt), maxVoltage, duration, amountOC, this.getOverclockingDurationDivisor(), this.getOverclockingVoltageMultiplier());
    }

    protected double getOverclockingDurationDivisor() {
        return this.hasPerfectOC ? 4.0 : 2.0;
    }

    protected double getOverclockingVoltageMultiplier() {
        return 4.0;
    }

    protected int getOverclockForTier(long voltage) {
        return GTUtility.getTierByVoltage(voltage);
    }

    public String[] getAvailableOverclockingTiers() {
        int maxTier = this.getOverclockForTier(this.getMaxVoltage());
        String[] result = new String[maxTier + 1];
        result[0] = "gregtech.gui.overclock.off";
        if (maxTier >= 0) {
            System.arraycopy(GTValues.VNF, 1, result, 1, maxTier);
        }

        return result;
    }

    protected void setupRecipe(AspectRecipe recipe) {
        this.progressTime = 1;
        this.setMaxProgress(this.overclockResults[1]);
        this.recipeEUt = this.overclockResults[0];
        int recipeTier = GTUtility.getTierByVoltage((long)recipe.getEUt());
        int machineTier = this.getOverclockForTier(this.getMaximumOverclockVoltage());
        this.aspectOutputs = AspectUtil.copyAspectList(recipe.getResultAspectOutputs(recipeTier, machineTier, this.getRecipeMap()));
        this.fluidOutputs = GTUtility.copyFluidList(recipe.getResultFluidOutputs(recipeTier, machineTier, this.getRecipeMap()));
        this.itemOutputs = GTUtility.copyStackList(recipe.getResultItemOutputs(recipeTier, machineTier, this.getRecipeMap()));
        if (this.wasActiveAndNeedsUpdate) {
            this.wasActiveAndNeedsUpdate = false;
        } else {
            this.setActive(true);
        }

    }

    protected void completeRecipe() {
        this.outputRecipeOutputs();
        this.progressTime = 0;
        this.setMaxProgress(0);
        this.recipeEUt = 0;
        this.aspectOutputs = null;
        this.fluidOutputs = null;
        this.itemOutputs = null;
        this.hasNotEnoughEnergy = false;
        this.wasActiveAndNeedsUpdate = true;
        this.parallelRecipesPerformed = 0;
        this.overclockResults = new int[]{0, 0};
    }

    protected void outputRecipeOutputs() {
        GTTransferUtils.addItemsToItemHandler(this.getOutputInventory(), false, this.itemOutputs);
        GTTransferUtils.addFluidsToFluidHandler(this.getOutputTank(), false, this.fluidOutputs);
        AspectUtil.addAspectsToAspectHandler(this.getOutputAspectTank(), false, this.aspectOutputs);
    }

    public double getProgressPercent() {
        return this.getMaxProgress() == 0 ? 0.0 : (double)this.getProgress() / ((double)this.getMaxProgress() * 1.0);
    }

    public int getProgress() {
        return this.progressTime;
    }

    public int getMaxProgress() {
        return this.maxProgressTime;
    }

    public int getRecipeEUt() {
        return this.recipeEUt;
    }

    public int getInfoProviderEUt() {
        return this.getRecipeEUt();
    }

    public int getPreviousRecipeDuration() {
        return this.getPreviousRecipe() == null ? 0 : this.getPreviousRecipe().getDuration();
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgressTime = maxProgress;
        this.AmetaTileEntity.markDirty();
    }

    protected void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            this.AmetaTileEntity.markDirty();
            World world = this.AmetaTileEntity.getWorld();
            if (world != null && !world.isRemote) {
                this.writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, (buf) -> {
                    buf.writeBoolean(active);
                });
            }
        }

    }

    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        this.AmetaTileEntity.markDirty();
        World world = this.AmetaTileEntity.getWorld();
        if (world != null && !world.isRemote) {
            this.writeCustomData(GregtechDataCodes.WORKING_ENABLED, (buf) -> {
                buf.writeBoolean(workingEnabled);
            });
        }

    }

    public boolean isHasNotEnoughEnergy() {
        return this.hasNotEnoughEnergy;
    }

    public boolean isWorkingEnabled() {
        return this.workingEnabled;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public boolean isWorking() {
        return this.isActive && !this.hasNotEnoughEnergy && this.workingEnabled;
    }

    public void setAllowOverclocking(boolean allowOverclocking) {
        this.allowOverclocking = allowOverclocking;
        this.overclockVoltage = allowOverclocking ? this.getMaximumOverclockVoltage() : GTValues.V[0];
        this.AmetaTileEntity.markDirty();
    }

    public boolean isAllowOverclocking() {
        return this.allowOverclocking;
    }

    public void setMaximumOverclockVoltage(long overclockVoltage) {
        this.overclockVoltage = overclockVoltage;
        this.allowOverclocking = overclockVoltage != GTValues.V[0];
        this.AmetaTileEntity.markDirty();
    }

    public long getMaximumOverclockVoltage() {
        return this.overclockVoltage;
    }

    public int getOverclockTier() {
        return !this.isAllowOverclocking() ? 0 : this.getOverclockForTier(this.overclockVoltage);
    }

    public void setOverclockTier(int tier) {
        this.setMaximumOverclockVoltage(GTValues.V[tier]);
    }

    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            this.isActive = buf.readBoolean();
            this.getMetaTileEntity().scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.workingEnabled = buf.readBoolean();
            this.getMetaTileEntity().scheduleRenderUpdate();
        }

    }

    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        buf.writeBoolean(this.isActive);
        buf.writeBoolean(this.workingEnabled);
    }

    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        this.isActive = buf.readBoolean();
        this.workingEnabled = buf.readBoolean();
    }

    public @NotNull NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("WorkEnabled", this.workingEnabled);
        compound.setBoolean("CanRecipeProgress", this.canRecipeProgress);
        compound.setBoolean("AllowOverclocking", this.allowOverclocking);
        compound.setLong("OverclockVoltage", this.overclockVoltage);
        if (this.progressTime > 0) {
            compound.setInteger("Progress", this.progressTime);
            compound.setInteger("MaxProgress", this.maxProgressTime);
            compound.setInteger("RecipeEUt", this.recipeEUt);

            NBTTagList itemOutputsList = new NBTTagList();
            if (!itemOutputsList.isEmpty())
            {
                for (ItemStack itemOutput : this.itemOutputs) {
                    itemOutputsList.appendTag(itemOutput.writeToNBT(new NBTTagCompound()));
                }
                compound.setTag("ItemOutputs", itemOutputsList);
            }


            NBTTagList fluidOutputsList = new NBTTagList();
            if (!fluidOutputsList.isEmpty()){
                for (FluidStack fluidOutput : this.fluidOutputs) {
                    fluidOutputsList.appendTag(fluidOutput.writeToNBT(new NBTTagCompound()));
                }
                compound.setTag("FluidOutputs", fluidOutputsList);
            }


            NBTTagList aspectOutputsList = new NBTTagList();
            if (!aspectOutputsList.isEmpty()){
                for (AspectStack fluidOutput : this.aspectOutputs) {
                    aspectOutputsList.appendTag(fluidOutput.writeToNBT(new NBTTagCompound()));
                }
                compound.setTag("AspectOutputs", aspectOutputsList);
            }
        }

        return compound;
    }

    public void deserializeNBT(@NotNull NBTTagCompound compound) {
        this.workingEnabled = compound.getBoolean("WorkEnabled");
        this.canRecipeProgress = compound.getBoolean("CanRecipeProgress");
        this.progressTime = compound.getInteger("Progress");
        this.allowOverclocking = compound.getBoolean("AllowOverclocking");
        this.overclockVoltage = compound.getLong("OverclockVoltage");
        this.isActive = false;
        if (this.progressTime > 0) {
            this.isActive = true;
            this.maxProgressTime = compound.getInteger("MaxProgress");
            this.recipeEUt = compound.getInteger("RecipeEUt");
            NBTTagList itemOutputsList = compound.getTagList("ItemOutputs", 10);
            this.itemOutputs = NonNullList.create();

            for(int i = 0; i < itemOutputsList.tagCount(); ++i) {
                this.itemOutputs.add(new ItemStack(itemOutputsList.getCompoundTagAt(i)));
            }

            NBTTagList fluidOutputsList = compound.getTagList("FluidOutputs", 10);
            this.fluidOutputs = new ArrayList();

            for(int i = 0; i < fluidOutputsList.tagCount(); ++i) {
                this.fluidOutputs.add(FluidStack.loadFluidStackFromNBT(fluidOutputsList.getCompoundTagAt(i)));
            }

            NBTTagList aspectOutputsList = compound.getTagList("AspectOutputs", 10);
            this.aspectOutputs = new ArrayList();

            for(int i = 0; i < aspectOutputsList.tagCount(); ++i) {
                this.aspectOutputs.add(AspectStack.loadAspectStackFromNBT(aspectOutputsList.getCompoundTagAt(i)));
            }
        }

    }
}

package com.zhenzi.zhenzigtqt.loaders;

import com.google.common.collect.ImmutableList;
import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectStack;
import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectUtil;
import com.zhenzi.zhenzigtqt.common.lib.aspect.IMultipleAspectTankHandler;
import com.zhenzi.zhenzigtqt.integration.jei.recipes.ZZRecipeCategory;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.category.GTRecipeCategory;
import gregtech.api.recipes.chance.boost.ChanceBoostFunction;
import gregtech.api.recipes.chance.output.ChancedOutputList;
import gregtech.api.recipes.chance.output.impl.ChancedFluidOutput;
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.recipeproperties.CleanroomProperty;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;
import gregtech.api.recipes.recipeproperties.RecipeProperty;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ItemStackHashStrategy;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AspectRecipe extends Recipe {
    private final List<AspectRecipeInput> aspectInputs;
    private final List<AspectStack> aspectOutputs;
    private final ChancedOutputList<AspectStack, ChancedAspectOutput> chancedAspctOutputs;
    private final ZZRecipeCategory zzrecipeCategory;

    public AspectRecipe(@NotNull List<GTRecipeInput> inputs, List<ItemStack> outputs, @NotNull ChancedOutputList<ItemStack, ChancedItemOutput> chancedOutputs, List<GTRecipeInput> fluidInputs, List<FluidStack> fluidOutputs, @NotNull ChancedOutputList<FluidStack, ChancedFluidOutput> chancedFluidOutputs, List<AspectRecipeInput> aspectInputs, List<AspectStack> aspectOutputs, @NotNull ChancedOutputList<AspectStack, ChancedAspectOutput> chancedAspectOutputs, int duration, int EUt, boolean hidden, boolean isCTRecipe, IRecipePropertyStorage recipePropertyStorage, @NotNull GTRecipeCategory recipeCategory, @NotNull ZZRecipeCategory zzrecipeCategory) {
        super(inputs, outputs, chancedOutputs, fluidInputs, fluidOutputs, chancedFluidOutputs, duration, EUt, hidden, isCTRecipe, recipePropertyStorage, recipeCategory);
        this.chancedAspctOutputs = chancedAspectOutputs;
        this.aspectInputs = AspectRecipeInputCache.deduplicateInputs(aspectInputs);
        this.aspectOutputs = (List)(aspectOutputs.isEmpty() ? Collections.emptyList() : ImmutableList.copyOf(aspectOutputs));
        this.zzrecipeCategory = zzrecipeCategory;
    }

    public @NotNull AspectRecipe copy() {
        return new AspectRecipe(this.getInputs(),
                this.getOutputs(),
                this.getChancedOutputs(),
                this.getFluidInputs(),
                this.getFluidOutputs(),
                this.getChancedFluidOutputs(),
                this.aspectInputs,
                this.aspectOutputs,
                this.chancedAspctOutputs,
                this.getDuration(),
                this.getEUt(),
                this.isHidden(),
                this.getIsCTRecipe(),
                this.getRecipePropertyStorage(),
                this.getRecipeCategory(),
                this.getZZRecipeCategory()
        );
    }

    public <T> T getProperty(RecipeProperty<T> property, T defaultValue) {
        return this.getRecipePropertyStorage().getRecipePropertyValue(property, defaultValue);
    }

    public @NotNull ZZRecipeCategory getZZRecipeCategory() {
        return this.zzrecipeCategory;
    }

    public static AspectRecipe trimRecipeOutputs(AspectRecipe currentRecipe, AspectRecipeMap<?> recipeMap, int itemTrimLimit, int fluidTrimLimit, int aspectTrimLimit) {
        if (itemTrimLimit == -1 && fluidTrimLimit == -1) {
            return currentRecipe;
        } else {
            currentRecipe = currentRecipe.copy();
            AspectRecipeBuilder<?> builder = new AspectRecipeBuilder(currentRecipe, recipeMap);
            builder.clearOutputs();
            builder.clearChancedOutput();
            builder.clearFluidOutputs();
            builder.clearChancedFluidOutputs();
            builder.clearAspectOutputs();
            builder.clearChancedAspectOutputs();
            Pair<List<ItemStack>, List<ChancedItemOutput>> recipeOutputs = currentRecipe.getItemAndChanceOutputs(itemTrimLimit);
            builder.chancedOutputs((List)recipeOutputs.getRight());
            builder.outputs((Collection)recipeOutputs.getLeft());
            Pair<List<FluidStack>, List<ChancedFluidOutput>> recipeFluidOutputs = currentRecipe.getFluidAndChanceOutputs(fluidTrimLimit);
            builder.chancedFluidOutputs((List)recipeFluidOutputs.getRight());
            builder.fluidOutputs((Collection)recipeFluidOutputs.getLeft());
            Pair<List<AspectStack>, List<ChancedAspectOutput>> recipeAspectOutputs = currentRecipe.getAspectAndChanceOutputs(aspectTrimLimit);
            builder.chancedAspectOutputs((List)recipeAspectOutputs.getRight());
            builder.aspectOutputs((Collection)recipeAspectOutputs.getLeft());
            return (AspectRecipe)builder.build().getResult();
        }
    }

    public final boolean matches(boolean consumeIfSuccessful, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, IMultipleAspectTankHandler aspectInputs) {
        Pair<Boolean, int[]> fluids = null;
        Pair<Boolean, int[]> items = null;
        Pair<Boolean, int[]> aspects = null;
        if (fluidInputs.getFluidTanks().size() > 0) {
            fluids = this.matchesFluid(GTUtility.fluidHandlerToList(fluidInputs));
            if (!(Boolean)fluids.getKey()) {
                return false;
            }
        }
        if (aspectInputs.getFluidTanks().size() > 0) {
            aspects = this.matchesAspect(AspectUtil.aspectHandlerToList(aspectInputs));
            if (!(Boolean)aspects.getKey()) {
                return false;
            }
        }

        if (inputs.getSlots() > 0) {
            items = this.matchesItems(GTUtility.itemHandlerToList(inputs));
            if (!(Boolean)items.getKey()) {
                return false;
            }
        }

        if (consumeIfSuccessful) {
            int[] itemAmountInSlot;
            if (fluids != null) {
                itemAmountInSlot = (int[])fluids.getValue();
                List<IMultipleTankHandler.MultiFluidTankEntry> backedList = fluidInputs.getFluidTanks();

                for(int i = 0; i < itemAmountInSlot.length; ++i) {
                    IMultipleTankHandler.MultiFluidTankEntry tank = (IMultipleTankHandler.MultiFluidTankEntry)backedList.get(i);
                    FluidStack fluidStack = tank.getFluid();
                    int fluidAmount = itemAmountInSlot[i];
                    if (fluidStack != null && fluidStack.amount != fluidAmount) {
                        tank.drain(Math.abs(fluidAmount - fluidStack.amount), true);
                    }
                }
            }
            if (aspects != null) {
                itemAmountInSlot = (int[])aspects.getValue();
                List<IMultipleAspectTankHandler.MultiAspectTankEntry> backedList = aspectInputs.getFluidTanks();

                for(int i = 0; i < itemAmountInSlot.length; ++i) {
                    IMultipleAspectTankHandler.MultiAspectTankEntry tank = (IMultipleAspectTankHandler.MultiAspectTankEntry)backedList.get(i);
                    AspectStack fluidStack = tank.getAspectStack();
                    int fluidAmount = itemAmountInSlot[i];
                    if (fluidStack != null && fluidStack.amount != fluidAmount) {
                        tank.drain(Math.abs(fluidAmount - fluidStack.amount), true);
                    }
                }
            }

            if (items != null) {
                itemAmountInSlot = (int[])items.getValue();

                for(int i = 0; i < itemAmountInSlot.length; ++i) {
                    ItemStack itemInSlot = inputs.getStackInSlot(i);
                    int itemAmount = itemAmountInSlot[i];
                    if (!itemInSlot.isEmpty() && itemInSlot.getCount() != itemAmount) {
                        inputs.extractItem(i, Math.abs(itemAmount - itemInSlot.getCount()), false);
                    }
                }
            }
        }

        return true;
    }

    public boolean matches(boolean consumeIfSuccessful, List<ItemStack> inputs, List<FluidStack> fluidInputs, List<AspectStack> aspectInputs) {
        Pair<Boolean, int[]> fluids = null;
        Pair<Boolean, int[]> items = null;
        Pair<Boolean, int[]> aspects = null;
        if (fluidInputs.size() > 0) {
            fluids = this.matchesFluid(fluidInputs);
            if (!(Boolean)fluids.getKey()) {
                return false;
            }
        }
        if (fluidInputs.size() > 0) {
            aspects = this.matchesAspect(aspectInputs);
            if (!(Boolean)aspects.getKey()) {
                return false;
            }
        }

        if (inputs.size() > 0) {
            items = this.matchesItems(inputs);
            if (!(Boolean)items.getKey()) {
                return false;
            }
        }

        if (consumeIfSuccessful) {
            int[] itemAmountInSlot;
            int i;
            int itemAmount;
            if (fluids != null) {
                itemAmountInSlot = (int[])fluids.getValue();

                for(i = 0; i < itemAmountInSlot.length; ++i) {
                    FluidStack fluidStack = (FluidStack)fluidInputs.get(i);
                    itemAmount = itemAmountInSlot[i];
                    if (fluidStack != null && fluidStack.amount != itemAmount) {
                        fluidStack.amount = itemAmount;
                        if (fluidStack.amount == 0) {
                            fluidInputs.set(i, null);
                        }
                    }
                }
            }

            if (aspects != null) {
                itemAmountInSlot = (int[])aspects.getValue();

                for(i = 0; i < itemAmountInSlot.length; ++i) {
                    AspectStack fluidStack = (AspectStack)aspectInputs.get(i);
                    itemAmount = itemAmountInSlot[i];
                    if (fluidStack != null && fluidStack.amount != itemAmount) {
                        fluidStack.amount = itemAmount;
                        if (fluidStack.amount == 0) {
                            fluidInputs.set(i, null);
                        }
                    }
                }
            }

            if (items != null) {
                itemAmountInSlot = (int[])items.getValue();

                for(i = 0; i < itemAmountInSlot.length; ++i) {
                    ItemStack itemInSlot = (ItemStack)inputs.get(i);
                    itemAmount = itemAmountInSlot[i];
                    if (!itemInSlot.isEmpty() && itemInSlot.getCount() != itemAmount) {
                        itemInSlot.setCount(itemAmountInSlot[i]);
                    }
                }
            }
        }

        return true;
    }

    private Pair<Boolean, int[]> matchesItems(List<ItemStack> inputs) {
        int[] itemAmountInSlot = new int[inputs.size()];
        int indexed = 0;
        List<GTRecipeInput> gtRecipeInputs = this.getInputs();
        Iterator var5 = gtRecipeInputs.iterator();

        int ingredientAmount;
        do {
            if (!var5.hasNext()) {
                int[] retItemAmountInSlot = new int[indexed];
                System.arraycopy(itemAmountInSlot, 0, retItemAmountInSlot, 0, indexed);
                return Pair.of(true, retItemAmountInSlot);
            }

            GTRecipeInput ingredient = (GTRecipeInput)var5.next();
            ingredientAmount = ingredient.getAmount();

            for(int j = 0; j < inputs.size(); ++j) {
                ItemStack inputStack = (ItemStack)inputs.get(j);
                if (j == indexed) {
                    itemAmountInSlot[j] = inputStack.isEmpty() ? 0 : inputStack.getCount();
                    ++indexed;
                }

                if (!inputStack.isEmpty() && ingredient.acceptsStack(inputStack)) {
                    int itemAmountToConsume = Math.min(itemAmountInSlot[j], ingredientAmount);
                    ingredientAmount -= itemAmountToConsume;
                    if (!ingredient.isNonConsumable()) {
                        itemAmountInSlot[j] -= itemAmountToConsume;
                    }

                    if (ingredientAmount == 0) {
                        break;
                    }
                }
            }
        } while(ingredientAmount <= 0);

        return Pair.of(false, itemAmountInSlot);
    }

    private Pair<Boolean, int[]> matchesFluid(List<FluidStack> fluidInputs) {
        int[] fluidAmountInTank = new int[fluidInputs.size()];
        int indexed = 0;
        List<GTRecipeInput> gtRecipeInputs = this.getFluidInputs();
        Iterator var5 = gtRecipeInputs.iterator();

        int fluidAmount;
        do {
            if (!var5.hasNext()) {
                int[] retfluidAmountInTank = new int[indexed];
                System.arraycopy(fluidAmountInTank, 0, retfluidAmountInTank, 0, indexed);
                return Pair.of(true, retfluidAmountInTank);
            }

            GTRecipeInput fluid = (GTRecipeInput)var5.next();
            fluidAmount = fluid.getAmount();

            for(int j = 0; j < fluidInputs.size(); ++j) {
                FluidStack tankFluid = (FluidStack)fluidInputs.get(j);
                if (j == indexed) {
                    ++indexed;
                    fluidAmountInTank[j] = tankFluid == null ? 0 : tankFluid.amount;
                }

                if (tankFluid != null && fluid.acceptsFluid(tankFluid)) {
                    int fluidAmountToConsume = Math.min(fluidAmountInTank[j], fluidAmount);
                    fluidAmount -= fluidAmountToConsume;
                    if (!fluid.isNonConsumable()) {
                        fluidAmountInTank[j] -= fluidAmountToConsume;
                    }

                    if (fluidAmount == 0) {
                        break;
                    }
                }
            }
        } while(fluidAmount <= 0);

        return Pair.of(false, fluidAmountInTank);
    }

    private Pair<Boolean, int[]> matchesAspect(List<AspectStack> fluidInputs) {
        int[] fluidAmountInTank = new int[fluidInputs.size()];
        int indexed = 0;
        List<AspectRecipeInput> gtRecipeInputs = this.aspectInputs;
        Iterator var5 = gtRecipeInputs.iterator();

        int fluidAmount;
        do {
            if (!var5.hasNext()) {
                int[] retfluidAmountInTank = new int[indexed];
                System.arraycopy(fluidAmountInTank, 0, retfluidAmountInTank, 0, indexed);
                return Pair.of(true, retfluidAmountInTank);
            }

            AspectRecipeInput fluid = (AspectRecipeInput)var5.next();
            fluidAmount = fluid.getAmount();

            for(int j = 0; j < fluidInputs.size(); ++j) {
                AspectStack tankFluid = (AspectStack)fluidInputs.get(j);
                if (j == indexed) {
                    ++indexed;
                    fluidAmountInTank[j] = tankFluid == null ? 0 : tankFluid.amount;
                }

                if (tankFluid != null && fluid.acceptsAspect(tankFluid)) {
                    int fluidAmountToConsume = Math.min(fluidAmountInTank[j], fluidAmount);
                    fluidAmount -= fluidAmountToConsume;
                    if (!fluid.isNonConsumable()) {
                        fluidAmountInTank[j] -= fluidAmountToConsume;
                    }

                    if (fluidAmount == 0) {
                        break;
                    }
                }
            }
        } while(fluidAmount <= 0);

        return Pair.of(false, fluidAmountInTank);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            AspectRecipe recipe = (AspectRecipe)o;
            return this.hasSameInputs(recipe) && this.hasSameFluidInputs(recipe) && this.hasSameAspectInputs(recipe);
        } else {
            return false;
        }
    }

    private int makeHashCode() {
        int hash = 31 * this.hashInputs();
        hash = 31 * hash + hashFluidList(this.getFluidInputs()) + hashAspectList(this.aspectInputs);
        return hash;
    }

    private int hashInputs() {
        int hash = 0;
        Iterator var2 = this.getInputs().iterator();

        while(true) {
            while(var2.hasNext()) {
                GTRecipeInput recipeIngredient = (GTRecipeInput)var2.next();
                if (!recipeIngredient.isOreDict()) {
                    ItemStack[] var4 = recipeIngredient.getInputStacks();
                    int var5 = var4.length;

                    for(int var6 = 0; var6 < var5; ++var6) {
                        ItemStack is = var4[var6];
                        hash = 31 * hash + ItemStackHashStrategy.comparingAll().hashCode(is);
                    }
                } else {
                    hash = 31 * hash + recipeIngredient.getOreDict();
                }
            }

            return hash;
        }
    }

    public static int hashAspectList(@NotNull List<AspectRecipeInput> fluids) {
        int hash = 0;

        AspectRecipeInput fluidInput;
        for(Iterator var2 = fluids.iterator(); var2.hasNext(); hash = 31 * hash + fluidInput.hashCode()) {
            fluidInput = (AspectRecipeInput)var2.next();
        }

        return hash;
    }

    private boolean hasSameInputs(AspectRecipe otherRecipe) {
        List<ItemStack> otherStackList = new ObjectArrayList(otherRecipe.getInputs().size());
        Iterator var3 = otherRecipe.getInputs().iterator();

        while(var3.hasNext()) {
            GTRecipeInput otherInputs = (GTRecipeInput)var3.next();
            otherStackList.addAll(Arrays.asList(otherInputs.getInputStacks()));
        }

        if (!(Boolean)this.matchesItems(otherStackList).getLeft()) {
            return false;
        } else {
            List<ItemStack> thisStackList = new ObjectArrayList(this.getInputs().size());
            Iterator var7 = this.getInputs().iterator();

            while(var7.hasNext()) {
                GTRecipeInput thisInputs = (GTRecipeInput)var7.next();
                thisStackList.addAll(Arrays.asList(thisInputs.getInputStacks()));
            }

            return (Boolean)otherRecipe.matchesItems(thisStackList).getLeft();
        }
    }

    private boolean hasSameFluidInputs(AspectRecipe otherRecipe) {
        List<FluidStack> otherFluidList = new ObjectArrayList(otherRecipe.getFluidInputs().size());
        Iterator var3 = otherRecipe.getFluidInputs().iterator();

        while(var3.hasNext()) {
            GTRecipeInput otherInputs = (GTRecipeInput)var3.next();
            FluidStack fluidStack = otherInputs.getInputFluidStack();
            otherFluidList.add(fluidStack);
        }

        if (!(Boolean)this.matchesFluid(otherFluidList).getLeft()) {
            return false;
        } else {
            List<FluidStack> thisFluidsList = new ObjectArrayList(this.getFluidInputs().size());
            Iterator var8 = this.getFluidInputs().iterator();

            while(var8.hasNext()) {
                GTRecipeInput thisFluidInputs = (GTRecipeInput)var8.next();
                FluidStack fluidStack = thisFluidInputs.getInputFluidStack();
                thisFluidsList.add(fluidStack);
            }

            return (Boolean)otherRecipe.matchesFluid(thisFluidsList).getLeft();
        }
    }

    private boolean hasSameAspectInputs(AspectRecipe otherRecipe) {
        List<AspectStack> otherFluidList = new ObjectArrayList(otherRecipe.aspectInputs.size());
        Iterator var3 = otherRecipe.aspectInputs.iterator();

        while(var3.hasNext()) {
            AspectRecipeInput otherInputs = (AspectRecipeInput)var3.next();
            AspectStack fluidStack = otherInputs.getInputAspectStack();
            otherFluidList.add(fluidStack);
        }

        if (!(Boolean)this.matchesAspect(otherFluidList).getLeft()) {
            return false;
        } else {
            List<AspectStack> thisFluidsList = new ObjectArrayList(this.aspectInputs.size());
            Iterator var8 = this.aspectInputs.iterator();

            while(var8.hasNext()) {
                AspectRecipeInput thisFluidInputs = (AspectRecipeInput)var8.next();
                AspectStack fluidStack = thisFluidInputs.getInputAspectStack();
                thisFluidsList.add(fluidStack);
            }

            return (Boolean)otherRecipe.matchesAspect(thisFluidsList).getLeft();
        }
    }

    public String toString() {
        return (new ToStringBuilder(this))
                .append("inputs", this.getInputs())
                .append("outputs", this.getOutputs())
                .append("chancedOutputs", this.getChancedOutputs())
                .append("fluidInputs", this.getFluidInputs())
                .append("fluidOutputs", this.getFluidOutputs())
                .append("aspectInputs", this.aspectInputs)
                .append("aspectOutputs", this.aspectOutputs)
                .append("duration", this.getDuration())
                .append("EUt", this.getEUt())
                .append("hidden", this.isHidden())
                .append("CTRecipe", this.getIsCTRecipe())
                .append("GSRecipe", this.isGroovyRecipe())
                .toString();
    }

    public List<AspectRecipeInput> getAspectInputs() {
        return this.aspectInputs;
    }

    public ChancedOutputList<AspectStack, ChancedAspectOutput> getChancedAspectOutputs() {
        return this.chancedAspctOutputs;
    }

    public boolean hasInputAspect(AspectStack fluid) {
        Iterator var2 = this.aspectInputs.iterator();

        AspectStack fluidStack;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            AspectRecipeInput fluidInput = (AspectRecipeInput)var2.next();
            fluidStack = fluidInput.getInputAspectStack();
        } while(!Objects.equals(fluid.getAspect().getTag(), fluidStack.getAspect().getTag()));

        return fluidStack.isAspectEqual(fluid);
    }

    public List<AspectStack> getAspectOutputs() {
        return this.aspectOutputs;
    }

    public Pair<List<AspectStack>, List<ChancedAspectOutput>> getAspectAndChanceOutputs(int outputLimit) {
        List<AspectStack> outputs = new ArrayList();
        List<ChancedAspectOutput> chancedOutputs = new ArrayList(this.getChancedAspectOutputs().getChancedEntries());
        if (outputLimit == -1) {
            outputs.addAll(AspectUtil.copyAspectList(this.getAspectOutputs()));
        } else if (this.getAspectOutputs().size() >= outputLimit) {
            outputs.addAll(AspectUtil.copyAspectList(this.getAspectOutputs()).subList(0, Math.min(outputLimit, this.getAspectOutputs().size())));
            ((List)chancedOutputs).clear();
        } else if (!this.getAspectOutputs().isEmpty() && this.getAspectOutputs().size() + ((List)chancedOutputs).size() >= outputLimit) {
            outputs.addAll(AspectUtil.copyAspectList(this.getAspectOutputs()));
            int numChanced = outputLimit - this.getAspectOutputs().size();
            chancedOutputs = ((List)chancedOutputs).subList(0, Math.min(numChanced, ((List)chancedOutputs).size()));
        } else if (this.getAspectOutputs().isEmpty()) {
            chancedOutputs = ((List)chancedOutputs).subList(0, Math.min(outputLimit, ((List)chancedOutputs).size()));
        } else {
            outputs.addAll(AspectUtil.copyAspectList(this.getAspectOutputs()));
        }

        return Pair.of(outputs, chancedOutputs);
    }

    public List<AspectStack> getAllAspectOutputs() {
        List<AspectStack> recipeOutputs = new ArrayList(this.aspectOutputs);

        for (ChancedAspectOutput entry : this.chancedAspctOutputs.getChancedEntries()) {
            recipeOutputs.add(((AspectStack) entry.getIngredient()).copy());
        }

        return recipeOutputs;
    }

    public List<AspectStack> getResultAspectOutputs(int recipeTier, int machineTier, RecipeMap<?> recipeMap) {
        List<AspectStack> outputs = new ArrayList(AspectUtil.copyAspectList(this.getAspectOutputs()));
        ChanceBoostFunction function = recipeMap.getChanceFunction();
        List<ChancedAspectOutput> chancedOutputsList = this.getChancedAspectOutputs().roll(function, recipeTier, machineTier);
        if (chancedOutputsList == null) {
            return outputs;
        } else {
            Collection<AspectStack> resultChanced = new ArrayList();
            Iterator var8 = chancedOutputsList.iterator();

            while(var8.hasNext()) {
                ChancedAspectOutput chancedOutput = (ChancedAspectOutput)var8.next();
                AspectStack stackToAdd = ((AspectStack)chancedOutput.getIngredient()).copy();
                Iterator var11 = resultChanced.iterator();

                while(var11.hasNext()) {
                    AspectStack stackInList = (AspectStack)var11.next();
                    int insertable = stackInList.amount;
                    if (insertable > 0 && stackInList.getAspect().getTag() == stackToAdd.getAspect().getTag()) {
                        stackInList.amount += stackToAdd.amount;
                        stackToAdd = null;
                        break;
                    }
                }

                if (stackToAdd != null) {
                    resultChanced.add(stackToAdd);
                }
            }

            outputs.addAll(resultChanced);
            return outputs;
        }
    }

    public boolean hasValidInputsForDisplay() {
        Iterator var1 = this.getInputs().iterator();

        AspectRecipeInput fluidInput;
        while(var1.hasNext()) {
            fluidInput = (AspectRecipeInput)var1.next();
            if (fluidInput.isOreDict()) {
                if (OreDictionary.getOres(OreDictionary.getOreName(fluidInput.getOreDict())).stream().anyMatch((s) -> {
                    return !s.isEmpty();
                })) {
                    return true;
                }
            } else if (Arrays.stream(fluidInput.getInputStacks()).anyMatch((s) -> {
                return !s.isEmpty();
            })) {
                return true;
            }
        }

        var1 = this.getFluidInputs().iterator();

        AspectStack fluidIngredient;
        do {
            if (!var1.hasNext()) {
                return false;
            }

            fluidInput = (AspectRecipeInput)var1.next();
            fluidIngredient = fluidInput.getInputAspectStack();
        } while(fluidIngredient == null || fluidIngredient.amount <= 0);

        return true;
    }
}

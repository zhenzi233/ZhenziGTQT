package com.zhenzi.zhenzigtqt.loaders;

import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectStack;
import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectUtil;
import com.zhenzi.zhenzigtqt.integration.jei.recipes.ZZRecipeCategory;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.AssemblerRecipeBuilder;
import gregtech.api.recipes.category.GTRecipeCategory;
import gregtech.api.recipes.chance.output.ChancedOutputList;
import gregtech.api.recipes.chance.output.ChancedOutputLogic;
import gregtech.api.recipes.chance.output.impl.ChancedFluidOutput;
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput;
import gregtech.api.recipes.ingredients.GTRecipeFluidInput;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.recipeproperties.RecipeProperty;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ValidationResult;
import gregtech.integration.groovy.GroovyScriptModule;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.Aspect;

import java.util.*;
import java.util.function.Consumer;

public class AspectRecipeBuilder<R extends AspectRecipeBuilder<R>> extends RecipeBuilder<R> {
    protected AspectRecipeMap<R> recipeMap;
    protected final List<AspectRecipeInput> aspectInputs;
    protected final List<AspectStack> aspectOutputs;
    protected final List<ChancedAspectOutput> chancedAspectOutputs;
    protected ChancedOutputLogic chancedAspectOutputLogic;
    protected ZZRecipeCategory zzcategory;

    protected AspectRecipeBuilder() {
        super();
        this.chancedAspectOutputLogic = ChancedOutputLogic.OR;
        this.aspectInputs = new ArrayList();
        this.aspectOutputs = new ArrayList();
        this.chancedAspectOutputs = new ArrayList();
    }
    public AspectRecipeBuilder(AspectRecipe recipe, AspectRecipeMap<R> recipeMap) {
        super(recipe, recipeMap);

        this.chancedAspectOutputLogic = ChancedOutputLogic.OR;
        this.aspectInputs = new ArrayList(recipe.getAspectInputs());
        this.aspectOutputs = AspectUtil.copyAspectList(recipe.getAspectOutputs());
        this.chancedAspectOutputs = new ArrayList(recipe.getChancedAspectOutputs().getChancedEntries());
        this.recipeMap = recipeMap;
        this.zzcategory = recipe.getZZRecipeCategory();
    }

    protected AspectRecipeBuilder(AspectRecipeBuilder<R> recipeBuilder) {
        super(recipeBuilder);
        this.chancedAspectOutputLogic = ChancedOutputLogic.OR;
        this.aspectInputs = new ArrayList(recipeBuilder.getAspectInputs());
        this.aspectOutputs = AspectUtil.copyAspectList(recipeBuilder.getAspectOutputs());
        this.chancedAspectOutputs = new ArrayList(recipeBuilder.chancedAspectOutputs);
        this.recipeMap = recipeBuilder.recipeMap;
        this.zzcategory = recipeBuilder.zzcategory;
    }

    public List<AspectRecipeInput> getAspectInputs() {
        return this.aspectInputs;
    }

    public List<AspectStack> getAspectOutputs() {
        return this.aspectOutputs;
    }

    public R setRecipeMap(AspectRecipeMap<R> recipeMap) {
        this.recipeMap = recipeMap;
        return self();
    }

    public R category(@NotNull ZZRecipeCategory category) {
        this.zzcategory = category;
        return self();
    }

    public R notConsumable(Aspect fluid, int amount) {
        AspectRecipeInput aspectRecipeInput = new AspectRecipeInput(fluid, amount);
        return this.aspectInputs((AspectRecipeInput)aspectRecipeInput.setNonConsumable());
    }

    public R notConsumable(Aspect fluid) {
        AspectRecipeInput aspectRecipeInput = new AspectRecipeInput(fluid, 1);
        return this.aspectInputs((AspectRecipeInput)aspectRecipeInput.setNonConsumable());
    }

    public R notConsumable(AspectStack fluidStack) {
        AspectRecipeInput aspectRecipeInput = new AspectRecipeInput(fluidStack);
        return this.aspectInputs((AspectRecipeInput)aspectRecipeInput.setNonConsumable());
    }

    public R aspectInputs(Collection<AspectRecipeInput> fluidIngredients) {
        this.aspectInputs.addAll(fluidIngredients);
        return self();
    }

    public R aspectInputs(AspectRecipeInput fluidIngredient) {
        this.aspectInputs.add(fluidIngredient);
        return self();
    }

    public R aspectInputs(AspectStack... fluidStacks) {
        ArrayList<AspectRecipeInput> fluidIngredients = new ArrayList();
        AspectStack[] var3 = fluidStacks;
        int var4 = fluidStacks.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            AspectStack fluidStack = var3[var5];
            if (fluidStack != null && fluidStack.amount > 0) {
                fluidIngredients.add(new AspectRecipeInput(fluidStack));
            } else if (fluidStack != null) {
                GTLog.logger.error("Count cannot be less than 0. Actual: {}.", fluidStack.amount);
                GTLog.logger.error("Stacktrace:", new IllegalArgumentException());
            } else {
                GTLog.logger.error("AspectStack cannot be null.");
            }
        }

        this.aspectInputs.addAll(fluidIngredients);
        return self();
    }

    public R clearAspectInputs() {
        this.aspectInputs.clear();
        return self();
    }

    public R aspectOutputs(AspectStack... outputs) {
        return this.aspectOutputs((Arrays.asList(outputs)));
    }

    public R aspectOutputs(Collection<AspectStack> outputs) {
        Collection<AspectStack> outputsA = new ArrayList(outputs);
        outputsA.removeIf(Objects::isNull);
        this.aspectOutputs.addAll(outputsA);
        return self();
    }

    public R clearAspectOutputs() {
        this.aspectOutputs.clear();
        return self();
    }

    public R chancedAspectOutput(AspectStack stack, int chance, int tierChanceBoost) {
        if (stack != null && stack.amount != 0) {
            if (0 < chance && chance <= ChancedOutputLogic.getMaxChancedValue()) {
                this.chancedAspectOutputs.add(new ChancedAspectOutput(stack.copy(), chance, tierChanceBoost));
                return self();
            } else {
                GTLog.logger.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.", ChancedOutputLogic.getMaxChancedValue(), chance);
                GTLog.logger.error("Stacktrace:", new IllegalArgumentException());
                this.recipeStatus = EnumValidationResult.INVALID;
                return self();
            }
        } else {
            return self();
        }
    }

    public R chancedAspectOutputs(List<ChancedAspectOutput> chancedOutputs) {
        for (ChancedAspectOutput output : chancedOutputs) {
            this.chancedAspectOutputs.add(output.copy());
        }

        return self();
    }

    public R clearChancedAspectOutputs() {
        this.chancedAspectOutputs.clear();
        return self();
    }

    public R chancedAspectOutputLogic(@NotNull ChancedOutputLogic logic) {
        this.chancedAspectOutputLogic = logic;
        return self();
    }

    public void chancedOutputsMultiply(AspectRecipe chancedOutputsFrom, int numberOfOperations) {
        Iterator var3 = chancedOutputsFrom.getChancedOutputs().getChancedEntries().iterator();

        int chance;
        int boost;
        int i;
        while(var3.hasNext()) {
            ChancedItemOutput entry = (ChancedItemOutput)var3.next();
            chance = entry.getChance();
            boost = entry.getChanceBoost();

            for(i = 0; i < numberOfOperations; ++i) {
                this.chancedOutput(((ItemStack)entry.getIngredient()).copy(), chance, boost);
            }
        }

        var3 = chancedOutputsFrom.getChancedFluidOutputs().getChancedEntries().iterator();

        while(var3.hasNext()) {
            ChancedFluidOutput entry = (ChancedFluidOutput)var3.next();
            chance = entry.getChance();
            boost = entry.getChanceBoost();

            for(i = 0; i < numberOfOperations; ++i) {
                this.chancedFluidOutput(((FluidStack)entry.getIngredient()).copy(), chance, boost);
            }
        }

        var3 = chancedOutputsFrom.getChancedAspectOutputs().getChancedEntries().iterator();

        while(var3.hasNext()) {
            ChancedAspectOutput entry = (ChancedAspectOutput)var3.next();
            chance = entry.getChance();
            boost = entry.getChanceBoost();

            for(i = 0; i < numberOfOperations; ++i) {
                this.chancedAspectOutput(((AspectStack)entry.getIngredient()).copy(), chance, boost);
            }
        }

    }

    public R append(AspectRecipe recipe, int multiplier, boolean multiplyDuration) {

        for (Map.Entry<RecipeProperty<?>, Object> recipePropertyObjectEntry : recipe.getPropertyValues()) {
            Map.Entry<RecipeProperty<?>, Object> property = (Map.Entry) recipePropertyObjectEntry;
            this.applyProperty(((RecipeProperty) property.getKey()).getKey(), property.getValue());
        }

        List<GTRecipeInput> newRecipeInputs = new ArrayList();
        List<GTRecipeInput> newFluidInputs = new ArrayList();
        List<GTRecipeInput> newAspectInputs = new ArrayList();
        List<ItemStack> outputItems = new ArrayList();
        List<FluidStack> outputFluids = new ArrayList();
        List<AspectStack> outputAspects = new ArrayList();
        multiplyInputsAndOutputs(newRecipeInputs, newFluidInputs, newAspectInputs, outputItems, outputFluids, outputAspects, recipe, multiplier);
        this.inputIngredients(newRecipeInputs);
        this.fluidInputs((Collection)newFluidInputs);
        this.aspectInputs((Collection)newAspectInputs);
        this.outputs((Collection)outputItems);
        this.fluidOutputs((Collection)outputFluids);
        this.aspectOutputs((Collection)outputAspects);
        this.chancedOutputsMultiply(recipe, multiplier);
        this.EUt(multiplyDuration ? recipe.getEUt() : this.EUt + recipe.getEUt() * multiplier);
        this.duration(multiplyDuration ? this.duration + recipe.getDuration() * multiplier : recipe.getDuration());
        this.parallel += multiplier;
        return self();
    }

    protected static void multiplyInputsAndOutputs(List<GTRecipeInput> newRecipeInputs, List<GTRecipeInput> newFluidInputs, List<GTRecipeInput> newAspectInputs, List<ItemStack> outputItems, List<FluidStack> outputFluids, List<AspectStack> outputAspects, AspectRecipe recipe, int numberOfOperations) {
        recipe.getInputs().forEach((ri) -> {
            if (ri.isNonConsumable()) {
                newRecipeInputs.add(ri);
            } else {
                newRecipeInputs.add(ri.withAmount(ri.getAmount() * numberOfOperations));
            }

        });
        recipe.getFluidInputs().forEach((fi) -> {
            if (fi.isNonConsumable()) {
                newFluidInputs.add(fi);
            } else {
                newFluidInputs.add(fi.withAmount(fi.getAmount() * numberOfOperations));
            }

        });

        recipe.getAspectInputs().forEach((ai) -> {
            if (ai.isNonConsumable()) {
                newAspectInputs.add(ai);
            } else {
                newAspectInputs.add(ai.withAmount(ai.getAmount() * numberOfOperations));
            }

        });

        recipe.getOutputs().forEach((itemStack) -> {
            outputItems.add(copyItemStackWithCount(itemStack, itemStack.getCount() * numberOfOperations));
        });
        recipe.getFluidOutputs().forEach((fluidStack) -> {
            outputFluids.add(copyFluidStackWithAmount(fluidStack, fluidStack.amount * numberOfOperations));
        });
        recipe.getAspectOutputs().forEach((fluidStack) -> {
            outputAspects.add(copyAspectStackWithAmount(fluidStack, fluidStack.amount * numberOfOperations));
        });
    }

    protected static AspectStack copyAspectStackWithAmount(AspectStack fluidStack, int count) {
        AspectStack fluidCopy = fluidStack.copy();
        fluidCopy.amount = count;
        return fluidCopy;
    }

    protected R onBuild(Consumer<R> consumer) {
        this.onBuildAction = consumer;
        return self();
    }

    public void buildAndRegister() {
        if (this.onBuildAction != null) {
            this.onBuildAction.accept(self());
        }

        ValidationResult<AspectRecipe> validationResult = this.buildA();
        this.recipeMap.addRecipeA(validationResult);
    }

    public R copy() {
        AspectRecipeBuilder<R> copy = new AspectRecipeBuilder<>(this);
        return copy.self();
    }

    public ValidationResult<AspectRecipe> buildA() {
        return ValidationResult.newResult(
                this.finalizeAndValidate(),
                new AspectRecipe(this.inputs,
                        this.outputs,
                        new ChancedOutputList(this.chancedOutputLogic, this.chancedOutputs),
                        this.fluidInputs,
                        this.fluidOutputs,
                        new ChancedOutputList(this.chancedFluidOutputLogic, this.chancedFluidOutputs),
                        this.aspectInputs,
                        this.aspectOutputs,
                        new ChancedOutputList<>(this.chancedAspectOutputLogic, this.chancedAspectOutputs),
                        this.duration,
                        this.EUt,
                        this.hidden,
                        this.isCTRecipe,
                        this.recipePropertyStorage,
                        this.category,
                        this.zzcategory));
    }

    protected EnumValidationResult validate() {
            if (this.EUt == 0) {
                GTLog.logger.error("EU/t cannot be equal to 0", new IllegalArgumentException());

                this.recipeStatus = EnumValidationResult.INVALID;
            }

            if (this.duration <= 0) {
                GTLog.logger.error("Duration cannot be less or equal to 0", new IllegalArgumentException());

                this.recipeStatus = EnumValidationResult.INVALID;
            }

            if (this.recipeMap != null) {
                if (this.zzcategory == null) {
                    GTLog.logger.error("Recipes must have a zzcategory", new IllegalArgumentException());

                    this.recipeStatus = EnumValidationResult.INVALID;
                } else if (this.zzcategory.getRecipeMap() != this.recipeMap) {
                    GTLog.logger.error("Cannot apply Category with incompatible RecipeMap", new IllegalArgumentException());

                    this.recipeStatus = EnumValidationResult.INVALID;
                }
            }

            if (this.recipeStatus == EnumValidationResult.INVALID) {
                GTLog.logger.error("Invalid recipe, read the errors above: {}", this);
            }

            if (this.recipePropertyStorage != null) {
                this.recipePropertyStorage.freeze(true);
            }

            return this.recipeStatus;
    }





    @SuppressWarnings("unchecked")
    protected R self() {
        return (R) this;
    }
}

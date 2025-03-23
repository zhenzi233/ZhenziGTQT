package com.zhenzi.zhenzigtqt.loaders;

import com.google.common.collect.ImmutableList;
import com.zhenzi.zhenzigtqt.client.gui.QuantumAspectTank.AspectRecipeProgressWidget;
import com.zhenzi.zhenzigtqt.client.gui.QuantumAspectTank.AspectTankWidget;
import com.zhenzi.zhenzigtqt.client.render.texture.ZZTextures;
import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectStack;
import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectTankList;
import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectUtil;
import com.zhenzi.zhenzigtqt.common.lib.aspect.IMultipleAspectTankHandler;
import com.zhenzi.zhenzigtqt.integration.jei.recipes.ZZRecipeCategory;
import gregtech.api.GTValues;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.RecipeProgressWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.category.GTRecipeCategory;
import gregtech.api.recipes.map.AbstractMapIngredient;
import gregtech.api.recipes.map.Either;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ValidationResult;
import gregtech.common.ConfigHolder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AspectRecipeMap<R extends AspectRecipeBuilder<R>> extends RecipeMap<R> {

    private static final Map<String, AspectRecipeMap<?>> RECIPE_MAP_REGISTRY = new Object2ReferenceOpenHashMap();
    private int maxAspectInputs;
    private int maxAspectOutputs;
    private final boolean modifyAspectInputs;
    private final R recipeBuilderSample;
    private boolean allowEmptyOutput;
    private static final Comparator<AspectRecipe> RECIPE_DURATION_THEN_EU = Comparator.comparingInt(AspectRecipe::getDuration).thenComparingInt(AspectRecipe::getEUt).thenComparing(AspectRecipe::hashCode);

    private final boolean modifyAspectOutputs;
    private final WeakHashMap<AbstractMapIngredient, WeakReference<AbstractMapIngredient>> aspectIngredientRoot;

    private final AspectBranch lookup;
    private final Map<ZZRecipeCategory, List<AspectRecipe>> recipeByCategory;
    private Consumer<R> onRecipeBuildAction;
    private AspectRecipeMap<?> smallRecipeMap;

    public AspectRecipeMap(@NotNull String unlocalizedName,
                                    int maxInputs, int maxOutputs,
                                    int maxFluidInputs, int maxFluidOutputs,
                                    int maxAspectInputs, int maxAspectOutputs,
                                    @NotNull R defaultRecipeBuilder,
                                    boolean isHidden) {
        this(unlocalizedName, maxInputs,
                true, maxOutputs,
                true, maxFluidInputs,
                true, maxFluidOutputs,
                true, maxAspectInputs,
                true, maxAspectOutputs,
                true, defaultRecipeBuilder,
                isHidden);
    }
    public AspectRecipeMap(@NotNull String unlocalizedName,
                           int maxInputs,
                           boolean modifyItemInputs,
                           int maxOutputs,
                           boolean modifyItemOutputs,
                           int maxFluidInputs,
                           boolean modifyFluidInputs,
                           int maxFluidOutputs,
                           boolean modifyFluidOutputs,
                           int maxAspectInputs,
                           boolean modifyAspectInputs,
                           int maxAspectOutputs,
                           boolean modifyAspectOutputs,
                           @NotNull R defaultRecipeBuilder,
                           boolean isHidden)
    {
        super(unlocalizedName, maxInputs, modifyItemInputs, maxOutputs, modifyItemOutputs, maxFluidInputs, modifyFluidInputs, maxFluidOutputs, modifyFluidOutputs, defaultRecipeBuilder, isHidden);
        this.aspectIngredientRoot = new WeakHashMap<>();
        this.maxAspectInputs = maxAspectInputs;
        this.maxAspectOutputs = maxAspectOutputs;
        this.modifyAspectInputs = modifyAspectInputs;
        this.modifyAspectOutputs = modifyAspectOutputs;
        this.lookup = new AspectBranch();
        this.recipeByCategory = new Object2ObjectOpenHashMap();
//        defaultRecipeBuilder.setRecipeMap(this);
//        defaultRecipeBuilder.category(GTRecipeCategory.create("gregtech", unlocalizedName, this.getTranslationKey(), this));
//        this.recipeBuilderSample = defaultRecipeBuilder;
        defaultRecipeBuilder.setRecipeMap(this);
        defaultRecipeBuilder.category(ZZRecipeCategory.create("zhenzigtqt", unlocalizedName, this.getTranslationKey(), this));
        this.recipeBuilderSample = defaultRecipeBuilder;
        RECIPE_MAP_REGISTRY.put(unlocalizedName, this);
    }

    public AspectRecipeMap<R> setSmallRecipeMap(AspectRecipeMap<?> recipeMap) {
        this.smallRecipeMap = recipeMap;
        return this;
    }

    public AspectRecipeMap<?> getSmallRecipeMap() {
        return this.smallRecipeMap;
    }


    public static List<AspectRecipeMap<?>> getRecipeMapsA() {
        return ImmutableList.copyOf(RECIPE_MAP_REGISTRY.values());
    }


    public static AspectRecipeMap<?> getByName(String unlocalizedName) {
        return (AspectRecipeMap)RECIPE_MAP_REGISTRY.get(unlocalizedName);
    }

    public AspectRecipeMap<R> onRecipeBuild(Consumer<R> consumer) {
        this.onRecipeBuildAction = consumer;
        return this;
    }

    public AspectRecipeMap<R> setSlotOverlay(boolean isOutput, boolean isFluid, boolean isAspect, TextureArea slotOverlay) {
        this.setSlotOverlay(isOutput, isFluid, isAspect, false, slotOverlay);
        return this.setSlotOverlay(isOutput, isFluid, isAspect, true, slotOverlay);
    }

    public AspectRecipeMap<R> setSlotOverlay(boolean isOutput, boolean isFluid, boolean isAspect, boolean isLast, TextureArea slotOverlay) {
        this.slotOverlays.put((byte)((isOutput ? 2 : 0) + (isFluid ? 1 : 0) + (isAspect ? 6 : 0) + (isLast ? 4 : 0)), slotOverlay);
        return this;
    }

    public AspectRecipeMap<R> setProgressBar(TextureArea progressBar, ProgressWidget.MoveType moveType) {
        this.progressBarTexture = progressBar;
        this.moveType = moveType;
        return this;
    }

    public AspectRecipeMap<R> setSound(SoundEvent sound) {
        this.sound = sound;
        return this;
    }

    public AspectRecipeMap<R> allowEmptyOutput() {
        this.allowEmptyOutput = true;
        return this;
    }

    public boolean addRecipeA(@NotNull ValidationResult<AspectRecipe> validationResult) {
        validationResult = this.postValidateRecipeA(validationResult);
        switch (validationResult.getType()) {
            case SKIP:
                return false;
            case INVALID:
                setFoundInvalidRecipe(true);
                return false;
            default:
                AspectRecipe recipe = (AspectRecipe)validationResult.getResult();

                return this.compileRecipe(recipe);
        }
    }

    protected @NotNull ValidationResult<AspectRecipe> postValidateRecipeA(@NotNull ValidationResult<AspectRecipe> validationResult) {
        EnumValidationResult recipeStatus = validationResult.getType();
        AspectRecipe recipe = (AspectRecipe)validationResult.getResult();
        if (recipe.isGroovyRecipe()) {
            return validationResult;
        } else {
            boolean emptyInputs = recipe.getInputs().isEmpty() && recipe.getFluidInputs().isEmpty() && recipe.getAspectInputs().isEmpty();
            if (emptyInputs) {
                GTLog.logger.error("Invalid amount of recipe inputs. Recipe inputs are empty.");
                GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Inputs"));

                recipeStatus = EnumValidationResult.INVALID;
            }

            boolean emptyOutputs = !this.allowEmptyOutput &&
                    recipe.getEUt() > 0 &&
                    recipe.getOutputs().isEmpty() &&
                    recipe.getFluidOutputs().isEmpty() &&
                    recipe.getAspectOutputs().isEmpty() &&
                    recipe.getChancedOutputs().getChancedEntries().isEmpty() &&
                    recipe.getChancedFluidOutputs().getChancedEntries().isEmpty() &&
                    recipe.getChancedAspectOutputs().getChancedEntries().isEmpty();
            if (emptyOutputs) {
                GTLog.logger.error("Invalid amount of recipe outputs. Recipe outputs are empty.");
                GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Outputs"));

                recipeStatus = EnumValidationResult.INVALID;
            }

            int amount = recipe.getInputs().size();
            if (amount > this.getMaxInputs()) {
                GTLog.logger.error("Invalid amount of recipe inputs. Actual: {}. Should be at most {}.", amount, this.getMaxInputs());
                GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Inputs"));

                recipeStatus = EnumValidationResult.INVALID;
            }

            amount = recipe.getOutputs().size() + recipe.getChancedOutputs().getChancedEntries().size();
            if (amount > this.getMaxOutputs()) {
                GTLog.logger.error("Invalid amount of recipe outputs. Actual: {}. Should be at most {}.", amount, this.getMaxOutputs());
                GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Outputs"));

                recipeStatus = EnumValidationResult.INVALID;
            }

            amount = recipe.getFluidInputs().size();
            if (amount > this.getMaxFluidInputs()) {
                GTLog.logger.error("Invalid amount of recipe fluid inputs. Actual: {}. Should be at most {}.", amount, this.getMaxFluidInputs());
                GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Fluid Inputs"));

                recipeStatus = EnumValidationResult.INVALID;
            }

            amount = recipe.getFluidOutputs().size() + recipe.getChancedFluidOutputs().getChancedEntries().size();
            if (amount > this.getMaxFluidOutputs()) {
                GTLog.logger.error("Invalid amount of recipe fluid outputs. Actual: {}. Should be at most {}.", amount, this.getMaxFluidOutputs());
                GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Fluid Outputs"));

                recipeStatus = EnumValidationResult.INVALID;
            }

            amount = recipe.getAspectInputs().size();
            if (amount > this.getMaxAspectInputs()) {
                GTLog.logger.error("Invalid amount of recipe fluid inputs. Actual: {}. Should be at most {}.", amount, this.getMaxFluidInputs());
                GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Fluid Inputs"));

                recipeStatus = EnumValidationResult.INVALID;
            }

            amount = recipe.getAspectOutputs().size() + recipe.getChancedAspectOutputs().getChancedEntries().size();
            if (amount > this.getMaxAspectOutputs()) {
                GTLog.logger.error("Invalid amount of recipe fluid outputs. Actual: {}. Should be at most {}.", amount, this.getMaxFluidOutputs());
                GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Fluid Outputs"));

                recipeStatus = EnumValidationResult.INVALID;
            }

            return ValidationResult.newResult(recipeStatus, recipe);
        }
    }

    private boolean shouldShiftWidgets() {
        return this.getMaxInputs() + this.getMaxOutputs() >= 6 || this.getMaxFluidInputs() + this.getMaxFluidOutputs() >= 6 || this.getMaxAspectInputs() + this.getMaxAspectOutputs() >= 6;
    }

    public @Nullable AspectRecipe findRecipe(long voltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, IMultipleAspectTankHandler aspectInputs) {
        return this.findRecipe(voltage, GTUtility.itemHandlerToList(inputs), GTUtility.fluidHandlerToList(fluidInputs), AspectUtil.aspectHandlerToList(aspectInputs));
    }

    public @Nullable AspectRecipe findRecipe(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs, List<AspectStack> aspectInputs) {
        return this.findRecipe(voltage, inputs, fluidInputs, aspectInputs, false);
    }

    public @Nullable AspectRecipe findRecipe(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs, List<AspectStack> aspectInputs, boolean exactVoltage) {
        List<ItemStack> items = (List)inputs.stream().filter((s) -> {
            return !s.isEmpty();
        }).collect(Collectors.toList());
        List<FluidStack> fluids = (List)fluidInputs.stream().filter((f) -> {
            return f != null && f.amount != 0;
        }).collect(Collectors.toList());
        List<AspectStack> aspects = (List)aspectInputs.stream().filter((f) -> {
            return f != null && f.amount != 0;
        }).collect(Collectors.toList());
        return this.find(items, fluids, aspects, (recipe) -> {
            if (exactVoltage && (long)recipe.getEUt() != voltage) {
                return false;
            } else {
                return (long)recipe.getEUt() > voltage ? false : recipe.matches(false, inputs, fluidInputs, aspectInputs);
            }
        });
    }

    public @Nullable AspectRecipe find(@NotNull Collection<ItemStack> items, @NotNull Collection<FluidStack> fluids, @NotNull Collection<AspectStack> aspects, @NotNull Predicate<AspectRecipe> canHandle) {
        List<List<AbstractMapIngredient>> list = this.prepareRecipeFind(items, fluids, aspects);
        return list == null ? null : this.recurseIngredientTreeFindRecipe(list, this.lookup, canHandle);
    }

    protected @Nullable List<List<AbstractMapIngredient>> prepareRecipeFind(@NotNull Collection<ItemStack> items, @NotNull Collection<FluidStack> fluids, @NotNull Collection<AspectStack> aspects) {
        if (items.size() != Integer.MAX_VALUE && fluids.size() != Integer.MAX_VALUE && aspects.size() != Integer.MAX_VALUE) {
            if (items.size() == 0 && fluids.size() == 0 && aspects.size() == 0) {
                return null;
            } else {
                List<List<AbstractMapIngredient>> list = new ObjectArrayList(items.size() + fluids.size() + aspects.size());
                if (items.size() > 0) {
                    this.buildFromItemStacks(list, uniqueItems(items));
                }

                if (fluids.size() > 0) {
                    this.buildFromFluidStacks(list, fluids);
                }

                if (aspects.size() > 0) {
                    this.buildFromAspectStacks(list, aspects);
                }

                return list.size() == 0 ? null : list;
            }
        } else {
            return null;
        }
    }

    private @Nullable AspectRecipe recurseIngredientTreeFindRecipe(@NotNull List<List<AbstractMapIngredient>> ingredients, @NotNull AspectBranch branchRoot, @NotNull Predicate<AspectRecipe> canHandle) {
        for(int i = 0; i < ingredients.size(); ++i) {
            AspectRecipe r = this.recurseIngredientTreeFindRecipe(ingredients, branchRoot, canHandle, i, 0, 1L << i);
            if (r != null) {
                return r;
            }
        }

        return null;
    }

    private @Nullable AspectRecipe recurseIngredientTreeFindRecipe(@NotNull List<List<AbstractMapIngredient>> ingredients, @NotNull AspectBranch branchMap, @NotNull Predicate<AspectRecipe> canHandle, int index, int count, long skip) {
        if (count == ingredients.size()) {
            return null;
        } else {
            Iterator var8 = ((List)ingredients.get(index)).iterator();

            while(var8.hasNext()) {
                AbstractMapIngredient obj = (AbstractMapIngredient)var8.next();
                Map<AbstractMapIngredient, Either<AspectRecipe, AspectBranch>> targetMap = determineRootNodesA(obj, branchMap);
                Either<AspectRecipe, AspectBranch> result = (Either)targetMap.get(obj);
                if (result != null) {
                    AspectRecipe r = (AspectRecipe)result.map((potentialRecipe) -> {
                        return canHandle.test(potentialRecipe) ? potentialRecipe : null;
                    }, (potentialBranch) -> {
                        return this.diveIngredientTreeFindRecipe(ingredients, potentialBranch, canHandle, index, count, skip);
                    });
                    if (r != null) {
                        return r;
                    }
                }
            }

            return null;
        }
    }

    protected static @NotNull Map<AbstractMapIngredient, Either<AspectRecipe, AspectBranch>> determineRootNodesA(@NotNull AbstractMapIngredient ingredient, @NotNull AspectBranch branchMap) {
        return ingredient.isSpecialIngredient() ? branchMap.getSpecialNodes() : branchMap.getNodes();
    }

    private @Nullable AspectRecipe diveIngredientTreeFindRecipe(@NotNull List<List<AbstractMapIngredient>> ingredients, @NotNull AspectBranch map, @NotNull Predicate<AspectRecipe> canHandle, int currentIndex, int count, long skip) {
        for(int i = (currentIndex + 1) % ingredients.size(); i != currentIndex; i = (i + 1) % ingredients.size()) {
            if ((skip & 1L << i) == 0L) {
                AspectRecipe found = this.recurseIngredientTreeFindRecipe(ingredients, map, canHandle, i, count + 1, skip | 1L << i);
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    public @Nullable Set<AspectRecipe> findRecipeCollisions(Collection<ItemStack> items, Collection<FluidStack> fluids, Collection<AspectStack> aspectStacks) {
        List<List<AbstractMapIngredient>> list = this.prepareRecipeFind(items, fluids, aspectStacks);
        if (list == null) {
            return null;
        } else {
            Set<AspectRecipe> collidingRecipes = new ObjectOpenHashSet();
            this.recurseIngredientTreeFindRecipeCollisions(list, this.lookup, collidingRecipes);
            return collidingRecipes;
        }
    }

    private void recurseIngredientTreeFindRecipeCollisions(@NotNull List<List<AbstractMapIngredient>> ingredients, @NotNull AspectBranch branchRoot, @NotNull Set<AspectRecipe> collidingRecipes) {
        for(int i = 0; i < ingredients.size(); ++i) {
            this.recurseIngredientTreeFindRecipeCollisions(ingredients, branchRoot, i, 0, 1L << i, collidingRecipes);
        }

    }

    private @Nullable AspectRecipe recurseIngredientTreeFindRecipeCollisions(@NotNull List<List<AbstractMapIngredient>> ingredients, @NotNull AspectBranch branchMap, int index, int count, long skip, @NotNull Set<AspectRecipe> collidingRecipes) {
        if (count == ingredients.size()) {
            return null;
        } else {
            List<AbstractMapIngredient> wr = (List)ingredients.get(index);
            Iterator var9 = wr.iterator();

            while(var9.hasNext()) {
                AbstractMapIngredient obj = (AbstractMapIngredient)var9.next();
                Map<AbstractMapIngredient, Either<AspectRecipe, AspectBranch>> targetMap = determineRootNodesA(obj, branchMap);
                Either<AspectRecipe, AspectBranch> result = (Either)targetMap.get(obj);
                if (result != null) {
                    AspectRecipe r = (AspectRecipe)result.map((recipe) -> {
                        return recipe;
                    }, (right) -> {
                        return this.diveIngredientTreeFindRecipeCollisions(ingredients, right, index, count, skip, collidingRecipes);
                    });
                    if (r != null) {
                        collidingRecipes.add(r);
                    }
                }
            }

            return null;
        }
    }

    private @Nullable AspectRecipe diveIngredientTreeFindRecipeCollisions(@NotNull List<List<AbstractMapIngredient>> ingredients, @NotNull AspectBranch map, int currentIndex, int count, long skip, @NotNull Set<AspectRecipe> collidingRecipes) {
        for(int i = (currentIndex + 1) % ingredients.size(); i != currentIndex; i = (i + 1) % ingredients.size()) {
            if ((skip & 1L << i) == 0L) {
                AspectRecipe r = this.recurseIngredientTreeFindRecipeCollisions(ingredients, map, i, count + 1, skip | 1L << i, collidingRecipes);
                if (r != null) {
                    return r;
                }
            }
        }

        return null;
    }

    public ModularUI.Builder createJeiUITemplate(IItemHandlerModifiable importItems,
                                                 IItemHandlerModifiable exportItems,
                                                 FluidTankList importFluids,
                                                 FluidTankList exportFluids,
                                                 AspectTankList importAspects,
                                                 AspectTankList exportAspects,
                                                 int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        builder.widget(new RecipeProgressWidget(200, 78, 23 + yOffset, 20, 20, this.progressBarTexture, this.moveType, this));
        this.addInventorySlotGroupA(builder, importItems, importFluids, importAspects, false, yOffset);
        this.addInventorySlotGroupA(builder, exportItems, exportFluids, exportAspects,  true, yOffset);
        if (this.specialTexture != null && this.specialTexturePosition != null) {
            this.addSpecialTexture(builder);
        }

        return builder;
    }

    protected void addInventorySlotGroupA(ModularUI.Builder builder,
                                         IItemHandlerModifiable itemHandler,
                                         FluidTankList fluidHandler,
                                         AspectTankList aspectHandler,
                                         boolean isOutputs, int yOffset) {
        int itemInputsCount = itemHandler.getSlots();
        int fluidInputsCount = fluidHandler.getTanks();
        int aspectInputsCount = aspectHandler.getTanks();
        boolean invertFluids = false;
        boolean invertAspects = false;
        if (itemInputsCount == 0) {
            int tmp = itemInputsCount;
            if (fluidInputsCount == 0)
            {
                itemInputsCount = aspectInputsCount;
                aspectInputsCount = tmp;
                invertAspects = true;
            }   else
            {
                itemInputsCount = fluidInputsCount;
                fluidInputsCount = tmp;
                invertFluids = true;
            }
        }

        int[] inputSlotGrid = determineSlotsGrid(itemInputsCount);
        int itemSlotsToLeft = inputSlotGrid[0];
        int itemSlotsToDown = inputSlotGrid[1];
        int startInputsX = isOutputs ? 106 : 70 - itemSlotsToLeft * 18;
        int startInputsY = 33 - (int)((double)itemSlotsToDown / 2.0 * 18.0) + yOffset;
        boolean wasGroup = itemHandler.getSlots() + fluidHandler.getTanks() + aspectHandler.getTanks() == 12;
        if (wasGroup) {
            startInputsY -= 9;
        } else if (itemHandler.getSlots() >= 6 && fluidHandler.getTanks() >= 2 && !isOutputs) {
            startInputsY -= 9;
        }

        int slotDown;
        int slotLeft;
        int slot;
        int x;
        int y;
        for(slotDown = 0; slotDown < itemSlotsToDown; ++slotDown) {
            for(slotLeft = 0; slotLeft < itemSlotsToLeft; ++slotLeft) {
                slot = slotDown * itemSlotsToLeft + slotLeft;
                if (slot >= itemInputsCount) {
                    break;
                }

                x = startInputsX + 18 * slotLeft;
                y = startInputsY + 18 * slotDown;
                this.addSlotA(builder, x, y, slot, itemHandler, fluidHandler, aspectHandler, invertFluids, invertAspects, isOutputs);
            }
        }

        if (wasGroup) {
            startInputsY += 2;
        }

        if (fluidInputsCount > 0 || invertFluids) {
            if (itemSlotsToDown >= fluidInputsCount && itemSlotsToLeft < 3) {
                x = isOutputs ? startInputsX + itemSlotsToLeft * 18 : startInputsX - 18;

                for(slotLeft = 0; slotLeft < fluidInputsCount; ++slotLeft) {
                    y = startInputsY + 18 * slotLeft;
                    this.addSlotA(builder, x, y, slotLeft, itemHandler, fluidHandler, aspectHandler, !invertFluids, invertAspects, isOutputs);
                }
            } else {
                slotDown = startInputsY + itemSlotsToDown * 18;

                for(slotLeft = 0; slotLeft < fluidInputsCount; ++slotLeft) {
                    x = isOutputs ? startInputsX + 18 * (slotLeft % 3) : startInputsX + itemSlotsToLeft * 18 - 18 - 18 * (slotLeft % 3);
                    y = slotDown + slotLeft / 3 * 18;
                    this.addSlotA(builder, x, y, slotLeft, itemHandler, fluidHandler, aspectHandler, !invertFluids, invertAspects, isOutputs);
                }
            }
        }

        if (aspectInputsCount > 0 || invertAspects) {
            if (itemSlotsToDown >= aspectInputsCount && itemSlotsToLeft < 3) {
                x = isOutputs ? startInputsX + itemSlotsToLeft * 18 : startInputsX - 18;

                for(slotLeft = 0; slotLeft < aspectInputsCount; ++slotLeft) {
                    y = startInputsY + 18 * slotLeft;
                    this.addSlotA(builder, x, y, slotLeft, itemHandler, fluidHandler, aspectHandler, invertFluids, !invertAspects, isOutputs);
                }
            } else {
                slotDown = startInputsY + itemSlotsToDown * 18;

                for(slotLeft = 0; slotLeft < aspectInputsCount; ++slotLeft) {
                    x = isOutputs ? startInputsX + 18 * (slotLeft % 3) : startInputsX + itemSlotsToLeft * 18 - 18 - 18 * (slotLeft % 3);
                    y = slotDown + slotLeft / 3 * 18;
                    this.addSlotA(builder, x, y, slotLeft, itemHandler, fluidHandler, aspectHandler, !invertFluids, !invertAspects, isOutputs);
                }
            }
        }

    }

    protected void addSlotA(ModularUI.Builder builder,
                           int x, int y, int slotIndex,
                           IItemHandlerModifiable itemHandler,
                           FluidTankList fluidHandler,
                           AspectTankList aspectHandler,
                           boolean isFluid,
                           boolean isAspect,
                           boolean isOutputs) {
        if (!isFluid) {
            if (!isAspect)
            {
                builder.widget(
                        (new SlotWidget(itemHandler, slotIndex, x, y, true, !isOutputs))
                                .setBackgroundTexture(this.getOverlaysForSlot(isOutputs, false, false, slotIndex == itemHandler.getSlots() - 1)));
            } else
            {
                builder.widget((new AspectTankWidget(aspectHandler.getTankAt(slotIndex), x, y, 18, 18))
                        .setAlwaysShowFull(true)
                        .setBackgroundTexture(this.getOverlaysForSlot(isOutputs, false, true,  slotIndex == aspectHandler.getTanks() - 1))
                        .setContainerClicking(true, !isOutputs)
                        .setEnableColor(true));
            }
            }
        else {
            builder.widget((new TankWidget(fluidHandler.getTankAt(slotIndex), x, y, 18, 18))
                    .setAlwaysShowFull(true)
                    .setBackgroundTexture(this.getOverlaysForSlot(isOutputs, true, false,slotIndex == fluidHandler.getTanks() - 1))
                    .setContainerClicking(true, !isOutputs));
        }

    }

    protected TextureArea[] getOverlaysForSlot(boolean isOutput, boolean isFluid, boolean isAspect, boolean isLast) {
        TextureArea base = null;
        if (isFluid)
        {
            base = GuiTextures.FLUID_SLOT;
        }   else if (isAspect)
        {
            base = ZZTextures.ASPECT_SLOT;
        }   else
        {
            base = GuiTextures.SLOT;
        }
        byte overlayKey = (byte)((isOutput ? 2 : 0) + (isFluid ? 1 : 0) + (isLast ? 4 : 0));
        return this.slotOverlays.containsKey(overlayKey) ? new TextureArea[]{base, (TextureArea)this.slotOverlays.get(overlayKey)} : new TextureArea[]{base};
    }

    public int getPropertyHeightShift() {
        int maxPropertyCount = 0;
        if (this.shouldShiftWidgets()) {
            Iterator var2 = this.getRecipeListA().iterator();

            while(var2.hasNext()) {
                Recipe recipe = (Recipe)var2.next();
                if (recipe.getPropertyCount() > maxPropertyCount) {
                    maxPropertyCount = recipe.getPropertyCount();
                }
            }
        }

        return maxPropertyCount * 10;
    }

    protected void buildFromAspectStacks(@NotNull List<List<AbstractMapIngredient>> list, @NotNull Iterable<AspectStack> ingredients) {
        for (AspectStack t : ingredients) {
            list.add(Collections.singletonList(new MapAspectIngredient(t)));
        }
    }

    public boolean compileRecipe(AspectRecipe recipe) {
        if (recipe == null) {
            return false;
        } else {
            List<List<AbstractMapIngredient>> items = this.fromRecipe(recipe);
            if (this.recurseIngredientTreeAdd(recipe, items, this.lookup, 0, 0)) {
                this.recipeByCategory.compute(recipe.getZZRecipeCategory(), (k, v) -> {
                    if (v == null) {
                        v = new ArrayList();
                    }

                    ((List)v).add(recipe);
                    return (List)v;
                });
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean recurseIngredientTreeAdd(@NotNull AspectRecipe recipe, @NotNull List<List<AbstractMapIngredient>> ingredients, @NotNull AspectBranch branchMap, int index, int count) {
        if (count >= ingredients.size()) {
            return true;
        } else if (index >= ingredients.size()) {
            throw new RuntimeException("Index out of bounds for recurseItemTreeAdd, should not happen");
        } else {
            List<AbstractMapIngredient> current = (List)ingredients.get(index);
            AspectBranch branchRight = new AspectBranch();
            Iterator var9 = current.iterator();

            while(var9.hasNext()) {
                AbstractMapIngredient obj = (AbstractMapIngredient)var9.next();
                Map<AbstractMapIngredient, Either<AspectRecipe, AspectBranch>> targetMap = determineRootNodesA(obj, branchMap);
                Either<AspectRecipe, AspectBranch> r = (Either)targetMap.compute(obj, (k, v) -> {
                    if (count == ingredients.size() - 1) {
                        if (v == null) {
                            return Either.left(recipe);
                        } else {
                            if (!v.left().isPresent() || v.left().get() != recipe) {
                                if (ConfigHolder.misc.debug || GTValues.isDeobfEnvironment()) {
                                    GTLog.logger.warn("Recipe duplicate or conflict found in RecipeMap {} and was not added. See next lines for details", this.unlocalizedName);
                                    GTLog.logger.warn("Attempted to add Recipe: {}", recipe.toString());
                                    if (v.left().isPresent()) {
                                        GTLog.logger.warn("Which conflicts with: {}", ((AspectRecipe)v.left().get()).toString());
                                    } else {
                                        GTLog.logger.warn("Could not find exact duplicate/conflict.");
                                    }
                                }
                            }

                            return v;
                        }
                    } else {
                        return v == null ? Either.right(branchRight) : v;
                    }
                });
                if (r.left().isPresent()) {
                    if (r.left().get() != recipe) {
                        return false;
                    }
                } else {
                    boolean addedNextBranch = r.right().filter((m) -> {
                        return this.recurseIngredientTreeAdd(recipe, ingredients, m, (index + 1) % ingredients.size(), count + 1);
                    }).isPresent();
                    if (!addedNextBranch) {
                        if (count == ingredients.size() - 1) {
                            targetMap.remove(obj);
                        } else if (((Either)targetMap.get(obj)).right().isPresent() && ((AspectBranch)((Either)targetMap.get(obj)).right().get()).isEmptyBranch()) {
                            targetMap.remove(obj);
                        }

                        return false;
                    }
                }
            }

            return true;
        }
    }

    public boolean removeRecipe(@NotNull AspectRecipe recipe) {
        List<List<AbstractMapIngredient>> items = this.fromRecipe(recipe);
        if (this.recurseIngredientTreeRemove(recipe, items, this.lookup, 0) != null) {

            this.recipeByCategory.compute(recipe.getZZRecipeCategory(), (k, v) -> {
                if (v != null) {
                    v.remove(recipe);
                }

                return v != null && !v.isEmpty() ? v : null;
            });
            return true;
        } else {
            return false;
        }
    }

    private @Nullable Recipe recurseIngredientTreeRemove(@NotNull AspectRecipe recipeToRemove, @NotNull List<List<AbstractMapIngredient>> ingredients, @NotNull AspectBranch branchMap, int depth) {
        Iterator var5 = ingredients.iterator();

        while(var5.hasNext()) {
            List<AbstractMapIngredient> current = (List)var5.next();
            Iterator var7 = current.iterator();

            while(var7.hasNext()) {
                AbstractMapIngredient obj = (AbstractMapIngredient)var7.next();
                Map<AbstractMapIngredient, Either<AspectRecipe, AspectBranch>> targetMap = determineRootNodesA(obj, branchMap);
                AspectRecipe found = null;
                Either<AspectRecipe, AspectBranch> result = (Either)targetMap.get(obj);
                if (result != null) {
                    AspectRecipe r = (AspectRecipe)result.map((potentialRecipe) -> {
                        return potentialRecipe;
                    }, (potentialBranch) -> {
                        return this.recurseIngredientTreeRemove(recipeToRemove, ingredients.subList(1, ingredients.size()), potentialBranch, depth + 1);
                    });
                    if (r == recipeToRemove) {
                        found = r;
                    } else {
                        if (ConfigHolder.misc.debug || GTValues.isDeobfEnvironment()) {
                            GTLog.logger.warn("Failed to remove recipe from RecipeMap {}. See next lines for details", this.unlocalizedName);
                            GTLog.logger.warn("Failed to remove Recipe: {}", recipeToRemove.toString());
                        }
                    }
                }

                if (found != null) {
                    if (ingredients.size() == 1) {
                        targetMap.remove(obj);
                    } else if (((Either)targetMap.get(obj)).right().isPresent()) {
                        AspectBranch branch = (AspectBranch)((Either)targetMap.get(obj)).right().get();
                        if (branch.isEmptyBranch()) {
                            targetMap.remove(obj);
                        }
                    }

                    return found;
                }
            }
        }

        return null;
    }

    protected @NotNull List<List<AbstractMapIngredient>> fromRecipe(@NotNull AspectRecipe r) {
        List<List<AbstractMapIngredient>> list = new ObjectArrayList(r.getInputs().size() + r.getFluidInputs().size() + r.getAspectInputs().size());
        if (r.getInputs().size() > 0) {
            this.buildFromRecipeItems(list, uniqueIngredientsList(r.getInputs()));
        }

        if (r.getFluidInputs().size() > 0) {
            this.buildFromRecipeFluids(list, r.getFluidInputs());
        }

        if (r.getAspectInputs().size() > 0) {
            this.buildFromRecipeAspects(list, r.getAspectInputs());
        }

        return list;
    }

    protected void buildFromRecipeAspects(@NotNull List<List<AbstractMapIngredient>> list, @NotNull List<AspectRecipeInput> aspectInputs) {
        for (AspectRecipeInput fluidInput : aspectInputs) {
            AbstractMapIngredient ingredient = new MapAspectIngredient(fluidInput);
            retrieveCachedIngredient(list, ingredient, this.aspectIngredientRoot);
        }

    }



    public Collection<AspectRecipe> getRecipeListA() {
        ObjectOpenHashSet<AspectRecipe> recipes = new ObjectOpenHashSet();
        Stream<AspectRecipe> var10000 = this.lookup.getRecipes(true);
        Objects.requireNonNull(recipes);
        return (Collection)var10000.filter(recipes::add).sorted(RECIPE_DURATION_THEN_EU).collect(Collectors.toList());
    }

    public R recipeBuilder() {
        R sample = this.recipeBuilderSample;
        R copy = sample.copy();
        R build = copy.onBuild(this.onRecipeBuildAction);
        return build;
    }



    public @NotNull Map<ZZRecipeCategory, List<AspectRecipe>> getRecipesByCategoryA() {
        return Collections.unmodifiableMap(this.recipeByCategory);
    }

    public int getMaxAspectInputs() {
        return this.maxAspectInputs;
    }

    public void setMaxAspectInputs(int maxFluidInputs) {
        if (this.modifyAspectInputs) {
            this.maxAspectInputs = Math.max(this.maxAspectInputs, maxFluidInputs);
        } else {
            throw new UnsupportedOperationException("Cannot change max aspect input amount for " + this.getUnlocalizedName());
        }
    }

    public ModularUI.Builder createUITemplate(DoubleSupplier progressSupplier, IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids,
                                              AspectTankList importAspects, AspectTankList exportAspect, int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        builder.widget(new AspectRecipeProgressWidget(progressSupplier, 78, 23 + yOffset, 20, 20, this.progressBarTexture, this.moveType, this));
        this.addInventorySlotGroupA(builder, importItems, importFluids, importAspects, false, yOffset);
        this.addInventorySlotGroupA(builder, exportItems, exportFluids, exportAspect, true, yOffset);
        if (this.specialTexture != null && this.specialTexturePosition != null) {
            this.addSpecialTexture(builder);
        }

        return builder;
    }

    public ModularUI.Builder createUITemplateNoOutputs(DoubleSupplier progressSupplier, IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids,
                                                       AspectTankList importAspects, AspectTankList exportAspect, int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        builder.widget(new AspectRecipeProgressWidget(progressSupplier, 78, 23 + yOffset, 20, 20, this.progressBarTexture, this.moveType, this));
        this.addInventorySlotGroupA(builder, importItems, importFluids, importAspects, false, yOffset);
        if (this.specialTexture != null && this.specialTexturePosition != null) {
            this.addSpecialTexture(builder);
        }

        return builder;
    }

    protected void addInventorySlotGroup(ModularUI.Builder builder, IItemHandlerModifiable itemHandler, FluidTankList fluidHandler, AspectTankList aspectHandler, boolean isOutputs, int yOffset) {
        int itemInputsCount = itemHandler.getSlots();
        int fluidInputsCount = fluidHandler.getTanks();
        int aspectInputCount = aspectHandler.getTanks();
        boolean invertFluids = false;
        boolean invertAspects = false;
        if (itemInputsCount == 0) {
            int tmp = itemInputsCount;
            if (fluidInputsCount == 0)
            {
                itemInputsCount = aspectInputCount;
                aspectInputCount = tmp;
                invertAspects = true;
            } else
            {
                itemInputsCount = fluidInputsCount;
                fluidInputsCount = tmp;
                invertFluids = true;
            }
        }

        int[] inputSlotGrid = determineSlotsGrid(itemInputsCount);
        int itemSlotsToLeft = inputSlotGrid[0];
        int itemSlotsToDown = inputSlotGrid[1];
        int startInputsX = isOutputs ? 106 : 70 - itemSlotsToLeft * 18;
        int startInputsY = 33 - (int)((double)itemSlotsToDown / 2.0 * 18.0) + yOffset;
        boolean wasGroup = itemHandler.getSlots() + fluidHandler.getTanks() + aspectHandler.getTanks() == 12;
        if (wasGroup) {
            startInputsY -= 9;
        } else if (itemHandler.getSlots() >= 4 && fluidHandler.getTanks() >= 2 && aspectHandler.getTanks() >=2 && !isOutputs) {
            startInputsY -= 9;
        }

        int startSpecY;
        int i;
        int x;
        int y;
        for(startSpecY = 0; startSpecY < itemSlotsToDown; ++startSpecY) {
            for(i = 0; i < itemSlotsToLeft; ++i) {
                x = startSpecY * itemSlotsToLeft + i;
                if (x >= itemInputsCount) {
                    break;
                }

                y = startInputsX + 18 * i;
                y = startInputsY + 18 * startSpecY;
                this.addSlotA(builder, y, y, x, itemHandler, fluidHandler, aspectHandler, invertFluids, invertAspects, isOutputs);
            }
        }

        if (wasGroup) {
            startInputsY += 2;
        }

        if (fluidInputsCount > 0 || invertFluids) {
            if (itemSlotsToDown >= fluidInputsCount && itemSlotsToLeft < 3) {
                startSpecY = isOutputs ? startInputsX + itemSlotsToLeft * 18 : startInputsX - 18;

                for(i = 0; i < fluidInputsCount; ++i) {
                    x = startInputsY + 18 * i;
                    this.addSlotA(builder, startSpecY, x, i, itemHandler, fluidHandler, aspectHandler, !invertFluids, invertAspects, isOutputs);
                }
            } else {
                startSpecY = startInputsY + itemSlotsToDown * 18;

                for(i = 0; i < fluidInputsCount; ++i) {
                    x = isOutputs ? startInputsX + 18 * (i % 3) : startInputsX + itemSlotsToLeft * 18 - 18 - 18 * (i % 3);
                    y = startSpecY + i / 3 * 18;
                    this.addSlotA(builder, x, y, i, itemHandler, fluidHandler, aspectHandler, !invertFluids, invertAspects, isOutputs);
                }
            }
        }

        if (aspectInputCount > 0 || invertAspects) {
            if (itemSlotsToDown >= aspectInputCount && itemSlotsToLeft < 3) {
                startSpecY = isOutputs ? startInputsX + itemSlotsToLeft * 18 : startInputsX - 18;

                for(i = 0; i < aspectInputCount; ++i) {
                    x = startInputsY + 18 * i;
                    this.addSlotA(builder, startSpecY, x, i, itemHandler, fluidHandler, aspectHandler, invertFluids, !invertAspects, isOutputs);
                }
            } else {
                startSpecY = startInputsY + itemSlotsToDown * 18;

                for(i = 0; i < aspectInputCount; ++i) {
                    x = isOutputs ? startInputsX + 18 * (i % 3) : startInputsX + itemSlotsToLeft * 18 - 18 - 18 * (i % 3);
                    y = startSpecY + i / 3 * 18;
                    this.addSlotA(builder, x, y, i, itemHandler, fluidHandler, aspectHandler, invertFluids, !invertAspects, isOutputs);
                }
            }
        }

    }

    protected static int[] determineSlotsGrid(int itemInputsCount) {
        double sqrt = Math.sqrt((double)itemInputsCount);
        int itemSlotsToLeft;
        int itemSlotsToDown;
        if (sqrt % 1.0 == 0.0) {
            itemSlotsToLeft = itemSlotsToDown = (int)sqrt;
        } else if (itemInputsCount == 3) {
            itemSlotsToLeft = 3;
            itemSlotsToDown = 1;
        } else {
            itemSlotsToLeft = (int)Math.ceil(sqrt);
            itemSlotsToDown = itemSlotsToLeft - 1;
            if (itemInputsCount > itemSlotsToLeft * itemSlotsToDown) {
                itemSlotsToDown = itemSlotsToLeft;
            }
        }

        return new int[]{itemSlotsToLeft, itemSlotsToDown};
    }

    public int getMaxAspectOutputs() {
        return this.maxAspectOutputs;
    }
}

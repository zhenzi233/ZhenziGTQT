//package com.zhenzi.zhenzigtqt.loaders;
//
//import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectKey;
//import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectStack;
//import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectUtil;
//import com.zhenzi.zhenzigtqt.common.lib.aspect.IMultipleAspectTankHandler;
//import gregtech.api.capability.IMultipleTankHandler;
//import gregtech.api.metatileentity.IVoidable;
//import gregtech.api.recipes.FluidKey;
//import gregtech.api.recipes.Recipe;
//import gregtech.api.recipes.RecipeBuilder;
//import gregtech.api.recipes.RecipeMap;
//import gregtech.api.recipes.ingredients.GTRecipeInput;
//import gregtech.api.util.GTHashMaps;
//import gregtech.api.util.ItemStackHashStrategy;
//import gregtech.api.util.OverlayedFluidHandler;
//import gregtech.api.util.OverlayedItemHandler;
//import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
//import it.unimi.dsi.fastutil.objects.Object2IntMap;
//import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
//import it.unimi.dsi.fastutil.objects.ObjectIterator;
//import net.minecraft.item.ItemStack;
//import net.minecraftforge.fluids.FluidStack;
//import net.minecraftforge.items.IItemHandlerModifiable;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.*;
//
//public abstract class AspectParallelLogic {
//    public AspectParallelLogic() {
//    }
//
//    public static int getMaxRecipeMultiplier(@NotNull AspectRecipe recipe, @NotNull IItemHandlerModifiable inputs, @NotNull IMultipleTankHandler fluidInputs, @NotNull IMultipleAspectTankHandler aspectInputs, int parallelAmount) {
//        Object2IntMap<ItemStack> ingredientStacks = GTHashMaps.fromItemHandler(inputs);
//        Map<FluidKey, Integer> fluidStacks = GTHashMaps.fromFluidHandler(fluidInputs);
//        Map<AspectKey, Integer> aspectStacks = AspectUtil.fromFluidHandler(aspectInputs);
//        int itemMultiplier = getMaxRatioItem(ingredientStacks, recipe, parallelAmount);
//        int fluidMultiplier = getMaxRatioFluid(fluidStacks, recipe, parallelAmount);
//        int aspectMultiplier = getMaxRatioAspect(aspectStacks, recipe, parallelAmount);
//        return itemMultiplier == Integer.MAX_VALUE &&
//                fluidMultiplier == Integer.MAX_VALUE ? 0 : Math.min(itemMultiplier, fluidMultiplier);
//    }
//
//    public static int limitByOutputMerging(@NotNull AspectRecipe recipe, @NotNull IItemHandlerModifiable outputs, @NotNull IMultipleTankHandler fluidOutputs, @NotNull IMultipleAspectTankHandler aspectOutputs, int parallelAmount, boolean voidItems, boolean voidFluids) {
//        int modifiedItemParallelAmount = Integer.MAX_VALUE;
//        int modifiedFluidParallelAmount = Integer.MAX_VALUE;
//        if (voidItems && voidFluids) {
//            return parallelAmount;
//        } else {
//            if (!recipe.getOutputs().isEmpty() || !recipe.getChancedOutputs().getChancedEntries().isEmpty()) {
//                if (voidItems) {
//                    modifiedItemParallelAmount = parallelAmount;
//                } else {
//                    modifiedItemParallelAmount = limitParallelByItems(recipe, new OverlayedItemHandler(outputs), parallelAmount);
//                }
//
//                if (modifiedItemParallelAmount == 0 && !voidItems) {
//                    return 0;
//                }
//            }
//
//            if (!recipe.getFluidOutputs().isEmpty() || !recipe.getChancedFluidOutputs().getChancedEntries().isEmpty()) {
//                if (voidFluids) {
//                    modifiedFluidParallelAmount = parallelAmount;
//                } else {
//                    modifiedFluidParallelAmount = limitParallelByFluids(recipe, new OverlayedFluidHandler(fluidOutputs), modifiedItemParallelAmount);
//                }
//
//                if (modifiedFluidParallelAmount == 0 && !voidFluids) {
//                    return 0;
//                }
//            }
//
//            return Math.min(modifiedFluidParallelAmount, modifiedItemParallelAmount);
//        }
//    }
//
//    public static int limitParallelByItems(@NotNull Recipe recipe, @NotNull OverlayedItemHandler overlayedItemHandler, int multiplier) {
//        int minMultiplier = 0;
//        int maxMultiplier = multiplier;
//
//        int[] bin;
//        for(Object2IntMap<ItemStack> recipeOutputs = GTHashMaps.fromItemStackCollection(recipe.getAllItemOutputs()); minMultiplier != maxMultiplier; maxMultiplier = bin[2]) {
//            overlayedItemHandler.reset();
//            int returnedAmount = 0;
//            ObjectIterator var8 = recipeOutputs.object2IntEntrySet().iterator();
//
//            while(var8.hasNext()) {
//                Object2IntMap.Entry<ItemStack> entry = (Object2IntMap.Entry)var8.next();
//                int amountToInsert;
//                if (entry.getIntValue() != 0 && multiplier > Integer.MAX_VALUE / entry.getIntValue()) {
//                    amountToInsert = Integer.MAX_VALUE;
//                } else {
//                    amountToInsert = entry.getIntValue() * multiplier;
//                }
//
//                returnedAmount = overlayedItemHandler.insertStackedItemStack((ItemStack)entry.getKey(), amountToInsert);
//                if (returnedAmount > 0) {
//                    break;
//                }
//            }
//
//            bin = adjustMultiplier(returnedAmount == 0, minMultiplier, multiplier, maxMultiplier);
//            minMultiplier = bin[0];
//            multiplier = bin[1];
//        }
//
//        return multiplier;
//    }
//
//    public static int limitParallelByItemsIncremental(@NotNull List<ItemStack> recipeOutputList, @NotNull List<ItemStack> outputsToAppend, @NotNull OverlayedItemHandler overlayedItemHandler, int multiplier) {
//        int minMultiplier = 0;
//        int currentMultiplier = multiplier;
//        int maxMultiplier = multiplier;
//        int previousMultiplier = multiplier;
//        Object2IntMap<ItemStack> recipeOutputs = GTHashMaps.fromItemStackCollection(recipeOutputList);
//        Object2IntMap<ItemStack> recipeOutputsToAppend = GTHashMaps.fromItemStackCollection(outputsToAppend);
//        Object2IntMap<ItemStack> appendedResultMap = new Object2IntLinkedOpenCustomHashMap(recipeOutputs, ItemStackHashStrategy.comparingAllButCount());
//        recipeOutputsToAppend.forEach((stackKey, amt) -> {
//            appendedResultMap.merge(stackKey, amt * multiplier, Integer::sum);
//        });
//
//        while(minMultiplier != maxMultiplier) {
//            overlayedItemHandler.reset();
//            int returnedAmount;
//            if (currentMultiplier != previousMultiplier) {
//                returnedAmount = currentMultiplier - previousMultiplier;
//                recipeOutputsToAppend.forEach((sk, amt) -> {
//                    appendedResultMap.put(sk, (Integer)appendedResultMap.get(sk) + amt * returnedAmount);
//                });
//                previousMultiplier = currentMultiplier;
//            }
//
//            returnedAmount = 0;
//            ObjectIterator var12 = appendedResultMap.object2IntEntrySet().iterator();
//
//            while(var12.hasNext()) {
//                Object2IntMap.Entry<ItemStack> entry = (Object2IntMap.Entry)var12.next();
//                int amountToInsert = entry.getIntValue();
//                returnedAmount = overlayedItemHandler.insertStackedItemStack((ItemStack)entry.getKey(), amountToInsert);
//                if (returnedAmount > 0) {
//                    break;
//                }
//            }
//
//            int[] bin = adjustMultiplier(returnedAmount == 0, minMultiplier, currentMultiplier, maxMultiplier);
//            minMultiplier = bin[0];
//            currentMultiplier = bin[1];
//            maxMultiplier = bin[2];
//        }
//
//        return currentMultiplier;
//    }
//
//    public static int @NotNull [] adjustMultiplier(boolean mergedAll, int minMultiplier, int multiplier, int maxMultiplier) {
//        if (mergedAll) {
//            minMultiplier = multiplier;
//            int remainder = (maxMultiplier - multiplier) % 2;
//            multiplier = multiplier + remainder + (maxMultiplier - multiplier) / 2;
//        } else {
//            maxMultiplier = multiplier;
//            multiplier = (multiplier + minMultiplier) / 2;
//        }
//
//        if (maxMultiplier - minMultiplier <= 1) {
//            maxMultiplier = minMultiplier;
//            multiplier = minMultiplier;
//        }
//
//        return new int[]{minMultiplier, multiplier, maxMultiplier};
//    }
//
//    public static int limitParallelByFluids(@NotNull Recipe recipe, @NotNull OverlayedFluidHandler overlayedFluidHandler, int multiplier) {
//        int minMultiplier = 0;
//
//        int[] bin;
//        for(int maxMultiplier = multiplier; minMultiplier != maxMultiplier; maxMultiplier = bin[2]) {
//            overlayedFluidHandler.reset();
//            int amountLeft = 0;
//            Iterator var6 = recipe.getFluidOutputs().iterator();
//
//            while(var6.hasNext()) {
//                FluidStack fluidStack = (FluidStack)var6.next();
//                if (fluidStack.amount > 0) {
//                    if (multiplier > Integer.MAX_VALUE / fluidStack.amount) {
//                        amountLeft = Integer.MAX_VALUE;
//                    } else {
//                        amountLeft = fluidStack.amount * multiplier;
//                    }
//
//                    int inserted = overlayedFluidHandler.insertFluid(fluidStack, amountLeft);
//                    if (inserted > 0) {
//                        amountLeft -= inserted;
//                    }
//
//                    if (amountLeft > 0) {
//                        break;
//                    }
//                }
//            }
//
//            bin = adjustMultiplier(amountLeft == 0, minMultiplier, multiplier, maxMultiplier);
//            minMultiplier = bin[0];
//            multiplier = bin[1];
//        }
//
//        return multiplier;
//    }
//
//    protected static int getMaxRatioItem(@NotNull Object2IntMap<ItemStack> countIngredients, @NotNull Recipe recipe, int parallelAmount) {
//        int minMultiplier = Integer.MAX_VALUE;
//        Object2IntOpenHashMap<GTRecipeInput> notConsumableMap = new Object2IntOpenHashMap();
//        Object2IntOpenHashMap<GTRecipeInput> countableMap = new Object2IntOpenHashMap();
//        Iterator var6 = recipe.getInputs().iterator();
//
//        int needed;
//        while(var6.hasNext()) {
//            GTRecipeInput recipeIngredient = (GTRecipeInput)var6.next();
//            needed = recipeIngredient.getAmount();
//            if (recipeIngredient.isNonConsumable()) {
//                notConsumableMap.computeIfPresent(recipeIngredient, (k, v) -> {
//                    return v + needed;
//                });
//                notConsumableMap.putIfAbsent(recipeIngredient, needed);
//            } else {
//                countableMap.computeIfPresent(recipeIngredient, (k, v) -> {
//                    return v + needed;
//                });
//                countableMap.putIfAbsent(recipeIngredient, needed);
//            }
//        }
//
//        ObjectIterator var12 = notConsumableMap.object2IntEntrySet().iterator();
//
//        int available;
//        do {
//            ObjectIterator var10;
//            Object2IntMap.Entry inventoryEntry;
//            Object2IntMap.Entry recipeInputEntry;
//            if (!var12.hasNext()) {
//                if (countableMap.isEmpty() && !notConsumableMap.isEmpty()) {
//                    return parallelAmount;
//                }
//
//                var12 = countableMap.object2IntEntrySet().iterator();
//
//                while(var12.hasNext()) {
//                    recipeInputEntry = (Object2IntMap.Entry)var12.next();
//                    needed = recipeInputEntry.getIntValue();
//                    available = 0;
//                    var10 = countIngredients.object2IntEntrySet().iterator();
//
//                    while(var10.hasNext()) {
//                        inventoryEntry = (Object2IntMap.Entry)var10.next();
//                        if (((GTRecipeInput)recipeInputEntry.getKey()).acceptsStack((ItemStack)inventoryEntry.getKey())) {
//                            available += inventoryEntry.getIntValue();
//                        }
//                    }
//
//                    if (available < needed) {
//                        return 0;
//                    }
//
//                    int ratio = Math.min(parallelAmount, available / needed);
//                    if (ratio < minMultiplier) {
//                        minMultiplier = ratio;
//                    }
//                }
//
//                return minMultiplier;
//            }
//
//            recipeInputEntry = (Object2IntMap.Entry)var12.next();
//            needed = recipeInputEntry.getIntValue();
//            available = 0;
//            var10 = countIngredients.object2IntEntrySet().iterator();
//
//            while(var10.hasNext()) {
//                inventoryEntry = (Object2IntMap.Entry)var10.next();
//                if (((GTRecipeInput)recipeInputEntry.getKey()).acceptsStack((ItemStack)inventoryEntry.getKey())) {
//                    available = inventoryEntry.getIntValue();
//                    if (available > needed) {
//                        inventoryEntry.setValue(available - needed);
//                        needed -= available;
//                        break;
//                    }
//
//                    inventoryEntry.setValue(0);
//                    recipeInputEntry.setValue(needed - available);
//                    needed -= available;
//                }
//            }
//        } while(needed < available);
//
//        return 0;
//    }
//
//    protected static int getMaxRatioFluid(@NotNull Map<FluidKey, Integer> countFluid, @NotNull Recipe recipe, int parallelAmount) {
//        int minMultiplier = Integer.MAX_VALUE;
//        Map<FluidKey, Integer> fluidCountMap = new HashMap();
//        Map<FluidKey, Integer> notConsumableMap = new HashMap();
//        Iterator var6 = recipe.getFluidInputs().iterator();
//
//        int needed;
//        while(var6.hasNext()) {
//            GTRecipeInput fluidInput = (GTRecipeInput)var6.next();
//            needed = fluidInput.getAmount();
//            if (fluidInput.isNonConsumable()) {
//                notConsumableMap.computeIfPresent(new FluidKey(fluidInput.getInputFluidStack()), (k, v) -> {
//                    return v + needed;
//                });
//                notConsumableMap.putIfAbsent(new FluidKey(fluidInput.getInputFluidStack()), needed);
//            } else {
//                fluidCountMap.computeIfPresent(new FluidKey(fluidInput.getInputFluidStack()), (k, v) -> {
//                    return v + needed;
//                });
//                fluidCountMap.putIfAbsent(new FluidKey(fluidInput.getInputFluidStack()), needed);
//            }
//        }
//
//        var6 = notConsumableMap.entrySet().iterator();
//
//        int available;
//        do {
//            Iterator var10;
//            Map.Entry inputFluid;
//            Map.Entry fs;
//            if (!var6.hasNext()) {
//                if (fluidCountMap.isEmpty() && !notConsumableMap.isEmpty()) {
//                    return parallelAmount;
//                }
//
//                var6 = fluidCountMap.entrySet().iterator();
//
//                while(var6.hasNext()) {
//                    fs = (Map.Entry)var6.next();
//                    needed = (Integer)fs.getValue();
//                    available = 0;
//                    var10 = countFluid.entrySet().iterator();
//
//                    while(var10.hasNext()) {
//                        inputFluid = (Map.Entry)var10.next();
//                        if (((FluidKey)fs.getKey()).equals(inputFluid.getKey())) {
//                            available += (Integer)inputFluid.getValue();
//                        }
//                    }
//
//                    if (available < needed) {
//                        return 0;
//                    }
//
//                    int ratio = Math.min(parallelAmount, available / needed);
//                    if (ratio < minMultiplier) {
//                        minMultiplier = ratio;
//                    }
//                }
//
//                return minMultiplier;
//            }
//
//            fs = (Map.Entry)var6.next();
//            needed = (Integer)fs.getValue();
//            available = 0;
//            var10 = countFluid.entrySet().iterator();
//
//            while(var10.hasNext()) {
//                inputFluid = (Map.Entry)var10.next();
//                if (((FluidKey)fs.getKey()).equals(inputFluid.getKey())) {
//                    available = (Integer)inputFluid.getValue();
//                    if (available > needed) {
//                        inputFluid.setValue(available - needed);
//                        needed -= available;
//                        break;
//                    }
//
//                    inputFluid.setValue(0);
//                    fs.setValue(needed - available);
//                    needed -= available;
//                }
//            }
//        } while(needed < available);
//
//        return 0;
//    }
//
//    protected static int getMaxRatioAspect(@NotNull Map<AspectKey, Integer> countFluid, @NotNull AspectRecipe recipe, int parallelAmount) {
//        int minMultiplier = Integer.MAX_VALUE;
//        Map<AspectKey, Integer> fluidCountMap = new HashMap();
//        Map<AspectKey, Integer> notConsumableMap = new HashMap();
//        Iterator var6 = recipe.getAspectInputs().iterator();
//
//        int needed;
//        while(var6.hasNext()) {
//            AspectRecipeInput fluidInput = (AspectRecipeInput)var6.next();
//            needed = fluidInput.getAmount();
//            if (fluidInput.isNonConsumable()) {
//                int finalNeeded = needed;
//                notConsumableMap.computeIfPresent(new AspectKey(fluidInput.getInputAspectStack()), (k, v) -> {
//                    return v + finalNeeded;
//                });
//                notConsumableMap.putIfAbsent(new AspectKey(fluidInput.getInputAspectStack()), needed);
//            } else {
//                int finalNeeded1 = needed;
//                fluidCountMap.computeIfPresent(new AspectKey(fluidInput.getInputAspectStack()), (k, v) -> {
//                    return v + finalNeeded1;
//                });
//                fluidCountMap.putIfAbsent(new AspectKey(fluidInput.getInputAspectStack()), needed);
//            }
//        }
//
//        var6 = notConsumableMap.entrySet().iterator();
//
//        int available;
//        do {
//            Iterator var10;
//            Map.Entry inputFluid;
//            Map.Entry fs;
//            if (!var6.hasNext()) {
//                if (fluidCountMap.isEmpty() && !notConsumableMap.isEmpty()) {
//                    return parallelAmount;
//                }
//
//                var6 = fluidCountMap.entrySet().iterator();
//
//                while(var6.hasNext()) {
//                    fs = (Map.Entry)var6.next();
//                    needed = (Integer)fs.getValue();
//                    available = 0;
//                    var10 = countFluid.entrySet().iterator();
//
//                    while(var10.hasNext()) {
//                        inputFluid = (Map.Entry)var10.next();
//                        if (((AspectKey)fs.getKey()).equals(inputFluid.getKey())) {
//                            available += (Integer)inputFluid.getValue();
//                        }
//                    }
//
//                    if (available < needed) {
//                        return 0;
//                    }
//
//                    int ratio = Math.min(parallelAmount, available / needed);
//                    if (ratio < minMultiplier) {
//                        minMultiplier = ratio;
//                    }
//                }
//
//                return minMultiplier;
//            }
//
//            fs = (Map.Entry)var6.next();
//            needed = (Integer)fs.getValue();
//            available = 0;
//            var10 = countFluid.entrySet().iterator();
//
//            while(var10.hasNext()) {
//                inputFluid = (Map.Entry)var10.next();
//                if (((AspectKey)fs.getKey()).equals(inputFluid.getKey())) {
//                    available = (Integer)inputFluid.getValue();
//                    if (available > needed) {
//                        inputFluid.setValue(available - needed);
//                        needed -= available;
//                        break;
//                    }
//
//                    inputFluid.setValue(0);
//                    fs.setValue(needed - available);
//                    needed -= available;
//                }
//            }
//        } while(needed < available);
//
//        return 0;
//    }
//
//    public static RecipeBuilder<?> doParallelRecipes(@NotNull Recipe currentRecipe, @NotNull RecipeMap<?> recipeMap, @NotNull IItemHandlerModifiable importInventory, @NotNull IMultipleTankHandler importFluids, @NotNull IItemHandlerModifiable exportInventory, @NotNull IMultipleTankHandler exportFluids, int parallelAmount, long maxVoltage, @NotNull IVoidable voidable) {
//        int multiplierByInputs = getMaxRecipeMultiplier(currentRecipe, importInventory, importFluids, parallelAmount);
//        if (multiplierByInputs == 0) {
//            return null;
//        } else {
//            RecipeBuilder<?> recipeBuilder = recipeMap.recipeBuilder().EUt(0);
//            boolean voidItems = voidable.canVoidRecipeItemOutputs();
//            boolean voidFluids = voidable.canVoidRecipeFluidOutputs();
//            int limitByOutput = limitByOutputMerging(currentRecipe, exportInventory, exportFluids, multiplierByInputs, voidItems, voidFluids);
//            int recipeEUt = currentRecipe.getEUt();
//            if (recipeEUt != 0) {
//                int limitByVoltage = Math.abs((int)(maxVoltage / (long)recipeEUt));
//                int parallelizable = Math.min(limitByVoltage, limitByOutput);
//                if (parallelizable != 0) {
//                    recipeBuilder.append(currentRecipe, Math.min(parallelizable, multiplierByInputs), false);
//                }
//            } else if (limitByOutput > 0) {
//                recipeBuilder.append(currentRecipe, limitByOutput, false);
//            }
//
//            return recipeBuilder;
//        }
//    }
//
//    public static RecipeBuilder<?> appendItemRecipes(@NotNull RecipeMap<?> recipeMap, @NotNull IItemHandlerModifiable importInventory, @NotNull IItemHandlerModifiable exportInventory, int parallelAmount, long maxVoltage, IVoidable voidable) {
//        RecipeBuilder<?> recipeBuilder = null;
//        OverlayedItemHandler overlayedItemHandler = new OverlayedItemHandler(exportInventory);
//        int engagedItems = 0;
//
//        for(int index = 0; index < importInventory.getSlots(); ++index) {
//            ItemStack currentInputItem = importInventory.getStackInSlot(index);
//            if (!currentInputItem.isEmpty()) {
//                Recipe matchingRecipe = recipeMap.findRecipe(maxVoltage, Collections.singletonList(currentInputItem), Collections.emptyList());
//                if (matchingRecipe != null) {
//                    GTRecipeInput inputIngredient = (GTRecipeInput)matchingRecipe.getInputs().get(0);
//                    if (recipeBuilder == null) {
//                        recipeBuilder = recipeMap.recipeBuilder().EUt(0).duration(0);
//                    }
//
//                    if (inputIngredient == null) {
//                        throw new IllegalStateException(String.format("Got recipe with null ingredient %s", matchingRecipe));
//                    }
//
//                    matchingRecipe = Recipe.trimRecipeOutputs(matchingRecipe, recipeMap, voidable.getItemOutputLimit(), voidable.getFluidOutputLimit());
//                    int ingredientRatio = Math.min(parallelAmount - engagedItems, currentInputItem.getCount() / Math.max(((GTRecipeInput)matchingRecipe.getInputs().get(0)).getAmount(), 1));
//                    int limitByOutput = Integer.MAX_VALUE;
//                    if (!voidable.canVoidRecipeItemOutputs()) {
//                        limitByOutput = limitParallelByItemsIncremental(recipeBuilder.getAllItemOutputs(), matchingRecipe.getOutputs(), overlayedItemHandler, ingredientRatio);
//                    }
//
//                    int multiplierRecipeAmount = Math.min(ingredientRatio, limitByOutput);
//                    if (multiplierRecipeAmount > 0) {
//                        recipeBuilder.append(matchingRecipe, multiplierRecipeAmount, true);
//                        engagedItems += multiplierRecipeAmount;
//                    }
//
//                    if (engagedItems == parallelAmount) {
//                        break;
//                    }
//                }
//            }
//        }
//
//        return recipeBuilder;
//    }
//}

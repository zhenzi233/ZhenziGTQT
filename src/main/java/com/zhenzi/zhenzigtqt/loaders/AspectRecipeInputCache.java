package com.zhenzi.zhenzigtqt.loaders;

import gregtech.api.GTValues;
import gregtech.api.persistence.PersistentData;
import gregtech.api.recipes.GTRecipeInputCache;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.util.GTLog;
import gregtech.common.ConfigHolder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class AspectRecipeInputCache {
    private static final int MINIMUM_CACHE_SIZE = 8192;
    private static final int MAXIMUM_CACHE_SIZE = 1073741824;
    private static ObjectOpenHashSet<GTRecipeInput> instances;
    private static final String DATA_NAME = "expectedIngredientInstances";

    private AspectRecipeInputCache() {
    }

    public static boolean isCacheEnabled() {
        return instances != null;
    }

    @ApiStatus.Internal
    public static void enableCache() {
        if (!isCacheEnabled()) {
            int size = calculateOptimalExpectedSize();
            instances = new ObjectOpenHashSet(size);
            if (ConfigHolder.misc.debug || GTValues.isDeobfEnvironment()) {
                GTLog.logger.info("GTRecipeInput cache enabled with expected size {}", size);
            }
        }

    }

    @ApiStatus.Internal
    public static void disableCache() {
        if (isCacheEnabled()) {
            int size = instances.size();
            if (ConfigHolder.misc.debug || GTValues.isDeobfEnvironment()) {
                GTLog.logger.info("GTRecipeInput cache disabled; releasing {} unique instances", size);
            }

            instances = null;
            if (size >= 8192 && size < 1073741824) {
                NBTTagCompound tagCompound = PersistentData.instance().getTag();
                if (getExpectedInstanceAmount(tagCompound) != size) {
                    tagCompound.setInteger("expectedIngredientInstances", size);
                    PersistentData.instance().save();
                }
            }
        }

    }

    private static int getExpectedInstanceAmount(@NotNull NBTTagCompound tagCompound) {
        return MathHelper.clamp(tagCompound.getInteger("expectedIngredientInstances"), 8192, 1073741824);
    }

    public static AspectRecipeInput deduplicate(AspectRecipeInput recipeInput) {
        if (isCacheEnabled() && !recipeInput.isCached()) {
            AspectRecipeInput cached = (AspectRecipeInput)instances.addOrGet(recipeInput);
            if (cached == recipeInput) {
                cached.setCached();
            }

            return cached;
        } else {
            return recipeInput;
        }
    }

    public static List<AspectRecipeInput> deduplicateInputs(List<AspectRecipeInput> inputs) {
        if (!isCacheEnabled()) {
            return inputs;
        } else if (inputs.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<AspectRecipeInput> list = new ArrayList(inputs.size());
            Iterator var2 = inputs.iterator();

            while(var2.hasNext()) {
                AspectRecipeInput input = (AspectRecipeInput)var2.next();
                list.add(deduplicate(input));
            }

            return list;
        }
    }

    private static int calculateOptimalExpectedSize() {
        int min = Math.max(getExpectedInstanceAmount(PersistentData.instance().getTag()), 8192);

        for(int i = 13; i < 31; ++i) {
            int sizeToTest = 1 << i;
            int arraySize = nextHighestPowerOf2((int)((float)sizeToTest / 0.75F));
            int maxStoredBeforeRehash = (int)((float)arraySize * 0.75F);
            if (maxStoredBeforeRehash >= min) {
                return sizeToTest;
            }
        }

        return 8192;
    }

    private static int nextHighestPowerOf2(int x) {
        --x;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;
        ++x;
        return x;
    }
}

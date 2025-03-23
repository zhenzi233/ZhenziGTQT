package com.zhenzi.zhenzigtqt.integration.jei.recipes;

import com.zhenzi.zhenzigtqt.loaders.AspectRecipeMap;
import gregtech.api.recipes.RecipeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class ZZRecipeCategory{
    private static final Map<String, ZZRecipeCategory> categories = new Object2ObjectOpenHashMap();
    private final String modid;
    private final String name;
    private final String uniqueID;
    private final String translationKey;
    private final AspectRecipeMap<?> recipeMap;
    private Object icon;

    public static @NotNull ZZRecipeCategory create(@NotNull String modid, @NotNull String categoryName, @NotNull String translationKey, @NotNull AspectRecipeMap<?> recipeMap) {
        return (ZZRecipeCategory)categories.computeIfAbsent(categoryName, (k) -> {
            return new ZZRecipeCategory(modid, categoryName, translationKey, recipeMap);
        });
    }

    public static @Nullable ZZRecipeCategory getByName(@NotNull String categoryName) {
        return (ZZRecipeCategory)categories.get(categoryName);
    }

    private ZZRecipeCategory(@NotNull String modid, @NotNull String name, @NotNull String translationKey, @NotNull AspectRecipeMap<?> recipeMap) {
        this.modid = modid;
        this.name = name;
        this.uniqueID = modid + ':' + this.name;
        this.translationKey = translationKey;
        this.recipeMap = recipeMap;
    }

    public @NotNull String getName() {
        return this.name;
    }

    public @NotNull String getModid() {
        return this.modid;
    }

    public @NotNull String getUniqueID() {
        return this.uniqueID;
    }

    public @NotNull String getTranslationKey() {
        return this.translationKey;
    }

    public @NotNull RecipeMap<?> getRecipeMap() {
        return this.recipeMap;
    }

    public ZZRecipeCategory jeiIcon(@Nullable Object icon) {
        this.icon = icon;
        return this;
    }

    public @Nullable Object getJEIIcon() {
        return this.icon;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            ZZRecipeCategory that = (ZZRecipeCategory)o;
            return this.getUniqueID().equals(that.getUniqueID());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.getUniqueID().hashCode();
    }

    public @NotNull String toString() {
        return "ZZRecipeCategory{" + this.uniqueID + '}';
    }
}

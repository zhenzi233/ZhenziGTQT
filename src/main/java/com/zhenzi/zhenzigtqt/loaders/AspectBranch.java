package com.zhenzi.zhenzigtqt.loaders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.map.AbstractMapIngredient;
import gregtech.api.recipes.map.Branch;
import gregtech.api.recipes.map.Either;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.stream.Stream;

public class AspectBranch {
    private Map<AbstractMapIngredient, Either<AspectRecipe, AspectBranch>> nodes;
    private Map<AbstractMapIngredient, Either<AspectRecipe, AspectBranch>> specialNodes;

    public AspectBranch() {
    }

    public Stream<AspectRecipe> getRecipes(boolean filterHidden) {
        Stream<AspectRecipe> stream = null;
        if (this.nodes != null) {
            stream = this.nodes.values().stream().flatMap((either) -> {
                return (Stream)either.map(Stream::of, (right) -> {
                    return right.getRecipes(filterHidden);
                });
            });
        }

        if (this.specialNodes != null) {
            if (stream == null) {
                stream = this.specialNodes.values().stream().flatMap((either) -> {
                    return (Stream)either.map(Stream::of, (right) -> {
                        return right.getRecipes(filterHidden);
                    });
                });
            } else {
                stream = Stream.concat(stream, this.specialNodes.values().stream().flatMap((either) -> {
                    return (Stream)either.map(Stream::of, (right) -> {
                        return right.getRecipes(filterHidden);
                    });
                }));
            }
        }

        if (stream == null) {
            return Stream.empty();
        } else {
            if (filterHidden) {
                stream = stream.filter((t) -> {
                    return !t.isHidden();
                });
            }

            return stream;
        }
    }

    public boolean isEmptyBranch() {
        return (this.nodes == null || this.nodes.isEmpty()) && (this.specialNodes == null || this.specialNodes.isEmpty());
    }

    public @NotNull Map<AbstractMapIngredient, Either<AspectRecipe, AspectBranch>> getNodes() {
        if (this.nodes == null) {
            this.nodes = new Object2ObjectOpenHashMap(2);
        }

        return this.nodes;
    }

    public @NotNull Map<AbstractMapIngredient, Either<AspectRecipe, AspectBranch>> getSpecialNodes() {
        if (this.specialNodes == null) {
            this.specialNodes = new Object2ObjectOpenHashMap(2);
        }

        return this.specialNodes;
    }
}

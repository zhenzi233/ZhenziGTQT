package com.zhenzi.zhenzigtqt.loaders;

import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectStack;
import gregtech.api.recipes.chance.output.BoostableChanceOutput;
import gregtech.api.recipes.chance.output.impl.ChancedFluidOutput;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class ChancedAspectOutput  extends BoostableChanceOutput<AspectStack> {
    public ChancedAspectOutput(@NotNull AspectStack ingredient, int chance, int chanceBoost) {
        super(ingredient, chance, chanceBoost);
    }

    public @NotNull ChancedAspectOutput copy() {
        return new ChancedAspectOutput(((AspectStack)this.getIngredient()).copy(), this.getChance(), this.getChanceBoost());
    }

    public String toString() {
        return "ChancedFluidOutput{ingredient=FluidStack{" + ((AspectStack)this.getIngredient()).getUnlocalizedName() + ", amount=" + ((AspectStack)this.getIngredient()).amount + "}, chance=" + this.getChance() + ", chanceBoost=" + this.getChanceBoost() + '}';
    }
}

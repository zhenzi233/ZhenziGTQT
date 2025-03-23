package com.zhenzi.zhenzigtqt.loaders;

import gregtech.api.metatileentity.IVoidable;
import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;

public interface IVoidableAspect {
    boolean canVoidRecipeItemOutputs();

    boolean canVoidRecipeFluidOutputs();
    boolean canVoidRecipeAspectOutputs();

    default int getItemOutputLimit() {
        return -1;
    }

    default int getFluidOutputLimit() {
        return -1;
    }
    default int getAspectOutputLimit() {
        return -1;
    }

    public static enum VoidingAspectMode implements IStringSerializable {
        VOID_NONE("gregtech.gui.multiblock_no_voiding"),
        VOID_ITEMS("gregtech.gui.multiblock_item_voiding"),
        VOID_FLUIDS("gregtech.gui.multiblock_fluid_voiding"),
        VOID_ASPECTS("gregtech.gui.multiblock_aspect_voiding"),
        VOID_ITEM_ASPECT("gregtech.gui.multiblock_item_aspect_voiding"),
        VOID_FLUID_ASPECT("gregtech.gui.multiblock_fluid_aspect_voiding"),
        VOID_ALL("gregtech.gui.multiblock_item_fluid_aspect_voiding");

        public static final IVoidableAspect.VoidingAspectMode[] VALUES = values();
        public final String localeName;

        private VoidingAspectMode(String name) {
            this.localeName = name;
        }

        public @NotNull String getName() {
            return this.localeName;
        }
    }
}

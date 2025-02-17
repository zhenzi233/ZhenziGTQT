package com.zhenzi.zhenzigtqt.common.block.metablocks;

import com.zhenzi.zhenzigtqt.common.CommonProxy;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

public class ZGTQTSolarBlock extends VariantBlock<ZGTQTSolarBlock.SolarBlockType> {
    //写个大饼（

    public ZGTQTSolarBlock() {
        super(Material.IRON);
        this.setTranslationKey("solar_panel");
        this.setHardness(5.0F);
        this.setResistance(10.0F);
        this.setSoundType(SoundType.METAL);
        this.setHarvestLevel("wrench", 2);
        this.setDefaultState(this.getState(ZGTQTSolarBlock.SolarBlockType.LV_PANEL));
        this.setCreativeTab(CommonProxy.ZHENZI_GTQT_TAB);
    }

    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public static enum SolarBlockType implements IStringSerializable {
        LV_PANEL("lv_panel"),
        MV_PANEL("mv_panel");

        private final String name;

        private SolarBlockType(String name) {
            this.name = name;
        }

        @Nonnull
        public String getName() {
            return this.name;
        }
    }
}

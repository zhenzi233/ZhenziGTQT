package com.zhenzi.zhenzigtqt.common.block;

import com.google.common.collect.UnmodifiableIterator;
import com.zhenzi.zhenzigtqt.common.block.metablocks.ZGTQTSolarBlock;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ZhenziGTQTMetaBlocks {

    public static ZGTQTSolarBlock SOLAR_BLOCK;

    public static void init() {
        SOLAR_BLOCK = new ZGTQTSolarBlock();
        SOLAR_BLOCK.setRegistryName("solar_panel");
    }

    @SideOnly(Side.CLIENT)
    public static void registerItemModels() {
        registerItemModel(SOLAR_BLOCK);
    }

    @SideOnly(Side.CLIENT)
    private static void registerItemModel(Block block) {
        UnmodifiableIterator var1 = block.getBlockState().getValidStates().iterator();

        while(var1.hasNext()) {
            IBlockState state = (IBlockState)var1.next();
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), block.getMetaFromState(state), new ModelResourceLocation(block.getRegistryName(), MetaBlocks.statePropertiesToString(state.getProperties())));
        }

    }
}

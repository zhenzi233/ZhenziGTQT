package com.zhenzi.zhenzigtqt.common.block;

import com.google.common.collect.UnmodifiableIterator;
import com.zhenzi.zhenzigtqt.common.block.metablocks.ZGTQTSolarBlock;
import com.zhenzi.zhenzigtqt.common.tile.TileGTEssentialInput;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.Thaumcraft;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.common.blocks.essentia.BlockEssentiaTransport;
import thaumcraft.common.tiles.essentia.TileEssentiaInput;

public class ZhenziGTQTMetaBlocks {

    public static ZGTQTSolarBlock SOLAR_BLOCK;

    public static Block GTessentialTransportInput;

    public static void init() {
        SOLAR_BLOCK = new ZGTQTSolarBlock();
        SOLAR_BLOCK.setRegistryName("solar_panel");

        GTessentialTransportInput = registerBlock(new BlockGTEssentiaTransport(TileGTEssentialInput.class, "gtessentia_input"));
        GameRegistry.registerTileEntity(TileGTEssentialInput.class, "zhenzigtqt:gtessentia_input");
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

    private static Block registerBlock(Block block) {
        return registerBlock(block, new ItemBlock(block));
    }

    private static Block registerBlock(Block block, ItemBlock itemBlock) {
        ForgeRegistries.BLOCKS.register(block);
        itemBlock.setRegistryName(block.getRegistryName());
        ForgeRegistries.ITEMS.register(itemBlock);
        Thaumcraft.proxy.registerModel(itemBlock);


        return block;
    }
}

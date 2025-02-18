package com.zhenzi.zhenzigtqt.common;

import com.zhenzi.zhenzigtqt.ZhenziGtqt;
import com.zhenzi.zhenzigtqt.common.block.ZhenziGTQTMetaBlocks;
import com.zhenzi.zhenzigtqt.common.metatileentity.ZhenziGTQTMetaTileEntity;
import com.zhenzi.zhenzigtqt.loaders.RecipeManager;
import gregtech.api.block.VariantItemBlock;
//import keqing.pollution.loaders.RecipeManger;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Objects;
import java.util.function.Function;

@Mod.EventBusSubscriber(
        modid = "zhenzigtqt"
)
public class CommonProxy {
    public static final CreativeTabs ZHENZI_GTQT_TAB = new CreativeTabs("Zhenzi GTQT") {
        public ItemStack createIcon() {
            return ItemStack.EMPTY;
        }
    };

    public CommonProxy() {
    }

    public void init() {
        RecipeManager.init();
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        ZhenziGtqt.LOGGER.info("Registering blocks...");
        IForgeRegistry<Block> registry = event.getRegistry();
        registry.register(ZhenziGTQTMetaBlocks.SOLAR_BLOCK);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        ZhenziGtqt.LOGGER.info("Registering Items...");
        IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(createItemBlock(ZhenziGTQTMetaBlocks.SOLAR_BLOCK, VariantItemBlock::new));
    }

    private static <T extends Block> ItemBlock createItemBlock(T block, Function<T, ItemBlock> producer) {
        ItemBlock itemBlock = (ItemBlock)producer.apply(block);
        itemBlock.setRegistryName((ResourceLocation) Objects.requireNonNull(block.getRegistryName()));
        return itemBlock;
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        ZhenziGtqt.LOGGER.info("Registering Recipes...");
    }
}

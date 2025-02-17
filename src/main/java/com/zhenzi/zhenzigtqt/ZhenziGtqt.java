package com.zhenzi.zhenzigtqt;

import com.example.modid.Tags;
import com.zhenzi.zhenzigtqt.common.CommonProxy;
import com.zhenzi.zhenzigtqt.common.block.ZhenziGTQTMetaBlocks;
import com.zhenzi.zhenzigtqt.common.metatileentity.ZhenziGTQTMetaTileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

@Mod(modid = "zhenzigtqt", name = "Zhenzi GTQT", version = "1.0.0")
public class ZhenziGtqt {
    public static final String MODID = "zhenzigtqt";
    public static final String NAME = "Zhenzi GTQT";
    public static final String VERSION = "1.0.0";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @SidedProxy(
            clientSide = "com.zhenzi.zhenzigtqt.client.ClientProxy",
            serverSide = "com.zhenzi.zhenzigtqt.common.CommonProxy"
    )
    public static CommonProxy proxy;

    /**
     * <a href="https://cleanroommc.com/wiki/forge-mod-development/event#overview">
     *     Take a look at how many FMLStateEvents you can listen to via the @Mod.EventHandler annotation here
     * </a>
     */

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Hello From {}!", NAME);
        ZhenziGTQTMetaBlocks.init();
        ZhenziGTQTMetaTileEntity.initialization();
    }

}

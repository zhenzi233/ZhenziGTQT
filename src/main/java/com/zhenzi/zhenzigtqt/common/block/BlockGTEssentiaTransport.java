package com.zhenzi.zhenzigtqt.common.block;

import thaumcraft.common.blocks.essentia.BlockEssentiaTransport;
import thaumcraft.common.config.ConfigItems;

import static com.zhenzi.zhenzigtqt.common.CommonProxy.ZHENZI_GTQT_TAB;

public class BlockGTEssentiaTransport extends BlockEssentiaTransport {
    public BlockGTEssentiaTransport(Class te, String name) {
        super(te, name);
        this.setCreativeTab(ZHENZI_GTQT_TAB);
    }
}

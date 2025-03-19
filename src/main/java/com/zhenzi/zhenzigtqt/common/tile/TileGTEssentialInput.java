package com.zhenzi.zhenzigtqt.common.tile;

import com.zhenzi.zhenzigtqt.common.lib.GTEssentiaHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.api.internal.WorldCoordinates;
import thaumcraft.common.lib.events.EssentiaHandler;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXEssentiaSource;
import thaumcraft.common.tiles.devices.TileMirrorEssentia;
import thaumcraft.common.tiles.essentia.TileEssentiaInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class TileGTEssentialInput extends TileEssentiaInput implements IEssentiaTransport, ITickable {
    int count = 0;

    public void update() {
        if (!this.world.isRemote && ++this.count % 5 == 0) {
            this.fillJar();
        }
    }

    void fillJar()
    {
        TileEntity offerTile = ThaumcraftApiHelper.getConnectableTile(this.world, this.getPos(), this.getFacing().getOpposite());
        if (offerTile != null) {
            IEssentiaTransport input = (IEssentiaTransport)offerTile;
            if (!input.canOutputTo(this.getFacing())) {
                return;
            }

            if (input.getEssentiaAmount(this.getFacing()) > 0 && input.getSuctionAmount(this.getFacing()) < this.getSuctionAmount(this.getFacing().getOpposite()) && this.getSuctionAmount(this.getFacing().getOpposite()) >= input.getMinimumSuction()) {
                Aspect aspect = input.getEssentiaType(this.getFacing());


                if (GTEssentiaHandler.addEssentia(this, aspect, this.getFacing(), 16, false, 5)) {
                    input.takeEssentia(aspect, 1, this.getFacing());
                }

            }
        }
    }

}

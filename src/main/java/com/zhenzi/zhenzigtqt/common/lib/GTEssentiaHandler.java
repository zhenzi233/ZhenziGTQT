package com.zhenzi.zhenzigtqt.common.lib;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.api.internal.WorldCoordinates;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXEssentiaSource;
import thaumcraft.common.tiles.devices.TileMirrorEssentia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class GTEssentiaHandler {

    private static HashMap<WorldCoordinates, ArrayList<WorldCoordinates>> sources = new HashMap();
    private static HashMap<WorldCoordinates, Long> sourcesDelay = new HashMap();

    private static int tick = 0;

    public GTEssentiaHandler() {
    }

    public static boolean addEssentiaToTile(MetaTileEntity metaTileEntity, Aspect aspect, EnumFacing facing, boolean ignoreMirror, int ext)
    {

        MetaTileEntity sourceMTE = metaTileEntity;
        TileEntity tile = sourceMTE.getNeighbor(facing);
        if (tile instanceof IAspectSource)
        {
            IAspectSource as = (IAspectSource) tile;
            if ((!ignoreMirror)) {
                AspectList ap = as.getAspects();
                if (ap != null)
                {
                    if (aspect != null && as.doesContainerAccept(aspect)  && as.addToContainer(aspect, 1) <= 0) {
                        if (ap.aspects != null)
                        {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean addEssentiaToMTE(MetaTileEntity metaTileEntity, Aspect aspect, EnumFacing facing, boolean ignoreMirror, int ext)
    {

        MetaTileEntity sourceMTE = metaTileEntity;
        MetaTileEntity tile = GTUtility.getMetaTileEntity(sourceMTE.getWorld(), sourceMTE.getPos().offset(facing));
        if (tile instanceof IAspectSource)
        {
            IAspectSource as = (IAspectSource) tile;
            if ((!ignoreMirror)) {
                AspectList ap = as.getAspects();
                if (ap != null)
                {
                    if (aspect != null && as.doesContainerAccept(aspect)  && as.addToContainer(aspect, 1) <= 0) {
                        if (ap.aspects != null)
                        {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean addEssentia(TileEntity tile, Aspect aspect, EnumFacing direction, int range, boolean ignoreMirror, int ext) {
        WorldCoordinates tileEPos = new WorldCoordinates(tile.getPos(), tile.getWorld().provider.getDimension());
        if (!sources.containsKey(tileEPos)) {
            getSources(tile.getWorld(), tileEPos, direction, range);
            return sources.containsKey(tileEPos) ? addEssentia(tile, aspect, direction, range, ignoreMirror, ext) : false;
        } else {
            ArrayList<WorldCoordinates> es = (ArrayList)sources.get(tileEPos);
            ArrayList<WorldCoordinates> empties = new ArrayList();
            Iterator var9 = es.iterator();

            WorldCoordinates source;
//            TileEntity sourceTile;
            MetaTileEntity sourceMetaTile;
            IAspectSource as;
            while(var9.hasNext()) {
                source = (WorldCoordinates)var9.next();
//                sourceTile = tile.getWorld().getTileEntity(source.pos);
                sourceMetaTile = GTUtility.getMetaTileEntity(tile.getWorld(), source.pos);

                if (sourceMetaTile == null || !(sourceMetaTile instanceof IAspectSource)) {
                    break;
                }

                as = (IAspectSource)sourceMetaTile;
                if (!as.isBlocked() && (!ignoreMirror)) {
                    if (!as.doesContainerAccept(aspect) || as.getAspects() != null && as.getAspects().visSize() != 0) {
                        if (as.doesContainerAccept(aspect) && as.addToContainer(aspect, 1) <= 0) {
                            PacketHandler.INSTANCE.sendToAllAround(new PacketFXEssentiaSource(source.pos, (byte)(source.pos.getX() - tile.getPos().getX()), (byte)(source.pos.getY() - tile.getPos().getY()), (byte)(source.pos.getZ() - tile.getPos().getZ()), aspect.getColor(), ext), new NetworkRegistry.TargetPoint(tile.getWorld().provider.getDimension(), (double)tile.getPos().getX(), (double)tile.getPos().getY(), (double)tile.getPos().getZ(), 32.0));
                            return true;
                        }
                    } else {
                        empties.add(source);
                    }
                }
            }

            var9 = empties.iterator();

            while(var9.hasNext()) {
                source = (WorldCoordinates)var9.next();
                if (source != null && source.pos != null) {
//                    sourceTile = tile.getWorld().getTileEntity(source.pos);
                    sourceMetaTile = GTUtility.getMetaTileEntity(tile.getWorld(), source.pos);
                    if (sourceMetaTile == null || !(sourceMetaTile instanceof IAspectSource)) {
                        break;
                    }

                    as = (IAspectSource)sourceMetaTile;
                    if (aspect != null && as.doesContainerAccept(aspect) && as.addToContainer(aspect, 1) <= 0) {
                        PacketHandler.INSTANCE.sendToAllAround(new PacketFXEssentiaSource(source.pos, (byte)(source.pos.getX() - tile.getPos().getX()), (byte)(source.pos.getY() - tile.getPos().getY()), (byte)(source.pos.getZ() - tile.getPos().getZ()), aspect.getColor(), ext), new NetworkRegistry.TargetPoint(tile.getWorld().provider.getDimension(), (double)tile.getPos().getX(), (double)tile.getPos().getY(), (double)tile.getPos().getZ(), 32.0));
                        return true;
                    }
                }
            }

            sources.remove(tileEPos);
            sourcesDelay.put(tileEPos, System.currentTimeMillis() + 10000L);
            return false;
        }
    }

    private static void getSources(World world, WorldCoordinates tileEPos, EnumFacing direction, int range) {
        if (sourcesDelay.containsKey(tileEPos)) {
            long d = (Long)sourcesDelay.get(tileEPos);
            if (d > System.currentTimeMillis()) {
                return;
            }

            sourcesDelay.remove(tileEPos);
        }

        TileEntity sourceTile = world.getTileEntity(tileEPos.pos);
        if (sourceTile == null) return;
        ArrayList<WorldCoordinates> sourceList = new ArrayList();
        int start = 0;
        if (direction == null) {
            start = -range;
            direction = EnumFacing.UP;
        }

        for(int aa = -range; aa <= range; ++aa) {
            for(int bb = -range; bb <= range; ++bb) {
                for(int cc = start; cc < range; ++cc) {
                    if (aa != 0 || bb != 0 || cc != 0) {
                        int xx = tileEPos.pos.getX();
                        int yy = tileEPos.pos.getY();
                        int zz = tileEPos.pos.getZ();
                        if (direction.getYOffset() != 0) {
                            xx += aa;
                            yy += cc * direction.getYOffset();
                            zz += bb;
                        } else if (direction.getXOffset() == 0) {
                            xx += aa;
                            yy += bb;
                            zz += cc * direction.getZOffset();
                        } else {
                            xx += cc * direction.getXOffset();
                            yy += aa;
                            zz += bb;
                        }

                        MetaTileEntity metaTileEntity = GTUtility.getMetaTileEntity(sourceTile.getWorld(), new BlockPos(xx, yy, zz));
                        if (metaTileEntity != null
                                && metaTileEntity instanceof IAspectSource
                                && (!(sourceTile instanceof TileMirrorEssentia) ))
                        {
                            sourceList.add(new WorldCoordinates(new BlockPos(xx, yy, zz), world.provider.getDimension()));
                        }
                    }
                }
            }
        }

        if (sourceList.size() > 0) {
            ArrayList<WorldCoordinates> sourceList2 = new ArrayList();
            Iterator sourceIterator = sourceList.iterator();

            while(true) {
                label72:
                while(sourceIterator.hasNext()) {
                    WorldCoordinates wc = (WorldCoordinates)sourceIterator.next();
                    double dist = wc.getDistanceSquaredToWorldCoordinates(tileEPos);
                    if (!sourceList2.isEmpty()) {
                        for(int a = 0; a < sourceList2.size(); ++a) {
                            double d2 = ((WorldCoordinates)sourceList2.get(a)).getDistanceSquaredToWorldCoordinates(tileEPos);
                            if (dist < d2) {
                                sourceList2.add(a, wc);
                                continue label72;
                            }
                        }
                    }

                    sourceList2.add(wc);
                }

                sources.put(tileEPos, sourceList2);
                break;
            }
        } else {
            sourcesDelay.put(tileEPos, System.currentTimeMillis() + 10000L);
        }

    }

}

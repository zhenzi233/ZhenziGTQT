package com.zhenzi.zhenzigtqt.common.lib.aspect;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.common.eventhandler.Event;

public class AspectEvent extends Event {
    private final AspectStack aspectStack;
    private final World world;
    private final BlockPos pos;

    public AspectEvent(AspectStack aspectStack, World world, BlockPos pos)
    {
        this.aspectStack = aspectStack;
        this.world = world;
        this.pos = pos;
    }

    public AspectStack getFluid()
    {
        return aspectStack;
    }

    public World getWorld()
    {
        return world;
    }

    public BlockPos getPos()
    {
        return pos;
    }

    /**
     * Mods should fire this event when they move fluids around.
     *
     */
    public static class AspectMotionEvent extends AspectEvent
    {
        public AspectMotionEvent(AspectStack aspectStack, World world, BlockPos pos)
        {
            super(aspectStack, world, pos);
        }
    }

    /**
     * Mods should fire this event when a fluid is {@link IFluidTank#fill(FluidStack, boolean)}
     * their tank implementation. {@link FluidTank} does.
     *
     */
    public static class AspectFillingEvent extends AspectEvent
    {
        private final IAspectTank tank;
        private final int amount;

        public AspectFillingEvent(AspectStack aspectStack, World world, BlockPos pos, IAspectTank tank, int amount)
        {
            super(aspectStack, world, pos);
            this.tank = tank;
            this.amount = amount;
        }

        public IAspectTank getTank()
        {
            return tank;
        }

        public int getAmount()
        {
            return amount;
        }
    }

    /**
     * Mods should fire this event when a fluid is {@link IFluidTank#drain(int, boolean)} from their
     * tank.
     *
     */
    public static class AspectDrainingEvent extends AspectEvent
    {
        private final IAspectTank tank;
        private final int amount;

        public AspectDrainingEvent(AspectStack aspectStack, World world, BlockPos pos, IAspectTank tank, int amount)
        {
            super(aspectStack, world, pos);
            this.amount = amount;
            this.tank = tank;
        }

        public IAspectTank getTank()
        {
            return tank;
        }

        public int getAmount()
        {
            return amount;
        }
    }

    /**
     * Mods should fire this event when a fluid "spills", for example, if a block containing fluid
     * is broken.
     *
     */
    public static class AspectSpilledEvent extends AspectEvent
    {
        public AspectSpilledEvent(AspectStack aspectStack, World world, BlockPos pos)
        {
            super(aspectStack, world, pos);
        }
    }

    /**
     * A handy shortcut for firing the various fluid events.
     *
     * @param event
     */
    public static final void fireEvent(AspectEvent event)
    {
        MinecraftForge.EVENT_BUS.post(event);
    }
}

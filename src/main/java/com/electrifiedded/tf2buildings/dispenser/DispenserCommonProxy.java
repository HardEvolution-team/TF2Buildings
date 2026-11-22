package com.electrifiedded.tf2buildings.dispenser;

import com.electrifiedded.tf2buildings.dispenser.tile.TileEntityTF2Dispenser;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class DispenserCommonProxy {
    public void preInit() {}

    public Object getGui(EntityPlayer player, TileEntityTF2Dispenser te) { return null; }

    public void spawnBeams(BlockPos pos, int[] playerIds) {}
}

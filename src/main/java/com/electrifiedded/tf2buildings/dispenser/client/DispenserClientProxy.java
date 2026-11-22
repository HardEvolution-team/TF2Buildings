package com.electrifiedded.tf2buildings.dispenser.client;

import com.electrifiedded.tf2buildings.dispenser.DispenserCommonProxy;
import com.electrifiedded.tf2buildings.dispenser.tile.TileEntityTF2Dispenser;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class DispenserClientProxy extends DispenserCommonProxy {
    @Override
    public void preInit() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTF2Dispenser.class, new TileEntityTF2DispenserRenderer());
        MinecraftForge.EVENT_BUS.register(DispenserBeams.class);
    }

    @Override
    public Object getGui(EntityPlayer player, TileEntityTF2Dispenser te) {
        return new GuiTF2Dispenser(player.inventory, te);
    }

    @Override
    public void spawnBeams(BlockPos pos, int[] ids) {
        DispenserBeams.spawn(pos, ids);
    }
}

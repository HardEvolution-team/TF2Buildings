package com.electrifiedded.tf2buildings.dispenser.net;

import com.electrifiedded.tf2buildings.dispenser.tile.TileEntityTF2Dispenser;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** client -> server: toggled dispenser options from the GUI */
public class MsgOptions implements IMessage {
    public BlockPos pos;
    public byte options;

    public MsgOptions() {}

    public MsgOptions(BlockPos pos, byte options) {
        this.pos = pos;
        this.options = options;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        options = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeByte(options);
    }

    public static class Handler implements IMessageHandler<MsgOptions, IMessage> {
        @Override
        public IMessage onMessage(MsgOptions m, MessageContext ctx) {
            EntityPlayerMP p = ctx.getServerHandler().player;
            p.getServerWorld().addScheduledTask(() -> {
                if (!p.world.isBlockLoaded(m.pos)) return;
                TileEntity te = p.world.getTileEntity(m.pos);
                if (te instanceof TileEntityTF2Dispenser && ((TileEntityTF2Dispenser) te).isUsableByPlayer(p)) {
                    ((TileEntityTF2Dispenser) te).setAndCheckOptions(m.options);
                    ((TileEntityTF2Dispenser) te).sync();
                }
            });
            return null;
        }
    }
}

package com.electrifiedded.tf2buildings.dispenser.net;

import com.electrifiedded.tf2buildings.dispenser.TF2DispenserMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

/** server -> client: draw energy beams from the dispenser at pos to these players */
public class MsgBeam implements IMessage {
    public BlockPos pos;
    public int[] ids = new int[0];

    public MsgBeam() {}

    public MsgBeam(BlockPos pos, List<EntityPlayer> players) {
        this.pos = pos;
        ids = new int[players.size()];
        for (int i = 0; i < ids.length; i++) ids[i] = players.get(i).getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        int n = Math.min(buf.readUnsignedByte(), 255);
        ids = new int[n];
        for (int i = 0; i < n; i++) ids[i] = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        int n = Math.min(ids.length, 255);
        buf.writeByte(n);
        for (int i = 0; i < n; i++) buf.writeInt(ids[i]);
    }

    public static class Handler implements IMessageHandler<MsgBeam, IMessage> {
        @Override
        public IMessage onMessage(MsgBeam m, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> TF2DispenserMod.proxy.spawnBeams(m.pos, m.ids));
            return null;
        }
    }
}

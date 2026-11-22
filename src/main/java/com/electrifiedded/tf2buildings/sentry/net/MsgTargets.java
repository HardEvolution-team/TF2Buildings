package com.electrifiedded.tf2buildings.sentry.net;

import com.electrifiedded.tf2buildings.sentry.SentryConfig;
import com.electrifiedded.tf2buildings.sentry.SentryTargets;
import com.electrifiedded.tf2buildings.sentry.TF2SentryMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Target list, both directions (client -> server: change request, server -> client: sync). */
public class MsgTargets implements IMessage {
    public List<String> names = new ArrayList<>();

    public MsgTargets() {}

    public MsgTargets(Collection<String> n) { synchronized (n) { names.addAll(n); } }

    @Override
    public void fromBytes(ByteBuf buf) {
        int n = Math.min(buf.readInt(), 4096);
        for (int i = 0; i < n; i++) names.add(ByteBufUtils.readUTF8String(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(names.size());
        for (String s : names) ByteBufUtils.writeUTF8String(buf, s);
    }

    public static class ServerHandler implements IMessageHandler<MsgTargets, IMessage> {
        @Override
        public IMessage onMessage(MsgTargets m, MessageContext ctx) {
            EntityPlayerMP p = ctx.getServerHandler().player;
            p.getServerWorld().addScheduledTask(() -> {
                if (SentryConfig.opsOnly && !p.server.getPlayerList().canSendCommands(p.getGameProfile())) {
                    p.sendMessage(new TextComponentString("Only OPs are allowed to change the targets!"));
                    TF2SentryMod.NET.sendTo(new MsgTargets(SentryTargets.SERVER), p);
                    return;
                }
                SentryTargets.SERVER.clear();
                SentryTargets.SERVER.addAll(m.names);
                SentryTargets.write();
                TF2SentryMod.NET.sendToAll(new MsgTargets(SentryTargets.SERVER));
            });
            return null;
        }
    }

    public static class ClientHandler implements IMessageHandler<MsgTargets, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MsgTargets m, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                SentryTargets.CLIENT.clear();
                SentryTargets.CLIENT.addAll(m.names);
            });
            return null;
        }
    }
}

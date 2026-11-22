package com.electrifiedded.tf2buildings.sentry;

import com.electrifiedded.archaiclib.legacy.registry.LegacyGameRegistry;
import com.electrifiedded.archaiclib.legacy.registry.LegacyRecipes;
import com.electrifiedded.archaiclib.legacy.sound.LegacyModSounds;
import com.electrifiedded.tf2buildings.Reference;
import com.electrifiedded.tf2buildings.TF2Originals;
import com.electrifiedded.tf2buildings.sentry.entity.*;
import com.electrifiedded.tf2buildings.sentry.item.SentryItems;
import com.electrifiedded.tf2buildings.sentry.net.MsgTargets;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = TF2SentryMod.MODID, name = "TF2 Sentry", version = Reference.VERSION, dependencies = "required-after:archaiclib@[1.33.0,)")
public class TF2SentryMod {
    public static final String MODID = "tf2sentry";

    @Mod.Instance(MODID)
    public static TF2SentryMod instance;

    @SidedProxy(clientSide = "com.electrifiedded.tf2buildings.sentry.client.SentryClientProxy",
            serverSide = "com.electrifiedded.tf2buildings.sentry.SentryCommonProxy")
    public static SentryCommonProxy proxy;

    public static SimpleNetworkWrapper NET;
    public static Item sentryRed, sentryBlue, sentryBase, sentryHead, monitor, wrench, wrenchGolden;

    public TF2SentryMod() {
        TF2Originals.require(MODID, "TF2Sentry-1.7.10.jar");
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        SentryConfig.load(e.getSuggestedConfigurationFile());
        LegacyModSounds.autoRegister(MODID);
        sentryRed = item(new SentryItems.Sentry(false), "TF2SentryRed", "itemsentry");
        sentryBlue = item(new SentryItems.Sentry(true), "TF2SentryBlu", "itemsentryblue");
        sentryBase = item(new SentryItems.Piece(), "SentryBase", "sentrybase");
        sentryHead = item(new SentryItems.Piece(), "SentryHead", "sentryhead");
        monitor = item(new SentryItems.Monitor(), "SentryMonitor", "itemmonitor");
        wrench = item(new SentryItems.Wrench(100), "Wrench", "wrench");
        wrenchGolden = item(new SentryItems.Wrench(200), "GoldenWrench", "wrenchgold");

        LegacyGameRegistry.registerModEntity(EntityTF2Bullet.class, "TF2Bullet", 0, this, 160, 5, true);
        LegacyGameRegistry.registerModEntity(EntityTF2Rocket.class, "TF2Rocket", 1, this, 160, 5, true);
        LegacyGameRegistry.registerModEntity(EntityTF2Sentry.class, "TF2Sentry1", 2, this, 160, 5, true);
        LegacyGameRegistry.registerModEntity(EntityTF2Sentry2.class, "TF2Sentry2", 3, this, 160, 5, true);
        LegacyGameRegistry.registerModEntity(EntityTF2Sentry3.class, "TF2Sentry3", 4, this, 160, 5, true);

        NET = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        NET.registerMessage(MsgTargets.ServerHandler.class, MsgTargets.class, 0, Side.SERVER);
        NET.registerMessage(MsgTargets.ClientHandler.class, MsgTargets.class, 1, Side.CLIENT);
        proxy.preInit();
        MinecraftForge.EVENT_BUS.register(this);
    }

    private static Item item(Item item, String name, String texture) {
        item.setTranslationKey(name);
        LegacyGameRegistry.registerItem(item, name.toLowerCase());
        LegacyGameRegistry.registerItemModel(item, 0, MODID + ":" + texture, "inventory");
        return item;
    }

    @EventHandler
    public void init(FMLInitializationEvent e) {
        LegacyRecipes.addRecipe(new ItemStack(sentryBase), " X ", " X ", "X X", 'X', Items.IRON_INGOT);
        LegacyRecipes.addRecipe(new ItemStack(sentryHead), "XYX", "O#O", "XYX", 'X', Items.IRON_INGOT, 'Y', Items.REDSTONE, 'O', Items.GUNPOWDER, '#', Blocks.DISPENSER);
        LegacyRecipes.addRecipe(new ItemStack(sentryRed), "#", "X", "Y", 'X', sentryHead, 'Y', sentryBase, '#', new ItemStack(Items.DYE, 1, 1));
        LegacyRecipes.addRecipe(new ItemStack(sentryBlue), "#", "X", "Y", 'X', sentryHead, 'Y', sentryBase, '#', new ItemStack(Items.DYE, 1, 4));
        LegacyRecipes.addRecipe(new ItemStack(monitor), "XYX", "X#X", "X#X", 'X', Items.REDSTONE, 'Y', Blocks.GLASS, '#', Blocks.STONE_BUTTON);
        LegacyRecipes.addRecipe(new ItemStack(wrench), "X  ", "XX ", "  X", 'X', Items.IRON_INGOT);
        LegacyRecipes.addRecipe(new ItemStack(wrenchGolden), "X  ", "XX ", "  X", 'X', Items.GOLD_INGOT);
        LegacyRecipes.addRecipe(new ItemStack(sentryRed), "X", "Y", 'Y', sentryBlue, 'X', new ItemStack(Items.DYE, 1, 1));
        LegacyRecipes.addRecipe(new ItemStack(sentryBlue), "X", "Y", 'Y', sentryRed, 'X', new ItemStack(Items.DYE, 1, 4));
    }

    @EventHandler
    public void serverStarted(FMLServerStartedEvent e) {
        SentryTargets.read();
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.player instanceof EntityPlayerMP) NET.sendTo(new MsgTargets(SentryTargets.SERVER), (EntityPlayerMP) e.player);
    }
}

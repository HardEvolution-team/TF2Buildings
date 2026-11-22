package com.electrifiedded.tf2buildings.dispenser;

import com.electrifiedded.archaiclib.legacy.registry.LegacyGameRegistry;
import com.electrifiedded.archaiclib.legacy.registry.LegacyRecipes;
import com.electrifiedded.archaiclib.legacy.sound.LegacyModSounds;
import com.electrifiedded.tf2buildings.Reference;
import com.electrifiedded.tf2buildings.TF2Originals;
import com.electrifiedded.tf2buildings.dispenser.block.BlockTF2Dispenser;
import com.electrifiedded.tf2buildings.dispenser.inventory.ContainerTF2Dispenser;
import com.electrifiedded.tf2buildings.dispenser.item.ItemTF2Dispenser;
import com.electrifiedded.tf2buildings.dispenser.net.MsgBeam;
import com.electrifiedded.tf2buildings.dispenser.net.MsgOptions;
import com.electrifiedded.tf2buildings.dispenser.tile.TileEntityTF2Dispenser;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = TF2DispenserMod.MODID, name = "TF2 Dispenser", version = Reference.VERSION, dependencies = "required-after:archaiclib@[1.33.0,)")
public class TF2DispenserMod implements IGuiHandler {
    public static final String MODID = "tf2dispenser";

    @Mod.Instance(MODID)
    public static TF2DispenserMod instance;

    @SidedProxy(clientSide = "com.electrifiedded.tf2buildings.dispenser.client.DispenserClientProxy",
            serverSide = "com.electrifiedded.tf2buildings.dispenser.DispenserCommonProxy")
    public static DispenserCommonProxy proxy;

    public static SimpleNetworkWrapper NET;
    public static Block dispenserBlock;
    public static Item dispenserItem;

    public TF2DispenserMod() {
        TF2Originals.require(MODID, "TF2Dispenser-1.7.10.jar");
    }

    public static SoundEvent sound(String name) {
        return LegacyModSounds.get(MODID, name);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        DispenserConfig.load(e.getSuggestedConfigurationFile());
        LegacyModSounds.autoRegister(MODID);
        dispenserBlock = new BlockTF2Dispenser().setTranslationKey("TF2Dispenser");
        LegacyGameRegistry.registerBlock(dispenserBlock, null, "tf2dispenser_block");
        dispenserItem = new ItemTF2Dispenser().setTranslationKey("TF2Dispenser");
        LegacyGameRegistry.registerItem(dispenserItem, "tf2dispenser");
        LegacyGameRegistry.registerItemModel(dispenserItem, 0, MODID + ":dispenseritemblue", "inventory");
        LegacyGameRegistry.registerItemModel(dispenserItem, 1, MODID + ":dispenseritemred", "inventory");
        GameRegistry.registerTileEntity(TileEntityTF2Dispenser.class, new ResourceLocation(MODID, "tf2dispenser"));

        NET = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        NET.registerMessage(MsgOptions.Handler.class, MsgOptions.class, 0, Side.SERVER);
        NET.registerMessage(MsgBeam.Handler.class, MsgBeam.class, 1, Side.CLIENT);
        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent e) {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, this);
        Object[] base = {"XYX", "O#O", "XZX", 'X', Items.IRON_INGOT, 'O', Items.REDSTONE, '#', Blocks.GLASS, 'Z', Blocks.DISPENSER, 'Y'};
        LegacyRecipes.addRecipe(new ItemStack(dispenserItem, 1, 1), with(base, new ItemStack(Items.DYE, 1, 1)));
        LegacyRecipes.addRecipe(new ItemStack(dispenserItem, 1, 0), with(base, new ItemStack(Items.DYE, 1, 4)));
        LegacyRecipes.addRecipe(new ItemStack(dispenserItem, 1, 0), "O", "X", 'X', new ItemStack(dispenserItem, 1, 1), 'O', new ItemStack(Items.DYE, 1, 4));
        LegacyRecipes.addRecipe(new ItemStack(dispenserItem, 1, 1), "O", "X", 'X', new ItemStack(dispenserItem, 1, 0), 'O', new ItemStack(Items.DYE, 1, 1));
    }

    private static Object[] with(Object[] a, Object last) {
        Object[] r = java.util.Arrays.copyOf(a, a.length + 1);
        r[a.length] = last;
        return r;
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        DispenserConfig.readItems();
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        return te instanceof TileEntityTF2Dispenser ? new ContainerTF2Dispenser(player.inventory, (TileEntityTF2Dispenser) te) : null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        return te instanceof TileEntityTF2Dispenser ? proxy.getGui(player, (TileEntityTF2Dispenser) te) : null;
    }
}

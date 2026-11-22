package com.electrifiedded.tf2buildings.dispenser.block;

import com.electrifiedded.tf2buildings.dispenser.TF2DispenserMod;
import com.electrifiedded.tf2buildings.dispenser.tile.TileEntityTF2Dispenser;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

/** The dispenser building. Facing + team colour in the metadata, energy in the tile entity (rendered by a TESR). */
public class BlockTF2Dispenser extends BlockContainer {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyBool RED = PropertyBool.create("red");
    private static final AxisAlignedBB BOX = new AxisAlignedBB(0.3, 0.0, 0.3, 0.7, 1.2, 0.7);

    public BlockTF2Dispenser() {
        super(Material.IRON);
        setHardness(1.0F);
        setSoundType(SoundType.METAL);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(RED, false));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, RED);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3)).withProperty(RED, (meta & 4) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState s) {
        return s.getValue(FACING).getHorizontalIndex() | (s.getValue(RED) ? 4 : 0);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hx, float hy, float hz) {
        if (!world.isRemote && world.getTileEntity(pos) instanceof TileEntityTF2Dispenser)
            player.openGui(TF2DispenserMod.instance, 1, world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityTF2Dispenser) InventoryHelper.dropInventoryItems(world, pos, (TileEntityTF2Dispenser) te);
        super.breakBlock(world, pos, state);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityTF2Dispenser();
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState s, IBlockAccess w, BlockPos p) { return BOX; }

    @Override
    public boolean isOpaqueCube(IBlockState s) { return false; }

    @Override
    public boolean isFullCube(IBlockState s) { return false; }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess w, IBlockState s, BlockPos p, EnumFacing f) { return BlockFaceShape.UNDEFINED; }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState s) { return EnumBlockRenderType.ENTITYBLOCK_ANIMATED; }

    @Override
    public Item getItemDropped(IBlockState s, Random r, int fortune) { return TF2DispenserMod.dispenserItem; }

    @Override
    public int damageDropped(IBlockState s) { return s.getValue(RED) ? 1 : 0; }

    @Override
    public ItemStack getPickBlock(IBlockState s, RayTraceResult t, World w, BlockPos p, EntityPlayer pl) {
        return new ItemStack(TF2DispenserMod.dispenserItem, 1, damageDropped(s));
    }
}

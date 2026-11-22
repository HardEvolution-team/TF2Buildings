package com.electrifiedded.tf2buildings.dispenser.item;

import com.electrifiedded.tf2buildings.dispenser.TF2DispenserMod;
import com.electrifiedded.tf2buildings.dispenser.block.BlockTF2Dispenser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** meta 0 = BLU, 1 = RED */
public class ItemTF2Dispenser extends Item {
    public ItemTF2Dispenser() {
        setMaxStackSize(1);
        setHasSubtypes(true);
        setMaxDamage(0);
        setCreativeTab(CreativeTabs.MISC);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey(stack) + (stack.getMetadata() == 1 ? ".red" : ".blu");
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hx, float hy, float hz) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.getBlockState(pos).getBlock().isReplaceable(world, pos)) pos = pos.offset(facing);
        if (stack.isEmpty() || !player.canPlayerEdit(pos, facing, stack) || !world.mayPlace(TF2DispenserMod.dispenserBlock, pos, false, facing, player))
            return EnumActionResult.FAIL;
        if (!world.isRemote) {
            // the model's front faces the player, like 1.7
            IBlockState s = TF2DispenserMod.dispenserBlock.getDefaultState()
                    .withProperty(BlockTF2Dispenser.FACING, player.getHorizontalFacing().getOpposite())
                    .withProperty(BlockTF2Dispenser.RED, stack.getMetadata() != 0);
            world.setBlockState(pos, s, 3);
            if (!player.capabilities.isCreativeMode) stack.shrink(1);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab)) return;
        items.add(new ItemStack(this, 1, 0));
        items.add(new ItemStack(this, 1, 1));
    }
}

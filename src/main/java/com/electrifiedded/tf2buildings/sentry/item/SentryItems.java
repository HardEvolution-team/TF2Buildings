package com.electrifiedded.tf2buildings.sentry.item;

import com.electrifiedded.tf2buildings.sentry.TF2SentryMod;
import com.electrifiedded.tf2buildings.sentry.entity.EntityTF2Sentry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/** The sentry mod's items (sentry kits, monitor, wrenches). */
public final class SentryItems {
    private SentryItems() {}

    public static class Sentry extends Item {
        private final boolean blue;

        public Sentry(boolean blue) {
            this.blue = blue;
            setMaxStackSize(1);
            setCreativeTab(CreativeTabs.MISC);
        }

        @Override
        public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hx, float hy, float hz) {
            if (world.isRemote) return EnumActionResult.SUCCESS;
            BlockPos p = pos.offset(facing);
            int rotation = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5) & 3;
            EntityTF2Sentry sentry = new EntityTF2Sentry(world, p.getX() + 0.5, p.getY(), p.getZ() + 0.5, rotation);
            if (blue) sentry.setBlue();
            world.spawnEntity(sentry);
            if (!player.capabilities.isCreativeMode) player.getHeldItem(hand).shrink(1);
            return EnumActionResult.SUCCESS;
        }
    }

    public static class Monitor extends Item {
        public Monitor() {
            setMaxStackSize(1);
            setCreativeTab(CreativeTabs.MISC);
        }

        @Override
        public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
            if (world.isRemote) TF2SentryMod.proxy.openMonitorGUI();
            return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        }
    }

    public static class Wrench extends Item {
        public Wrench(int maxDamage) {
            setMaxStackSize(1);
            setMaxDamage(maxDamage);
            setCreativeTab(CreativeTabs.MISC);
        }

        @Override
        public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
            stack.damageItem(2, attacker);
            return true;
        }

        @Override
        public boolean isFull3D() { return true; }
    }

    public static class Piece extends Item {
        public Piece() {
            setMaxStackSize(1);
            setCreativeTab(CreativeTabs.MISC);
        }
    }
}

package com.electrifiedded.tf2buildings.dispenser.tile;

import com.electrifiedded.tf2buildings.dispenser.DispenserConfig;
import com.electrifiedded.tf2buildings.dispenser.TF2DispenserMod;
import com.electrifiedded.tf2buildings.dispenser.block.BlockTF2Dispenser;
import com.electrifiedded.tf2buildings.dispenser.net.MsgBeam;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.ArrayList;
import java.util.List;

public class TileEntityTF2Dispenser extends TileEntity implements ITickable, ISidedInventory {
    public static final byte HEAL = 1, FEED = 2, REPAIR = 4, NEUTRALIZE = 8;

    private final NonNullList<ItemStack> inv = NonNullList.withSize(1, ItemStack.EMPTY);
    public int energy;
    public int maxEnergy;
    public byte options;
    private int counter;
    private int lastEnergy = -1, lastOptions = -1;
    /** client-only render state (hatch animation), kept per tile instead of per shared model */
    public float hatchAngle;

    public TileEntityTF2Dispenser() {
        options = defaultOptions();
    }

    private static byte defaultOptions() {
        byte b = 0;
        if (DispenserConfig.defaultHealing) b |= HEAL;
        if (DispenserConfig.defaultFeeding) b |= FEED;
        if (DispenserConfig.defaultRepairing) b |= REPAIR;
        if (DispenserConfig.defaultNeutralizingAffects) b |= NEUTRALIZE;
        return b;
    }

    public void setAndCheckOptions(byte b) {
        options = apply(options, b, HEAL, DispenserConfig.allowHealing);
        options = apply(options, b, FEED, DispenserConfig.allowFeeding);
        options = apply(options, b, REPAIR, DispenserConfig.allowRepairing);
        options = apply(options, b, NEUTRALIZE, DispenserConfig.allowNeutralizingAffects);
        markDirty();
    }

    private static byte apply(byte cur, byte want, byte bit, boolean allowed) {
        if (!allowed) return cur;
        return (byte) ((want & bit) != 0 ? cur | bit : cur & ~bit);
    }

    @Override
    public void update() {
        if (world.isRemote) return;
        boolean active = isActive();
        List<EntityPlayer> beams = new ArrayList<>();
        if (++counter > 10) {
            if (active) {
                for (EntityPlayer p : getPlayersInRange()) {
                    boolean beam = false;
                    if (p.isBurning()) {
                        p.extinguish();
                        beam = true;
                    }
                    if ((options & HEAL) != 0) beam |= heal(p);
                    if ((options & FEED) != 0) beam |= feed(p);
                    if ((options & REPAIR) != 0) beam |= repair(p);
                    if ((options & NEUTRALIZE) != 0) beam |= neutralize(p);
                    if (beam) beams.add(p);
                }
            }
            if (!isActive() && !inv.get(0).isEmpty()) fillUp();
            if (energy < 0) energy = 0;
            if (active != isActive()) markDirty();
            counter = 0;
        }
        if (!beams.isEmpty() && active && options > 0) {
            world.playSound(null, pos, TF2DispenserMod.sound("beam"), SoundCategory.BLOCKS, DispenserConfig.beamVolume, 1.0F);
            TF2DispenserMod.NET.sendToAllAround(new MsgBeam(pos, beams),
                    new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64));
        }
        if (lastEnergy != energy || lastOptions != options) {
            lastEnergy = energy;
            lastOptions = options;
            sync();
        }
    }

    public void sync() {
        IBlockState s = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, s, s, 3);
    }

    private void fillUp() {
        ItemStack s = inv.get(0);
        int value = fuelValue(s.getItem());
        if (value == 0) return;
        energy = maxEnergy = value;
        s.shrink(1);
        world.playSound(null, pos, TF2DispenserMod.sound("fillUp"), SoundCategory.BLOCKS, DispenserConfig.fillUpVolume, 1.0F);
    }

    public static int fuelValue(Item i) {
        if (i == Items.IRON_INGOT) return 100;
        if (i == Items.GOLD_INGOT) return 200;
        if (i == Items.DIAMOND) return 800;
        return 0;
    }

    private boolean heal(EntityPlayer p) {
        if (p.getHealth() >= p.getMaxHealth()) return false;
        p.heal(1.0F);
        energy -= 2;
        return true;
    }

    private boolean feed(EntityPlayer p) {
        if (!p.getFoodStats().needFood()) return false;
        p.getFoodStats().addStats(1, 0.2F);
        energy -= 2;
        return true;
    }

    private boolean repair(EntityPlayer p) {
        ItemStack s = p.getHeldItemMainhand();
        if (s.isEmpty() || !s.isItemDamaged() || DispenserConfig.IGNORED.contains(s.getItem())) return false;
        s.setItemDamage(Math.max(0, s.getItemDamage() - 50));
        energy -= Math.min(15, energy);
        return true;
    }

    private boolean neutralize(EntityPlayer p) {
        if (p.getActivePotionEffects().isEmpty()) return false;
        p.clearActivePotions();
        energy -= 10;
        return true;
    }

    public boolean isRed() {
        IBlockState s = world == null ? null : world.getBlockState(pos);
        return s != null && s.getBlock() instanceof BlockTF2Dispenser && s.getValue(BlockTF2Dispenser.RED);
    }

    public EnumFacing getFacing() {
        IBlockState s = world == null ? null : world.getBlockState(pos);
        return s != null && s.getBlock() instanceof BlockTF2Dispenser ? s.getValue(BlockTF2Dispenser.FACING) : EnumFacing.NORTH;
    }

    public List<EntityPlayer> getPlayersInRange() {
        List<EntityPlayer> out = new ArrayList<>();
        for (EntityPlayer p : world.playerEntities)
            if (!p.isSpectator() && p.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 15.0) out.add(p);
        return out;
    }

    public boolean isActive() { return energy > 0; }

    public int getMaxEnergy() { return maxEnergy; }

    // ---- NBT / sync
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        readSync(tag);
        inv.set(0, tag.hasKey("Item") ? new ItemStack(tag.getCompoundTag("Item")) : ItemStack.EMPTY);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        writeSync(tag);
        if (!inv.get(0).isEmpty()) tag.setTag("Item", inv.get(0).writeToNBT(new NBTTagCompound()));
        return tag;
    }

    private void readSync(NBTTagCompound tag) {
        energy = tag.getInteger("Energy");
        maxEnergy = tag.getInteger("MaxEnergy");
        options = tag.getByte("Options");
    }

    private void writeSync(NBTTagCompound tag) {
        tag.setInteger("Energy", energy);
        tag.setInteger("MaxEnergy", maxEnergy);
        tag.setByte("Options", options);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound t = super.getUpdateTag();
        writeSync(t);
        return t;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        readSync(tag);
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound t = new NBTTagCompound();
        writeSync(t);
        return new SPacketUpdateTileEntity(pos, 0, t);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readSync(pkt.getNbtCompound());
    }

    @Override
    public boolean shouldRefresh(World w, BlockPos p, IBlockState o, IBlockState n) {
        return o.getBlock() != n.getBlock();
    }

    // ---- inventory
    @Override
    public int getSizeInventory() { return 1; }

    @Override
    public boolean isEmpty() { return inv.get(0).isEmpty(); }

    @Override
    public ItemStack getStackInSlot(int i) { return inv.get(0); }

    @Override
    public ItemStack decrStackSize(int i, int n) {
        ItemStack s = ItemStackHelper.getAndSplit(inv, 0, n);
        if (!s.isEmpty()) markDirty();
        return s;
    }

    @Override
    public ItemStack removeStackFromSlot(int i) { return ItemStackHelper.getAndRemove(inv, 0); }

    @Override
    public void setInventorySlotContents(int i, ItemStack s) {
        inv.set(0, s);
        if (s.getCount() > getInventoryStackLimit()) s.setCount(getInventoryStackLimit());
        markDirty();
    }

    @Override
    public int getInventoryStackLimit() { return 64; }

    @Override
    public boolean isUsableByPlayer(EntityPlayer p) {
        return world.getTileEntity(pos) == this && p.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void openInventory(EntityPlayer p) {}

    @Override
    public void closeInventory(EntityPlayer p) {}

    @Override
    public boolean isItemValidForSlot(int i, ItemStack s) { return fuelValue(s.getItem()) > 0; }

    @Override
    public int getField(int id) { return 0; }

    @Override
    public void setField(int id, int value) {}

    @Override
    public int getFieldCount() { return 0; }

    @Override
    public void clear() { inv.set(0, ItemStack.EMPTY); }

    @Override
    public int[] getSlotsForFace(EnumFacing side) { return new int[]{0}; }

    @Override
    public boolean canInsertItem(int i, ItemStack s, EnumFacing side) { return isItemValidForSlot(i, s); }

    @Override
    public boolean canExtractItem(int i, ItemStack s, EnumFacing side) { return false; }

    @Override
    public String getName() { return "TF2 Dispenser"; }

    @Override
    public boolean hasCustomName() { return false; }

    @Override
    public ITextComponent getDisplayName() { return new TextComponentString(getName()); }
}

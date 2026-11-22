package com.electrifiedded.tf2buildings.dispenser.inventory;

import com.electrifiedded.tf2buildings.dispenser.tile.TileEntityTF2Dispenser;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerTF2Dispenser extends Container {
    private final TileEntityTF2Dispenser dispenser;

    public ContainerTF2Dispenser(InventoryPlayer inv, TileEntityTF2Dispenser te) {
        dispenser = te;
        addSlotToContainer(new Slot(te, 0, 80, 35) {
            @Override
            public boolean isItemValid(ItemStack s) {
                return TileEntityTF2Dispenser.fuelValue(s.getItem()) > 0;
            }
        });
        for (int i = 0; i < 3; i++)
            for (int k = 0; k < 9; k++) addSlotToContainer(new Slot(inv, k + i * 9 + 9, 8 + k * 18, 84 + i * 18));
        for (int j = 0; j < 9; j++) addSlotToContainer(new Slot(inv, j, 8 + j * 18, 142));
    }

    @Override
    public boolean canInteractWith(EntityPlayer p) {
        return dispenser.isUsableByPlayer(p);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer p, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack s = slot.getStack();
            copy = s.copy();
            if (index == 0) {
                if (!mergeItemStack(s, 1, 37, true)) return ItemStack.EMPTY;
            } else if (inventorySlots.get(0).isItemValid(s)) {
                if (!mergeItemStack(s, 0, 1, false)) return ItemStack.EMPTY;
            } else return ItemStack.EMPTY;
            if (s.isEmpty()) slot.putStack(ItemStack.EMPTY);
            else slot.onSlotChanged();
            if (s.getCount() == copy.getCount()) return ItemStack.EMPTY;
            slot.onTake(p, s);
        }
        return copy;
    }
}

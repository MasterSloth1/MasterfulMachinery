package com.ticticboooom.mods.mm.block.tile;

import com.ticticboooom.mods.mm.block.container.PortBlockContainer;
import com.ticticboooom.mods.mm.network.PacketHandler;
import com.ticticboooom.mods.mm.network.packets.TileClientUpdatePacket;
import com.ticticboooom.mods.mm.ports.storage.IPortStorage;
import lombok.Getter;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MachinePortBlockEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider {

    private ContainerType<?> container;
    @Getter
    private IPortStorage storage;
    @Getter
    private boolean input;


    public MachinePortBlockEntity(TileEntityType<?> p_i48289_1_, ContainerType<?> container, IPortStorage storage, boolean input) {
        super(p_i48289_1_);
        this.container = container;
        this.storage = storage;
        this.input = input;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (storage.validate(cap)){
            return storage.getLO();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt.put("inv", storage.save(new CompoundNBT()));
        return super.save(nbt);
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        storage.load(nbt.getCompound("inv"));
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent("Port");
    }

    @Nullable
    @Override
    public Container createMenu(int windowsId, PlayerInventory inv, PlayerEntity player) {
        return new PortBlockContainer(container, windowsId, inv,this);
    }

    @Override
    public void tick() {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new TileClientUpdatePacket.Data(worldPosition, save(new CompoundNBT())));
    }
}

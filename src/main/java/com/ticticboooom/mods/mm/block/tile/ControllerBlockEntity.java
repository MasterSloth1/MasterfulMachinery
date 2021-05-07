package com.ticticboooom.mods.mm.block.tile;

import com.ticticboooom.mods.mm.block.container.ControllerBlockContainer;
import com.ticticboooom.mods.mm.data.MachineProcessRecipe;
import com.ticticboooom.mods.mm.data.MachineStructureRecipe;
import com.ticticboooom.mods.mm.model.ProcessUpdate;
import com.ticticboooom.mods.mm.ports.storage.IPortStorage;
import com.ticticboooom.mods.mm.registration.RecipeTypes;
import lombok.Getter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ControllerBlockEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider {


    private RegistryObject<ContainerType<ControllerBlockContainer>> container;
    private String controllerId;
    @Getter
    private ProcessUpdate update = new ProcessUpdate(0, "");

    public ControllerBlockEntity(RegistryObject<TileEntityType<?>> type, RegistryObject<ContainerType<ControllerBlockContainer>> container, String controllerId) {
        super(type.get());
        this.container = container;
        this.controllerId = controllerId;
    }


    @Override
    public void tick() {
        List<MachineStructureRecipe> recipes = level.getRecipeManager().getAllRecipesFor(RecipeTypes.MACHINE_STRUCTURE);
        for (MachineStructureRecipe recipe : recipes) {
            if (recipe.matches(this.worldPosition, level, controllerId)) {
                update.setMsg("Found structure");
                onStructureFound(recipe);
                return;
            }
        }
        update.setMsg("Failed to construct \nthe machine");
    }

    private void onStructureFound(MachineStructureRecipe structure) {
        ArrayList<BlockPos> ports = structure.getPorts(worldPosition, level);
        List<IPortStorage> inputPorts = new ArrayList<>();
        List<IPortStorage> outputPorts = new ArrayList<>();
        for (BlockPos port : ports) {
            TileEntity blockEntity = level.getBlockEntity(port);
            if (blockEntity instanceof MachinePortBlockEntity) {
                MachinePortBlockEntity portBE = (MachinePortBlockEntity) blockEntity;

                if (portBE.isInput()) {
                    inputPorts.add(portBE.getStorage());
                } else {
                    outputPorts.add(portBE.getStorage());
                }
            }
        }

        onPortsEstablished(inputPorts, outputPorts, structure);
    }

    private void onPortsEstablished(List<IPortStorage> inputPorts, List<IPortStorage> outputPorts, MachineStructureRecipe structure) {
        List<MachineProcessRecipe> processRecipes = level.getRecipeManager().getAllRecipesFor(RecipeTypes.MACHINE_PROCESS);
        for (MachineProcessRecipe recipe : processRecipes) {
            if (recipe.matches(inputPorts, structure.getStructureId())) {
                ProcessUpdate update = recipe.process(inputPorts, outputPorts, this.update);
                this.update = update;
                return;
            }
        }
    }


    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("container.masterfulmachinery.controller.name");
    }

    @Nullable
    @Override
    public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {
        return new ControllerBlockContainer(container.get(), p_createMenu_1_, this);
    }
}

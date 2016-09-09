package net.blay09.mods.excompressum.tile;

import net.blay09.mods.excompressum.ExCompressumConfig;
import net.blay09.mods.excompressum.handler.VanillaPacketHandler;
import net.blay09.mods.excompressum.registry.ExNihiloProvider;
import net.blay09.mods.excompressum.registry.ExRegistro;
import net.blay09.mods.excompressum.registry.crucible.WoodenCrucibleRegistry;
import net.blay09.mods.excompressum.registry.crucible.WoodenMeltable;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class TileWoodenCrucible extends TileEntity implements ITickable {

	private static final int RAIN_FILL_INTERVAL = 20;
	private static final int MELT_INTERVAL = 20;
	private static final int RAIN_FILL_SPEED = 8;
	private static final int SYNC_INTERVAL = 10;

	private ItemStackHandler itemHandler = new ItemStackHandler(1) {
		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			ItemStack copy = stack.copy();
			if (addItem(copy)) {
				return copy.stackSize == 0 ? null : copy;
			}
			return stack;
		}
	};

	private FluidTank fluidTank = new FluidTank(1999) {
		@Override
		public int fill(FluidStack resource, boolean doFill) {
			int result = super.fill(resource, doFill);
			if (fluid != null && fluid.amount > 1000) {
				fluid.amount = 1000;
			}
			return result;
		}

		@Override
		public int getCapacity() {
			return 1000;
		}

		@Override
		public boolean canFill() {
			return itemHandler.getStackInSlot(0) == null;
		}

		@Override
		public boolean canFillFluidType(FluidStack fluid) {
			return super.canFillFluidType(fluid) && fluid.getFluid().getTemperature() <= 300;
		}

		@Override
		protected void onContentsChanged() {
			markDirty();
			isDirty = true;
		}
	};

	private int ticksSinceSync;
	private boolean isDirty;
	private int ticksSinceRain;
	private int ticksSinceMelt;
	private WoodenMeltable currentMeltable;
	private int solidVolume;

	public boolean addItem(ItemStack itemStack) {
		// TODO This should be registryfied:
		// When inserting dust, turn it into clay if we have enough liquid
		if (ExCompressumConfig.woodenCrucibleMakesClay && fluidTank.getFluidAmount() >= Fluid.BUCKET_VOLUME && ExRegistro.isNihiloItem(itemStack, ExNihiloProvider.NihiloItems.DUST)) {
			itemStack.stackSize--;
			itemHandler.setStackInSlot(0, new ItemStack(Blocks.CLAY));
			fluidTank.setFluid(null);
			VanillaPacketHandler.sendTileEntityUpdate(this);
			return true;
		}

		// Otherwise, try to add it as a meltable
		WoodenMeltable meltable = WoodenCrucibleRegistry.getMeltable(itemStack);
		if (meltable != null) {
			int capacityLeft = fluidTank.getCapacity() - fluidTank.getFluidAmount() - solidVolume;
			if (capacityLeft >= meltable.fluidStack.amount) {
				itemStack.stackSize--;
				currentMeltable = meltable;
				solidVolume += meltable.fluidStack.amount;
				VanillaPacketHandler.sendTileEntityUpdate(this);
				return true;
			}
		}
		return false;
	}

	@Override
	public void update() {
		if (!worldObj.isRemote) {
			// Fill the crucible from rain
			// Note: It'd be stupid to depend on the biome's rainfall since this is a skyblock. The biome isn't known unless you go into the debug screen.
			if (worldObj.getWorldInfo().isRaining() && worldObj.canBlockSeeSky(pos) && ExCompressumConfig.woodenCrucibleFillFromRain) {
				ticksSinceRain++;
				if (ticksSinceRain >= RAIN_FILL_INTERVAL) {
					fluidTank.fill(new FluidStack(FluidRegistry.WATER, RAIN_FILL_SPEED), true);
					ticksSinceRain = 0;
					isDirty = true;
				}
			}

			// Melt down content
			if (currentMeltable != null) {
				ticksSinceMelt++;
				if (ticksSinceMelt >= MELT_INTERVAL && fluidTank.getFluidAmount() < fluidTank.getCapacity()) {
					int amount = Math.min(ExCompressumConfig.woodenCrucibleSpeed, solidVolume);
					fluidTank.fill(new FluidStack(currentMeltable.fluidStack.getFluid(), amount), true);
					solidVolume = Math.max(0, solidVolume - amount);
					ticksSinceMelt = 0;
					isDirty = true;
				}
			}

			// Sync to clients
			ticksSinceSync++;
			if (ticksSinceSync >= SYNC_INTERVAL) {
				ticksSinceSync = 0;
				if (isDirty) {
					VanillaPacketHandler.sendTileEntityUpdate(this);
					isDirty = false;
				}
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		solidVolume = tagCompound.getInteger("SolidVolume");
		fluidTank.readFromNBT(tagCompound.getCompoundTag("FluidTank"));
		itemHandler.deserializeNBT(tagCompound.getCompoundTag("ItemHandler"));
		if (tagCompound.hasKey("Content")) {
			currentMeltable = WoodenCrucibleRegistry.getMeltable(ItemStack.loadItemStackFromNBT(tagCompound.getCompoundTag("Content")));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		if (currentMeltable != null) {
			tagCompound.setTag("Content", currentMeltable.itemStack.writeToNBT(new NBTTagCompound()));
		}
		tagCompound.setInteger("SolidVolume", solidVolume);
		tagCompound.setTag("FluidTank", fluidTank.writeToNBT(new NBTTagCompound()));
		tagCompound.setTag("ItemHandler", itemHandler.serializeNBT());
		return tagCompound;
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, getBlockMetadata(), getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
				|| capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
				|| super.hasCapability(capability, facing);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return (T) fluidTank;
		}
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return (T) itemHandler;
		}
		return super.getCapability(capability, facing);
	}

	public FluidTank getFluidTank() {
		return fluidTank;
	}

	public int getSolidVolume() {
		return solidVolume;
	}

	public int getSolidCapacity() {
		return fluidTank.getCapacity();
	}

	public ItemStackHandler getItemHandler() {
		return itemHandler;
	}
}
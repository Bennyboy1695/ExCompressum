package net.blay09.mods.excompressum.utils;

import net.minecraftforge.energy.EnergyStorage;

public class EnergyStorageModifiable extends EnergyStorage {
	public EnergyStorageModifiable(int capacity) {
		super(capacity);
	}

	public void setEnergyStored(int energy) {
		this.energy = energy;
	}
}

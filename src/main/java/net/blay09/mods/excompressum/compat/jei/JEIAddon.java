package net.blay09.mods.excompressum.compat.jei;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import net.blay09.mods.excompressum.block.ModBlocks;
import net.blay09.mods.excompressum.config.BlockConfig;
import net.blay09.mods.excompressum.config.ExCompressumConfig;
import net.blay09.mods.excompressum.config.ItemConfig;
import net.blay09.mods.excompressum.item.ModItems;
import net.blay09.mods.excompressum.registry.ExRegistro;
import net.blay09.mods.excompressum.registry.compressedhammer.CompressedHammerRegistry;
import net.blay09.mods.excompressum.registry.compressedhammer.CompressedHammerRegistryEntry;
import net.blay09.mods.excompressum.registry.heavysieve.HeavySieveRegistry;
import net.blay09.mods.excompressum.registry.heavysieve.HeavySieveRegistryEntry;
import net.blay09.mods.excompressum.registry.sievemesh.SieveMeshRegistry;
import net.blay09.mods.excompressum.registry.sievemesh.SieveMeshRegistryEntry;
import net.blay09.mods.excompressum.registry.woodencrucible.WoodenCrucibleRegistry;
import net.blay09.mods.excompressum.registry.woodencrucible.WoodenCrucibleRegistryEntry;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.Collection;
import java.util.List;

@JEIPlugin
public class JEIAddon extends BlankModPlugin {

	public static IJeiHelpers jeiHelpers;

	@Override
	public void register(IModRegistry registry) {
		jeiHelpers = registry.getJeiHelpers();

		registry.addRecipeCategories(
				new HeavySieveRecipeCategory(registry.getJeiHelpers().getGuiHelper()),
				new CompressedHammerRecipeCategory(registry.getJeiHelpers().getGuiHelper()),
				new WoodenCrucibleRecipeCategory(registry.getJeiHelpers().getGuiHelper()),
				new ChickenStickRecipeCategory(registry.getJeiHelpers().getGuiHelper())
				);
		registry.addRecipeHandlers(
				new HeavySieveRecipeHandler(),
				new CompressedHammerRecipeHandler(),
				new WoodenCrucibleRecipeHandler(),
				new ChickenStickRecipeHandler()
				);

		List<HeavySieveRecipe> heavySieveRecipes = Lists.newArrayList();
		if(ExRegistro.doMeshesSplitLootTables()) {
			Collection<HeavySieveRegistryEntry> entries = HeavySieveRegistry.INSTANCE.getEntries().values();
			for (SieveMeshRegistryEntry sieveMesh : SieveMeshRegistry.getEntries().values()) {
				for(HeavySieveRegistryEntry entry : entries) {
					if(!entry.getRewardsForMesh(sieveMesh).isEmpty()) {
						heavySieveRecipes.add(new HeavySieveRecipe(entry, sieveMesh));
					}
				}
			}
		} else {
			for(HeavySieveRegistryEntry entry : HeavySieveRegistry.INSTANCE.getEntries().values()) {
				heavySieveRecipes.add(new HeavySieveRecipe(entry));
			}
		}
		registry.addRecipes(heavySieveRecipes);

		List<CompressedHammerRecipe> compressedHammerRecipes = Lists.newArrayList();
		for(CompressedHammerRegistryEntry entry : CompressedHammerRegistry.INSTANCE.getEntries().values()) {
			compressedHammerRecipes.add(new CompressedHammerRecipe(entry));
		}
		registry.addRecipes(compressedHammerRecipes);

		List<WoodenCrucibleRecipe> woodenCrucibleRecipes = Lists.newArrayList();
		ArrayListMultimap<String, WoodenCrucibleRegistryEntry> fluidOutputMap = ArrayListMultimap.create();
		for(WoodenCrucibleRegistryEntry entry : WoodenCrucibleRegistry.INSTANCE.getEntries().values()) {
			fluidOutputMap.put(entry.getFluid().getName(), entry);
		}
		for(String fluidName : fluidOutputMap.keySet()) {
			woodenCrucibleRecipes.add(new WoodenCrucibleRecipe(FluidRegistry.getFluid(fluidName), fluidOutputMap.get(fluidName)));
		}
		registry.addRecipes(woodenCrucibleRecipes);

		registry.addRecipes(Lists.newArrayList(new ChickenStickRecipe()));

		registry.addRecipeCategoryCraftingItem(new ItemStack(ModBlocks.heavySieve), HeavySieveRecipeCategory.UID);
		registry.addRecipeCategoryCraftingItem(new ItemStack(ModBlocks.woodenCrucible), WoodenCrucibleRecipeCategory.UID);
		registry.addRecipeCategoryCraftingItem(new ItemStack(ModItems.compressedHammerDiamond), CompressedHammerRecipeCategory.UID);
		registry.addRecipeCategoryCraftingItem(new ItemStack(ModItems.compressedHammerGold), CompressedHammerRecipeCategory.UID);
		registry.addRecipeCategoryCraftingItem(new ItemStack(ModItems.compressedHammerIron), CompressedHammerRecipeCategory.UID);
		registry.addRecipeCategoryCraftingItem(new ItemStack(ModItems.compressedHammerStone), CompressedHammerRecipeCategory.UID);
		registry.addRecipeCategoryCraftingItem(new ItemStack(ModItems.compressedHammerWood), CompressedHammerRecipeCategory.UID);

		for(BlockConfig.Entry entry : BlockConfig.getEntries()) {
			if(!entry.isEnabled()) {
				registry.getJeiHelpers().getItemBlacklist().addItemToBlacklist(entry.getItemStack());
			}
		}

		for(ItemConfig.Entry entry : ItemConfig.getEntries()) {
			if(!entry.isEnabled()) {
				registry.getJeiHelpers().getItemBlacklist().addItemToBlacklist(entry.getItemStack());
			}
		}

		if(!ExCompressumConfig.enableWoodChippings) {
			registry.getJeiHelpers().getItemBlacklist().addItemToBlacklist(new ItemStack(ModItems.woodChipping));
		}
	}

}

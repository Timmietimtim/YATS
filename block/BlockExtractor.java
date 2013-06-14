package YATS.block;

import YATS.tile.TileExtractor;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockExtractor extends Block implements ITileEntityProvider
{
	public BlockExtractor(int id)
	{
		super(id, Material.rock);
		setCreativeTab(CreativeTabs.tabMisc);
	}
	public TileEntity createNewTileEntity(World world)
	{
		return new TileExtractor();
	}
}

package YATS.util;

import YATS.api.ICapsule;
import YATS.api.ITubeConnectible;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

public class TubeRouting
{
	PriorityQueue<TubeRoute> queue = new PriorityQueue<TubeRoute>();
	HashSet explored = new HashSet();
	World world;
	public TubeRoute result;

	public TubeRouting(World world){ this.world = world; }

	public void ScanBlock(LazUtils.XYZCoords coords, ForgeDirection side, ForgeDirection direction, int priority, ICapsule capsule)
	{
		if(world.blockHasTileEntity(coords.x,coords.y,coords.z))
		{
			if(!explored.contains(coords))
			{
				if (world.getBlockTileEntity(coords.x,coords.y,coords.z) instanceof ITubeConnectible)
				{
					ITubeConnectible tube = (ITubeConnectible)world.getBlockTileEntity(coords.x,coords.y,coords.z);
					if(tube.CanRoute())
					{
						explored.add(coords);
						if(tube.CanAccept(capsule))
						{
							TubeRoute route = new TubeRoute(coords, side, direction, priority+tube.GetAdditionalPriority());
							route.isComplete=true;
							queue.add(route);
							return;
						}
						queue.add(new TubeRoute(coords, side, direction, priority+tube.GetAdditionalPriority()));
					}
				}
				else if (world.getBlockTileEntity(coords.x,coords.y,coords.z) instanceof IInventory)
				{
					IInventory inv = (IInventory)world.getBlockTileEntity(coords.x,coords.y,coords.z);
					if(capsule.GetContents() instanceof ItemStack && LazUtils.InventoryCore.CanAddToInventory(coords, (ItemStack) capsule.GetContents()))
					{
						TubeRoute route = new TubeRoute(coords,side,direction,priority);
						route.isComplete=true;
						queue.add(route);
						return;
					}
				}
			}
		}
	}
	public ForgeDirection FindRoute(LazUtils.XYZCoords coords, ForgeDirection initial, List<ForgeDirection> sides, ICapsule capsule)
	{
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			if(sides.contains(side))
			{
				LazUtils.XYZCoords newcoords = coords.Copy();
				newcoords.Next(side);
				ScanBlock(newcoords,side,side,(side == initial ? 0 : 1),capsule);
			}
		}

		while(!queue.isEmpty())
		{
			TubeRoute route = queue.poll();
			if(route.isComplete)
			{
				this.result = route;
				return route.direction;
			}

			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				if(side != route.destside && ((ITubeConnectible)route.destblock.ToTile()).IsConnectedOnSide(side))
				{
					LazUtils.XYZCoords newcoords = route.destblock.Copy();
					newcoords.Next(side);
					ScanBlock(newcoords, route.direction, side, route.priority + 1, capsule);
				}
			}
		}
		return ForgeDirection.UNKNOWN;
	}

	class TubeRoute implements Comparable<TubeRoute>
	{
		public LazUtils.XYZCoords destblock;
		public ForgeDirection destside;
		public ForgeDirection direction;
		public int priority;
		public boolean isComplete;

		public TubeRoute(LazUtils.XYZCoords coords, ForgeDirection side, ForgeDirection dir, int weight)
		{
			this.destblock=coords;
			this.destside=side;
			this.direction = dir;
			this.priority = weight;
		}
		public int compareTo(TubeRoute route)
		{
			return this.priority - route.priority;
		}
	}
}




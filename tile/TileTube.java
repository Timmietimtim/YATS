package YATS.tile;

import YATS.api.ICapsule;
import YATS.api.ITubeConnectible;
import YATS.api.YATSRegistry;
import YATS.block.BlockTube;
import YATS.common.YATS;
import YATS.util.*;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import java.util.ArrayList;

public class TileTube extends TileEntity implements ITubeConnectible
{
	public int pressure = 1;
	public ArrayList<ICapsule> contents;
	public boolean isConnectableOnSide[] = {true,true,true,true,true,true};
	public boolean isConnectedOnSide[] = new boolean[6];
	public Colours colour = Colours.NONE;

	public TileTube()
	{
		super();
		contents = new ArrayList<ICapsule>();
	}

	@Override
	public boolean AcceptsItemsOnSide(ForgeDirection side)
	{
		return isConnectedOnSide[side.ordinal()];
	}

	@Override
	public boolean IsConnectedOnSide(ForgeDirection side)
	{
		return isConnectedOnSide[side.ordinal()];
	}

	@Override
	public boolean CanAccept(ICapsule capsule)
	{
		return (capsule.GetColour() == this.colour || capsule.GetColour() == Colours.NONE || this.colour == Colours.NONE);
	}

	@Override
	public Boolean CanRoute()
	{
		return true;
	}

	@Override
	public int GetPressure()
	{
		return this.pressure;
	}

	@Override
	public void SetPressure(int pressure)
	{
		this.pressure = pressure;
	}

	@Override
	public Colours GetColour()
	{
		return this.colour;
	}

	@Override
	public void SetColour(Colours colour)
	{
		this.colour = colour;
	}

	@Override
	public int GetAdditionalPriority()
	{
		return 0;
	}

	@Override
	public void AcceptCapsule(ICapsule capsule)
	{
		capsule.resetProgress();
		this.contents.add(capsule);
		if(YATS.IS_DEBUG)
		LazUtils.logNormal("Tolerance! Accepting capsule type %s at %s,%s,%s. Contents: %s", YATSRegistry.getIDFromCapsule(capsule),xCoord,yCoord,zCoord,contents.toString());
	}

	@Override
	public void SetConnectionOnSide(ForgeDirection side, boolean connected)
	{
		isConnectedOnSide[side.ordinal()] = connected;
	}

	@Override
	public boolean IsConnectableOnSide(ForgeDirection side)
	{
		return isConnectableOnSide[side.ordinal()];
	}

	@Override
	public boolean canUpdate() { return true; }

	@Override
	public void updateEntity()
	{
		ArrayList<ICapsule> capsulesToRemove = new ArrayList<ICapsule>();
		BlockTube.CheckTubeConnections(worldObj,xCoord,yCoord,zCoord);
		for (ICapsule capsule : contents)
		{
			if(YATS.IS_DEBUG)
			LazUtils.logNormal("Discovery! We have a capsule, updating. %s, %s, %s", xCoord,yCoord,zCoord);
			capsule.SetHeading(new TubeRouting(worldObj).FindRoute(LazUtils.XYZCoords.FromTile(this), capsule.GetHeading(), GetConnectedSides(), capsule));
			if(YATS.IS_DEBUG)
			LazUtils.logNormal("Guidance! Capsule routing updated: %s, %s, %s", xCoord, yCoord, zCoord);
			capsule.addProgress((float) pressure / 100);
			if(YATS.IS_DEBUG)
			LazUtils.logNormal("Ambition! Capsule moved forward successfully, capsule progress %s, pressure %s. %s, %s, %s",capsule.getProgress(),pressure,xCoord,yCoord,zCoord);
			if(capsule.getProgress() >= 1)
			{
				LazUtils.XYZCoords coords = LazUtils.XYZCoords.FromTile(this);
				coords.Next(capsule.GetHeading());
				TileEntity tile = coords.ToTile();
				if(tile instanceof IInventory && capsule.GetContents() instanceof ItemStack &&
						LazUtils.InventoryCore.CanAddToInventory(coords, (ItemStack) capsule.GetContents()))
				{
					capsulesToRemove.add(capsule);
					LazUtils.InventoryCore.AddToInventory((IInventory) tile, (ItemStack) capsule.GetContents());
				}
				else if(tile instanceof ITubeConnectible && ((ITubeConnectible)tile).CanAccept(capsule))
				{
					capsulesToRemove.add(capsule);
					((ITubeConnectible)tile).AcceptCapsule(capsule);
				}
			}
		}
		for(ICapsule capsule : capsulesToRemove)
		{
			contents.remove(capsule);
		}
	}

	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("colour",colour.ordinal());
		nbt.setInteger("pressure",pressure);
		NBTTagList taglist = new NBTTagList();
		for(ICapsule capsule : contents)
		{
			taglist.appendTag(YATSRegistry.getCapsuleNBT(capsule));
		}
		nbt.setTag("contents",taglist);
	}

	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		colour = Colours.values()[nbt.getInteger("colour")];
		pressure = nbt.getInteger("pressure");
		NBTTagList list = nbt.getTagList("contents");
		for(int i = 0; i < list.tagCount();i++)
		{
			contents.add(YATSRegistry.handleCapsuleNBT((NBTTagCompound)list.tagAt(i)));
		}
	}

	public ArrayList<ForgeDirection> GetConnectedSides()
	{
		ArrayList<ForgeDirection> result = new ArrayList<ForgeDirection>();
		for(int i = 0;i < 6;i++)
		{
			if(isConnectedOnSide[i]) result.add(ForgeDirection.getOrientation(i));
		}
		return result;
	}
}

package YATS.api;

import YATS.util.Colours;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;

/**
 * Created with IntelliJ IDEA.
 * User: Alex
 * Date: 6/13/13
 * Time: 1:47 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ICapsule
{
	public Object GetContents();
	public Colours GetColour();
	public void SetColour(Colours colour);
	public ForgeDirection GetHeading();
	public void SetHeading(ForgeDirection heading);
	public ICapsuleRenderer GetRenderer();
	public float getProgress();
	public void addProgress(float progress);
	public void resetProgress();
	public NBTTagCompound getNBT();
	public void loadFromNBT(NBTTagCompound nbt);
}

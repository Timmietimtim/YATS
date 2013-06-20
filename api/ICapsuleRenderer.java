package YATS.api;

import YATS.tile.TileTube;

public interface ICapsuleRenderer
{
	public void RenderCapsule(TileTube tube, ICapsule capsule, double x, double y, double z);
}

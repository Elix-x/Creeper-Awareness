package code.elix_x.mods.avoidexplodingcreepers.api;

import code.elix_x.mods.avoidexplodingcreepers.api.events.GetExplosionSourceFromEntityEvent;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/**
 * Implement this interface on {@link Entity} to make it valid explosion source.<br>
 * If you cannot implement this on {@link Entity}, but want to make it valid explosion source, subscribe to {@link GetExplosionSourceFromEntityEvent}, and change {@link GetExplosionSourceFromEntityEvent#explosionSource} field to valid explosion source.
 * @author elix_x
 *
 */
public interface IExplosionSource {
	
	/**
	 * {@link Entity} that this explosion source handles.<br>
	 * <b>Can</b> be null.
	 * @return
	 */
	public Entity getHandledEntity();
	
	public World getWorldObj();

	public double getXPos();
	
	public double getYPos();
	
	public double getZPos();
	
	public boolean isExploding();
	
	public int getTimeBeforeExplosion();
	
	public double getRange();
	
	/**
	 * Use default update method?
	 * @return true to use default update (tick) method or false to handle update (tick) yourself in {@link #update()}.
	 */
	public boolean doDefaultUpdate();
	
	/**
	 * Handle update (tick) yourself, if you don't like default one.<br>
	 * Will only be called if {@link #doDefaultUpdate()} returns false.
	 */
	public void update();
	
	/**
	 * Whether or not this source is still valid and should be updated.
	 * @return is this source valid and should be updated.
	 */
	public boolean isValid();
	
}

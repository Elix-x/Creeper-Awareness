package code.elix_x.mods.creeperawareness.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import code.elix_x.excore.utils.shape3d.Shape3D;
import code.elix_x.mods.creeperawareness.api.events.GetExplosionSourceFromEntityEvent;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/**
 * Implement this interface on {@link Entity} to make it valid explosion source.
 * <br>
 * If you cannot implement this on {@link Entity}, but want to make it valid
 * explosion source (or want to change explosion source of entity already
 * implementing {@link IExplosionSource}), subscribe to
 * {@link GetExplosionSourceFromEntityEvent}, and change
 * {@link GetExplosionSourceFromEntityEvent#explosionSource} to explosion source
 * you want it to be. <br>
 * If you want to create explosion source in world, which is not an entity, call
 * {@link ExplosionSrcManager#addExplosionSource(IExplosionSource)} with
 * explosion source to add to the world.
 * 
 * @author elix_x
 *
 */
public interface IExplosionSource {

	/**
	 * {@link Entity} that this explosion source handles.<br>
	 * <b>Can</b> be null.
	 * 
	 * @return
	 */
	@Nullable
	public Entity getHandledEntity();

	@Nonnull
	public World getWorldObj();

	public boolean isExploding();

	public int getTimeBeforeExplosion();

	public Shape3D getExplosionShape();

	public int getExplosionRadius();

	public boolean update();

	/**
	 * If this source has changed since last update.
	 * 
	 * @return whether or not all entities should be reprocessed.
	 */
	public boolean isDirty();

	/**
	 * Marks this source as being dirty / not dirty.
	 * 
	 * @param dirty
	 *            - new value
	 */
	public boolean setDirty(boolean dirty);

	/**
	 * Whether or not this source is still valid and should be updated. <br>
	 * If source becomes invalid, it will be thrown away and garbage collected.
	 * 
	 * @return is this source valid and should be updated.
	 */
	public boolean isValid();

}

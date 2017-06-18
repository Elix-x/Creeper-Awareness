package code.elix_x.mods.creeperawareness.api;

import code.elix_x.excore.utils.shape3d.Shape3D;
import code.elix_x.mods.creeperawareness.ExplosionSourcesManager;
import net.minecraft.entity.Entity;

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
 * {@link ExplosionSourcesManager#addExplosionSource(IExplosionSource)} with
 * explosion source to add to the world.
 * 
 * @author Elix_x
 *
 */
public interface IExplosionSource {

	boolean isExploding();

	int getTimeBeforeExplosion();

	Shape3D getExplosionShape();

	@Deprecated //Remove in future release
	int getExplosionRadius();

	boolean affectsEntity(Entity entity);

	/**
	 * If this source has changed since last call.
	 * 
	 * @return whether or not all entities should be reprocessed.
	 */
	boolean hasChanged();

	/**
	 * Marks this source as being dirty / not dirty.<br>
	 * <br>
	 * Called by explosion sources manager
	 * 
	 * @param dirty - new value
	 *
	boolean setDirty(boolean dirty);*/

	/**
	 * Whether or not this source is still valid and should be updated. <br>
	 * If source becomes invalid, it will be thrown away and garbage collected.
	 * 
	 * @return is this source valid and should be updated.
	 */
	boolean isValid();

}

package code.elix_x.mods.creeperawareness.api.events;

import code.elix_x.mods.creeperawareness.api.IExplosionSource;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * Event that allows you to bind {@link IExplosionSource} to {@link Entity} that does not implement {@link IExplosionSource}.
 * <br>
 * Simply change {@link #explosionSource} to one that you want to be bound to that entity.
 * <br>
 * This event is fired on {@link MinecraftForge#EVENT_BUS}.
 * <br>
 * This event is not {@link Cancelable}.
 * @author elix_x
 *
 */
public class GetExplosionSourceFromEntityEvent extends EntityEvent {

	public IExplosionSource explosionSource;

	public GetExplosionSourceFromEntityEvent(Entity entity, IExplosionSource explosionSource){
		super(entity);
		this.explosionSource = explosionSource;
	}

}

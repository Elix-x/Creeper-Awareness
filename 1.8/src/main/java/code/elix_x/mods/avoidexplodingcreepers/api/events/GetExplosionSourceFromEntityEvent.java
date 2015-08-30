package code.elix_x.mods.avoidexplodingcreepers.api.events;

import code.elix_x.mods.avoidexplodingcreepers.api.IExplosionSource;
import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;

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
public class GetExplosionSourceFromEntityEvent extends EntityEvent{

	public IExplosionSource explosionSource;
	
	public GetExplosionSourceFromEntityEvent(Entity entity) {
		super(entity);
	}

}

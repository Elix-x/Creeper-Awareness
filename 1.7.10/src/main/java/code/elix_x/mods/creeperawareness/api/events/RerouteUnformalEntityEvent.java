package code.elix_x.mods.creeperawareness.api.events;

import code.elix_x.mods.creeperawareness.api.IExplosionSource;
import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;

/**
 * This event is allows you to reroute manually entities that aren't {@link EntityCreature} from given {@link IExplosionSource}.
 * <br>
 * <br>
 * This event is fired on {@link MinecraftForge.EVENT_BUS}.
 * <br>
 * This event is not {@link Cancelable}.
 * @author elix_x
 *
 */
public class RerouteUnformalEntityEvent extends EntityEvent {

	public IExplosionSource source;

	public RerouteUnformalEntityEvent(IExplosionSource source, Entity entity){
		super(entity);
		this.source = source;
	}


}
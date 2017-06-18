package code.elix_x.mods.creeperawareness.api.events;

import code.elix_x.mods.creeperawareness.api.IExplosionSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * This event is allows you to reroute manually entities that aren't
 * {@link EntityCreature} from given {@link IExplosionSource}. <br>
 * <br>
 * This event is fired on {@link MinecraftForge#EVENT_BUS}. <br>
 * This event is not {@link Cancelable}.
 * 
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
package code.elix_x.mods.creeperawareness.api.events;

import code.elix_x.mods.creeperawareness.api.IExplosionSource;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

import java.util.stream.Stream;

/**
 * This event is allows you to path find entities from given {@link IExplosionSource}s.<br>
 * Default logic is run with {@linkplain EventPriority#LOWEST}. Cancel this event to prevent default logic.
 * <br>
 * This event is fired on {@link MinecraftForge#EVENT_BUS}. <br>
 * This event is {@link Cancelable}.
 * 
 * @author elix_x
 *
 */
@Cancelable
public class PathfindEntityEvent extends EntityEvent {

	private Stream<IExplosionSource> sources;

	public PathfindEntityEvent(Entity entity, Stream<IExplosionSource> sources){
		super(entity);
		this.sources = sources;
	}

	public Stream<IExplosionSource> getSources(){
		return sources;
	}

}
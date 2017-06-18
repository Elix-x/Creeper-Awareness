package code.elix_x.mods.creeperawareness;

import code.elix_x.mods.creeperawareness.api.IExplosionSource;
import code.elix_x.mods.creeperawareness.api.IExplosionSourcesManager;
import code.elix_x.mods.creeperawareness.api.events.PathfindEntityEvent;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.lang.ref.WeakReference;
import java.util.*;

public class ExplosionSourcesManager implements IExplosionSourcesManager {

	private World world;
	private Queue<IExplosionSource> newOrNotExplody = new LinkedList<>();
	private Multimap<IExplosionSource, WeakReference<Entity>> sources = HashMultimap.create();
	private Multimap<WeakReference<Entity>, IExplosionSource> entity2sources = HashMultimap.create();

	@Override
	public void addExplosionSource(IExplosionSource source){
		newOrNotExplody.add(source);
	}

	void tick(World wworld){
		if(world == null) world = wworld;
		assert wworld == world : "Cannot tick this explosion sources manager on a different world.";
		sources.keySet().removeIf(source -> !source.isValid());
		Iterator<IExplosionSource> iterator = newOrNotExplody.iterator();
		while(iterator.hasNext()){
			IExplosionSource next = iterator.next();
			if(next.isExploding()){
				process(next);
				iterator.remove();
			}
		}
		List<IExplosionSource> forRemoval = new ArrayList<>();
		for(IExplosionSource source : sources.keySet()){
			if(!source.isExploding()) forRemoval.add(source);
			else {
				if(source.hasChanged()) sources.removeAll(source);
				process(source);
				sources.get(source).removeIf(reference -> reference.get() == null);
			}
		}
		for(IExplosionSource remove : forRemoval){
			newOrNotExplody.add(remove);
			sources.removeAll(remove);
		}
		entity2sources.keySet().removeIf(reference -> reference.get() == null);
		entity2sources.keySet().forEach(entity -> MinecraftForge.EVENT_BUS.post(new PathfindEntityEvent(entity.get(), entity2sources.get(entity).stream().filter(source -> source.isValid() && source.isExploding()))));
	}

	private void process(IExplosionSource source){
		source.getExplosionShape().getAffectedEntities(world, Entity.class).stream().filter(entity -> !sources.containsEntry(source, entity) && source.affectsEntity(entity)).forEach(entity -> processEntity(source, entity));
	}

	private void processEntity(IExplosionSource source, Entity entity){
		sources.put(source, new WeakReference<>(entity));
		entity2sources.put(new WeakReference<>(entity), source);
	}

}

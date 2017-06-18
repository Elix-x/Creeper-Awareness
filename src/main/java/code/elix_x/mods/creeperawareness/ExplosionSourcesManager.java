package code.elix_x.mods.creeperawareness;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import code.elix_x.mods.creeperawareness.api.IExplosionSource;
import code.elix_x.mods.creeperawareness.api.IExplosionSourcesManager;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import code.elix_x.excore.utils.shape3d.AxisAlignedBox;
import code.elix_x.excore.utils.shape3d.Shape3D;
import code.elix_x.mods.creeperawareness.api.events.RerouteUnformalEntityEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

public class ExplosionSourcesManager implements IExplosionSourcesManager {

	public static final Logger logger = LogManager.getLogger("Creeper Awareness Explosions Mananger");

	private World world;

	public ExplosionSourcesManager(){
	}

	@Override
	public void addExplosionSource(IExplosionSource source){

	}

	void tick(World wworld){
		if(world == null) world = wworld;
		assert wworld == world : "Cannot tick this explosion sources manager on a different world.";

	}

	/*private static Map<Entity, IExplosionSource> entitySourceMap = new HashMap<Entity, IExplosionSource>();
	private static List<IExplosionSource> specialSources = new ArrayList<IExplosionSource>();

	public void addExplosionSource(IExplosionSource source){
		if(!entitySourceMap.containsValue(source)) specialSources.add(source);
	}

	private void tick(){
		Iterator<IExplosionSource> it = entitySourceMap.values().iterator();
		while(it.hasNext()){
			IExplosionSource source = it.next();
			if(!source.isValid()){
				it.remove();
				sourceEntitiesMap.removeAll(source);
			}
		}
		it = specialSources.iterator();
		while(it.hasNext()){
			IExplosionSource source = it.next();
			if(!source.isValid()){
				it.remove();
				sourceEntitiesMap.removeAll(source);
			}
		}
		for(IExplosionSource source : specialSources){
			processSource(source);
		}
	}

	private void tick(World world){
		for(int i = 0; i < world.loadedEntityList.size(); i++){
			Entity entity = world.loadedEntityList.get(i);
			IExplosionSource source = entitySourceMap.get(entity);
			if(source != null){
				processSource(source);
			} else{
				if(entity instanceof IExplosionSource){
					source = (IExplosionSource) entity;
				}

				if(source != null){
					entitySourceMap.put(entity, source);
					processSource(source);
				}
			}
		}
	}

	private Multimap<IExplosionSource, Entity> sourceEntitiesMap = HashMultimap.create();

	private void processSource(IExplosionSource source){
		if(source.isValid()){
			if(source.update()){
				if(source.isExploding()){
					for(Entity entity : source.getExplosionShape().getAffectedEntities(source.getWorldObj(), Entity.class)){
						if(entity != source.getHandledEntity() && (source.isDirty() || !sourceEntitiesMap.containsEntry(source, entity))){
							sourceEntitiesMap.put(source, entity);
							processEntity(source, entity);
						}
					}
					source.setDirty(false);
				}
			}
		}
	}

	private void processEntity(IExplosionSource source, Entity e){
		if(e instanceof EntityPlayer) return;
		if(e instanceof EntityCreature){
			EntityCreature entity = (EntityCreature) e;
			Vec3d vec3;
			Shape3D shape = source.getExplosionShape();
			Vec3d sourcePos = shape.getPos();
			if(smartMobs){
				Vec3d ePos = new Vec3d(entity.posX, entity.posY, entity.posZ);
				Vec3d vec = ePos.subtract(sourcePos).normalize();
				Vec3d out = new Vec3d(0, 0, 0);
				AxisAlignedBB box = entity.getEntityBoundingBox();
				while(box != null ? shape.intersectsWith(source.getWorldObj(), new AxisAlignedBox(box.offset(out.x, out.y, out.z))) : shape.isInside(source.getWorldObj(), ePos.addVector(out.x, out.y, out.z))){
					out = out.addVector(vec.x, vec.y, vec.z);
				}
				double x = ePos.x + out.x;
				double y = ePos.y + out.y;
				double z = ePos.z + out.z;
				for(int i = 0; source.getWorldObj().getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.AIR; i = i == 0 ? 1 : i > 0 ? -i : -i + 1){
					y = ePos.y + out.y + i;
					if(i < 0 || i > 255){
						y = source.getWorldObj().getHeight(new BlockPos(x, y, z)).getY();
						break;
					}
				}
				vec3 = new Vec3d(x, y, z);
			} else{
				shape = shape.getBounds();
				vec3 = RandomPositionGenerator.findRandomTargetBlockAwayFrom(entity, source.getExplosionRadius(), source.getExplosionRadius(), sourcePos);
			}
			double eb = entity.getDistanceSq(vec3.x, vec3.y, vec3.z);
			double speed = eb / (source.getTimeBeforeExplosion() / 2.5);

			PathNavigate navigator = entity.getNavigator();
			navigator.clearPathEntity();
			navigator.tryMoveToXYZ(vec3.x, vec3.y, vec3.z, speed);
		} else{
			MinecraftForge.EVENT_BUS.post(new RerouteUnformalEntityEvent(source, e));
		}
	}*/
}

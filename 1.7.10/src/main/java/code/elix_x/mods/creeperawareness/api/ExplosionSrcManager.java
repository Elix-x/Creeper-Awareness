package code.elix_x.mods.creeperawareness.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import code.elix_x.excore.utils.shape3d.AxisAlignedBox;
import code.elix_x.excore.utils.shape3d.Shape3D;
import code.elix_x.mods.creeperawareness.api.events.GetExplosionSourceFromEntityEvent;
import code.elix_x.mods.creeperawareness.api.events.RerouteUnformalEntityEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

public class ExplosionSrcManager {

	public static final Logger logger = LogManager.getLogger("Creeper Awareness Explosions Mananger");

	public static File configFolder;
	public static File configFile;
	public static Configuration config;

	public static boolean smartMobs = true;

	public static void preInit(FMLPreInitializationEvent event){
		configFolder = new File(event.getModConfigurationDirectory(), "Creeper Awareness");
		configFolder.mkdir();
		configFile = new File(configFolder, "API.cfg");
		try {
			configFile.createNewFile();
		} catch (IOException e){
			logger.error("Caught exception while creating config file: ", e);
		}
		config = new Configuration(configFile);
		config.load();
		smartMobs = config.getBoolean("smartMobs", "Mobs", true, "Mobs know vector math and calculate exact runaway position using it.\nIf false, mobs don't know vector math and run to random block away...");
		config.save();
	}

	public static void init(FMLInitializationEvent event){
		FMLCommonHandler.instance().bus().register(new TickEvent());
	}

	public static void postInit(FMLPostInitializationEvent event){

	}

	public static void serverStopping(FMLServerStoppingEvent event){
		entitySourceMap.clear();
		specialSources.clear();
		sourceEntitiesMap.clear();
	}

	public static class TickEvent {

		@SubscribeEvent
		public void onTickWorld(WorldTickEvent event){
			if(!event.world.isRemote && event.phase == Phase.START){
				tick(event.world);
			}
		}

		@SubscribeEvent
		public void onTickServer(ServerTickEvent event){
			if(event.phase == Phase.START){
				tick();
			}
		}

	}

	private static Map<Entity, IExplosionSource> entitySourceMap = new HashMap<Entity, IExplosionSource>();
	private static List<IExplosionSource> specialSources = new ArrayList<IExplosionSource>();

	public static void addExplosionSource(IExplosionSource source){
		if(!entitySourceMap.containsValue(source)) specialSources.add(source);
	}

	private static void tick(){
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

	private static void tick(World world){
		for(Object o : world.loadedEntityList){
			Entity entity = (Entity) o;
			IExplosionSource source = entitySourceMap.get(entity);
			if(source != null){
				processSource(source);
			} else { 
				if(entity instanceof IExplosionSource){
					source = (IExplosionSource) entity;
				}
				GetExplosionSourceFromEntityEvent event = new GetExplosionSourceFromEntityEvent(entity, source);
				MinecraftForge.EVENT_BUS.post(event);
				source = event.explosionSource;

				if(source != null){
					entitySourceMap.put(entity, source);
					processSource(source);
				}
			}
		}
	}

	private static Multimap<IExplosionSource, Entity> sourceEntitiesMap = HashMultimap.create();

	private static void processSource(IExplosionSource source){
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

	private static void processEntity(IExplosionSource source, Entity e){
		if(e == source.getHandledEntity()) return;
		if(e instanceof EntityPlayer) return;
		if(e instanceof EntityCreature){
			EntityCreature entity = (EntityCreature) e;
			Vec3 vec3;
			Shape3D shape = source.getExplosionShape();
			Vec3 sourcePos = shape.getPos();
			if(smartMobs){
				Vec3 ePos = Vec3.createVectorHelper(entity.posX, entity.posY, entity.posZ);
				Vec3 vec = ePos.subtract(sourcePos).normalize();
				Vec3 out = Vec3.createVectorHelper(0, 0, 0);
				AxisAlignedBB box = entity.boundingBox;
				while(box != null ? shape.intersectsWith(source.getWorldObj(), new AxisAlignedBox(box.copy().offset(out.xCoord, out.yCoord, out.zCoord))) : shape.isInside(source.getWorldObj(), ePos.addVector(out.xCoord, out.yCoord, out.zCoord))){
					out = out.addVector(vec.xCoord, vec.yCoord, vec.zCoord);
				}
				double x = ePos.xCoord + out.xCoord;
				double y = ePos.yCoord + out.yCoord;
				double z = ePos.zCoord + out.zCoord;
				for(int i = 0; source.getWorldObj().getBlock((int) x, (int) y, (int) z) != Blocks.air; i = i == 0 ? 1 : i > 0 ? -i : -i + 1){
					y = ePos.yCoord + out.yCoord + i;
					if(i < 0 || i > 255){
						y = source.getWorldObj().getHeightValue((int) x, (int) z);
						break;
					}
				}
				vec3 = Vec3.createVectorHelper(x, y, z);
			} else {
				shape = shape.getBounds();
				vec3 = RandomPositionGenerator.findRandomTargetBlockAwayFrom(entity, source.getExplosionRadius(), source.getExplosionRadius(), sourcePos);
			}
			double eb = entity.getDistanceSq(vec3.xCoord, vec3.yCoord, vec3.zCoord);
			double speed = eb / (source.getTimeBeforeExplosion() / 2.5);

			PathNavigate navigator = entity.getNavigator();
			navigator.clearPathEntity();
			navigator.tryMoveToXYZ(vec3.xCoord, vec3.yCoord, vec3.zCoord, speed);
		} else {
			MinecraftForge.EVENT_BUS.post(new RerouteUnformalEntityEvent(source, e));
		}
	}
}

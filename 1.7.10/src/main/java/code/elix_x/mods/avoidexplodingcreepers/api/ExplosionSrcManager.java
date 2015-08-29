package code.elix_x.mods.avoidexplodingcreepers.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import code.elix_x.mods.avoidexplodingcreepers.api.events.GetExplosionSourceFromEntityEvent;
import code.elix_x.mods.avoidexplodingcreepers.api.events.RerouteUnformalEntityEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;

public class ExplosionSrcManager {

	public static final Logger logger = LogManager.getLogger("AEC Explosions Mananger");

	public static File configFile;
	public static Configuration config;

	public static boolean smartMobs = true;

	public static void preInit(FMLPreInitializationEvent event)
	{
		configFile = new File(event.getModConfigurationDirectory(), "AvoidExplodingCreepers/API.cfg");
		try {
			configFile.createNewFile();
		} catch (IOException e) {
			logger.error("Caught exception while creating config file: ", e);
		}
		config = new Configuration(configFile);
		config.load();
		smartMobs = config.getBoolean("smartMobs", "Mobs", true, "Mobs know vector math and calculate exact runaway position using it.\nIf false, mobs don't know vector math and run to random block away...");
		config.save();
	}

	public static void init(FMLInitializationEvent event)
	{
		FMLCommonHandler.instance().bus().register(new OnWorldTickEvent());
	}

	public static void postInit(FMLPostInitializationEvent event)
	{

	}

	public static class OnWorldTickEvent {

		public OnWorldTickEvent() {

		}

		@SubscribeEvent
		public void onTickWorld(WorldTickEvent event){
			if(event.phase == Phase.START){
				tick(event.world);
			}
		}
		
		@SubscribeEvent
		public void onTickServer(ServerTickEvent event){
			if(event.phase == Phase.START){
				tickServer();
			}
		}

	}

	private static Map<Entity, IExplosionSource> entitySourceMap = new HashMap<Entity, IExplosionSource>();
	private static List<IExplosionSource> specialSources = new ArrayList<IExplosionSource>();

	private static void tickServer(){
		Iterator<IExplosionSource> it = entitySourceMap.values().iterator();
		while(it.hasNext()){
			if(!it.next().isValid()){
				it.remove();
			}
		}
		it = specialSources.iterator();
		while(it.hasNext()){
			if(!it.next().isValid()){
				it.remove();
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
				} else {
					GetExplosionSourceFromEntityEvent event = new GetExplosionSourceFromEntityEvent(entity);
					MinecraftForge.EVENT_BUS.post(event);
					source = event.explosionSource;
				}

				if(source != null){
					entitySourceMap.put(entity, source);
					processSource(source);
				}
			}
		}
	}

	private static Map<IExplosionSource, List<Entity>> sourceEntitiesMap = new HashMap<IExplosionSource, List<Entity>>();

	private static void processSource(IExplosionSource source) {
		if(source.isValid()){
			if(source.doDefaultUpdate()){
				if(source.isExploding()){
					AxisAlignedBB range = AxisAlignedBB.getBoundingBox(source.getXPos() - source.getRange() * 1.5, source.getYPos() - source.getRange() * 1.5, source.getZPos() - source.getRange() * 1.5, source.getXPos() + source.getRange() * 1.5, source.getYPos() + source.getRange() * 1.5, source.getZPos() + source.getRange() * 1.5);
					List<Entity> all = source.getWorldObj().getEntitiesWithinAABBExcludingEntity(source.getHandledEntity(), range);
					List<Entity> not = sourceEntitiesMap.get(source);
					if(not == null){
						not = new ArrayList<Entity>();
					}
					for(Entity entity : all){
						if(!not.contains(entity)){
							not.add(entity);
							processEntity(source, entity);
						}
					}
					sourceEntitiesMap.put(source, not);
				}
			} else {
				source.update();
			}
		}
	}

	private static void processEntity(IExplosionSource source, Entity e) {
		if(e == source.getHandledEntity()) return;
		if(e instanceof EntityPlayer) return;
		if(e instanceof EntityCreature){
			EntityCreature entity = (EntityCreature) e;
			Vec3 vec3;
			if(smartMobs){
				double ddd = 2.5;
				AxisAlignedBB range = AxisAlignedBB.getBoundingBox(source.getXPos() - source.getRange() * 1.5 - ddd, source.getYPos() - source.getRange() * 1.5 - ddd, source.getZPos() - source.getRange() * 1.5 - ddd, source.getXPos() + source.getRange() * 1.5 + ddd, source.getYPos() + source.getRange() * 1.5 + ddd, source.getZPos() + source.getRange() * 1.5 + ddd);
				Vec3 vec = Vec3.createVectorHelper(e.posX - source.getXPos(), e.posY - source.getYPos(), e.posZ - source.getZPos()).normalize();
				Vec3 out = Vec3.createVectorHelper(source.getXPos(), source.getYPos(), source.getZPos());
				while(range.isVecInside(out)){
					out.xCoord += vec.xCoord;
					out.yCoord += vec.yCoord;
					out.zCoord += vec.zCoord;
				}
				double x = out.xCoord;
				double y = out.yCoord;
				double z = out.zCoord;
				if(source.getWorldObj().getBlock((int) x, (int) y, (int) z) != Blocks.air){
					y = source.getWorldObj().getHeightValue((int) x, (int) z);
				}
				vec3 = Vec3.createVectorHelper(x, y, z);
			} else {
				vec3 = RandomPositionGenerator.findRandomTargetBlockAwayFrom(entity, (int) (source.getRange() * 5), (int) (source.getRange() * 1.5), Vec3.createVectorHelper(source.getXPos(), source.getYPos(), source.getZPos()));
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

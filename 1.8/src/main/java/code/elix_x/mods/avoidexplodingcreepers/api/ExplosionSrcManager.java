package code.elix_x.mods.avoidexplodingcreepers.api;

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

import code.elix_x.mods.avoidexplodingcreepers.api.events.GetExplosionSourceFromEntityEvent;
import code.elix_x.mods.avoidexplodingcreepers.api.events.RerouteUnformalEntityEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

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
		FMLCommonHandler.instance().bus().register(new TickEvent());
	}

	public static void postInit(FMLPostInitializationEvent event)
	{

	}

	public static void serverStopped(FMLServerStoppingEvent event){
		entitySourceMap.clear();
		specialSources.clear();
		sourceEntitiesMap.clear();
	}
	
	public static class TickEvent {

		public TickEvent() {

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
				tick();
			}
		}

	}

	private static Map<Entity, IExplosionSource> entitySourceMap = new HashMap<Entity, IExplosionSource>();
	private static List<IExplosionSource> specialSources = new ArrayList<IExplosionSource>();

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

	private static Multimap<IExplosionSource, Entity> sourceEntitiesMap = HashMultimap.create();

	private static void processSource(IExplosionSource source) {
		if(source.isValid()){
			if(source.doDefaultUpdate()){
				if(source.isExploding()){
					AxisAlignedBB range = new AxisAlignedBB(source.getXPos() - source.getRange() * 1.5, source.getYPos() - source.getRange() * 1.5, source.getZPos() - source.getRange() * 1.5, source.getXPos() + source.getRange() * 1.5, source.getYPos() + source.getRange() * 1.5, source.getZPos() + source.getRange() * 1.5);
					List<Entity> all = source.getWorldObj().getEntitiesWithinAABBExcludingEntity(source.getHandledEntity(), range);
					for(Entity entity : all){
						if(!sourceEntitiesMap.containsEntry(source, entity)){
							sourceEntitiesMap.put(source, entity);
							processEntity(source, entity);
						}
					}
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
				AxisAlignedBB range = new AxisAlignedBB(source.getXPos() - source.getRange() * 1.5 - ddd, source.getYPos() - source.getRange() * 1.5 - ddd, source.getZPos() - source.getRange() * 1.5 - ddd, source.getXPos() + source.getRange() * 1.5 + ddd, source.getYPos() + source.getRange() * 1.5 + ddd, source.getZPos() + source.getRange() * 1.5 + ddd);
				Vec3 vec = new Vec3(e.posX - source.getXPos(), e.posY - source.getYPos(), e.posZ - source.getZPos()).normalize();
				Vec3 out = new Vec3(source.getXPos(), source.getYPos(), source.getZPos());
				while(range.isVecInside(out)){
					out = out.add(vec);
				}
				double x = out.xCoord;
				double y = out.yCoord;
				double z = out.zCoord;
				if(source.getWorldObj().getBlockState(new BlockPos(out)) != Blocks.air.getDefaultState()){
					y = source.getWorldObj().getHorizon(new BlockPos(out)).getY();
				}
				vec3 = new Vec3(x, y, z);
			} else {
				vec3 = RandomPositionGenerator.findRandomTargetBlockAwayFrom(entity, (int) (source.getRange() * 5), (int) (source.getRange() * 1.5), new Vec3(source.getXPos(), source.getYPos(), source.getZPos()));
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

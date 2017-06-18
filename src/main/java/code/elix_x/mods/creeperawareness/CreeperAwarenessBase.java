package code.elix_x.mods.creeperawareness;

import code.elix_x.excore.EXCore;
import code.elix_x.mods.creeperawareness.api.IExplosionSourcesManager;
import code.elix_x.mods.creeperawareness.events.BindCreeperEvent;
import code.elix_x.mods.creeperawareness.events.BindTntEvent;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

@Mod.EventBusSubscriber
@Mod(modid = CreeperAwarenessBase.MODID, name = CreeperAwarenessBase.NAME, version = CreeperAwarenessBase.VERSION, dependencies = "required-after:" + EXCore.DEPENDENCY, acceptedMinecraftVersions = EXCore.MCVERSIONDEPENDENCY, acceptableRemoteVersions = "*")
public class CreeperAwarenessBase {

	public static final String MODID = "creeperawareness";
	public static final String NAME = "Creeper Awareness";
	public static final String VERSION = "@VERSION@";

	public static final Logger logger = LogManager.getLogger(NAME);

	@CapabilityInject(IExplosionSourcesManager.class)
	public static Capability<ExplosionSourcesManager> managerCapability;

	public Configuration apiConfig;
	private Configuration coreConfig;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event){
		File configFolder = new File(event.getModConfigurationDirectory(), CreeperAwarenessBase.NAME);
		configFolder.mkdir();

		File apiConfigFile = new File(configFolder, "API.cfg");
		try{
			apiConfigFile.createNewFile();
		} catch(IOException e){
			logger.error("Caught exception while creating apiConfig file: ", e);
		}
		apiConfig = new Configuration(apiConfigFile);
		apiConfig.load();
		//smartMobs = apiConfig.getBoolean("smartMobs", "Mobs", true, "Mobs know vector math and calculate exact runaway position using it.\nIf false, mobs don't know vector math and run to random block away...");
		apiConfig.save();

		File coreConfigFile = new File(configFolder, "Explosion Sources.cfg");
		try{
			coreConfigFile.createNewFile();
		} catch(IOException e){
			logger.error("Caught exception while creating coreConfig file: ", e);
		}
		coreConfig = new Configuration(coreConfigFile);

		coreConfig.load();
		if(coreConfig.getBoolean("creeper", "Vanilla", true, "Should creepers be valid explosion sources?")){
			MinecraftForge.EVENT_BUS.register(new BindCreeperEvent());
		}
		if(coreConfig.getBoolean("tnt", "Vanilla", true, "Should tnts be valid explosion sources?")){
			MinecraftForge.EVENT_BUS.register(new BindTntEvent());
		}
		coreConfig.save();

		CapabilityManager.INSTANCE.register(IExplosionSourcesManager.class, new Capability.IStorage<IExplosionSourcesManager>() {

			@Nullable
			@Override
			public NBTBase writeNBT(Capability<IExplosionSourcesManager> capability, IExplosionSourcesManager instance, EnumFacing side){
				return null;
			}

			@Override
			public void readNBT(Capability<IExplosionSourcesManager> capability, IExplosionSourcesManager instance, EnumFacing side, NBTBase nbt){

			}

		}, ExplosionSourcesManager.class);
	}

	@EventHandler
	public void init(FMLInitializationEvent event){

	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event){

	}

	@SubscribeEvent
	public void tick(TickEvent.WorldTickEvent event){
		if(event.phase == TickEvent.Phase.START && !event.world.isRemote) event.world.getCapability(managerCapability, null).tick(event.world);
	}

}
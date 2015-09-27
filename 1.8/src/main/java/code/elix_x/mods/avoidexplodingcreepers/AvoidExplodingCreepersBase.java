package code.elix_x.mods.avoidexplodingcreepers;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import code.elix_x.mods.avoidexplodingcreepers.api.ExplosionSrcManager;
import code.elix_x.mods.avoidexplodingcreepers.events.BindCreeperEvent;
import code.elix_x.mods.avoidexplodingcreepers.events.BindTntEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;

@Mod(modid = "avoidexplodingcreepers", version = "2.0", name = "Avoid Exploding Creepers"/*, dependencies = "required-after:excore"*/, acceptableRemoteVersions = "*", acceptedMinecraftVersions = "1.8")
public class AvoidExplodingCreepersBase {

	public static final String MODID = "avoidexplodingcreepers";
	public static final String NAME = "Avoid Exploding Creepers";
	public static final String VERSION = "2.0";
	
	public static final Logger logger = LogManager.getLogger("AEC");
	
	public static File configFile;
	public static Configuration config;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		ExplosionSrcManager.preInit(event);
		
		configFile = new File(event.getModConfigurationDirectory(), "AvoidExplodingCreepers/Explosion Sources.cfg");
		try {
			configFile.createNewFile();
		} catch (IOException e) {
			logger.error("Caught exception while creating config file: ", e);
		}
		config = new Configuration(configFile);
		
		config.load();
		if(config.getBoolean("creeper", "Vanilla", true, "Should creepers be valid explosion sources?")){
			MinecraftForge.EVENT_BUS.register(new BindCreeperEvent());
		}
		if(config.getBoolean("tnt", "Vanilla", true, "Should tnts be valid explosion sources?")){
			MinecraftForge.EVENT_BUS.register(new BindTntEvent());
		}
		config.save();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		ExplosionSrcManager.init(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		ExplosionSrcManager.postInit(event);
	}
	
	@EventHandler
	public void serverStopped(FMLServerStoppedEvent event){
		ExplosionSrcManager.serverStopped(event);
	}
	
}
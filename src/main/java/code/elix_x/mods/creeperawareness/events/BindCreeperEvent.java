package code.elix_x.mods.creeperawareness.events;

import code.elix_x.excore.utils.shape3d.Shape3D;
import code.elix_x.excore.utils.shape3d.Sphere;
import code.elix_x.mods.creeperawareness.CreeperAwarenessBase;
import code.elix_x.mods.creeperawareness.api.IExplosionSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BindCreeperEvent {

	@SubscribeEvent
	public void bind(EntityJoinWorldEvent event){
		if(!event.getWorld().isRemote && event.getEntity() instanceof EntityCreeper){
			final EntityCreeper creeper = (EntityCreeper) event.getEntity();
			event.getWorld().getCapability(CreeperAwarenessBase.managerCapability, null).addExplosionSource(new IExplosionSource(){

				private boolean prevExplode = false;

				@Override
				public boolean isExploding(){
					return creeper.hasIgnited();
				}

				@Override
				public int getTimeBeforeExplosion(){
					return (Integer) ObfuscationReflectionHelper.getPrivateValue(EntityCreeper.class, creeper, "fuseTime", "field_82225_f") - (Integer) ObfuscationReflectionHelper.getPrivateValue(EntityCreeper.class, creeper, "timeSinceIgnited", "field_70833_d");
				}

				@Override
				public int getExplosionRadius(){
					return (Integer) ObfuscationReflectionHelper.getPrivateValue(EntityCreeper.class, creeper, "explosionRadius", "field_82226_g") * (creeper.getPowered() ? 4 : 2);
				}

				@Override
				public Shape3D getExplosionShape(){
					return new Sphere(creeper.posX, creeper.posY, creeper.posZ, getExplosionRadius());
				}

				@Override
				public boolean affectsEntity(Entity entity){
					return entity != creeper;
				}

				@Override
				public boolean hasChanged(){
					boolean changed = creeper.posX != creeper.prevPosX || creeper.posY != creeper.prevPosY || creeper.posZ != creeper.prevPosZ || isExploding() != prevExplode;
					prevExplode = isExploding();
					return changed;
				}

				@Override
				public boolean isValid(){
					return creeper != null && !creeper.isDead;
				}

			});
		}
	}

}

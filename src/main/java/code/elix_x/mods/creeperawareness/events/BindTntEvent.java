package code.elix_x.mods.creeperawareness.events;

import code.elix_x.excore.utils.shape3d.Shape3D;
import code.elix_x.excore.utils.shape3d.Sphere;
import code.elix_x.mods.creeperawareness.CreeperAwarenessBase;
import code.elix_x.mods.creeperawareness.api.IExplosionSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BindTntEvent {

	@SubscribeEvent
	public void bind(EntityJoinWorldEvent event){
		if(!event.getWorld().isRemote && event.getEntity() instanceof EntityTNTPrimed){
			final EntityTNTPrimed tnt = (EntityTNTPrimed) event.getEntity();
			event.getWorld().getCapability(CreeperAwarenessBase.managerCapability, null).addExplosionSource(new IExplosionSource(){

				@Override
				public boolean isExploding(){
					return true;
				}

				@Override
				public int getTimeBeforeExplosion(){
					return tnt.getFuse();
				}

				@Override
				public int getExplosionRadius(){
					return 8;
				}

				@Override
				public Shape3D getExplosionShape(){
					return new Sphere(tnt.posX, tnt.posY, tnt.posZ, getExplosionRadius());
				}

				@Override
				public boolean affectsEntity(Entity entity){
					return entity != tnt;
				}

				@Override
				public boolean hasChanged(){
					return tnt.posX != tnt.prevPosX || tnt.posY != tnt.prevPosY || tnt.posZ != tnt.prevPosZ;
				}

				@Override
				public boolean isValid(){
					return tnt != null && !tnt.isDead;
				}

			});
		}
	}
}

package code.elix_x.mods.creeperawareness.events;

import code.elix_x.excore.utils.shape3d.Shape3D;
import code.elix_x.excore.utils.shape3d.Sphere;
import code.elix_x.mods.creeperawareness.api.IExplosionSource;
import code.elix_x.mods.creeperawareness.api.events.GetExplosionSourceFromEntityEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BindTntEvent {

	@SubscribeEvent
	public void bind(GetExplosionSourceFromEntityEvent event){
		if(event.getEntity() instanceof EntityTNTPrimed && event.explosionSource == null){
			final EntityTNTPrimed tnt = (EntityTNTPrimed) event.getEntity();
			event.explosionSource = new IExplosionSource(){

				private boolean dirty = true;

				@Override
				public Entity getHandledEntity(){
					return tnt;
				}

				@Override
				public World getWorldObj(){
					return tnt.world;
				}

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
				public boolean update(){
					if(tnt.posX != tnt.prevPosX || tnt.posY != tnt.prevPosY || tnt.posZ != tnt.prevPosZ) setDirty(true);
					return true;
				}

				@Override
				public boolean isDirty(){
					return dirty;
				}

				@Override
				public boolean setDirty(boolean dirty){
					return this.dirty = dirty;
				}

				@Override
				public boolean isValid(){
					return tnt != null && !tnt.isDead;
				}

			};
		}
	}
}

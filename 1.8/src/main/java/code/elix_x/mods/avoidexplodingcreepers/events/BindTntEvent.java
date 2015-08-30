package code.elix_x.mods.avoidexplodingcreepers.events;

import code.elix_x.mods.avoidexplodingcreepers.api.IExplosionSource;
import code.elix_x.mods.avoidexplodingcreepers.api.events.GetExplosionSourceFromEntityEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.world.World;

public class BindTntEvent {

	public BindTntEvent() {
		
	}
	
	@SubscribeEvent
	public void bind(GetExplosionSourceFromEntityEvent event){
		if(event.entity instanceof EntityTNTPrimed && event.explosionSource == null){
			final EntityTNTPrimed tnt = (EntityTNTPrimed) event.entity;
			event.explosionSource = new IExplosionSource() {

				@Override
				public Entity getHandledEntity() {
					return tnt;
				}

				@Override
				public World getWorldObj() {
					return tnt.worldObj;
				}

				@Override
				public double getXPos() {
					return tnt.posX;
				}

				@Override
				public double getYPos() {
					return tnt.posY;
				}

				@Override
				public double getZPos() {
					return tnt.posZ;
				}

				@Override
				public boolean isExploding() {
					return true;
				}

				@Override
				public int getTimeBeforeExplosion() {
					return tnt.fuse;
				}

				@Override
				public double getRange() {
					return 4.0;
				}

				@Override
				public boolean doDefaultUpdate() {
					return true;
				}

				@Override
				public void update() {

				}

				@Override
				public boolean isValid() {
					return tnt != null && !tnt.isDead;
				}

			};
		}
	}
}

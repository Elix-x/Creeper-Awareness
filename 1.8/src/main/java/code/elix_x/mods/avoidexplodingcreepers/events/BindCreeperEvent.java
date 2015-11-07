package code.elix_x.mods.avoidexplodingcreepers.events;

import code.elix_x.mods.avoidexplodingcreepers.api.IExplosionSource;
import code.elix_x.mods.avoidexplodingcreepers.api.events.GetExplosionSourceFromEntityEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BindCreeperEvent {

	public BindCreeperEvent() {

	}

	@SubscribeEvent
	public void bind(GetExplosionSourceFromEntityEvent event){
		if(event.entity instanceof EntityCreeper && ((EntityCreeper) event.entity).getCreeperState() == 1 && event.explosionSource == null){
			final EntityCreeper creeper = (EntityCreeper) event.entity;
			event.explosionSource = new IExplosionSource() {

				@Override
				public Entity getHandledEntity() {
					return creeper;
				}

				@Override
				public World getWorldObj() {
					return creeper.worldObj;
				}

				@Override
				public double getXPos() {
					return creeper.posX;
				}

				@Override
				public double getYPos() {
					return creeper.posY;
				}

				@Override
				public double getZPos() {
					return creeper.posZ;
				}

				@Override
				public boolean isExploding() {
					return creeper.getCreeperState() == 1;
				}

				@Override
				public int getTimeBeforeExplosion() {
					return (Integer) ObfuscationReflectionHelper.getPrivateValue(EntityCreeper.class, creeper, "fuseTime", "field_82225_f") - (Integer) ObfuscationReflectionHelper.getPrivateValue(EntityCreeper.class, creeper, "timeSinceIgnited", "field_70833_d");
				}

				@Override
				public double getRange() {
					return (Integer) ObfuscationReflectionHelper.getPrivateValue(EntityCreeper.class, creeper, "explosionRadius", "field_82226_g") * (creeper.getPowered() ? 2 : 1);
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
					return creeper != null && !creeper.isDead;
				}
				
			};
		}
	}

}

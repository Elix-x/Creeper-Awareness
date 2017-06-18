package code.elix_x.mods.creeperawareness.events;

import code.elix_x.excore.utils.shape3d.Shape3D;
import code.elix_x.excore.utils.shape3d.Sphere;
import code.elix_x.mods.creeperawareness.CreeperAwarenessBase;
import code.elix_x.mods.creeperawareness.api.IExplosionSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BindCreeperEvent {

	@SubscribeEvent
	public void bind(EntityEvent.EntityConstructing event){
		if(event.getEntity() instanceof EntityCreeper && ((EntityCreeper) event.getEntity()).getCreeperState() == 1){
			final EntityCreeper creeper = (EntityCreeper) event.getEntity();
			event.getEntity().world.getCapability(CreeperAwarenessBase.managerCapability, null).addExplosionSource(new IExplosionSource(){

//				private boolean dirty = true;
				private boolean prevExplode = false;

				/*@Override
				public Entity getHandledEntity(){
					return creeper;
				}

				@Override
				public World getWorldObj(){
					return creeper.world;
				}*/

				@Override
				public boolean isExploding(){
					return creeper.getCreeperState() == 1;
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

				/*@Override
				public boolean update(){
					if(creeper.posX != creeper.prevPosX || creeper.posY != creeper.prevPosY || creeper.posZ != creeper.prevPosZ)
						setDirty(true);
					return true;
				}

				@Override
				public boolean isDirty(){
					return dirty;
				}

				@Override
				public boolean setDirty(boolean dirty){
					return this.dirty = dirty;
				}*/

				@Override
				public boolean isValid(){
					return creeper != null && !creeper.isDead;
				}

			});
		}
	}

}

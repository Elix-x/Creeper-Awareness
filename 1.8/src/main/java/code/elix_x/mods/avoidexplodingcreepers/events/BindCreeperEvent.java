package code.elix_x.mods.avoidexplodingcreepers.events;

import code.elix_x.mods.avoidexplodingcreepers.api.IExplosionSource;
import code.elix_x.mods.avoidexplodingcreepers.api.events.GetExplosionSourceFromEntityEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BindCreeperEvent {

	public BindCreeperEvent() {

	}

	@SubscribeEvent
	public void bind(GetExplosionSourceFromEntityEvent event){
		if(event.entity instanceof EntityCreeper && event.explosionSource == null){
			final EntityCreeper creeper = (EntityCreeper) event.entity;
			event.explosionSource = new IExplosionSource() {

				private final NBTTagCompound nbt = getNbt();

				private NBTTagCompound getNbt() {
					NBTTagCompound nbt = new NBTTagCompound();
					creeper.writeEntityToNBT(nbt);
					return nbt;
				}

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
					return nbt.getShort("Fuse") - (Integer) ObfuscationReflectionHelper.getPrivateValue(EntityCreeper.class, creeper, "timeSinceIgnited");
				}

				@Override
				public double getRange() {
					return nbt.getInteger("ExplosionRadius") * (nbt.hasKey("powered") && nbt.getBoolean("powered") ? 2 : 1);
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

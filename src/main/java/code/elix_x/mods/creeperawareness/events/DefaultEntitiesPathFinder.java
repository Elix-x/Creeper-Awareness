package code.elix_x.mods.creeperawareness.events;

import code.elix_x.excore.utils.shape3d.AxisAlignedBox;
import code.elix_x.excore.utils.shape3d.Shape3D;
import code.elix_x.mods.creeperawareness.CreeperAwarenessBase;
import code.elix_x.mods.creeperawareness.api.IExplosionSource;
import code.elix_x.mods.creeperawareness.api.events.PathfindEntityEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = CreeperAwarenessBase.MODID)
public class DefaultEntitiesPathFinder {

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void path(PathfindEntityEvent event){
		Entity e = event.getEntity();
		IExplosionSource source = event.getSources().iterator().next();
		if(e instanceof EntityPlayer) return;
		if(e instanceof EntityCreature){
			EntityCreature entity = (EntityCreature) e;
			Vec3d vec3;
			Shape3D shape = source.getExplosionShape();
			Vec3d sourcePos = shape.getPos();
			if(true){
				Vec3d ePos = new Vec3d(entity.posX, entity.posY, entity.posZ);
				Vec3d vec = ePos.subtract(sourcePos).normalize();
				Vec3d out = new Vec3d(0, 0, 0);
				AxisAlignedBB box = entity.getEntityBoundingBox();
				while(box != null ? shape.intersectsWith(e.world, new AxisAlignedBox(box.offset(out.x, out.y, out.z))) : shape.isInside(e.world, ePos.addVector(out.x, out.y, out.z))){
					out = out.addVector(vec.x, vec.y, vec.z);
				}
				double x = ePos.x + out.x;
				double y = ePos.y + out.y;
				double z = ePos.z + out.z;
				for(int i = 0; e.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.AIR; i = i == 0 ? 1 : i > 0 ? -i : -i + 1){
					y = ePos.y + out.y + i;
					if(i < 0 || i > 255){
						y = e.world.getHeight(new BlockPos(x, y, z)).getY();
						break;
					}
				}
				vec3 = new Vec3d(x, y, z);
			} else{
				vec3 = RandomPositionGenerator.findRandomTargetBlockAwayFrom(entity, source.getExplosionRadius(), source.getExplosionRadius(), sourcePos);
			}
			double eb = entity.getDistanceSq(vec3.x, vec3.y, vec3.z);
			double speed = eb / (source.getTimeBeforeExplosion() / 2.5);

			PathNavigate navigator = entity.getNavigator();
			navigator.clearPathEntity();
			navigator.tryMoveToXYZ(vec3.x, vec3.y, vec3.z, speed);
			event.setCanceled(true);
		}
	}

}

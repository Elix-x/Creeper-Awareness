package code.elix_x.mods.creeperawareness.api;

import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

/**
 * Explosion sources manager manages explosion sources for one world. It is a world capability, so use {@linkplain CapabilityInject} to get the capability during initialization and use {@linkplain World#getCapability(Capability, EnumFacing)} to retrieve explosion sources manager for given world.
 */
public interface IExplosionSourcesManager {

	void addExplosionSource(IExplosionSource source);

}

package com.lishid.orebfuscator.worldguard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bouncycastle.util.Arrays;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.CraftOrebfuscator;
import com.lishid.orebfuscator.api.chunk.ChunkData;
import com.lishid.orebfuscator.api.config.IWorldConfig;
import com.lishid.orebfuscator.api.types.BlockCoord;
import com.lishid.orebfuscator.obfuscation.Calculations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuardCalculations extends Calculations {

	private final Map<PermissionRegion, Result> cache = new ConcurrentHashMap<>();

	private WorldGuardHandler worldGuard;

	public WorldGuardCalculations(CraftOrebfuscator plugin) {
		super(plugin);
	}

	@Override
	public void onInit() {
		super.onInit();
		this.worldGuard = this.plugin.getWorldGuardHandler();
	}

	@Override
	public Result obfuscateOrUseCache(ChunkData chunkData, Player player, IWorldConfig worldConfig) throws Exception {
		int x = chunkData.chunkX << 4;
		int z = chunkData.chunkZ << 4;
		int xEnd = x + 16;
		int zEnd = z + 16;

		Collection<ProtectedRegion> regions = this.worldGuard.getRegionManager(chunkData.world).getRegions().values();
		ProtectedRegion chunkRegion = new ProtectedCuboidRegion("", BlockVector3.at(x, 0, z), BlockVector3.at(xEnd, 255, zEnd));
		List<ProtectedRegion> intersectedRegions = chunkRegion.getIntersectingRegions(regions);
		PermissionRegion permissionRegion = new PermissionRegion();

		for (ProtectedRegion region : intersectedRegions) {
			if (player.hasPermission("orebfuscator.region." + region.getId())) {
				permissionRegion.addRegion(region);
			}
		}

		Result obfuscatedResult = super.obfuscateOrUseCache(chunkData, player, worldConfig);

		if (!permissionRegion.isEmpty()) {
			Result result = this.cache.get(permissionRegion);

			if (result == null) {
				List<BlockCoord> removedEntities = new ArrayList<>();
				for (BlockCoord tileEnity : obfuscatedResult.removedEntities) {
					if (!permissionRegion.isTileEntityInside(BlockVector3.at(tileEnity.x, tileEnity.y, tileEnity.z))) {
						removedEntities.add(tileEnity);
					}
				}

				byte[] data = Arrays.copyOf(obfuscatedResult.output, obfuscatedResult.output.length);
				
				
//				result = new Result(Arrays.copyOf(obfuscatedResult.output, obfuscatedResult.output.length), new ArrayList<>(obfuscatedResult.removedEntities));
			}
		}

		return obfuscatedResult;
	}

	private class PermissionRegion {

		private List<ProtectedRegion> regions = new ArrayList<>();

		public void addRegion(ProtectedRegion region) {
			this.regions.add(region);
		}

		public boolean isTileEntityInside(BlockVector3 blockVector) {
			for (ProtectedRegion region : this.regions) {
				if (region.contains(blockVector)) {
					return true;
				}
			}

			return false;
		}

		public boolean isEmpty() {
			return this.regions.isEmpty();
		}

		@Override
		public int hashCode() {
			return regions.hashCode();
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else if (!(object instanceof PermissionRegion)) {
				return false;
			} else {
				PermissionRegion other = (PermissionRegion) object;
				return this.regions.equals(other.regions);
			}
		}
	}
}
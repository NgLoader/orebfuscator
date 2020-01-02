package com.lishid.orebfuscator.hook;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.CraftOrebfuscator;
import com.lishid.orebfuscator.api.Orebfuscator;
import com.lishid.orebfuscator.api.hook.IWorldGuardHandler;
import com.lishid.orebfuscator.handler.CraftHandler;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuardHandler extends CraftHandler implements IWorldGuardHandler {

	private final Map<String, List<UUID>> allowedRegions = new HashMap<>();
	private final Map<World, RegionManager> regionManagers = new HashMap<>();

	private WorldGuard worldGuard;
	private WorldGuardPlatform platform;
	private boolean supported = false;

	public WorldGuardHandler(Orebfuscator plugin) {
		super(plugin);
	}

	@Override
	public boolean canEnable() {
		return this.plugin.getConfigHandler().getConfig().isWorldGuardSupport();
	}

	@Override
	public void onInit() {
		if(Bukkit.getPluginManager().getPlugin("WorldGuard") == null) {
			Bukkit.getConsoleSender().sendMessage(CraftOrebfuscator.PREFIX + "§aWorldGuard §7support is §cdisabled§8.");
			return;
		}

		this.worldGuard = WorldGuard.getInstance();
		this.platform = this.worldGuard.getPlatform();
	}

	public boolean isInRegion(Player player, World world, int x, int y, int z) {
		Set<ProtectedRegion> regions = this.getRegionManager(world).getApplicableRegions(BlockVector3.at(x, y, z)).getRegions();
		UUID uuid = player.getUniqueId();

		if (regions.isEmpty()) {
			return false;
		}

		for (ProtectedRegion region : regions) {
			List<UUID> uuids = this.allowedRegions.get(region.getId());

			if (uuids == null || uuids.isEmpty()) {
				continue;
			}

			if (uuids.contains(uuid)) {
				return true;
			}
		}

		return false;
	}

	public RegionManager getRegionManager(World world) {
		RegionManager regionManager = this.regionManagers.get(world);

		if (regionManager == null) {
			regionManager = this.platform.getRegionContainer().get(BukkitAdapter.adapt(world));
			this.regionManagers.put(world, regionManager);
		}

		return regionManager;
	}

	@Override
	public WorldGuard getWorldGuard() {
		return this.worldGuard;
	}

	@Override
	public WorldGuardPlatform getPlatform() {
		return this.platform;
	}

	@Override
	public boolean isSupported() {
		return this.supported;
	}
}
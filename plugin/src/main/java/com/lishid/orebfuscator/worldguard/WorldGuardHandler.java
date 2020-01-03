package com.lishid.orebfuscator.worldguard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.CraftOrebfuscator;
import com.lishid.orebfuscator.api.Orebfuscator;
import com.lishid.orebfuscator.api.hook.IWorldGuardHandler;
import com.lishid.orebfuscator.handler.CraftHandler;
import com.lishid.orebfuscator.utils.Pair;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuardHandler extends CraftHandler implements IWorldGuardHandler {

	private static final Pair<Boolean, Boolean> PAIR_FALSE_FALSE = new Pair<>(false, false);
	private static final Pair<Boolean, Boolean> PAIR_TRUE_FALSE = new Pair<>(true, false);
	private static final Pair<Boolean, Boolean> PAIR_TRUE_TRUE = new Pair<>(true, true);

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

		this.supported = true;
		this.worldGuard = WorldGuard.getInstance();
	}

	@Override
	public void onDisable() {
		this.supported = false;

		this.regionManagers.clear();
	}

	public Set<ProtectedRegion> getRegions(World world, int x, int y, int z) {
		return this.getRegionManager(world).getApplicableRegions(BlockVector3.at(x, y, z)).getRegions();
	}

	public List<String> getRegionIds(World world, int x, int y, int z) {
		return this.getRegionManager(world).getApplicableRegions(BlockVector3.at(x, y, z)).getRegions().stream().map(region -> region.getId()).collect(Collectors.toList());
	}

	public boolean isRegion(World world, int x, int y, int z) {
		return !this.getRegions(world, x, y, z).isEmpty();
	}

	public Map.Entry<Boolean, Boolean> isInRegionAndHasPermission(Player player, World world, int x, int y, int z) {
		Set<ProtectedRegion> regions = this.getRegions(world, x, y, z);

		if (regions.isEmpty()) {
			return WorldGuardHandler.PAIR_FALSE_FALSE;
		}

		for (ProtectedRegion region : regions) {
			if (player.hasPermission("orebfuscator.region." + region.getId())) {
				return WorldGuardHandler.PAIR_TRUE_TRUE;
			}
		}

		return WorldGuardHandler.PAIR_TRUE_FALSE;
	}

	public boolean hasRegionPermission(Player player, World world, int x, int y, int z) {
		Set<ProtectedRegion> regions = this.getRegions(world, x, y, z);

		if (regions.isEmpty()) {
			return false;
		}

		for (ProtectedRegion region : regions) {
			if (player.hasPermission("orebfuscator.region." + region.getId())) {
				return true;
			}
		}

		return false;
	}

	public boolean hasPermission(Player player, String... ids) {
		for (String id : ids) {
			if (player.hasPermission("orebfuscator.region." + id)) {
				return true;
			}
		}
		return false;
	}

	public RegionManager getRegionManager(World world) {
		RegionManager regionManager = this.regionManagers.get(world);

		if (regionManager == null) {
			regionManager = this.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
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
		if (this.platform == null) {
			this.platform = this.worldGuard.getPlatform();
		}

		return this.platform;
	}

	@Override
	public boolean isSupported() {
		return this.supported;
	}
}
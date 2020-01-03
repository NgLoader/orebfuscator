package com.lishid.orebfuscator.api.hook;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.api.Handler;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public interface IWorldGuardHandler extends Handler {

	public Set<ProtectedRegion> getRegions(World world, int x, int y, int z);
	public List<String> getRegionIds(World world, int x, int y, int z);

	public boolean isRegion(World world, int x, int y, int z);

	public Map.Entry<Boolean, Boolean> isInRegionAndHasPermission(Player player, World world, int x, int y, int z);
	public boolean hasRegionPermission(Player player, World world, int x, int y, int z);

	public RegionManager getRegionManager(World world);

	public WorldGuard getWorldGuard();
	public WorldGuardPlatform getPlatform();

	public boolean isSupported();
}
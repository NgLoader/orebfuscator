package com.lishid.orebfuscator.api.hook;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.api.Handler;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.managers.RegionManager;

public interface IWorldGuardHandler extends Handler {

	public boolean isInRegion(Player player, World world, int x, int y, int z);
	public RegionManager getRegionManager(World world);

	public WorldGuard getWorldGuard();
	public WorldGuardPlatform getPlatform();

	public boolean isSupported();
}
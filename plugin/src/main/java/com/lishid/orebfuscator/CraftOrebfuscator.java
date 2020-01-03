/*
 * Copyright (C) 2011-2014 lishid. All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.orebfuscator;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import com.lishid.orebfuscator.api.Handler;
import com.lishid.orebfuscator.api.Orebfuscator;
import com.lishid.orebfuscator.api.nms.INmsManager;
import com.lishid.orebfuscator.cache.ObfuscatedDataCacheHandler;
import com.lishid.orebfuscator.chunkmap.ChunkMapHandler;
import com.lishid.orebfuscator.commands.OrebfuscatorCommandExecutor;
import com.lishid.orebfuscator.config.ConfigHandler;
import com.lishid.orebfuscator.handler.CraftHandler;
import com.lishid.orebfuscator.handler.NmsHandler;
import com.lishid.orebfuscator.hithack.BlockHitHandler;
import com.lishid.orebfuscator.hook.ProtocolLibHandler;
import com.lishid.orebfuscator.listeners.OrebfuscatorBlockListener;
import com.lishid.orebfuscator.listeners.OrebfuscatorEntityListener;
import com.lishid.orebfuscator.listeners.OrebfuscatorPlayerListener;
import com.lishid.orebfuscator.obfuscation.BlockUpdate;
import com.lishid.orebfuscator.obfuscation.Calculations;
import com.lishid.orebfuscator.proximityhider.ProximityHiderHandler;
import com.lishid.orebfuscator.utils.MaterialHelper;
import com.lishid.orebfuscator.worldguard.WorldGuardHandler;

/**
 * Orebfuscator Anti X-RAY
 *
 * @author lishid
 */
public class CraftOrebfuscator extends JavaPlugin implements Orebfuscator {

	public static final String PREFIX = "§8[§aOFC§8] §8";

	private final NmsHandler nmsHandler;
	private final ObfuscatedDataCacheHandler obfuscatedDataCacheHandler;
	private final ConfigHandler configHandler;
	private final ChunkMapHandler chunkMapHandler;
	private final BlockHitHandler blockHitHandler;
	private final ProximityHiderHandler proximityHiderHandler;
	private final ProtocolLibHandler protocolLibHandler;
	private final WorldGuardHandler worldGuardHandler;

	private final Calculations calculations;
	private final BlockUpdate blockUpdate;
	private final MaterialHelper materialHelper;

	public CraftOrebfuscator() {
		this.nmsHandler = new NmsHandler(this);
		this.obfuscatedDataCacheHandler = new ObfuscatedDataCacheHandler(this);

		this.materialHelper = new MaterialHelper(this);

		this.configHandler = new ConfigHandler(this);

		this.calculations = new Calculations(this);
		this.blockUpdate = new BlockUpdate(this);

		this.chunkMapHandler = new ChunkMapHandler(this);
		this.blockHitHandler = new BlockHitHandler(this);
		this.proximityHiderHandler = new ProximityHiderHandler(this);
		this.protocolLibHandler = new ProtocolLibHandler(this);
		this.worldGuardHandler = new WorldGuardHandler(this);
	}

	@Override
	public void onLoad() {
		CraftHandler.getCraftHandlers().forEach(Handler::init);
	}

	@Override
	public void onEnable() {
		this.getServer().getServicesManager().register(Orebfuscator.class, this, this, ServicePriority.High);

		CraftHandler.getCraftHandlers().forEach(Handler::enable);

		// Orebfuscator events
		PluginManager pluginManager = Bukkit.getPluginManager();
		pluginManager.registerEvents(new OrebfuscatorPlayerListener(this), this);
		pluginManager.registerEvents(new OrebfuscatorEntityListener(this), this);
		pluginManager.registerEvents(new OrebfuscatorBlockListener(this), this);

		// Orebfuscator commands
		getCommand("ofc").setExecutor(new OrebfuscatorCommandExecutor(this));
	}

	@Override
	public void onDisable() {
		CraftHandler.getCraftHandlers().forEach(Handler::disable);
		getServer().getScheduler().cancelTasks(this);
	}

	public ConfigHandler getConfigHandler() {
		return this.configHandler;
	}

	public INmsManager getNmsManager() {
		return this.nmsHandler;
	}

	public ChunkMapHandler getChunkMapHandler() {
		return this.chunkMapHandler;
	}

	public ObfuscatedDataCacheHandler getObfuscatedDataCacheHandler() {
		return this.obfuscatedDataCacheHandler;
	}

	public BlockHitHandler getBlockHitHandler() {
		return this.blockHitHandler;
	}

	public ProximityHiderHandler getProximityHiderHandler() {
		return this.proximityHiderHandler;
	}

	public ProtocolLibHandler getProtocolLibHandler() {
		return this.protocolLibHandler;
	}

	public WorldGuardHandler getWorldGuardHandler() {
		return this.worldGuardHandler;
	}

	public Calculations getCalculations() {
		return this.calculations;
	}

	public BlockUpdate getBlockUpdate() {
		return this.blockUpdate;
	}

	public MaterialHelper getMaterialHelper() {
		return this.materialHelper;
	}
}

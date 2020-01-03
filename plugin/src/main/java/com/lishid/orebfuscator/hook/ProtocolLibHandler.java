/*
 * Copyright (C) 2011-2014 lishid.  All rights reserved.
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

package com.lishid.orebfuscator.hook;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.lishid.orebfuscator.CraftOrebfuscator;
import com.lishid.orebfuscator.api.chunk.ChunkData;
import com.lishid.orebfuscator.api.config.IConfigManager;
import com.lishid.orebfuscator.api.config.IOrebfuscatorConfig;
import com.lishid.orebfuscator.api.config.IWorldConfig;
import com.lishid.orebfuscator.api.hithack.IBlockHitHandler;
import com.lishid.orebfuscator.api.hook.IProtocolLibHandler;
import com.lishid.orebfuscator.api.logger.OFCLogger;
import com.lishid.orebfuscator.api.types.BlockCoord;
import com.lishid.orebfuscator.api.utils.ICalculations;
import com.lishid.orebfuscator.handler.CraftHandler;
import com.lishid.orebfuscator.obfuscation.Calculations;

public class ProtocolLibHandler extends CraftHandler implements IProtocolLibHandler {

	private final List<PacketListener> packetListeners = new ArrayList<PacketListener>();

	private IOrebfuscatorConfig config;
	private IConfigManager configManager;
	private IBlockHitHandler blockHitHandler;
	private ICalculations calculations;

	private ProtocolManager manager;

	public ProtocolLibHandler(CraftOrebfuscator plugin) {
		super(plugin);
	}

	@Override
	public void onInit() {
		this.blockHitHandler = this.plugin.getBlockHitHandler();
		this.config = this.plugin.getConfigHandler().getConfig();
		this.configManager = this.plugin.getConfigHandler().getConfigManager();
		this.calculations = this.plugin.getCalculations();

		this.registerListener();
		this.manager = Bukkit.getPluginManager().getPlugin("ProtocolLib") != null ? com.comphenix.protocol.ProtocolLibrary.getProtocolManager() : null;

		if (this.manager == null) {
			Bukkit.getPluginManager().disablePlugin(this.plugin);
			OFCLogger.log("[OFC] Plugin was disabled! ProtocolLib was not found.");
		}
	}

	@Override
	public void onEnable() {
		if (this.manager != null) {
			this.packetListeners.forEach(listener -> this.manager.addPacketListener(listener));
		}
	}

	@Override
	public void onDisable() {
		if (this.manager != null) {
			this.packetListeners.forEach(listener -> this.manager.removePacketListener(listener));
		}
	}

	private void registerListener() {
		this.packetListeners.add(new PacketAdapter(plugin, PacketType.Play.Server.MAP_CHUNK) {

			@Override
			public void onPacketSending(PacketEvent event) {
				ChunkData chunkData = null;

				try {
					Player player = event.getPlayer();

					if (!config.isEnabled() || !config.obfuscateForPlayer(player)) {
						return;
					}

					IWorldConfig worldConfig = configManager.getWorld(player.getWorld());

					if (!worldConfig.isEnabled()) {
						return;
					}

					PacketContainer packet = event.getPacket();

					StructureModifier<Integer> ints = packet.getIntegers();
					StructureModifier<byte[]> byteArray = packet.getByteArrays();
					StructureModifier<Boolean> bools = packet.getBooleans();
					@SuppressWarnings("rawtypes")
					StructureModifier<List> list = packet.getSpecificModifier(List.class);

					List<?> nmsTags = list.read(0);

					chunkData = new ChunkData();
					chunkData.chunkX = ints.read(0);
					chunkData.chunkZ = ints.read(1);
					chunkData.groundUpContinuous = bools.read(0);
					chunkData.primaryBitMask = ints.read(2);
					chunkData.data = byteArray.read(0);
					chunkData.world = event.getPlayer().getWorld();
					chunkData.isOverworld = chunkData.world.getEnvironment() == World.Environment.NORMAL;
					chunkData.blockEntities = getBlockEntities(nmsTags);

					Calculations.Result result = calculations.obfuscateOrUseCache(chunkData, player, worldConfig);

					if (result != null && result.output != null) {
						byteArray.write(0, result.output);

						if (nmsTags != null) {
							removeBlockEntities(nmsTags, chunkData.blockEntities, result.removedEntities);
							list.write(0, nmsTags);
						}
					}
				} catch (Exception e) {
					if (chunkData != null) {
						OFCLogger.logger.log(Level.SEVERE, "ChunkX = " + chunkData.chunkX + ", chunkZ = " + chunkData.chunkZ);
					}

					e.printStackTrace();
				}
			}
		});

		String serverVersion = this.plugin.getNmsManager().getServerVersion();
		if (!serverVersion.equals("v1_15_R1") && !serverVersion.equals("v1_14_R1")) { // TODO check and fix!
			this.packetListeners.add(new PacketAdapter(plugin, PacketType.Play.Client.BLOCK_DIG) {

				@Override
				public void onPacketReceiving(PacketEvent event) {
					EnumWrappers.PlayerDigType status = event.getPacket().getPlayerDigTypes().read(0);
					if (status == EnumWrappers.PlayerDigType.ABORT_DESTROY_BLOCK) {
						if (!blockHitHandler.hitBlock(event.getPlayer(), null)) {
							event.setCancelled(true);
						}
					}
				}
			});
		}
	}

	private List<NbtCompound> getBlockEntities(List<?> nmsTags) {
		List<NbtCompound> entities = new ArrayList<>();

		if (nmsTags != null) {
			for (Object nmsTag : nmsTags) {
				entities.add(NbtFactory.fromNMSCompound(nmsTag));
			}
		}

		return entities;
	}

	private void removeBlockEntities(List<?> nmsTags, List<NbtCompound> tags, List<BlockCoord> removedEntities) {
		for (int i = nmsTags.size() - 1; i >= 0; i--) {
			if (removedEntities.size() == 0) {
				break;
			}

			NbtCompound tag = tags.get(i);
			int x = tag.getInteger("x");
			int y = tag.getInteger("y");
			int z = tag.getInteger("z");

			for (int k = 0; k < removedEntities.size(); k++) {
				BlockCoord blockCoord = removedEntities.get(k);

				if (blockCoord.x == x && blockCoord.y == y && blockCoord.z == z) {
					nmsTags.remove(i);
					removedEntities.remove(k);
					break;
				}
			}
		}
	}

	public ProtocolManager getManager() {
		return this.manager;
	}

	/*
	 * private static boolean _isSaved; private void saveTestData(ChunkData
	 * chunkData) { if(_isSaved) return;
	 * 
	 * _isSaved = true;
	 * 
	 * FileOutputStream fos; try { fos = new FileOutputStream("D:\\Temp\\chunk_X" +
	 * chunkData.chunkX + "_Z" + chunkData.chunkZ + ".dat");
	 * fos.write(chunkData.chunkX & 0xff); fos.write((chunkData.chunkX >> 8) &
	 * 0xff); fos.write(chunkData.chunkZ & 0xff); fos.write((chunkData.chunkZ >> 8)
	 * & 0xff); fos.write(chunkData.primaryBitMask & 0xff);
	 * fos.write((chunkData.primaryBitMask >> 8) & 0xff);
	 * fos.write(chunkData.data.length & 0xff); fos.write((chunkData.data.length >>
	 * 8) & 0xff); fos.write((chunkData.data.length >> 16) & 0xff);
	 * fos.write(chunkData.data); fos.close(); } catch (FileNotFoundException e) {
	 * e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); } }
	 */
}

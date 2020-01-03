package com.lishid.orebfuscator.api.chunk;

import java.util.List;

import org.bukkit.World;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;

public class ChunkData {

	public int chunkX;
	public int chunkZ;
	public boolean groundUpContinuous;
	public int primaryBitMask;
	public byte[] data;
	public boolean isOverworld;
	public boolean useCache;
	public List<NbtCompound> blockEntities;
	public World world;
}

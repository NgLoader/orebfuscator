package com.lishid.orebfuscator.utils;

public class ChunkCoordPair {

	public final int x;
	public final int z;

	public ChunkCoordPair(int x, int z) {
		this.x = x;
		this.z = z;
	}

	public int hashCode() {
		int var0 = 1664525 * this.x + 1013904223;
		int var1 = 1664525 * (this.z ^ -559038737) + 1013904223;
		return var0 ^ var1;
	}

	public boolean equals(Object var0) {
		if (this == var0) {
			return true;
		} else if (!(var0 instanceof ChunkCoordPair)) {
			return false;
		} else {
			ChunkCoordPair var1 = (ChunkCoordPair) var0;
			return this.x == var1.x && this.z == var1.z;
		}
	}
}
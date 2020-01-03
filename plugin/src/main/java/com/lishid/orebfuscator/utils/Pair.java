package com.lishid.orebfuscator.utils;

import java.util.Map;

public class Pair<K, V> implements Map.Entry<K, V> {

	private final K key;
	private V value;

	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return this.key;
	}

	public V getValue() {
		return this.value;
	}

	public V setValue(V value) {
		V oldValue = this.value;
		this.value = value;

		return oldValue;
	}
}
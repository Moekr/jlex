package com.moekr.jlex;

import java.util.BitSet;

class ComplementBitSet {
	private BitSet bitSet;
	private boolean complement;

	ComplementBitSet(){
		bitSet = new BitSet();
	}

	void complement(){
		complement = true;
	}

	void add(int i){
		bitSet.set(i);
	}

	void add(char c){
		add((int)c);
		add((int)Character.toLowerCase(c));
		add((int)Character.toTitleCase(c));
		add((int)Character.toUpperCase(c));
	}

	boolean contains(int i){
		boolean result = bitSet.get(i);
		return complement != result;
	}

	void map(ComplementBitSet another,int[] map){
		complement = another.complement;
		bitSet.clear();
		another.bitSet.stream().filter(value -> value < map.length).forEach(value -> bitSet.set(map[value]));
	}

	@Override
	public Object clone(){
		ComplementBitSet complementBitSet = new ComplementBitSet();
		complementBitSet.complement = complement;
		complementBitSet.bitSet = (BitSet) bitSet.clone();
		return complementBitSet;
	}
}

package com.moneybags.tempfly.hook.region;

import org.bukkit.util.Vector;

public class CompatRegion {

	private String id;
	private Vector min, max;
	
	public CompatRegion(String id, Vector min, Vector max) {
		this.id = id;
		this.min = min;
		this.max = max;
	}

	public CompatRegion(String id) {
		this.id = id;
		this.min = new Vector(0, 0, 0);
		this.max = new Vector(0, 0, 0);
	}

	public String getId() {
		return id;
	}
	
	public Vector getMin() {
		return min;
	}
	
	public Vector getMax() {
		return max;
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof CompatRegion) && super.equals(o)
				|| ((CompatRegion)o).getId().equals(id)
				&& this.min.equals(((CompatRegion)o).getMin())
				&& this.max.equals(((CompatRegion)o).getMax()); 
	}
}

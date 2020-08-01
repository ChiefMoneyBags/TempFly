package com.moneybags.tempfly.hook.region;

public class CompatRegion {

	private String id;
	
	public CompatRegion(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof CompatRegion) && super.equals(o) || ((CompatRegion)o).getId().equals(id); 
	}
}

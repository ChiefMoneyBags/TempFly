package com.moneybags.tempfly.gui.abstraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.moneybags.tempfly.gui.GuiSession;

public abstract class DynamicPage extends Page {

	//Page number
	private int num;
	private List<Integer> open = new ArrayList<>();
	private int rows = 3;
	
	public DynamicPage(GuiSession session) {
		super(session);
	}
	
	public void calculateSlots(int num, int items) {
		this.num = num;
		
		int skip = 21 * num;
		int pageItems = items-skip > 21 ? 21 : items-skip;
		final List<Integer> sides = Arrays.asList(17, 18, 26, 27, 35);
		int rowIndex = 0;
		for (int i = 10; i < 35; i++) {
			if (sides.contains(i)) {
				continue;
			}
			if ((i == 10) || (i == 19) || (i == 28)) {
				rowIndex++;
				int rowItems = pageItems - ((rowIndex-1) * 7) < 7 ? pageItems - ((rowIndex-1) * 7) : 7;
				if (rowItems < 7 && rowItems > 0) {
					Iterator<Integer> it = getPageLayout(pageItems % 7).iterator();
					while (it.hasNext()) {
						open.add(it.next() + (9 * (rowIndex)));
					}
					break;
				} else if (rowItems <= 0) {
					rowIndex--;
					break;
				} 
			}
			open.add(i);
		}
		rows = rows+rowIndex;
	}
	
	public static List<Integer> getPageLayout(int remainder) {
		switch (remainder) {
		case 1:
			return Arrays.asList(4);
		case 2:
			return Arrays.asList(3, 5);
		case 3:
			return Arrays.asList(3, 4, 5);
		case 4:
			return Arrays.asList(2, 3, 5, 6);
		case 5:
			return Arrays.asList(2, 3, 4, 5, 6);
		case 6:
			return Arrays.asList(1, 2, 3, 5, 6, 7);
		default:
			return Arrays.asList();
		}
	}
	
	public int getPageNumber() {
		return num;
	}
	
	public void setPageNumber(int num) {
		this.num = num;
	}
	
	public List<Integer> getOpenSlots() {
		return open;
	}
	
	public int getRows() {
		return rows;
	}
}

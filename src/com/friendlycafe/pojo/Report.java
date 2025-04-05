/**
 * Author 			: prasanths 
 * Last Modified By : prasanths
 */
package com.friendlycafe.pojo;

import java.util.Objects;


public class Report {


	public String itemId;
	public String itemName;
	public Float itemCost;
	public Integer count;
	public Float totalCost;
	
	/**
	 * @param name
	 * @param value
	 * @param f
	 */
	public Report(String itemId, String itemName,float itemCost, Integer count) {
		// TODO Auto-generated constructor stub
		this.itemId = itemId;
		this.itemName = itemName;
		this.itemCost = itemCost;
		this.count = count;
		this.totalCost =  itemCost * count;
	}
	
	@Override
	public boolean equals(Object object) {
		Report newItem = (Report) object;
		if(this.itemId.equals(newItem.itemId)) return true;
		else if(this == object) return true;
		else return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(itemId);
	}
}

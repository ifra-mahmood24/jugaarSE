/**
 * Author 			: prasanths 
 * Last Modified By : prasanths
 */
package com.friendlycafe.pojo;

import java.util.Objects;

public class Item {
	public String itemId;
	public String name;
	public String description;
	public float cost;
	
	
	/**
	 * @param itemId
	 * @param name
	 * @param description
	 * @param cost
	 */
	public Item(String itemId, String name, String description, float cost) {
		super();
		this.itemId = itemId;
		this.name = name;
		this.description = description;
		this.cost = cost;
	}
	
	@Override
	public boolean equals(Object object) {
		Item newItem = (Item) object;
		if(this.itemId.equals(newItem.itemId)) return true;
		else if(this == object) return true;
		else return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(itemId);
	}
	
	/**
	 * 
	 */
	public Item() {
		// TODO Auto-generated constructor stub
	}


	public boolean addItem(String name, float cost){
		return true;
	}
	
	public boolean updateCost(String itemId, float cost) {
		return true;
	}
	
	public Item getItem(String itemId) {
		// use itemId to find the particular item
		return  this;
	}
	
	public boolean deleteItem(String itemId) {
		return true;
	}
}

package com.friendlycafe.pojo;

public class Dessert extends Item {
     
    public boolean sugarFree;
	
	
	/**
	 * @param itemId
	 * @param itemName
	 * @param description
	 * @param cost
     * @param sugarFree
	 */
	public Dessert(String itemId, String itemName, String description, Float cost, boolean sugarFree) {
		// TODO Auto-generated constructor stub
		this.itemId = itemId;
		this.name = itemName;
		this.description = description;
		this.cost = cost;
		this.sugarFree = sugarFree;
	}

	public void isSugarFree() {
        if (this.sugarFree == false)
        {
        }
    }
}

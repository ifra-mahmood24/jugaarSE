/**
 * Author 			: prasanths 
 * Last Modified By : prasanths
 */
package com.friendlycafe.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.friendlycafe.pojo.Order;

/**
 * 
 */
public class OrderDTO {
	@JsonProperty
	List<Order> orders;

	/**
	 * @return the orders
	 */
	public List<Order> getOrders() {
		return orders;
	}
	/**
	 * @param orders the orders to set
	 */
	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}

}

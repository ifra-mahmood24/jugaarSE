/**
 * Author 			: prasanths 
 * Last Modified By : prasanths
 */
package com.friendlycafe.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.friendlycafe.pojo.Customer;

/**
 * 
 */
public class CustomerDTO {
	@JsonProperty
	List<Customer> customers;

	/**
	 * @return the orders
	 */
	public List<Customer> getCustomers() {
		return customers;
	}
	/**
	 * @param orders the orders to set
	 */
	public void setCustomers(List<Customer> customer) {
		this.customers = customer;
	}

}

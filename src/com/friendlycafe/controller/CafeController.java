/**
 * Author 			: prasanths 
 * Last Modified By : prasanths
 */
package com.friendlycafe.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

import com.friendlycafe.dtoservice.CafeService;
import com.friendlycafe.service.DataService;
import com.friendlycafe.exception.CustomerFoundException;
import com.friendlycafe.exception.InvalidMailFormatException;
import com.friendlycafe.pojo.Item;
import com.friendlycafe.pojo.Order;

/**
 * 
 */
public class CafeController {

	private DataService dataService = new DataService();
	private CafeService cafeService = new CafeService();
	
	
	public ArrayList<Item> getMenu(){	
		
		return cafeService.getMenu();

	}

	public boolean checkCustomer(String customerId) {
				
		try {
			return dataService.checkCustomer(customerId);
		} catch (CustomerFoundException e) {
			e.printStackTrace();
		} catch (InvalidMailFormatException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void saveCustomerDetails(String name, String mailId) {
		dataService.saveCustomerDetails(name, mailId);
	}
	
	
	public double getTotalCost(HashMap<String, Integer> orderedItems) {	

		return cafeService.calculateCost(orderedItems);		 
	
	}
	
	public double getDiscountedCost(double cost) {
	
		return cafeService.applyDiscount(cost);
	
	}	
	
	public Order saveAsActiveOrder(String customerMailId, HashMap<String, Integer> orderedItems, boolean isOffered, double cost) {
	
		Order order = constructOrder(customerMailId, orderedItems,isOffered, cost);		
		
		return dataService.saveAsActiveOrder(order);
		
	}
	
	public Order saveOrder(String customerMailId,  HashMap<String, Integer> orderedItems, boolean isOffered, double cost) {
						
		Order order = constructOrder(customerMailId, orderedItems,isOffered, cost);		
		
		return dataService.saveOrder(order);
	}


	public void generateReport() {
		dataService.generateReport();
	}
	
//	-------------- PRIVATE HELPER METHODS -----------------	
	private Order constructOrder(String customerMailId, HashMap<String, Integer> orderedItems, boolean isOffered, double cost) {

		Random random = new Random();
		Integer orderId = random.nextInt();
		String timeStamp = LocalDateTime.now().toString();

		return new Order("ORD"+ orderId.toString(),customerMailId, timeStamp, orderedItems, isOffered, cost);
	}
}

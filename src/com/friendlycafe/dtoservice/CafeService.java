package com.friendlycafe.dtoservice;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import com.friendlycafe.daoservice.DataAccessService;
import com.friendlycafe.pojo.Beverage;
import com.friendlycafe.pojo.Dessert;
import com.friendlycafe.pojo.Item;
import com.friendlycafe.pojo.Beverage.DrinkSize;
import com.friendlycafe.pojo.Beverage.TempType;

public class CafeService {
	
	private Logger logger = Logger.getLogger(CafeService.class.getName());

	public static final ArrayList<Item> menuList = new ArrayList<>();
	private final DataAccessService daoService = new DataAccessService();

	public ArrayList<Item> getMenu() {
		try {


			JSONArray foodItemListAsObject = daoService.readJSONFile("src/main/resources/foodMenu.json", "foodItems");
			JSONArray beverageItemListAsObject = daoService.readJSONFile("src/main/resources/beverageMenu.json", "beverageItems");
			JSONArray dessertItemListAsObject = daoService.readJSONFile("src/main/resources/dessertMenu.json", "dessertItems");

			ArrayList<Item> foodItemList = new ArrayList<>();
			ArrayList<Item> beverageItemList = new ArrayList<>();
			ArrayList<Item> dessertItemList = new ArrayList<>();

			
			//Reading Food Menu List 
			for(int index = 0; index < foodItemListAsObject.length(); index++) {
				
				JSONObject JsonIndex = foodItemListAsObject.getJSONObject(index);
				
				String itemId = JsonIndex.getString("itemId");
				String itemName = JsonIndex.getString("name");
				String description = JsonIndex.getString("description");
				Float cost = Float.parseFloat(JsonIndex.getString("cost"));
				
				Item foodItem = new Item(itemId, itemName, description, cost);
				foodItemList.add(foodItem);
			}
			
			//Reading Beverage Menu List 
			for(int index = 0; index < beverageItemListAsObject.length(); index++) {
				
				JSONObject JsonIndex = beverageItemListAsObject.getJSONObject(index);
				
				String itemId = JsonIndex.getString("itemId");
				String itemName = JsonIndex.getString("name");
				String description = JsonIndex.getString("description");
				Float cost = Float.parseFloat(JsonIndex.getString("cost"));
				String temp = JsonIndex.getString("temp");
				String size = JsonIndex.getString("size");
				//Possible exception for Temp or size value not matching with enum type
				Beverage beverage = new Beverage(itemId, itemName, description, cost, TempType.valueOf(temp), DrinkSize.valueOf(size));
				
				beverageItemList.add(beverage);
			}

			//Reading Dessert List
			for (int index = 0; index < dessertItemListAsObject.length(); index++) {

				JSONObject JsonIndex = dessertItemListAsObject.getJSONObject(index);

				String itemId = JsonIndex.getString("itemId");
				String itemName = JsonIndex.getString("name");
				String description = JsonIndex.getString("description");
				Float cost = Float.parseFloat(JsonIndex.getString("cost"));
				Boolean sugarFree = JsonIndex.getBoolean("sugarFree");

				Dessert dessert = new Dessert(itemId, itemName, description, cost, sugarFree);
				dessertItemList.add(dessert);
			}

			menuList.addAll(foodItemList);
			menuList.addAll(beverageItemList);
			menuList.addAll(dessertItemList);
			
			return menuList;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
    public double applyDiscount(double cost) {
    	
        boolean isFridayFeast = LocalDate.now().getDayOfWeek().toString() == DayOfWeek.FRIDAY.toString(); 			// 10% offer day
        boolean isWednesdayBanger = LocalDate.now().getDayOfWeek().toString() == DayOfWeek.WEDNESDAY.toString(); 	// 20% offer day
        
        return isFridayFeast ? cost * 0.90 : isWednesdayBanger ? cost * 0.80 : cost; 

    }
    
	public double calculateCost(HashMap<String, Integer> orderedItems) {
		
		double orderCost = 0f;
		
		ArrayList<Item> menu = getMenu();
		HashMap<String, Float> menuRate = new HashMap<>();
		
		for(Item item : menu) 
			menuRate.put(item.itemId, item.cost);
		
		for(Entry<String, Integer> orderedItem : orderedItems.entrySet()) {
			logger.info("KEY: "+orderedItem.getKey()  +" MENURATE: "+ menuRate.get(orderedItem.getKey()) +" VALUE : "+orderedItem.getValue());
			orderCost += (menuRate.get(orderedItem.getKey())) * orderedItem.getValue();
		}
		logger.info("RETURNING... ->"+orderCost);
		return orderCost;
	}
    

    

}


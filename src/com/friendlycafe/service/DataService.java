/**
 * Author 			: prasanths 
 * Last Modified By : prasanths
 * Modified for Stage 2 requirements
 */
package com.friendlycafe.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.friendlycafe.daoservice.DataAccessService;
import com.friendlycafe.exception.CustomerFoundException;
import com.friendlycafe.exception.InvalidMailFormatException;
import com.friendlycafe.pojo.Beverage;
import com.friendlycafe.pojo.Beverage.DrinkSize;
import com.friendlycafe.pojo.Beverage.TempType;
import com.friendlycafe.pojo.Dessert;
import com.friendlycafe.model.Customer;
import com.friendlycafe.pojo.Item;
import com.friendlycafe.pojo.Order;
import com.friendlycafe.pojo.Report;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class DataService {
    Map<String, Map<String,String>> customers = new HashMap<>();


    public static final ArrayList<Item> menuList = new ArrayList<>();
    private final DataAccessService daoService = new DataAccessService();
    
    /**
     * Get all menu items
     */
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
            return new ArrayList<>();
        }
    }
    
    /**
     * Save an order to the database
     */
    public Order saveOrder(Order order) {
        float orderCost = calculateCost(order);
        order.setCost(orderCost);
        
        ArrayList<Order> allOldOrders = getAllOldOrders();
        allOldOrders.add(order);
        
        daoService.writeJSONFileForOrders("src/main/resources/orders.json", allOldOrders);
        
        // Add log entry
        LogService.getInstance().log("Saved order " + order.getOrderId() + " to database");

        return order;
    }

    /**
     * Save an order with customer details
     */
    public void saveOrder(String customerMailId, HashMap<String, Integer> orderDetail) {
        Random random = new Random();
        Integer orderId = random.nextInt();
        String timeStamp = LocalDateTime.now().toString();
        Order order = new Order("ORD"+ orderId.toString(), customerMailId, timeStamp, orderDetail);
        ArrayList<Order> allOrders = new ArrayList<>();
        allOrders.add(order);
        
        JSONArray allOrdersAsJSON = daoService.readJSONFile("src/main/resources/orders.json", "orders");
        ObjectMapper objectMapper = new ObjectMapper();

        for(Object oldOrder : allOrdersAsJSON) {
            try {
                Order thisOrder = objectMapper.readValue(oldOrder.toString(), Order.class);
                allOrders.add(thisOrder);
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        
        if(!allOrders.isEmpty()) {
            daoService.writeJSONFileForOrders("src/main/resources/orders.json", allOrders);
            // Add log entry
            LogService.getInstance().log("Saved new order for customer " + customerMailId);
        }
    }

    /**
     * Check if a customer exists
     */
    public boolean checkCustomer(String mailId) throws CustomerFoundException, InvalidMailFormatException {
        JSONArray customersListAsObject = daoService.readJSONFile("src/main/resources/customers.json", "customers");
        
        for(int index = 0; index < customersListAsObject.length(); index++) {
            JSONObject JsonIndex = customersListAsObject.getJSONObject(index);
            if(JsonIndex.getString("mailId").toString().equalsIgnoreCase(mailId) == true) return true;
        }
        return false;
    }
    
    /**
     * Save customer details
     */
    public boolean saveCustomerDetails(String name, String mailId) {
        Customer newCustomer = new Customer(name, mailId);
        JSONArray customersListAsObject = daoService.readJSONFile("src/main/resources/customers.json", "customers");
        ArrayList<Customer> allCustomers = new ArrayList<>();

        for(int index = 0; index < customersListAsObject.length(); index++) {
            JSONObject JsonIndex = customersListAsObject.getJSONObject(index);
            Customer customer = new Customer(JsonIndex.getString("name"), JsonIndex.getString("mailId"));
            allCustomers.add(customer);
        }
        
        allCustomers.add(newCustomer);
        
        if(!allCustomers.isEmpty()) {
            daoService.writeJSONFileForCustomers("src/main/resources/customers.json", allCustomers);
            // Add log entry
            LogService.getInstance().log("Saved new customer: " + name + " (" + mailId + ")");
        }

        return false;
    }
    
    /**
     * Generate a report of orders
     */
    public void generateReport() {
        ArrayList<Report> allOrderedItems = new ArrayList<>();
        ArrayList<Item> menu = getMenu();
        ArrayList<Order> todaysOrders = new ArrayList<>();
        try {    
            JSONArray ordersListAsObject = daoService.readJSONFile("src/main/resources/orders.json", "orders");

            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;

            for(int index = 0; index < ordersListAsObject.length(); index++) {

                JSONObject JsonIndex = ordersListAsObject.getJSONObject(index);
                String orderId = JsonIndex.getString("orderId");
                String customerId = JsonIndex.getString("customerId");
                String timeStamp = JsonIndex.getString("timeStamp");
                
                boolean isTodayOrder = LocalDate.parse(timeStamp.split("T")[0], formatter).toString().equals(LocalDate.now().toString());
                
                if(isTodayOrder) {
                    JSONObject orderedItemsAsJSON = JsonIndex.getJSONObject("orderedItems");
                    Map<String, Object> map = orderedItemsAsJSON.toMap();
                    
                    HashMap<String, Integer> hashMap = new HashMap<>();
                    
                    for(Entry<String, Object> entry : map.entrySet()) {
                        hashMap.put(entry.getKey(), Integer.parseInt(entry.getValue().toString()));
                    }
                    Order thisOrder = new Order(orderId, customerId, timeStamp, hashMap);
                    
                    todaysOrders.add(thisOrder);
                }
                    
            }
            LogService.getInstance().log("Generated report with " + todaysOrders.size() + " orders");
        
        } catch(Exception e) {
            e.printStackTrace();
            LogService.getInstance().log("Error generating report: " + e.getMessage());
        }
        
        Map<String, Integer> map = new HashMap<>();
        Set<String> computedOrders = new HashSet<>();
        
        for(Order order : todaysOrders) 
            for(Entry<String, Integer> orderedItems : order.getOrderedItems().entrySet())
                if(!map.containsKey(orderedItems.getKey()))
                    map.put(orderedItems.getKey(), orderedItems.getValue());
                else
                    map.put(orderedItems.getKey(), map.get(orderedItems.getKey()) + orderedItems.getValue());
        
        for(Entry<String, Integer> entry : map.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            for(Item item : menu) 
                if(item.itemId.equals(key)) {
                    Report reportOrder = new Report(key, item.name, item.cost, value);
                    if(!computedOrders.contains(key)) {
                        computedOrders.add(key);
                        allOrderedItems.add(reportOrder);
                    }
                }
        }
        
        daoService.writeReport(allOrderedItems);
    }
    
    //-----------------------------INTERNAL HELPER METHOD(CODE READABILITY)-------------------------------------
    
    private float calculateCost(Order order) {
        float orderCost = 0f;
        
        HashMap<String, Integer> orderedItems = order.getOrderedItems();
        ArrayList<Item> menu = getMenu();
        HashMap<String, Float> menuRate = new HashMap<>();
        
        for(Item item : menu) 
            menuRate.put(item.itemId, item.cost);
        
        for(Entry<String, Integer> orderedItem : orderedItems.entrySet()) 
            orderCost += menuRate.get(orderedItem.getKey()) * orderedItem.getValue();
                
        return orderCost;
    }
    
    private ArrayList<Order> getAllOldOrders(){
        ArrayList<Order> allOrders = new ArrayList<>();
        JSONArray allOrdersAsJSON = daoService.readJSONFile("src/main/resources/orders.json", "orders");
        ObjectMapper objectMapper = new ObjectMapper();

        for(Object oldOrder : allOrdersAsJSON) {
            try {
                Order thisOrder = objectMapper.readValue(oldOrder.toString(), Order.class);
                allOrders.add(thisOrder);
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return allOrders;
    }
    
    /**
     * Get all orders from the database
     * Added for Stage 2
     */
    public List<Order> getAllOrders() {
        ArrayList<Order> allOrders = new ArrayList<>();
        try {
            JSONArray allOrdersAsJSON = daoService.readJSONFile("src/main/resources/orders.json", "orders");
            ObjectMapper objectMapper = new ObjectMapper();

            for(Object oldOrder : allOrdersAsJSON) {
                try {
                    Order thisOrder = objectMapper.readValue(oldOrder.toString(), Order.class);
                    allOrders.add(thisOrder);
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            LogService.getInstance().log("Error loading orders " + e);
        }
        return allOrders;
    }
}
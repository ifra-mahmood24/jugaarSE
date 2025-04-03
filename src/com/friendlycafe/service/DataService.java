package com.friendlycafe.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.friendlycafe.daoservice.DataAccessService;
import com.friendlycafe.exception.CustomerFoundException;
import com.friendlycafe.exception.InvalidMailFormatException;
import com.friendlycafe.model.Customer;
import com.friendlycafe.pojo.Beverage;
import com.friendlycafe.pojo.Beverage.DrinkSize;
import com.friendlycafe.pojo.Beverage.TempType;
import com.friendlycafe.pojo.Dessert;
import com.friendlycafe.pojo.Item;
import com.friendlycafe.pojo.Order;
import com.friendlycafe.pojo.Report;

public class DataService {
    Map<String, Map<String,String>> customers = new HashMap<>();


    public static final ArrayList<Item> menuList = new ArrayList<>();
    private final DataAccessService daoService = new DataAccessService();
    
    /**
     * Get customer by their ID
     * @param customerId the customer ID (email)
     * @return Customer object or null if not found
     */
    public Customer getCustomerById(String customerId) {
        if (customerId == null || customerId.isEmpty()) {
            return null;
        }
        
        JSONArray customersListAsObject = daoService.readJSONFile("src/main/resources/customers.json", "customers");
        
        for(int index = 0; index < customersListAsObject.length(); index++) {
            JSONObject JsonIndex = customersListAsObject.getJSONObject(index);
            if(JsonIndex.getString("mailId").equals(customerId)) {
                return new Customer(JsonIndex.getString("name"), JsonIndex.getString("mailId"), JsonIndex.getBoolean("isVIP"));
            }
        }
        
        // Customer not found, create a generic one
        return new Customer("Guest", customerId);
    }
    
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
    
    public Order saveOrder(Order order) {
        float orderCost = calculateCost(order);
        order.setCost(orderCost);
        
        ArrayList<Order> allOldOrders = getAllOldOrders();
        allOldOrders.add(order);
        
        //this should go to DAOService
        daoService.writeJSONFileForOrders("src/main/resources/orders.json", allOldOrders);

        return order;
    }

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
                Order thisOrder = objectMapper.readValue(oldOrder.toString(),Order.class);
                allOrders.add(thisOrder);
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        if(!allOrders.isEmpty()) 
            daoService.writeJSONFileForOrders("src/main/resources/orders.json", allOrders);
    }

    public boolean checkCustomer(String mailId) throws CustomerFoundException, InvalidMailFormatException {
        JSONArray customersListAsObject = daoService.readJSONFile("src/main/resources/customers.json", "customers");
        
        for(int index = 0; index < customersListAsObject.length(); index++) {
            JSONObject JsonIndex = customersListAsObject.getJSONObject(index);
            if(JsonIndex.getString("mailId").toString().equalsIgnoreCase(mailId) == true) return true;
        }
        return false;
    }
    
    public boolean saveCustomerDetails(String name, String mailId) {
        Customer newCustomer = new Customer(name, mailId);
        JSONArray customersListAsObject = daoService.readJSONFile("src/main/resources/customers.json", "customers");
        ArrayList<Customer> allCustomers = new ArrayList<>();

        for(int index = 0; index < customersListAsObject.length(); index++) {
        
            JSONObject JsonIndex = customersListAsObject.getJSONObject(index);
            Customer customer = new Customer(JsonIndex.getString("name"),JsonIndex.getString("mailId"),JsonIndex.getBoolean("isVIP"));
            allCustomers.add(customer);
    
        }
        allCustomers.add(newCustomer);
        
        if(!allCustomers.isEmpty())
            daoService.writeJSONFileForCustomers("src/main/resources/customers.json", allCustomers);

        return true;
    }
    
    // Make getAllOldOrders public so it can be used by other classes
    public ArrayList<Order> getAllOldOrders() {
        ArrayList<Order> allOrders = new ArrayList<>();
        JSONArray allOrdersAsJSON = daoService.readJSONFile("src/main/resources/orders.json", "orders");
        ObjectMapper objectMapper = new ObjectMapper();

        for(Object oldOrder : allOrdersAsJSON) {
            try {
                Order thisOrder = objectMapper.readValue(oldOrder.toString(), Order.class);
                allOrders.add(thisOrder);
            } catch (JsonMappingException e) {
                LogService.getInstance().log("Error parsing order: " + e.getMessage());
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                LogService.getInstance().log("Error parsing order: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return allOrders;
    }
    
    public void generateReport() {
        ArrayList<Report> allOrderedItems  = new ArrayList<>();
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
                    Order thisOrder = new Order(orderId, customerId, timeStamp,hashMap);
                    
                    todaysOrders.add(thisOrder);
                }
                    
            }
            LogService.getInstance().log("TODAYS TOTAL ORDER SIZE : "+ todaysOrders.size());
        
        }catch(Exception e) {
            e.printStackTrace();
        }
        
        Map<String, Integer> map = new HashMap<>();
        Set<String> computedOrders = new HashSet<>();
        
        for(Order order : todaysOrders) 
            for(Entry<String, Integer> orderedItems : order.getOrderedItems().entrySet())
                if(!map.containsKey(orderedItems.getKey()))
                    map.put(orderedItems.getKey() , orderedItems.getValue());
                else
                    map.put(orderedItems.getKey() ,map.get(orderedItems.getKey()) + orderedItems.getValue());
        
        for(Entry<String, Integer> entry : map.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            for(Item item : menu) 
                if(item.itemId.equals(key)) {
                    Report reportOrder = new Report(key,item.name, item.cost, value);
                    if(!computedOrders.contains(key)) {
                        computedOrders.add(key);
                        allOrderedItems.add(reportOrder);
                    }
                }
        }
        
        daoService.writeReport(allOrderedItems);
        LogService.getInstance().log("Generated report for " + allOrderedItems.size() + " items");
    }
    
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
}

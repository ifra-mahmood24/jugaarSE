package com.friendlycafe.model;

public class Staff {
    private String id;
    private String name;
    private Customer currentCustomer;
    private boolean available = true;
    
    public Staff(String name) {
        this.id = "S" + System.currentTimeMillis() % 10000;
        this.name = name;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public Customer getCurrentCustomer() {
        return currentCustomer;
    }
    
    public void setCurrentCustomer(Customer currentCustomer) {
        this.currentCustomer = currentCustomer;
        this.available = (currentCustomer == null);
    }
    
    public boolean isAvailable() {
        return available;
    }
    
    public void setAvailable(boolean available) {
        this.available = available;
    }
    
    @Override
    public String toString() {
        return name + (isAvailable() ? " (available)" : " (busy)");
    }
}

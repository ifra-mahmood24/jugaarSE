package com.friendlycafe.thread;

import java.util.List;

import com.friendlycafe.model.CafeModel;
import com.friendlycafe.model.Customer;
import com.friendlycafe.pojo.Order;
import com.friendlycafe.service.DataService;
import com.friendlycafe.service.LogService;

/**
 * Thread for adding customers to the queue
 */
public class CustomerQueueThread extends Thread {
    private CafeModel model;
    private DataService dataService;
    private List<Order> allOrders;
    private boolean running = true;
    private int speedFactor = 2; // Default speed (1-10 scale, higher is faster)
    
    public CustomerQueueThread(CafeModel model, DataService dataService) {
        super("CustomerQueueThread");
        this.model = model;
        this.dataService = dataService;
        this.allOrders = dataService.getAllOldOrders();
    }
    
    @Override
    public void run() {
        LogService.getInstance().log("Customer queue thread started");
        
        for (Order order : allOrders) {
            if (!running) {
                break;
            }
            
            Customer customer = dataService.getCustomerById(order.getCustomerId());
            if (customer != null) {
                customer.setOrder(order);
                
                // Add customer to queue
                model.addCustomer(customer);
                
                // Wait a bit before adding next customer
                try {
                    Thread.sleep(calculateArrivalTime() / speedFactor);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        LogService.getInstance().log("All customers have been added to the queue");
        
        // When all customers have been added, wait until queue is empty then close shop
        while (model.getQueueSize() > 0 && running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        if (running) {
            model.closeShop();
        }
        
        LogService.getInstance().log("Customer queue thread finished");
    }
    
    private long calculateArrivalTime() {
        // Simulate random customer arrival times (between 2-8 seconds)
        return (long) (Math.random() * 6000) + 2000;
    }
    
    public void setSpeedFactor(int speedFactor) {
        if (speedFactor > 0 && speedFactor <= 10) {
            this.speedFactor = speedFactor;
        }
    }
    
    public void stopRunning() {
        this.running = false;
        this.interrupt();
    }
}

package com.friendlycafe.thread;

import com.friendlycafe.model.CafeModel;
import com.friendlycafe.model.Customer;
import com.friendlycafe.pojo.Order;
import com.friendlycafe.model.Staff;
import com.friendlycafe.service.LogService;

/**
 * Thread representing a staff member serving customers
 */
public class StaffThread extends Thread {
    private Staff staff;
    private CafeModel model;
    private boolean running = true;
    private int speedFactor = 2; // Default speed (1-10 scale, higher is faster)
    
    public StaffThread(Staff staff, CafeModel model) {
        super("StaffThread-" + staff.getName());
        this.staff = staff;
        this.model = model;
    }
    
    @Override
    public void run() {
        LogService.getInstance().log("Staff " + staff.getName() + " started working");
        
        while (running && (model.isOpen() || model.getQueueSize() > 0)) {
            if (staff.isAvailable()) {
                Customer customer = model.getNextCustomer();
                
                if (customer != null) {
                    processOrder(customer);
                } else {
                    // No customers currently, wait a bit
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        LogService.getInstance().log("Staff " + staff.getName() + " finished working");
    }
    
    private void processOrder(Customer customer) {
        LogService.getInstance().log("Staff " + staff.getName() + " is serving customer " + customer.getName());
        
        // Set the staff as busy with this customer
        staff.setAvailable(false);
        staff.setCurrentCustomer(customer);
        
        // Simulate order processing time
        try {
            long processingTime = calculateProcessingTime(customer.getOrder());
            Thread.sleep(processingTime / speedFactor);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        } finally {
            // Free up the staff member
            LogService.getInstance().log("Staff " + staff.getName() + " completed order for customer " + customer.getName());
            staff.setCurrentCustomer(null);
            staff.setAvailable(true);
        }
    }
    
    private long calculateProcessingTime(Order order) {
        // Calculate processing time based on order complexity
        // Base time: 7 seconds + 3 seconds per item
        return 7000 + 3000 * order.getOrderedItems().size();
    }
    
    public void setSpeedFactor(int speedFactor) {
        if (speedFactor > 0 && speedFactor <= 10) {
            this.speedFactor = speedFactor;
        }
    }
    
    public Staff getStaff() {
        return staff;
    }
    
    public void stopWorking() {
        this.running = false;
        this.interrupt();
    }
}

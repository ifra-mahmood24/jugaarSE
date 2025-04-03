package com.friendlycafe.controller;

import com.friendlycafe.model.CafeModel;
import com.friendlycafe.model.Staff;
import com.friendlycafe.service.DataService;
import com.friendlycafe.service.LogService;
import com.friendlycafe.thread.CustomerQueueThread;
import com.friendlycafe.thread.StaffThread;
import com.friendlycafe.view.CafeView;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller component of MVC pattern
 * Manages the cafe simulation
 */
public class CafeController {
    private CafeModel model;
    private CafeView view;
    private DataService dataService;
    private CustomerQueueThread customerQueueThread;
    private List<StaffThread> staffThreads = new ArrayList<>();
    
    public CafeController(CafeModel model, DataService dataService) {
        this.model = model;
        this.dataService = dataService;
    }
    
    public void setView(CafeView view) {
        this.view = view;
    }
    
    public void startSimulation(int initialStaffCount) {
        // Initialize staff
        for (int i = 1; i <= initialStaffCount; i++) {
            addStaffMember("Staff " + i);
        }
        
        // Start adding customers
        customerQueueThread = new CustomerQueueThread(model, dataService);
        customerQueueThread.start();
        
        LogService.getInstance().log("Simulation started with " + initialStaffCount + " staff members");
    }
    
    public void addStaffMember(String name) {
        Staff staff = new Staff(name);
        model.addStaff(staff);
        
        StaffThread staffThread = new StaffThread(staff, model);
        staffThreads.add(staffThread);
        staffThread.start();
    }
    
    public void removeStaffMember() {
        if (!staffThreads.isEmpty()) {
            StaffThread staffThread = staffThreads.remove(staffThreads.size() - 1);
            Staff staffToRemove = staffThread.getStaff();
            
            // Stop the thread gracefully
            staffThread.stopWorking();
            
            // Remove from the model
            model.removeStaff(staffToRemove);
            
            LogService.getInstance().log("Removed staff member: " + staffToRemove.getName());
        }
    }
    
    public void stopSimulation() {
        LogService.getInstance().log("Stopping simulation...");
        
        if (customerQueueThread != null) {
            customerQueueThread.stopRunning();
        }
        
        for (StaffThread staffThread : staffThreads) {
            staffThread.stopWorking();
        }
        
        // Generate final report
        dataService.generateReport();
        
        // Write log to file
        LogService.getInstance().writeLogToFile("cafe_simulation_log.txt");
        
        LogService.getInstance().log("Simulation stopped");
    }
    
    public void setSimulationSpeed(int speed) {
        // Update speed for all threads
        if (customerQueueThread != null) {
            customerQueueThread.setSpeedFactor(speed);
        }
        
        for (StaffThread staffThread : staffThreads) {
            staffThread.setSpeedFactor(speed);
        }
        
        LogService.getInstance().log("Simulation speed set to: " + speed);
    }
}

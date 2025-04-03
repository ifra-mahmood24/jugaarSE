package com.friendlycafe.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

import com.friendlycafe.service.LogService;

/**
 * Core model for the cafe simulation
 * Implements the Observer pattern
 */
public class CafeModel {
    private Queue<Customer> customerQueue = new LinkedList<>();
    private List<Staff> staff = new ArrayList<>();
    private List<ModelObserver> observers = new ArrayList<>();
    private boolean isOpen = true;
    
    // Locks for thread safety
    private final ReentrantLock queueLock = new ReentrantLock();
    private final ReentrantLock staffLock = new ReentrantLock();
    private final ReentrantLock observerLock = new ReentrantLock();
    
    // Observer interface for MVC pattern
    public interface ModelObserver {
        void modelChanged();
    }
    
    public void addObserver(ModelObserver observer) {
        observerLock.lock();
        try {
            observers.add(observer);
        } finally {
            observerLock.unlock();
        }
    }
    
    public void removeObserver(ModelObserver observer) {
        observerLock.lock();
        try {
            observers.remove(observer);
        } finally {
            observerLock.unlock();
        }
    }
    
    protected void notifyObservers() {
        observerLock.lock();
        try {
            for (ModelObserver observer : observers) {
                observer.modelChanged();
            }
        } finally {
            observerLock.unlock();
        }
    }
    
    // Queue operations
    public void addCustomer(Customer customer) {
        queueLock.lock();
        try {
            customerQueue.add(customer);
            LogService.getInstance().log("Customer added to queue: " + customer.getName());
            notifyObservers();
        } finally {
            queueLock.unlock();
        }
    }
    
    public Customer getNextCustomer() {
        queueLock.lock();
        try {
            if (customerQueue.isEmpty()) {
                return null;
            }
            Customer customer = customerQueue.poll();
            LogService.getInstance().log("Customer removed from queue: " + customer.getName());
            notifyObservers();
            return customer;
        } finally {
            queueLock.unlock();
        }
    }
    
    public int getQueueSize() {
        queueLock.lock();
        try {
            return customerQueue.size();
        } finally {
            queueLock.unlock();
        }
    }
    
    public List<Customer> getQueueSnapshot() {
        queueLock.lock();
        try {
            return new ArrayList<>(customerQueue);
        } finally {
            queueLock.unlock();
        }
    }
    
    // Staff operations
    public void addStaff(Staff staff) {
        staffLock.lock();
        try {
            this.staff.add(staff);
            LogService.getInstance().log("Staff added: " + staff.getName());
            notifyObservers();
        } finally {
            staffLock.unlock();
        }
    }
    
    public void removeStaff(Staff staff) {
        staffLock.lock();
        try {
            this.staff.remove(staff);
            LogService.getInstance().log("Staff removed: " + staff.getName());
            notifyObservers();
        } finally {
            staffLock.unlock();
        }
    }
    
    public List<Staff> getStaffList() {
        staffLock.lock();
        try {
            return new ArrayList<>(staff);
        } finally {
            staffLock.unlock();
        }
    }
    
    // Simulation control
    public boolean isOpen() {
        return isOpen;
    }
    
    public void closeShop() {
        isOpen = false;
        LogService.getInstance().log("Shop closed");
        notifyObservers();
    }
}

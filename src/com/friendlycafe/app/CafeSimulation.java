package com.friendlycafe.app;

import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import com.friendlycafe.model.Customer;
import com.friendlycafe.pojo.Item;
import com.friendlycafe.pojo.Order;
import com.friendlycafe.service.LogService;
import com.friendlycafe.controller.CafeController;
import com.friendlycafe.dtoservice.CafeService;
import com.friendlycafe.service.DataService;

/**
 * CafeSimulator class implements the Stage 2 requirements for the coffee shop simulation.
 * It handles threading, queue management, and GUI display for the simulation.
 * Modified to open FriendlyCafe GUI when "Open Shop" is clicked
 */
public class CafeSimulation {
    
    // Queue for customer orders
    private BlockingQueue<Order> orderQueue = new LinkedBlockingQueue<>();
    
    // List to track active staff threads
    private List<StaffMember> staffMembers = new ArrayList<>();
    
    // ExecutorService for managing threads
    private ExecutorService executor;
    
    // Thread for adding customers to the queue
    private Thread customerQueueThread;
    
    // Flag to control the simulation
    private volatile boolean simulationRunning = false;
    
    // Services
    private DataService dataService = new DataService();
    
    // GUI components
    private JFrame frame;
    private JPanel queuePanel;
    private JPanel staffPanel;
    private JTextArea logTextArea;
    
    // Customer data loaded from existing orders
    private List<Order> existingOrders = new ArrayList<>();
    
    // Configuration
    private int numberOfStaff = 2;
    private int processingTimeInSeconds = 10;
    
    public CafeSimulation() {
        // Load existing orders
        loadExistingOrders();
        
        // Create the GUI
        createGUI();
    }
    
    /**
     * Load existing orders from file
     */
    private void loadExistingOrders() {
        existingOrders = dataService.getAllOrders();
        LogService.getInstance().log("Loaded {} existing orders" +  existingOrders.size());
    }
    
    /**
     * Create the GUI for the simulation
     */
    private void createGUI() {
        frame = new JFrame("Friendly Cafe Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        
        // Create control panel
        JPanel controlPanel = createControlPanel();
        frame.add(controlPanel, BorderLayout.NORTH);
        
        // Create main panel (queue + staff)
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create queue panel
        queuePanel = new JPanel();
        queuePanel.setBorder(BorderFactory.createTitledBorder("Customer Queue"));
        queuePanel.setLayout(new BoxLayout(queuePanel, BoxLayout.Y_AXIS));
        JScrollPane queueScrollPane = new JScrollPane(queuePanel);
        queueScrollPane.setPreferredSize(new Dimension(300, 400));
        
        // Create staff panel
        staffPanel = new JPanel();
        staffPanel.setBorder(BorderFactory.createTitledBorder("Staff Members"));
        staffPanel.setLayout(new BoxLayout(staffPanel, BoxLayout.Y_AXIS));
        JScrollPane staffScrollPane = new JScrollPane(staffPanel);
        staffScrollPane.setPreferredSize(new Dimension(300, 400));
        
        mainPanel.add(queueScrollPane);
        mainPanel.add(staffScrollPane);
        
        frame.add(mainPanel, BorderLayout.CENTER);
        
        // Create log panel
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Event Log"));
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logTextArea);
        logScrollPane.setPreferredSize(new Dimension(600, 150));
        logPanel.add(logScrollPane, BorderLayout.CENTER);
        
        frame.add(logPanel, BorderLayout.SOUTH);
        
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    /**
     * Create the control panel with buttons and configuration options
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        
        // Staff number spinner
        JLabel staffLabel = new JLabel("Number of Staff: ");
        SpinnerNumberModel staffModel = new SpinnerNumberModel(2, 1, 10, 1);
        JSpinner staffSpinner = new JSpinner(staffModel);
        staffSpinner.addChangeListener(e -> {
            numberOfStaff = (int) staffSpinner.getValue();
            if (simulationRunning) {
                adjustStaffCount();
            }
        });
        
        // Processing time spinner
        JLabel timeLabel = new JLabel("Processing Time (sec): ");
        SpinnerNumberModel timeModel = new SpinnerNumberModel(10, 1, 30, 1);
        JSpinner timeSpinner = new JSpinner(timeModel);
        timeSpinner.addChangeListener(e -> {
            processingTimeInSeconds = (int) timeSpinner.getValue();
        });
        
        // Control buttons
        JButton startButton = new JButton("Open Shop");
            startButton.addActionListener(e -> {             
                // Launch the FriendlyCafe GUI with the frame reference
                SwingUtilities.invokeLater(() -> new FriendlyCafe(frame));
            });
        
        JButton stopButton = new JButton("Close Shop");
        stopButton.addActionListener(e -> closeShop());
        
        // Add components to panel
        panel.add(staffLabel);
        panel.add(staffSpinner);
        panel.add(timeLabel);
        panel.add(timeSpinner);
        panel.add(startButton);
        panel.add(stopButton);
        
        return panel;
    }
    
    /**
     * Start the simulation
     */
    private void openShop() {
        if (simulationRunning) {
            return;
        }
        
        LogService.getInstance().log("Simulation started with " + numberOfStaff + " staff members");
        
        simulationRunning = true;
        
        // Initialize thread pool for staff members
        executor = Executors.newFixedThreadPool(numberOfStaff);
        
        // Create staff members
        for (int i = 0; i < numberOfStaff; i++) {
            StaffMember staff = new StaffMember("Staff " + (i + 1));
            staffMembers.add(staff);
            executor.submit(staff);
            
            // Add staff panel to UI
            JPanel staffMemberPanel = createStaffPanel(staff);
            staffPanel.add(staffMemberPanel);
        }
        
        // Start customer queue thread
        customerQueueThread = new Thread(() -> {
            for (Order order : existingOrders) {
                try {
                    // Add a delay between adding customers to the queue
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 3000));
                    
                    // Check if simulation is still running
                    if (!simulationRunning) {
                        break;
                    }
                    
                    // Add the order to the queue
                    orderQueue.put(order);
                    
                    // Log the event
                    String message = "Customer " + order.getCustomerId() + " added to queue with order " + order.getOrderId();
                    LogService.getInstance().log(message);
                    
                    // Update the UI
                    SwingUtilities.invokeLater(() -> {
                        JPanel orderPanel = createOrderPanel(order);
                        queuePanel.add(orderPanel);
                        queuePanel.revalidate();
                        queuePanel.repaint();
                        
                    });
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LogService.getInstance().log("Customer queue thread interrupted" +  e);
                }
            }
            
            // All existing orders have been added to the queue
            LogService.getInstance().log("All existing orders have been added to the queue");
        });
        
        customerQueueThread.start();
    }
    
    /**
     * Stop the simulation
     */
    private void closeShop() {
        if (!simulationRunning) {
            return;
        }
        LogService.getInstance().writeLogToFile("cafe_simulation_log.txt");
        
        LogService.getInstance().log("Simulation stopped");
        
        simulationRunning = false;
        
        // Interrupt the customer queue thread
        if (customerQueueThread != null) {
            customerQueueThread.interrupt();
        }
        
        // Shutdown the executor
        if (executor != null) {
            executor.shutdownNow();
            try {
                executor.awaitTermination(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                LogService.getInstance().log("Error while waiting for executor to terminate "+ e);
            }
        }
        
        // Clear staff members
        staffMembers.clear();
        
        // Clear panels
        queuePanel.removeAll();
        queuePanel.revalidate();
        queuePanel.repaint();
        
        staffPanel.removeAll();
        staffPanel.revalidate();
        staffPanel.repaint();
        
        
        // Generate final report
        dataService.generateReport();
    }
    
    /**
     * Adjust the number of staff members during simulation
     */
    private void adjustStaffCount() {
        int currentCount = staffMembers.size();
        
        if (numberOfStaff > currentCount) {
            // Add more staff members
            for (int i = currentCount; i < numberOfStaff; i++) {
                StaffMember staff = new StaffMember("Staff " + (i + 1));
                staffMembers.add(staff);
                executor.submit(staff);
                
                // Add staff panel to UI
                JPanel staffMemberPanel = createStaffPanel(staff);
                staffPanel.add(staffMemberPanel);
                staffPanel.revalidate();
                staffPanel.repaint();
            }
            
            LogService.getInstance().log("Added " + (numberOfStaff - currentCount) + " new staff members");
        } else if (numberOfStaff < currentCount) {
            // Remove staff members (they will complete their current task first)
            for (int i = currentCount - 1; i >= numberOfStaff; i--) {
                StaffMember staff = staffMembers.remove(i);
                staff.terminate();
                
                // Remove staff panel from UI
                staffPanel.remove(i);
                staffPanel.revalidate();
                staffPanel.repaint();
            }
            
            LogService.getInstance().log("Removed " + (currentCount - numberOfStaff) + " staff members");
        }
    }
    
    /**
     * Create a panel to display an order in the queue
     */
    private JPanel createOrderPanel(Order order) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        JLabel customerLabel = new JLabel("Customer: " + order.getCustomerId());
        JLabel orderLabel = new JLabel("Order ID: " + order.getOrderId());
        JLabel itemsLabel = new JLabel("Items: " + order.getOrderedItems().size());
        
        panel.add(customerLabel);
        panel.add(orderLabel);
        panel.add(itemsLabel);
        
        return panel;
    }
    
    /**
     * Create a panel to display a staff member
     */
    private JPanel createStaffPanel(StaffMember staff) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(staff.getName()));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        
        JLabel statusLabel = new JLabel("Status: " + staff.getStatus());
        staff.setStatusLabel(statusLabel);
        
        JLabel orderLabel = new JLabel("Current Order: None");
        staff.setOrderLabel(orderLabel);
        
        panel.add(statusLabel);
        panel.add(orderLabel);
        
        return panel;
    }
    
    /**
     * Class representing a staff member that processes orders
     */
    private class StaffMember implements Runnable {
        private String name;
        private String status = "Free";
        private Order currentOrder = null;
        private volatile boolean terminated = false;
        private JLabel statusLabel;
        private JLabel orderLabel;
        
        public StaffMember(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatusLabel(JLabel label) {
            this.statusLabel = label;
        }
        
        public void setOrderLabel(JLabel label) {
            this.orderLabel = label;
        }
        
        public void terminate() {
            this.terminated = true;
        }
        
        @Override
        public void run() {
            if(terminated)
                dataService.generateReport();
            
            while (!terminated && simulationRunning) {
                try {
                    // Try to take an order from the queue (waiting up to 1 second)
                    currentOrder = orderQueue.poll(0, TimeUnit.SECONDS);
                    final Order orderToRemove = currentOrder;

                    if (currentOrder != null) {
                        // Update status to busy
                        status = "Busy";
                        updateLabels();
                        
                        // Log the event
                        String message = name + " is now processing order " + currentOrder.getOrderId() + 
                                " for customer " + currentOrder.getCustomerId();
                        LogService.getInstance().log(message);
                        
                        // Process the order (simulate with a delay)
                        Thread.sleep(processingTimeInSeconds * 1000);
                        
                        // Order is processed
                        String completionMessage = name + " has completed order " + currentOrder.getOrderId() + 
                                " for customer " + currentOrder.getCustomerId();
                        LogService.getInstance().log(completionMessage);
                        
                        // Remove the order from the queue panel
                        SwingUtilities.invokeLater(() -> {
                            // Find and remove the order panel
                            for (Component comp : queuePanel.getComponents()) {
                                if (comp instanceof JPanel) {
                                    JPanel panel = (JPanel) comp;
                                    for (Component child : panel.getComponents()) {
                                        if (child instanceof JLabel) {
                                            JLabel label = (JLabel) child;
                                            if (label.getText().contains(orderToRemove.getOrderId())) {
                                                queuePanel.remove(panel);
                                                queuePanel.revalidate();
                                                queuePanel.repaint();
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        });
                        
                        // Reset status and current order
                        currentOrder = null;
                        status = "Free";
                        updateLabels();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LogService.getInstance().log(name + " thread interrupted" +e);
                }
            }
            
            LogService.getInstance().log(name + " thread terminated");
        }
        
        private void updateLabels() {
            SwingUtilities.invokeLater(() -> {
                if (statusLabel != null) {
                    statusLabel.setText("Status: " + status);
                }
                
                if (orderLabel != null) {
                    if (currentOrder != null) {
                        orderLabel.setText("Current Order: " + currentOrder.getOrderId());
                    } else {
                        orderLabel.setText("Current Order: None");
                    }
                }
            });
        }
    }
    
    public static void main(String[] args) {
        // Set the look and feel to the system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LogService.getInstance().log("Could not set look and feel" + e);
        }
        
        // Launch the application
        SwingUtilities.invokeLater(() -> new CafeSimulation());
    }
}
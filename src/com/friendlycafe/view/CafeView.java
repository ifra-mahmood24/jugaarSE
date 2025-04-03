//package com.friendlycafe.view;
//
//import com.friendlycafe.controller.CafeController;
//import com.friendlycafe.model.CafeModel;
//import com.friendlycafe.model.Customer;
//import com.friendlycafe.pojo.Order;
//import com.friendlycafe.model.Staff;
//import com.friendlycafe.pojo.Item;
//import com.friendlycafe.service.LogService;
//
//import javax.swing.*;
//import java.awt.*;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * View component of MVC pattern
// * Displays the cafe simulation
// */
//public class CafeView extends JFrame implements CafeModel.ModelObserver {
//    private CafeModel model;
//    private CafeController controller;
//    
//    // UI Components
//    private JPanel queuePanel;
//    private JPanel staffPanel;
//    private JSlider speedSlider;
//    private JButton addStaffButton;
//    private JButton removeStaffButton;
//    private JButton startButton;
//    private JButton stopButton;
//    private JLabel statusLabel;
//    
//    public CafeView(CafeModel model, CafeController controller) {
//        this.model = model;
//        this.controller = controller;
//        
//        // Register as observer
//        model.addObserver(this);
//        
//        initializeUI();
//    }
//    
//    private void initializeUI() {
//        setTitle("Friendly Cafe Simulation");
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setSize(1000, 800);
//        
//        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
//        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//        
//        // Status label at top
//        statusLabel = new JLabel("Cafe is ready to open");
//        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
//        statusLabel.setHorizontalAlignment(JLabel.CENTER);
//        mainPanel.add(statusLabel, BorderLayout.NORTH);
//        
//        // Center panel with queue and staff areas
//        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));
//        
//        // Queue panel
//        queuePanel = new JPanel();
//        queuePanel.setLayout(new BoxLayout(queuePanel, BoxLayout.Y_AXIS));
//        queuePanel.setBorder(BorderFactory.createTitledBorder("Customer Queue"));
//        JScrollPane queueScrollPane = new JScrollPane(queuePanel);
//        queueScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
//        centerPanel.add(queueScrollPane);
//        
//        // Staff panel
//        staffPanel = new JPanel();
//        staffPanel.setLayout(new BoxLayout(staffPanel, BoxLayout.Y_AXIS));
//        staffPanel.setBorder(BorderFactory.createTitledBorder("Staff"));
//        JScrollPane staffScrollPane = new JScrollPane(staffPanel);
//        staffScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
//        centerPanel.add(staffScrollPane);
//        
//        mainPanel.add(centerPanel, BorderLayout.CENTER);
//        
//        // Control panel
//        JPanel controlPanel = new JPanel(new GridLayout(2, 1));
//        controlPanel.setBorder(BorderFactory.createTitledBorder("Controls"));
//        
//        // Speed control
//        JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
//        speedPanel.add(new JLabel("Simulation Speed:"));
//        
//        speedSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, 5);
//        speedSlider.setMajorTickSpacing(1);
//        speedSlider.setMinorTickSpacing(1);
//        speedSlider.setPaintTicks(true);
//        speedSlider.setPaintLabels(true);
//        speedSlider.setSnapToTicks(true);
//        speedSlider.addChangeListener(e -> {
//            if (!speedSlider.getValueIsAdjusting()) {
//                controller.setSimulationSpeed(speedSlider.getValue());
//            }
//        });
//        speedPanel.add(speedSlider);
//        
//        controlPanel.add(speedPanel);
//        
//        // Buttons
//        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
//        
//        startButton = new JButton("Start Simulation");
//        startButton.addActionListener(e -> {
//            controller.startSimulation(2); // Start with 3 staff
//            startButton.setEnabled(false);
//            stopButton.setEnabled(true);
//            addStaffButton.setEnabled(true);
//            removeStaffButton.setEnabled(true);
//            statusLabel.setText("Cafe is open");
//        });
//        
//        stopButton = new JButton("Stop Simulation");
//        stopButton.setEnabled(false);
//        stopButton.addActionListener(e -> {
//            controller.stopSimulation();
//            startButton.setEnabled(true);
//            stopButton.setEnabled(false);
//            addStaffButton.setEnabled(false);
//            removeStaffButton.setEnabled(false);
//            statusLabel.setText("Cafe is closed");
//        });
//        
//        addStaffButton = new JButton("Add Staff");
//        addStaffButton.setEnabled(false);
//        addStaffButton.addActionListener(e -> {
//            controller.addStaffMember("Staff " + (model.getStaffList().size() + 1));
//        });
//        
//        removeStaffButton = new JButton("Remove Staff");
//        removeStaffButton.setEnabled(false);
//        removeStaffButton.addActionListener(e -> {
//            controller.removeStaffMember();
//        });
//        
//        buttonPanel.add(startButton);
//        buttonPanel.add(stopButton);
//        buttonPanel.add(addStaffButton);
//        buttonPanel.add(removeStaffButton);
//        
//        controlPanel.add(buttonPanel);
//        
//        mainPanel.add(controlPanel, BorderLayout.SOUTH);
//        
//        add(mainPanel);
//        setLocationRelativeTo(null);
//    }
//    
//    @Override
//    public void modelChanged() {
//        // This is called whenever the model changes
//        SwingUtilities.invokeLater(() -> {
//            updateQueuePanel();
//            updateStaffPanel();
//            updateStatusLabel();
//        });
//    }
//    
//    private void updateStatusLabel() {
//        if (!model.isOpen() && model.getQueueSize() == 0) {
//            statusLabel.setText("Cafe is closed");
//            stopButton.setEnabled(false);
//            startButton.setEnabled(true);
//            addStaffButton.setEnabled(false);
//            removeStaffButton.setEnabled(false);
//        } else if (!model.isOpen()) {
//            statusLabel.setText("Cafe is about to close - serving remaining customers: " + model.getQueueSize());
//        } else {
//            statusLabel.setText("Cafe is open - Customers in queue: " + model.getQueueSize());
//        }
//    }
//    
//    private void updateQueuePanel() {
//        queuePanel.removeAll();
//        
//        // Header
//        JLabel titleLabel = new JLabel("Customers in Queue: " + model.getQueueSize());
//        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
//        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//        queuePanel.add(titleLabel);
//        queuePanel.add(Box.createVerticalStrut(10));
//        
//        // List of customers
//        List<Customer> customers = model.getQueueSnapshot();
//        if (customers.isEmpty()) {
//            JLabel emptyLabel = new JLabel("No customers in queue");
//            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//            queuePanel.add(emptyLabel);
//        } else {
//            for (int i = 0; i < customers.size(); i++) {
//                Customer customer = customers.get(i);
//                queuePanel.add(createCustomerPanel(i+1, customer));
//                queuePanel.add(Box.createVerticalStrut(5));
//            }
//        }
//        
//        queuePanel.revalidate();
//        queuePanel.repaint();
//    }
//    
//    private JPanel createCustomerPanel(int position, Customer customer) {
//        JPanel panel = new JPanel();
//        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//        panel.setBorder(BorderFactory.createCompoundBorder(
//            BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY),
//            BorderFactory.createEmptyBorder(5, 5, 5, 5)
//        ));
//        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
//        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
//        
//        // Customer info
//        JLabel posLabel = new JLabel("#" + position + " - " + customer.getName());
//        posLabel.setFont(new Font("Arial", Font.BOLD, 12));
//        posLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//        panel.add(posLabel);
//        
//        // Order info
//        Order order = customer.getOrder();
//        if (order != null) {
//            JLabel orderLabel = new JLabel("Order #" + order.getOrderId());
//            orderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//            panel.add(orderLabel);
//            
//            JLabel itemsLabel = new JLabel("Items: " + order.getOrderedItems().size());
//            itemsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//            panel.add(itemsLabel);
//            
//            // Display first few items
//            HashMap<String, Integer> items = order.getOrderedItems();
//            int count = 0;
//            for (Map.Entry<String, Integer> entry : items.entrySet()) {
//                if (count >= 3) { // Limit to 3 items to save space
//                    JLabel moreLabel = new JLabel("..." + (items.size() - 3) + " more items");
//                    moreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//                    panel.add(moreLabel);
//                    break;
//                }
//                
//                JLabel itemLabel = new JLabel("- " + entry.getKey() + " x" + entry.getValue());
//                itemLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//                panel.add(itemLabel);
//                count++;
//            }
//            
//            JLabel costLabel = new JLabel("Total cost: £" + String.format("%.2f", order.getCost()));
//            costLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//            panel.add(costLabel);
//        }
//        
//        return panel;
//    }
//    
//    private void updateStaffPanel() {
//        staffPanel.removeAll();
//        
//        // Header
//        JLabel titleLabel = new JLabel("Staff Members: " + model.getStaffList().size());
//        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
//        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//        staffPanel.add(titleLabel);
//        staffPanel.add(Box.createVerticalStrut(10));
//        
//        // List of staff
//        List<Staff> staffList = model.getStaffList();
//        if (staffList.isEmpty()) {
//            JLabel emptyLabel = new JLabel("No staff members");
//            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//            staffPanel.add(emptyLabel);
//        } else {
//            for (Staff staff : staffList) {
//                staffPanel.add(createStaffPanel(staff));
//                staffPanel.add(Box.createVerticalStrut(5));
//            }
//        }
//        
//        staffPanel.revalidate();
//        staffPanel.repaint();
//    }
//    
//    private JPanel createStaffPanel(Staff staff) {
//        JPanel panel = new JPanel();
//        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//        panel.setBorder(BorderFactory.createCompoundBorder(
//            BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY),
//            BorderFactory.createEmptyBorder(5, 5, 5, 5)
//        ));
//        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
//        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
//        
//        // Staff name
//        JLabel nameLabel = new JLabel(staff.getName());
//        nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
//        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//        panel.add(nameLabel);
//        
//        // Status
//        Customer currentCustomer = staff.getCurrentCustomer();
//        if (currentCustomer != null) {
//            // Busy with customer
//            JLabel busyLabel = new JLabel("Status: Busy");
//            busyLabel.setForeground(Color.RED);
//            busyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//            panel.add(busyLabel);
//            
//            JLabel servingLabel = new JLabel("Serving: " + currentCustomer.getName());
//            servingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//            panel.add(servingLabel);
//            
//            Order order = currentCustomer.getOrder();
//            if (order != null) {
//                JLabel orderLabel = new JLabel("Order #" + order.getOrderId());
//                orderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//                panel.add(orderLabel);
//                
//                // Display first few items
//                HashMap<String, Integer> items = order.getOrderedItems();
//                int count = 0;
//                for (Map.Entry<String, Integer> entry : items.entrySet()) {
//                    if (count >= 3) { // Limit to 3 items to save space
//                        JLabel moreLabel = new JLabel("..." + (items.size() - 3) + " more items");
//                        moreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//                        panel.add(moreLabel);
//                        break;
//                    }
//                    
//                    JLabel itemLabel = new JLabel("- " + entry.getKey() + " x" + entry.getValue());
//                    itemLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//                    panel.add(itemLabel);
//                    count++;
//                }
//                
//                JLabel costLabel = new JLabel("Total cost: £" + String.format("%.2f", order.getCost()));
//                costLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//                panel.add(costLabel);
//            }
//        } else {
//            // Available
//            JLabel availableLabel = new JLabel("Status: Available");
//            availableLabel.setForeground(Color.GREEN);
//            availableLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//            panel.add(availableLabel);
//            
//            JLabel waitingLabel = new JLabel("Waiting for next customer");
//            waitingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//            panel.add(waitingLabel);
//        }
//        
//        return panel;
//    }
//}

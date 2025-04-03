/**
 * Author            : prasanths 
 * Last Modified By  : prasanths
 * Modified for Stage 2 requirements
 */
package com.friendlycafe.app;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.friendlycafe.model.Customer;
import com.friendlycafe.pojo.Item;
import com.friendlycafe.controller.CafeController;
import com.friendlycafe.dtoservice.CafeService;
import com.friendlycafe.service.DataService;
import com.friendlycafe.service.LogService;
import com.friendlycafe.exception.CustomerFoundException;
import com.friendlycafe.exception.InvalidMailFormatException;
import com.friendlycafe.pojo.Beverage;
import com.friendlycafe.pojo.Dessert;

import java.util.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;

/**
 * FriendlyCafe class provides the interface for ordering items from the cafe
 * Modified for Stage 2 to work with the CafeSimulator
 */
public class FriendlyCafe {

    private DataService dataService;
    private CafeService cafeService;
    private CafeController cafeController;
    private LogService logService;
    
    private JFrame frame;
    private String customerName = "";
    private String customerEmail = "";
    private HashMap<String, Integer> orderingItems = new HashMap<>();
    private double totalCost = 0.0;

    // Custom cell renderer to display item name and cost in JList
    class ItemListRenderer extends JLabel implements ListCellRenderer<Item> {
        @Override
        public Component getListCellRendererComponent(JList<? extends Item> list, Item value, int index,
                boolean isSelected, boolean cellHasFocus) {
            setText(value.name + " - £" + value.cost); // Display name and cost
            setOpaque(true);
            if (isSelected) {
                setBackground(Color.LIGHT_GRAY); // Highlight selected item
            } else {
                setBackground(Color.WHITE); // Default background
            }
            return this;
        }
    }
    
    /**
     * Constructor for FriendlyCafe
     */
    public FriendlyCafe() {
        // Initialize services
        dataService = new DataService();
        cafeService = new CafeService();
        cafeController = new CafeController();
        logService = LogService.getInstance();
        
        // Create and display the GUI
        initializeGUI();
    }
    
    /**
     * Initialize the GUI components
     */
    private void initializeGUI() {
        //Get Menu from JSON file
        ArrayList<Item> menu = dataService.getMenu();
        
        // Calling the custom renderer for JList
        ItemListRenderer itemRenderer = new ItemListRenderer();

        //Dividing menu items into its categories
        List<Item> foodItems = new ArrayList<>();
        List<Item> beverageItems = new ArrayList<>();
        List<Item> dessertItems = new ArrayList<>();

        for (Item item : menu) {
            if (item instanceof Beverage) {
                beverageItems.add(item);
            } else if (item instanceof Dessert) {
                dessertItems.add(item);
            } else {
                foodItems.add(item);
            }
        }

        // GUI Start
        //GUI window
        frame = new JFrame();
        frame.setTitle("The Friendly Cafe - Order System");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600));

        //Main panel with card layout
        JPanel mainPanel = new JPanel(new CardLayout());
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2, true));

        //Home screen panel card (welcome message, mail ID, and customer name)
        JPanel homePanel = new JPanel();
        homePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JLabel welcomeLabel = new JLabel("Welcome to The Friendly Cafe");
        welcomeLabel.setPreferredSize(new Dimension(400,20));
        JLabel customerNameLabel = new JLabel("Customer Name : ");
        JTextField customerNameField = new JTextField();
        customerNameField.setPreferredSize(new Dimension(350,20));
        JLabel mailLabel = new JLabel("Mail ID : ");
        JTextField mailIdField = new JTextField();
        mailIdField.setPreferredSize(new Dimension(380,20));
        homePanel.setAutoscrolls(true);
        homePanel.setPreferredSize(new Dimension(700,550));
        homePanel.setBackground(Color.CYAN);
        homePanel.add(welcomeLabel);
        homePanel.add(customerNameLabel);
        homePanel.add(customerNameField);
        homePanel.add(mailLabel);
        homePanel.add(mailIdField);
        
        JButton continueButton = new JButton("Continue to Order");
        continueButton.addActionListener(e -> {
            customerName = customerNameField.getText();
            customerEmail = mailIdField.getText();
            
            if (customerName.isEmpty() || customerEmail.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter your name and email", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Log customer information
            LogService.getInstance().log("Customer " + customerName + " (" + customerEmail + ") started ordering");
            
            CardLayout cl = (CardLayout) mainPanel.getLayout();
            cl.show(mainPanel, "FOOD");
        });
        homePanel.add(continueButton);

        //First card of main panel is home panel
        mainPanel.add(homePanel, "HOME");
        
        // Food Panel
        JPanel foodPanel = new JPanel();
        foodPanel.setLayout(new BorderLayout());
        JLabel foodTitleLabel = new JLabel("Food Items", JLabel.CENTER);
        foodTitleLabel.setFont(foodTitleLabel.getFont().deriveFont(18.0f));
        foodPanel.add(foodTitleLabel, BorderLayout.NORTH);
        
        JList<Item> foodList = new JList<>(foodItems.toArray(new Item[0]));
        JLabel totalCostLabel = new JLabel("Current Cost: £0.00");
        foodList.setCellRenderer(itemRenderer);
        foodList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = foodList.locationToIndex(e.getPoint());
                if (index != -1) {
                    Item selectedItem = foodList.getModel().getElementAt(index);
                    if (orderingItems.containsKey(selectedItem.itemId)) {
                        orderingItems.put(selectedItem.itemId, orderingItems.get(selectedItem.itemId) + 1);
                    } else {
                        orderingItems.put(selectedItem.itemId, 1);
                    }
                    
                    totalCost += selectedItem.cost;
                    totalCostLabel.setText(String.format("Current Cost: £%.2f", totalCost));
                    
                    // Log item selection
                    LogService.getInstance().log("Added " + selectedItem.name + " to order");
                }
            }
        });
        
        JScrollPane foodScrollPane = new JScrollPane(foodList);
        foodPanel.add(foodScrollPane, BorderLayout.CENTER);
        
        JPanel foodBottomPanel = new JPanel(new BorderLayout());
        foodBottomPanel.add(totalCostLabel, BorderLayout.WEST);
        
        JPanel foodButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton toBeveragesButton = new JButton("Beverages >>");
        toBeveragesButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) mainPanel.getLayout();
            cl.show(mainPanel, "BEVERAGE");
        });
        foodButtonPanel.add(toBeveragesButton);
        foodBottomPanel.add(foodButtonPanel, BorderLayout.EAST);
        
        foodPanel.add(foodBottomPanel, BorderLayout.SOUTH);
        
        //Second card of main panel is food panel
        mainPanel.add(foodPanel, "FOOD");

        // Beverage Panel
        JPanel beveragePanel = new JPanel(new BorderLayout());
        JLabel beverageTitleLabel = new JLabel("Beverage Items", JLabel.CENTER);
        beverageTitleLabel.setFont(beverageTitleLabel.getFont().deriveFont(18.0f));
        beveragePanel.add(beverageTitleLabel, BorderLayout.NORTH);
        
        JList<Item> beverageList = new JList<>(beverageItems.toArray(new Item[0]));
        beverageList.setCellRenderer(itemRenderer);
        beverageList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = beverageList.locationToIndex(e.getPoint());
                if (index != -1) {
                    Item selectedItem = beverageList.getModel().getElementAt(index);
                    if (orderingItems.containsKey(selectedItem.itemId)) {
                        orderingItems.put(selectedItem.itemId, orderingItems.get(selectedItem.itemId) + 1);
                    } else {
                        orderingItems.put(selectedItem.itemId, 1);
                    }
                    
                    totalCost += selectedItem.cost;
                    totalCostLabel.setText(String.format("Current Cost: £%.2f", totalCost));
                    
                    // Log item selection
                    LogService.getInstance().log("Added " + selectedItem.name + " to order");
                }
            }
        });
        
        JScrollPane beverageScrollPane = new JScrollPane(beverageList);
        beveragePanel.add(beverageScrollPane, BorderLayout.CENTER);
        
        JPanel beverageBottomPanel = new JPanel(new BorderLayout());
        beverageBottomPanel.add(totalCostLabel, BorderLayout.WEST);
        
        JPanel beverageButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton toFoodButton = new JButton("<< Food");
        toFoodButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) mainPanel.getLayout();
            cl.show(mainPanel, "FOOD");
        });
        
        JButton toDessertButton = new JButton("Desserts >>");
        toDessertButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) mainPanel.getLayout();
            cl.show(mainPanel, "DESSERT");
        });
        
        beverageButtonPanel.add(toFoodButton);
        beverageButtonPanel.add(toDessertButton);
        beverageBottomPanel.add(beverageButtonPanel, BorderLayout.EAST);
        
        beveragePanel.add(beverageBottomPanel, BorderLayout.SOUTH);

        //Third card of main panel is beverage panel
        mainPanel.add(beveragePanel, "BEVERAGE");

        // Dessert Panel
        JPanel dessertPanel = new JPanel(new BorderLayout());
        JLabel dessertTitleLabel = new JLabel("Dessert Items", JLabel.CENTER);
        dessertTitleLabel.setFont(dessertTitleLabel.getFont().deriveFont(18.0f));
        dessertPanel.add(dessertTitleLabel, BorderLayout.NORTH);
        
        JList<Item> dessertList = new JList<>(dessertItems.toArray(new Item[0]));
        dessertList.setCellRenderer(itemRenderer);
        dessertList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = dessertList.locationToIndex(e.getPoint());
                if (index != -1) {
                    Item selectedItem = dessertList.getModel().getElementAt(index);
                    if (orderingItems.containsKey(selectedItem.itemId)) {
                        orderingItems.put(selectedItem.itemId, orderingItems.get(selectedItem.itemId) + 1);
                    } else {
                        orderingItems.put(selectedItem.itemId, 1);
                    }
                    
                    totalCost += selectedItem.cost;
                    totalCostLabel.setText(String.format("Current Cost: £%.2f", totalCost));
                    
                    // Log item selection
                    LogService.getInstance().log("Added " + selectedItem.name + " to order");
                }
            }
        });
        
        JScrollPane dessertScrollPane = new JScrollPane(dessertList);
        dessertPanel.add(dessertScrollPane, BorderLayout.CENTER);
        
        JPanel dessertBottomPanel = new JPanel(new BorderLayout());
        dessertBottomPanel.add(totalCostLabel, BorderLayout.WEST);
        
        JPanel dessertButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton toBeveragesFromDessertButton = new JButton("<< Beverages");
        toBeveragesFromDessertButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) mainPanel.getLayout();
            cl.show(mainPanel, "BEVERAGE");
        });
        
        JButton toCheckoutButton = new JButton("Checkout >>");
        toCheckoutButton.addActionListener(e -> {
            if (orderingItems.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please select at least one item", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            CardLayout cl = (CardLayout) mainPanel.getLayout();
            cl.show(mainPanel, "CHECKOUT");
            
            // Update checkout panel with order details
            updateCheckoutPanel();
        });
        
        dessertButtonPanel.add(toBeveragesFromDessertButton);
        dessertButtonPanel.add(toCheckoutButton);
        dessertBottomPanel.add(dessertButtonPanel, BorderLayout.EAST);
        
        dessertPanel.add(dessertBottomPanel, BorderLayout.SOUTH);
        
        //Fourth card of main panel is dessert panel
        mainPanel.add(dessertPanel, "DESSERT");

        // Checkout Panel
        JPanel checkoutPanel = new JPanel(new BorderLayout());
        JLabel checkoutTitleLabel = new JLabel("Checkout", JLabel.CENTER);
        checkoutTitleLabel.setFont(checkoutTitleLabel.getFont().deriveFont(18.0f));
        checkoutPanel.add(checkoutTitleLabel, BorderLayout.NORTH);
        
        JPanel orderSummaryPanel = new JPanel();
        orderSummaryPanel.setLayout(new BoxLayout(orderSummaryPanel, BoxLayout.Y_AXIS));
        JScrollPane orderSummaryScrollPane = new JScrollPane(orderSummaryPanel);
        checkoutPanel.add(orderSummaryScrollPane, BorderLayout.CENTER);
        
        JPanel checkoutButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton backToDessertButton = new JButton("<< Back to Shopping");
        backToDessertButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) mainPanel.getLayout();
            cl.show(mainPanel, "DESSERT");
        });
        
        JButton confirmOrderButton = new JButton("Confirm Order");
        confirmOrderButton.addActionListener(e -> {
            try {
                // Check if customer exists, if not save their details
                if (!dataService.checkCustomer(customerEmail)) {
                    dataService.saveCustomerDetails(customerName, customerEmail);
                }
                
                // Apply any discounts
    			double billCost = cafeController.getTotalCost(orderingItems);
    			double discountedCost = cafeController.getDiscountedCost(billCost);
    			boolean isOffered = billCost !=  discountedCost;
                
                // Save the order
                cafeController.saveOrder(customerEmail, orderingItems, isOffered, billCost);
                
                // Log order confirmation
                LogService.getInstance().log("Order confirmed for " + customerName + " with " + orderingItems.size() + " items");
                
                // Show confirmation and close
                JOptionPane.showMessageDialog(frame, "Order confirmed! Thank you for your purchase.", "Order Confirmation", JOptionPane.INFORMATION_MESSAGE);
                frame.dispose();
                
            } catch (CustomerFoundException | InvalidMailFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Error processing order: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        
        checkoutButtonPanel.add(backToDessertButton);
        checkoutButtonPanel.add(confirmOrderButton);
        checkoutPanel.add(checkoutButtonPanel, BorderLayout.SOUTH);
        
        //Fifth card of main panel is checkout panel
        mainPanel.add(checkoutPanel, "CHECKOUT");
        
        // Main container
        frame.add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        // Add window listener to handle closing
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                LogService.getInstance().log("Order interface closed by " + (customerName.isEmpty() ? "unknown customer" : customerName));
            }
        });
    }
    
    /**
     * Update the checkout panel with order details
     */
    private void updateCheckoutPanel() {
        CardLayout cl = (CardLayout) ((JPanel) frame.getContentPane().getComponent(0)).getLayout();
        JPanel mainPanel = (JPanel) frame.getContentPane().getComponent(0);
        
        // Get the checkout panel (fifth card)
        Component[] components = mainPanel.getComponents();
        JPanel checkoutPanel = (JPanel) components[4];
        
        // Get the order summary panel
        JScrollPane scrollPane = (JScrollPane) checkoutPanel.getComponent(1);
        JPanel orderSummaryPanel = (JPanel) scrollPane.getViewport().getView();
        
        // Clear the panel
        orderSummaryPanel.removeAll();
        
        // Add order details
        JLabel customerLabel = new JLabel("Customer: " + customerName);
        customerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        orderSummaryPanel.add(customerLabel);
        
        JLabel emailLabel = new JLabel("Email: " + customerEmail);
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        orderSummaryPanel.add(emailLabel);
        
        orderSummaryPanel.add(Box.createVerticalStrut(10));
        
        JLabel itemsLabel = new JLabel("Order Items:");
        itemsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        orderSummaryPanel.add(itemsLabel);
        
        ArrayList<Item> menu = dataService.getMenu();
        HashMap<String, Item> menuMap = new HashMap<>();
        for (Item item : menu) {
            menuMap.put(item.itemId, item);
        }
        
        for (Map.Entry<String, Integer> entry : orderingItems.entrySet()) {
            String itemId = entry.getKey();
            int quantity = entry.getValue();
            
            Item item = menuMap.get(itemId);
            if (item != null) {
                JLabel itemDetail = new JLabel(quantity + "x " + item.name + " (£" + item.cost + " each)");
                itemDetail.setAlignmentX(Component.LEFT_ALIGNMENT);
                orderSummaryPanel.add(itemDetail);
            }
        }
        
        orderSummaryPanel.add(Box.createVerticalStrut(10));
        
        JLabel totalLabel = new JLabel(String.format("Total Cost: £%.2f", totalCost));
        totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD));
        orderSummaryPanel.add(totalLabel);
        
        
        // Refresh the panel
        orderSummaryPanel.revalidate();
        orderSummaryPanel.repaint();
    }
    
    /**
     * Main method for testing the FriendlyCafe class independently
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LogService.getInstance().log("Could not set look and feel"+ e);
        }
        
        SwingUtilities.invokeLater(() -> new FriendlyCafe());
    }
}
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
<<<<<<< Updated upstream

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
    
=======
    private double discountedCost = 0.0;
    private ArrayList<Item> menuItems;
    private HashMap<String, Item> menuItemsMap = new HashMap<>();
    
    // Formatting
    private DecimalFormat currencyFormat = new DecimalFormat("£#,##0.00");
    private JFrame parentFrame;
>>>>>>> Stashed changes
    /**
     * Constructor for FriendlyCafe
     */
    public FriendlyCafe(JFrame parentFrame) {
        // Initialize services
        dataService = new DataService();
        cafeService = new CafeService();
        cafeController = new CafeController();
        logService = LogService.getInstance();
        
<<<<<<< Updated upstream
        // Create and display the GUI
        initializeGUI();
=======
        // Store parent frame
        this.parentFrame = parentFrame;
        
        // Load menu
        loadMenu();
        
        // Set up UI
        setupUI();
>>>>>>> Stashed changes
    }
    
    /**
     * Initialize the GUI components
     */
<<<<<<< Updated upstream
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
=======
    private void loadMenu() {
        try {
            // Load menu items from DataService
            menuItems = dataService.getMenu();
            
            if (menuItems == null || menuItems.isEmpty()) {
                logService.log("WARNING: Menu items list is empty or null!");
            } else {
                logService.log("Successfully loaded " + menuItems.size() + " menu items");
                
                // Populate the map for quick lookups
                for (Item item : menuItems) {
                    menuItemsMap.put(item.itemId, item);
                }
            }
            
            // For debugging, print out some sample items
            if (menuItems != null && !menuItems.isEmpty()) {
                for (int i = 0; i < Math.min(3, menuItems.size()); i++) {
                    Item item = menuItems.get(i);
                    logService.log("Sample item " + i + ": " + item.itemId + " - " + item.name);
                }
            }
        } catch (Exception e) {
            logService.log("ERROR loading menu: " + e.getMessage());
            e.printStackTrace();
            
            // Initialize with empty list to avoid null pointer exceptions
            menuItems = new ArrayList<>();
        }
    }
    
    /**
     * Set up the user interface
     */
    private void setupUI() {
        // Set up the main frame
        frame = new JFrame("Friendly Cafe");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 700);
>>>>>>> Stashed changes
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        // Add window listener to handle closing
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
<<<<<<< Updated upstream
                LogService.getInstance().log("Order interface closed by " + (customerName.isEmpty() ? "unknown customer" : customerName));
=======
                logService.log("FriendlyCafe GUI closed");
                
                // Show parent frame if it exists
                if (parentFrame != null) {
                    parentFrame.setVisible(true);
                }
>>>>>>> Stashed changes
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
        
<<<<<<< Updated upstream
        // Get the order summary panel
        JScrollPane scrollPane = (JScrollPane) checkoutPanel.getComponent(1);
        JPanel orderSummaryPanel = (JPanel) scrollPane.getViewport().getView();
=======
        JLabel titleLabel = new JLabel("Welcome to Friendly Cafe");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Center panel
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(VERY_LIGHT_RED);
        
        JLabel welcomeLabel = new JLabel("Delicious Food & Beverages");
        welcomeLabel.setFont(new Font("Arial", Font.ITALIC, 20));
        welcomeLabel.setForeground(DARK_RED);
        
        JButton placeOrderButton = createStyledButton("Place Order", MEDIUM_RED, Color.WHITE);
        placeOrderButton.setPreferredSize(new Dimension(200, 50));
        placeOrderButton.addActionListener(e -> cardLayout.show(mainPanel, "ORDER"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 50, 0);
        centerPanel.add(welcomeLabel, gbc);
        
        gbc.gridy = 1;
        centerPanel.add(placeOrderButton, gbc);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        // Footer panel
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(DARK_RED);
        footerPanel.setPreferredSize(new Dimension(900, 50));
        
        JLabel footerLabel = new JLabel("© 2025 Friendly Cafe");
        footerLabel.setForeground(Color.WHITE);
        footerPanel.add(footerLabel);
        
        panel.add(footerPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create the order screen with split layout
     */
    private JPanel createOrderScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Create the top menu bar with category buttons
        JPanel topMenuBar = new JPanel(new GridLayout(1, 3));
        topMenuBar.setBackground(MEDIUM_RED);
        topMenuBar.setPreferredSize(new Dimension(900, 50));
        
        JButton foodButton = createStyledButton("Food", DARK_RED, Color.WHITE);
        JButton beverageButton = createStyledButton("Beverages", DARK_RED, Color.WHITE);
        JButton dessertButton = createStyledButton("Desserts", DARK_RED, Color.WHITE);
        
        topMenuBar.add(foodButton);
        topMenuBar.add(beverageButton);
        topMenuBar.add(dessertButton);
        
        panel.add(topMenuBar, BorderLayout.NORTH);
        
        // Create the bottom buttons panel
        JPanel bottomButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomButtonsPanel.setBackground(MEDIUM_RED);
        bottomButtonsPanel.setPreferredSize(new Dimension(900, 50));
        
        JButton homeButton = createStyledButton("Home", DARK_RED, Color.WHITE);
        homeButton.addActionListener(e -> cardLayout.show(mainPanel, "HOME"));
        
        JButton placeOrderButton = createStyledButton("Place Order", DARK_RED, Color.WHITE);
        placeOrderButton.addActionListener(e -> {
            if (orderingItems.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please add items to your cart first", 
                        "Empty Cart", JOptionPane.WARNING_MESSAGE);
            } else {
                cardLayout.show(mainPanel, "PAYMENT");
            }
        });
        
        bottomButtonsPanel.add(homeButton);
        bottomButtonsPanel.add(placeOrderButton);
        
        panel.add(bottomButtonsPanel, BorderLayout.SOUTH);
        
        // Create main content panel with split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(650);
        splitPane.setEnabled(false); // Prevent user from moving divider
        
        // Left panel for displaying menu items
        itemDisplayPanel = new JPanel(new BorderLayout());
        itemDisplayPanel.setBackground(VERY_LIGHT_RED);
        JScrollPane itemScrollPane = new JScrollPane(itemDisplayPanel);
        itemScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        
        // Right panel for cart
        cartPanel = createCartPanel();
        JScrollPane cartScrollPane = new JScrollPane(cartPanel);
        cartScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        
        splitPane.setLeftComponent(itemScrollPane);
        splitPane.setRightComponent(cartScrollPane);
        
        panel.add(splitPane, BorderLayout.CENTER);
        
        // Set up action listeners for category buttons
        foodButton.addActionListener(e -> populateItemDisplay(getItemsByCategory("Food")));
        beverageButton.addActionListener(e -> populateItemDisplay(getItemsByCategory("Beverage")));
        dessertButton.addActionListener(e -> populateItemDisplay(getItemsByCategory("Dessert")));
        
        // Initially show food items
        foodButton.doClick();
        
        return panel;
    }
    
    /**
     * Create the cart panel
     */
    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_RED);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Cart header
        JLabel cartHeader = new JLabel("Cart", SwingConstants.CENTER);
        cartHeader.setFont(new Font("Arial", Font.BOLD, 20));
        cartHeader.setForeground(DARK_RED);
        cartHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        panel.add(cartHeader, BorderLayout.NORTH);
        
        // Cart items panel
        JPanel cartItemsPanel = new JPanel();
        cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS));
        cartItemsPanel.setBackground(VERY_LIGHT_RED);
        
        JScrollPane cartScrollPane = new JScrollPane(cartItemsPanel);
        cartScrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        panel.add(cartScrollPane, BorderLayout.CENTER);
        
        // Cart summary panel
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBackground(LIGHT_RED);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JLabel subtotalLabel = new JLabel("Subtotal: " + currencyFormat.format(0.0));
        subtotalLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtotalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel discountLabel = new JLabel("Discount: " + currencyFormat.format(0.0));
        discountLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        discountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel totalLabel = new JLabel("Total: " + currencyFormat.format(0.0));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setForeground(DARK_RED);
        totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        summaryPanel.add(subtotalLabel);
        summaryPanel.add(Box.createVerticalStrut(5));
        summaryPanel.add(discountLabel);
        summaryPanel.add(Box.createVerticalStrut(5));
        summaryPanel.add(totalLabel);
        
        panel.add(summaryPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create the payment screen
     */
    private JPanel createPaymentScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(VERY_LIGHT_RED);
        
        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(DARK_RED);
        headerPanel.setPreferredSize(new Dimension(900, 70));
        
        JLabel titleLabel = new JLabel("Payment Information");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Center panel with payment form
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(VERY_LIGHT_RED);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Name field
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(DARK_RED);
        
        JTextField nameField = new JTextField(20);
        nameField.setFont(new Font("Arial", Font.PLAIN, 16));
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1;
        centerPanel.add(nameField, gbc);
        
        // Email field
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.BOLD, 16));
        emailLabel.setForeground(DARK_RED);
        
        JTextField emailField = new JTextField(20);
        emailField.setFont(new Font("Arial", Font.PLAIN, 16));
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        centerPanel.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        centerPanel.add(emailField, gbc);
        
        // Payment method
        JLabel paymentMethodLabel = new JLabel("Payment Method:");
        paymentMethodLabel.setFont(new Font("Arial", Font.BOLD, 16));
        paymentMethodLabel.setForeground(DARK_RED);
        
        JPanel paymentMethodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        paymentMethodPanel.setBackground(VERY_LIGHT_RED);
        
        JRadioButton cashButton = new JRadioButton("Cash");
        cashButton.setFont(new Font("Arial", Font.PLAIN, 16));
        cashButton.setBackground(VERY_LIGHT_RED);
        cashButton.setForeground(DARK_RED);
        cashButton.setSelected(true);
        
        JRadioButton cardButton = new JRadioButton("Card");
        cardButton.setFont(new Font("Arial", Font.PLAIN, 16));
        cardButton.setBackground(VERY_LIGHT_RED);
        cardButton.setForeground(DARK_RED);
        
        ButtonGroup paymentGroup = new ButtonGroup();
        paymentGroup.add(cashButton);
        paymentGroup.add(cardButton);
        
        paymentMethodPanel.add(cashButton);
        paymentMethodPanel.add(cardButton);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        centerPanel.add(paymentMethodLabel, gbc);
        
        gbc.gridx = 1;
        centerPanel.add(paymentMethodPanel, gbc);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel with buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(MEDIUM_RED);
        bottomPanel.setPreferredSize(new Dimension(900, 70));
        
        JButton cancelButton = createStyledButton("Cancel", DARK_RED, Color.WHITE);
        cancelButton.addActionListener(e -> cardLayout.show(mainPanel, "ORDER"));
        
        JButton confirmButton = createStyledButton("Confirm Order", DARK_RED, Color.WHITE);
        confirmButton.addActionListener(e -> {
            customerName = nameField.getText();
            customerEmail = emailField.getText();
            
            if (customerName.isEmpty() || customerEmail.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please fill in all fields", 
                        "Missing Information", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                // Save customer details if new customer
                if (!dataService.checkCustomer(customerEmail)) {
                    dataService.saveCustomerDetails(customerName, customerEmail);
                }
                
                // Calculate final costs
                double billCost = cafeController.getTotalCost(orderingItems);
                double discountedCost = cafeController.getDiscountedCost(billCost);
                boolean isOffered = billCost != discountedCost;
                
                // Save the order
                cafeController.saveOrder(customerEmail, orderingItems, isOffered, discountedCost);
                
                // Update bill screen and show it
                updateBillScreen();
                cardLayout.show(mainPanel, "BILL");
                
                // Log order
                logService.log("Order completed by " + customerName + " (" + customerEmail + ") for " + 
                        currencyFormat.format(discountedCost));
                
            } catch (CustomerFoundException | InvalidMailFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Error processing order: " + ex.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        bottomPanel.add(cancelButton);
        bottomPanel.add(Box.createHorizontalStrut(30));
        bottomPanel.add(confirmButton);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create the bill screen
     */
    private JPanel createBillScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(VERY_LIGHT_RED);
        
        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(DARK_RED);
        headerPanel.setPreferredSize(new Dimension(900, 70));
        
        JLabel titleLabel = new JLabel("Bill / Receipt");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Center panel with bill details
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(VERY_LIGHT_RED);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        
        // This will be populated later in updateBillScreen()
        
        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Bottom panel with button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(MEDIUM_RED);
        bottomPanel.setPreferredSize(new Dimension(900, 70));
        
        JButton homeButton = createStyledButton("Return to Home", DARK_RED, Color.WHITE);
        homeButton.addActionListener(e -> {
            // Reset order data
            orderingItems.clear();
            totalCost = 0.0;
            discountedCost = 0.0;
            
            // Go to home screen
            cardLayout.show(mainPanel, "HOME");
        });
        
        bottomPanel.add(homeButton);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Update the bill screen with order details
     */
    private void updateBillScreen() {
        JPanel centerPanel = (JPanel) ((JScrollPane) ((JPanel) mainPanel.getComponent(3)).getComponent(1)).getViewport().getView();
        centerPanel.removeAll();
        
        // Add thank you message
        JLabel thankYouLabel = new JLabel("Thank you for your order, " + customerName + "!");
        thankYouLabel.setFont(new Font("Arial", Font.BOLD, 18));
        thankYouLabel.setForeground(DARK_RED);
        thankYouLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(thankYouLabel);
        
        centerPanel.add(Box.createVerticalStrut(20));
        
        // Add order details heading
        JLabel orderDetailsLabel = new JLabel("Order Details:");
        orderDetailsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        orderDetailsLabel.setForeground(DARK_RED);
        orderDetailsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(orderDetailsLabel);
        
        centerPanel.add(Box.createVerticalStrut(10));
        
        // Create a panel for items
        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(VERY_LIGHT_RED);
        itemsPanel.setBorder(BorderFactory.createLineBorder(MEDIUM_RED));
        itemsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Add each item
        for (Map.Entry<String, Integer> entry : orderingItems.entrySet()) {
            String itemId = entry.getKey();
            int quantity = entry.getValue();
            Item item = menuItemsMap.get(itemId);
            
            if (item != null) {
                JPanel itemRow = new JPanel(new BorderLayout());
                itemRow.setBackground(VERY_LIGHT_RED);
                itemRow.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                
                JLabel itemLabel = new JLabel(quantity + " x " + item.name);
                JLabel priceLabel = new JLabel(currencyFormat.format(item.cost * quantity));
                
                itemRow.add(itemLabel, BorderLayout.WEST);
                itemRow.add(priceLabel, BorderLayout.EAST);
                
                itemsPanel.add(itemRow);
            }
        }
        
        centerPanel.add(itemsPanel);
        centerPanel.add(Box.createVerticalStrut(20));
        
        // Calculate totals
        double subtotal = cafeController.getTotalCost(orderingItems);
        double discountedTotal = cafeController.getDiscountedCost(subtotal);
        double discount = subtotal - discountedTotal;
        
        // Add totals
        JPanel totalsPanel = new JPanel();
        totalsPanel.setLayout(new BoxLayout(totalsPanel, BoxLayout.Y_AXIS));
        totalsPanel.setBackground(LIGHT_RED);
        totalsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        totalsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Create totals rows
        JPanel subtotalRow = createBillRow("Subtotal:", currencyFormat.format(subtotal));
        JPanel discountRow = createBillRow("Discount:", currencyFormat.format(discount));
        JPanel totalRow = createBillRow("Total:", currencyFormat.format(discountedTotal));
        
        // Style the total row differently
        totalRow.setBackground(MEDIUM_RED);
        totalRow.getComponent(0).setFont(new Font("Arial", Font.BOLD, 16));
        totalRow.getComponent(1).setFont(new Font("Arial", Font.BOLD, 16));
        
        totalsPanel.add(subtotalRow);
        totalsPanel.add(discountRow);
        totalsPanel.add(totalRow);
        
        centerPanel.add(totalsPanel);
        
        // Add a message at the bottom
        centerPanel.add(Box.createVerticalStrut(20));
        
        JLabel messageLabel = new JLabel("We hope to see you again soon!");
        messageLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        messageLabel.setForeground(DARK_RED);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(messageLabel);
        
        // Refresh the panel
        centerPanel.revalidate();
        centerPanel.repaint();
    }
    
    /**
     * Helper method to create a row for the bill
     */
    private JPanel createBillRow(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_RED);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Arial", Font.PLAIN, 14));
        
        panel.add(labelComponent, BorderLayout.WEST);
        panel.add(valueComponent, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Helper method to populate the item display panel
     */
    private void populateItemDisplay(List<Item> items) {
        itemDisplayPanel.removeAll();
        
        // Log for debugging
        logService.log("Displaying " + (items != null ? items.size() : 0) + " items");
        
        if (items == null || items.isEmpty()) {
            JLabel emptyLabel = new JLabel("No items available in this category", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Arial", Font.BOLD, 14));
            emptyLabel.setForeground(DARK_RED);
            itemDisplayPanel.add(emptyLabel, BorderLayout.CENTER);
        } else {
            JPanel gridPanel = new JPanel(new GridLayout(0, 1, 10, 10));
            gridPanel.setBackground(VERY_LIGHT_RED);
            gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            for (Item item : items) {
                JPanel itemPanel = createItemPanel(item);
                gridPanel.add(itemPanel);
            }
            
            itemDisplayPanel.add(gridPanel, BorderLayout.NORTH);
        }
        
        itemDisplayPanel.revalidate();
        itemDisplayPanel.repaint();
    }
    
    /**
     * Helper method to create a panel for a menu item
     */
    private JPanel createItemPanel(Item item) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MEDIUM_RED),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Item name and price
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JLabel nameLabel = new JLabel(item.name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(DARK_RED);
        
        JLabel priceLabel = new JLabel(currencyFormat.format(item.cost));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        priceLabel.setForeground(ACCENT_RED);
        
        headerPanel.add(nameLabel, BorderLayout.WEST);
        headerPanel.add(priceLabel, BorderLayout.EAST);
        
        // Item description
        JLabel descriptionLabel = new JLabel("<html><body style='width: 300px'>" + item.description + "</body></html>");
        descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Add to cart button
        JButton addButton = createStyledButton("Add to Cart", MEDIUM_RED, Color.WHITE);
        addButton.addActionListener(e -> addItemToCart(item));
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(descriptionLabel, BorderLayout.CENTER);
        panel.add(addButton, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Add an item to the cart
     */
    private void addItemToCart(Item item) {
        // Update the order map
        if (orderingItems.containsKey(item.itemId)) {
            orderingItems.put(item.itemId, orderingItems.get(item.itemId) + 1);
        } else {
            orderingItems.put(item.itemId, 1);
        }
        
        // Update the cart panel
        updateCartPanel();
        
        // Log addition
        logService.log("Added " + item.name + " to cart");
        
        // Show a confirmation message
        JOptionPane.showMessageDialog(frame, item.name + " added to cart", 
                "Item Added", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Update the cart panel with current items
     */
    private void updateCartPanel() {
        // Get the cart items panel (the center component of the cart panel)
        JScrollPane scrollPane = (JScrollPane) cartPanel.getComponent(1);
        JPanel cartItemsPanel = (JPanel) scrollPane.getViewport().getView();
>>>>>>> Stashed changes
        
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
        
<<<<<<< Updated upstream
        SwingUtilities.invokeLater(() -> new FriendlyCafe());
=======
        // Create and show the application
        SwingUtilities.invokeLater(() -> new FriendlyCafe(null));
>>>>>>> Stashed changes
    }
}
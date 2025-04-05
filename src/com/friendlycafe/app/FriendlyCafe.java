package com.friendlycafe.app;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.friendlycafe.controller.CafeController;
import com.friendlycafe.dtoservice.CafeService;
import com.friendlycafe.exception.CustomerFoundException;
import com.friendlycafe.exception.InvalidMailFormatException;
import com.friendlycafe.pojo.Beverage;
import com.friendlycafe.pojo.Dessert;
import com.friendlycafe.pojo.Item;
import com.friendlycafe.service.DataService;
import com.friendlycafe.service.LogService;

/**
 * Redesigned FriendlyCafe class with improved UI layout
 */
public class FriendlyCafe {
    // Define color palette
    private static final Color DARK_RED = new Color(139, 0, 0);
    private static final Color MEDIUM_RED = new Color(200, 0, 0);
    private static final Color LIGHT_RED = new Color(255, 153, 153);
    private static final Color VERY_LIGHT_RED = new Color(255, 204, 204);
    private static final Color ACCENT_RED = new Color(220, 20, 60);
    
    // Services
    private DataService dataService;
    private CafeService cafeService;
    private CafeController cafeController;
    private LogService logService;
    
    // UI Components
    private JFrame parentFrame;
    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel itemDisplayPanel;
    private JPanel cartPanel;
    
    // Data
    private String customerName = "";
    private String customerEmail = "";
    private HashMap<String, Integer> orderingItems = new HashMap<>();
    private double totalCost = 0.0;
    private double discountedCost = 0.0;
    private ArrayList<Item> menuItems;
    private HashMap<String, Item> menuItemsMap = new HashMap<>();
    
    // Formatting
    private DecimalFormat currencyFormat = new DecimalFormat("£#,##0.00");
    
    /**
     * Constructor for FriendlyCafe
     */
    public FriendlyCafe() {
        this(null);
    }

    /**
     * Constructor with parent frame
     */
    public FriendlyCafe(JFrame parentFrame) {
        // Initialize services
        dataService = new DataService();
        cafeService = new CafeService();
        cafeController = new CafeController();
        logService = LogService.getInstance();
        
        // Store parent frame
        this.parentFrame = parentFrame;
        
        // Load menu
        loadMenu();
        
        // Set up UI
        setupUI();
    }
    
    /**
     * Load menu items from data service
     */
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
        frame.setLocationRelativeTo(null);
        
        // Create main panel with card layout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Create the different screens
        JPanel homeScreen = createHomeScreen();
        JPanel orderScreen = createOrderScreen();
        JPanel paymentScreen = createPaymentScreen();
        JPanel billScreen = createBillScreen();
        
        // Add screens to card layout
        mainPanel.add(homeScreen, "HOME");
        mainPanel.add(orderScreen, "ORDER");
        mainPanel.add(paymentScreen, "PAYMENT");
        mainPanel.add(billScreen, "BILL");
        
        // Add main panel to frame
        frame.getContentPane().add(mainPanel);
        
        // Show the frame
        frame.setVisible(true);
        
        // Add window listener to handle closing
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logService.log("FriendlyCafe GUI closed");
                
                // Show parent frame if it exists
                if (parentFrame != null) {
                    parentFrame.setVisible(true);
                }
            }
        });
    }
    
    /**
     * Create the home screen
     */
    private JPanel createHomeScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(VERY_LIGHT_RED);
        
        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(DARK_RED);
        headerPanel.setPreferredSize(new Dimension(900, 100));
        
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
        
        // Clear the panel
        cartItemsPanel.removeAll();
        
        // Add each item in the cart
        for (Map.Entry<String, Integer> entry : orderingItems.entrySet()) {
            String itemId = entry.getKey();
            int quantity = entry.getValue();
            Item item = menuItemsMap.get(itemId);
            
            if (item != null) {
                JPanel itemPanel = new JPanel(new BorderLayout(5, 0));
                itemPanel.setBackground(Color.WHITE);
                itemPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, MEDIUM_RED),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
                
                JLabel quantityLabel = new JLabel(String.valueOf(quantity) + "x");
                quantityLabel.setFont(new Font("Arial", Font.BOLD, 14));
                
                JLabel nameLabel = new JLabel(item.name);
                nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                
                JLabel priceLabel = new JLabel(currencyFormat.format(item.cost * quantity));
                priceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                
                JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                leftPanel.setBackground(Color.WHITE);
                leftPanel.add(quantityLabel);
                leftPanel.add(nameLabel);
                
                JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                rightPanel.setBackground(Color.WHITE);
                
                JButton removeButton = new JButton("X");
                removeButton.setFont(new Font("Arial", Font.BOLD, 10));
                removeButton.setForeground(Color.WHITE);
                removeButton.setBackground(ACCENT_RED);
                removeButton.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                removeButton.setFocusPainted(false);
                
                // Add action to remove item
                final String itemIdToRemove = itemId;
                removeButton.addActionListener(e -> {
                    orderingItems.remove(itemIdToRemove);
                    updateCartPanel();
                });
                
                rightPanel.add(priceLabel);
                rightPanel.add(removeButton);
                
                itemPanel.add(leftPanel, BorderLayout.WEST);
                itemPanel.add(rightPanel, BorderLayout.EAST);
                
                cartItemsPanel.add(itemPanel);
            }
        }
        
        // Add empty message if cart is empty
        if (orderingItems.isEmpty()) {
            JLabel emptyLabel = new JLabel("Your cart is empty", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            emptyLabel.setForeground(DARK_RED);
            cartItemsPanel.add(emptyLabel);
        }
        
        // Update the summary panel
        updateCartSummary();
        
        // Refresh the panel
        cartItemsPanel.revalidate();
        cartItemsPanel.repaint();
    }
    
    /**
     * Update the cart summary with current totals
     */
    private void updateCartSummary() {
        // Calculate totals
        totalCost = 0.0;
        for (Map.Entry<String, Integer> entry : orderingItems.entrySet()) {
            String itemId = entry.getKey();
            int quantity = entry.getValue();
            Item item = menuItemsMap.get(itemId);
            
            if (item != null) {
                totalCost += item.cost * quantity;
            }
        }
        
        discountedCost = cafeController.getDiscountedCost(totalCost);
        double discount = totalCost - discountedCost;
        
        // Get the summary panel (the south component of the cart panel)
        JPanel summaryPanel = (JPanel) cartPanel.getComponent(2);
        
        // Update the labels
        JLabel subtotalLabel = (JLabel) summaryPanel.getComponent(0);
        subtotalLabel.setText("Subtotal: " + currencyFormat.format(totalCost));
        
        JLabel discountLabel = (JLabel) summaryPanel.getComponent(2);
        discountLabel.setText("Discount: " + currencyFormat.format(discount));
        
        JLabel totalLabel = (JLabel) summaryPanel.getComponent(4);
        totalLabel.setText("Total: " + currencyFormat.format(discountedCost));
    }
    
    /**
     * Get items by category
     */
    private List<Item> getItemsByCategory(String category) {
        List<Item> result = new ArrayList<>();
        
        for (Item item : menuItems) {
            if (category.equals("Food") && !(item instanceof Beverage) && !(item instanceof Dessert)) {
                result.add(item);
            } else if (category.equals("Beverage") && item instanceof Beverage) {
                result.add(item);
            } else if (category.equals("Dessert") && item instanceof Dessert) {
                result.add(item);
            }
        }
        
        return result;
    }
    
    /**
     * Helper method to create styled buttons
     */
    private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DARK_RED),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(ACCENT_RED);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create and show the application
        SwingUtilities.invokeLater(() -> new FriendlyCafe());
    }
}
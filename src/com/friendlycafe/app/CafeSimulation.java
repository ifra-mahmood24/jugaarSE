package com.friendlycafe.app;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import com.friendlycafe.pojo.Order;
import com.friendlycafe.service.LogService;
import com.friendlycafe.service.DataService;

public class CafeSimulation {

    private static final Color DARK_RED = new Color(139, 0, 0);
    private static final Color MEDIUM_RED = new Color(200, 0, 0);
    private static final Color LIGHT_RED = new Color(255, 153, 153);
    private static final Color VERY_LIGHT_RED = new Color(255, 204, 204);
    private static final Color ACCENT_RED = new Color(220, 20, 60);

	private BlockingQueue<Order> orderQueue = new LinkedBlockingQueue<>();
	private List<StaffMember> staffMembers = new ArrayList<>();
	private ExecutorService executor;
	private Thread customerQueueThread;
	private Thread  newCustomersThread;
	private volatile boolean simulationRunning = false;
	private DataService dataService = new DataService();
	private List<Order> existingOrders = new ArrayList<>();
    private final CountDownLatch latch = new CountDownLatch(1);

	private JFrame frame;
	private JPanel queuePanel;
	private JPanel staffPanel;

	private int numberOfStaff = 2;
	private int processingTimeInSeconds = 10;

	public CafeSimulation() {
		loadExistingOrders();

		createGUI();

		openShop();
	}
	public CafeSimulation(boolean isCustomerComing) {
		customersComing();
		Frame[] frames = Frame.getFrames();
		for (Frame frame : frames) {
		    if (frame.getTitle().equals("Friendly Cafe Simulation")) {
		    	System.out.println("CLOSING OLD FRAME");
		        frame.dispose();
		    }
		}
		createGUI();
		openShop();
	}

	protected void loadExistingOrders() {
		existingOrders = dataService.getAllActiveOrders();
		LogService.getInstance().log("Loaded " + existingOrders.size() + " existing orders");
	}

	public void cleanup() {

	}

	private void createGUI() {
		frame = new JFrame("Friendly Cafe Simulation");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());

		JPanel controlPanel = createControlPanel();
		frame.add(controlPanel, BorderLayout.NORTH);

		JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 10));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		queuePanel = new JPanel();
		queuePanel.setBackground(LIGHT_RED);
		queuePanel.setBorder(BorderFactory.createTitledBorder(
			    BorderFactory.createLineBorder(DARK_RED),  
			    "Customer Queue",                           
			    TitledBorder.DEFAULT_JUSTIFICATION,         
			    TitledBorder.CENTER,               
			    new Font("Arial", Font.BOLD, 12),            
			    DARK_RED                                 
			));	
		queuePanel.setLayout(new BoxLayout(queuePanel, BoxLayout.Y_AXIS));
		JScrollPane queueScrollPane = new JScrollPane(queuePanel);
		queueScrollPane.setPreferredSize(new Dimension(300, 400));

		staffPanel = new JPanel();
		staffPanel.setBackground(LIGHT_RED);
		staffPanel.setBorder(BorderFactory.createTitledBorder(
			    BorderFactory.createLineBorder(DARK_RED),  
			    "Staff Members",                            
			    TitledBorder.DEFAULT_JUSTIFICATION,          
			    TitledBorder.CENTER,              
			    new Font("Arial", Font.BOLD, 12),  
			    DARK_RED                                 
			));	
		staffPanel.setLayout(new BoxLayout(staffPanel, BoxLayout.Y_AXIS));
		JScrollPane staffScrollPane = new JScrollPane(staffPanel);
		staffScrollPane.setBackground(LIGHT_RED);

		staffScrollPane.setPreferredSize(new Dimension(300, 400));

		mainPanel.add(queueScrollPane);
		mainPanel.add(staffScrollPane);

		frame.add(mainPanel, BorderLayout.CENTER);

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private JPanel createControlPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());

		JLabel staffLabel = new JLabel("Number of Staff: ");
		
		SpinnerNumberModel staffModel = new SpinnerNumberModel(2, 1, 10, 1);
		JSpinner staffSpinner = new JSpinner(staffModel);
		staffSpinner.setForeground(MEDIUM_RED);
		
		staffSpinner.addChangeListener(e -> {
			numberOfStaff = (int) staffSpinner.getValue();
			if (simulationRunning) {
				adjustStaffCount();
			}
		});

		JLabel timeLabel = new JLabel("Processing Time (sec): ");
		timeLabel.setBackground(Color.WHITE);
		SpinnerNumberModel timeModel = new SpinnerNumberModel(10, 1, 30, 1);
		JSpinner timeSpinner = new JSpinner(timeModel);
		timeSpinner.addChangeListener(e -> {
			processingTimeInSeconds = (int) timeSpinner.getValue();
		});

		// Control buttons
		JButton startButton = new JButton("Take order");
		startButton.setForeground(Color.BLACK);
		startButton.setBorder(BorderFactory.createCompoundBorder(
	               BorderFactory.createLineBorder(DARK_RED, 3, true),
	               BorderFactory.createEmptyBorder(2, 7, 2, 7)
	           ));
		startButton.addActionListener(e -> {
			SwingUtilities.invokeLater(() -> new FriendlyCafe(frame));
		});

		JButton stopButton = new JButton("Close Shop");
		stopButton.addActionListener(e -> closeShop());
		stopButton.setBorder(BorderFactory.createCompoundBorder(
	               BorderFactory.createLineBorder(DARK_RED, 3, true),
	               BorderFactory.createEmptyBorder(2, 7, 2, 7)
	           ));
		panel.setBackground(VERY_LIGHT_RED);
		panel.setBorder(BorderFactory.createTitledBorder(
			    BorderFactory.createLineBorder(Color.BLACK),  
			    "WELCOME TO FRIENDLY CAFE",                            
			    TitledBorder.CENTER,          
			    TitledBorder.CENTER,              
			    new Font("Arial", Font.BOLD, 14),  
			    DARK_RED
			));	
		panel.add(staffLabel);
		panel.add(staffSpinner);
		panel.add(timeLabel);
		panel.add(timeSpinner);
		panel.add(startButton);
		panel.add(stopButton);

		return panel;
	}

	protected void openShop() {
		if (simulationRunning) 
			return;
				
		LogService.getInstance().log("Simulation started with " + numberOfStaff + " staff members");

		simulationRunning = true;

		executor = Executors.newFixedThreadPool(numberOfStaff);

		for (int i = 0; i < numberOfStaff; i++) {
			StaffMember staff = new StaffMember("Staff " + (i + 1));
			staffMembers.add(staff);
			executor.submit(staff);

			JPanel staffMemberPanel = createStaffPanel(staff);
			staffPanel.add(staffMemberPanel);
		}

		customerQueueThread = new Thread(() -> {
			for (Order order : existingOrders)
				try {
					Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 3000));

					if (!simulationRunning)
						break;

					orderQueue.put(order);

					String message = "Customer " + order.getCustomerId() + " added to queue with order "
							+ order.getOrderId();
					LogService.getInstance().log(message);

					SwingUtilities.invokeLater(() -> {
						JPanel orderPanel = createOrderPanel(order);
						queuePanel.add(orderPanel);
						queuePanel.revalidate();
						queuePanel.repaint();

					});

				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					LogService.getInstance().log("Customer queue thread interrupted" + e);
				}
			
            if (latch.getCount() > 0) {
                latch.countDown();
            }


			LogService.getInstance().log("All existing orders have been added to the queue");
		});
		

		customerQueueThread.start();
	}

	protected void customersComing() {
		newCustomersThread = new Thread(() ->  {
			try {
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("INCOMING NEW CUST THREAD STARTED");
			loadExistingOrders();

			for (Order order : existingOrders)
			 try {
				orderQueue.put(order);

				String message = "Incoming Customer " + order.getCustomerId() + " added to queue with order "
						+ order.getOrderId();
				LogService.getInstance().log(message);

				SwingUtilities.invokeLater(() -> {
					JPanel orderPanel = createOrderPanel(order);
					queuePanel.add(orderPanel);
					queuePanel.revalidate();
					queuePanel.repaint();

				});
		  		 Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 3000));

			 }catch(Exception e) {
					Thread.currentThread().interrupt();
					LogService.getInstance().log("Incoming New Customer queue thread interrupted" + e);				 
			 }
		});
		newCustomersThread.start();
	}
	
	private void closeShop() {
		if (!simulationRunning) {
			return;
		}
		LogService.getInstance().writeLogToFile("cafe_simulation_log.txt");

		LogService.getInstance().log("Simulation stopped");

		simulationRunning = false;

		if (customerQueueThread != null) {
			customerQueueThread.interrupt();
		}
		if (newCustomersThread != null) {
			newCustomersThread.interrupt();
		}

		if (executor != null) {
			executor.shutdownNow();
			try {
				executor.awaitTermination(3, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				LogService.getInstance().log("Error while waiting for executor to terminate " + e);
			}
		}

		staffMembers.clear();

		Runtime.getRuntime().addShutdownHook(new Thread(this::cleanup));

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cleanup();
			}
		});

		// Clear panels
		queuePanel.removeAll();
		queuePanel.revalidate();
		queuePanel.repaint();

		staffPanel.removeAll();
		staffPanel.revalidate();
		staffPanel.repaint();

		// Generate final report
		dataService.generateReport();
		System.exit(0);
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
		panel.setBackground(VERY_LIGHT_RED);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createLineBorder(DARK_RED));
	
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
		panel.setBackground(VERY_LIGHT_RED);

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
			if (terminated)
				dataService.generateReport();

			while (!terminated && simulationRunning) {
				try {
					// Try to take an order from the queue (waiting up to 1 second)
					currentOrder = orderQueue.poll(1, TimeUnit.SECONDS);

					if (currentOrder != null) {
						final Order orderToRemove = currentOrder;
						// Update status to busy
						status = "Busy";
						updateLabels();

						// Log the event
						String message = name + " is now processing order " + currentOrder.getOrderId()
								+ " for customer " + currentOrder.getCustomerId();
						LogService.getInstance().log(message);

						// Process the order (simulate with a delay)
						Thread.sleep(processingTimeInSeconds * 1000);

						// Order is processed
						String completionMessage = name + " has completed order " + currentOrder.getOrderId()
								+ " for customer " + currentOrder.getCustomerId();
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
												// remove order from active orders and add it to orders
												dataService.removeFromActiveOrder(orderToRemove);
												dataService.saveOrder(orderToRemove);
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
					LogService.getInstance().log(name + " thread interrupted" + e);
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
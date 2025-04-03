package com.friendlycafe.app;

import com.friendlycafe.controller.CafeController;
import com.friendlycafe.model.CafeModel;
import com.friendlycafe.service.DataService;
import com.friendlycafe.service.LogService;
import com.friendlycafe.view.CafeView;

/**
 * Main application entry point
 * Initializes and connects MVC components
 */
public class CafeSimulation {
    public static void main(String[] args) {
        LogService.getInstance().log("Starting Cafe Simulation");
        
        // Initialize services and model
        DataService dataService = new DataService();
        CafeModel model = new CafeModel();
        
        // Initialize controller
        CafeController controller = new CafeController(model, dataService);
        
        // Initialize view
        CafeView view = new CafeView(model, controller);
        
        // Connect MVC components
        controller.setView(view);
        
        // Load menu
        dataService.getMenu();
        LogService.getInstance().log("Menu loaded with " + DataService.menuList.size() + " items");
        
        // Start the application
        LogService.getInstance().log("Displaying application window");
        view.setVisible(true);
    }
}

package com.friendlycafe.app;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;


import com.friendlycafe.service.LogService;

/**
 * Main application class for launching the Friendly Cafe application
 */
public class CafeMain {
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LogService.getInstance().log("Could not set look and feel "+ e);
        }
        
        // Launch the cafe simulator application
        SwingUtilities.invokeLater(() -> new CafeSimulation());
    }
}
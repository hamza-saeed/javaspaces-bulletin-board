package Controller;

import Model.OneUser;
import javax.swing.JOptionPane;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace05;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Settings
 *
 * @author Hamza Saeed - u1550400
 */
public class Settings {

    public OneUser currentUser;

    public JavaSpace05 space;

    public TransactionManager mgr;

    public Settings() {
        getJavaspace();
        getTransactionManager();
    }

    public void getJavaspace() {
        // Get the JavaSpace
        space = SpaceUtils.getSpace();
        //if not found, display error msg and close program
        if (space == null) {
            JOptionPane.showMessageDialog(null, "Javaspace not found.");
            System.exit(1);
        }
    }

    public void getTransactionManager() {
        //Get the transaction manager
        mgr = SpaceUtils.getManager();
        //if not found, display error msg and close program
        if (mgr == null) {
            JOptionPane.showMessageDialog(null, "Transaction Manager not found.");
            System.exit(1);
        }
    }

}

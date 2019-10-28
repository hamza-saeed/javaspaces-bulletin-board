/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import AESSecurity.AESAlgorithm;
import Model.OneUser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import net.jini.core.lease.Lease;

/**
 * LoginController
 *
 * @author Hamza Saeed - u1550400
 */
public class LoginController {

    public Settings Settings;
    AESAlgorithm aes;

    public LoginController(Settings Settings) {
        this.Settings = Settings;
        aes = new AESAlgorithm(Settings);
    }

    public boolean doesUserExist(String username) {
        //create template with inputted username
        OneUser userTemplate = new OneUser();
        userTemplate.setName(username);
        try {
            //read from space
            OneUser userResult = (OneUser) Settings.space.readIfExists(userTemplate, null, 100);
            //if result is null, user does not exist. Return false.
            if (userResult == null) {
                return false;
            } else {
                //user exists, so return true
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean registerUser(String username, String password) {
        //check if already exists with same username
        if (doesUserExist(username) == false) {
            try {
                //encrypt password using public key
                byte[] encryptedPassword = aes.encrypt(password);
                //if not, create new user and write to the space
                OneUser newUser = new OneUser(username, encryptedPassword);
                Settings.space.write(newUser, null, Lease.FOREVER);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return false;
        }

        return false;
    }

    public boolean loginUser(String username, String attemptedPassword) {
        //create a template with inputted username
        OneUser userTemplate = new OneUser();
        userTemplate.setName(username);
        try {
            //get user
            OneUser userResult = (OneUser) Settings.space.readIfExists(userTemplate, null, 100);
            //ensure object is not null
            if (userResult != null) {
                //decrypt encrypted password using private key
                String decryptedPassword = aes.decrypt(userResult.getPassword());
                //check is inputted password is the same as the retreived user password
                if (decryptedPassword.equals(attemptedPassword)) {
                    //if correct, save user details and return true
                    Settings.currentUser = userResult;
                    return true;
                } else {
                    //otherwise return false
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean validateLogin(String attemptedUsername, String attemptedPassword) {
        //checks to ensure username and password are not null, blank or just spaces
        if ((attemptedUsername != null) && (attemptedPassword != null)
                && (!attemptedUsername.trim().isEmpty()) && (!attemptedPassword.trim().isEmpty())) {
            //calls method to check to ensure user exists
            if (doesUserExist(attemptedUsername)) {
                //calls login method to check username/password is valid
                if (loginUser(attemptedUsername, attemptedPassword)) {
                    JOptionPane.showMessageDialog(null, "Login successful");
                    return true;
                } else {
                    JOptionPane.showMessageDialog(null, "Incorrect username/password.", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } else {
                JOptionPane.showMessageDialog(null, "User does not exist", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please enter a username and password.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public void validateRegisteration(String newUsername, String newPassword) {
        //checks to ensure username and password are not null, blank or just spaces
        if ((newUsername != null) && (newPassword != null)
                && (!newUsername.trim().isEmpty()) && (!newPassword.trim().isEmpty())) {
            if (passwordStrength(newPassword)) {
                String passwordConf = passwordConfirmation();
                if (passwordConf == null) {
                } else if (passwordConf.equals(newPassword)) {
                    //calls method to register user
                    if (registerUser(newUsername, newPassword)) {
                        JOptionPane.showMessageDialog(null, "User Successfully Registered");
                    } else {
                        JOptionPane.showMessageDialog(null, "User Already Exists", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Incorrect password confirmation", "Error", JOptionPane.ERROR_MESSAGE);

                }
            } else {
                JOptionPane.showMessageDialog(null, "Your password must contain at least one capital letter, one lower case letter and one number and contain between 6-15 characters", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please enter a username and password.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean passwordStrength(String password) {
        //regex to check password strength
        return password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9]).{6,15}$");
    }

    private String passwordConfirmation() {

        JPanel panel = new JPanel();
        JLabel lblConfirm = new JLabel("Please confirm your password:");
        JPasswordField txtPasswordConfirmation = new JPasswordField(20);
        panel.add(lblConfirm);
        panel.add(txtPasswordConfirmation);
        String[] options = new String[]{"Confirm", "Cancel"};
        int option = JOptionPane.showOptionDialog(null, panel, "Confirm password", JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
        if (option == 0) {
            return String.valueOf(txtPasswordConfirmation.getPassword());
        } else {
            return null;
        }
    }
}

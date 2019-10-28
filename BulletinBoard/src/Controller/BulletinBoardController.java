/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import AESSecurity.AESAlgorithm;
import Model.OneDeletedTopicToken;
import Model.OneMessage;
import Model.OneNotificationAccess;
import Model.OneNotificationSubscription;
import Model.OneTopic;
import Model.OneUser;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.CannotAbortException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.UnknownTransactionException;
import net.jini.space.AvailabilityEvent;
import net.jini.space.MatchSet;

/**
 * BulletinBoardController
 *
 * @author Hamza Saeed - u1550400
 */
public class BulletinBoardController {

    public Settings Settings;
    AESAlgorithm aes;

    public BulletinBoardController(Settings Settings) {
        this.Settings = Settings;
        aes = new AESAlgorithm(Settings);

    }

    public void retrieveTopics(ArrayList<OneTopic> topics, DefaultTableModel topicModel) {

        try {
            //create a matchset of all topics
            MatchSet allTopics = Settings.space.contents(Arrays.asList(new OneTopic()), null, 100 * 3, Long.MAX_VALUE);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            OneTopic topicResult;
            //loop through each result in matchset, assigning the result to topicResult
            while ((topicResult = (OneTopic) allTopics.next()) != null) {
                //add result to arraylist
                topics.add(topicResult);
                //add result to row in table model
                topicModel.addRow(new Object[]{topicResult.getTitle(), topicResult.getOwner().getName(), dtf.format(topicResult.getDateTimeAdded())});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getNotifications(DefaultTableModel notifsModel, JLabel lblNotifs) {
        //set table to 0 rows
        notifsModel.setRowCount(0);
        //create notification access template and set username to current user's
        OneNotificationAccess accessTemplate = new OneNotificationAccess();
        accessTemplate.setUsername(Settings.currentUser.getName());
        OneNotificationAccess accessResult = null;
        try {
            //read the last time notifications were accessed from the space
            accessResult = (OneNotificationAccess) Settings.space.readIfExists(accessTemplate, null, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //create a subscription template and set the username to the current user's
        OneNotificationSubscription subscriptionTemplate = new OneNotificationSubscription();
        subscriptionTemplate.setUsername(Settings.currentUser.getName());
        try {
            //Create a matchset of all subscriptions that the user has
            MatchSet msSubs = Settings.space.contents(Arrays.asList(subscriptionTemplate), null, 1000, Long.MAX_VALUE);
            OneNotificationSubscription subscriptionResult;
            //loop through each result, assigning the result to subscriptionResult
            while ((subscriptionResult = (OneNotificationSubscription) msSubs.next()) != null) {
                //create a template to get all messages for the topic
                OneMessage msgTemplate = new OneMessage();
                msgTemplate.setTopicID(subscriptionResult.getTopicID());
                //create a matchset of all messages for subscribed topics
                MatchSet msMessages = Settings.space.contents(Arrays.asList(msgTemplate), null, 1000, Long.MAX_VALUE);
                OneMessage msgResult;
                //loop through each message, assigning each result to msgResult
                while ((msgResult = (OneMessage) msMessages.next()) != null) {
                    //create a topic template for each message
                    OneTopic topicTemplate = new OneTopic();
                    topicTemplate.setTopicID(msgResult.getTopicID());
                    //read the topic from the javaspace
                    OneTopic topicRes = (OneTopic) Settings.space.readIfExists(topicTemplate, null, 1000);
                    //if the message was posted after the notifications were checked last time, add notification
                    if (accessResult.getLastAccessed().isBefore(msgResult.getDateTimeAdded()) && (!msgResult.getPrivateMsg()) && (!msgResult.getOwner().getName().equals(Settings.currentUser.getName()))) {
                        notifsModel.addRow(new Object[]{topicRes.getTitle(), msgResult.getOwner().getName(), msgResult.getContent()});
                    } //if the private messagte was posted after the notifications were checked last time, add notification for the topic owner
                    else if (accessResult.getLastAccessed().isBefore(msgResult.getDateTimeAdded()) && (msgResult.getPrivateMsg()) && (topicRes.getOwner().getName().equals(Settings.currentUser.getName()))) {
                        notifsModel.addRow(new Object[]{topicRes.getTitle(), msgResult.getOwner().getName(), "PM: " + msgResult.getContent()});
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //update notifications label
        lblNotifs.setText("Notifications (" + notifsModel.getRowCount() + ")");
    }

    public void notifyAllDeletes(DefaultTableModel notifsModel, JLabel lblNotifs) {
        //create a deleted topic template
        OneDeletedTopicToken deletedTopicTemplate = new OneDeletedTopicToken();
        try {
            //create a matchset to get all deleted topic templates
            MatchSet ms = Settings.space.contents(Arrays.asList(deletedTopicTemplate), null, 100 * 3, Long.MAX_VALUE);
            OneDeletedTopicToken deletedTopicResult;
            //create a notification access template with the current username to get the last time the user accessed notifications
            OneNotificationAccess notificationAccess = new OneNotificationAccess();
            notificationAccess.setUsername(Settings.currentUser.getName());
            //read the last time notifications were accessed from the javaspace
            OneNotificationAccess notificationAccessResult = (OneNotificationAccess) Settings.space.readIfExists(notificationAccess, null, 100);
            //loop through each result in the matchset, assigning each result to deletedTopicResult
            while ((deletedTopicResult = (OneDeletedTopicToken) ms.next()) != null) {
                //create a subscriptionTemplate with the deleted topic ID and the current username
                OneNotificationSubscription subscriptionTemplate = new OneNotificationSubscription(deletedTopicResult.TopicID, Settings.currentUser.getName());
                //read from the space into subscriptionResult
                OneNotificationSubscription subscriptionResult = (OneNotificationSubscription) Settings.space.readIfExists(subscriptionTemplate, null, 100);
                //ensure subscriptionResult is not null
                if (subscriptionResult != null) {
                    //check if the topic was deleted after the user accessed their notifications last time
                    if (notificationAccessResult.getLastAccessed().isBefore(deletedTopicResult.DateTimeDeleted)) {
                        //if so, add to the notifications table
                        notifsModel.addRow(new Object[]{deletedTopicResult.TopicTitle, deletedTopicResult.Owner.getName(), "This topic was deleted."});
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //update notification count
        lblNotifs.setText("Notifications (" + notifsModel.getRowCount() + ")");
    }

    public void notifySubscribersOfMessage(OneMessage msg, DefaultTableModel notifsModel, JLabel notifs) {
        //create a subscription template with topic id of inputted message and current username 
        OneNotificationSubscription subscriptionTemplate = new OneNotificationSubscription(msg.getTopicID(), Settings.currentUser.getName());
        try {
            //read the subscription result from the javaspace
            OneNotificationSubscription subscriptionResult = (OneNotificationSubscription) Settings.space.readIfExists(subscriptionTemplate, null, 100);
            //ensure result is not null
            if (subscriptionResult != null) {
                //create a topic template, setting the topic id to inputted message topic id
                OneTopic topicTemplate = new OneTopic();
                topicTemplate.setTopicID(msg.getTopicID());
                //read the topic from the javaspace
                OneTopic topicResult = (OneTopic) Settings.space.readIfExists(topicTemplate, null, 100);
                if (topicResult != null) {
                    //add a notification to the notification table
                    notifsModel.addRow(new Object[]{topicResult.getTitle(), msg.getOwner().getName(), msg.getContent()});
                    //update the notification count
                    notifs.setText("Notifications (" + notifsModel.getRowCount() + ")");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void notifySubscribersOfDelete(OneDeletedTopicToken deletionToken, DefaultTableModel notifsModel, JLabel notifs) {
        //create a subscription template with the topic id of the inputted dleetion token and the current username
        OneNotificationSubscription subscriptionTemplate = new OneNotificationSubscription(deletionToken.TopicID, Settings.currentUser.getName());
        try {
            //take the notification subscription from the javaspace if it exists
            OneNotificationSubscription subscription = (OneNotificationSubscription) Settings.space.readIfExists(subscriptionTemplate, null, 100);
            //ensure it's not null
            if (subscription != null) {
                //add notification to the table
                notifsModel.addRow(new Object[]{deletionToken.TopicTitle, deletionToken.Owner.getName(), "This topic was deleted."});
                //update notification count
                notifs.setText("Notifications (" + notifsModel.getRowCount() + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerTopicForAvailability(RemoteEventListener listener) {
        try {
            //create a topic list template
            List<OneTopic> topicsTemplate = new ArrayList<OneTopic>();
            OneTopic topicTemplate = new OneTopic();
            topicsTemplate.add(topicTemplate);
            //registers for any notifications of new topics
            UnicastRemoteObject.exportObject(listener, 0);
            Settings.space.registerForAvailabilityEvent(topicsTemplate, null, false, listener, Lease.FOREVER, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerMessagesForAvailability(RemoteEventListener listener) {
        try {
            //create a messge list template
            List<OneMessage> messagesTemplate = new ArrayList<OneMessage>();
            OneMessage messageTemplate = new OneMessage();
            messagesTemplate.add(messageTemplate);
            //registers for any notifications of new messages
            UnicastRemoteObject.exportObject(listener, 0);
            Settings.space.registerForAvailabilityEvent(messagesTemplate, null, false, listener, Lease.FOREVER, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerDeletionForAvailability(RemoteEventListener listener) {
        try {
            //create a deletiontoken list template
            List<OneDeletedTopicToken> deletionTokensTemplate = new ArrayList<OneDeletedTopicToken>();
            OneDeletedTopicToken deletionTokenTemplate = new OneDeletedTopicToken();
            deletionTokensTemplate.add(deletionTokenTemplate);
            //registers for any notifications of new deletion tokens
            UnicastRemoteObject.exportObject(listener, 0);
            Settings.space.registerForAvailabilityEvent(deletionTokensTemplate, null, false, listener, Lease.FOREVER, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateNotificationLastAccess() {
        try {
            //create a template for notifications access using current user's username
            OneNotificationAccess template = new OneNotificationAccess();
            template.setUsername(Settings.currentUser.getName());
            //remove any current results
            Settings.space.take(Arrays.asList(template), null, 1000 * 3, 1000 * 3);
            LocalDateTime ldt = LocalDateTime.now();
            //create a new notification access object with the current time
            OneNotificationAccess initialNotifAccess = new OneNotificationAccess(Settings.currentUser.getName(), ldt);
            //write to the javaspace
            Settings.space.write(initialNotifAccess, null, Lease.FOREVER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteTopic(int topicID) {
        //create a topic template using the inputted topicID
        OneTopic topicToDeleteTemplate = new OneTopic();
        topicToDeleteTemplate.setTopicID(topicID);
        OneTopic deletedTopic = null;
        try {
            //take/delete the topic from the space
            deletedTopic = (OneTopic) Settings.space.takeIfExists(topicToDeleteTemplate, null, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //create a template with the deleted topic id
        OneMessage msgsToDeleteTemplate = new OneMessage();
        msgsToDeleteTemplate.setTopicID(topicID);
        try {
            //take/delete all messages associated with deleted topic
            Settings.space.take(Arrays.asList(msgsToDeleteTemplate), null, 100 * 3, Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addTopic(String newTopicTitle) {
        LocalDateTime now = LocalDateTime.now();
        try {
            //Create a new topic with a unique ID
            OneTopic newTopic = new OneTopic(getID(), newTopicTitle, Settings.currentUser, now);
            //write to the space
            Settings.space.write(newTopic, null, Lease.FOREVER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Integer getID() {
        //create new arraylist and initialise with the value '0'.
        ArrayList<Integer> topics = new ArrayList<Integer>();
        topics.add(0);
        try {
            //Create a matchset of all topics
            MatchSet ms = Settings.space.contents(Arrays.asList(new OneTopic()), null, 100 * 3, Long.MAX_VALUE);
            OneTopic topicResult;
            //loop through all results in matchset, assigning the result to topicResult
            while ((topicResult = (OneTopic) ms.next()) != null) {
                //add the result to topics arraylist
                topics.add(topicResult.getTopicID());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //get the maximum number stored in the arraylist
        Integer max = Collections.max(topics);
        //return maximum incremented by 1
        return max + 1;
    }

    public void writeDeletedTopicToken(OneTopic topicToDelete, Integer tableIndex) {
        try {
            //create a deletion token using current date + time
            LocalDateTime ldt = LocalDateTime.now();
            OneDeletedTopicToken deletionToken = new OneDeletedTopicToken(topicToDelete.getTopicID(), tableIndex, ldt, topicToDelete.getOwner(), topicToDelete.getTitle());
            //write to space
            Settings.space.write(deletionToken, null, Lease.FOREVER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean changePassword(String oldPassword, String newPassword) {
        //Create a transaction using transaction manager
        Transaction.Created trc = null;
        try {
            trc = TransactionFactory.create(Settings.mgr, 3000);
        } catch (Exception e) {
            System.out.println("Could not create transaction " + e);;
        }
        Transaction txn = trc.transaction;
        //Create a user tempalate using current user's username
        OneUser userTemplate = new OneUser();
        userTemplate.setName(Settings.currentUser.getName());
        try {
            //take from javaspace if it exists
            OneUser userResult = (OneUser) Settings.space.takeIfExists(userTemplate, txn, 100);
            //ensure result is not null
            if (userResult != null) {
                //decrypt result password and ensure user input is correct
                if (aes.decrypt(userResult.EncryptedPassword).equals(oldPassword)) {
                    //update password with encrypted version of new password
                    userResult.setPassword(aes.encrypt(newPassword));
                    //write new user details to space
                    Settings.space.write(userResult, txn, Lease.FOREVER);
                    //commit transaction
                    txn.commit();
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                //abort transaction since password change failed
                txn.abort();
            } catch (RemoteException | CannotAbortException | UnknownTransactionException e2) {
                e2.printStackTrace();
            }
        }
        return false;
    }

    public void listenForTopicsAndNotify(ArrayList<OneTopic> topics, DefaultTableModel topicModel) {
        //listens for any new topics
        RemoteEventListener topicsListener = new RemoteEventListener() {
            @Override
            //method to be ran if any new topics are updates
            public void notify(RemoteEvent theEvent) throws UnknownEventException, RemoteException {
                AvailabilityEvent event = (AvailabilityEvent) theEvent;
                try {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    OneTopic topic = (OneTopic) event.getEntry();
                    //add new topic to the table model
                    topics.add(topic);
                    topicModel.addRow(new Object[]{topic.getTitle(), topic.getOwner().getName(), dtf.format(topic.getDateTimeAdded())});
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        };
        //registers for event triggered when new topic added
        registerTopicForAvailability(topicsListener);
    }

    public void listenForMessagesAndNotify(DefaultTableModel notifsModel, JLabel lblNotifications) {
        //listens for any new messages for subscribed topics
        RemoteEventListener notifsListener = new RemoteEventListener() {
            @Override
            //method to be ran if any new messages are added
            public void notify(RemoteEvent theEvent) throws UnknownEventException, RemoteException {
                AvailabilityEvent event = (AvailabilityEvent) theEvent;
                try {
                    OneMessage message = (OneMessage) event.getEntry();
                    //run method to notify all subscribers
                    notifySubscribersOfMessage(message, notifsModel, lblNotifications);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        //registers for event triggered when new message added
        registerMessagesForAvailability(notifsListener);
    }

    public void listenForDeletionAndNotify(DefaultTableModel notifsModel, DefaultTableModel topicModel, ArrayList<OneTopic> topics, JLabel lblNotifications) {
        //listens for any new deleted topic tokens
        RemoteEventListener deletedTopicsListener = new RemoteEventListener() {
            @Override
            //method to be ran if new deleted topic tokens are added 
            public void notify(RemoteEvent theEvent) throws UnknownEventException, RemoteException {
                AvailabilityEvent event = (AvailabilityEvent) theEvent;
                try {
                    OneDeletedTopicToken deletionToken = (OneDeletedTopicToken) event.getEntry();
                    //remove row from table
                    topicModel.removeRow(deletionToken.TableIndex);
                    //run method to notify subscribers
                    notifySubscribersOfDelete(deletionToken, notifsModel, lblNotifications);
                    //remove from arraylist
                    topics.remove(deletionToken.TableIndex.intValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        //registers for event triggered when new deletion token added
        registerDeletionForAvailability(deletedTopicsListener);
    }

    public boolean validateNewTopic(String newTopicTitle) {
        //ensure that txtNewTopic is not blank or full of spaces
        if ((newTopicTitle != null) && (!newTopicTitle.isEmpty())) {
            //ensure new topic name is between 5 and 40 characters
            if ((newTopicTitle.length() >= 5) && (newTopicTitle.length() <= 40)) {
                //call method to add new topic
                addTopic(newTopicTitle);
                JOptionPane.showMessageDialog(null, "Topic added successfully");
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Topic Name must be 5-40 characters", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            JOptionPane.showMessageDialog(null, "Enter a topic name", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public void verifyPasswordChange(String currentPassword, String newPassword, String confirmedNewPassword) {
        //ensure new password and confirmation are the same
        if (newPassword.equals(confirmedNewPassword)) {
            //check password strength
            if (passwordStrength(newPassword)) {
                //call method to change password. If it returns true, password change successful
                if (changePassword(currentPassword, newPassword)) {
                    JOptionPane.showMessageDialog(null, "Password change successful");
                } else {
                    //password change failed
                    JOptionPane.showMessageDialog(null, "Current password is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);

                }
            } else {
                //not a strong password
                JOptionPane.showMessageDialog(null, "Your new password must contain at least one capital letter, one lower case letter and one number and contain between 6-15 characters", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            //new password and confirmation password are different
            JOptionPane.showMessageDialog(null, "Password confirmation must match the new password!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean passwordStrength(String password) {
        //regex to check password strength
        return password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9]).{6,15}$");
    }
}

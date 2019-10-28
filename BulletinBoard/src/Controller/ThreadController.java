/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Model.OneMessage;
import Model.OneNotificationAccess;
import Model.OneNotificationSubscription;
import Model.OneTopic;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lease.Lease;
import net.jini.space.AvailabilityEvent;
import net.jini.space.MatchSet;

/**
 * ThreadController
 *
 * @author Hamza Saeed - u1550400
 */
public class ThreadController {

    public Settings Settings;

    public ThreadController(Settings Settings) {
        this.Settings = Settings;
    }

    public void loadSubscriptionSettings(JCheckBox checkSubc, OneTopic currentTopic) {
        //create a new subscription template with the topic id and the user's username
        OneNotificationSubscription subscriptionTemplate = new OneNotificationSubscription(currentTopic.getTopicID(), Settings.currentUser.getName());
        System.out.println("currentTopic:" + currentTopic.getTitle() + Settings.currentUser.getName());
        try {
            //read from the javaspace
            OneNotificationSubscription subscriptionResult = (OneNotificationSubscription) Settings.space.readIfExists(subscriptionTemplate, null, 3000);
            //if it exists, check the checkbox
            if (subscriptionResult != null) {
                System.out.println("It Exists");
                checkSubc.setSelected(true);
            } //if it doesn't exist, uncheck the checkbox 
            else {
                checkSubc.setSelected(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void retrieveMessages(OneTopic currentTopic, DefaultTableModel model) {
        try {
            //create a matchset of messages
            MatchSet messagesMatchSet = Settings.space.contents(Arrays.asList(new OneMessage(currentTopic.getTopicID())), null, 100 * 3, Long.MAX_VALUE);
            OneMessage messageResult;
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            //loop through each result, initialising each result to messageResult 
            while ((messageResult = (OneMessage) messagesMatchSet.next()) != null) {
                //if result is a private message, display to the topic owner only. add prefix 'PM' to indiciate that it's a private message. Also, use HTML to colour PMs red.
                if (messageResult.getPrivateMsg()) {
                    if (Settings.currentUser.getName().equals(messageResult.getOwner().getName()) || (Settings.currentUser.getName().equals(currentTopic.getOwner().getName()))) {
                        model.addRow(new Object[]{"<html><font color=red> PM: " + messageResult.getContent() + "</font></html>", "<html><font color=red>" + messageResult.getOwner().getName() + "</font></html>", "<html><font color=red>" + dtf.format(messageResult.getDateTimeAdded()) + "</font></html>"});
                    }
                } else {
                    //otherwise display to everyone
                    model.addRow(new Object[]{messageResult.getContent(), messageResult.getOwner().getName(), dtf.format(messageResult.getDateTimeAdded())});
                }
            }
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

    public boolean doesTopicStillExist(OneTopic currentTopic) {
        //create topic template with current topic id
        OneTopic topicTemplate = new OneTopic();
        topicTemplate.setTopicID(currentTopic.getTopicID());
        try {
            //read from space
            OneTopic topicResult = (OneTopic) Settings.space.readIfExists(topicTemplate, null, 100);
            //if not null, topic still exists, return true
            if (topicResult != null) {
                return true;
            } //otherwise return true
            else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addMessage(String newMsgText, OneTopic currentTopic, JCheckBox checkPrivate) {
        LocalDateTime now = LocalDateTime.now();
        try {
            //new message
            OneMessage newMessage = new OneMessage(currentTopic.getTopicID(), newMsgText, Settings.currentUser, now, checkPrivate.isSelected());
            //write to the javaspace
            Settings.space.write(newMessage, null, Lease.FOREVER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateSubscription(JCheckBox checkSubsc, OneTopic currentTopic) {
        //if user wants to subscribe to current topic
        if (checkSubsc.isSelected()) {
            //Create a subscription template with the topic id and the user's username
            OneNotificationSubscription newSubscription = new OneNotificationSubscription(currentTopic.getTopicID(), Settings.currentUser.getName());
            try {
                //write a new subscription to the space
                Settings.space.write(newSubscription, null, Lease.FOREVER);
                LocalDateTime ldt = LocalDateTime.now();
                //add the current time as the last time notifications were accessed and write to the space
                OneNotificationAccess notificationAccessTemplate = new OneNotificationAccess(Settings.currentUser.getName(), ldt);
                Settings.space.write(notificationAccessTemplate, null, Lease.FOREVER);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //if user wants to unsubscribe from current topic
        } else {
            try {
                //Create a subscription template with the current topic id and the current user's username
                OneNotificationSubscription subscriptionTemplate = new OneNotificationSubscription(currentTopic.getTopicID(), Settings.currentUser.getName());
                //take/delete from the space
                Settings.space.takeIfExists(subscriptionTemplate, null, 200);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void listenForMessagesAndNotify(DefaultTableModel messageModel, OneTopic currentTopic) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        //listens for any new messages 
        RemoteEventListener messageListener = new RemoteEventListener() {
            @Override
            //method to be ran if new messages are added
            public void notify(RemoteEvent theEvent) throws UnknownEventException, RemoteException {
                AvailabilityEvent event = (AvailabilityEvent) theEvent;
                try {
                    OneMessage message = (OneMessage) event.getEntry();
                    //if message is a private message, add it to the table for the owner only with "PM:" proceeding message to indicate it's a private message. Also, use HTML to colour PMs red.
                    if (message.getPrivateMsg()) {
                        if (Settings.currentUser.getName().equals(message.getOwner().getName()) || (Settings.currentUser.getName().equals(currentTopic.getOwner().getName()))) {
                            messageModel.addRow(new Object[]{"<html><font color=red> PM: " + message.getContent() + "</font></html>", "<html><font color=red>" + message.getOwner().getName() + "</font></html>", "<html><font color=red>" + dtf.format(message.getDateTimeAdded()) + "</font></html>"});
                        }
                    } else {
                        //add message to table
                        messageModel.addRow(new Object[]{message.getContent(), message.getOwner().getName(), dtf.format(message.getDateTimeAdded())});
                    }
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        };
        //registers for event triggered when new message added
        registerMessagesForAvailability(messageListener);
    }

    public boolean verifyNewMessage(String newMessage, OneTopic currentTopic, JCheckBox checkPrivate) {
        OneTopic topicTemplate = new OneTopic();
        topicTemplate.TopicID = currentTopic.TopicID;
        if (newMessage.length() <= 100) {
            //ensure that txtComment is not blank or full of spaces
            if ((newMessage != null) && (!newMessage.trim().isEmpty())) {
                //call method to add message
                addMessage(newMessage, currentTopic, checkPrivate);
                JOptionPane.showMessageDialog(null, "Message successfully added");
                return true;

            } else {
                JOptionPane.showMessageDialog(null, "Enter a message", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            JOptionPane.showMessageDialog(null, "Message must be between 1 and 100 characters", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}



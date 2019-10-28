/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import net.jini.core.entry.Entry;

/**
 * OneNotificationSubscription
 * @author Hamza Saeed - u1550400
 */
public class OneNotificationSubscription implements Entry {
    
    public Integer TopicID;
    public String Username;
    
    public OneNotificationSubscription()
    {
    }
    
    public OneNotificationSubscription(Integer topicID, String username)
    {
        TopicID = topicID;
        Username = username;
    }

    public Integer getTopicID() {
        return TopicID;
    }

    public void setTopicID(Integer TopicID) {
        this.TopicID = TopicID;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String Username) {
        this.Username = Username;
    }
    
}

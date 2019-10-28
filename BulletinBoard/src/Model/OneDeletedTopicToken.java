/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.time.LocalDateTime;
import net.jini.core.entry.Entry;

/**
 * OneDeletedTopicToken
 * @author Hamza Saeed - u1550400
 */
public class OneDeletedTopicToken implements Entry {

    public Integer TopicID;
    public Integer TableIndex;
    public LocalDateTime DateTimeDeleted;
    public OneUser Owner;
    public String TopicTitle;
    
    public OneDeletedTopicToken() {

    }

    public OneDeletedTopicToken(Integer topicID, Integer tableIndex, LocalDateTime dateTimeDeleted, OneUser owner, String topicTitle) {
        TopicID = topicID;
        TableIndex = tableIndex;
        DateTimeDeleted = dateTimeDeleted;
        Owner = owner;
        TopicTitle = topicTitle;
    }

}

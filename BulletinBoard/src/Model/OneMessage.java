package Model;

import java.time.LocalDateTime;
import net.jini.core.entry.Entry;

/**
 * OneMessage
 * @author Hamza Saeed - u1550400
 */
public class OneMessage implements Entry{
    
        public Integer TopicID;
    	public String Content;
	public OneUser Owner; 
	public LocalDateTime DateTimeAdded;
        public Boolean PrivateMsg;
    
        public OneMessage()
        {    
        }
        
        public OneMessage(Integer topicID)
        {    
            TopicID = topicID;
        }
        
        public OneMessage(Integer topicID, String content, OneUser owner, LocalDateTime dateTimeAdded, boolean privateMsg)
        {
            TopicID = topicID;
            Content = content;
            Owner = owner;
            DateTimeAdded = dateTimeAdded;
            PrivateMsg = privateMsg;
        }

    public Integer getTopicID() {
        return TopicID;
    }

    public void setTopicID(Integer TopicID) {
        this.TopicID = TopicID;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String Content) {
        this.Content = Content;
    }

    public OneUser getOwner() {
        return Owner;
    }

    public void setOwner(OneUser Owner) {
        this.Owner = Owner;
    }

    public LocalDateTime getDateTimeAdded() {
        return DateTimeAdded;
    }

    public void setDateTimeAdded(LocalDateTime DateTimeAdded) {
        this.DateTimeAdded = DateTimeAdded;
    }

    public Boolean getPrivateMsg() {
        return PrivateMsg;
    }

    public void setPrivateMsg(Boolean PrivateMsg) {
        this.PrivateMsg = PrivateMsg;
    }
        
        
    
}

package Model;

import java.time.LocalDateTime;
import net.jini.core.entry.Entry;

/**
 * OneTopic
 * @author Hamza Saeed - u1550400
 */

public class OneTopic implements Entry {
	
        public Integer TopicID;
	public String Title;
	public OneUser Owner; 
	public LocalDateTime DateTimeAdded;
        
	public OneTopic()
	{
	}
	
	public  OneTopic(Integer topicID, String title, OneUser owner, LocalDateTime dateTime)
	{
                TopicID = topicID;
		Title = title;
		Owner = owner;
                DateTimeAdded = dateTime;
        }

    public Integer getTopicID() {
        return TopicID;
    }

    public void setTopicID(Integer TopicID) {
        this.TopicID = TopicID;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String Title) {
        this.Title = Title;
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
	
        
        
}

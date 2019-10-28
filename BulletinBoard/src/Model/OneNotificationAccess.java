 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.time.LocalDateTime;
import net.jini.core.entry.Entry;

/**
 * OneNotificationAccess
 * @author Hamza Saeed - u1550400
 */
public class OneNotificationAccess implements Entry{
    public String Username ;
    public LocalDateTime LastAccessed;
    
    public OneNotificationAccess()
    {
    }
    
    public OneNotificationAccess(String username, LocalDateTime lastAccess)
    {
        Username = username;
        LastAccessed = lastAccess;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String Username) {
        this.Username = Username;
    }

    public LocalDateTime getLastAccessed() {
        return LastAccessed;
    }

    public void setLastAccessed(LocalDateTime LastAccessed) {
        this.LastAccessed = LastAccessed;
    }
    
}

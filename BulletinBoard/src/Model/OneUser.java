package Model;

import net.jini.core.entry.Entry;

/**
 * OneUser
 *
 * @author Hamza Saeed - u1550400
 */
public class OneUser implements Entry {

    public String Name;
    public byte[] EncryptedPassword;

    public OneUser() {
    }

    public OneUser(String name, byte[] encryptedPassword) {
        Name = name;
        EncryptedPassword = encryptedPassword;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public byte[] getPassword() {
        return EncryptedPassword;
    }

    public void setPassword(byte[] encryptedPassword) {
        this.EncryptedPassword = encryptedPassword;
    }

}

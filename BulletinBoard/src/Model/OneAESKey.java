/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import javax.crypto.SecretKey;
import net.jini.core.entry.Entry;

/**
 * OneAESKey
 *
 * @author Hamza Saeed - u1550400
 */
public class OneAESKey implements Entry {

    public SecretKey aesKey;

    public OneAESKey() {
    }

    public SecretKey getAesKey() {
        return aesKey;
    }

    public void setAesKey(SecretKey aesKey) {
        this.aesKey = aesKey;
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AESSecurity;

import Controller.Settings;
import Model.OneAESKey;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import net.jini.core.lease.Lease;

/**
 * AESAlgorithm
 * @author Hamza Saeed - u1550400
 */
public class AESAlgorithm {

    public SecretKey aesKey;

    public AESAlgorithm(Settings settings) {
        try {
            //create oneAESKey template
            OneAESKey aesKeyTemplate = new OneAESKey();
            //retrieve from javaspace
            OneAESKey aesKeyResult = (OneAESKey) settings.space.read(aesKeyTemplate, null, 200);
            //if the result isn't null, set the key to the value retrieved
            if (aesKeyResult != null) {
                aesKey = aesKeyResult.getAesKey();
            } 
            //if it's null, generate a new key and write it to the javaspace
            else {
                KeyGenerator kgen = KeyGenerator.getInstance("AES");
                kgen.init(128);
                aesKey = kgen.generateKey();
                OneAESKey newKey = new OneAESKey();
                newKey.setAesKey(aesKey);
                settings.space.write(newKey, null, Lease.FOREVER);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] encrypt(String password) {
        byte[] encrypted = null;
        try {
            //AES encryption setup
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            //encrypt password
            encrypted = cipher.doFinal(password.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encrypted;
    }

    public String decrypt(byte[] encryptedPassword) {
        String decrypted = null;
        try {
            //AES decryption setup
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            //decrypt encryptedpassword
            decrypted = new String(cipher.doFinal(encryptedPassword));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decrypted;
    }
}

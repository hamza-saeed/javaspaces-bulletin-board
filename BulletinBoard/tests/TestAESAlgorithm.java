import AESSecurity.AESAlgorithm;
import Controller.Settings;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnitTest - TestAESAlgorithm
 * @author Hamza Saeed - u1550400
 */
public class TestAESAlgorithm {
    AESAlgorithm aes;
    
    public TestAESAlgorithm() {
        Settings settings = new Settings();
        aes = new AESAlgorithm(settings);
    }
    @Test
    public void testLowerCase() {
        //test with lower case string
        TestAES("test");
    }
    @Test
    public void testWithCapital() {
        //test with string containing capital
        TestAES("Test");
    }
    @Test
    public void testContainingNum() {
        //test with string containing number
        TestAES("Test123");
    }
    @Test
    public void testContainingSpecialChar() {
        //test with string containing special character
        TestAES("Test123.");
    }
    public void TestAES(String inputString) {
        byte[] encryptedString = aes.encrypt(inputString);
        assertNotEquals(inputString, encryptedString.toString());
        String decryptedString = aes.decrypt(encryptedString);
        assertEquals(inputString, decryptedString);
    }
}

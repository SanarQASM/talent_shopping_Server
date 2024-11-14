package application;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionAndDecryptionPass {
    private static EncryptionAndDecryptionPass eADP;
    private static final String AES = "AES";
    private static final int KEY_SIZE = 128;
    private static SecretKey secretKey;

    public static EncryptionAndDecryptionPass getInstance() {
        if(eADP==null){
            eADP = new EncryptionAndDecryptionPass();
        }
        return eADP;
    }

    public String generateSecretKey(){
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[KEY_SIZE / 8];
        secureRandom.nextBytes(key);
        secretKey = new SecretKeySpec(key, AES);
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }
    public EncryptionAndDecryptionPass(){}
    public EncryptionAndDecryptionPass(String seceretKey){
        byte[] keyBytes = Base64.getDecoder().decode(seceretKey);
        EncryptionAndDecryptionPass.secretKey= new SecretKeySpec(keyBytes, "AES");
    }
    public String encrypt(String plainText) throws Exception{
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
    public String decrypt(String encryptedText)throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}

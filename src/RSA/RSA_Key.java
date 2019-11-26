package RSA;

import java.io.File;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

public class RSA_Key {
    public static PublicKey publicKey;
    public static PrivateKey privateKey;
    public static void GenerateKeys(){
        try{
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(1024);
            RSA_Key.privateKey = generator.genKeyPair().getPrivate();
            RSA_Key.publicKey = generator.genKeyPair().getPublic();
        } catch(NoSuchAlgorithmException e){
            System.out.println(e);
        }
    }
    public static PublicKey parsePublicKey(String publicKey) throws InvalidKeySpecException, NoSuchAlgorithmException{
        byte[] keyBytes = Base64.getDecoder().decode(publicKey) ;
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
    public static String encrypt(String str,PublicKey pubkey) throws Exception{
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubkey);
        byte[] byteEncrypted = cipher.doFinal(str.getBytes());
        return  Base64.getEncoder().encodeToString(byteEncrypted);
    }
    public static String decrypt(String str,PrivateKey privateKey) throws Exception{
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] byteDecrypted = cipher.doFinal(Base64.getDecoder().decode(str));
        return new String(byteDecrypted);
    }
}
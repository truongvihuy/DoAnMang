package RSA;

import java.io.File;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

public class RSA {
    public static final String PUBLIC_KEY_FILE = "rsa_keypair/public_key.der";
	public static final String PRIVATE_KEY_FILE = "rsa_keypair/private_key.der";
	public static PrivateKey getPrivateKey() throws Exception {
		byte[] keyBytes = Files.readAllBytes(new File(PRIVATE_KEY_FILE).toPath());
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePrivate(spec);
	}

	public static PublicKey getPublicKey() throws Exception {
		byte[] keyBytes = Files.readAllBytes(new File(PUBLIC_KEY_FILE).toPath());
		X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(spec);
	}
	public static String encrypt(String str) throws Exception{
            PublicKey publicKey = getPublicKey();
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] byteEncrypted = cipher.doFinal(str.getBytes());
            return  Base64.getEncoder().encodeToString(byteEncrypted);
        }
        public static String decrypt(String str) throws Exception{
            PrivateKey privateKey = getPrivateKey();
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] byteDecrypted = cipher.doFinal(Base64.getDecoder().decode(str));
            return new String(byteDecrypted);
        }
}
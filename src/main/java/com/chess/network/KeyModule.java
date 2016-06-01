package com.chess.network;

import javax.crypto.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * A class to assist with key exchanges on both client & serverside.
 */
public class KeyModule {

    /* Our current keypair for clients - Server will use secret keys to communicate with everyone. */
    public static KeyPair keys;

    /* Key pair algorithms are more limited. Our options are
     * Diffie-Hellman: A key exchange algorithm
     * RSA - Two separate algorithms, one for asymmetric encryption and another for digital signatures
     * DSA - Digital signature algorithm - I'm not sure about the practical application of this.
     * EC - Elliptic curve algorithm - Also unsure about the practical application of this one.
     *
     * We are going to be using asymmetric encryption to exchange the secret key, so we will be going with RSA.
     * Not using diffie-hellman due to ignorance of how to protect it against a man in the middle attack, I think we'd need some sort of trusted cert.
     * */
    private static String keyPairAlgorithm = "RSA";
    /* This will be our secret key algorithm. This can literally be anything, as long as the client/server are using the same thing. */
    private static String secretKeyAlgorithm = "AES";

    private static String encryptedAlgorithm = "AES/ECB/PKCS5Padding";


    //Do we want to store you or initialise you every time?
    private static KeyPairGenerator keyPairGenerator;

    public KeyModule(){
        keys = generateKeyPair();
    }

    public byte[] decrypt(byte[] encrypted, SecretKey key)
    {
        try {
            Cipher cipher = Cipher.getInstance(encryptedAlgorithm);
            cipher.init(Cipher.DECRYPT_MODE,key);
            return cipher.doFinal(encrypted);

        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        } catch (NoSuchPaddingException e)
        {
            e.printStackTrace();
        } catch (InvalidKeyException e)
        {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e)
        {
            e.printStackTrace();
        } catch (BadPaddingException e)
        {
            e.printStackTrace();
        }


        return null;
    }
    public byte[] encrypt(byte[] unencrypted, SecretKey key)
    {
        try {
            Cipher cipher = Cipher.getInstance(encryptedAlgorithm);
            cipher.init(Cipher.ENCRYPT_MODE,key);
            return cipher.doFinal(unencrypted);
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        } catch (NoSuchPaddingException e)
        {
            e.printStackTrace();
        } catch (InvalidKeyException e)
        {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e)
        {
            e.printStackTrace();
        } catch (BadPaddingException e)
        {
            e.printStackTrace();
        }
        return null;
    }


    public KeyPair generateKeyPair(){
        try {
            keyPairGenerator = KeyPairGenerator.getInstance(keyPairAlgorithm);
            keyPairGenerator.initialize(1024);
            return keyPairGenerator.genKeyPair();
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

        return null;
    }
    public static byte[] wrapSecretKey(SecretKey unencryptedKey, PublicKey publicKey)
    {
        try {
            /* We initialise it with our padding and everything specified with our keyPairEncryption string */
            Cipher cipher = Cipher.getInstance(keyPairAlgorithm);
            /* We only wrap the private key with our public keys */
            cipher.init(Cipher.WRAP_MODE,publicKey);
            /* Return the wrapped key */
            byte[] wrappedKey = cipher.wrap(unencryptedKey);
            return (wrappedKey);

        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        } catch (NoSuchPaddingException e)
        {
            e.printStackTrace();
        } catch (InvalidKeyException e)
        {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e)
        {
            e.printStackTrace();
        }
        return null; //Pls never get called.
    }

    /* Using the built in methods to wrap and unwrap keys */
    public static Key unwrapSecretKey(byte[] encryptedKey, PrivateKey privateKey)
    {
        try {
            Cipher cipher = Cipher.getInstance(keyPairAlgorithm);
            /* Unwrap using a a private key */
            cipher.init(Cipher.UNWRAP_MODE,privateKey);
            Key unWrappedKey = cipher.unwrap(encryptedKey,"AES",Cipher.SECRET_KEY);
            return unWrappedKey;
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        } catch (NoSuchPaddingException e)
        {
            e.printStackTrace();
        } catch (InvalidKeyException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    /* Because we send our public key across the network unencrypted, we will use our own unwrap sort of method for the public key. */
    public static PublicKey unwrapPublicKey(byte[] key)
    {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
        try{
            KeyFactory keyFactory = KeyFactory.getInstance(keyPairAlgorithm);
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        } catch (InvalidKeySpecException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public KeyPair getKeys()
    {
        return keys;
    }
    /* We'll generate our secret keys serverside alone. Realistically it can be anything, given that we keep it a secret */
    public static SecretKey generateSecretKey()
    {
        /* For now we'll just make you return a random key */
        KeyGenerator keyGenerator=null;
        try {
            keyGenerator = KeyGenerator.getInstance(secretKeyAlgorithm);
            keyGenerator.init(256);
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return keyGenerator.generateKey();
    }
}

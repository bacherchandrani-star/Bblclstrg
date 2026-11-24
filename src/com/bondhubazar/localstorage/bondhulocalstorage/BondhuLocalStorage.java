
package com.bondhubazar.bondhusecurestorage;

// Android Context ব্যবহার করার জন্য
import android.content.Context;

// ফোনের SharedPreferences-এ Encrypted Data রাখার জন্য
import android.content.SharedPreferences;

// Android KeyStore-এ নিরাপদ Key রাখার জন্য
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyStore;

// AES Encryption ও Decryption করার জন্য
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

// Encryption-এর IV সংরক্ষণের জন্য
import javax.crypto.spec.GCMParameterSpec;

// Base64-এ Encrypted Data রূপান্তর করার জন্য
import android.util.Base64;

// App Inventor Extension-এর মূল ক্লাস
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;

// App Inventor Form
import com.google.appinventor.components.runtime.Form;

// Extension Annotation
import com.google.appinventor.components.annotations.Extension;

// Block তৈরির Annotation
import com.google.appinventor.components.annotations.SimpleFunction;

// Event তৈরির Annotation
import com.google.appinventor.components.annotations.SimpleEvent;

// Event পাঠানোর জন্য
import com.google.appinventor.components.runtime.EventDispatcher;


// Java-এর Exception ব্যবহার
import java.security.GeneralSecurityException;


// Extension-এর পরিচয়
@Extension(
    description = "Android KeyStore এবং AES-GCM ব্যবহার করে Secure Data ও Login Session সংরক্ষণ করে"
)

public class BondhuSecureStorage
        extends AndroidNonvisibleComponent {

    // SharedPreferences-এ Encrypted Data রাখা হবে
    private SharedPreferences preferences;

    // Android KeyStore-এর নাম
    private static final String KEYSTORE_NAME =
            "AndroidKeyStore";

    // Encryption Key-এর পরিচয়
    private static final String KEY_ALIAS =
            "BondhuSecureStorageKey";

    // Session Token-এর Tag
    private static final String SESSION_TAG =
            "bondhu_login_session";


    // ==============================
    // Constructor
    // ==============================

    public BondhuSecureStorage(Form form) {

        // App Inventor Form সেট করা
        super(form);

        // Encrypted Data রাখার Preferences খোলা
        preferences = form.getSharedPreferences(
                "BondhuSecureStorage",
                Context.MODE_PRIVATE
        );
    }


    // ==============================
    // Android KeyStore খোলা
    // ==============================

    private KeyStore getKeyStore()
            throws Exception {

        // Android-এর KeyStore খোলা
        KeyStore keyStore = KeyStore.getInstance(
                KEYSTORE_NAME
        );

        // KeyStore Load করা
        keyStore.load(null);

        // KeyStore ফেরত
        return keyStore;
    }


    // ==============================
    // Encryption Key তৈরি
    // ==============================

    private SecretKey getSecretKey()
            throws Exception {

        // Android KeyStore খোলা
        KeyStore keyStore = getKeyStore();

        // আগে Key তৈরি করা থাকলে সেটি নেওয়া
        if (keyStore.containsAlias(KEY_ALIAS)) {

            // আগের Secret Key ফেরত
            return (SecretKey) keyStore.getKey(
                    KEY_ALIAS,
                    null
            );
        }

        // AES Key তৈরি করার Generator
        KeyGenerator keyGenerator =
                KeyGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES,
                        KEYSTORE_NAME
                );

        // Encryption Key-এর নিয়ম তৈরি
        KeyGenParameterSpec spec =
                new KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT
                        | KeyProperties.PURPOSE_DECRYPT
                )

                // AES-GCM Encryption ব্যবহার
                .setBlockModes(
                        KeyProperties.BLOCK_MODE_GCM
                )

                // Padding ব্যবহার হবে না
                .setEncryptionPaddings(
                        KeyProperties.ENCRYPTION_PADDING_NONE
                )

                // 256 Bit Key
                .setKeySize(256)

                // Key-এর নিয়ম সম্পন্ন
                .build();

        // KeyGenerator-এ নিয়ম সেট
        keyGenerator.init(spec);

        // নতুন Secret Key তৈরি
        return keyGenerator.generateKey();
    }


    // ==============================
    // Data Encrypt
    // ==============================

    private String encrypt(String value)
            throws Exception {

        // AES-GCM Cipher তৈরি
        Cipher cipher = Cipher.getInstance(
                "AES/GCM/NoPadding"
        );

        // Secret Key নেওয়া
        SecretKey secretKey = getSecretKey();

        // Encryption শুরু
        cipher.init(
                Cipher.ENCRYPT_MODE,
                secretKey
        );

        // Text-কে Byte-এ রূপান্তর
        byte[] encrypted = cipher.doFinal(
                value.getBytes("UTF-8")
        );

        // Encryption-এর IV নেওয়া
        byte[] iv = cipher.getIV();

        // IV এবং Encrypted Data একসঙ্গে রাখা
        byte[] combined = new byte[
                iv.length + encrypted.length
        ];

        // প্রথম অংশে IV রাখা
        System.arraycopy(
                iv,
                0,
                combined,
                0,
                iv.length
        );

        // পরের অংশে Encrypted Data রাখা
        System.arraycopy(
                encrypted,
                0,
                combined,
                iv.length,
                encrypted.length
        );

        // Base64-এ রূপান্তর করে ফেরত
        return Base64.encodeToString(
                combined,
                Base64.NO_WRAP
        );
    }


    // ==============================
    // Data Decrypt
    // ==============================

    private String decrypt(String encryptedValue)
            throws Exception {

        // Base64 থেকে Byte-এ রূপান্তর
        byte[] combined = Base64.decode(
                encryptedValue,
                Base64.NO_WRAP
        );

        // IV-এর দৈর্ঘ্য
        int ivLength = 12;

        // IV আলাদা করা
        byte[] iv = new byte[ivLength];

        System.arraycopy(
                combined,
                0,
                iv,
                0,
                ivLength
        );

        // Encrypted Data আলাদা করা
        byte[] encrypted = new byte[
                combined.length - ivLength
        ];

        System.arraycopy(
                combined,
                ivLength,
                encrypted,
                0,
                encrypted.length
        );

        // AES-GCM Cipher তৈরি
        Cipher cipher = Cipher.getInstance(
                "AES/GCM/NoPadding"
        );

        // Secret Key নেওয়া
        SecretKey secretKey = getSecretKey();

        // IV ব্যবহার করে Decryption চালু
        cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey,
                new GCMParameterSpec(
                        128,
                        iv
                )
        );

        // Encrypted Data Decrypt
        byte[] decrypted = cipher.doFinal(
                encrypted
        );

        // Byte থেকে Text-এ রূপান্তর
        return new String(
                decrypted,
                "UTF-8"
        );
    }


    // ==============================
    // Secure Data Save
    // ==============================

    @SimpleFunction(
        description = "Tag এবং Value Encrypt করে নিরাপদভাবে সংরক্ষণ করে"
    )
    public void SaveSecureData(
            String tag,
            String value
    ) {

        try {

            // Value Encrypt করা
            String encrypted = encrypt(value);

            // Encrypted Data Save
            preferences.edit()
                    .putString(tag, encrypted)
                    .apply();

            // Save Event পাঠানো
            SecureDataSaved(tag);

        } catch (Exception e) {

            // Error Event পাঠানো
            SecureError(e.toString());
        }
    }


    // ==============================
    // Secure Data Read
    // ==============================

    @SimpleFunction(
        description = "Tag-এর Encrypted Data Decrypt করে ফেরত দেয়"
    )
    public String GetSecureData(
            String tag,
            String defaultValue
    ) {

        // Tag-এর Encrypted Value নেওয়া
        String encrypted = preferences.getString(
                tag,
                ""
        );

        // Data না থাকলে Default Value ফেরত
        if (encrypted.isEmpty()) {
            return defaultValue;
        }

        try {

            // Data Decrypt করে ফেরত
            return decrypt(encrypted);

        } catch (Exception e) {

            // Decryption Error Event
            SecureError(e.toString());

            // ভুল Data হলে Default Value
            return defaultValue;
        }
    }


    // ==============================
    // Secure Data Delete
    // ==============================

    @SimpleFunction(
        description = "নির্দিষ্ট Secure Data মুছে দেয়"
    )
    public void RemoveSecureData(String tag) {

        // নির্দিষ্ট Tag Delete
        preferences.edit()
                .remove(tag)
                .apply();

        // Delete Event পাঠানো
        SecureDataRemoved(tag);
    }


    // ==============================
    // Secure Tag Check
    // ==============================

    @SimpleFunction(
        description = "Secure Storage-এ Tag আছে কিনা পরীক্ষা করে"
    )
    public boolean ContainsSecureData(
            String tag
    ) {

        // Tag থাকলে True ফেরত
        return preferences.contains(tag);
    }


    // ==============================
    // Login Session Save
    // ==============================

    @SimpleFunction(
        description = "Login Session Token Encrypt করে সংরক্ষণ করে"
    )
    public void SaveSession(
            String sessionToken
    ) {

        // Session Token Secure Storage-এ রাখা
        SaveSecureData(
                SESSION_TAG,
                sessionToken
        );
    }


    // ==============================
    // Login Session Read
    // ==============================

    @SimpleFunction(
        description = "সংরক্ষিত Login Session Token ফেরত দেয়"
    )
    public String GetSession() {

        // Session Token পড়া
        return GetSecureData(
                SESSION_TAG,
                ""
        );
    }


    // ==============================
    // Login Check
    // ==============================

    @SimpleFunction(
        description = "Session Token সংরক্ষিত আছে কিনা পরীক্ষা করে"
    )
    public boolean IsLoggedIn() {

        // Session Token পড়া
        String session = GetSession();

        // Token খালি না হলে True
        return session != null
                && !session.trim().isEmpty();
    }


    // ==============================
    // Session Clear
    // ==============================

    @SimpleFunction(
        description = "Login Session মুছে দেয়"
    )
    public void ClearSession() {

        // Session Tag Delete
        RemoveSecureData(
                SESSION_TAG
        );
    }


    // ==============================
    // সব Secure Data Delete
    // ==============================

    @SimpleFunction(
        description = "এই Extension-এর সব Secure Data মুছে দেয়"
    )
    public void ClearAllSecureData() {

        // SharedPreferences-এর সব Data Delete
        preferences.edit()
                .clear()
                .apply();

        // Secure Data Clear Event
        SecureStorageCleared();
    }


    // ==============================
    // Encryption Key Delete
    // ==============================

    @SimpleFunction(
        description = "Android KeyStore থেকে Encryption Key Delete করে"
    )
    public void DeleteEncryptionKey() {

        try {

            // KeyStore খোলা
            KeyStore keyStore = getKeyStore();

            // Key থাকলে মুছে ফেলা
            if (keyStore.containsAlias(KEY_ALIAS)) {

                keyStore.deleteEntry(
                        KEY_ALIAS
                );
            }

            // Key Delete Event
            SecureStorageCleared();

        } catch (Exception e) {

            // Error Event
            SecureError(e.toString());
        }
    }


    // ==============================
    // Secure Data Saved Event
    // ==============================

    @SimpleEvent
    public void SecureDataSaved(
            String tag
    ) {

        // Kodular-এ Event পাঠানো
        EventDispatcher.dispatchEvent(
                this,
                "SecureDataSaved",
                tag
        );
    }


    // ==============================
    // Secure Data Removed Event
    // ==============================

    @SimpleEvent
    public void SecureDataRemoved(
            String tag
    ) {

        // Kodular-এ Event পাঠানো
        EventDispatcher.dispatchEvent(
                this,
                "SecureDataRemoved",
                tag
        );
    }


    // ==============================
    // Secure Storage Cleared Event
    // ==============================

    @SimpleEvent
    public void SecureStorageCleared() {

        // Kodular-এ Event পাঠানো
        EventDispatcher.dispatchEvent(
                this,
                "SecureStorageCleared"
        );
    }


    // ==============================
    // Secure Error Event
    // ==============================

    @SimpleEvent
    public void SecureError(
            String message
    ) {

        // Kodular-এ Error Event পাঠানো
        EventDispatcher.dispatchEvent(
                this,
                "SecureError",
                message
        );
    }

}


package com.bondhubazar.localstorage;

// Android-এর Context ব্যবহার করার জন্য
import android.content.Context;

// ফোনের স্থায়ী Key-Value Storage ব্যবহার করার জন্য
import android.content.SharedPreferences;

// App Inventor Extension-এর মূল ক্লাস
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;

// App Inventor-এর Form ব্যবহার করার জন্য
import com.google.appinventor.components.runtime.Form;

// Extension তৈরি করার Annotation
import com.google.appinventor.components.annotations.Extension;

// Block তৈরি করার Annotation
import com.google.appinventor.components.annotations.SimpleFunction;

// Event তৈরি করার Annotation
import com.google.appinventor.components.annotations.SimpleEvent;

// Block-এর ভিতরে Event পাঠানোর জন্য
import com.google.appinventor.components.runtime.EventDispatcher;


// Extension-এর পরিচয়
@Extension(
    description = "ফোনে স্থায়ীভাবে ছোট ছোট ডেটা সংরক্ষণ করে"
)

public class BondhuLocalStorage
        extends AndroidNonvisibleComponent {

    // SharedPreferences-এর ভেরিয়েবল
    private SharedPreferences storage;

    // Extension-এর Constructor
    public BondhuLocalStorage(Form form) {

        // App Inventor-এর Form-কে Parent হিসেবে সেট করা
        super(form);

        // ফোনের Internal Preference Storage তৈরি বা খোলা
        storage = form.getSharedPreferences(
                "BondhuLocalStorage",
                Context.MODE_PRIVATE
        );
    }


    // ==============================
    // Text Data Save
    // ==============================

    @SimpleFunction(
        description = "Tag ব্যবহার করে Text Data ফোনে Save করে"
    )
    public void SaveData(String tag, String value) {

        // Editor তৈরি করে ডেটা লেখার প্রস্তুতি নেওয়া
        SharedPreferences.Editor editor = storage.edit();

        // Tag-এর মধ্যে Text Value সংরক্ষণ
        editor.putString(tag, value);

        // ডেটা স্থায়ীভাবে Save করা
        editor.apply();

        // Save সফলভাবে সম্পন্ন হলে Event পাঠানো
        DataSaved(tag);
    }


    // ==============================
    // Text Data Read
    // ==============================

    @SimpleFunction(
        description = "Tag দিয়ে ফোন থেকে Text Data পড়ে"
    )
    public String GetData(String tag, String defaultValue) {

        // Tag-এর ডেটা পাওয়া গেলে ফেরত দেবে
        // না পাওয়া গেলে Default Value ফেরত দেবে
        return storage.getString(tag, defaultValue);
    }


    // ==============================
    // Number Save
    // ==============================

    @SimpleFunction(
        description = "Integer Number ফোনে Save করে"
    )
    public void SaveNumber(String tag, int value) {

        // Editor তৈরি
        SharedPreferences.Editor editor = storage.edit();

        // Integer Number সংরক্ষণ
        editor.putInt(tag, value);

        // Save সম্পন্ন
        editor.apply();

        // Save Event পাঠানো
        DataSaved(tag);
    }


    // ==============================
    // Number Read
    // ==============================

    @SimpleFunction(
        description = "ফোন থেকে Integer Number পড়ে"
    )
    public int GetNumber(String tag, int defaultValue) {

        // সংরক্ষিত Number ফেরত দেয়
        // না থাকলে Default Number দেয়
        return storage.getInt(tag, defaultValue);
    }


    // ==============================
    // Boolean Save
    // ==============================

    @SimpleFunction(
        description = "True অথবা False ফোনে Save করে"
    )
    public void SaveBoolean(String tag, boolean value) {

        // Editor তৈরি
        SharedPreferences.Editor editor = storage.edit();

        // Boolean Data Save
        editor.putBoolean(tag, value);

        // Save সম্পন্ন
        editor.apply();

        // Save Event পাঠানো
        DataSaved(tag);
    }


    // ==============================
    // Boolean Read
    // ==============================

    @SimpleFunction(
        description = "ফোন থেকে True অথবা False পড়ে"
    )
    public boolean GetBoolean(String tag, boolean defaultValue) {

        // Tag-এর Boolean Value ফেরত দেয়
        return storage.getBoolean(tag, defaultValue);
    }


    // ==============================
    // Tag Delete
    // ==============================

    @SimpleFunction(
        description = "নির্দিষ্ট Tag-এর Data Delete করে"
    )
    public void RemoveData(String tag) {

        // Editor তৈরি
        SharedPreferences.Editor editor = storage.edit();

        // নির্দিষ্ট Tag Delete
        editor.remove(tag);

        // পরিবর্তন Save
        editor.apply();

        // Delete Event পাঠানো
        DataRemoved(tag);
    }


    // ==============================
    // Tag Check
    // ==============================

    @SimpleFunction(
        description = "Tag ফোনে আছে কিনা পরীক্ষা করে"
    )
    public boolean ContainsTag(String tag) {

        // Tag থাকলে True, না থাকলে False
        return storage.contains(tag);
    }


    // ==============================
    // সব Data Clear
    // ==============================

    @SimpleFunction(
        description = "এই Extension-এর সব সংরক্ষিত Data Delete করে"
    )
    public void ClearStorage() {

        // Editor তৈরি
        SharedPreferences.Editor editor = storage.edit();

        // সব Data মুছে ফেলা
        editor.clear();

        // পরিবর্তন Save
        editor.apply();
    }


    // ==============================
    // Data Saved Event
    // ==============================

    @SimpleEvent
    public void DataSaved(String tag) {

        // Kodular-এ DataSaved Event পাঠানো
        EventDispatcher.dispatchEvent(
                this,
                "DataSaved",
                tag
        );
    }


    // ==============================
    // Data Removed Event
    // ==============================

    @SimpleEvent
    public void DataRemoved(String tag) {

        // Kodular-এ DataRemoved Event পাঠানো
        EventDispatcher.dispatchEvent(
                this,
                "DataRemoved",
                tag
        );
    }

}

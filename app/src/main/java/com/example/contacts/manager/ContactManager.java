package com.example.contacts.manager;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.example.contacts.model.Contact;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ContactManager {
    private Callback mCallback;

    public interface Callback {
        void onContactsGot(List<Contact> contacts);
    }

    private List<Contact> mContactList = new ArrayList<>();

    static class Holder {
        static ContactManager sManager = new ContactManager();
    }

    public static ContactManager getInstance() {
        return Holder.sManager;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void removeCallback() {
        mCallback = null;
    }

    public void getContacts(Context context, boolean refresh) {
        // when not required to refresh or there are some data, we don't do the load
        if (refresh || mContactList.isEmpty()) {
            new LoadTask(context, "contacts.json", mContactList, mCallback).execute();
        } else {
            if (mCallback != null) {
                mCallback.onContactsGot(mContactList);
            }
        }
    }

    static class LoadTask extends AsyncTask<Object, Void, List<Contact>> {

        private final WeakReference<Context> mWRContext;
        private final String mFileName;
        private final WeakReference<List<Contact>> mWRList;
        private final WeakReference<Callback> mWRCallback;

        LoadTask(Context context, String fileName, List<Contact> list, Callback callback) {
            mWRContext = new WeakReference<>(context);
            mWRList = new WeakReference<>(list);
            mWRCallback = new WeakReference<>(callback);
            mFileName = fileName;
        }

        @Override
        protected List<Contact> doInBackground(Object... params) {
            List<Contact> list = new ArrayList<>();
            Context context = mWRContext.get();
            if (context == null) {
                return null;
            }
            if (TextUtils.isEmpty(mFileName)) {
                return null;
            }
            AssetManager am = context.getAssets();
            try {
                InputStream is = am.open(mFileName);
                int len = is.available();
                byte[] buffer = new byte[len];
                is.read(buffer);
                String result = new String(buffer, "utf8");
                if (!TextUtils.isEmpty(result)) {
                    try {
                        JSONArray array = new JSONArray(result);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject json = array.getJSONObject(i);
                            Contact contact = new Contact();
                            contact.setFirstName(json.optString("first_name"));
                            contact.setLastName(json.optString("last_name"));
                            contact.setTitle(json.optString("title"));
                            contact.setIntroduction(json.optString("introduction"));
                            contact.setAvatarFilename(json.optString("avatar_filename"));
                            list.add(contact);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<Contact> list) {
            super.onPostExecute(list);
            List<Contact> contactList = mWRList.get();
            if (contactList != null) {
                contactList.clear();
                contactList.addAll(list);
            }
            if (mWRCallback.get() != null) {
                mWRCallback.get().onContactsGot(list);
            }
        }
    }
}

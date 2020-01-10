package com.devkotasagar.listMe.test.data;

import android.provider.BaseColumns;

public class ListContract {

    private ListContract(){}

    public static final class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "user";
        public static final String COLUMN_USER_ID = BaseColumns._ID;
        public static final String COLUMN_USER_IDD = "idd"; // We need to store the id from the API too so
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_phone = "phone";
        public static final String COLUMN_ADDRESS_STREET = "street";
        public static final String COLUMN_ADDRESS_ZIP = "zip";
    }
}

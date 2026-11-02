package com.example.firstaidhero;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private EditText etContactName, etContactPhone;
    private Button btnAddContact, btnCallAmbulance, btnCallFire, btnCallPolice;
    private ListView lvCustomContacts;
    private ArrayList<String> contactsDisplayList;
    private ArrayList<String> phoneNumbersList;
    private ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        dbHelper = new DatabaseHelper(this);

        etContactName = findViewById(R.id.etContactName);
        etContactPhone = findViewById(R.id.etContactPhone);
        btnAddContact = findViewById(R.id.btnAddContact);
        lvCustomContacts = findViewById(R.id.lvCustomContacts);

        btnCallAmbulance = findViewById(R.id.btnCallAmbulance);
        btnCallFire = findViewById(R.id.btnCallFire);
        btnCallPolice = findViewById(R.id.btnCallPolice);

        contactsDisplayList = new ArrayList<>();
        phoneNumbersList = new ArrayList<>();

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contactsDisplayList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.parseColor("#111111"));
                tv.setTextSize(15);
                tv.setPadding(12, 16, 12, 16);
                return tv;
            }
        };
        lvCustomContacts.setAdapter(adapter);


        loadCustomContacts();


        btnCallAmbulance.setOnClickListener(v -> dialNumber("110"));
        btnCallFire.setOnClickListener(v -> dialNumber("113"));
        btnCallPolice.setOnClickListener(v -> dialNumber("112"));


        btnAddContact.setOnClickListener(v -> {
            String name = etContactName.getText().toString().trim();
            String phone = etContactPhone.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "يرجى إدخال الاسم ورقم الهاتف", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean inserted = dbHelper.addCustomContact(name, phone);
            if (inserted) {
                Toast.makeText(this, "تم حفظ جهة الاتصال بنجاح", Toast.LENGTH_SHORT).show();
                etContactName.setText("");
                etContactPhone.setText("");
                loadCustomContacts();
            } else {
                Toast.makeText(this, "حدث خطأ أثناء الحفظ", Toast.LENGTH_SHORT).show();
            }
        });
        lvCustomContacts.setOnItemClickListener((parent, view, position, id) -> {
            String phone = phoneNumbersList.get(position);
            dialNumber(phone);
        });
    }

    private void dialNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    private void loadCustomContacts() {
        contactsDisplayList.clear();
        phoneNumbersList.clear();

        Cursor cursor = dbHelper.getAllCustomContacts();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("contact_name"));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone_number"));

                contactsDisplayList.add("📞 " + name + " : " + phone);
                phoneNumbersList.add(phone);
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }
}
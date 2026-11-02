package com.example.firstaidhero;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
public class MainActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private EditText etSearch;
    private ListView lvEmergencies;
    private Button btnNavContacts, btnNavHistory;
    static class CaseItem {
        int id;
        String title;
        String danger;

        CaseItem(int id, String title, String danger) {
            this.id = id;
            this.title = title;
            this.danger = danger;
        }
    }
    private ArrayList<CaseItem> casesList;
    private EmergencyAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etSearch = findViewById(R.id.etSearch);
        lvEmergencies = findViewById(R.id.lvEmergencies);
        btnNavContacts = findViewById(R.id.btnNavContacts);
        btnNavHistory = findViewById(R.id.btnNavHistory);
        casesList = new ArrayList<>();
        adapter = new EmergencyAdapter();
        lvEmergencies.setAdapter(adapter);
        dbHelper = new DatabaseHelper(this);
        loadCases("");
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadCases(s.toString().trim());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        lvEmergencies.setOnItemClickListener((parent, view, position, id) -> {
            CaseItem selectedCase = casesList.get(position);
            Intent intent = new Intent(MainActivity.this, CaseDetailActivity.class);
            intent.putExtra("case_id", selectedCase.id);
            startActivity(intent);
        });
        btnNavContacts.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
            startActivity(intent);
        });
        btnNavHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
    }
    private void loadCases(String query) {
        casesList.clear();
        Cursor cursor;
        if (query.isEmpty()) {
            cursor = dbHelper.getAllEmergencies();
        } else {
            cursor = dbHelper.searchEmergencies(query);
        }
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int caseId = cursor.getInt(cursor.getColumnIndexOrThrow("case_id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String danger = cursor.getString(cursor.getColumnIndexOrThrow("danger_level"));
                casesList.add(new CaseItem(caseId, title, danger));
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }
    private class EmergencyAdapter extends BaseAdapter {
        @Override
        public int getCount() { return casesList.size(); }
        @Override
        public Object getItem(int position) { return casesList.get(position); }
        @Override
        public long getItemId(int position) { return casesList.get(position).id; }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_emergency_case, parent, false);
            }
            TextView tvTitle = convertView.findViewById(R.id.tvCaseTitleItem);
            TextView tvDanger = convertView.findViewById(R.id.tvCaseDangerItem);
            CaseItem item = casesList.get(position);
            tvTitle.setText(item.title);
            tvDanger.setText(item.danger);
            return convertView;
        }
    }
}
package com.example.firstaidhero;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView tvCriticalCount, tvMediumCount, tvTreatedTotalCount, tvImageStatus;
    private EditText etLogDescription, etLogProcedures;
    private Button btnPickImage, btnSaveTreatedCase;
    private ImageView ivCasePreview;
    private ListView lvTreatedCases;

    private String selectedImageUri = "";
    private ArrayList<String> treatedList;
    private ArrayAdapter<String> adapter;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri.toString();
                    ivCasePreview.setImageURI(uri);
                    tvImageStatus.setText("تم اختيار صورة");
                    tvImageStatus.setTextColor(Color.parseColor("#2E7D32"));
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        dbHelper = new DatabaseHelper(this);

        tvCriticalCount = findViewById(R.id.tvCriticalCount);
        tvMediumCount = findViewById(R.id.tvMediumCount);
        tvTreatedTotalCount = findViewById(R.id.tvTreatedTotalCount);
        tvImageStatus = findViewById(R.id.tvImageStatus);

        etLogDescription = findViewById(R.id.etLogDescription);
        etLogProcedures = findViewById(R.id.etLogProcedures);
        btnPickImage = findViewById(R.id.btnPickImage);
        btnSaveTreatedCase = findViewById(R.id.btnSaveTreatedCase);
        ivCasePreview = findViewById(R.id.ivCasePreview);
        lvTreatedCases = findViewById(R.id.lvTreatedCases);

        treatedList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, treatedList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.parseColor("#111111"));
                tv.setTextSize(14);
                tv.setLineSpacing(1.1f, 1.1f);
                tv.setPadding(10, 14, 10, 14);
                return tv;
            }
        };
        lvTreatedCases.setAdapter(adapter);
        btnPickImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnSaveTreatedCase.setOnClickListener(v -> {
            String desc = etLogDescription.getText().toString().trim();
            String proc = etLogProcedures.getText().toString().trim();
            String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

            if (desc.isEmpty() || proc.isEmpty()) {
                Toast.makeText(this, "يرجى تعبئة وصف الحالة والإجراءات", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean inserted = dbHelper.addTreatedCase(desc, currentDate, selectedImageUri, proc);
            if (inserted) {
                Toast.makeText(this, "تم حفظ الحالة في السجل بنجاح", Toast.LENGTH_SHORT).show();
                etLogDescription.setText("");
                etLogProcedures.setText("");
                selectedImageUri = "";
                ivCasePreview.setImageURI(null);
                tvImageStatus.setText("لم يتم تحديد صورة");
                tvImageStatus.setTextColor(Color.parseColor("#888888"));

                loadDashboardData();
            } else {
                Toast.makeText(this, "حدث خطأ أثناء الحفظ", Toast.LENGTH_SHORT).show();
            }
        });

        loadDashboardData();
    }
    private void loadDashboardData() {
        try {

            tvCriticalCount.setText(String.valueOf(dbHelper.getCaseCountByDanger("حرج")));
            tvMediumCount.setText(String.valueOf(dbHelper.getCaseCountByDanger("متوسط")));
            tvTreatedTotalCount.setText(String.valueOf(dbHelper.getTotalTreatedCasesCount()));
            Cursor cursor = dbHelper.getAllTreatedCases();
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {

                        int descIdx = cursor.getColumnIndex("description");
                        if (descIdx == -1) descIdx = cursor.getColumnIndex("Description");

                        int dateIdx = cursor.getColumnIndex("date");
                        if (dateIdx == -1) dateIdx = cursor.getColumnIndex("Date");

                        int procIdx = cursor.getColumnIndex("procedures");
                        if (procIdx == -1) procIdx = cursor.getColumnIndex("Procedures");

                        String desc = (descIdx != -1) ? cursor.getString(descIdx) : "";
                        String date = (dateIdx != -1) ? cursor.getString(dateIdx) : "";
                        String proc = (procIdx != -1) ? cursor.getString(procIdx) : "";

                        treatedList.add("📅 " + date + "\nالحالة: " + desc + "\nالإجراء: " + proc);
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "خطأ في قراءة السجل: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
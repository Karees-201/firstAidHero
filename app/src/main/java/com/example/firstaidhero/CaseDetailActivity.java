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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class CaseDetailActivity extends AppCompatActivity {

    private TextView tvDetailTitle, tvDetailDanger, tvDetailSymptoms;
    private ListView lvSteps;
    private Button btnQuickCall;
    private DatabaseHelper dbHelper;
    private ArrayList<String> stepsList;
    private ArrayAdapter<String> stepsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_detail);

        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailDanger = findViewById(R.id.tvDetailDanger);
        tvDetailSymptoms = findViewById(R.id.tvDetailSymptoms);
        lvSteps = findViewById(R.id.lvSteps);
        btnQuickCall = findViewById(R.id.btnQuickCall);

        dbHelper = new DatabaseHelper(this);
        stepsList = new ArrayList<>();


        stepsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stepsList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.parseColor("#111111"));
                tv.setTextSize(15);
                tv.setLineSpacing(1.2f, 1.2f);
                tv.setPadding(12, 16, 12, 16);
                return tv;
            }
        };
        lvSteps.setAdapter(stepsAdapter);


        int caseId = getIntent().getIntExtra("case_id", -1);

        if (caseId != -1) {
            loadCaseData(caseId);
        } else {
            Toast.makeText(this, "تعذر تحميل بيانات الحالة", Toast.LENGTH_SHORT).show();
        }
        btnQuickCall.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:110"));
            startActivity(callIntent);
        });
    }
    private void loadCaseData(int caseId) {
        Cursor cursor = dbHelper.getEmergencyById(caseId);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String danger = cursor.getString(cursor.getColumnIndexOrThrow("danger_level"));
                String symptoms = cursor.getString(cursor.getColumnIndexOrThrow("symptoms"));

                tvDetailTitle.setText(title);
                tvDetailDanger.setText("مستوى الخطورة: " + danger);
                tvDetailSymptoms.setText(symptoms);
            }
            cursor.close();
        }
        stepsList.clear();
        Cursor stepsCursor = dbHelper.getStepsByCaseId(caseId);
        if (stepsCursor != null) {
            if (stepsCursor.moveToFirst()) {
                do {
                    int stepNum = stepsCursor.getInt(stepsCursor.getColumnIndexOrThrow("step_number"));
                    String instruction = stepsCursor.getString(stepsCursor.getColumnIndexOrThrow("instruction_text"));
                    stepsList.add(stepNum + ". " + instruction);
                } while (stepsCursor.moveToNext());
            }
            stepsCursor.close();
        }
        stepsAdapter.notifyDataSetChanged();
    }
}
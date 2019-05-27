package com.example.simulacion2019;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LineChart chart = findViewById(R.id.chart);

       final GenPseudAleat rnd = new GenPseudAleat();



       rnd.generateAsync();

        findViewById(R.id.button).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                for(int i=0;i<1000;i++){
                    rnd.testDn(1000);
                }

                return true;
            }
        });

        findViewById(R.id.button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {


                LineChart chart = findViewById(R.id.chart);

                List<Entry> entries = new ArrayList<>();
                for (int i = 0; i < 10000; i=i+100) {
                    // turn your data into Entry objects
                    entries.add(new Entry(i, (rnd.get().floatValue())));
                }

                LineDataSet dataSet = new LineDataSet(entries, "generados"); // add entries to dataset
                dataSet.setColor(Color.RED);
                dataSet.setValueTextColor(Color.RED); // styling, ...

                List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                dataSets.add(dataSet);
                LineData lineDataValue = new LineData(dataSets);

                chart.setData(lineDataValue);

                chart.invalidate(); // refresh

            }
        });


        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            // turn your data into Entry objects
            entries.add(new Entry(2019+i, i));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Polución [PPM]"); // add entries to dataset
        dataSet.setColor(Color.RED);
        dataSet.setValueTextColor(Color.RED); // styling, ...

        List<Entry> maxValue = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            // turn your data into Entry objects
            maxValue.add(new Entry(2019+i, 15));
        }

        LineDataSet dataSetValue = new LineDataSet(maxValue, "Maximo permitido [PPM]"); // add entries to dataset
        dataSetValue.setColor(Color.GREEN);
        dataSetValue.setValueTextColor(Color.GREEN); // styling, ...

        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(dataSet);
        dataSets.add(dataSetValue);

        LineData lineDataValue = new LineData(dataSets);

        chart.setData(lineDataValue);

        chart.invalidate(); // refresh


    }
}

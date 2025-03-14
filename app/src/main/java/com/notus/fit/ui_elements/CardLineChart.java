package com.notus.fit.ui_elements;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.notus.fit.BuildConfig;
import com.notus.fit.R;
import com.notus.fit.lineChart.LineChartData;
import com.notus.fit.models.WeekReport;
import com.notus.fit.utils.Devices;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CardLineChart extends com.notus.fit.ui_elements.CardWithChart {

    @Bind(R.id.chart)
    LineChart lineChart;
    @Bind(R.id.week_average)
    TextView weekAverage;
    private LineChartData lineChartData;

    public CardLineChart(Context context, LineChartData lineChartData) {
        super(context, R.layout.card_line_all);
        this.lineChartData = lineChartData;
    }

    public CardLineChart(Context context) {
        super(context, R.layout.card_line_all);
    }

    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);
        ButterKnife.bind(this, view);
        List dataSets = new ArrayList();
        this.lineChart.setDrawGridBackground(false);
        this.lineChart.getAxisRight().setEnabled(false);
        this.lineChart.getAxisLeft().setDrawGridLines(false);
        this.lineChart.getXAxis().setDrawGridLines(false);
        this.lineChart.setDescription(BuildConfig.FLAVOR);
        Iterator it = this.lineChartData.getWeekReports().iterator();
        while (it.hasNext()) {
            WeekReport w = (WeekReport) it.next();
            List<Entry> values = new ArrayList<>();
            if (w.getStepList() != null) {
                int i;
                List<Integer> steps = w.getStepList();
                for (i = 0; i < steps.size(); i++) {
                    values.add(new Entry((float) steps.get(i), i));
                }
                if (steps.size() == 0) {
                    for (i = 0; i < 7; i++) {
                        steps.add(0);
                    }
                }
                LineDataSet d = new LineDataSet(values, w.getDevice());
                try {
                    switch (w.getDevice()) {
                        case Devices.GOOGLE_FIT:
                            d.setColor(getContext().getResources().getColor(R.color.red_400));
                            break;
                        case Devices.FITBIT:
                            d.setColor(getContext().getResources().getColor(R.color.teal_400));
                            break;
                        case Devices.JAWBONE:
                            d.setColor(getContext().getResources().getColor(R.color.blue_500));
                            break;
                        case Devices.MISFIT:
                            d.setColor(getContext().getResources().getColor(R.color.grey_black_1000));
                            break;
                        case Devices.MOVES:
                            d.setColor(getContext().getResources().getColor(R.color.green_400));
                            break;
                        default:
                            d.setColor(getContext().getResources().getColor(R.color.purple_700));
                            break;
                    }
                    d.setLineWidth(2.0f);
                    d.setCircleSize(3.0f);
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                }
                dataSets.add(d);
            }
        }
        LineData lineData = new LineData(WeekReport.getWeekLabels(), dataSets);
        lineData.setDrawValues(false);
        this.lineChart.setData(lineData);
        this.lineChart.invalidate();
        this.lineChart.animateXY(2000, 2000);
        this.weekAverage.setText(this.lineChartData.getBottomText());
    }

    public Chart getChart() {
        return this.lineChart;
    }
}
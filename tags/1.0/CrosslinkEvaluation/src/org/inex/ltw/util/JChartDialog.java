package org.inex.ltw.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class JChartDialog {

    private Vector<Object[]> plotDataSet = null;
    private String plotTitle = "";

    public JChartDialog(java.awt.Frame parent, boolean modal,
            String pTitle, Vector<Object[]> dataSet) {

        plotTitle = pTitle;
        plotDataSet = dataSet;

        showPlotReport(plotTitle, plotDataSet);

    }

//    public static void main(String[] args) {
//    }
    private void showPlotReport(String pTitle, Vector<Object[]> dataSet) {

        String chartTitle = pTitle;
        String xAxiaLabel = "Recall";
        String yAxiaLabel = "Interpolated-Precision";

        XYDataset xyData = populateDataSet(dataSet);
        JFreeChart jfc = ChartFactory.createXYLineChart(chartTitle, xAxiaLabel, yAxiaLabel,
                xyData, PlotOrientation.VERTICAL, true, true, false);

        XYPlot pp = (XYPlot) jfc.getPlot();
        pp.setBackgroundPaint(Color.WHITE);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        for (int i = 0; i < dataSet.size(); i++) {
            renderer.setSeriesShapesVisible(i, true);

            Object[] dataObj = dataSet.get(i);
            Object[] ct = (Object[]) dataObj[1];
            Paint painter = (Color) ct[0];
            Boolean isThick = (Boolean) ct[1];

            renderer.setSeriesPaint(i, painter);
            if (isThick) {
                renderer.setSeriesStroke(0, new BasicStroke(2f, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_BEVEL, 0.5f, new float[]{0.5f, 0.5f, 0.5f, 0.5f}, 0.5f));
            } else {
                renderer.setSeriesStroke(0, new BasicStroke(0.5f, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_BEVEL, 0.5f, new float[]{0.5f, 0.5f, 0.5f, 0.5f}, 0.5f));
            }
        }
        pp.setRenderer(renderer);

        // display a report in JPanel
//        JPanel reportPanel = new ChartPanel(jfc);
        JPanel reportPanel = new toolTipChartPanel(jfc);
        // place the report JPane in a scroll pane
        JScrollPane scrollPane = new JScrollPane(reportPanel);
        // display the scroll pane in a frame
        JFrame frame = new JFrame();
        frame.setTitle("Link-The-Wiki Evaluation Charts");
        frame.getContentPane().add(scrollPane);
        
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 500);
        frame.setVisible(true);

    }

    private XYDataset populateDataSet(Vector<Object[]> dataSet) {

        String[] xAxisLabels = {"0.05", "0.10", "0.15", "0.20", "0.25", "0.30", "0.35", "0.40", "0.45", "0.50",
            "0.55", "0.60", "0.65", "0.70", "0.75", "0.80", "0.85", "0.90", "0.95", "1.00"
        };

        XYSeriesCollection dataset = new XYSeriesCollection();
        for (int i = 0; i < dataSet.size(); i++) {
            Object[] dataObj = dataSet.get(i);
            // Get Data
            double[] data = (double[]) dataObj[0];
//            Object[] ct = (Object[]) dataObj[1];
//            Paint p = (Color) ct[0];
//            Boolean isThick = (Boolean) ct[1];
            String legendLabel = dataObj[2].toString();
            XYSeries xyPair = new XYSeries(legendLabel);
            for (int j = 0; j < data.length; j++) {
                xyPair.add(Double.valueOf(xAxisLabels[j]), (Double) data[j]);
            }
            dataset.addSeries(xyPair);
        }
        return dataset;
    }
}

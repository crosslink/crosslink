/*
 * JChartHistogram.java
 *
 */
package org.inex.ltw.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.jCharts.axisChart.AxisChart;
import org.jCharts.chartData.AxisChartDataSet;
import org.jCharts.chartData.ChartDataException;
import org.jCharts.chartData.DataSeries;
import org.jCharts.chartData.PieChartDataSet;
import org.jCharts.nonAxisChart.PieChart2D;
import org.jCharts.properties.AxisProperties;
import org.jCharts.properties.AxisTypeProperties;
import org.jCharts.properties.BarChartProperties;
import org.jCharts.properties.ChartProperties;
import org.jCharts.properties.ClusteredBarChartProperties;
import org.jCharts.properties.DataAxisProperties;
import org.jCharts.properties.LegendProperties;
import org.jCharts.properties.LineChartProperties;
import org.jCharts.properties.PieChart2DProperties;
import org.jCharts.properties.PointChartProperties;
import org.jCharts.properties.PropertyException;
import org.jCharts.properties.util.ChartStroke;
import org.jCharts.test.TestDataGenerator;
import org.jCharts.types.ChartType;
import org.jCharts.encoders.PNGEncoder;
/**
 * Created on 26 November 2007, 12:02
 * @author Darren Huang
 */
public class JChartHistogram extends JDialog {
    
    private BufferedImage image = null;
    private AxisChart doneChart = null;
//    private Vector<Object[]> histogramData = null;
    private String plotTitle = "";
    /** Creates a new instance of JChartHistogram */
    public JChartHistogram(java.awt.Dialog parent, boolean modal,
            String plotTitle) throws ChartDataException, PropertyException {    // Vector<Object[]> histogramData
        super(parent, modal);
        
        // initComponents();
//        this.histogramData = histogramData;
        this.plotTitle = plotTitle;
        paintChart();
        this.paintArea = new JPanel(){
            public void paintComponent(Graphics g){
                g.drawImage(JChartHistogram.this.image, 0, 0, this);
            }
        };
        
        this.paintArea.setDoubleBuffered(true);
        this.paintArea.setSize(780, 780);
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        this.getContentPane().add(this.paintArea);
        JPanel buttonPanel = new JPanel();
        JButton jbutton = new JButton("Save as Image");
        jbutton.addActionListener(
                new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OutputStream os = null;
                try{
                    String imageFile = "";
                    if (JChartHistogram.this.plotTitle.endsWith("Incoming")) {
                        imageFile = "IncomingIntePRPlot_" + JChartHistogram.this.getNow("MMddmmss") + ".png";
                    } else if (JChartHistogram.this.plotTitle.endsWith("Incoming")) {
                        imageFile = "OutgoingIntePRPlot_" + JChartHistogram.this.getNow("MMddmmss") + ".png";
                    } else if (JChartHistogram.this.plotTitle.endsWith("Combination")) {
                        imageFile = "CombinationIntePRPlot_" + JChartHistogram.this.getNow("MMddmmss") + ".png";
                    } else {
                        imageFile = "IntePRPlot_" + JChartHistogram.this.getNow("MMddmmss") + ".png";
                    }
                    os = new FileOutputStream(imageFile);
                    PNGEncoder.encode(JChartHistogram.this.doneChart, os);
                } catch(Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        os.close();
                    } catch (Exception e) {
                        
                    }
                }
            }
        }
        );
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        buttonPanel.setMaximumSize(new java.awt.Dimension(780, 30));
        buttonPanel.setMinimumSize(new java.awt.Dimension(780, 30));
        buttonPanel.setPreferredSize(new java.awt.Dimension(780, 30));
        buttonPanel.add(jbutton);
        this.getContentPane().add(buttonPanel);
        this.setSize(820, 820);
        this.setResizable(true);
    }
    
    private String getNow(String dateFormat) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(cal.getTime());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">
    private void initComponents() {
        paintArea = new javax.swing.JPanel();
        
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        javax.swing.GroupLayout paintAreaLayout = new javax.swing.GroupLayout(paintArea);
        paintArea.setLayout(paintAreaLayout);
        paintAreaLayout.setHorizontalGroup(
                paintAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 479, Short.MAX_VALUE)
                );
        paintAreaLayout.setVerticalGroup(
                paintAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 493, Short.MAX_VALUE)
                );
        
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(paintArea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(paintArea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                );
        pack();
    }// </editor-fold>
    
    private void paintChart() throws ChartDataException, PropertyException {
        
        String[] xAxisLabels = {"111.xml", "2341.xml", "651932.xml", "219856.xml", "3699.xml", "3019.xml", "3399.xml"};
        
        String xAxisTitle = "Topics";
        String yAxisTitle = "Differences of MAP";
        
        DataSeries dataSeries = new DataSeries(xAxisLabels, xAxisTitle, yAxisTitle, this.plotTitle);
        
        // Start from here
        double[][] data = new double[][]{{25, 45, -36, 66, -15, 80, 55 }, { 150, 15, 6, 62, -54, 10, 84 }, { 20, 145, 36, 6, 45, 18, 5 }};
        String[] legendLabels = {"Run012", "Run029", "Run031"};
        Paint[] paints = TestDataGenerator.getRandomPaints(3);
//        BarChartProperties barChartProperties = new BarChartProperties();
//        barChartProperties.setWidthPercentage(0.5f);
        ClusteredBarChartProperties clusterbarChartProperties = new ClusteredBarChartProperties();
        
        AxisChartDataSet axisChartDataSet = new AxisChartDataSet(data, legendLabels, paints, ChartType.BAR_CLUSTERED, clusterbarChartProperties);
        dataSeries.addIAxisPlotDataSet(axisChartDataSet);
        
        ChartProperties chartProperties = new ChartProperties();
        //---to make this plot horizontally, pass true to the AxisProperties Constructor
        AxisProperties axisProperties = new AxisProperties();
        
//        ChartStroke xAxisGridLines= new ChartStroke(new BasicStroke(1.0f), Color.ORANGE);
//        axisProperties.getXAxisProperties().setGridLineChartStroke(xAxisGridLines);
//        axisProperties.getXAxisProperties().setShowGridLines(AxisTypeProperties.GRID_LINES_ONLY_WITH_LABELS);
//        
//        DataAxisProperties dataAxisProperties= (DataAxisProperties)axisProperties.getYAxisProperties();
//        dataAxisProperties.setUserDefinedScale(0.00, 0.10);
//        dataAxisProperties.setRoundToNearest(-1);
//        dataAxisProperties.setNumItems(11);
        
        LegendProperties legendProperties = new LegendProperties();
//        legendProperties.setNumColumns(5);
        
        AxisChart axisChart = new AxisChart(dataSeries, chartProperties, axisProperties, legendProperties, 750, 750);
        
        BufferedImage bufferedImage = new BufferedImage(750, 750, BufferedImage.TYPE_INT_RGB);
        axisChart.setGraphics2D(bufferedImage.createGraphics());
        axisChart.render();
        this.doneChart = axisChart;
        this.image = bufferedImage;
    }
    
    // Variables declaration - do not modify
    private javax.swing.JPanel paintArea;
    // End of variables declaration
    
}

/**
 * Copyright 2016 Carnegie Mellon University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cmu.sv.modelinference.eventtool.charting;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleEdge;

/**
 * @author Kasper Luckow
 */
public class DataChart extends ApplicationFrame {

  private static final long serialVersionUID = 7777887151534005094L;

  private Crosshair xCrosshair;
  private Crosshair yCrosshair;
  private ChartPanel chartPanel;

  public DataChart(String title) {
    super(title);
  }
  
  public JFreeChart chart(String yLabel) {
    JFreeChart chart = createChart(yLabel);
    createChartPanel(chart);
    return chart;
  }

  private void createChartPanel(JFreeChart chart) {
    chartPanel = new ChartPanel(chart);

    chartPanel.addChartMouseListener(new ChartMouseListener() {
      
      @Override
      public void chartMouseClicked(ChartMouseEvent arg0) {
        //ignore
      }

      @Override
      public void chartMouseMoved(ChartMouseEvent event) {
        Rectangle2D dataArea = chartPanel.getScreenDataArea();
        JFreeChart chart = event.getChart();
        XYPlot plot = (XYPlot) chart.getPlot();
        ValueAxis xAxis = plot.getDomainAxis();
        double x = xAxis.java2DToValue(event.getTrigger().getX(), dataArea, 
            RectangleEdge.BOTTOM);
        ValueAxis yAxis = plot.getRangeAxis();
        double y = yAxis.java2DToValue(event.getTrigger().getY(), dataArea, 
            RectangleEdge.LEFT);

        //Alternatively, obtain y for one of the subplots, which would be very neat.
        //We should find the "nearest" subplot to the cursor -- this is easy
        //double y = DatasetUtilities.findYValue(plot.getDataset(), 0, x);
        xCrosshair.setValue(x);
        yCrosshair.setValue(y);
      }
    });

    CrosshairOverlay crosshairOverlay = new CrosshairOverlay();
    xCrosshair = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(0f));
    xCrosshair.setLabelVisible(true);
    yCrosshair = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(0f));
    yCrosshair.setLabelVisible(true);
    crosshairOverlay.addDomainCrosshair(xCrosshair);
    crosshairOverlay.addRangeCrosshair(yCrosshair);
    chartPanel.addOverlay(crosshairOverlay);

    chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
    setContentPane(chartPanel);
  }

  private JFreeChart createChart(String yLabel) {
    //Create the chart
    final JFreeChart chart = ChartFactory.createXYLineChart(
        "Chart",
        "Time",
        yLabel,
        null
        );

    chart.setBackgroundPaint(Color.white);

    XYPlot plot = chart.getXYPlot();
    plot.setBackgroundPaint(Color.lightGray);
    plot.setDomainGridlinePaint(Color.white);
    plot.setRangeGridlinePaint(Color.white);

    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    plot.setRenderer(renderer);

    // change the auto tick unit selection to integer units only...
    final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    return chart;
  }
}

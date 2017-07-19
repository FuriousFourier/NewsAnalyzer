package pl.edu.agh.Analyzer.ui;

import org.gephi.graph.api.Node;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.demo.charts.ExampleChart;
import org.knowm.xchart.demo.charts.bar.BarChart01;
import org.knowm.xchart.style.Styler;
import org.wouterspekkink.plugins.metric.lineage.Lineage;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by karolina on 18.07.17.
 */
public class ReportCreator implements ExampleChart<CategoryChart> {

    private List<ReportInput> reportInput;
    private String currentParam;
    private List<String> paramValues;
    private List<Number> values;
    private boolean isNodeAnalysis;
    String xAxis;

    public void showChart(List<ReportInput> input){
            reportInput = input;
            if (input == null || input.isEmpty()){
                System.out.println("Input is empty - returning...");
                return;
            }

            xAxis = reportInput.get(0).paramName;
            String fileName;
            if (!input.get(0).paramValue.equals(input.get(1).paramValue))
                fileName = input.get(0).paramName;
            else
                fileName = input.get(0).paramValue;
            //node params
        isNodeAnalysis = true;
            for (String p: ReportInput.nodesParams) {
                paramValues = new ArrayList<>();
                values = new ArrayList<>();
                currentParam = p;
                CategoryChart chart = this.getChart();
                if (chart == null) {
                    System.out.println("No chart to display for " + input.get(0).paramValue + ", param: " + p);
                    continue;
                }
                try {
                    BitmapEncoder.saveBitmap(chart, "./Sample_Chart_"+fileName+"_"+p, BitmapEncoder.BitmapFormat.PNG);
                    //new SwingWrapper<CategoryChart>(chart).displayChart();
                } catch (HeadlessException e){
                    System.out.println("HeadlessExcpetion has been thrown!");
                    continue;
                }catch (IOException e){
                    System.out.println("IOException has been thrown!");
                }
                /*try {
                    System.out.println("Press space to continue");
                    waitForSpace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            }

            //graph params
        isNodeAnalysis = false;
        for (String p: input.get(0).getGraphParams()) {
            paramValues = new ArrayList<>();
            values = new ArrayList<>();
            currentParam = p;
            CategoryChart chart = this.getChart();
            if (chart == null) {
                System.out.println("No chart to display for " + input.get(0).paramValue + ", param: " + p);
                continue;
            }
            try {
                BitmapEncoder.saveBitmap(chart, "./Sample_Chart_"+fileName+"_"+p, BitmapEncoder.BitmapFormat.PNG);
                //new SwingWrapper<CategoryChart>(chart).displayChart();
            } catch (HeadlessException e){
                System.out.println("HeadlessExcpetion has been thrown!");
                continue;
            }catch (IOException e){
                System.out.println("IOException has been thrown!");
            }
                /*try {
                    System.out.println("Press space to continue");
                    waitForSpace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
        }
        }

/*        private void waitForSpace() throws InterruptedException {
            final CountDownLatch latch = new CountDownLatch(1);
            KeyEventDispatcher dispatcher = new KeyEventDispatcher() {
                @Override
                public boolean dispatchKeyEvent(KeyEvent keyEvent) {
                    if (keyEvent.getKeyCode() == KeyEvent.VK_SPACE)
                        latch.countDown();
                    return false;
                }
            };
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
            latch.await();
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
        }*/

        @Override
        public CategoryChart getChart() {
        //iteracja po parametrach grafu/wezlow/itp.
            //iteracja po podgrafach
            System.out.println("================================getChart===============");
                for (ReportInput r: reportInput){
                    if (r == null)
                        continue;
                    paramValues.add(r.paramValue);
                    Node n;
                    Number number;
                    if (isNodeAnalysis) {
                        n = (Node) r.getNodeMaxValue(currentParam);
                        if (n == null)
                            number = 0;
                        else
                            number = (Number) n.getAttribute(currentParam);
                    }
                    else {
                        number = (Number) r.getGraphValue(currentParam);
                    }
                    values.add(number);
                }

            System.out.println("X Labels:");
            for (String s: paramValues){
                System.out.print(s + ", ");
            }
            System.out.println();
            System.out.println("Values:");
            for (Number d : values) {
                System.out.println(d + " ");
            }
            System.out.println();

            // Create Chart
            CategoryChart chart = new CategoryChartBuilder().width(800).height(600).title(currentParam).xAxisTitle(xAxis).yAxisTitle("Value").build();

            // Customize Chart
            chart.getStyler().setLegendVisible(false);
            chart.getStyler().setHasAnnotations(true);
            chart.getStyler().setXAxisLabelRotation(90);

            // Series
            chart.addSeries("test 1", paramValues, values);
            return chart;
        }

}

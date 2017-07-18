package pl.edu.agh.Analyzer.ui;

import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.demo.charts.ExampleChart;
import org.knowm.xchart.demo.charts.bar.BarChart01;
import org.knowm.xchart.style.Styler;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by karolina on 18.07.17.
 */
public class ReportCreator implements ExampleChart<CategoryChart> {

    private static List<ReportInput> reportInput;
    private static String currentParam;
    private static List<String> paramValues = new ArrayList<>();
    private static List<Double> values = new ArrayList<>();

    public static void showChart(List<ReportInput> input){
            reportInput = input;
            ExampleChart<CategoryChart> exampleChart = new BarChart01();
            for (String p: input.nodesParams) {
                currentParam = p;
                CategoryChart chart = exampleChart.getChart();
                if (chart == null) {
                    System.out.println("No chart to display");
                    return;
                }
                try {
                    new SwingWrapper<CategoryChart>(chart).displayChart();
                } catch (HeadlessException e){

                }
                try {
                    System.out.println("Press space to continue");
                    waitForSpace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private static void waitForSpace() throws InterruptedException {
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
        }

        @Override
        public CategoryChart getChart() {
            //iteracja po parametrach grafu/wezlow/itp.
            //iteracja po podgrafach

                for (ReportInput r: reportInput){
                    if (r == null)
                        continue;
                    paramValues.add(r.paramValue);
                    values.add((Double)r.getGraphValue(currentParam));
                }

            // Create Chart
            CategoryChart chart = new CategoryChartBuilder().width(1000).height(800).title(currentParam).xAxisTitle(reportInput.get(0).paramName).yAxisTitle("Value").build();

            // Customize Chart
            chart.getStyler().setLegendVisible(false);
            chart.getStyler().setHasAnnotations(true);

            // Series
            chart.addSeries("test 1", paramValues, values);

            return chart;
        }

}

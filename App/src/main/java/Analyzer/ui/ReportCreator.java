package Analyzer.ui;

import com.itextpdf.text.*;
import com.itextpdf.text.Image;
import org.gephi.graph.api.Node;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.demo.charts.ExampleChart;
import org.knowm.xchart.style.Styler;
import org.wouterspekkink.plugins.metric.lineage.Lineage;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	private String xAxis;

	public List<ReportInput> createReportInputList(List<String> paths){
		return new ArrayList<ReportInput>();
	}
	public void showChart(List<ReportInput> input, Document report){
		String chartName;
		if (input.get(0).date.equals(""))
			chartName = "Newspapers";
		else if (input.get(0).newspaper.equals(""))
			chartName = "Dates";
		else
			chartName = (input.get(0).newspaper); //TODO: informacja, jaka to jest data (dzienna, miesieczna, itd.)
		com.itextpdf.text.Rectangle rect = report.getPageSize();
		float margin = report.rightMargin() + report.leftMargin();
		reportInput = input;
		if (input == null || input.isEmpty()){
			System.out.println("Input is empty - returning...");
			return;
		}

		xAxis = chartName;

		//node params - currently not drawing
        /*isNodeAnalysis = true;
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
                    BitmapEncoder.saveBitmap(chart, "src/main/resources/charts/"+fileName+"_"+p, BitmapEncoder.BitmapFormat.PNG);
                    URI uri;
                    for (int i = 0; i < 100; i++) {
                        if (ClassLoader.getSystemResource(fileName + "_" + p + ".png") != null) {
                            uri = ClassLoader.getSystemResource(fileName+"_"+p+".png").toURI();
                            Path path = Paths.get(uri);
                            Image img = Image.getInstance(path.toAbsolutePath().toString());
                            img.scaleToFit(rect.getWidth()-margin, rect.getHeight());
                            report.add(img);
                            break;
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    return;
                }
            }
*/
		//graph params
		isNodeAnalysis = false;
		for (String p: input.get(0).getGraphParams()) {
			paramValues = new ArrayList<>();
			values = new ArrayList<>();
			currentParam = p;
			CategoryChart chart = this.getChart();
			if (chart == null) {
				System.out.println("No chart to display for " + input.get(0).newspaper + " " + input.get(0).date + ", param: " + p);
				continue;
			}
			try {
				BitmapEncoder.saveBitmap(chart, "src/main/resources/charts/"+chartName+"_"+p, BitmapEncoder.BitmapFormat.PNG);
				URI uri;
				System.out.println(ClassLoader.getSystemResource("charts/"+chartName + "_" + p + ".png"));
				//przerobic na pozyskiwanie charts bezposrednio z resources, a nie dopiero z target!
					if (ClassLoader.getSystemResource("charts/"+chartName + "_" + p + ".png") != null) {
						uri = ClassLoader.getSystemResource("charts/"+chartName+"_"+p+".png").toURI();
						Path path = Paths.get(uri);
						Image img = Image.getInstance(path.toAbsolutePath().toString());
						img.scaleToFit(rect.getWidth()-margin, rect.getHeight());
						report.add(img);
					}
			}catch (Exception e){
				e.printStackTrace();
				return;
			}
		}
	}

	@Override
	public CategoryChart getChart() {
		//iteracja po parametrach grafu/wezlow/itp.
		//iteracja po podgrafach
		System.out.println("================================getChart===============");
		for (ReportInput r: reportInput){
			if (r == null)
				continue;
			if (r.date.equals(""))
				paramValues.add(r.newspaper);
			else if (r.newspaper.equals(""))
				paramValues.add(r.date);
			else
				paramValues.add(r.newspaper+"("+r.date+")");
			Node n;
			Number number;
			if (isNodeAnalysis) {
				n = (Node) r.getNodeMaxValue(currentParam);
				if (n == null)
					number = 0;
				else
					number = (Number) n.getAttribute(currentParam);
			}
			else
				number = (Number) r.getGraphValue(currentParam);
			values.add(number);
		}

		System.out.println("X Labels:");
		for (String s: paramValues)
			System.out.print(s + ", ");
		System.out.println();
		System.out.println("Values:");
		for (Number d : values)
			System.out.println(d + " ");
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

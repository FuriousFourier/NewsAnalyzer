package Analyzer.ui;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.demo.charts.ExampleChart;
import org.knowm.xchart.style.Styler;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.List;

import static java.util.Collections.sort;

/**
 * Created by karolina on 18.07.17.
 */
public class ReportCreator implements ExampleChart<CategoryChart> {
	private String currentParam;
	private List<String> paramValues;
	private List<String> series;
	private Map<String, List<Number>> values;
	private boolean isNodeAnalysis;
	private String xAxis;
	private int nrOfSerieses;

	public void extractRelevantInputs(CSVReader reader,  CSVWriter writer, String date1, String date2, boolean initColumns) throws IOException {
		System.out.println("ExtractRelevantInputs");
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			if (nextLine[0].startsWith("Date") && initColumns) {
				writer.writeNext(nextLine);
				System.out.println(nextLine[0] + " - write...");
				continue;
			}

			if (nextLine[0].compareTo(date1) < 0 || nextLine[0].compareTo(date2) > 0){
				System.out.println(nextLine[0] + " - skipping...");
				continue;
			}

			System.out.println(nextLine[0] + nextLine[1]);
			writer.writeNext(nextLine);
		}
	}

	public Document createReportBase(String chartName) throws FileNotFoundException, DocumentException {
		Document report = new Document();
		PdfWriter.getInstance(report, new FileOutputStream("src/main/resources/reports/"+chartName+".pdf"));
		report.open();
		Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD,14,BaseColor.BLACK);
		Paragraph p = new Paragraph(chartName, titleFont);
		report.add(p);
		return report;
	}

	public class DataContainer implements Comparable{
		private String date;
		private Number value;
		private DataContainer(){
		}
		private DataContainer(String date, Number value){
			this.date = date;
			this.value = value;
		}

		@Override
		public int compareTo(Object o) {
			return date.compareTo( ((DataContainer)o).date );
		}
	}

	//ponizsze dziala tylko dla parametrow grafu z dwiema (ew. jedna) seriami danych
	public synchronized void showChart(String dataPath, int nrOfSerieses, Document report, String chartName) throws IOException, ParseException {
		//below analysis is for the params of the whole graph
		//to perform analysis for nodes, showChart need new boolean (if it's about nodes) and appropriate dataPath during invocation
		//moreover, currently there is no output for node analysis here
		//output would be rather a text, not dozen of charts
		File inputFile = new File(dataPath);
		CSVReader reader = new CSVReader(new FileReader(inputFile), '\t');
		String[] nextLine;
		HashMap<String, HashMap<String, List<DataContainer>>> data = new HashMap<>();
		String[] columnNames = null;
		while ((nextLine = reader.readNext()) != null) {
			if (columnNames == null && nextLine[0].equals("Date")){
				columnNames = nextLine;
				for (int j = 2; j < columnNames.length; j++)
					data.put(columnNames[j], new HashMap<>());
				continue;
			}
			for (int j = 2; j < columnNames.length; j++){
				if (nextLine[0].equals("Date"))
					continue;
				data.get(columnNames[j]).putIfAbsent(nextLine[1], new ArrayList<>());
				DataContainer container = new DataContainer(nextLine[0], NumberFormat.getInstance().parse(nextLine[j]));
				data.get(columnNames[j]).get(nextLine[1]).add(container);
			}

		}

		com.itextpdf.text.Rectangle rect = report.getPageSize();
		float margin = report.rightMargin() + report.leftMargin();
		if (data.isEmpty()){
			System.out.println("Input is empty - returning...");
			return;
		}

		xAxis = chartName;

		//graph params
		isNodeAnalysis = false; //for nodes it would be true
		for (String p: columnNames) { //for nodes it would be ReportInput.nodesParams
			if (p.equals("Date") || p.equals("Newspaper"))
				continue;
			currentParam = p;
			Set <String> paramValuesSet = new HashSet<>();
			values = new HashMap<>();
			series = new ArrayList<>();
			Map<String, List<DataContainer>> newspaperData = data.get(p);
			System.out.println("Param: " + p);
			int i = 0; //liczba gazet
			for(String n: newspaperData.keySet()){
				series.add(n);
				System.out.println("Newspaper: " + n);
				List<DataContainer> dateData = newspaperData.get(n);
				sort(dateData);
				for (DataContainer d : dateData){
					paramValuesSet.add(d.date);
					values.putIfAbsent(d.date, new ArrayList<>(nrOfSerieses));
					System.out.println("Nr of serieses; " + nrOfSerieses);
					System.out.println("list size: " + values.get(d.date).size());
					values.get(d.date).add(i, d.value);
					System.out.println(d.value + " added to series  " + i + " for " + d.date);
				}
				i++;
			}

			paramValues = new ArrayList<>(paramValuesSet);
			sort(paramValues);
			CategoryChart chart = this.getChart();
			if (chart == null) {
				System.out.println("No chart to display for " + series.get(0) + " etc. " + paramValues.get(0) + " etc., param: " + p);
				continue;
			}
			try {//cos nie jest dobrze inicjalizowane dla a>r>m, trzeba sprawdzic, co!
				BitmapEncoder.saveBitmap(chart, "target/classes/charts/"+chartName+"_"+p, BitmapEncoder.BitmapFormat.PNG);
				URI uri;
				System.out.println(ClassLoader.getSystemResource(""));
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
	public synchronized CategoryChart getChart() {
		//iteracja po parametrach grafu/wezlow/itp.
		//iteracja po podgrafach
		System.out.println("================================getChart===============");

		// Create Chart
		CategoryChart chart = new CategoryChartBuilder().width(800).height(600).title(currentParam).xAxisTitle(xAxis).yAxisTitle("Value").build();

		// Customize Chart
		chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideSE);
		chart.getStyler().setHasAnnotations(true);
		chart.getStyler().setXAxisLabelRotation(80);

		// Series

		for (int i = 0; i < nrOfSerieses; i++){
			List<Number> currentValues = new ArrayList<>();
			for (String date: paramValues){
				if (i >= values.get(date).size() || values.get(date).get(i) == null)
					currentValues.add(0);
				else
					currentValues.add(values.get(date).get(i));
			}
			chart.addSeries(series.get(i), paramValues, currentValues);
		}

		return chart;
	}

}

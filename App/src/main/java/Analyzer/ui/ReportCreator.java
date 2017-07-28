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
import java.math.BigDecimal;
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
	private List<String> labelValues;
	private List<String> series;
	private Map<String, List<Number>> values;
	private boolean isNodeAnalysis;
	private String xAxis;
	private int nrOfSerieses;

	public synchronized void extractRelevantInputs(CSVReader reader,  CSVWriter writer, String date1, String date2, boolean initColumns) throws IOException {
		System.out.println("ExtractRelevantInputs");
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			if (nextLine[0].startsWith("Date") && initColumns) {
				writer.writeNext(nextLine);
				System.out.println(nextLine[0] + " - write...");
				continue;
			}

			if (nextLine[0].compareTo(date1) < 0 || nextLine[0].compareTo(date2) > 0){
				//System.out.println(nextLine[0] + " - skipping...");
				continue;
			}

			//System.out.println(nextLine[0] + nextLine[1]);
			writer.writeNext(nextLine);
		}
	}

	public synchronized  Document createReportBase(String chartName) throws FileNotFoundException, DocumentException {
		Document report = new Document();
		PdfWriter.getInstance(report, new FileOutputStream("src/main/resources/reports/"+chartName+".pdf"));
		report.open();
		Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD,14,BaseColor.BLACK);
		Paragraph p = new Paragraph(chartName, titleFont);
		report.add(p);
		return report;
	}

	public class LabelComparator implements Comparator<DataContainer>{
		@Override
		public synchronized int compare(DataContainer t0, DataContainer t1) {
			return t0.date.compareTo( t1.date );
		}
	}
	public class ValueComparator implements Comparator<DataContainer>{
		@Override
		public synchronized  int compare(DataContainer t0, DataContainer t1) {
			return new BigDecimal(t0.value.toString()).compareTo(new BigDecimal(t1.value.toString()));
		}
	}
	public class DataContainer{
		private String date;
		private Number value;
		private DataContainer(){
		}
		private DataContainer(String date, Number value){
			this.date = date; //nie 'date', tylko 'label' - bo labelem moze tez byc tag
			this.value = value;
		}
	}


	//ponizsze dziala tylko dla parametrow grafu z dwiema (ew. jedna) seriami danych
	public synchronized void showChart(String dataPath, int nrOfSerieses, Document report, String chartName, boolean isNodeAnalysis) {
		//below analysis is for the params of the whole graph
		//to perform analysis for nodes, showChart need new boolean (if it's about nodes) and appropriate dataPath during invocation
		//moreover, currently there is no output for node analysis here
		//output would be rather a text, not dozen of charts
		try {
			LabelComparator labelComparator = new LabelComparator();
			ValueComparator valueComparator = new ValueComparator();

			this.nrOfSerieses = nrOfSerieses;
			File inputFile = new File(dataPath);
			CSVReader reader = null;
			try {
				reader = new CSVReader(new FileReader(inputFile), '\t');
			} catch (FileNotFoundException e) {
				System.out.println("EXCEPTION in showChart!!");
				e.printStackTrace();
			}
			String[] nextLine;
			Map<String, Map<String, List<DataContainer>>> data = new HashMap<>(); //do analizy grafu
			String[] columnNames = null;
			try {
				while ((nextLine = reader.readNext()) != null) {
					if (nextLine[0].equals("Date")) {
						if (columnNames == null) { //dla analizy wezlow - musze sprawic, aby najpierw wazniejsza gazeta byla uwzgledniona!
							columnNames = nextLine;
						}
						continue;
					}
					data.putIfAbsent(nextLine[2], new HashMap<>()); //zapisuje parametr
					data.get(nextLine[2]).putIfAbsent(nextLine[1], new ArrayList<>()); //tu moge juz dodac gazete, bo znam parametr
					//dla grafow - dodaje parametr i basta
					if (!isNodeAnalysis) {
						DataContainer container;
						if (nextLine[3].equals("NaN"))
							container = new DataContainer(nextLine[0], -0.1);
						else
							container = new DataContainer(nextLine[0], NumberFormat.getInstance().parse(nextLine[3]));
						data.get(nextLine[2]).get(nextLine[1]).add(container);
					} else {
						for (int j = 3; j < columnNames.length; j++) {
							DataContainer container = new DataContainer(columnNames[j], NumberFormat.getInstance().parse(nextLine[j]));
							data.get(nextLine[2]).get(nextLine[1]).add(container);
						}
					}
				}
			} catch (IOException e) {
				System.out.println("EXCEPTION IN showChart!!!");
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}

			com.itextpdf.text.Rectangle rect = report.getPageSize();
			float margin = report.rightMargin() + report.leftMargin();
			if (data.isEmpty()) {
				System.out.println("Input is empty - returning...");
				return;
			}

			xAxis = chartName;

			//graph params
			//isNodeAnalysis = false; //for nodes it would be true
			List<DataContainer> importantTags = new ArrayList<>();
			List<DataContainer> currentTags = new ArrayList<>();
			SortedSet<String> labelsSet = new TreeSet<>(data.keySet());
			for (String p : labelsSet) { //for nodes it would be ReportInput.nodesParams
				currentParam = p;
				Set<String> LabelsSet = new HashSet<>();
				values = new HashMap<>();
				series = new ArrayList<>();
				Map<String, List<DataContainer>> newspaperData = data.get(p);
				System.out.println("Param: " + p);
				int i = 0; //liczba gazet
				System.out.println("Nr of serieses; " + nrOfSerieses);
				for (String n : newspaperData.keySet()) {
					series.add(n);
					System.out.println("Newspaper: " + n);
					List<DataContainer> dateData = newspaperData.get(n);
					//jezeli jest wykres dla parametrow grafu, to tylko labelCOmparator
					//jezeli wykres dla parametrow wezlow, to za pierwszym razem valueCOmparator, a dla kolejnych trzeba sciagac reczenie (pomocnicza HashMap?)
					//ew. recznie inicjalizuje importantTags i wtedy tu wyciagam je dla kazdej gazety
					if (!isNodeAnalysis) {
						sort(dateData, labelComparator);
						currentTags = dateData;
					} else {
						if (i == 0 && importantTags.isEmpty()) {
							sort(dateData, valueComparator);
							for (int k = 0; k < 10; k++) { //sparametryzowac po liczbie tagow, w wywolaniu funkcji
								importantTags.add(dateData.get(k));
							}
							currentTags = dateData;
						} else {
							for (DataContainer d : importantTags) {
								int index = getTagIndex(dateData, d.date);
								if (index != -1)
									currentTags.add(dateData.get(index));
								else
									System.out.println("Tag " + d.date + " hasn't been found in dateData!!!");
							}
						}
					}
					for (DataContainer d : currentTags) {
						LabelsSet.add(d.date);
						values.putIfAbsent(d.date, new ArrayList<>(nrOfSerieses));
						System.out.println("list size: " + values.get(d.date).size());
						while (values.get(d.date).size() < i)
							values.get(d.date).add(0); //naprawienie IndexOutOfBoundsException
						values.get(d.date).add(i, d.value);
						System.out.println(d.value + " added to series  " + i + " for " + d.date);
					}
					i++;
				}

				labelValues = new ArrayList<>(LabelsSet);
				sort(labelValues);
				CategoryChart chart = this.getChart();
				if (chart == null) {
					System.out.println("No chart to display for " + series.get(0) + " etc. " + labelValues.get(0) + " etc., param: " + p);
					continue;
				}
				try {
					BitmapEncoder.saveBitmap(chart, "target/classes/charts/" + chartName + "_" + p, BitmapEncoder.BitmapFormat.PNG);
					URI uri;
					System.out.println(ClassLoader.getSystemResource(""));
					if (ClassLoader.getSystemResource("charts/" + chartName + "_" + p + ".png") != null) {
						uri = ClassLoader.getSystemResource("charts/" + chartName + "_" + p + ".png").toURI();
						Path path = Paths.get(uri);
						Image img = Image.getInstance(path.toAbsolutePath().toString());
						img.scaleToFit(rect.getWidth() - margin, rect.getHeight());
						report.add(img);
					}
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
		}catch (Exception E){
			System.out.println("DZIWNY EXCEPTION!");
			E.printStackTrace();
		}
	}

	private synchronized int getTagIndex(List<DataContainer> list, String tag){
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).date.equals(tag))
				return i;
		}
		return -1;
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
			for (String date: labelValues){
				if (i >= values.get(date).size() || values.get(date).get(i) == null)
					currentValues.add(-0.1);
				else
					currentValues.add(values.get(date).get(i));
			}
			chart.addSeries(series.get(i), labelValues, currentValues);
			System.out.println("Series name: " + series.get(i));
			System.out.println("currentValues size: " + currentValues.size());
			for (Number n: currentValues)
				System.out.print(n+"\t");
			System.out.println();
			for (String date: labelValues)
				System.out.print(date + "\t");
			System.out.println();
		}

		return chart;
	}

}

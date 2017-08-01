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
	private String xAxis, colName;
	private int nrOfSerieses;
	private Map<String, List<Integer>> importantTagsList;//liczby wskazuja, ktory to tag jest z kolei

	public synchronized void extractRelevantInputs(CSVReader reader,  CSVWriter writer, String date1, String date2, boolean initColumns, boolean useImportantTags) throws IOException {
		System.out.println("ExtractRelevantInputs");
		String[] nextLine;
		List<Integer> tagsNums;
		String[] nextTopLine = null;
		String[] columnLine = null;
		if(useImportantTags) {
			tagsNums = importantTagsList.get("hub"); //moze byc dowolne inne istniejace pole
			nextTopLine = new String[tagsNums.size() + 3];
		}
		while ((nextLine = reader.readNext()) != null) {
			if (nextLine[0].startsWith("Date") && initColumns) {
				columnLine = nextLine;
				if (useImportantTags){
					continue;
				}
				else {
					writer.writeNext(nextLine);
					System.out.println(nextLine[0] + " - write...");
					continue;
				}
			}

			if (!nextLine[0].equals("Date") &&(nextLine[0].compareTo(date1) < 0 || nextLine[0].compareTo(date2) > 0)){
				//System.out.println(nextLine[0] + " - skipping...");
				continue;
			}

			//System.out.println(nextLine[0] + nextLine[1]);
			//jesli useImportantTags==true, utworz nowa tablice o dlugosci rownej liczbie tagow + 3 (chyba)
			//iteruj po nextLine i wyekstrahuj, co trzeba
			if (useImportantTags){
				//napierw pisze wiersz z kolumnami
				nextTopLine[0] = columnLine[0];
				nextTopLine[1] = columnLine[1];
				nextTopLine[2] = columnLine[2];
				tagsNums = importantTagsList.get(nextLine[2]);
				for (int i = 0; i < tagsNums.size(); i++){
					nextTopLine[3+i] = columnLine[3+ tagsNums.get(i)];
				}
				writer.writeNext(nextTopLine);

				nextTopLine[0] = nextLine[0];
				nextTopLine[1] = nextLine[1];
				nextTopLine[2] = nextLine[2];
				for (int i = 0; i < tagsNums.size(); i++){
					nextTopLine[3+i] = nextLine[3+ tagsNums.get(i)];
				}
				writer.writeNext(nextTopLine);
			}
			else {
				writer.writeNext(nextLine);
			}
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
	public synchronized void showChart(String dataPath, int nrOfSerieses, Document report, String chartName, int nrOfTopTags, int colNr, boolean isNodeAnalysis, boolean getTop, boolean willCreateReport) {
		try {
			LabelComparator labelComparator = new LabelComparator();
			ValueComparator valueComparator = new ValueComparator();

			this.nrOfSerieses = nrOfSerieses;
			File inputFile = new File(dataPath);
			CSVReader reader = null;
			reader = new CSVReader(new FileReader(inputFile), '\t');
			String[] nextLine;
			Map<String, Map<String, List<DataContainer>>> data = new HashMap<>(); //do analizy grafu
			String[] columnNames = null;
			while ((nextLine = reader.readNext()) != null) {
				if (nextLine[0].equals("Date")) {
					columnNames = nextLine; //mam nadzieje, ze wykomentowanie warunku nie pospuje niczego
					continue;
				}
				try {
					data.putIfAbsent(nextLine[2], new HashMap<>()); //zapisuje parametr
				}catch (Exception e){
					System.out.println("Wypis felernej linijki (rozmiar: " + nextLine.length);
					for (String s: nextLine)
						System.out.print(s);
					System.out.println();
				}

				data.get(nextLine[2]).putIfAbsent(nextLine[1], new ArrayList<>()); //tu moge juz dodac gazete, bo znam parametr
				//dla grafow - dodaje parametr i basta
				if (!getTop) {
					if (nextLine[colNr].equals("NaN"))
						nextLine[colNr] = "-0.1";
					DataContainer container;
					container = new DataContainer(nextLine[0], NumberFormat.getInstance(Locale.ENGLISH).parse(nextLine[colNr]));
					colName = columnNames[colNr];
					data.get(nextLine[2]).get(nextLine[1]).add(container);
				} else {
					for (int j = 3; j < columnNames.length; j++) {
						DataContainer container = null;
						try {
							container = new DataContainer(columnNames[j], NumberFormat.getInstance(Locale.ENGLISH).parse(nextLine[j]));
						} catch (Exception e){
							System.out.println("columns names["+j+"]: " + columnNames[j]);
							System.out.println("Felerna linijka: " + nextLine[0] + " " +nextLine[1] + " " + nextLine[2]+" "+nextLine[j]);
							continue;
						}
						data.get(nextLine[2]).get(nextLine[1]).add(container);
					}
				}
			}
			com.itextpdf.text.Rectangle rect = null;
			float margin = 0;
			if (willCreateReport) {
				rect = report.getPageSize();
				margin = report.rightMargin() + report.leftMargin();
			}
			if (data.isEmpty()) {
				System.out.println("Input is empty - returning...");
				return;
			}
			xAxis = chartName;
			//graph params
			List<String> importantTagsNames;
			SortedSet<String> labelsSet = new TreeSet<>(data.keySet());
			//give TOP nrOfTopTags to output .csv files

			String topFileName = "src/main/resources/csv/"+chartName+"_TOP.csv";
			File topFile;
			CSVWriter topWriter = null;
			if (getTop){
				topFile = new File(topFileName);
				topFile.delete();
				topFile.createNewFile();
				topWriter = new CSVWriter(new FileWriter(topFileName, true), '\t', CSVWriter.NO_QUOTE_CHARACTER);
			}

			String[] textForTop = new String[3 + 2 * nrOfTopTags];
			if (getTop) {
				textForTop[0] = "Date";
				textForTop[1] = "Newspaper";
				textForTop[2] = "Param name";
				for (int i = 0; i < nrOfTopTags; i += 2) {
					textForTop[3 + i] = (i / 2 + 1) + "-tag";
					textForTop[3 + i + 1] = "Tag rank";
				}
				if (topWriter != null)
					topWriter.writeNext(textForTop);

				textForTop[0] = chartName;
			}
			importantTagsList = new HashMap<>();
			for (String p : labelsSet) { //for nodes it would be ReportInput.nodesParams
				importantTagsNames = new ArrayList<>();
				importantTagsList.put(p, new ArrayList<>());
				textForTop[2] = p;
				currentParam = p; //dla korelacji dla wartosci wezlow  - bedzie tu jescze nazwa tagu
				Set<String> LabelsSet = new HashSet<>();
				values = new HashMap<>();
				series = new ArrayList<>();
				Map<String, List<DataContainer>> newspaperData = data.get(p);
				System.out.println("Param: " + p);
				int i = 0; //liczba gazet
				System.out.println("Nr of serieses; " + nrOfSerieses);
				for (String n : newspaperData.keySet()) {
					//currentTags = new ArrayList<>();

					textForTop[1] = n;
					series.add(n);
					System.out.println("Newspaper: " + n);
					List<DataContainer> dateDataCopy = newspaperData.get(n);
					List<DataContainer>  dateData = new ArrayList<>(newspaperData.get(n));
					//jezeli jest wykres dla parametrow grafu, to tylko labelCOmparator
					//jezeli wykres dla parametrow wezlow, to za pierwszym razem valueCOmparator, a dla kolejnych trzeba sciagac reczenie (pomocnicza HashMap?)
					//ew. recznie inicjalizuje importantTags i wtedy tu wyciagam je dla kazdej gazety
					if (getTop) {
						if (i == 0 && importantTagsNames.isEmpty()) {
							dateDataCopy.sort(valueComparator);
						//	System.out.println("Important tags:");
							//tu musze zapamietac nazwy tagow
							//za chwile zapametam indeksy w zaleznosci od kolejnosci alfabetycznej
							for (int k = 0; k < nrOfTopTags; k++) { //sparametryzowac po liczbie tagow, w wywolaniu funkcji
								int index = dateData.size()-1-k;
								importantTagsNames.add(dateDataCopy.get(index).date);//importantTags.add(dateData.get(index));
								textForTop[3+2*k] = dateDataCopy.get(index).date;
								textForTop[3+2*k+1] = dateDataCopy.get(index).value.toString();
							}
							topWriter.writeNext(textForTop);
							for (String t: importantTagsNames){
								importantTagsList.get(p).add(getTagIndex(dateData, t));
								//System.out.println(t+"\t"+getTagIndex(dateDataCopy, t));
							}
							//System.out.println("Important tags list size for "+ p+": " + importantTagsList.get(p).size());
						} else {
							for(int k = 0; k < importantTagsList.get(p).size(); k++){
								int index = importantTagsList.get(p).get(k);
								textForTop[3+2*k] = dateData.get(index).date;
								textForTop[3+2*k+1] = dateData.get(index).value.toString();
							}
							topWriter.writeNext(textForTop);
						}
					}
					//System.out.println("currentTags.size(): " + currentTags.size());
					if (willCreateReport) {
						if (!getTop) {
							for (DataContainer d : dateData) {
								LabelsSet.add(d.date);
								values.putIfAbsent(d.date, new ArrayList<>(nrOfSerieses));
								//System.out.println("list size: " + values.get(d.date).size());
								while (values.get(d.date).size() < i)
									values.get(d.date).add(0);
								values.get(d.date).add(i, d.value);
								//System.out.println(d.value + " added to series  " + i + " for " + d.date);
							}
						} else {
							System.out.println("Important tags list size for " + p + ": " + importantTagsList.get(p).size());
							for (Integer m : importantTagsList.get(p)) {
								DataContainer d = dateData.get(m);
								LabelsSet.add(d.date);
								values.putIfAbsent(d.date, new ArrayList<>(nrOfSerieses));
								System.out.println("list size: " + values.get(d.date).size());
								while (values.get(d.date).size() < i)
									values.get(d.date).add(0);
								values.get(d.date).add(i, d.value);
								System.out.println(d.value + " added to series  " + i + " for " + d.date);
							}
						}
					}
					i++;
				}

				if (willCreateReport) {
					labelValues = new ArrayList<>(LabelsSet);
					//if (!getTop)
					sort(labelValues);
					CategoryChart chart = this.getChart();
					if (chart == null) {
						System.out.println("No chart to display for " + series.get(0) + " etc. " + labelValues.get(0) + " etc., param: " + p);
						continue;
					}
					try {
						BitmapEncoder.saveBitmap(chart, "src/main/resources/charts/" + chartName + "_" + p, BitmapEncoder.BitmapFormat.PNG);
						Path path = Paths.get("src/main/resources/charts/" + chartName + "_" + p + ".png");
						Image img = Image.getInstance(path.toAbsolutePath().toString());
						img.scaleToFit(rect.getWidth() - margin, rect.getHeight());
						report.add(img);
					} catch (Exception e) {
						System.err.println("BŁĄD");
						continue;
					}
				}
			}

			if (topWriter != null)
				topWriter.close();
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
		System.out.println("Current param in getChart: " + currentParam);

		// Customize Chart
		chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
		chart.getStyler().setHasAnnotations(true);
		chart.getStyler().setXAxisLabelRotation(70);
		chart.getStyler().setYAxisDecimalPattern("#0.000");

		// Series
		List<CoefficientData> coefficientData = new ArrayList<>(labelValues.size());
		for (int i = 0; i < nrOfSerieses; i++){
			List<Number> currentValues = new ArrayList<>();
			for (String date: labelValues){
				if (i >= values.get(date).size() || values.get(date).get(i) == null)
					currentValues.add(0);
				else
					currentValues.add(values.get(date).get(i));
			}
			if (i == 0){
				for(int k = 0; k < labelValues.size();k++){
					if (coefficientData.size() <= k)
						coefficientData.add(new CoefficientData());
					coefficientData.get(k).X = currentValues.get(k);
				}
			} else if (i == 1){
				for(int k = 0; k < labelValues.size();k++){
					if (coefficientData.size() <= k)
						coefficientData.add(new CoefficientData());
					coefficientData.get(k).Y = currentValues.get(k);
				}
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

		if (nrOfSerieses == 2 && labelValues.size() > 1) {
			coefficientData.sort(new xComparator());
			for (int k  = 0; k < labelValues.size(); k++)
				coefficientData.get(k).xRank = k+1;

			coefficientData.sort(new yComparator());
			for (int k  = 0; k < labelValues.size(); k++)
				coefficientData.get(k).yRank = k+1;

			double sum = 0;
			for (CoefficientData d : coefficientData){
				double diff = d.xRank - d.yRank;
				sum+= diff * diff;
			}
			double n = labelValues.size();
			double correlation = 1 - (6*sum)/(n * (n*n -1));
			chart.setTitle(currentParam + " - " + colName + "(correlation: " + correlation+")");
		}
		return chart;
	}

	private class CoefficientData{ //algorytm w getChart dziala  tylko dla niepowtarzajacych sie wartosci w X i w Y
		private Number X, Y;
		private double xRank, yRank;
	}
	private class xComparator implements Comparator<CoefficientData>{
		@Override
		public int compare(CoefficientData t0, CoefficientData t1) {
			Double d0 = t0.X.doubleValue();
			Double d1 = t1.X.doubleValue();
			return d0.compareTo(d1);
		}
	}
	private class yComparator implements Comparator<CoefficientData>{
		@Override
		public int compare(CoefficientData t0, CoefficientData t1) {
			Double d0 = t0.Y.doubleValue();
			Double d1 = t1.Y.doubleValue();
			return d0.compareTo(d1);
		}
	}

}

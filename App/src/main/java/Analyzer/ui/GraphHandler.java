package Analyzer.ui;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import org.gephi.appearance.api.Partition;
import org.gephi.graph.api.*;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.preview.PDFExporter;
import org.gephi.plugins.prestige.PrestigeStatistics;
import org.gephi.plugins.prestige.calculation.DomainCalculator;
import org.gephi.plugins.prestige.calculation.IndegreeCalculator;
import org.gephi.plugins.prestige.calculation.ProximityCalculator;
import org.gephi.plugins.prestige.calculation.RankCalculator;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.*;
import org.openide.util.Lookup;
import Analyzer.model.Country;
import Analyzer.model.Feed;
import Analyzer.model.PressRelease;
import Analyzer.model.Tag;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by karolina on 17.07.17.
 */
public class GraphHandler {
	private static Set<PressRelease> notes;
	private static ReportInput input;
	private static GraphModel graphModel;

	private static String[] columnsToSee = {
			GraphDistance.BETWEENNESS,
			GraphDistance.CLOSENESS,
			GraphDistance.ECCENTRICITY,
			GraphDistance.HARMONIC_CLOSENESS,
			Degree.INDEGREE,
			Degree.OUTDEGREE,
			Hits.AUTHORITY,
			Hits.HUB,
			PageRank.PAGERANK,
			Modularity.MODULARITY_CLASS,
			EigenvectorCentrality.EIGENVECTOR,
			ClusteringCoefficient.CLUSTERING_COEFF,
			ConnectedComponents.STRONG,
			ConnectedComponents.WEAKLY,
			WeightedDegree.WINDEGREE,
			WeightedDegree.WOUTDEGREE,

			//Prestige
			DomainCalculator.DOMAIN_KEY,
			IndegreeCalculator.INDEGREE_KEY,
			IndegreeCalculator.INDEGREE_NORMALIZED_KEY,
			ProximityCalculator.PROXIMITY_KEY,
			RankCalculator.RANK_KEY,
			RankCalculator.NORMALIZED_RANK_KEY,

	};

	public static ReportInput getInput(){
		return input;
	}
	public static void resetInput() {
		input = null;
	}

	public static void initFakePressReleases(){
		notes = new HashSet<>();
		for (int i = 0; i < 100; i++) {
			String title = "titel" + i;
			String content = "content" + i;
			Date date = new Date();
			Feed feed = new Feed("name", "section");
			Set<Tag> tags = new HashSet<>();
			for (int j = 0; j < 3; j++) {
				String tagName = "tag" + (new Integer((i + j)%10)).toString();
				tags.add(new Tag(tagName, new Country(), new HashSet<>()));
			}
			PressRelease pr = new PressRelease(title, date, content, tags, feed);
			notes.add(pr);
		}

		for (int i = 101; i < 110; i++) {
			String title = "title" + i;
			String content = "content" + i;
			Date date = new Date();
			Feed feed = new Feed("name", "section");
			Set<Tag> tags = new HashSet<>();
			for (int j = 0; j < 3; j++) {
				String tagName = "tag" + (new Integer((i + j))).toString();
				tags.add(new Tag(tagName, new Country(), new HashSet<>()));
			}
			PressRelease pr = new PressRelease(title, date, content, tags, feed);
			notes.add(pr);
		}
	}

	public static void addEdge(GraphModel graphModel, Node n1, Node n2){
		DirectedGraph directedGraph = graphModel.getDirectedGraph();
		Edge e1 = directedGraph.getEdge(n1, n2);
		if(e1 == null){
			e1 = graphModel.factory().newEdge(n1, n2, 0, 1.0, true);
			directedGraph.addEdge(e1);
		}
		else {
			double weight = e1.getWeight();
			e1.setWeight(weight+1);
		}
	}

	public static void initGraphFromPressReleases(Set<PressRelease> newNotes) throws DocumentException {
		notes = newNotes;
		if (notes == null || notes.isEmpty()){
			System.out.println("Empty result");
			return;
		}
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		Workspace workspace = pc.getCurrentWorkspace();
		graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);

		DirectedGraph directedGraph= graphModel.getDirectedGraph();
		for (PressRelease pr : notes) {
			Set<Tag> noteTags = pr.getTags();
			List<Tag> noteTagsList = new ArrayList<>(noteTags);
			for (int i = 0; i < noteTags.size(); i++) {
				for (int j = i + 1; j < noteTags.size(); j++) {
					Tag tag1 = noteTagsList.get(i);
					Tag tag2 = noteTagsList.get(j);

					Node n1 = directedGraph.getNode(tag1.getName());
					if (n1 == null){
						n1 = graphModel.factory().newNode(tag1.getName());
						n1.setLabel(tag1.getName());
						directedGraph.addNode(n1);
					}

					Node n2 = directedGraph.getNode(tag2.getName());
					if (n2 == null) {
						n2 = graphModel.factory().newNode(tag2.getName());
						n2.setLabel(tag2.getName());
						directedGraph.addNode(n2);
					}

					addEdge(graphModel, n1, n2);
					addEdge(graphModel, n2, n1);
				}
			}
		}
		if (directedGraph.getNodeCount() == 0){
			System.out.println("Node's count: 0  - the end of analysis");
			return;
		}
		System.out.println();

	}

	private static void addCsv(String date1, String date2, CSVReader reader, DirectedGraph directedGraph) throws IOException {
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null){
			if(nextLine[0].compareTo(date1)< 0 || nextLine[0].compareTo(date2)>0)
				continue;
			System.out.print("Line: ");
			for (String s : nextLine){
				System.out.print(s + "\t");
			}
			Node n1 = directedGraph.getNode(nextLine[2]);
			if (n1 == null){
				n1 = graphModel.factory().newNode(nextLine[2]);
				n1.setLabel(nextLine[2]);
				directedGraph.addNode(n1);
			}
			for (int k = 3; ; k++){
				if (nextLine[k].equals(""))
					break;
				Node n2 = directedGraph.getNode(nextLine[k]);
				if (n2 == null) {
					n2 = graphModel.factory().newNode(nextLine[k]);
					n2.setLabel(nextLine[k]);
					directedGraph.addNode(n2);
				}
				addEdge(graphModel, n1, n2);
				addEdge(graphModel, n2, n1);
			}

		}
	}
	public static void initGraphFromCsv(String date1, String date2, CSVReader reader1) throws IOException {
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		Workspace workspace = pc.getCurrentWorkspace();
		graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);

		DirectedGraph directedGraph= graphModel.getDirectedGraph();
		addCsv(date1, date2, reader1, directedGraph);

		if (directedGraph.getNodeCount() == 0){
			System.out.println("Node's count: 0  - the end of analysis");
			return;
		}
		System.out.println();
	}


	public static void graphCreator(String date, String newspaper, Document report, SortedSet<Tag> tags,
									CSVWriter graphWriter, CSVWriter nodesWriter, CSVWriter edgesWriter,
									boolean initColumns) throws DocumentException {
        /*//Iterate over nodes
        for (Node n : directedGraph.getNodes()) {
            Node[] neighbors = directedGraph.getNeighbors(n).toArray();
            System.out.println(n.getLabel() + " has " + neighbors.length + " neighbors");
        }
        //Iterate over edges
        for (Edge e : directedGraph.getEdges()) {
            System.out.println(e.getSource().getId() + " -> " + e.getTarget().getId());
        }*/

		DirectedGraph directedGraph= graphModel.getDirectedGraph();
        System.out.println("Nodes analysis:");
		//do not create pdf
		/*Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
		Font font = FontFactory.getFont(FontFactory.COURIER, 12, BaseColor.BLACK);
		Font fontSmall = FontFactory.getFont(FontFactory.COURIER, 8, BaseColor.BLUE);
		Paragraph chunk = new Paragraph(newspaper + " " + date, subtitleFont);
		report.add(chunk);*/

		GraphDistance distance = new GraphDistance();
		distance.setDirected(true);
		distance.setNormalized(true); //NORMALIZED
		distance.execute(graphModel);
		Degree degree = new Degree();
		degree.execute(graphModel);
		Hits hits = new Hits();
		hits.setUndirected(false);
		hits.execute(graphModel);
		PageRank pageRank = new PageRank();
		pageRank.setDirected(true);
		pageRank.setUseEdgeWeight(true);
		pageRank.execute(graphModel);
		EigenvectorCentrality eigenvectorCentrality = new EigenvectorCentrality();
		eigenvectorCentrality.setDirected(true);
		eigenvectorCentrality.execute(graphModel);
		ClusteringCoefficient clusteringCoefficient = new ClusteringCoefficient();
		clusteringCoefficient.setDirected(true);
		clusteringCoefficient.execute(graphModel);
		ConnectedComponents connectedComponents = new ConnectedComponents();
		connectedComponents.setDirected(true);
		connectedComponents.execute(graphModel);
		WeightedDegree weightedDegree = new WeightedDegree();
		weightedDegree.execute(graphModel);
		Modularity modularity = new Modularity();
		modularity.setUseWeight(true);
		modularity.execute(graphModel);
		GraphDensity density = new GraphDensity();
		density.execute(graphModel);

		//PrestigeStatistics
		PrestigeStatistics prestigeStatistics = new PrestigeStatistics();
		prestigeStatistics.setCalculateDomain(true);
		prestigeStatistics.setCalculateIndegree(true);
		prestigeStatistics.setCalculateProximity(true);
		prestigeStatistics.setRankProminenceAttributeId(GraphDistance.BETWEENNESS); //dla proby
		prestigeStatistics.setCalculateRank(true);
		prestigeStatistics.execute(graphModel);


		String[] textForEdges = new String[tags.size()+3];
		int j=3;
		if (initColumns) {
			textForEdges[0] = "Date";
			textForEdges[1] = "Newspaper";
			textForEdges[2] = "Source";
			for (;j<tags.size()+3; j++){
				textForEdges[j] = "N" + j;
			}
			edgesWriter.writeNext(textForEdges);
		}

		textForEdges[0] = date;
		textForEdges[1] = newspaper;
		for (Tag t : tags) {
			j = 3;
			Node n1 = directedGraph.getNode(t.getName());
			if (n1 != null){
				textForEdges[2] = n1.getLabel();
				List<Node> neighbors = (ArrayList<Node>)directedGraph.getNeighbors(n1).toCollection();
				for (Node n2 : neighbors){
					textForEdges[j] = n2.getLabel();
					j++;
				}
				for (;j < tags.size()+3; j++)
					textForEdges[j] = "";
				edgesWriter.writeNext(textForEdges);
			}
		}


		String[] textForNodes = new String[tags.size()+3];
		j=3;
		if (initColumns) {
			textForNodes[0] = "Date";
			textForNodes[1] = "Newspaper";
			textForNodes[2] = "Param name";
			for (Tag t : tags) {
				textForNodes[j] = t.getName();
				j++;
			}
			nodesWriter.writeNext(textForNodes);
		}
		textForNodes[0] = date;
		textForNodes[1] = newspaper;
		Table attributes = graphModel.getNodeTable();
		for (String col: columnsToSee){
			textForNodes[2] = col;
			Column current = attributes.getColumn(col);
			System.out.println(col+ ":");
			/*chunk = new Paragraph(col+ ":\n", font);
			report.add(chunk);*/
			j = 3;
			for (Tag t : tags) {
				Node n = graphModel.getGraph().getNode(t.getName());
				if (n != null){
					System.out.println(n.getLabel() + ": " + n.getAttribute(current));
					textForNodes[j] = n.getAttribute(current).toString();
					/*chunk = new Paragraph(n.getLabel() + ": " + n.getAttribute(current) + "\n", fontSmall);
					report.add(chunk);*/
				}
				else
					textForNodes[j] = String.valueOf(0);
				j++;
			}
			nodesWriter.writeNext(textForNodes);
		}

		//get nodes for each connected component and for each modularity class
		Map<Integer, List<Node>> componentsMap = new HashMap<Integer, List<Node>>();
		Map<Integer, List<Node>> modularityMap = new HashMap<Integer, List<Node>>(); //jak znalezc liczbe spolecznosci?
		for (int i = 0; i < connectedComponents.getConnectedComponentsCount(); i++){
			componentsMap.put(new Integer(i), new ArrayList<>());
		}
		for (Node n: graphModel.getGraph().getNodes()){
			componentsMap.get(n.getAttribute(ConnectedComponents.STRONG)).add(n);
		}

		input = new ReportInput();
		input.initNodeMaxValues(graphModel);
		input.setGraphValue("Nr of nodes", directedGraph.getNodeCount());
		input.setGraphValue("Nr of edges", directedGraph.getEdgeCount());
		input.setGraphValue("Nr of connected components", connectedComponents.getConnectedComponentsCount());
		input.setGraphValue("Average shortest path length", distance.getPathLength());
		input.setGraphValue("Diameter", distance.getDiameter());
		input.setGraphValue("Radius", distance.getRadius());
		input.setGraphValue("Average clustering coefficient", clusteringCoefficient.getAverageClusteringCoefficient());
		input.setGraphValue("Average degree", degree.getAverageDegree());
		input.setGraphValue("Modularity", modularity.getModularity());
		input.setGraphValue("The density of the graph", density.getDensity());
		input.date = date;
		input.newspaper = newspaper;

		SortedSet<String> graphParams = new TreeSet<>(input.getGraphParams());
		String[] textForGraph = new String[graphParams.size()+2];

		j = 2;
		if (initColumns) {
			textForGraph[0] = "Date";
			textForGraph[1] = "Newspaper";
			for (String param : graphParams) {
				textForGraph[j] = param;
				j++;
			}
			graphWriter.writeNext(textForGraph);
		}

		textForGraph[0] = date;
		textForGraph[1] = newspaper;

		j =2;
		for (String s: graphParams){
			System.out.println(s + ": "+  input.getGraphValue(s));
			textForGraph[j] = input.getGraphValue(s).toString();
			j++;
			//chunk = new Paragraph(s + input.getGraphValue(s), fontSmall);
			//report.add(chunk);
		}
		graphWriter.writeNext(textForGraph);

        /*PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
        PreviewModel previewModel = previewController.getModel();
        previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.WHITE));
        previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
        previewModel.getProperties().putValue(PreviewProperty.EDGE_OPACITY, 50);
        previewModel.getProperties().putValue(PreviewProperty.BACKGROUND_COLOR, Color.BLACK);

        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        try {
            ec.exportFile(new File("graf_" + paramName+ "_" + paramValue+"_simple.pdf"));
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        //PDF Exporter config and export to Byte array

        PDFExporter pdfExporter = (PDFExporter) ec.getExporter("pdf");
        pdfExporter.setPageSize(PageSize.A4);
        pdfExporter.setWorkspace(workspace);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ec.exportStream(baos, pdfExporter);
        byte[] pdf = baos.toByteArray();
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream("graf_" + paramName+ "_" + paramValue+".pdf");
            fos.write(pdf);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
	}

}

package Analyzer.ui;

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
			Set<Tag> tags = new HashSet<Tag>();
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
			Set<Tag> tags = new HashSet<Tag>();
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


	public static void graphCreator(String paramName, String paramValue, Set<PressRelease> newNotes, Document report) throws DocumentException {
		notes = newNotes;
		if (notes == null || notes.isEmpty()){
			System.out.println("Empty result");
			return;
		}
		// initFakePressReleases();
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		Workspace workspace = pc.getCurrentWorkspace();

//Get a graph model - it exists because we have a workspace
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);

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

        /*//Iterate over nodes
        for (Node n : directedGraph.getNodes()) {
            Node[] neighbors = directedGraph.getNeighbors(n).toArray();
            System.out.println(n.getLabel() + " has " + neighbors.length + " neighbors");
        }
        //Iterate over edges
        for (Edge e : directedGraph.getEdges()) {
            System.out.println(e.getSource().getId() + " -> " + e.getTarget().getId());
        }*/

		System.out.println("Nodes analysis:");
		Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
		Font font = FontFactory.getFont(FontFactory.COURIER, 12, BaseColor.BLACK);
		Font fontSmall = FontFactory.getFont(FontFactory.COURIER, 8, BaseColor.BLUE);
		Paragraph chunk = new Paragraph(paramValue, subtitleFont);
		report.add(chunk);

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

		//PrestigeStatistics
		PrestigeStatistics prestigeStatistics = new PrestigeStatistics();
		prestigeStatistics.setCalculateDomain(true);
		prestigeStatistics.setCalculateIndegree(true);
		prestigeStatistics.setCalculateProximity(true);
		prestigeStatistics.setRankProminenceAttributeId(GraphDistance.BETWEENNESS); //dla proby
		prestigeStatistics.setCalculateRank(true);
		prestigeStatistics.execute(graphModel);


		Table attributes = graphModel.getNodeTable();
		//Iterate over values
		for (String col : columnsToSee) {
			Column current = attributes.getColumn(col);
			System.out.println(col+ ":");
			chunk = new Paragraph(col+ ":\n", font);
			report.add(chunk);
			for (Node n : graphModel.getGraph().getNodes()) {
				System.out.println(n.getLabel() + ": " + n.getAttribute(current));
				chunk = new Paragraph(n.getLabel() + ": " + n.getAttribute(current) + "\n", fontSmall);
				report.add(chunk);
			}
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
		System.out.println("Nr of nodes: " + directedGraph.getNodeCount());
		System.out.println("Nr of edges: " + directedGraph.getEdgeCount());
		System.out.println("Nr of connected components: " + connectedComponents.getConnectedComponentsCount());

		System.out.println("The average shortest path length in the network: " + distance.getPathLength());
		System.out.println("The diameter of the network: "+ distance.getDiameter());
		System.out.println("The radius of the network: "+ distance.getRadius());
		System.out.println("The average clustering coefficient: " + clusteringCoefficient.getAverageClusteringCoefficient());
		System.out.println("The average degree: " + degree.getAverageDegree());
		System.out.println("Modularity: "+ modularity.getModularity());
		//System.out.println("Lineage origin: " + lineage.getOrigin());

       /* chunk = new Paragraph("Nr of nodes: " + directedGraph.getNodeCount() + "\n", fontSmall);
        report.add(chunk);
        chunk = new Paragraph("Nr of edges: " + directedGraph.getEdgeCount() + "\n", fontSmall);
        report.add(chunk);
        chunk = new Paragraph("Nr of connected components: " + connectedComponents.getConnectedComponentsCount() + "\n", fontSmall);
        report.add(chunk);

        chunk = new Paragraph("The average shortest path length in the network: " + distance.getPathLength() + "\n", fontSmall);
        report.add(chunk);
        chunk = new Paragraph("The diameter of the network: "+ distance.getDiameter() + "\n", fontSmall);
        report.add(chunk);
        chunk = new Paragraph("The radius of the network: "+ distance.getRadius() + "\n", fontSmall);
        report.add(chunk);
        chunk = new Paragraph("The average clustering coefficient: " + clusteringCoefficient.getAverageClusteringCoefficient() + "\n", fontSmall);
        report.add(chunk);
        chunk = new Paragraph("The average degree: " + degree.getAverageDegree() + "\n", fontSmall);
        report.add(chunk);
        chunk = new Paragraph("Modularity: "+ modularity.getModularity() + "\n", fontSmall);
        report.add(chunk);*/

		GraphDensity density = new GraphDensity();
		density.execute(graphModel);
		System.out.println("The density of the graph: "+ density.getDensity());
       /* chunk = new Paragraph("The density of the graph: "+ density.getDensity() + "\n", fontSmall);
        report.add(chunk);*/

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
		input.setGraphValue("The density of the graph: ", density.getDensity());
		//z pluginów
		input.paramValue = paramValue;
		input.paramName = paramName;

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

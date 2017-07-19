package pl.edu.agh.Analyzer.ui;

import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfContentByte;
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
import org.wouterspekkink.plugins.metric.lineage.Lineage;
import pl.edu.agh.Analyzer.model.Country;
import pl.edu.agh.Analyzer.model.Feed;
import pl.edu.agh.Analyzer.model.PressRelease;
import pl.edu.agh.Analyzer.model.Tag;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by karolina on 17.07.17.
 */
public class GraphHandler {
    private static List<PressRelease> notes;
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

            //Lineage
            /*Lineage.ADISTANCE,
            Lineage.ANCESTOR,
            Lineage.DESCENDANT,
            Lineage.DDISTANCE,
            Lineage.LINEAGE,
            Lineage.ORIGIN,*/
    };

    public static ReportInput getInput(){
        return input;
    }
    public static void resetInput() {
        input = null;
    }

    public static void initFakePressReleases(){
        notes = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            String title = "titel" + i;
            String content = "content" + i;
            Date date = new Date();
            Feed feed = new Feed("name", "section");
            List<Tag> tags = new ArrayList<Tag>();
            for (int j = 0; j < 3; j++) {
                String tagName = "tag" + (new Integer((i + j)%10)).toString();
                tags.add(new Tag(tagName, new Country(), new ArrayList<>()));
            }
            PressRelease pr = new PressRelease(title, date, content, tags, feed);
            notes.add(pr);
        }

        for (int i = 101; i < 110; i++) {
            String title = "title" + i;
            String content = "content" + i;
            Date date = new Date();
            Feed feed = new Feed("name", "section");
            List<Tag> tags = new ArrayList<Tag>();
            for (int j = 0; j < 3; j++) {
                String tagName = "tag" + (new Integer((i + j))).toString();
                tags.add(new Tag(tagName, new Country(), new ArrayList<>()));
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


    public static void graphCreator(String paramName, String paramValue, List<PressRelease> newNotes) {
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
            List<Tag> noteTags = pr.getTags();
            for (int i = 0; i < noteTags.size(); i++) {
                for (int j = i + 1; j < noteTags.size(); j++) {
                    Tag tag1 = noteTags.get(i);
                    Tag tag2 = noteTags.get(j);

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
        modularity.execute(graphModel);

        //PrestigeStatistics
        PrestigeStatistics prestigeStatistics = new PrestigeStatistics();
        prestigeStatistics.setCalculateDomain(true);
        prestigeStatistics.setCalculateIndegree(true);
        prestigeStatistics.setCalculateProximity(true);
        prestigeStatistics.setRankProminenceAttributeId(GraphDistance.BETWEENNESS); //dla proby
        prestigeStatistics.setCalculateRank(true);
        prestigeStatistics.execute(graphModel);

        //Lineage
        /*Lineage lineage = new Lineage();
        lineage.setDirected(true);
        lineage.setOrigin("tag2");
        lineage.execute(graphModel);*/

        Table attributes = graphModel.getNodeTable();
        //Iterate over values
        for (String col : columnsToSee) {

            //String col = GraphDistance.;
            Column current = attributes.getColumn(col);
            System.out.println(col+ ":");
            for (Node n : graphModel.getGraph().getNodes()) {
                System.out.println(n.getLabel() + ": " + n.getAttribute(current));
            }
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

        GraphDensity density = new GraphDensity();
        density.execute(graphModel);
        System.out.println("The density of the graph: "+ density.getDensity());

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
        //z pluginów
        //input.setGraphValue("Lineage origin", lineage.getOrigin());
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

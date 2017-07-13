package pl.edu.agh.Analyzer.ui;

import org.gephi.graph.api.*;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.*;
import org.openide.util.Lookup;
import pl.edu.agh.Analyzer.model.*;
import pl.edu.agh.Analyzer.controller.AnalysisController;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;


/**
 * Created by karolina on 11.07.17.
 */
public class AnalysisHandler {
    private BufferedReader br;
    private AnalysisController controller;

    public AnalysisHandler(BufferedReader br){
        this.br = br;
        controller = new AnalysisController();
    }
    public void startHandling() throws IOException {
        graphCreator(); // zmienic, aby bazowal na wynikach z zapytania dla dat

        String field = "", value = "", fieldName = "";

        System.out.println("Enter the field you'd like to focus on: \n" +
                "\t d -> month and year\n" +
                "\t n -> newspaper title\n" +
                "\t c -> country\n" +
                "\t l -> language");
        field = br.readLine();

        if (field.startsWith("d")){
            fieldName = "month and year (mm-yyyy)";
        }
        else if (field.startsWith("n")){
            fieldName = "newspaper title";
        }
        else if (field.startsWith("c")){
            fieldName = "country name";
        }
        else if (field.startsWith("l")){
            fieldName = "language";
        }
        System.out.println("Enter " + fieldName +  " (type ?? to show all possible values):");
        value = br.readLine();
        while (value.startsWith("??")){
            if (field.startsWith("d")){
                listDates();
            }
            else if (field.startsWith("n")){
                listNewspapers();
            }
            else if (field.startsWith("c")){
                listCountries();
            }
            else if (field.startsWith("l")){
                listLanguages();
            }
            value = br.readLine();
        }
        System.out.println("You've chosen field " + fieldName + " and value "+ value);
        System.out.println("All db entries will be fetched soon...");


        if (field.startsWith("d")) {
            String month = value.substring(0, 2);
            String year = value.substring(3, 7);
            System.out.println("Chosen month: " + month + ", year: " + year);
            List<PressRelease> result = controller.getPressReleases(month, year);
            System.out.println("Result: ");
            for (PressRelease pr : result) {
                System.out.println("ID: " + pr.getId() + "; Title: " + pr.getTitle() + "; Content: " + pr.getContent());
            }
        }
        else {
            System.out.println("Sorry, option for " + fieldName + " is currently unsupported");
        }
        

    }

    private void graphCreator() {
        //fakeowe pressreleases
        List<PressRelease> notes = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            String title = "titel" + i;
            String content = "content" + i;
            Date date = new Date();
            Feed feed = new Feed("name", "section");
            List<Tag> tags = new ArrayList<Tag>();
            for (int j = 0; j < 3; j++) {
                String tagName = "tag" + (new Integer(i + j)).toString();
                tags.add(new Tag(tagName, new Country(), new ArrayList<>()));
            }
            PressRelease pr = new PressRelease(title, date, content, tags, feed);
            notes.add(pr);
        }
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

//Get a graph model - it exists because we have a workspace
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        //AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();

        //Set<Node> nodes = new HashSet<>();
        //Set<Edge> edges = new HashSet<>();
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
                    //nodes.add(n1);
                    Node n2 = directedGraph.getNode(tag2.getName());
                    if (n2 == null) {
                        n2 = graphModel.factory().newNode(tag2.getName());
                        n2.setLabel(tag2.getName());
                        directedGraph.addNode(n2);
                    }
                    //nodes.add(n2);

                    Edge e1 = directedGraph.getEdge(n1, n2);
                    if(e1 == null){
                        e1 = graphModel.factory().newEdge(n1, n2, 0, 1.0, true);
                        directedGraph.addEdge(e1);
                    }
                    Edge e2 = directedGraph.getEdge(n2, n1);
                    if (e2 == null) {
                        e2 = graphModel.factory().newEdge(n2, n1, 0, 1.0, true);
                        directedGraph.addEdge(e2);
                    }
                    //edges.add(e1);
                    //edges.add(e2);
                    //jak dodawac parametry do Edge?
                }
            }
        }
        /*for (Node n : nodes){
            System.out.println("ID: " + n.getId() + "; label: " + n.getLabel());
        }

        DirectedGraph directedGraph= graphModel.getDirectedGraph();
        directedGraph.addAllNodes(nodes);
        //directedGraph.addAllEdges(edges); //to jest zle, trzeba bedzie iterowac i sprawdzac, czy jest - jesli tak, zwiekszam wage
        for (Edge e : edges){
            try {
                directedGraph.addEdge(e);
                System.out.print("+");
            }catch (Exception ex){
                System.out.print("-");
            }

        }*/
        System.out.println();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();


        //Iterate over nodes
        for (Node n : undirectedGraph.getNodes()) {
            Node[] neighbors = undirectedGraph.getNeighbors(n).toArray();
            System.out.println(n.getLabel() + " has " + neighbors.length + " neighbors");
        }

        //Iterate over edges
        for (Edge e : undirectedGraph.getEdges()) {
            System.out.println(e.getSource().getId() + " -> " + e.getTarget().getId());
        }

        System.out.println("Basic analysis:");
        System.out.println("Nr of nodes: " + undirectedGraph.getNodeCount());
        System.out.println("Nr of edges: " + undirectedGraph.getEdgeCount());


        GraphDistance distance = new GraphDistance();
        //distance.setDirected(true); //co to robi???
        distance.execute(graphModel);
        Degree degree = new Degree();
        degree.execute(graphModel);
        Hits hits = new Hits();
        hits.execute(graphModel);
        PageRank pageRank = new PageRank();
        pageRank.execute(graphModel);
        EigenvectorCentrality eigenvectorCentrality = new EigenvectorCentrality();
        eigenvectorCentrality.execute(graphModel);
        ClusteringCoefficient clusteringCoefficient = new ClusteringCoefficient();
        clusteringCoefficient.execute(graphModel);
        ConnectedComponents connectedComponents = new ConnectedComponents();
        connectedComponents.execute(graphModel);
        WeightedDegree weightedDegree = new WeightedDegree();
        weightedDegree.execute(graphModel);


        Table attributes = graphModel.getNodeTable();
        //Iterate over values
        List<String> columnToSee = new ArrayList<>();
        columnToSee.add(GraphDistance.BETWEENNESS);
        columnToSee.add(GraphDistance.CLOSENESS);
        columnToSee.add(GraphDistance.ECCENTRICITY);
        columnToSee.add(GraphDistance.HARMONIC_CLOSENESS);
        columnToSee.add(Degree.INDEGREE);
        columnToSee.add(Degree.OUTDEGREE);
        columnToSee.add(Hits.AUTHORITY);
        columnToSee.add(Hits.HUB);
        columnToSee.add(PageRank.PAGERANK);
        columnToSee.add(EigenvectorCentrality.EIGENVECTOR);
        columnToSee.add(ClusteringCoefficient.CLUSTERING_COEFF);
        //columnToSee.add(ConnectedComponents.STRONG);
        columnToSee.add(ConnectedComponents.WEAKLY);
        columnToSee.add(WeightedDegree.WINDEGREE);
        columnToSee.add(WeightedDegree.WOUTDEGREE);
        for (String col : columnToSee) {
            Column current = attributes.getColumn(col);
            System.out.println(current.getTitle() + ":");
            for (Node n : graphModel.getGraph().getNodes()) {
                System.out.println(n.getLabel() + ": " + n.getAttribute(current));
            }

        }
        System.out.println("The average shortest path length in the network: " + distance.getPathLength());
        System.out.println("The diameter of the network: "+ distance.getDiameter());
        System.out.println("The radius of the network: "+ distance.getRadius());

        GraphDensity density = new GraphDensity();
        density.execute(graphModel);
        System.out.println("The density of the graph: "+ density.getDensity());
    }

    private void listNewspapers(){
        List<Newspaper> result = controller.getAllNewspapers();
        System.out.println("Newspapers:");
        for (Newspaper n : result) {
            System.out.println(n.getName());
        }
    }
    private void listCountries(){
        List<Country> result = controller.getAllCountries();
        System.out.println("Countries (use tag in parenthesis to choose one):");
        for (Country c : result) {
            System.out.println(c.getName() + "("+c.getTag()+")");
        }
    }  
    private void listLanguages(){
        List<Language> result = controller.getAllLanguages();
        System.out.println("Languages:");
        for (Language l : result) {
            System.out.println(l.getName());
        }
    }
    private void listDates(){
        List<PressRelease> result = controller.getPressReleasesSortedByDate();
        int size = result.size();
        System.out.println("First date: " + result.get(0).getDate());
        System.out.println("Last date: " + result.get(size-1).getDate());
    }
}

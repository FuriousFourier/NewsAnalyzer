package pl.edu.agh.Analyzer.ui;

import org.gephi.graph.api.*;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import pl.edu.agh.Analyzer.model.Country;
import pl.edu.agh.Analyzer.model.Feed;
import pl.edu.agh.Analyzer.model.PressRelease;
import pl.edu.agh.Analyzer.model.Tag;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by karolina on 11.07.17.
 */
public class AnalysisHandler {
    private BufferedReader br;

    public AnalysisHandler(BufferedReader br){
        this.br = br;
    }
    public void startHandling() throws IOException {
        String field = "", value = "", fieldName = "";

        MainUI.myPrint("Enter the field you'd like to focus on: " +
                "\t m -> month and year" +
                "\t n -> newspaper title" +
                "\t c -> country" +
                "\t l -> language");
        field = br.readLine();

        if (field.startsWith("m")){
            fieldName = "month and year, mm:yyyy";
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
        MainUI.myPrint("Enter " + fieldName +  " (type ?? to show all possible values):");
        value = br.readLine();
        while (value.startsWith("??")){
            if (field.startsWith("m")){
                listYears();
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
        //tu bedzie zapytanie
        //tu powstanie graf
    }

    private void graphCreator(){
        //fakeowe pressreleases
        List<PressRelease> notes = new ArrayList<>();
     for (int i  = 0; i < 100; i++){
         String title = "titel" + i;
         String content = "content" + i;
                 Date date = new Date();
                 Feed feed = new Feed("name", "section");
         List<Tag> tags= new ArrayList<Tag>();
         for (int j = 0; j < 3; j++){
             String tagName = "tag" + (new Integer(i+j)).toString();
             tags.add(new Tag(tagName, new Country(), new ArrayList<>()));
         }
        PressRelease pr = new PressRelease(title, date, content, tags, feed);
         notes.add(pr);
        }
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

//Get a graph model - it exists because we have a workspace
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();
        for (PressRelease pr : notes) {
            List<Tag> noteTags = pr.getTags();
            for (int i = 0; i < noteTags.size(); i++) {
                for (int j = i+1; j < noteTags.size(); j++) {
                    
                    Tag tag1 = noteTags.get(i);
                    Tag tag2 = noteTags.get(j);

                    Node n1 = graphModel.factory().newNode(tag1.getName());
                    n1.setLabel(tag1.getName());
                    nodes.add(n1);

                    Node n2 = graphModel.factory().newNode(tag2.getName());
                    n2.setLabel(tag2.getName());
                    nodes.add(n2);
                    
                    Edge e = graphModel.factory().newEdge(n1, n2, false);
                    edges.add(e);
                    //jak dodawac parametry do Edge?
                }   
            }
        }
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        undirectedGraph.addAllNodes(nodes);
        undirectedGraph.addAllEdges(edges);
    }

    private void listNewspapers(){

    }
    private void listCountries(){

    }
    private void listLanguages(){

    }
    private void listYears(){

    }
}

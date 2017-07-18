package pl.edu.agh.Analyzer.ui;

import org.gephi.graph.api.*;
import org.gephi.plugins.prestige.calculation.DomainCalculator;
import org.gephi.plugins.prestige.calculation.IndegreeCalculator;
import org.gephi.plugins.prestige.calculation.ProximityCalculator;
import org.gephi.plugins.prestige.calculation.RankCalculator;
import org.gephi.statistics.plugin.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by karolina on 17.07.17.
 */
public class ReportInput {
    public static final String[] nodesParams = {
            GraphDistance.BETWEENNESS,
            GraphDistance.CLOSENESS,
            GraphDistance.ECCENTRICITY,
            GraphDistance.HARMONIC_CLOSENESS,
            Degree.INDEGREE,
            Degree.OUTDEGREE,
            Hits.AUTHORITY,
            Hits.HUB,
            PageRank.PAGERANK,
            EigenvectorCentrality.EIGENVECTOR,
            ClusteringCoefficient.CLUSTERING_COEFF,
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

    private Map<String, Node> nodeMaxValues = new HashMap<>();
    private Map<String, Object> graphValues = new HashMap<>();
    public String paramValue;
    public String paramName;
    //pozniej zrobic z modularity, spojnymi skladowymi i ew. czyms jeszcze (clustering coefficient??)


    public void initNodeMaxValues(GraphModel graphModel){
        for (String s : nodesParams){
            nodeMaxValues.put(s, null);
        }

        Table attributes = graphModel.getNodeTable();
        //dla kazdego labela iteruje po wszystkich node'ach
        for (String col : nodesParams) {
            Column currentCol = attributes.getColumn(col);
            for (Node currentNode : graphModel.getGraph().getNodes()) {
                Node n = nodeMaxValues.get(col);
                if (n != null){
                    //sprawdzam, czy dla currenta jest lepsza wartosc
                    if ((Double)n.getAttribute(col) < (Double)currentNode.getAttribute(col)){
                        nodeMaxValues.put(col, currentNode);
                    }
                }
            }
        }
    }

    public void setGraphValue(String attrName, Object val){
        graphValues.put(attrName, val);
    }
    public Object getNodeMaxValue(String attrName){
        return nodeMaxValues.get(attrName);
    }
    public Object getGraphValue(String attrName) {
        return graphValues.get(attrName);
    }
    public List<String> getGraphParams(){
        return graphValues.keySet();
    }

}


package pl.edu.agh.Analyzer.ui;

import org.gephi.graph.api.*;
import org.gephi.statistics.plugin.*;

import java.util.List;
import java.util.Map;

/**
 * Created by karolina on 17.07.17.
 */
public class ReportInput {
    private static String[] nodesParams = {
            GraphDistance.BETWEENNESS,
            GraphDistance.CLOSENESS,
            GraphDistance.ECCENTRICITY,
            GraphDistance.HARMONIC_CLOSENESS,
            Degree.AVERAGE_DEGREE,
            Degree.INDEGREE,
            Degree.OUTDEGREE,
            Hits.AUTHORITY,
            Hits.HUB,
            PageRank.PAGERANK,
            EigenvectorCentrality.EIGENVECTOR,
            ClusteringCoefficient.CLUSTERING_COEFF,
            WeightedDegree.WINDEGREE,
            WeightedDegree.WOUTDEGREE,
    };
    private Map<String, Node> nodeMaxValues;
    private Map<String, Object> graphValues;
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

}


package chains;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class Point {
    double x;
    double y;
    double wIn;
    double wOut;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
        this.wIn = 0;
        this.wOut = 0;
    }

    public static List<Point> readPoints() throws IOException {
        List<Point> points = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get("src/verts.txt"));

        for (String line : lines) {
            String[] coordinates = line.split(" ");
            double x = Double.parseDouble(coordinates[0]);
            double y = Double.parseDouble(coordinates[1]);
            points.add(new Point(x, y));
        }

        return points;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", x, y);
    }
}

class Edge {
    Point start;
    Point end;
    double weight;
    double rotation;

    public Edge(Point start, Point end) {
        this.start = start;
        this.end = end;
        this.weight = 0;
        this.rotation = Math.atan2(end.y - start.y, end.x - start.x);
    }

    public static List<Edge> readEdges(List<Point> vertices) throws IOException {
        List<Edge> edges = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get("src/edges.txt"));

        for (String line : lines) {
            String[] indices = line.split(" ");
            int startIndex = Integer.parseInt(indices[0]);
            int endIndex = Integer.parseInt(indices[1]);
            edges.add(new Edge(vertices.get(startIndex), vertices.get(endIndex)));
        }

        return edges;
    }
}

public class ChainsMethod {

    private static double summarizeWeight(List<Edge> edges) {
        double result = 0;

        for (Edge edge : edges) {
            result += edge.weight;
        }
        return result;
    }

    private static Edge topLeftEdge(List<Edge> edges) {
        int i = 0;
        Edge result = edges.get(0);

        while (i < edges.size()) {
            if (edges.get(i).weight > 0) {
                result = edges.get(i);
                break;
            }
            i++;
        }

        return result;
    }

    private static List<Edge> sortEdges(List<Edge> edges) {
        edges.sort((edge1, edge2) -> Double.compare(edge2.rotation, edge1.rotation));
        return edges;
    }

    private static void createChain(List<Edge> chain, List<List<Edge>> orderedEdgesOut, List<Point> vertices, int n) {
        int vertex = 0;

        while (vertex != n - 1) {
            Edge newInChain = topLeftEdge(orderedEdgesOut.get(vertex));
            chain.add(newInChain);
            newInChain.weight -= 1;
            vertex = vertices.indexOf(newInChain.end);
        }
    }

    private static String findPoint(Point point, List<List<Edge>> chains, int chainsNumber) {
        for (int p = 0; p < chainsNumber; p++) {
            List<Edge> chain = chains.get(p);

            for (Edge edge : chain) {
                if (edge.start.y < point.y && point.y < edge.end.y) {
                    Point pointV = new Point(point.x - edge.start.x, point.y - edge.start.y);
                    Point edgeV = new Point(edge.end.x - edge.start.x, edge.end.y - edge.start.y);

                    if (Math.atan2(pointV.y, pointV.x) > Math.atan2(edgeV.y, edgeV.x)) {
                        return "Chains, between which chains.Point " + point + " lays: " + (p - 1) + " , " + p;
                    }
                }
            }
        }
        return "chains.Point " + point + " is outside the graph";
    }

    public static void main(String[] args) {
        try {
            List<Point> vertices = Point.readPoints();
            List<Edge> edges = Edge.readEdges(vertices);

            vertices.sort(Comparator.comparingDouble(point -> point.y));

            int size = vertices.size();

            List<List<Edge>> edgesIn = new ArrayList<>(size);
            List<List<Edge>> edgesOut = new ArrayList<>(size);

            for (int i = 0; i < size; i++) {
                edgesIn.add(new ArrayList<>());
                edgesOut.add(new ArrayList<>());
            }

            for (Edge edge : edges) {
                int from = vertices.indexOf(edge.start);
                int to = vertices.indexOf(edge.end);
                edgesOut.get(from).add(edge);
                edgesIn.get(to).add(edge);
                edge.weight = 1;
            }

            for (int i = 1; i < size - 1; i++) {
                vertices.get(i).wIn = summarizeWeight(edgesIn.get(i));
                vertices.get(i).wOut = summarizeWeight(edgesOut.get(i));
                edgesOut.set(i, sortEdges(edgesOut.get(i)));

                if (vertices.get(i).wIn > vertices.get(i).wOut) {
                    edgesOut.get(i).get(0).weight = vertices.get(i).wIn - vertices.get(i).wOut + 1;
                }
            }

            for (int i = size - 1; i > 1; i--) {
                vertices.get(i).wIn = summarizeWeight(edgesIn.get(i));
                vertices.get(i).wOut = summarizeWeight(edgesOut.get(i));
                edgesIn.set(i, sortEdges(edgesIn.get(i)));

                if (vertices.get(i).wOut > vertices.get(i).wIn) {
                    edgesIn.get(i).get(0).weight = vertices.get(i).wOut - vertices.get(i).wIn + edgesIn.get(i).get(0).weight;
                }
            }

            List<List<Edge>> chains = new ArrayList<>();
            int chainsCount = (int) summarizeWeight(edgesOut.get(0));
            List<List<Edge>> orderedEdgesOut = new ArrayList<>();

            for (List<Edge> v : edgesOut) {
                orderedEdgesOut.add(sortEdges(new ArrayList<>(v)));
            }

            for (int j = 0; j < chainsCount; j++) {
                chains.add(new ArrayList<>());
                createChain(chains.get(j), orderedEdgesOut, vertices, size);
            }

            Point pointToFind = new Point(8, 18);

            System.out.println(findPoint(pointToFind, chains, chainsCount));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
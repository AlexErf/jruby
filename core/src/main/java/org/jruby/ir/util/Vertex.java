package org.jruby.ir.util;

import java.util.*;

/**
 *
 */
public class Vertex<T> implements Comparable<Vertex<T>> {
    private DirectedGraph graph;
    private T data;
    private Set<Edge<T>> incoming = Collections.EMPTY_SET;
    private Set<Edge<T>> outgoing = Collections.EMPTY_SET;
    int id;

    public Vertex(DirectedGraph graph, T data, int id) {
        this.graph = graph;
        this.data = data;
        this.id = id;
    }

    public void addEdgeTo(Vertex destination) {
        addEdgeTo(destination, null);
    }

    public void addEdgeTo(Vertex destination, Object type) {
        Edge edge = new Edge<T>(this, destination, type);
        addOutgoingEdge(edge);
        destination.addIncomingEdge(edge);
        graph.edges().add(edge);
    }

    public void addEdgeTo(T destination) {
        addEdgeTo(destination, null);
    }

    public void addEdgeTo(T destination, Object type) {
        Vertex destinationVertex = graph.findOrCreateVertexFor(destination);

        addEdgeTo(destinationVertex, type);
    }

    public boolean removeEdgeTo(Vertex destination) {
        for (Edge edge: getOutgoingEdges()) {
            if (edge.getDestination() == destination) {
                removeOutgoingEdge(edge);
                edge.getDestination().removeIncomingEdge(edge);
                graph.edges().remove(edge);
                if(outDegree() == 0) {
                    outgoing = Collections.EMPTY_SET;
                }
                if(destination.inDegree() == 0) {
                    destination.incoming = Collections.EMPTY_SET;
                }
                return true;
            }
        }

        return false;
    }

    public void removeAllIncomingEdges() {
        for (Edge edge: getIncomingEdges()) {
            edge.getSource().removeOutgoingEdge(edge);
            graph.edges().remove(edge);
        }
        incoming = Collections.EMPTY_SET;
    }

    public void removeAllOutgoingEdges() {
        for (Edge edge: getOutgoingEdges()) {
            edge.getDestination().removeIncomingEdge(edge);
            graph.edges().remove(edge);
        }
        outgoing = Collections.EMPTY_SET;
    }

    public void removeAllEdges() {
        removeAllIncomingEdges();
        removeAllOutgoingEdges();
    }

    public int inDegree() {
        return incoming.size();
    }

    public int outDegree() {
        return outgoing.size();
    }

    public Iterable<Edge<T>> getIncomingEdgesOfType(Object type) {
        return new EdgeTypeIterable<T>(getIncomingEdges(), type);
    }

    public Iterable<Edge<T>> getIncomingEdgesNotOfType(Object type) {
        return new EdgeTypeIterable<T>(getIncomingEdges(), type, true);
    }

    public Iterable<Edge<T>> getOutgoingEdgesOfType(Object type) {
        return new EdgeTypeIterable<T>(getOutgoingEdges(), type);
    }

    public T getIncomingSourceData() {
        Edge<T> edge = getFirstEdge(getIncomingEdges().iterator());

        return edge == null ? null : edge.getSource().getData();
    }

    public T getIncomingSourceDataOfType(Object type) {
        Edge<T> edge = getFirstEdge(getIncomingEdgesOfType(type).iterator());

        return edge == null ? null : edge.getSource().getData();
    }

    public Iterable<T> getIncomingSourcesData() {
        return new DataIterable<T>(getIncomingEdges(), null, true, true);
    }

    public Iterable<T> getIncomingSourcesDataOfType(Object type) {
        return new DataIterable<T>(getIncomingEdges(), type, true, false);
    }

    public Iterable<T> getIncomingSourcesDataNotOfType(Object type) {
        return new DataIterable<T>(getIncomingEdges(), type, true, true);
    }

    public Iterable<Edge<T>> getOutgoingEdgesNotOfType(Object type) {
        return new EdgeTypeIterable<T>(getOutgoingEdges(), type, true);
    }

    public Iterable<T> getOutgoingDestinationsData() {
        return new DataIterable<T>(getOutgoingEdges(), null, false, true);
    }

    public Iterable<T> getOutgoingDestinationsDataOfType(Object type) {
        return new DataIterable<T>(getOutgoingEdges(), type, false, false);
    }

    public Iterable<T> getOutgoingDestinationsDataNotOfType(Object type) {
        return new DataIterable<T>(getOutgoingEdges(), type, false, true);
    }

    public T getOutgoingDestinationData() {
        Edge<T> edge = getFirstEdge(getOutgoingEdges().iterator());

        return edge == null ? null : edge.getDestination().getData();
    }

    public T getOutgoingDestinationDataOfType(Object type) {
        Edge<T> edge = getFirstEdge(getOutgoingEdgesOfType(type).iterator());

        return edge == null ? null : edge.getDestination().getData();
    }

    private Edge<T> getFirstEdge(Iterator<Edge<T>> iterator) {
        return iterator.hasNext() ? iterator.next() : null;
    }

    public Edge<T> getIncomingEdgeOfType(Object type) {
        return getFirstEdge(getIncomingEdgesOfType(type).iterator());
    }

    public Edge<T> getOutgoingEdgeOfType(Object type) {
        return getFirstEdge(getOutgoingEdgesOfType(type).iterator());
    }

    public Edge<T> getIncomingEdge() {
        return getFirstEdge(getIncomingEdgesNotOfType(null).iterator());
    }

    public Edge<T> getOutgoingEdge() {
        return getFirstEdge(getOutgoingEdgesNotOfType(null).iterator());
    }

    public Set<Edge<T>> getIncomingEdges() {
        return incoming;
    }

    public void addIncomingEdge(Edge<T> edge) {
        getIncomingEdgesForWrite().add(edge);
    }

    public void removeIncomingEdge(Edge<T> edge) {
        if (incoming.size() == 0) return;
        incoming.remove(edge);
        if (incoming.size() == 0) incoming = Collections.EMPTY_SET;
    }

    private Set<Edge<T>> getIncomingEdgesForWrite() {
        if (incoming == Collections.EMPTY_SET) incoming = new HashSet<Edge<T>>();

        return incoming;
    }

    public void addOutgoingEdge(Edge<T> edge) {
        getOutgoingEdgesForWrite().add(edge);
    }

    public void removeOutgoingEdge(Edge<T> edge) {
        if (outgoing.size() == 0) return;
        outgoing.remove(edge);
        if (outgoing.size() == 0) outgoing = Collections.EMPTY_SET;
    }

    public Set<Edge<T>> getOutgoingEdges() {
        return outgoing;
    }

    private Set<Edge<T>> getOutgoingEdgesForWrite() {
        if (outgoing == Collections.EMPTY_SET) outgoing = new HashSet<Edge<T>>();

        return outgoing;
    }

    public T getData() {
        return data;
    }

    public int getID() {
        return data instanceof ExplicitVertexID ? ((ExplicitVertexID) data).getID() : id;
    }

    // FIXME: This is pretty ugly...creating massive number of comparators
    @Override
    public String toString() {
        boolean found = false;
        StringBuilder buf = new StringBuilder(data.toString());

        buf.append(":");

        Set<Edge<T>> edges = getOutgoingEdges();
        int size = edges.size();

        if (size > 0) {
            found = true;
            buf.append(">[");
            List<Edge<T>> e = new ArrayList<Edge<T>>(edges);
            Collections.sort(e, new DestinationCompare());

            for (int i = 0; i < size - 1; i++) {
                buf.append(e.get(i).getDestination().getID()).append(",");
            }
            buf.append(e.get(size - 1).getDestination().getID()).append("]");
        }

        edges = getIncomingEdges();
        size = edges.size();

        if (size > 0) {
            if (found) buf.append(", ");
            buf.append("<[");
            List<Edge<T>> e = new ArrayList<Edge<T>>(edges);
            Collections.sort(e, new SourceCompare());

            for (int i = 0; i < size - 1; i++) {
                buf.append(e.get(i).getSource().getID()).append(",");
            }
            buf.append(e.get(size - 1).getSource().getID()).append("]");
        }
        buf.append("\n");

        return buf.toString();
    }

    @Override
    public int compareTo(Vertex<T> that) {
        if (getID() == that.getID()) return 0;
        if (getID() < that.getID()) return -1;
        return 1;
    }

    class SourceCompare implements Comparator<Edge<T>> {
        @Override
        public int compare(Edge<T> o1, Edge<T> o2) {
            int i1 = o1.getSource().getID();
            int i2 = o2.getSource().getID();

            if (i1 == i2) return 0;
            return i1 < i2 ? -1 : 1;
        }
    }

    class DestinationCompare implements Comparator<Edge<T>> {
        @Override
        public int compare(Edge<T> o1, Edge<T> o2) {
            int i1 = o1.getDestination().getID();
            int i2 = o2.getDestination().getID();

            if (i1 == i2) return 0;
            return i1 < i2 ? -1 : 1;
        }
    }
}

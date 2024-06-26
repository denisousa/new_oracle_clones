package edu.uci.ics.jung.samples;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EllipseVertexShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.subLayout.GraphCollapser;
import edu.uci.ics.jung.visualization.util.PredicatedParallelEdgeIndexFunction;
@SuppressWarnings ( "serial" )
public class VertexCollapseDemo extends JApplet {
    String instructions =
        "<html>Use the mouse to select multiple vertices" +
        "<p>either by dragging a region, or by shift-clicking" +
        "<p>on multiple vertices." +
        "<p>After you select vertices, use the Collapse button" +
        "<p>to combine them into a single vertex." +
        "<p>Select a 'collapsed' vertex and use the Expand button" +
        "<p>to restore the collapsed vertices." +
        "<p>The Restore button will restore the original graph." +
        "<p>If you select 2 (and only 2) vertices, then press" +
        "<p>the Compress Edges button, parallel edges between" +
        "<p>those two vertices will no longer be expanded." +
        "<p>If you select 2 (and only 2) vertices, then press" +
        "<p>the Expand Edges button, parallel edges between" +
        "<p>those two vertices will be expanded." +
        "<p>You can drag the vertices with the mouse." +
        "<p>Use the 'Picking'/'Transforming' combo-box to switch" +
        "<p>between picking and transforming mode.</html>";
    Graph graph;
    VisualizationViewer vv;
    Layout layout;
    GraphCollapser collapser;
    public VertexCollapseDemo() {
        graph =
            TestGraphs.getOneComponentGraph();
        collapser = new GraphCollapser ( graph );
        layout = new FRLayout ( graph );
        Dimension preferredSize = new Dimension ( 400, 400 );
        final VisualizationModel visualizationModel =
            new DefaultVisualizationModel ( layout, preferredSize );
        vv =  new VisualizationViewer ( visualizationModel, preferredSize );
        vv.getRenderContext().setVertexShapeTransformer ( new ClusterVertexShapeFunction() );
        final PredicatedParallelEdgeIndexFunction eif = PredicatedParallelEdgeIndexFunction.getInstance();
        final Set exclusions = new HashSet();
        eif.setPredicate ( new Predicate() {
            public boolean evaluate ( Object e ) {
                return exclusions.contains ( e );
            }
        } );
        vv.getRenderContext().setParallelEdgeIndexFunction ( eif );
        vv.setBackground ( Color.white );
        vv.setVertexToolTipTransformer ( new ToStringLabeller() {
            @Override
            public String transform ( Object v ) {
                if ( v instanceof Graph ) {
                    return ( ( Graph ) v ).getVertices().toString();
                }
                return super.transform ( v );
            }
        } );
        final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
        vv.setGraphMouse ( graphMouse );
        Container content = getContentPane();
        GraphZoomScrollPane gzsp = new GraphZoomScrollPane ( vv );
        content.add ( gzsp );
        JComboBox modeBox = graphMouse.getModeComboBox();
        modeBox.addItemListener ( graphMouse.getModeListener() );
        graphMouse.setMode ( ModalGraphMouse.Mode.PICKING );
        final ScalingControl scaler = new CrossoverScalingControl();
        JButton plus = new JButton ( "+" );
        plus.addActionListener ( new ActionListener() {
            public void actionPerformed ( ActionEvent e ) {
                scaler.scale ( vv, 1.1f, vv.getCenter() );
            }
        } );
        JButton minus = new JButton ( "-" );
        minus.addActionListener ( new ActionListener() {
            public void actionPerformed ( ActionEvent e ) {
                scaler.scale ( vv, 1 / 1.1f, vv.getCenter() );
            }
        } );
        JButton collapse = new JButton ( "Collapse" );
        collapse.addActionListener ( new ActionListener() {
            public void actionPerformed ( ActionEvent e ) {
                Collection picked = new HashSet ( vv.getPickedVertexState().getPicked() );
                if ( picked.size() > 1 ) {
                    Graph inGraph = layout.getGraph();
                    Graph clusterGraph = collapser.getClusterGraph ( inGraph, picked );
                    Graph g = collapser.collapse ( layout.getGraph(), clusterGraph );
                    double sumx = 0;
                    double sumy = 0;
                    for ( Object v : picked ) {
                        Point2D p = ( Point2D ) layout.transform ( v );
                        sumx += p.getX();
                        sumy += p.getY();
                    }
                    Point2D cp = new Point2D.Double ( sumx / picked.size(), sumy / picked.size() );
                    vv.getRenderContext().getParallelEdgeIndexFunction().reset();
                    layout.setGraph ( g );
                    layout.setLocation ( clusterGraph, cp );
                    vv.getPickedVertexState().clear();
                    vv.repaint();
                }
            }
        } );
        JButton compressEdges = new JButton ( "Compress Edges" );
        compressEdges.addActionListener ( new ActionListener() {
            public void actionPerformed ( ActionEvent e ) {
                Collection picked = vv.getPickedVertexState().getPicked();
                if ( picked.size() == 2 ) {
                    Pair pair = new Pair ( picked );
                    Graph graph = layout.getGraph();
                    Collection edges = new HashSet ( graph.getIncidentEdges ( pair.getFirst() ) );
                    edges.retainAll ( graph.getIncidentEdges ( pair.getSecond() ) );
                    exclusions.addAll ( edges );
                    vv.repaint();
                }
            }
        } );
        JButton expandEdges = new JButton ( "Expand Edges" );
        expandEdges.addActionListener ( new ActionListener() {
            public void actionPerformed ( ActionEvent e ) {
                Collection picked = vv.getPickedVertexState().getPicked();
                if ( picked.size() == 2 ) {
                    Pair pair = new Pair ( picked );
                    Graph graph = layout.getGraph();
                    Collection edges = new HashSet ( graph.getIncidentEdges ( pair.getFirst() ) );
                    edges.retainAll ( graph.getIncidentEdges ( pair.getSecond() ) );
                    exclusions.removeAll ( edges );
                    vv.repaint();
                }
            }
        } );
        JButton expand = new JButton ( "Expand" );
        expand.addActionListener ( new ActionListener() {
            public void actionPerformed ( ActionEvent e ) {
                Collection picked = new HashSet ( vv.getPickedVertexState().getPicked() );
                for ( Object v : picked ) {
                    if ( v instanceof Graph ) {
                        Graph g = collapser.expand ( layout.getGraph(), ( Graph ) v );
                        vv.getRenderContext().getParallelEdgeIndexFunction().reset();
                        layout.setGraph ( g );
                    }
                    vv.getPickedVertexState().clear();
                    vv.repaint();
                }
            }
        } );
        JButton reset = new JButton ( "Reset" );
        reset.addActionListener ( new ActionListener() {
            public void actionPerformed ( ActionEvent e ) {
                layout.setGraph ( graph );
                exclusions.clear();
                vv.repaint();
            }
        } );
        JButton help = new JButton ( "Help" );
        help.addActionListener ( new ActionListener() {
            public void actionPerformed ( ActionEvent e ) {
                JOptionPane.showMessageDialog ( ( JComponent ) e.getSource(), instructions, "Help", JOptionPane.PLAIN_MESSAGE );
            }
        } );
        JPanel controls = new JPanel();
        JPanel zoomControls = new JPanel ( new GridLayout ( 2, 1 ) );
        zoomControls.setBorder ( BorderFactory.createTitledBorder ( "Zoom" ) );
        zoomControls.add ( plus );
        zoomControls.add ( minus );
        controls.add ( zoomControls );
        JPanel collapseControls = new JPanel ( new GridLayout ( 3, 1 ) );
        collapseControls.setBorder ( BorderFactory.createTitledBorder ( "Picked" ) );
        collapseControls.add ( collapse );
        collapseControls.add ( expand );
        collapseControls.add ( compressEdges );
        collapseControls.add ( expandEdges );
        collapseControls.add ( reset );
        controls.add ( collapseControls );
        controls.add ( modeBox );
        controls.add ( help );
        content.add ( controls, BorderLayout.SOUTH );
    }
    class ClusterVertexShapeFunction<V> extends EllipseVertexShapeTransformer<V> {
        ClusterVertexShapeFunction() {
            setSizeTransformer ( new ClusterVertexSizeFunction<V> ( 20 ) );
        }
        @Override
        public Shape transform ( V v ) {
            if ( v instanceof Graph ) {
                int size = ( ( Graph ) v ).getVertexCount();
                if ( size < 8 ) {
                    int sides = Math.max ( size, 3 );
                    return factory.getRegularPolygon ( v, sides );
                } else {
                    return factory.getRegularStar ( v, size );
                }
            }
            return super.transform ( v );
        }
    }
    class ClusterVertexSizeFunction<V> implements Transformer<V, Integer> {
        int size;
        public ClusterVertexSizeFunction ( Integer size ) {
            this.size = size;
        }
        public Integer transform ( V v ) {
            if ( v instanceof Graph ) {
                return 30;
            }
            return size;
        }
    }
    public static void main ( String[] args ) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation ( JFrame.EXIT_ON_CLOSE );
        f.getContentPane().add ( new VertexCollapseDemo() );
        f.pack();
        f.setVisible ( true );
    }
}

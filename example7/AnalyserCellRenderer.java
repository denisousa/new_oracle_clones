package org.netbeans.modules.java.stackanalyzer;
import java.awt.Color;
import java.awt.Component;
import java.util.regex.Matcher;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.modules.java.stackanalyzer.StackLineAnalyser.Link;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
class AnalyserCellRenderer extends DefaultListCellRenderer {
    private Color               foreground;
    public AnalyserCellRenderer () {
        FontColorSettings fontColorSettings = MimeLookup.getLookup ( MimePath.EMPTY ).lookup ( FontColorSettings.class );
        AttributeSet attributeSet = fontColorSettings.getFontColors ( "hyperlinks" );
        foreground = ( Color ) attributeSet.getAttribute ( StyleConstants.Foreground );
    }
    private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder ( 1, 1, 1, 1 );
    private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder ( 1, 1, 1, 1 );
    private Border getNoFocusBorder() {
        Border border = UIManager.getBorder ( "List.cellNoFocusBorder" );
        if ( System.getSecurityManager() != null ) {
            if ( border != null ) {
                return border;
            }
            return SAFE_NO_FOCUS_BORDER;
        } else {
            if ( border != null &&
                    ( noFocusBorder == null ||
                      noFocusBorder == DEFAULT_NO_FOCUS_BORDER ) ) {
                return border;
            }
            return noFocusBorder;
        }
    }
    @Override
    public Component getListCellRendererComponent (
        JList                   list,
        Object                  value,
        int                     index,
        boolean                 isSelected,
        boolean                 cellHasFocus
    ) {
        setComponentOrientation ( list.getComponentOrientation () );
        Color bg = null;
        Color fg = null;
        JList.DropLocation dropLocation = list.getDropLocation ();
        if ( dropLocation != null && !dropLocation.isInsert () && dropLocation.getIndex () == index ) {
            bg = UIManager.getColor ( "List.dropCellBackground" );
            fg = UIManager.getColor ( "List.dropCellForeground" );
            isSelected = true;
        }
        String line = ( String ) value;
        Link link = StackLineAnalyser.analyse ( line );
        if ( isSelected ) {
            setBackground ( bg == null ? list.getSelectionBackground () : bg );
            setForeground ( fg == null ? list.getSelectionForeground() : fg );
        } else {
            setBackground ( list.getBackground () );
            setForeground ( list.getForeground () );
        }
        if ( link != null ) {
            StringBuilder sb = new StringBuilder ();
            sb.append ( "<html>" );
            sb.append ( line.substring ( 0, link.getStartOffset () ) );
            sb.append ( "<a href=\"\">" );
            sb.append ( line.substring ( link.getStartOffset (), link.getEndOffset () ) );
            sb.append ( "</a>" );
            sb.append ( line.substring ( link.getEndOffset () ) );
            sb.append ( "</html>" );
            setText ( sb.toString () );
        } else {
            setText ( line.trim () );
        }
        setEnabled ( list.isEnabled () );
        Border border = null;
        if ( cellHasFocus ) {
            if ( isSelected ) {
                border = UIManager.getBorder ( "List.focusSelectedCellHighlightBorder" );
            }
            if ( border == null ) {
                border = UIManager.getBorder ( "List.focusCellHighlightBorder" );
            }
        } else {
            border = getNoFocusBorder ();
        }
        setBorder ( border );
        return this;
    }
}

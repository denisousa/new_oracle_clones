package org.netbeans.modules.spellchecker.options;
import java.awt.Component;
import java.awt.Color;
import java.awt.Rectangle;
import java.io.Serializable;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
public class CheckBoxRenderrer extends JCheckBox implements ListCellRenderer, Serializable {
    private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder ( 1, 1, 1, 1 );
    private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder ( 1, 1, 1, 1 );
    protected static Border     noFocusBorder = DEFAULT_NO_FOCUS_BORDER;
    public CheckBoxRenderrer () {
        super ();
        setOpaque ( true );
        setBorder ( getNoFocusBorder () );
        setName ( "List.cellRenderer" );
    }
    private Border getNoFocusBorder () {
        Border border = UIManager.getBorder ( "List.cellNoFocusBorder" );
        if ( System.getSecurityManager () != null ) {
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
        if ( isSelected ) {
            setBackground ( bg == null ? list.getSelectionBackground () : bg );
            setForeground ( fg == null ? list.getSelectionForeground () : fg );
        } else {
            setBackground ( list.getBackground () );
            setForeground ( list.getForeground () );
        }
        String name = ( String ) value;
        setText ( name.substring ( 1 ) );
        setSelected ( name.charAt ( 0 ) == '+' );
        setEnabled ( list.isEnabled () );
        setFont ( list.getFont () );
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
    @Override
    public boolean isOpaque () {
        Color back = getBackground ();
        Component p = getParent ();
        if ( p != null ) {
            p = p.getParent ();
        }
        boolean colorMatch = ( back != null ) && ( p != null ) &&
                             back.equals ( p.getBackground () ) &&
                             p.isOpaque ();
        return !colorMatch && super.isOpaque ();
    }
    @Override
    public void validate () {
    }
    @Override
    public void invalidate () {
    }
    @Override
    public void repaint () {
    }
    @Override
    public void revalidate () {
    }
    @Override
    public void repaint ( long tm, int x, int y, int width, int height ) {
    }
    @Override
    public void repaint ( Rectangle r ) {
    }
    @Override
    protected void firePropertyChange ( String propertyName, Object oldValue, Object newValue ) {
        if ( propertyName == "text" || ( ( propertyName == "font" || propertyName == "foreground" ) &&
                                         oldValue != newValue &&
                                         getClientProperty ( javax.swing.plaf.basic.BasicHTML.propertyKey ) != null )
           ) {
            super.firePropertyChange ( propertyName, oldValue, newValue );
        }
    }
    @Override
    public void firePropertyChange ( String propertyName, byte oldValue, byte newValue ) {
    }
    @Override
    public void firePropertyChange ( String propertyName, char oldValue, char newValue ) {
    }
    @Override
    public void firePropertyChange ( String propertyName, short oldValue, short newValue ) {
    }
    @Override
    public void firePropertyChange ( String propertyName, int oldValue, int newValue ) {
    }
    @Override
    public void firePropertyChange ( String propertyName, long oldValue, long newValue ) {
    }
    @Override
    public void firePropertyChange ( String propertyName, float oldValue, float newValue ) {
    }
    @Override
    public void firePropertyChange ( String propertyName, double oldValue, double newValue ) {
    }
    @Override
    public void firePropertyChange ( String propertyName, boolean oldValue, boolean newValue ) {
    }
}

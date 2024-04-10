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
        @SuppressWarnings ( "unchecked" )
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
package org.compiere.translate;
public class AmtInWords_EN implements AmtInWords {
    public AmtInWords_EN () {
        super ();
    }	
    private static final String[]	majorNames	= {
        "",
        "Thousand-",
        "Million-",
        "Billion-",
        "Trillion-",
        "Quadrillion-",
        "Quintillion-"
    };
    private static final String[]	tensNames	= {
        "",
        "Ten",
        "Twenty",
        "Thirty",
        "Fourty",
        "Fifty",
        "Sixty",
        "Seventy",
        "Eighty",
        "Ninety"
    };
    private static final String[]	numNames	= {
        "",
        "One",
        "Two",
        "Three",
        "Four",
        "Five",
        "Six",
        "Seven",
        "Eight",
        "Nine",
        "Ten",
        "Eleven",
        "Twelve",
        "Thirteen",
        "Fourteen",
        "Fifteen",
        "Sixteen",
        "Seventeen",
        "Eighteen",
        "Nineteen"
    };
    private String convertLessThanOneThousand ( int number ) {
        String soFar;
        if ( number % 100 < 20 ) {
            soFar = numNames[number % 100];
            number /= 100;
        } else {
            soFar = numNames[number % 10];
            number /= 10;
            soFar = tensNames[number % 10] + soFar;
            number /= 10;
        }
        if ( number == 0 ) {
            return soFar;
        }
        return numNames[number] + "Hundred-" + soFar;
    }	
    private String convert ( long number ) {
        if ( number == 0 ) {
            return "Zero";
        }
        String prefix = "";
        if ( number < 0 ) {
            number = -number;
            prefix = "Negative ";
        }
        String soFar = "";
        int place = 0;
        do {
            long n = number % 1000;
            if ( n != 0 ) {
                String s = convertLessThanOneThousand ( ( int ) n );
                soFar = s + majorNames[place] + soFar;
            }
            place++;
            number /= 1000;
        } while ( number > 0 );
        return ( prefix + soFar ).trim ();
    }	
    public String getAmtInWords ( String amount ) throws Exception {
        if ( amount == null ) {
            return amount;
        }
        StringBuffer sb = new StringBuffer ();
        int pos = amount.lastIndexOf ( '.' );
        int pos2 = amount.lastIndexOf ( ',' );
        if ( pos2 > pos ) {
            pos = pos2;
        }
        String oldamt = amount;
        amount = amount.replaceAll ( ",", "" );
        int newpos = amount.lastIndexOf ( '.' );
        long dollars = Long.parseLong ( amount.substring ( 0, newpos ) );
        sb.append ( convert ( dollars ) );
        for ( int i = 0; i < oldamt.length (); i++ ) {
            if ( pos == i ) { 
                String cents = oldamt.substring ( i + 1 );
                sb.append ( ' ' ).append ( cents ).append ( "/100" );
                break;
            }
        }
        return sb.toString ();
    }	
    private void print ( String amt ) {
        try {
            System.out.println ( amt + " = " + getAmtInWords ( amt ) );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }	
    public static void main ( String[] args ) {
        AmtInWords_EN aiw = new AmtInWords_EN();
        aiw.print ( "0.23" );
        aiw.print ( "1.23" );
        aiw.print ( "12.345" );
        aiw.print ( "123.45" );
        aiw.print ( "1234.56" );
        aiw.print ( "12345.78" );
        aiw.print ( "123457.89" );
        aiw.print ( "1,234,578.90" );
    }	
}	

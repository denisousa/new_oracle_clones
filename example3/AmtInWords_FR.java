package org.compiere.translate;
public class AmtInWords_FR implements AmtInWords {
    public AmtInWords_FR () {
        super ();
    }	
    private static final String[]	majorNames	= {
        "",
        " mille",
        " million",
        " milliard",
        " trillion",
        " quadrillion",
        " quintillion"
    };
    private static final String[]	tensNames	= {
        "",
        " dix",
        " vingt",
        " trente",
        " quarante",
        " cinquante",
        " soixante",
        " soixante-dix",
        " quatre-vingt",
        " quatre-vingt-dix"
    };
    private static final String[]	numNames	= {
        "",
        " un",
        " deux",
        " trois",
        " quatre",
        " cinq",
        " six",
        " sept",
        " huit",
        " neuf",
        " dix",
        " onze",
        " douze",
        " treize",
        " quatorze",
        " quinze",
        " seize",
        " dix-sept",
        " dix-huit",
        " dix-neuf"
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
        if ( number == 1 )
        {
            return " cent" + soFar;
        } else {
            return numNames[number] + " cent" + soFar;
        }
    }	
    private String convert ( int number ) {
        if ( number == 0 ) {
            return "zero";
        }
        String prefix = "";
        if ( number < 0 ) {
            number = -number;
            prefix = "moins";
        }
        String soFar = "";
        int place = 0;
        boolean pluralPossible = true;
        boolean pluralForm = false;
        do {
            int n = number % 1000;
            if ( n != 0 ) {
                String s = convertLessThanOneThousand ( n );
                if ( s.trim ().equals ( "un" ) && place == 1 ) {
                    soFar = majorNames[place] + soFar;
                } else {
                    if ( place == 0 ) {
                        if ( s.trim ().endsWith ( "cent" )
                                && !s.trim ().startsWith ( "cent" ) ) {
                            pluralForm = true;
                        } else {
                            pluralPossible = false;
                        }
                    }
                    if ( place > 0 && pluralPossible ) {
                        if ( !s.trim ().startsWith ( "un" ) ) {
                            pluralForm = true;
                        } else {
                            pluralPossible = false;
                        }
                    }
                    soFar = s + majorNames[place] + soFar;
                }
            }
            place++;
            number /= 1000;
        } while ( number > 0 );
        String result = ( prefix + soFar ).trim ();
        return ( pluralForm ? result + "s" : result );
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
        int pesos = Integer.parseInt ( amount.substring ( 0, newpos ) );
        sb.append ( convert ( pesos ) );
        for ( int i = 0; i < oldamt.length (); i++ ) {
            if ( pos == i ) { 
                String cents = oldamt.substring ( i + 1 );
                sb.append ( ' ' ).append ( cents ).append ( "/100" );
                break;
            }
        }
        return sb.toString ();
    }	
}	

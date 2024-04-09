    private static String convertLessThanOneThousand ( int number ) {
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
        return numNames[number] + " hundred" + soFar;
    }
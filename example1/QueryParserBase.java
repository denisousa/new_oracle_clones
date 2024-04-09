package org.apache.lucene.queryparser.classic;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.util.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.queryparser.flexible.standard.CommonQueryParserConfiguration;
import org.apache.lucene.search.*;
import org.apache.lucene.search.BooleanQuery.TooManyClauses;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
public abstract class QueryParserBase implements CommonQueryParserConfiguration {
    public static class MethodRemovedUseAnother extends Throwable {}
    static final int CONJ_NONE   = 0;
    static final int CONJ_AND    = 1;
    static final int CONJ_OR     = 2;
    static final int MOD_NONE    = 0;
    static final int MOD_NOT     = 10;
    static final int MOD_REQ     = 11;
    public static final Operator AND_OPERATOR = Operator.AND;
    public static final Operator OR_OPERATOR = Operator.OR;
    Operator operator = OR_OPERATOR;
    boolean lowercaseExpandedTerms = true;
    MultiTermQuery.RewriteMethod multiTermRewriteMethod = MultiTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT;
    boolean allowLeadingWildcard = false;
    boolean enablePositionIncrements = true;
    Analyzer analyzer;
    String field;
    int phraseSlop = 0;
    float fuzzyMinSim = FuzzyQuery.defaultMinSimilarity;
    int fuzzyPrefixLength = FuzzyQuery.defaultPrefixLength;
    Locale locale = Locale.getDefault();
    TimeZone timeZone = TimeZone.getDefault();
    DateTools.Resolution dateResolution = null;
    Map<String, DateTools.Resolution> fieldToDateResolution = null;
    boolean analyzeRangeTerms = false;
    boolean autoGeneratePhraseQueries;
    protected QueryParserBase() {
    }
    public void init ( Version matchVersion, String f, Analyzer a ) {
        analyzer = a;
        field = f;
        if ( matchVersion.onOrAfter ( Version.LUCENE_31 ) ) {
            setAutoGeneratePhraseQueries ( false );
        } else {
            setAutoGeneratePhraseQueries ( true );
        }
    }
    public abstract void ReInit ( CharStream stream );
    public abstract Query TopLevelQuery ( String field ) throws ParseException;
    public Query parse ( String query ) throws ParseException {
        ReInit ( new FastCharStream ( new StringReader ( query ) ) );
        try {
            Query res = TopLevelQuery ( field );
            return res != null ? res : newBooleanQuery ( false );
        } catch ( ParseException tme ) {
            ParseException e = new ParseException ( "Cannot parse '" + query + "': " + tme.getMessage() );
            e.initCause ( tme );
            throw e;
        } catch ( TokenMgrError tme ) {
            ParseException e = new ParseException ( "Cannot parse '" + query + "': " + tme.getMessage() );
            e.initCause ( tme );
            throw e;
        } catch ( BooleanQuery.TooManyClauses tmc ) {
            ParseException e = new ParseException ( "Cannot parse '" + query + "': too many boolean clauses" );
            e.initCause ( tmc );
            throw e;
        }
    }
    @Override
    public Analyzer getAnalyzer() {
        return analyzer;
    }
    public String getField() {
        return field;
    }
    public final boolean getAutoGeneratePhraseQueries() {
        return autoGeneratePhraseQueries;
    }
    public final void setAutoGeneratePhraseQueries ( boolean value ) {
        this.autoGeneratePhraseQueries = value;
    }
    @Override
    public float getFuzzyMinSim() {
        return fuzzyMinSim;
    }
    @Override
    public void setFuzzyMinSim ( float fuzzyMinSim ) {
        this.fuzzyMinSim = fuzzyMinSim;
    }
    @Override
    public int getFuzzyPrefixLength() {
        return fuzzyPrefixLength;
    }
    @Override
    public void setFuzzyPrefixLength ( int fuzzyPrefixLength ) {
        this.fuzzyPrefixLength = fuzzyPrefixLength;
    }
    @Override
    public void setPhraseSlop ( int phraseSlop ) {
        this.phraseSlop = phraseSlop;
    }
    @Override
    public int getPhraseSlop() {
        return phraseSlop;
    }
    @Override
    public void setAllowLeadingWildcard ( boolean allowLeadingWildcard ) {
        this.allowLeadingWildcard = allowLeadingWildcard;
    }
    @Override
    public boolean getAllowLeadingWildcard() {
        return allowLeadingWildcard;
    }
    @Override
    public void setEnablePositionIncrements ( boolean enable ) {
        this.enablePositionIncrements = enable;
    }
    @Override
    public boolean getEnablePositionIncrements() {
        return enablePositionIncrements;
    }
    public void setDefaultOperator ( Operator op ) {
        this.operator = op;
    }
    public Operator getDefaultOperator() {
        return operator;
    }
    @Override
    public void setLowercaseExpandedTerms ( boolean lowercaseExpandedTerms ) {
        this.lowercaseExpandedTerms = lowercaseExpandedTerms;
    }
    @Override
    public boolean getLowercaseExpandedTerms() {
        return lowercaseExpandedTerms;
    }
    @Override
    public void setMultiTermRewriteMethod ( MultiTermQuery.RewriteMethod method ) {
        multiTermRewriteMethod = method;
    }
    @Override
    public MultiTermQuery.RewriteMethod getMultiTermRewriteMethod() {
        return multiTermRewriteMethod;
    }
    @Override
    public void setLocale ( Locale locale ) {
        this.locale = locale;
    }
    @Override
    public Locale getLocale() {
        return locale;
    }
    @Override
    public void setTimeZone ( TimeZone timeZone ) {
        this.timeZone = timeZone;
    }
    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }
    @Override
    public void setDateResolution ( DateTools.Resolution dateResolution ) {
        this.dateResolution = dateResolution;
    }
    public void setDateResolution ( String fieldName, DateTools.Resolution dateResolution ) {
        if ( fieldName == null ) {
            throw new IllegalArgumentException ( "Field cannot be null." );
        }
        if ( fieldToDateResolution == null ) {
            fieldToDateResolution = new HashMap<String, DateTools.Resolution>();
        }
        fieldToDateResolution.put ( fieldName, dateResolution );
    }
    public DateTools.Resolution getDateResolution ( String fieldName ) {
        if ( fieldName == null ) {
            throw new IllegalArgumentException ( "Field cannot be null." );
        }
        if ( fieldToDateResolution == null ) {
            return this.dateResolution;
        }
        DateTools.Resolution resolution = fieldToDateResolution.get ( fieldName );
        if ( resolution == null ) {
            resolution = this.dateResolution;
        }
        return resolution;
    }
    public void setAnalyzeRangeTerms ( boolean analyzeRangeTerms ) {
        this.analyzeRangeTerms = analyzeRangeTerms;
    }
    public boolean getAnalyzeRangeTerms() {
        return analyzeRangeTerms;
    }
    protected void addClause ( List<BooleanClause> clauses, int conj, int mods, Query q ) {
        boolean required, prohibited;
        if ( clauses.size() > 0 && conj == CONJ_AND ) {
            BooleanClause c = clauses.get ( clauses.size() - 1 );
            if ( !c.isProhibited() ) {
                c.setOccur ( BooleanClause.Occur.MUST );
            }
        }
        if ( clauses.size() > 0 && operator == AND_OPERATOR && conj == CONJ_OR ) {
            BooleanClause c = clauses.get ( clauses.size() - 1 );
            if ( !c.isProhibited() ) {
                c.setOccur ( BooleanClause.Occur.SHOULD );
            }
        }
        if ( q == null ) {
            return;
        }
        if ( operator == OR_OPERATOR ) {
            prohibited = ( mods == MOD_NOT );
            required = ( mods == MOD_REQ );
            if ( conj == CONJ_AND && !prohibited ) {
                required = true;
            }
        } else {
            prohibited = ( mods == MOD_NOT );
            required   = ( !prohibited && conj != CONJ_OR );
        }
        if ( required && !prohibited ) {
            clauses.add ( newBooleanClause ( q, BooleanClause.Occur.MUST ) );
        } else if ( !required && !prohibited ) {
            clauses.add ( newBooleanClause ( q, BooleanClause.Occur.SHOULD ) );
        } else if ( !required && prohibited ) {
            clauses.add ( newBooleanClause ( q, BooleanClause.Occur.MUST_NOT ) );
        } else {
            throw new RuntimeException ( "Clause cannot be both required and prohibited" );
        }
    }
    protected Query getFieldQuery ( String field, String queryText, boolean quoted ) throws ParseException {
        return newFieldQuery ( analyzer, field, queryText, quoted );
    }
    protected Query newFieldQuery ( Analyzer analyzer, String field, String queryText, boolean quoted )  throws ParseException {
        TokenStream source;
        try {
            source = analyzer.tokenStream ( field, new StringReader ( queryText ) );
            source.reset();
        } catch ( IOException e ) {
            ParseException p = new ParseException ( "Unable to initialize TokenStream to analyze query text" );
            p.initCause ( e );
            throw p;
        }
        CachingTokenFilter buffer = new CachingTokenFilter ( source );
        TermToBytesRefAttribute termAtt = null;
        PositionIncrementAttribute posIncrAtt = null;
        int numTokens = 0;
        buffer.reset();
        if ( buffer.hasAttribute ( TermToBytesRefAttribute.class ) ) {
            termAtt = buffer.getAttribute ( TermToBytesRefAttribute.class );
        }
        if ( buffer.hasAttribute ( PositionIncrementAttribute.class ) ) {
            posIncrAtt = buffer.getAttribute ( PositionIncrementAttribute.class );
        }
        int positionCount = 0;
        boolean severalTokensAtSamePosition = false;
        boolean hasMoreTokens = false;
        if ( termAtt != null ) {
            try {
                hasMoreTokens = buffer.incrementToken();
                while ( hasMoreTokens ) {
                    numTokens++;
                    int positionIncrement = ( posIncrAtt != null ) ? posIncrAtt.getPositionIncrement() : 1;
                    if ( positionIncrement != 0 ) {
                        positionCount += positionIncrement;
                    } else {
                        severalTokensAtSamePosition = true;
                    }
                    hasMoreTokens = buffer.incrementToken();
                }
            } catch ( IOException e ) {
            }
        }
        try {
            buffer.reset();
            source.close();
        } catch ( IOException e ) {
            ParseException p = new ParseException ( "Cannot close TokenStream analyzing query text" );
            p.initCause ( e );
            throw p;
        }
        BytesRef bytes = termAtt == null ? null : termAtt.getBytesRef();
        if ( numTokens == 0 ) {
            return null;
        } else if ( numTokens == 1 ) {
            try {
                boolean hasNext = buffer.incrementToken();
                assert hasNext == true;
                termAtt.fillBytesRef();
            } catch ( IOException e ) {
            }
            return newTermQuery ( new Term ( field, BytesRef.deepCopyOf ( bytes ) ) );
        } else {
            if ( severalTokensAtSamePosition || ( !quoted && !autoGeneratePhraseQueries ) ) {
                if ( positionCount == 1 || ( !quoted && !autoGeneratePhraseQueries ) ) {
                    BooleanQuery q = newBooleanQuery ( positionCount == 1 );
                    BooleanClause.Occur occur = positionCount > 1 && operator == AND_OPERATOR ?
                                                BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD;
                    for ( int i = 0; i < numTokens; i++ ) {
                        try {
                            boolean hasNext = buffer.incrementToken();
                            assert hasNext == true;
                            termAtt.fillBytesRef();
                        } catch ( IOException e ) {
                        }
                        Query currentQuery = newTermQuery (
                                                 new Term ( field, BytesRef.deepCopyOf ( bytes ) ) );
                        q.add ( currentQuery, occur );
                    }
                    return q;
                } else {
                    MultiPhraseQuery mpq = newMultiPhraseQuery();
                    mpq.setSlop ( phraseSlop );
                    List<Term> multiTerms = new ArrayList<Term>();
                    int position = -1;
                    for ( int i = 0; i < numTokens; i++ ) {
                        int positionIncrement = 1;
                        try {
                            boolean hasNext = buffer.incrementToken();
                            assert hasNext == true;
                            termAtt.fillBytesRef();
                            if ( posIncrAtt != null ) {
                                positionIncrement = posIncrAtt.getPositionIncrement();
                            }
                        } catch ( IOException e ) {
                        }
                        if ( positionIncrement > 0 && multiTerms.size() > 0 ) {
                            if ( enablePositionIncrements ) {
                                mpq.add ( multiTerms.toArray ( new Term[0] ), position );
                            } else {
                                mpq.add ( multiTerms.toArray ( new Term[0] ) );
                            }
                            multiTerms.clear();
                        }
                        position += positionIncrement;
                        multiTerms.add ( new Term ( field, BytesRef.deepCopyOf ( bytes ) ) );
                    }
                    if ( enablePositionIncrements ) {
                        mpq.add ( multiTerms.toArray ( new Term[0] ), position );
                    } else {
                        mpq.add ( multiTerms.toArray ( new Term[0] ) );
                    }
                    return mpq;
                }
            } else {
                PhraseQuery pq = newPhraseQuery();
                pq.setSlop ( phraseSlop );
                int position = -1;
                for ( int i = 0; i < numTokens; i++ ) {
                    int positionIncrement = 1;
                    try {
                        boolean hasNext = buffer.incrementToken();
                        assert hasNext == true;
                        termAtt.fillBytesRef();
                        if ( posIncrAtt != null ) {
                            positionIncrement = posIncrAtt.getPositionIncrement();
                        }
                    } catch ( IOException e ) {
                    }
                    if ( enablePositionIncrements ) {
                        position += positionIncrement;
                        pq.add ( new Term ( field, BytesRef.deepCopyOf ( bytes ) ), position );
                    } else {
                        pq.add ( new Term ( field, BytesRef.deepCopyOf ( bytes ) ) );
                    }
                }
                return pq;
            }
        }
    }
    protected Query getFieldQuery ( String field, String queryText, int slop )
    throws ParseException {
        Query query = getFieldQuery ( field, queryText, true );
        if ( query instanceof PhraseQuery ) {
            ( ( PhraseQuery ) query ).setSlop ( slop );
        }
        if ( query instanceof MultiPhraseQuery ) {
            ( ( MultiPhraseQuery ) query ).setSlop ( slop );
        }
        return query;
    }
    protected Query getRangeQuery ( String field,
                                    String part1,
                                    String part2,
                                    boolean startInclusive,
                                    boolean endInclusive ) throws ParseException {
        if ( lowercaseExpandedTerms ) {
            part1 = part1 == null ? null : part1.toLowerCase ( locale );
            part2 = part2 == null ? null : part2.toLowerCase ( locale );
        }
        DateFormat df = DateFormat.getDateInstance ( DateFormat.SHORT, locale );
        df.setLenient ( true );
        DateTools.Resolution resolution = getDateResolution ( field );
        try {
            part1 = DateTools.dateToString ( df.parse ( part1 ), resolution );
        } catch ( Exception e ) { }
        try {
            Date d2 = df.parse ( part2 );
            if ( endInclusive ) {
                Calendar cal = Calendar.getInstance ( timeZone, locale );
                cal.setTime ( d2 );
                cal.set ( Calendar.HOUR_OF_DAY, 23 );
                cal.set ( Calendar.MINUTE, 59 );
                cal.set ( Calendar.SECOND, 59 );
                cal.set ( Calendar.MILLISECOND, 999 );
                d2 = cal.getTime();
            }
            part2 = DateTools.dateToString ( d2, resolution );
        } catch ( Exception e ) { }
        return newRangeQuery ( field, part1, part2, startInclusive, endInclusive );
    }
    protected BooleanQuery newBooleanQuery ( boolean disableCoord ) {
        return new BooleanQuery ( disableCoord );
    }
    protected BooleanClause newBooleanClause ( Query q, BooleanClause.Occur occur ) {
        return new BooleanClause ( q, occur );
    }
    protected Query newTermQuery ( Term term ) {
        return new TermQuery ( term );
    }
    protected PhraseQuery newPhraseQuery() {
        return new PhraseQuery();
    }
    protected MultiPhraseQuery newMultiPhraseQuery() {
        return new MultiPhraseQuery();
    }
    protected Query newPrefixQuery ( Term prefix ) {
        PrefixQuery query = new PrefixQuery ( prefix );
        query.setRewriteMethod ( multiTermRewriteMethod );
        return query;
    }
    protected Query newRegexpQuery ( Term regexp ) {
        RegexpQuery query = new RegexpQuery ( regexp );
        query.setRewriteMethod ( multiTermRewriteMethod );
        return query;
    }
    protected Query newFuzzyQuery ( Term term, float minimumSimilarity, int prefixLength ) {
        String text = term.text();
        int numEdits = FuzzyQuery.floatToEdits ( minimumSimilarity,
                       text.codePointCount ( 0, text.length() ) );
        return new FuzzyQuery ( term, numEdits, prefixLength );
    }
    private BytesRef analyzeMultitermTerm ( String field, String part ) {
        return analyzeMultitermTerm ( field, part, analyzer );
    }
    protected BytesRef analyzeMultitermTerm ( String field, String part, Analyzer analyzerIn ) {
        TokenStream source;
        if ( analyzerIn == null ) {
            analyzerIn = analyzer;
        }
        try {
            source = analyzerIn.tokenStream ( field, new StringReader ( part ) );
            source.reset();
        } catch ( IOException e ) {
            throw new RuntimeException ( "Unable to initialize TokenStream to analyze multiTerm term: " + part, e );
        }
        TermToBytesRefAttribute termAtt = source.getAttribute ( TermToBytesRefAttribute.class );
        BytesRef bytes = termAtt.getBytesRef();
        try {
            if ( !source.incrementToken() ) {
                throw new IllegalArgumentException ( "analyzer returned no terms for multiTerm term: " + part );
            }
            termAtt.fillBytesRef();
            if ( source.incrementToken() ) {
                throw new IllegalArgumentException ( "analyzer returned too many terms for multiTerm term: " + part );
            }
        } catch ( IOException e ) {
            throw new RuntimeException ( "error analyzing range part: " + part, e );
        }
        try {
            source.end();
            source.close();
        } catch ( IOException e ) {
            throw new RuntimeException ( "Unable to end & close TokenStream after analyzing multiTerm term: " + part, e );
        }
        return BytesRef.deepCopyOf ( bytes );
    }
    protected Query newRangeQuery ( String field, String part1, String part2, boolean startInclusive, boolean endInclusive ) {
        final BytesRef start;
        final BytesRef end;
        if ( part1 == null ) {
            start = null;
        } else {
            start = analyzeRangeTerms ? analyzeMultitermTerm ( field, part1 ) : new BytesRef ( part1 );
        }
        if ( part2 == null ) {
            end = null;
        } else {
            end = analyzeRangeTerms ? analyzeMultitermTerm ( field, part2 ) : new BytesRef ( part2 );
        }
        final TermRangeQuery query = new TermRangeQuery ( field, start, end, startInclusive, endInclusive );
        query.setRewriteMethod ( multiTermRewriteMethod );
        return query;
    }
    protected Query newMatchAllDocsQuery() {
        return new MatchAllDocsQuery();
    }
    protected Query newWildcardQuery ( Term t ) {
        WildcardQuery query = new WildcardQuery ( t );
        query.setRewriteMethod ( multiTermRewriteMethod );
        return query;
    }
    protected Query getBooleanQuery ( List<BooleanClause> clauses ) throws ParseException {
        return getBooleanQuery ( clauses, false );
    }
    protected Query getBooleanQuery ( List<BooleanClause> clauses, boolean disableCoord )
    throws ParseException {
        if ( clauses.size() == 0 ) {
            return null;
        }
        BooleanQuery query = newBooleanQuery ( disableCoord );
        for ( final BooleanClause clause : clauses ) {
            query.add ( clause );
        }
        return query;
    }
    protected Query getWildcardQuery ( String field, String termStr ) throws ParseException {
        if ( "*".equals ( field ) ) {
            if ( "*".equals ( termStr ) ) {
                return newMatchAllDocsQuery();
            }
        }
        if ( !allowLeadingWildcard && ( termStr.startsWith ( "*" ) || termStr.startsWith ( "?" ) ) ) {
            throw new ParseException ( "'*' or '?' not allowed as first character in WildcardQuery" );
        }
        if ( lowercaseExpandedTerms ) {
            termStr = termStr.toLowerCase ( locale );
        }
        Term t = new Term ( field, termStr );
        return newWildcardQuery ( t );
    }
    protected Query getRegexpQuery ( String field, String termStr ) throws ParseException {
        if ( lowercaseExpandedTerms ) {
            termStr = termStr.toLowerCase ( locale );
        }
        Term t = new Term ( field, termStr );
        return newRegexpQuery ( t );
    }
    protected Query getPrefixQuery ( String field, String termStr ) throws ParseException {
        if ( !allowLeadingWildcard && termStr.startsWith ( "*" ) ) {
            throw new ParseException ( "'*' not allowed as first character in PrefixQuery" );
        }
        if ( lowercaseExpandedTerms ) {
            termStr = termStr.toLowerCase ( locale );
        }
        Term t = new Term ( field, termStr );
        return newPrefixQuery ( t );
    }
    protected Query getFuzzyQuery ( String field, String termStr, float minSimilarity ) throws ParseException {
        if ( lowercaseExpandedTerms ) {
            termStr = termStr.toLowerCase ( locale );
        }
        Term t = new Term ( field, termStr );
        return newFuzzyQuery ( t, minSimilarity, fuzzyPrefixLength );
    }
    Query handleBareTokenQuery ( String qfield, Token term, Token fuzzySlop, boolean prefix, boolean wildcard, boolean fuzzy, boolean regexp ) throws ParseException {
        Query q;
        String termImage = discardEscapeChar ( term.image );
        if ( wildcard ) {
            q = getWildcardQuery ( qfield, term.image );
        } else if ( prefix ) {
            q = getPrefixQuery ( qfield,
                                 discardEscapeChar ( term.image.substring
                                         ( 0, term.image.length() - 1 ) ) );
        } else if ( regexp ) {
            q = getRegexpQuery ( qfield, term.image.substring ( 1, term.image.length() - 1 ) );
        } else if ( fuzzy ) {
            q = handleBareFuzzy ( qfield, fuzzySlop, termImage );
        } else {
            q = getFieldQuery ( qfield, termImage, false );
        }
        return q;
    }
    Query handleBareFuzzy ( String qfield, Token fuzzySlop, String termImage )
    throws ParseException {
        Query q;
        float fms = fuzzyMinSim;
        try {
            fms = Float.valueOf ( fuzzySlop.image.substring ( 1 ) ).floatValue();
        } catch ( Exception ignored ) { }
        if ( fms < 0.0f ) {
            throw new ParseException ( "Minimum similarity for a FuzzyQuery has to be between 0.0f and 1.0f !" );
        } else if ( fms >= 1.0f && fms != ( int ) fms ) {
            throw new ParseException ( "Fractional edit distances are not allowed!" );
        }
        q = getFuzzyQuery ( qfield, termImage, fms );
        return q;
    }
    Query handleQuotedTerm ( String qfield, Token term, Token fuzzySlop ) throws ParseException {
        int s = phraseSlop;
        if ( fuzzySlop != null ) {
            try {
                s = Float.valueOf ( fuzzySlop.image.substring ( 1 ) ).intValue();
            } catch ( Exception ignored ) { }
        }
        return getFieldQuery ( qfield, discardEscapeChar ( term.image.substring ( 1, term.image.length() - 1 ) ), s );
    }
    Query handleBoost ( Query q, Token boost ) {
        if ( boost != null ) {
            float f = ( float ) 1.0;
            try {
                f = Float.valueOf ( boost.image ).floatValue();
            } catch ( Exception ignored ) {
            }
            if ( q != null ) {
                q.setBoost ( f );
            }
        }
        return q;
    }
    String discardEscapeChar ( String input ) throws ParseException {
        char[] output = new char[input.length()];
        int length = 0;
        boolean lastCharWasEscapeChar = false;
        int codePointMultiplier = 0;
        int codePoint = 0;
        for ( int i = 0; i < input.length(); i++ ) {
            char curChar = input.charAt ( i );
            if ( codePointMultiplier > 0 ) {
                codePoint += hexToInt ( curChar ) * codePointMultiplier;
                codePointMultiplier >>>= 4;
                if ( codePointMultiplier == 0 ) {
                    output[length++] = ( char ) codePoint;
                    codePoint = 0;
                }
            } else if ( lastCharWasEscapeChar ) {
                if ( curChar == 'u' ) {
                    codePointMultiplier = 16 * 16 * 16;
                } else {
                    output[length] = curChar;
                    length++;
                }
                lastCharWasEscapeChar = false;
            } else {
                if ( curChar == '\\' ) {
                    lastCharWasEscapeChar = true;
                } else {
                    output[length] = curChar;
                    length++;
                }
            }
        }
        if ( codePointMultiplier > 0 ) {
            throw new ParseException ( "Truncated unicode escape sequence." );
        }
        if ( lastCharWasEscapeChar ) {
            throw new ParseException ( "Term can not end with escape character." );
        }
        return new String ( output, 0, length );
    }
    static final int hexToInt ( char c ) throws ParseException {
        if ( '0' <= c && c <= '9' ) {
            return c - '0';
        } else if ( 'a' <= c && c <= 'f' ) {
            return c - 'a' + 10;
        } else if ( 'A' <= c && c <= 'F' ) {
            return c - 'A' + 10;
        } else {
            throw new ParseException ( "Non-hex character in Unicode escape sequence: " + c );
        }
    }
    public static String escape ( String s ) {
        StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < s.length(); i++ ) {
            char c = s.charAt ( i );
            if ( c == '\\' || c == '+' || c == '-' || c == '!' || c == '(' || c == ')' || c == ':'
                    || c == '^' || c == '[' || c == ']' || c == '\"' || c == '{' || c == '}' || c == '~'
                    || c == '*' || c == '?' || c == '|' || c == '&' || c == '/' ) {
                sb.append ( '\\' );
            }
            sb.append ( c );
        }
        return sb.toString();
    }
}

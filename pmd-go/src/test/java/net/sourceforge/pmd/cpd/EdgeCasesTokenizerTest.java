
package net.sourceforge.pmd.cpd;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class EdgeCasesTokenizerTest {

    private String getSampleCode(final String filename) throws IOException {
        return IOUtils.toString(GoTokenizer.class.getResourceAsStream(filename));
    }
    
    @Test
    public void testEscapedBackSlash() throws IOException {
        // See https://github.com/pmd/pmd/issues/1751
        final String filename = "issue1751.go";
        final GoTokenizer tokenizer = new GoTokenizer();
        final SourceCode sourceCode = new SourceCode(new SourceCode.StringCodeLoader(getSampleCode(filename), filename));
        
        final Tokens tokens = new Tokens();
        tokenizer.tokenize(sourceCode, tokens); // it should simply not fail
    }
}

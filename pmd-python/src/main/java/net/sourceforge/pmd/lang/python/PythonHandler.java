/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.python;

import net.sourceforge.pmd.lang.AbstractCpdLanguageVersionHandler;
import net.sourceforge.pmd.lang.Parser;
import net.sourceforge.pmd.lang.ParserOptions;


/**
 * Implementation of LanguageVersionHandler for the Python Language.
 */
public class PythonHandler extends AbstractCpdLanguageVersionHandler {


    @Override
    protected String getLanguageName() {
        return "Python";
    }


    @Override
    public Parser getParser(ParserOptions parserOptions) {
        return new PythonParser(parserOptions);
    }
}
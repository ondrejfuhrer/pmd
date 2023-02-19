/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.AbstractConfiguration;
import net.sourceforge.pmd.cpd.renderer.CPDReportRenderer;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.util.log.internal.SimpleMessageReporter;

/**
 *
 * @author Brian Remedios
 * @author Romain Pelisse - &lt;belaran@gmail.com&gt;
 */
public class CPDConfiguration extends AbstractConfiguration {

    public static final String DEFAULT_LANGUAGE = "java";
    public static final String DEFAULT_RENDERER = "text";

    private static final Map<String, Class<? extends CPDReportRenderer>> RENDERERS = new HashMap<>();


    static {
        RENDERERS.put(DEFAULT_RENDERER, SimpleRenderer.class);
        RENDERERS.put("xml", XMLRenderer.class);
        RENDERERS.put("csv", CSVRenderer.class);
        RENDERERS.put("csv_with_linecount_per_file", CSVWithLinecountPerFileRenderer.class);
        RENDERERS.put("vs", VSRenderer.class);
    }


    private int minimumTileSize;

    private boolean skipDuplicates;

    private String rendererName = DEFAULT_RENDERER;

    private @Nullable CPDReportRenderer cpdReportRenderer;

    private boolean ignoreLiterals;

    private boolean ignoreIdentifiers;

    private boolean ignoreAnnotations;

    private boolean ignoreUsings;

    private boolean ignoreLiteralSequences = false;

    private boolean skipLexicalErrors = false;

    private boolean noSkipBlocks = false;

    private String skipBlocksPattern = Tokenizer.DEFAULT_SKIP_BLOCKS_PATTERN;

    private List<Path> files = Collections.emptyList();

    private Path fileListPath;

    private List<Path> excludes;

    private boolean nonRecursive;

    private URI uri;

    private boolean help;

    private boolean failOnViolation = true;


    public CPDConfiguration() {
        this(LanguageRegistry.CPD);
    }

    public CPDConfiguration(LanguageRegistry languageRegistry) {
        super(languageRegistry, new SimpleMessageReporter(LoggerFactory.getLogger(CpdAnalysis.class)));
    }

    @Override
    public void setSourceEncoding(Charset sourceEncoding) {
        super.setSourceEncoding(sourceEncoding);
        if (cpdReportRenderer != null) {
            setRendererEncoding(cpdReportRenderer, sourceEncoding);
        }
    }

    static CPDReportRenderer createRendererByName(String name, Charset encoding) {
        if (name == null || "".equals(name)) {
            name = DEFAULT_RENDERER;
        }
        Class<? extends CPDReportRenderer> rendererClass = RENDERERS.get(name.toLowerCase(Locale.ROOT));
        if (rendererClass == null) {
            Class<?> klass;
            try {
                klass = Class.forName(name);
                if (CPDReportRenderer.class.isAssignableFrom(klass)) {
                    rendererClass = (Class) klass;
                } else {
                    throw new IllegalArgumentException("Class " + name + " does not implement " + CPDReportRenderer.class);
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Cannot find class " + name);
            }
        }

        CPDReportRenderer renderer;
        try {
            renderer = rendererClass.getDeclaredConstructor().newInstance();
            setRendererEncoding(renderer, encoding);
        } catch (Exception e) {
            System.err.println("Couldn't instantiate renderer, defaulting to SimpleRenderer: " + e);
            renderer = new SimpleRenderer();
        }
        return renderer;
    }

    private static void setRendererEncoding(@NonNull Object renderer, Charset encoding) {
        try {
            PropertyDescriptor encodingProperty = new PropertyDescriptor("encoding", renderer.getClass());
            Method method = encodingProperty.getWriteMethod();
            if (method == null) {
                return;
            }
            if (method.getParameterTypes()[0] == Charset.class) {
                method.invoke(renderer, encoding);
            } else if (method.getParameterTypes()[0] == String.class) {
                method.invoke(renderer, encoding.name());
            }
        } catch (IntrospectionException | ReflectiveOperationException ignored) {
            // ignored - maybe this renderer doesn't have a encoding property
        }
    }

    public static Set<String> getRenderers() {
        return Collections.unmodifiableSet(RENDERERS.keySet());
    }

    public void setLanguage(net.sourceforge.pmd.lang.Language language) {
        setForceLanguageVersion(language.getDefaultVersion());
    }

    public int getMinimumTileSize() {
        return minimumTileSize;
    }

    public void setMinimumTileSize(int minimumTileSize) {
        this.minimumTileSize = minimumTileSize;
    }

    public boolean isSkipDuplicates() {
        return skipDuplicates;
    }

    public void setSkipDuplicates(boolean skipDuplicates) {
        this.skipDuplicates = skipDuplicates;
    }

    public String getRendererName() {
        return rendererName;
    }

    public void setRendererName(String rendererName) {
        this.rendererName = rendererName;
        if (rendererName == null) {
            this.cpdReportRenderer = null;
        }
        this.cpdReportRenderer = createRendererByName(rendererName, getSourceEncoding());
    }


    public CPDReportRenderer getCPDReportRenderer() {
        return cpdReportRenderer;
    }

    void setRenderer(CPDReportRenderer renderer) {
        this.cpdReportRenderer = renderer;
    }

    public boolean isIgnoreLiterals() {
        return ignoreLiterals;
    }

    public void setIgnoreLiterals(boolean ignoreLiterals) {
        this.ignoreLiterals = ignoreLiterals;
    }

    public boolean isIgnoreIdentifiers() {
        return ignoreIdentifiers;
    }

    public void setIgnoreIdentifiers(boolean ignoreIdentifiers) {
        this.ignoreIdentifiers = ignoreIdentifiers;
    }

    public boolean isIgnoreAnnotations() {
        return ignoreAnnotations;
    }

    public void setIgnoreAnnotations(boolean ignoreAnnotations) {
        this.ignoreAnnotations = ignoreAnnotations;
    }

    public boolean isIgnoreUsings() {
        return ignoreUsings;
    }

    public void setIgnoreUsings(boolean ignoreUsings) {
        this.ignoreUsings = ignoreUsings;
    }

    public boolean isIgnoreLiteralSequences() {
        return ignoreLiteralSequences;
    }

    public void setIgnoreLiteralSequences(boolean ignoreLiteralSequences) {
        this.ignoreLiteralSequences = ignoreLiteralSequences;
    }

    public boolean isSkipLexicalErrors() {
        return skipLexicalErrors;
    }

    public void setSkipLexicalErrors(boolean skipLexicalErrors) {
        this.skipLexicalErrors = skipLexicalErrors;
    }

    public @NonNull List<Path> getFiles() {
        return files;
    }

    public void setFiles(List<Path> files) {
        this.files = Objects.requireNonNull(files);
    }

    public Path getFileListPath() {
        return fileListPath;
    }

    public void setFileListPath(Path fileListPath) {
        this.fileListPath = fileListPath;
    }

    public URI getURI() {
        return uri;
    }

    public void setURI(URI uri) {
        this.uri = uri;
    }

    public List<Path> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<Path> excludes) {
        this.excludes = excludes;
    }

    public boolean isNonRecursive() {
        return nonRecursive;
    }

    public void setNonRecursive(boolean nonRecursive) {
        this.nonRecursive = nonRecursive;
    }

    public boolean isHelp() {
        return help;
    }

    public void setHelp(boolean help) {
        this.help = help;
    }

    public boolean isNoSkipBlocks() {
        return noSkipBlocks;
    }

    public void setNoSkipBlocks(boolean noSkipBlocks) {
        this.noSkipBlocks = noSkipBlocks;
    }

    public String getSkipBlocksPattern() {
        return skipBlocksPattern;
    }

    public void setSkipBlocksPattern(String skipBlocksPattern) {
        this.skipBlocksPattern = skipBlocksPattern;
    }

    public boolean isFailOnViolation() {
        return failOnViolation;
    }

    public void setFailOnViolation(boolean failOnViolation) {
        this.failOnViolation = failOnViolation;
    }

}

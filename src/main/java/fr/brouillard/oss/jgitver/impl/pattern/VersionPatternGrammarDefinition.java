package fr.brouillard.oss.jgitver.impl.pattern;

import fr.brouillard.oss.jgitver.Version;
import fr.brouillard.oss.jgitver.metadata.Metadatas;
import org.petitparser.parser.Parser;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VersionPatternGrammarDefinition extends VersionGrammarDefinition {

    private AutoSeparatorProvider separatorProvider;

    public VersionPatternGrammarDefinition(Version version, Function<String, Optional<String>> env, Function<String, Optional<String>> sys, Function<Metadatas, Optional<String>> meta) {
        super();
        resetSeparatorProvider();

        action("pattern", (List<?> elements) -> {
            String computedVersion = elements.stream().map(o -> ((Object) o).toString()).collect(Collectors.joining());
            return Version.parse(computedVersion);
        });
        action("pattern_element", (o) -> o);
        action("delimitedPlaceholder", (o) -> {
            return o;
        });
        action("placeholder", (o) -> o);
        action("withPrefixPlaceholder", (List<?> elements) -> {
            Prefix p = (Prefix) elements.get(0);
            Optional<String> value = (Optional<String>) elements.get(1);

            Optional<String> result = p.apply(value);
            result.ifPresent((ignore) -> getSeparatorProvider().next());
            return result;
        });
        action("chars", (o) -> o);
        action("prefix_placeholder", (o) -> o);
        action("fixed_prefix_placeholder", (List<?> elements) -> {
            getSeparatorProvider().endVersion();
            String prefix = (String) elements.get(0);
            Mode mode = (Mode) elements.get(1);
            return new Prefix(mode, prefix);
        });
        action("mandatory_prefix_placeholder", (o) -> o);
        action("optional_prefix_placeholder",      (o) -> o);
        action("auto_prefix_placeholder", (ignore) -> {
//            getSeparatorProvider().next();
            return new Prefix(Mode.OPTIONAL, () -> this.getSeparatorProvider().currentSeparator());
        });
        action("meta", (String s) -> {
            try {
                Metadatas m = Metadatas.valueOf(s);
                return meta.apply(m);
            } catch (IllegalArgumentException iae) {
                return Optional.empty();
            }
        });
        action("sys", (String s) -> sys.apply(s));
        action("env", (String s) -> env.apply(s));
        action("full_version", (o) -> {
            getSeparatorProvider().major();
            getSeparatorProvider().minor();
            getSeparatorProvider().patch();
            getSeparatorProvider().next();
            return Optional.of(String.format("%s.%s.%s", version.getMajor(), version.getMinor(), version.getPatch()));
        });
        action("major_version", (o) -> {
            getSeparatorProvider().major();
            return Optional.of("" + version.getMajor());
        });
        action("minor_version", (o) -> {
            getSeparatorProvider().minor();
            return Optional.of("" + version.getMinor());
        });
        action("patch_version", (o) -> {
            getSeparatorProvider().patch();
            return Optional.of("" + version.getPatch());
        });
        action("inner_placeholder", (o) -> o);
        action("placeholder", (Optional<String> o) -> o.orElse(""));
    }

    @Override
    public Parser build() {
        resetSeparatorProvider();
        return super.build();
    }

    public void resetSeparatorProvider() {
        separatorProvider = new AutoSeparatorProvider();
    }

    private AutoSeparatorProvider getSeparatorProvider() {
        return separatorProvider;
    }
}
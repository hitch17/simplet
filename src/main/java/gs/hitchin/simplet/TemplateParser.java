package gs.hitchin.simplet;

import static java.util.Collections.unmodifiableList;
import static org.codehaus.jparsec.Parsers.between;
import static org.codehaus.jparsec.Parsers.or;
import static org.codehaus.jparsec.Parsers.sequence;
import static org.codehaus.jparsec.Scanners.WHITESPACES;
import static org.codehaus.jparsec.Scanners.pattern;
import static org.codehaus.jparsec.Scanners.string;
import static org.codehaus.jparsec.pattern.Patterns.and;
import static org.codehaus.jparsec.pattern.Patterns.notString;
import gs.hitchin.simplet.Content.Literal;
import gs.hitchin.simplet.Content.Variable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec._;
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Map3;
import org.codehaus.jparsec.pattern.Patterns;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.InputSupplier;

public class TemplateParser {

	public static TemplateContainer fromStream(URI uri, InputSupplier<? extends InputStream> supplier) {
		try {
			InputStream input = supplier.getInput();
			try {
				return new TemplateContainer(uri, fromStream(input));
			} finally {
				input.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	static java.util.Map<String, Template> fromStream(InputStream input) throws IOException {
		ImmutableMap.Builder<String, Template> templates = ImmutableMap.builder();
		for (Template t : formatParser().parse(new InputStreamReader(input))) {
			// Duplicate keys are not allowed according to docs, which is what I want.
			templates.put(t.getId(), t);
		}
		return templates.build();
	}

	public static Template fromString(String value) {
		List<Content> content = contentParser().parse(value);
		Set<String> variables = getVariables(content);
		return new Template("#", variables, content);
	}
	
	static Parser<List<Template>> formatParser() {
		return assignmentParser().many();
	}

	static Parser<Template> assignmentParser() {
		Parser<List<_>> ws = wsParser();
		return sequence(
				idToken(), 
				assignTokenParser().between(ws, ws), 
				rightAssignmentParser(), 
				new Map3<String, String, List<Content>, Template>() {
					@Override
					public Template map(String id, String skip, List<Content> exprs) {
						Set<String> vars = getVariables(exprs);
						return new Template(id, vars, unmodifiableList(exprs));
					}
				})
				.between(ws, ws);
	}

	static Set<String> getVariables(List<Content> exprs) {
		ImmutableSet.Builder<String> r = ImmutableSet.builder();
		for (Content c : exprs) {
			if (c instanceof Variable) {
				Variable v = (Variable) c;
				r.add(v.getName());
			}
		}
		return r.build();
	}

	static Parser<List<Content>> rightAssignmentParser() {
		return between(openFormParser(), contentParser(), closeFormParser());
	}

	static Parser<List<Content>> contentParser() {
		return expressionParser().many();
	}

	static Parser<Content> expressionParser() {
		return or(variableParser(), literalParser());
	}

	static Parser<String> notTokenLiteralParser() {
		return pattern(and(
				notString("{{"),
				notString("}}"),
				notString("<<"),
				notString(">>")), 
				"Not Token").source();
	}

	static Parser<String> escapedLiteralParser() {
		return pattern(Patterns.or(
				Patterns.string("\\{"), 
				Patterns.string("\\}"),
				Patterns.string("\\<"), 
				Patterns.string("\\>")), 
				"Escaped Token")
				.source()
				.map(new Map<String, String>() {
					@Override
					public String map(String s) {
						if ("\\{".equals(s)) { 
							return "{";
						} else if ("\\}".equals(s)) {
							return "}";
						} else if ("\\<".equals(s)) {
							return "<";
						} else if ("\\>".equals(s)) {
							return ">";
						}
						throw new RuntimeException("failz");
 					}});
	}
	
	static Parser<Literal> literalParser() {
		Parser<String> escaped = escapedLiteralParser();
		Parser<String> any = notTokenLiteralParser().source();
		Parser<Literal> orz = Parsers.or(escaped, any).many()
		.map(new Map<List<String>, Literal>() {
			@Override
			public Literal map(List<String> tokens) {
				StringBuilder b = new StringBuilder();
				for (String s : tokens) {
					b.append(s);
				}
				return new Literal(b.toString());
			}});
		return orz;
	}
	
	static Parser<Literal> literalParserOld() {
		return pattern(
				and(
					notString("{{"),
					notString("}}"),
					notString("<<"),
					notString(">>"),
					notString("=")), 
				"Not Token").many().source()
				.map(new Map<String, Literal>() {
					@Override
					public Literal map(String s) {
						return new Literal(s);
					}});
	}

	static Parser<Variable> variableParser() {
		Parser<String> openVar = openVarParser();
		Parser<String> closeVar = closeVarParser();
		Parser<String> ids = idToken();
		Parser<List<_>> ws = wsParser();
		return ids
				.between(ws, ws)
				.between(openVar, closeVar)
				.map(new Map<String, Variable>() {
					@Override
					public Variable map(String s) {
						return new Variable(s);
					}});
	}

	static Parser<String> idToken() {
		return Terminals.Identifier.TOKENIZER.source();
	}

	static Parser<String> assignTokenParser() {
		return string("=").source();
	}

	static Parser<String> closeFormParser() {
		return string(">>").source();
	}

	static Parser<String> openFormParser() {
		return string("<<").source();
	}

	static Parser<String> closeVarParser() {
		return string("}}").source();
	}

	static Parser<String> openVarParser() {
		return string("{{").source();
	}
	
	static Parser<List<_>> wsParser() {
		return WHITESPACES.many();
	}
	
}

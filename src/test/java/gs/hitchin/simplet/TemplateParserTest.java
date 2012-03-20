package gs.hitchin.simplet;

import static gs.hitchin.simplet.TemplateParser.assignTokenParser;
import static gs.hitchin.simplet.TemplateParser.assignmentParser;
import static gs.hitchin.simplet.TemplateParser.closeFormParser;
import static gs.hitchin.simplet.TemplateParser.closeVarParser;
import static gs.hitchin.simplet.TemplateParser.contentParser;
import static gs.hitchin.simplet.TemplateParser.escapedLiteralParser;
import static gs.hitchin.simplet.TemplateParser.expressionParser;
import static gs.hitchin.simplet.TemplateParser.formatParser;
import static gs.hitchin.simplet.TemplateParser.getVariables;
import static gs.hitchin.simplet.TemplateParser.idToken;
import static gs.hitchin.simplet.TemplateParser.literalParser;
import static gs.hitchin.simplet.TemplateParser.openFormParser;
import static gs.hitchin.simplet.TemplateParser.openVarParser;
import static gs.hitchin.simplet.TemplateParser.rightAssignmentParser;
import static gs.hitchin.simplet.TemplateParser.variableParser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gs.hitchin.simplet.Content.Literal;
import gs.hitchin.simplet.Content.Variable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Set;

import org.codehaus.jparsec.Parser;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.io.InputSupplier;

public class TemplateParserTest {

	@Test
	public void testParser() {

		Parser<String> openVar = openVarParser();
		assertEquals("{{", openVar.parse("{{"));
		
		Parser<String> closeVar = closeVarParser();
		assertEquals("}}", closeVar.parse("}}"));
		
		Parser<String> openForm = openFormParser();
		assertEquals("<<", openForm.parse("<<"));
		
		Parser<String> closeForm = closeFormParser();
		assertEquals(">>", closeForm.parse(">>"));
		
		Parser<String> assignToken = assignTokenParser();
		assertEquals("=", assignToken.parse("="));
		
		Parser<String> ids = idToken();
		assertEquals("a_1", ids.parse("a_1"));
		
		Parser<Variable> varz = variableParser();
		assertEquals(new Variable("abc"), varz.parse("{{abc}}"));
		assertEquals(new Variable("abc"), varz.parse("{{ abc }}"));

		Parser<String> escaped = escapedLiteralParser();
		assertEquals("{", escaped.parse("\\{"));
		assertEquals("}", escaped.parse("\\}"));
		assertEquals("<", escaped.parse("\\<"));
		assertEquals(">", escaped.parse("\\>"));
		assertEquals(ImmutableList.of("{", "{", "{"), escaped.many().parse("\\{\\{\\{"));

		Parser<List<Literal>> litz = literalParser().many();
		assertEquals(ImmutableList.of(new Literal("{a")), litz.parse("\\{a"));
		assertEquals(ImmutableList.of(new Literal("<>ab{}")), 
				litz.parse("\\<\\>ab\\{\\}"));
		assertEquals(ImmutableList.of(new Literal("ab{")), litz.parse("ab\\{"));
		assertEquals(ImmutableList.of(new Literal("ab")), litz.parse("ab"));
		
		Parser<Content> expr = expressionParser();
		assertEquals(new Literal("a"), expr.parse("a"));
		assertEquals(new Variable("abc"), expr.parse("{{abc}}"));

		Parser<List<Content>> content = contentParser();
		assertEquals(ImmutableList.of(new Variable("y"), new Literal("b")), content.parse("{{y}}b"));
		assertEquals(ImmutableList.of(new Literal("a"), new Variable("x")), content.parse("a{{x}}"));

		Parser<List<Content>> rightAssign = rightAssignmentParser();
		assertEquals(ImmutableList.of(new Literal("a"), new Variable("x")), rightAssign.parse("<<a{{x}}>>"));
		assertEquals(ImmutableList.of(new Variable("y"), new Literal("b")), rightAssign.parse("<<{{y}}b>>"));

		Parser<Template> assignment = assignmentParser();
		Template a = assignment.parse("world =<<a  {{ x}}>>");
		assertEquals("world", a.getId());
		assertEquals(ImmutableList.of(new Literal("a  "), new Variable("x")), a.getContent());

		Template b = assignment.parse("\n\nworld =<<a  {{ x}}>>\n\n");
		assertEquals("world", b.getId());
		assertEquals(ImmutableList.of(new Literal("a  "), new Variable("x")), b.getContent());
		
		Parser<List<Template>> format = formatParser();
		List<Template> cs = format.parse("\n\nfirst =<<a  {{ x }}>>\n\t\n \n\nsecond =<< {{y}}b>>\n\n");
		assertEquals(2, cs.size());
		Template c = cs.get(0);
		Template d = cs.get(1);
		assertEquals("first", c.getId());
		assertEquals(ImmutableList.of(new Literal("a  "), new Variable("x")), c.getContent());
		assertEquals("second", d.getId());
		assertEquals(ImmutableList.of(new Literal(" "), new Variable("y"), new Literal("b")), d.getContent());

		Template e = format.parse("a=<<\\<{{b}}\\>>>").get(0);
		assertEquals("a", e.getId());
		assertEquals(ImmutableList.of(new Literal("<"), new Variable("b"), new Literal(">")), e.getContent());
		
	}

	@Test
	public void testGetVariable() {
		Set<String> r = getVariables(ImmutableList.of(
				new Variable("a"), new Literal("xyz"), new Variable("b"), new Variable("a")));
		assertEquals(2, r.size());
		assertTrue(r.contains("a"));
		assertTrue(r.contains("b"));
	}

	@Test
	public void testReadFromString() {
		Template e = TemplateParser.fromString("\\<{{b}}\\>");
		assertEquals("#", e.getId());
		assertEquals(ImmutableList.of(new Literal("<"), new Variable("b"), new Literal(">")), e.getContent());
	}
	
	@Test
	public void testReadFromStream() {
		URI uri = URI.create("file://something");
		InputSupplier<? extends InputStream> supplier = new InputSupplier<InputStream>() {
			@Override
			public InputStream getInput() throws IOException {
				return new ByteArrayInputStream(
						"\n\nfirst =<<a  {{ x }}>>\n\t\n \n\nsecond =<< {{y}}b>>\n\n".getBytes());
			}
		};
		TemplateContainer container = TemplateParser.fromStream(uri, supplier);
		assertEquals(2, container.getTemplates().size());
		Template c = container.getTemplate("first");
		Template d = container.getTemplate("second");
		assertEquals("first", c.getId());
		assertEquals(ImmutableList.of(new Literal("a  "), new Variable("x")), c.getContent());
		assertEquals("second", d.getId());
		assertEquals(ImmutableList.of(new Literal(" "), new Variable("y"), new Literal("b")), d.getContent());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testReadFromStreamDuplicateTemplateNames() {
		URI uri = URI.create("file://something");
		InputSupplier<? extends InputStream> supplier = new InputSupplier<InputStream>() {
			@Override
			public InputStream getInput() throws IOException {
				return new ByteArrayInputStream(
						"one=<<>>one=<<>>".getBytes());
			}
		};
		TemplateParser.fromStream(uri, supplier);
	}
	
}

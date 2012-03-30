package gs.hitchin.simplet;

import static gs.hitchin.simplet.Simplet.simplet;
import static org.junit.Assert.assertEquals;
import gs.hitchin.simplet.ann.N;
import gs.hitchin.simplet.ann.TemplateFile;
import gs.hitchin.simplet.ann.TemplateResource;
import gs.hitchin.simplet.ann.TemplateString;

import java.lang.reflect.Method;

import org.junit.Test;

public class SimpletTest {

	@Test(expected=IllegalArgumentException.class)
	public void testAnnotationMissing() {
		simplet().template(BrokenTemplateNoAnnotation.class);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testMustReturnStringBuilder() {
		simplet().template(BrokenTemplateDoesntReturnStringBuilder.class);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testMissingParamName() {
		simplet().template(BrokenTemplateMissingParamName.class);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCantReuseSameName() {
		simplet().template(BrokenTemplateCantReuseSameName.class);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testMissingVariable() {
		simplet().template(BrokenTemplateMissingVariable.class);
	}
	
	@Test(expected=NullPointerException.class)
	public void testNoNulls() {
		simplet().template(TestTemplate.class).generate("a", null);
	}
	
	@Test
	public void testOutputs() throws SecurityException, NoSuchMethodException {
		TestTemplate t = simplet().template(TestTemplate.class);
		StringBuilder r = t.generate("World", "Hello");
		assertEquals("Hello, World!", r.toString());

		Appendable r2 = t.appendable("World", "Hello");
		assertEquals("Hello, World!", r2.toString());

		CharSequence r3 = t.charSequence("World", "Hello");
		assertEquals("Hello, World!", r3.toString());

		String r4 = t.string("World", "Hello");
		assertEquals("Hello, World!", r4.toString());

		Fragment r5 = t.fragment("World", "Hello");
		assertEquals("{fragment: {a=World, b=Hello}}", r5.toString());
		assertEquals(2, r5.getContext().size());
		assertEquals("World", r5.getContext().get("a"));
		assertEquals("Hello", r5.getContext().get("b"));
		Method method = TestTemplate.class.getMethod("fragment", new Class[] {String.class, String.class}); 
		assertEquals(method, r5.getMethod());
		assertEquals("Hello, World!", r5.getResult().toString());
	}

	public static interface TestTemplate {
		@TemplateString("{{b}}, {{a}}!")
		StringBuilder generate(@N("a") String a, @N("b")String b);

		@TemplateString("{{b}}, {{a}}!")
		Fragment fragment(@N("a") String a, @N("b")String b);

		@TemplateString("{{b}}, {{a}}!")
		Appendable appendable(@N("a") String a, @N("b")String b);

		@TemplateString("{{b}}, {{a}}!")
		String string(@N("a") String a, @N("b")String b);

		@TemplateString("{{b}}, {{a}}!")
		CharSequence charSequence(@N("a") String a, @N("b")String b);
	}

	public static interface BrokenTemplateNoAnnotation {
		StringBuilder generate(@N("a") String a, @N("b")String b);
	}

	public static interface BrokenTemplateDoesntReturnStringBuilder {
		void generate(@N("a") String a, @N("b")String b);
	}
	
	public static interface BrokenTemplateMissingParamName {
		@TemplateString("{{b}}, {{a}}!")
		StringBuilder generate(@N("a") String a, String b);
  }

	public static interface BrokenTemplateCantReuseSameName {
		@TemplateString("{{b}}, {{a}}!")
		StringBuilder generate(@N("a") String a, @N("a")String b);
  }

	public static interface BrokenTemplateMissingVariable {
		@TemplateString("{{c}}, {{a}}!")
		StringBuilder generate(@N("a") String a, @N("b")String b);
  }
		
	@Test
	public void testExternal() {
		TestTemplate2 t = simplet().template(TestTemplate2.class);
		assertEquals("Hello, World!", t.hello("World", "Hello"));
		assertEquals("{{b}}, World!<>", t.world("World", "Hello"));
	}

	static interface TestTemplate2 {
		@TemplateResource(resource="gs/hitchin/simplet/hello.s", template="hello")
		String hello(@N("a") String who, @N("b") String say);
		@TemplateFile(file="src/test/resources/gs/hitchin/simplet/hello.s", template="world")
		String world(@N("a") String who, @N("b") String say);
	}
	
	@Test
	public void testNoParamsTemplate() {
		assertEquals("xyz", Simplet.simplet().template(NoParamsTemplate.class).content());
	}
	
	static interface NoParamsTemplate {
		@TemplateString("xyz")
		String content();
	}
	
}

package gs.hitchin.simplet;

import static gs.hitchin.simplet.TestFragment.fragment;
import static org.junit.Assert.assertEquals;
import gs.hitchin.simplet.ann.N;
import gs.hitchin.simplet.ann.TemplateString;

import org.junit.Test;

public class DelegateTest {

	@Test
	public void testOverride() {
		TestTemplate t1 = Simplet.simplet().template(TestTemplate.class);
		assertEquals("Hello, World!", t1.hello("World").getResult().toString());
		assertEquals("Good Bye, World!", t1.bye("World").getResult().toString());

		OverrideTemplate t2 = Simplet.simplet().template(OverrideTemplate.class);
		assertEquals("Wassup, World!", t2.hello("World").getResult().toString());
		assertEquals("Good Bye, World!", t2.bye("World").getResult().toString());
	}

	@Test
	public void testDelegate() {
		
		fragment("wrapper")
			.param("a", fragment("hello").param("a", "Fred"))
			.param("b", fragment("bye").param("a", "Fred"))
			.resultContains("Hello, Fred!")
		  .assertOn(new Delegate<TestTemplate>(TestTemplate.class).message("Fred"));

		assertEquals("Hello, Fred!\nIt's good to meet you.\nGood Bye, Fred!\n", 
				new Delegate<TestTemplate>(TestTemplate.class).message("Fred").getResult().toString());

		fragment("wrapper")
			.param("a", fragment("hello").param("a", "Donnie"))
			.param("b", fragment("bye").param("a", "Donnie"))
			.resultContains("Wassup, Donnie!")
		  .assertOn(new Delegate<OverrideTemplate>(OverrideTemplate.class).message("Donnie"));

		assertEquals("Wassup, Donnie!\nIt's good to meet you.\nGood Bye, Donnie!\n", 
				new Delegate<OverrideTemplate>(OverrideTemplate.class).message("Donnie").getResult().toString());

	}
	
	static class Delegate<T extends TestTemplate> {
		private T t;
		public Delegate(Class<T> c) {
			t = Simplet.simplet().template(c);
		}
		public Fragment message(String name) {
			return t.wrapper(t.hello(name), t.bye(name));
		}
	}
	
	static interface OverrideTemplate extends TestTemplate {
		@TemplateString("Wassup, {{a}}!")
		public Fragment hello(@N("a") String a);
	}
	
	static interface TestTemplate {
		@TemplateString("{{a}}\nIt's good to meet you.\n{{b}}\n")
		public Fragment wrapper(@N("a") Fragment a, @N("b") Fragment b);
		@TemplateString("Hello, {{a}}!")
		public Fragment hello(@N("a") String a);
		@TemplateString("Good Bye, {{a}}!")
		public Fragment bye(@N("a") String a);
	}
	
}

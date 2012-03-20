package gs.hitchin.simplet;

import static gs.hitchin.simplet.Simplet.simplet;
import static gs.hitchin.simplet.TestFragment.fragment;
import static org.junit.Assert.assertEquals;
import gs.hitchin.simplet.ann.N;
import gs.hitchin.simplet.ann.TemplateString;

import java.lang.reflect.Method;

import org.junit.Test;

public class FragmentTest {

	@Test
	public void testFragments() throws SecurityException, NoSuchMethodException {
		TestTemplate t = simplet().template(TestTemplate.class);

		Fragment f1 = t.first(5);
		assertEquals("{first: {v=5.0}}", f1.toString());
		assertEquals(1, f1.getContext().size());
		assertEquals(5.0, f1.getContext().get("v"));
		Method m1 = TestTemplate.class.getMethod("first", new Class[] {double.class}); 
		assertEquals(m1, f1.getMethod());
		assertEquals("(5.0)", f1.getResult().toString());

		Fragment f2 = t.second(f1);
		assertEquals("{second: {p={first: {v=5.0}}}}", f2.toString());
		assertEquals(1, f2.getContext().size());
		assertEquals("{first: {v=5.0}}", f2.getContext().get("p").toString());
		Method m2 = TestTemplate.class.getMethod("second", new Class[] {Fragment.class}); 
		assertEquals(m2, f2.getMethod());
		assertEquals("[(5.0)]", f2.getResult().toString());

		fragment("second")
			.param("p", 
					fragment("first")
						.param("v", 5.00)
						.assertOn(f1))
			.assertOn(f2);
		
	}

	static interface TestTemplate {
		@TemplateString("({{v}})")
		Fragment first(@N("v") double v);
		@TemplateString("[{{p}}]")
		Fragment second(@N("p") Fragment p);
	}
	
}

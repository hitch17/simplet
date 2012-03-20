package gs.hitchin.simplet;

import static gs.hitchin.simplet.Simplet.simplet;
import static gs.hitchin.simplet.TestFragment.fragment;
import gs.hitchin.simplet.ann.N;
import gs.hitchin.simplet.ann.TemplateString;

import org.junit.ComparisonFailure;
import org.junit.Test;

public class TestFragmentTest {

	@Test
	public void testFragments() {
		TestTemplate t = simplet().template(TestTemplate.class);

		Fragment f1 = t.first(5);
		Fragment f2 = t.second(f1);

		fragment("second")
			.param("p", 
					fragment("first")
						.param("v", 5.00)
						.assertOn(f1))
			.resultContains("5.0")
			.resultEquals("[(5.0)]")
			.assertOn(f2);
		
	}
	
	@Test(expected=ComparisonFailure.class)
	public void testTestFragmentName() {
		TestTemplate t = simplet().template(TestTemplate.class);
		fragment("first2") // error
			.param("v", 5.00)
			.assertOn(t.first(5));
	}

	@Test(expected=AssertionError.class)
	public void testTestFragmentParam() {
		TestTemplate t = simplet().template(TestTemplate.class);
		fragment("first") 
			.param("v", 6.0) // error
			.assertOn(t.first(5));
	}

	@Test(expected=AssertionError.class)
	public void testTestFragmentMissingParam() {
		TestTemplate t = simplet().template(TestTemplate.class);
		fragment("first") 
			// .param("v", 5.0) // Missing
			.assertOn(t.first(5));
	}

	@Test(expected=AssertionError.class)
	public void testTestFragmentDifferentParam() {
		TestTemplate t = simplet().template(TestTemplate.class);
		fragment("first") 
			.param("z", 5.0) // wrong name
			.assertOn(t.first(5));
	}
	
	@Test(expected=AssertionError.class)
	public void testTestFragmentContains() {
		TestTemplate t = simplet().template(TestTemplate.class);
		fragment("first")
			.param("v", 5.00)
			.resultContains("not in result") // error
			.assertOn(t.first(5));
	}
	
	@Test(expected=AssertionError.class)
	public void testTestFragmentEquals() {
		TestTemplate t = simplet().template(TestTemplate.class);
		fragment("first")
			.param("v", 5.00)
			.resultEquals("not equal") // error
			.assertOn(t.first(5));
	}
	
	static interface TestTemplate {
		@TemplateString("({{v}})")
		Fragment first(@N("v") double v);
		@TemplateString("[{{p}}]")
		Fragment second(@N("p") Fragment p);
	}

}

package gs.hitchin.simplet;

import static gs.hitchin.simplet.TestFragment.fragment;
import static junit.framework.Assert.assertEquals;
import gs.hitchin.simplet.ann.N;
import gs.hitchin.simplet.ann.TemplateString;

import org.junit.Test;

public class ExamplesTest {
	
/*
Yes, I know that there are quite a few options for templating and yes, I wrote another one.

So, what's the motivation behind it?  Testability.  I think there are plenty of really powerful 
and expressive templating engines out there, but I've always found that it requires quite a bit 
a discipline to ensure that my templates were easy to test.  Putting logic into a template is so 
very tempting, but it's difficult to draw the line between what is good and bad logic in a view.  
With logic in the template itself, it can be harder to use some of the useful testing techniques 
and settle for testing if the result equals or contains some testing strings.  

This brings us to the most important feature.  No logic in your template.  It's literally just 
a smart (hopefully) string replacement engine.  On top of that, there's some code that should 
make using and testing templates a little bit easier.  The second feature, you can only use the 
java primitive types, wrapper types, CharSequence, StringBuilder, String, and Fragment as parameters.  
That means if you want to do any clever formatting with your other objects, you need to convert them 
before passing them in.

How about an example.

*/
	
	static interface Example1 {
		@TemplateString("Hello, {{name}}!")
		String hello(@N("name") String name);
	}

	@Test
	public void testExample1() {
		Example1 t = Simplet.simplet().template(Example1.class);
		assertEquals("Hello, World!", t.hello("World"));
	}

/*
Pretty simple to get started.  Now, let's return a Fragment instead and get some of that nice 
testing goodness.  A Fragment is basically the result of a template's rendering, plus information 
about the method called and it's parameters.
*/
	
	static interface Example2 {
		@TemplateString("Hello, {{name}}!")
		Fragment hello(@N("name") String name);
	}
	
/*
We can test how the method was called, instead of just testing the result.  We can test the string 
result, too.  The nice part is that we have the flexibility to test the way that makes the most sense.  
For logic tests, we only need to check param() values and to test the rendering, we check the result. 
*/

	@Test
	public void testExample2() {
		Example2 t = Simplet.simplet().template(Example2.class);
		TestFragment.fragment("hello")
			.param("name", "World")
			.resultEquals("Hello, World!")
			.resultContains(", W")
			.assertOn(t.hello("World"));	
	}
	
/*
Now, for a more complicated example.  How about nesting templates?  That's pretty straightforward to do and 
easy to test.  You can pass a Fragment into another template and it will preserve the method calls.  You can 
also make assertions on each of the fragments.
*/

	static interface Example3 {
		@TemplateString("({{q}})")
		Fragment inside(@N("q") String q);
		@TemplateString("[{{p}}]")
		Fragment outside(@N("p") Fragment p);
	}
	
	@Test
	public void testExample3() {
		Example3 t = Simplet.simplet().template(Example3.class);
		fragment("outside")
			.param("p", 
			  fragment("inside")
				  .param("q", "text"))
			.resultEquals("[(text)]")
			.assertOn(t.outside(t.inside("text")));	
	}
	
/*
We can also override and extend interfaces, although I'm not sure if this is actually a good idea yet.  
This example uses outside() template from the previous example and overrides the inside() method.  It works
just as we'd expect. 
*/

	static interface Example4 extends Example3 {
		@TemplateString("|{{q}}|")
		Fragment inside(@N("q") String q);
	}

	@Test
	public void testExample4() {
		Example4 t = Simplet.simplet().template(Example4.class);
		fragment("outside")
			.param("p", 
			  fragment("inside")
				  .param("q", "text"))
			.resultEquals("[|text|]")
			.assertOn(t.outside(t.inside("text")));	
	}
	
/*
So that's it for the introduction.  I'm sure you'll think of other crazy ways to use this.

- John
*/
	
}

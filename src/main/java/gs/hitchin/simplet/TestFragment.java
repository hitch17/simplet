package gs.hitchin.simplet;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class TestFragment {

	private final String name;
	private final Map<String, Object> params = newHashMap();
	private final List<String> contains = newLinkedList();
	private String expected;

	public TestFragment(String name) {
		this.name = name;
	}

	public TestFragment assertOn(Fragment f) {
		assertEquals(name, f.getMethod().getName());
		for (Entry<String, Object> e : params.entrySet()) {
			Object o = e.getValue();
			assertTrue(String.format("Fragment [%s] was expected to have variable [%s]", name, e.getKey()), 
					f.getContext().containsKey(e.getKey()));
			if (o instanceof TestFragment) {
				TestFragment test = (TestFragment) o;
				test.assertOn((Fragment) f.getContext().get(e.getKey()));
			} else {
				assertEquals(String.format("Parameter %s.%s", name, e.getKey()), 
						o, f.getContext().get(e.getKey()));
			}
		}
		Set<String> leftovers = newHashSet(f.getContext().keySet());
		leftovers.removeAll(params.keySet());
		assertTrue(String.format("Fragment [%s] has untested params: %s", 
				name, leftovers),
				leftovers.isEmpty());
		String result = f.getResult().toString();
		for (String s : contains) {
			assertTrue(String.format("Result should contain \"%s\"", s), result.contains(s));
		}
		if (expected != null) {
			assertTrue(String.format("Result should equal \"%s\"", result), result.contentEquals(expected));
		}
		return this;
	}

	public TestFragment param(String variable, Object value) {
		params.put(variable, value);
		return this;
	}
	
	public TestFragment resultContains(String string) {
		contains.add(string);
		return this;
	}

	public TestFragment resultEquals(String expected) {
		this.expected = expected;
		return this;
	}
	
	public static TestFragment fragment(String name) {
		return new TestFragment(name);
	}

}

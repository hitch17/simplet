package gs.hitchin.simplet;

import java.lang.reflect.Method;
import java.util.Map;

public class Fragment {

	private final Method method;
	private final Map<String, Object> context;
	private final StringBuilder result;

	public Fragment(Method method, Map<String, Object> context, StringBuilder result) {
		this.method = method;
		this.context = context;
		this.result = result;
	}

	public Method getMethod() {
		return method;
	}

	public Map<String, Object> getContext() {
		return context;
	}

	public StringBuilder getResult() {
		return result;
	}
	
	@Override
	public String toString() {
		return String.format("{%s: %s}", method.getName(), context);
	}

}

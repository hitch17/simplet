package gs.hitchin.simplet;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;
import java.util.Set;

public interface TemplateAnnotationHandler {
	public Template getTemplate(Method method, Map<URI, TemplateContainer> cache, Set<String> vars);
}

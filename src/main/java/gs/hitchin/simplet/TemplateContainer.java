package gs.hitchin.simplet;

import static java.util.Collections.unmodifiableMap;

import java.net.URI;
import java.util.Map;

public class TemplateContainer {

	private final URI uri;
	private final Map<String, Template> templates;

	public TemplateContainer(URI uri, Map<String, Template> templates) {
		this.uri = uri;
		this.templates = templates;
	}

	public Template getTemplate(String template) {
		return templates.get(template);
	}

	public URI getURI() {
		return uri;
	}

	public Map<String, Template> getTemplates() {
		return unmodifiableMap(templates);
	}
	
}

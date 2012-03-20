package gs.hitchin.simplet;


import java.util.List;
import java.util.Set;

public class Template {

	private final String id;
	private final Set<String> variables;
	private final List<Content> content;

	public Template(String id, Set<String> variables, List<Content> content) {
		this.id = id;
		this.variables = variables;
		this.content = content;
	}

	public String getId() {
		return id;
	}

	public Set<String> getVariables() {
		return variables;
	}

	public List<Content> getContent() {
		return content;
	}

}

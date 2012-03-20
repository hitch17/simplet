package gs.hitchin.simplet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.base.Objects;

public interface Content {
	
	public void render(Appendable result, Map<String, String> context) throws IOException;
	
	public static class Literal implements Content {
		final CharSequence value;
		public Literal(CharSequence value) {
			this.value = value;
		}
		@Override
		public void render(Appendable result, Map<String, String> context) throws IOException {
			result.append(value);
		}
		@Override
		public String toString() {
			return String.format("{Lit:'%s'}", value) ;
		}
		@Override
		public boolean equals(Object obj) {
	    return this == obj || (obj instanceof Literal && Objects.equal(value, ((Literal)obj).value));
		}
		@Override
		public int hashCode() {
			return Objects.hashCode(value);
		}
	}
	
	public static class Variable implements Content {
		final String name;
		public Variable(String name) {
			this.name = name;
		}
		@Override
		public void render(Appendable result, Map<String, String> context) throws IOException {
			result.append(context.get(name));
		}
		public String getName() {
			return name;
		}
		@Override
		public String toString() {
			return String.format("{Var:'%s'}", name) ;
		}
		@Override
		public boolean equals(Object obj) {
			return this == obj || (obj instanceof Variable && Objects.equal(name, ((Variable)obj).name));
		}
		@Override
		public int hashCode() {
			return Objects.hashCode(name);
		}
	}
	
	public static class ContentList implements Content {
		final Iterable<Content> content;
		public ContentList(Iterable<Content> content) {
			this.content = content;
		}
		@Override
		public void render(Appendable result, Map<String, String> context) throws IOException {
			for (Content c : content) {
				c.render(result, context);
			}
		}
	}
	
	static class TemplateFragment {
		private String id;
		private List<Content> content;
		public TemplateFragment(String id, List<Content> content) {
			this.id = id;
			this.content = content;
		}
		@Override
		public String toString() {
			return String.format("{%s=%s}", id, content);
		}
		public String getId() {
			return id;
		}
		public List<Content> getContent() {
			return content;
		}
	}
	
}

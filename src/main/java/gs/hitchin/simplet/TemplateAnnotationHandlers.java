package gs.hitchin.simplet;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import gs.hitchin.simplet.ann.TemplateFile;
import gs.hitchin.simplet.ann.TemplateResource;
import gs.hitchin.simplet.ann.TemplateString;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.google.common.io.Resources;

public class TemplateAnnotationHandlers {

	public static final List<TemplateAnnotationHandler> defaultHandlers = ImmutableList.of(
			templateStringHandler(),
			templateFileHandler(),
			templateResourceHandler()); 

	public static TemplateAnnotationHandler templateStringHandler() {
		return new TemplateAnnotationHandler() {
			@Override
			public Template getTemplate(
					Method method, Map<URI, TemplateContainer> cache, Set<String> vars) {
				TemplateString a = method.getAnnotation(TemplateString.class);
				if (a == null) {
					return null;
				}
				final Template template = TemplateParser.fromString(a.value()); 
				for (String n : template.getVariables()) {
					checkArgument(vars.contains(n), 
							"Template for method [%s] was expected to have variable [%s]", method, n);
				}
				return template;
			}
		};
	}

	public static TemplateAnnotationHandler templateFileHandler() {
		return new TemplateAnnotationHandler() {
			@Override
			public Template getTemplate(
					Method method, Map<URI, TemplateContainer> cache, Set<String> vars) {
				TemplateFile a = method.getAnnotation(TemplateFile.class);
				if (a == null) {
					return null;
				}
				File f = new File(a.file());
				checkState(f.exists(), "File [%s] does not exist.", a.file());
				URI uri = f.toURI();
				TemplateContainer container = cache.get(f.toURI());
				if (container == null) {
					container = TemplateParser.fromStream(uri, Files.newInputStreamSupplier(f));
					cache.put(container.getURI(), container);
				}
				final Template template = checkNotNull(
						container.getTemplate(a.template()), 
						"Resource [%s] does not contain template [%s].", a.file(), a.template());
				for (String n : template.getVariables()) {
					checkArgument(vars.contains(n), 
							"File [%s] template [%s] was expected to have variable [%s]", a.file(), a.template(), n);
				}
				return template;
			}
		};
	}

	public static TemplateAnnotationHandler templateResourceHandler() {
		return new TemplateAnnotationHandler() {
			@Override
			public Template getTemplate(
					Method method, Map<URI, TemplateContainer> cache, Set<String> vars) {
				TemplateResource a = method.getAnnotation(TemplateResource.class);
				if (a == null) {
					return null;
				}
				URL resourceUrl = Resources.getResource(a.resource());
				URI uri = toURI(resourceUrl);
				TemplateContainer container = cache.get(uri);
				if (container == null) {
					container = TemplateParser.fromStream(uri, Resources.newInputStreamSupplier(resourceUrl));
					cache.put(container.getURI(), container);
				}
				final Template template = checkNotNull(
						container.getTemplate(a.template()), 
						"Resource [%s] does not contain template [%s].", a.resource(), a.template());
				for (String n : template.getVariables()) {
					checkArgument(vars.contains(n), 
							"Resource [%s] template [%s] was expected to have variable [%s]", a.resource(), a.template(), n);
				}
				return template;
			}
		};
	}

	static URI toURI(URL resourceUrl) {
		try {
			return resourceUrl.toURI();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

}

package gs.hitchin.simplet;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static com.google.common.primitives.Primitives.allPrimitiveTypes;
import static com.google.common.primitives.Primitives.allWrapperTypes;
import static java.lang.String.format;
import static java.lang.reflect.Proxy.newProxyInstance;
import gs.hitchin.simplet.ann.N;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;


public class Simplet {

	public static final Set<Class<?>> ACCEPTED_PARAMS = ImmutableSet.<Class<?>>builder()
			.addAll(allPrimitiveTypes())
			.addAll(allWrapperTypes())
			.add(CharSequence.class)
			.add(StringBuilder.class)
			.add(String.class)
			.add(Fragment.class)
			.build();

	public static final Set<Class<?>> ACCEPTED_RETURNS = ImmutableSet.<Class<?>>builder()
			.add(CharSequence.class)
			.add(Appendable.class)
			.add(StringBuilder.class)
			.add(String.class)
			.add(Fragment.class)
			.build();

	public static final List<TemplateAnnotationHandler> handlers = TemplateAnnotationHandlers.defaultHandlers;

	private final ConcurrentHashMap<URI, TemplateContainer> cache = 
			new ConcurrentHashMap<URI,TemplateContainer>();
	
	public <T> T template(Class<T> clazz) {
		List<Method> methods = Arrays.asList(clazz.getMethods());
		final Map<Method, TemplateRenderer> calls = newHashMapWithExpectedSize(methods.size());
		for (Method m : methods) {
			checkArgument(isReturnAccepted(m.getReturnType()), "Method [%s] must return one of %s", m.getName(), ACCEPTED_RETURNS);
			calls.put(m, getRendererForMethod(m));
		}
		return getProxy(clazz, calls);
	}
	
	@SuppressWarnings("unchecked")
	<T> T getProxy(Class<T> clazz, final Map<Method, TemplateRenderer> calls) {
		return (T) newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[] {clazz}, new InvocationHandler() {
			@Override
			public Object invoke(Object instance, Method method, Object[] params) throws Throwable {
				return calls.get(method).render(params);
			}
		});
	}

	TemplateRenderer getRendererForMethod(Method input) {
		Class<?>[] types = input.getParameterTypes();
		Annotation[][] annotations = input.getParameterAnnotations();
		Map<Integer, String> lookup = newHashMapWithExpectedSize(types.length);
		parameter: for (int i = 0; i < types.length; i++) {
			Class<?> type = types[i];
			checkArgument(isParamAccepted(type), "Method [%s] has unsupported param [%s]", input.getName(), type);
			for (Annotation a : annotations[i]) {
				if (a.annotationType() == N.class) {
					N name = (N) a;
					checkArgument(!lookup.containsKey(name.value()), 
							"Method [%s] has multiple params with same name [%s]", input.getName(), name.value());
					lookup.put(i, name.value());
					continue parameter;
				}
			}
			throw new IllegalArgumentException(format("Method [%s] has unnamed parameter (no @N annotation)", input.getName()));
		}

		Template template = null;
		Iterator<TemplateAnnotationHandler> checkHandlers = handlers.iterator();
		while (template == null && checkHandlers.hasNext()) {
			TemplateAnnotationHandler h = checkHandlers.next();
			template = h.getTemplate(input, cache, ImmutableSet.copyOf(lookup.values()));
		}
		checkArgument(template != null, "Method [%s] is not annotated with a TemplateProvider.", input.getName());

		return getRenderer(input, input.getReturnType(), template, lookup);
	}

	TemplateRenderer getRenderer(
			final Method method, 
			final Class<?> returnType, 
			final Template template, 
			final Map<Integer, String> lookup) {
		return new TemplateRenderer() {
			@Override
			public Object render(Object[] params) {
				Builder<String, String> t = ImmutableMap.builder();
				if (params != null) {
					for (int i = 0; i < params.length; i++) {
						Object p = checkNotNull(params[i], "Method [%s] was passed null to parameter #%s.", method, i);
						if (p instanceof Fragment) {
							t.put(lookup.get(i), ((Fragment)p).getResult().toString());
						} else {
							t.put(lookup.get(i), p.toString());
						}
					}
				}
				Map<String, String> context = t.build();
				try {
					final StringBuilder b = new StringBuilder();
					for (Content c : template.getContent()) {
						c.render(b, context);
					}
					
					if (returnType == String.class) {
						return b.toString();
					} else if (returnType == Fragment.class) {
						Builder<String, Object> objects = ImmutableMap.builder();
						if (params != null) {
							for (int i = 0; i < params.length; i++) {
								objects.put(lookup.get(i), params[i]);
							}
						}
						return new Fragment(method, objects.build(), b);
					} else {
						return b;
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

	public boolean isReturnAccepted(Class<?> returnType) {
		return ACCEPTED_RETURNS.contains(returnType);
	}

	public boolean isParamAccepted(Class<?> type) {
		return ACCEPTED_PARAMS.contains(type);
	}

	public static Simplet simplet() {
		return new Simplet();
	}

}

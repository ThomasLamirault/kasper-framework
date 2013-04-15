// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================

package com.viadeo.kasper.tools;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;

/**
 *
 * Utility class used to retrieve types of parameterized classes
 *
 */
public final class ReflectionGenericsResolver {

	private ReflectionGenericsResolver() { /* singleton */ }
	
	/**
	 * @param runtimeType the runtime class to be analyzed
	 * @param targetType the target type to resolve the runtimeType against
	 * @param nbParameter the generic parameter position on the targetType
	 * 
	 * @return the (optional) type of the resolved parameter at specific position
	 * 
	 * ex:
	 * targetClass implements targetType<Integer, String>
	 * getParameterTypeFromClass(targetClass, targetType, 1) ==> String
	 *
	 * ex:
	 * targetClass extends temporary<Integer, String>
	 * temporary<R, B> implements targetType<A,G>
	 * getParameterTypeFromClass(targetClass, targetType, 0) ==> Integer
	 * 
	 */
	@SuppressWarnings("rawtypes")
	static public Optional<? extends Class> getParameterTypeFromClass(final Type runtimeType, final Type targetType,
			final Integer nbParameter) {

		// Boot recursive process with an empty bindings maps
		return ReflectionGenericsResolver.getParameterTypeFromClass(
				runtimeType, targetType, nbParameter, new HashMap<Type, Type>());

	}

	// ========================================================================

	/**
	 * Get class from type, taking care of parameterized types
	 *
	 * @param _type a type or class to be resolved
	 * 
	 * @return the (optional) class
	 * 
	 */
	@SuppressWarnings("rawtypes")
	public static final Optional<Class> getClass(final Type _type) {
		final Optional<Class> ret;
		if (_type instanceof Class) {
			ret = Optional.of((Class) _type);
		} else if (_type instanceof ParameterizedType) {
			ret = ReflectionGenericsResolver.getClass(((ParameterizedType) _type).getRawType());
		} else {
			ret = Optional.absent();
		}
		return ret;
	}

	// ------------------------------------------------------------------------

	/**
	 * Resolve generic parameters bindings during class hierarchy traversal
	 * 
	 * @param classType the class to be analyzed
	 * @param bindings the bindings map to fill
	 * 
	 */
	static private void fillBindingsFromClass(final Type classType, final Map<Type, Type> bindings) {
		if (classType instanceof ParameterizedType) {
			final Type[] paramTypes = ((ParameterizedType) classType).getActualTypeArguments();
			final Class<?> rawClass = (Class<?>) ((ParameterizedType) classType).getRawType();
			final Type[] rawTypes = rawClass.getTypeParameters();

			int i = 0;
			for (final Type rawType : rawTypes) {
				if (!ReflectionGenericsResolver.getClass(rawType).isPresent()) {
					Type bindType = paramTypes[i];
					if (!ReflectionGenericsResolver.getClass(bindType).isPresent()) {
						bindType = bindings.get(bindType);
					}
					bindings.put(rawType, bindType);
				}
				i++;
			}
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Algorithm body - recurse on class hierarchy taking care of type bindings
	 * 
	 * @param runtimeType the runtime class to be analyzed
	 * @param targetType the target type to resolve the runtimeType against
	 * @param nbParameter the generic parameter position on the targetType
	 * @param bindings the current type binding map
	 * 
	 * @return the (optional) resolved type
	 * 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static private Optional<Class> getParameterTypeFromClass(final Type runtimeType, final Type targetType,
			final Integer nbParameter, final Map<Type, Type> bindings) {

		final Optional<Class> runtimeClass = ReflectionGenericsResolver.getClass(runtimeType);
		final Optional<Class> targetClass = ReflectionGenericsResolver.getClass(targetType);

		if (!runtimeClass.isPresent() || !targetClass.isPresent()) {
			return Optional.absent();
		}

		if (!targetClass.get().isAssignableFrom(runtimeClass.get())) {
			return Optional.absent();
		}

		// First step : directly accessible information ---------------------------
		ReflectionGenericsResolver.fillBindingsFromClass(runtimeType, bindings);

		final Type[] types = runtimeClass.get().getGenericInterfaces();
		final List<Type> currentTypes = new ArrayList<Type>();
		currentTypes.add(runtimeType);
		currentTypes.addAll(Arrays.asList(types));

		for (final Type type : currentTypes) {
			if (ReflectionGenericsResolver.getClass(type).equals(targetClass) && ParameterizedType.class.isAssignableFrom(type.getClass())) {
				final ParameterizedType pt = (ParameterizedType) type;
				final Type[] parameters = pt.getActualTypeArguments();
				final Type parameter = parameters[nbParameter];

				Optional<Class> retClass = ReflectionGenericsResolver.getClass(parameter);
				if (!retClass.isPresent()) {
					retClass = ReflectionGenericsResolver.getClass(bindings.get(parameter));
				}

				return retClass;
			}
		}

		// Second step : parent and implemented interfaces ------------------------
		final Type parent = runtimeClass.get().getGenericSuperclass();
		final Type[] interfaces = runtimeClass.get().getGenericInterfaces();
		final List<Type> proposalTypes = new ArrayList<Type>();
		proposalTypes.add(parent);
		proposalTypes.addAll(Arrays.asList(interfaces));

		for (final Type proposalType : proposalTypes) {
			if (null != proposalType) {
				ReflectionGenericsResolver.fillBindingsFromClass(proposalType, bindings);

				final Optional<Class> retClass =
						ReflectionGenericsResolver.getParameterTypeFromClass(proposalType, targetType, nbParameter,	bindings);

				if (retClass.isPresent()) {
					return retClass;
				}

			}
		}

		return Optional.absent();
	}

}

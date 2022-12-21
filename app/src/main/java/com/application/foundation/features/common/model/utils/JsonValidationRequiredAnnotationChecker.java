package com.application.foundation.features.common.model.utils;

import androidx.annotation.Nullable;
import com.application.foundation.utils.DateYYYYMMDDTHHMMSS_SSSZ;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kotlin.Lazy;
import kotlin.properties.ReadWriteProperty;

public class JsonValidationRequiredAnnotationChecker implements JsonValidater {

	@Override
	public void validate(@Nullable Object object) throws
			AnnotationMisuseException,
			JsonValidationException {

		if (object != null &&
			!isObjectPrimitive(object) &&
			object.getClass() != String.class &&
			object.getClass() != BigDecimal.class &&
			object.getClass() != DateYYYYMMDDTHHMMSS_SSSZ.class) {

			if (object instanceof List) {
				for (Object element : (List) object) {
					validate(element);
				}
			} else if (object instanceof Set) {
				for (Object element : (Set) object) {
					validate(element);
				}
			} else if (object instanceof Map) {
				@SuppressWarnings("unchecked")
				Set<Map.Entry> set = ((Map) object).entrySet();

				for (Map.Entry entry : set) {
					validate(entry.getKey());
					validate(entry.getValue());
				}
			} else {
				if (object.getClass().getSuperclass() != null && object.getClass().getSuperclass() != Object.class) {
					validate(object.getClass().getSuperclass().getDeclaredFields(), object);
				}

				validate(object.getClass().getDeclaredFields(), object);
			}
		}
	}

	private void validate(Field[] declaredFields, @Nullable Object object) throws
			AnnotationMisuseException,
			JsonValidationException {

		for (Field field : declaredFields) {
			if ((field.getModifiers() & Modifier.STATIC) == 0 &&
				(field.getModifiers() & Modifier.TRANSIENT) == 0 &&
				 field.getType() != Lazy.class && // Lazy.class for by lazy in Kotlin, see Currency class
				 field.getType() != ReadWriteProperty.class) { // ReadWriteProperty for Delegates.notNull(), see CartContentForCheckoutResponse class

				Annotation[] annotations = field.getAnnotations();

				boolean required = false;
				boolean collectionWithoutNulls = false;
				boolean collectionIsNotEmptyAndWithoutNulls = false;

				for (Annotation annotation : annotations) {
					if (annotation instanceof Required) {
						required = true;
					}
					if (annotation instanceof CollectionWithoutNulls) {
						required = true;
						collectionWithoutNulls = true;
						break;
					}
					if (annotation instanceof CollectionIsNotEmptyAndWithoutNulls) {
						required = true;
						collectionWithoutNulls = true;
						collectionIsNotEmptyAndWithoutNulls = true;
						break;
					}
				}

				boolean isPrimitive = isFieldPrimitive(field);

				if (required && isPrimitive) {
					throw new AnnotationMisuseException("field \"" + field.getName() + "\" from class \"" + object.getClass().getName() +
							"\" is declared as required but is's a primitive. Declare a primitive as a boxed primitive for ex. as an Integer");
				}

				if (!isPrimitive) {

					if (!field.isAccessible()) {
						field.setAccessible(true);
					}

					Object value = null;
					try {
						value = field.get(object);
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}

					if (value == null && required) {
						throw new JsonValidationException("field \"" + field.getName() + "\" from class \"" + object.getClass().getName() +
								"\" is declared as required but it's null");
					}

					if (collectionWithoutNulls) {
						if (value instanceof List || value instanceof Set) {
							Collection collection = (Collection) value;

							if (collectionIsNotEmptyAndWithoutNulls && collection.isEmpty()) {
								throw new JsonValidationException("field \"" + field.getName() + "\" from class \"" + object.getClass().getName() +
										"\" is declared as a CollectionIsNotEmptyAndWithoutNulls but it's empty");
							}
							for (Object element : collection) {
								if (element == null) {
									throw new JsonValidationException("field \"" + field.getName() + "\" from class \"" + object.getClass().getName() +
											"\" is declared as a " +
											(collectionIsNotEmptyAndWithoutNulls ? "CollectionIsNotEmptyAndWithoutNulls" :
													"CollectionWithoutNulls") + " but it has a null element");
								}
							}
						} else {
							throw new AnnotationMisuseException("field \"" + field.getName() + "\" from class \"" + object.getClass().getName() +
									"\" is declared as a " +
									(collectionIsNotEmptyAndWithoutNulls ? "CollectionIsNotEmptyAndWithoutNulls" :
											"CollectionWithoutNulls") + " but it isn't a collection");
						}
					}

					validate(value);
				}
			}
		}
	}


	private static boolean isFieldPrimitive(Field field) {
		Class clazz = field.getType();

		return clazz == boolean.class ||
				clazz == byte.class ||
				clazz == short.class ||
				clazz == int.class ||
				clazz == long.class ||
				clazz == float.class ||
				clazz == double.class;
	}

	private static boolean isObjectPrimitive(Object object) {
		Class clazz = object.getClass();

		return clazz == Boolean.class ||
				clazz == Byte.class ||
				clazz == Short.class ||
				clazz == Integer.class ||
				clazz == Long.class ||
				clazz == Float.class ||
				clazz == Double.class;
	}

	public static class JsonValidationException extends RuntimeException {
		JsonValidationException(String msg) {
			super(msg);
		}
	}

	public static class AnnotationMisuseException extends RuntimeException {
		AnnotationMisuseException(String msg) {
			super(msg);
		}
	}
}
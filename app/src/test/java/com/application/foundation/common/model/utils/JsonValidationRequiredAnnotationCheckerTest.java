package com.application.foundation.common.model.utils;

import com.application.foundation.features.common.model.utils.JsonValidationRequiredAnnotationChecker;
import static com.application.foundation.features.common.model.utils.JsonValidationRequiredAnnotationChecker.JsonValidationException;
import static com.application.foundation.features.common.model.utils.JsonValidationRequiredAnnotationChecker.AnnotationMisuseException;
import org.junit.Test;

public class JsonValidationRequiredAnnotationCheckerTest {

	@Test(expected = AnnotationMisuseException.class)
	public void testPrimitiveField1() {
		new JsonValidationRequiredAnnotationChecker().validate(
				new JsonValidationRequiredAnnotationCheckerTestClasses.TestPrimitive1());
	}

	@Test(expected = AnnotationMisuseException.class)
	public void testPrimitiveField2() {
		new JsonValidationRequiredAnnotationChecker().validate(
				new JsonValidationRequiredAnnotationCheckerTestClasses.TestPrimitive2());
	}

	@Test(expected = AnnotationMisuseException.class)
	public void testPrimitiveField3() {
		new JsonValidationRequiredAnnotationChecker().validate(
				new JsonValidationRequiredAnnotationCheckerTestClasses.TestPrimitive3());
	}

	@Test
	public void testPrimitiveField4() {
		new JsonValidationRequiredAnnotationChecker().validate(
				new JsonValidationRequiredAnnotationCheckerTestClasses.TestPrimitive4());
	}


	@Test
	public void testString1() {
		new JsonValidationRequiredAnnotationChecker().validate(
				new JsonValidationRequiredAnnotationCheckerTestClasses.TestString1());
	}

	@Test
	public void testString2() {
		new JsonValidationRequiredAnnotationChecker().validate(
				new JsonValidationRequiredAnnotationCheckerTestClasses.TestString2());
	}

	@Test(expected = JsonValidationRequiredAnnotationChecker.JsonValidationException.class)
	public void testString3() {
		new JsonValidationRequiredAnnotationChecker().validate(
				new JsonValidationRequiredAnnotationCheckerTestClasses.TestString3());
	}

	@Test
	public void testString4() {
		new JsonValidationRequiredAnnotationChecker().validate(
				new JsonValidationRequiredAnnotationCheckerTestClasses.TestString4());
	}


	@Test(expected = JsonValidationException.class)
	public void testList1() {
		new JsonValidationRequiredAnnotationChecker().validate(
				new JsonValidationRequiredAnnotationCheckerTestClasses.TestList1());
	}

	@Test
	public void testList2() {
		new JsonValidationRequiredAnnotationChecker().validate(
				new JsonValidationRequiredAnnotationCheckerTestClasses.TestList2());
	}

	@Test(expected = JsonValidationException.class)
	public void testList3() {
		new JsonValidationRequiredAnnotationChecker().validate(
				new JsonValidationRequiredAnnotationCheckerTestClasses.TestList3());
	}

	@Test
	public void testList4() {
		new JsonValidationRequiredAnnotationChecker().validate(
				new JsonValidationRequiredAnnotationCheckerTestClasses.TestList4());
	}

	@Test(expected = JsonValidationException.class)
	public void testList5() {
		new JsonValidationRequiredAnnotationChecker().validate(
				new JsonValidationRequiredAnnotationCheckerTestClasses.TestList5());
	}

	@Test(expected = JsonValidationException.class)
	public void testObject1() {
		new JsonValidationRequiredAnnotationChecker().validate(
				new JsonValidationRequiredAnnotationCheckerTestClasses.TestObject1());
	}

	@Test
	public void testList6() {
		new JsonValidationRequiredAnnotationChecker().validate(
				new JsonValidationRequiredAnnotationCheckerTestClasses.TestList6());
	}

	@Test
	public void testTestListRequiredClass1() {
		new JsonValidationRequiredAnnotationChecker().validate(
				new JsonValidationRequiredAnnotationCheckerTestClasses.TestListRequiredClass());
	}
}
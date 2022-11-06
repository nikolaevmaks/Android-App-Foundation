package com.application.foundation.common.model.utils;

import com.application.foundation.features.common.model.utils.CollectionIsNotEmptyAndWithoutNulls;
import com.application.foundation.features.common.model.utils.Required;
import java.util.ArrayList;
import java.util.List;

public class JsonValidationRequiredAnnotationCheckerTestClasses {

	static class TestPrimitive1 {
		@Required
		private boolean b;
	}

	static class TestPrimitive2 {
		@Required
		private boolean b = false;
	}

	static class TestPrimitive3 {
		@Required
		private boolean b = true;
	}

	static class TestPrimitive4 {
		private boolean b;
	}


	protected static class TestString1 {
		String s;
	}

	public static class TestString2 {
		String s = "";
	}

	static class TestString3 {
		@Required
		private String s;
	}

	static class TestString4 {
		@Required
		private String s = "";
	}


	static class TestList1 {
		@Required
		private List<Stub1> list;
	}

	static class TestList2 {
		List<Stub1> list;
	}

	static class TestList3 {

		TestList3() {
			list = new ArrayList<>();
			list.add(new Stub1());
		}
		protected List<Stub1> list;
	}

	static class TestList4 {

		TestList4() {
			list = new ArrayList<>();
			Stub1 stub = new Stub1();
			stub.s2 = "";
			list.add(stub);
		}
		private List<Stub1> list;
	}

	private static class Stub1 {

		private String s;

		boolean b;

		@Required
		private String s2;
	}


	static class TestObject1 {
		@Required
		private Stub1 stub1;
	}


	static class TestList5 {

		@Required
		private static Stub1 sStub1;
		@Required
		private static String s;

		@Required
		private Stub1 stub1;

		@Required
		Integer i = 7;

		@Required
		Float f = 0.5f;

		@Required
		Double d = 3d;

		double d2 = 7.0;

		private List<List<Stub2>> list;
	}

	public static class TestList6 {

		TestList6() {
			stub1 = new Stub1();
			stub1.s2 = "";

			list = new ArrayList<>();
			for (int i = 0; i < 3; i++) {
				List<Stub2> l = new ArrayList<>();
				for (int j = 0; j < 2; j++) {
					Stub2 s = new Stub2();
					s.b = true;
					s.i1 = 0;
					s.l = 4l;
					s.s2 = "";
					l.add(s);
				}
				list.add(l);
			}
		}

		@Required
		private final static Stub1 sStub1 = new Stub1();
		@Required
		private static String s;

		@Required
		public Stub1 stub1;

		@Required
		final Integer i = 7;

		@Required
		final Float f = 0.5f;

		@Required
		Double d = 3d;

		final double d2 = 7.0;

		private List<List<Stub2>> list;
	}


	private static class Stub2 {

		private String s;

		@Required
		Integer a = 7;

		@Required
		Boolean bool = false;

		boolean b;

		@Required
		Integer i1;

		@Required
		Long l;

		@Required
		private String s2;
	}

	static class TestListRequiredClass {

		TestListRequiredClass() {
			list = new ArrayList<>();
			list.add(new StubRequired1());
		}
		@CollectionIsNotEmptyAndWithoutNulls
		protected List<StubRequired1> list;
	}

	private static class StubRequired1 {

		private String s;

		boolean b;

		@Required
		private String s2 = "";
	}
}

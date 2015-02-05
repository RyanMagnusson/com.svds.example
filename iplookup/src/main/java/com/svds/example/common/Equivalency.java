package com.svds.example.common;

import org.joda.time.DateTime;

public abstract class Equivalency<T> {

	protected Equivalency() { }
	
	public abstract boolean areEqual(T t1, T t2);
	public abstract boolean areNotEqual(T t1, T t2);
	
	public static class CaseSensitiveEquivalency extends Equivalency<CharSequence> {

		CaseSensitiveEquivalency() {}
		
		@Override
		public boolean areEqual(CharSequence text1, CharSequence text2) {
			return areEqual(text1, text2, true);
		}
		
		public boolean areEqual(CharSequence text1, CharSequence text2, boolean trimFirst) {
			if (text1 == null && text2 == null) { return true; }
			if (text1 == text2) { return true; }
			
			final String trimmed1 = trimFirst ? text1.toString().trim() : text1.toString();
			final String trimmed2 = trimFirst ? text2.toString().trim() : text2.toString();
			return trimmed1.equals(trimmed2);
		}
	
		@Override
		public boolean areNotEqual(CharSequence text1, CharSequence text2) {
			return !areEqual(text1, text2, true);
		}
		
		public boolean areNotEqual(CharSequence text1, CharSequence text2, boolean trimFirst) {
			return !areEqual(text1, text2, trimFirst);
		}
	}
	
	public static class IgnoringCaseEquivalency extends Equivalency<CharSequence> {

		IgnoringCaseEquivalency() {}
		
		@Override
		public boolean areEqual(CharSequence text1, CharSequence text2) {
			return areEqual(text1, text2, true);
		}
		
		public boolean areEqual(CharSequence text1, CharSequence text2, boolean trimFirst) {
			if (text1 == null && text2 == null) { return true; }
			if (text1 == text2) { return true; }
			
			final String trimmed1 = trimFirst ? text1.toString().trim() : text1.toString();
			final String trimmed2 = trimFirst ? text2.toString().trim() : text2.toString();
			return trimmed1.equalsIgnoreCase(trimmed2);
		}
		
		@Override
		public boolean areNotEqual(CharSequence text1, CharSequence text2) {
			return !areEqual(text1, text2, true);
		}
		
		public boolean areNotEqual(CharSequence text1, CharSequence text2, boolean trimFirst) {
			return !areEqual(text1, text2, trimFirst);
		}
	}
	
	public static class DateTimeEquivalency extends Equivalency<DateTime> {

		DateTimeEquivalency() {}
		@Override
		public boolean areEqual(DateTime t1, DateTime t2) {
			if (t1 == null && t2 == null) { return true; }
			if (t1 == t2) { return true; }
			
			if (t1.isBefore(t2)) { return false; }
			if (t1.isAfter(t2)) { return false; }
			return true;
		}

		@Override
		public boolean areNotEqual(DateTime t1, DateTime t2) {
			return !areEqual(t1,t2);
		}
		
	}
	
	public static class IntegerEquivalency extends Equivalency<Integer> {

		IntegerEquivalency() {}
		@Override
		public boolean areEqual(Integer n1, Integer n2) {
			if (n1 == null && n2 == null) { return true; }
			if (n1 == n2) { return true; }
			
			return n1.intValue() == n2.intValue();
		}

		@Override
		public boolean areNotEqual(Integer n1, Integer n2) {
			return !areEqual(n1,n2);
		}
		
	}
	
	public static IgnoringCaseEquivalency ignoringCase() {
		return new IgnoringCaseEquivalency();
	}
	
	public static CaseSensitiveEquivalency caseSensitive() {
		return new CaseSensitiveEquivalency();
	}
	
	public static DateTimeEquivalency forDateTime() {
		return new DateTimeEquivalency();
	}

	public static IntegerEquivalency forInts() {
		return new IntegerEquivalency();
	}
}

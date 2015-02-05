package com.svds.example.common;

/**
 * Contract for performing a deep-copy of the properties of a JavaBean.
 * <p />
 * This interface is a companion to the Object#clone() method.  
 * The primary purpose for the methods here is to ensure that a deep-copy 
 * of all of the properties of an object and those in its object-graph is performed.
 * <p />
 * The {@link #copy()} method also provides for type-safe copying of objects as well. 
 * <p />
 * In contrast, the Object#clone() method which only performs an initial shallow
 * copy of all of the properties of an object and can only return the type Object.  
 * The {@link #copy(Copyable)} method helps enhance the {@link #clone()} method 
 * to ensure that a deep copy can be performed.     
 * 
 * @author Ryan Magnusson
 */
public interface Copyable<T extends Copyable<T>> {

	/**
	 * Creates a new deep copy of the current object.
	 * <p />
	 * In the case of static immutables (example: enums) then the
	 * same singleton instance is returned. 
	 * 
	 * @return new deeply instance of the current object
	 * 
	 * @see Object#clone()
	 */
	T copy();
	
	/** 
	 * Performs a deep copy of the properties of the current instance to
	 * the <code>copyTo</code> argument.  
	 * <p />
	 * The general expectation is that if the argument {@code target} 
	 * is {@code null}, then the method should exit gracefully (example: do nothing)
	 * instead of throwing a NullPointerException.
	 * 
	 * @param target - the object to copy onto 
	 * 
	 * @see Object#clone()
	 */
	void copyTo(T target);
	
}

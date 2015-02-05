package com.svds.example.accesslog;

public interface Formatter<T> {

	String format(T entity);

}

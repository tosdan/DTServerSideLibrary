package com.github.tosdan.utils.varie;

import com.google.gson.GsonBuilder;

public class JsonUtils
{
	public static <T> String toJSON(T obj) {
		return new GsonBuilder().setPrettyPrinting()
								.create()
								.toJson( obj );
	}
}

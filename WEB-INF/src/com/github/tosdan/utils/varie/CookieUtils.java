package com.github.tosdan.utils.varie;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieUtils {
	
	public static String getCookieValue(HttpServletRequest req, String name) {
	    Cookie[] cookies = req.getCookies();
	    if (cookies != null) {
	        for (Cookie cookie : cookies) {
	            if (name.equals(cookie.getName())) 
	                return cookie.getValue();	            
	        }
	    }
	    return null;
	}

	public static void addCookie(HttpServletResponse resp, String name, String value, int maxAge) {
	    Cookie cookie = new Cookie(name, value);
	    cookie.setPath("/");
	    cookie.setMaxAge(maxAge);
	    resp.addCookie(cookie);
	}

	public static void removeCookie(HttpServletResponse resp, String name) {
	    addCookie(resp, name, null, 0);
	}

}

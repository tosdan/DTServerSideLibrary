package com.github.tosdan.utils.stringhe;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringTemplate {
//    final private String template;
    final private Matcher m;
    static final private Pattern keyPattern = 
        Pattern.compile("\\$\\{([a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)*)\\}");
    private boolean blanknull=false;

    public StringTemplate(String template) { 
//        this.template=template;
        this.m = keyPattern.matcher(template);
    }

    /**
     * @param map substitution map
     * @return substituted string
     */
    public String substitute(Map<String, ? extends Object> map)
    {
        this.m.reset();
        StringBuffer sb = new StringBuffer();
        while ( this.m.find() )
        {
            String k0 = this.m.group(); // ${stringaDaSostituire}
            String k = this.m.group(1); // stringaDaSostituire
            Object vobj = map.get(k);
            String v = (vobj == null) 
                ? (this.blanknull ? "" : k0)
                : vobj.toString();
            this.m.appendReplacement(sb, Matcher.quoteReplacement(v));
        }
        this.m.appendTail(sb);
        return sb.toString();       
    }

    public StringTemplate setBlankNull()
    {
        this.blanknull=true;
        return this;
    }

    static public void main(String[] args)
    {
        StringTemplate t1 = new StringTemplate("${this} is a '${test}' of the ${foo} bar=${bar} ${emergency.broadcasting.system}");
        t1.setBlankNull();
        Map<String, String> m = new HashMap<String, String>();
        m.put("this", "*This*");
        m.put("test", "*TEST*");
        m.put("foo", "$$$aaa\\\\111");
        m.put("emergency.broadcasting.system", "EBS");
        System.out.println( t1.substitute(m) );
    }
}
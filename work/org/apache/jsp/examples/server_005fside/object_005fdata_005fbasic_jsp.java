package org.apache.jsp.examples.server_005fside;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

public final class object_005fdata_005fbasic_jsp extends org.apache.jasper.runtime.HttpJspBase
    implements org.apache.jasper.runtime.JspSourceDependent {

  private static final JspFactory _jspxFactory = JspFactory.getDefaultFactory();

  private static java.util.List _jspx_dependants;

  private javax.el.ExpressionFactory _el_expressionfactory;
  private org.apache.AnnotationProcessor _jsp_annotationprocessor;

  public Object getDependants() {
    return _jspx_dependants;
  }

  public void _jspInit() {
    _el_expressionfactory = _jspxFactory.getJspApplicationContext(getServletConfig().getServletContext()).getExpressionFactory();
    _jsp_annotationprocessor = (org.apache.AnnotationProcessor) getServletConfig().getServletContext().getAttribute(org.apache.AnnotationProcessor.class.getName());
  }

  public void _jspDestroy() {
  }

  public void _jspService(HttpServletRequest request, HttpServletResponse response)
        throws java.io.IOException, ServletException {

    PageContext pageContext = null;
    HttpSession session = null;
    ServletContext application = null;
    ServletConfig config = null;
    JspWriter out = null;
    Object page = this;
    JspWriter _jspx_out = null;
    PageContext _jspx_page_context = null;


    try {
      response.setContentType("text/html");
      pageContext = _jspxFactory.getPageContext(this, request, response,
      			null, true, 8192, true);
      _jspx_page_context = pageContext;
      application = pageContext.getServletContext();
      config = pageContext.getServletConfig();
      session = pageContext.getSession();
      out = pageContext.getOut();
      _jspx_out = out;

      out.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n");
      out.write("<html>\n");
      out.write("\t<head>\n");
      out.write("\t\t<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />\n");
      out.write("\t\t<link rel=\"shortcut icon\" type=\"image/ico\" href=\"./images/favicon.ico\" /> \n");
      out.write("\t\t\n");
      out.write("\t\t<title>DataTables basic example</title>\n");
      out.write("\t\t\n");
      out.write("\t\t<style type=\"text/css\" title=\"currentStyle\">\n");
      out.write("\t\t\t@import \"./css/object_data.css\";\n");
      out.write("\t\t</style>\n");
      out.write("\t\t\n");
      out.write("\t\t<script type=\"text/javascript\" src=\"./js-3rd-party/jquery-1.9.1.js\"></script>\n");
      out.write("\t\t<script type=\"text/javascript\" src=\"./js-3rd-party/jquery.dataTables.js\"></script>\n");
      out.write("\t\t\n");
      out.write("\t\t<script type=\"text/javascript\" src=\"./js/fnJQDisableTextSelect.js\"></script>\n");
      out.write("\t\t<script type=\"text/javascript\" src=\"./js/dtCustomFunctionLibrary.js\"></script>\n");
      out.write("\t\t\n");
      out.write("\t\t<script type=\"text/javascript\" src=\"./js/fnSetActionPulsanteElimina.js\"></script>\n");
      out.write("\t\t<script type=\"text/javascript\" src=\"./js/setActionGenericoPulsanteAzione.js\"></script> \n");
      out.write("\t\t<script type=\"text/javascript\" src=\"./js/fnConfiguraCustomToobar_basic.js\"></script> <!-- Configura pulsanti e azioni collegate  --> \n");
      out.write("\t\t\n");
      out.write("\t\t<script type=\"text/javascript\" src=\"./js/fnServerParams_basic.js\"></script>\n");
      out.write("\t\t<script type=\"text/javascript\" src=\"./js/object_data_basic.js\"></script>\n");
      out.write("\n");
      out.write("\t</head>\n");
      out.write("\t\n");
      out.write("\t<body id=\"dt_example\">\n");
      out.write("\t\t<div id=\"container\">\n");
      out.write("\t\t\t<div class=\"full_width big\">\n");
      out.write("\t\t\t\tDataTables Esempio server-side Basic - sorgente dati JSON\n");
      out.write("\t\t\t</div>\n");
      out.write("\t\t\t\n");
      out.write("\t\t\t<h1>Esempio Live</h1>\n");
      out.write("\t\t\t<div id=\"dynamic\">\n");
      out.write("\t\t\t\n");
      out.write("<!-- ***\t\t\tStruttura statica della tabella\t\t**** START -->\n");
      out.write("\t\t\t<table cellpadding=\"0\" width=\"100%\" cellspacing=\"0\" border=\"0\" class=\"display\" id=\"example\">\n");
      out.write("\t\t\t\n");
      out.write("\t\t\t\t<thead>\n");
      out.write("\t\t\t\t\t<tr>\n");
      out.write("\t\t\t\t\t\t<th width=\"25%\">Rendering engine</th>\n");
      out.write("\t\t\t\t\t\t<th width=\"20%\">Browser</th>\n");
      out.write("\t\t\t\t\t\t<th width=\"25%\">Platform(s)</th>\n");
      out.write("\t\t\t\t\t\t<th width=\"16%\">Engine version</th>\n");
      out.write("\t\t\t\t\t\t<th width=\"10%\">CSS grade</th>\n");
      out.write("\t\t\t\t\t</tr>\n");
      out.write("\t\t\t\t</thead>\n");
      out.write("\t\t\t\t\n");
      out.write("\t\t\t\t\n");
      out.write("\t\t\t\t<tbody>\n");
      out.write("\t\t\t\t\t<tr>\n");
      out.write("\t\t\t\t\t\t<td colspan=\"5\" class=\"dataTables_empty\">Loading data from server</td>\n");
      out.write("\t\t\t\t\t</tr>\n");
      out.write("\t\t\t\t</tbody>\n");
      out.write("\t\t\t\t\n");
      out.write("\t\t\t</table>\n");
      out.write("<!-- ***\t\t\tStruttura statica della tabella \t*** END -->\t\n");
      out.write("\t\n");
      out.write("\t\t\t</div>\n");
      out.write("\t\t\t<div class=\"spacer\"></div>\n");
      out.write("\t\t</div>\n");
      out.write("\t</body>\n");
      out.write("</html>");
    } catch (Throwable t) {
      if (!(t instanceof SkipPageException)){
        out = _jspx_out;
        if (out != null && out.getBufferSize() != 0)
          try { out.clearBuffer(); } catch (java.io.IOException e) {}
        if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);
      }
    } finally {
      _jspxFactory.releasePageContext(_jspx_page_context);
    }
  }
}

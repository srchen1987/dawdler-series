package com.anywide.web.listener;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebListener;

import com.anywide.dawdler.clientplug.velocity.PageStyle;
import com.anywide.dawdler.clientplug.web.listener.WebContextListener;
import com.anywide.dawdler.clientplug.web.validator.RuleOperatorProvider;
@WebListener
public class SystemListener implements WebContextListener {

	@Override
	public void contextDestroyed(ServletContext context) {
		
	}

	@Override
	public void contextInitialized(ServletContext context) {
		RuleOperatorProvider.help();//输出验证规则
		PageStyle.export("mystyle",
						// "<ul id=\"pagination-digg\">",
						"<li class=\"paginate_button previous disabled\" id=\"datatable-responsive_previous\"><a aria-controls=\"datatable-responsive\" data-dt-idx=\"0\" tabindex=\"0\" href=\"" + PageStyle.CONTENTMARK+ "\">首页</a></li>",
						"<li class=\"paginate_button previous disabled\" id=\"datatable-responsive_previous\"><a aria-controls=\"datatable-responsive\" data-dt-idx=\"0\" tabindex=\"0\" href=\""+ PageStyle.CONTENTMARK + "\">上一页</a></li>",
						"<li class=\"paginate_button \"><a aria-controls=\"datatable-responsive\" data-dt-idx=\"2\" tabindex=\"0\" href=\""+ PageStyle.CONTENTMARK + "\">"+ PageStyle.PMARK + "</a></li>",
						"<li class=\"paginate_button active\"><a aria-controls=\"datatable-responsive\" data-dt-idx=\"1\" tabindex=\"0\" href=\"javascript:;\">"+ PageStyle.PMARK + "</a></li>",
						"<li class=\"paginate_button next\" id=\"datatable-responsive_next\"><a aria-controls=\"datatable-responsive\" data-dt-idx=\"7\" tabindex=\"0\" href=\""+ PageStyle.CONTENTMARK + "\">下一页</a></li>",
						"<li class=\"paginate_button next\" id=\"datatable-responsive_next\"><a aria-controls=\"datatable-responsive\" data-dt-idx=\"7\" tabindex=\"0\" style=\"margin-left:5px\" href=\""+ PageStyle.CONTENTMARK + "\">尾页</a></li>"
						// "</ul>"
						, null);
	}

}
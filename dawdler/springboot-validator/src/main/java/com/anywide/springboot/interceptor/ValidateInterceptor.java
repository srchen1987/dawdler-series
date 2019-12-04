package com.anywide.springboot.interceptor;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import com.anywide.dawdler.clientplug.web.util.JsonProcessUtil;
import com.anywide.dawdler.clientplug.web.validator.ValidateParser;
import com.anywide.dawdler.clientplug.web.validator.entity.ControlField;
import com.anywide.dawdler.clientplug.web.validator.entity.ControlValidator;
import com.anywide.dawdler.clientplug.web.validator.webbind.ValidateResourceLoader;
import com.anywide.springboot.annotation.RequestMappingAssist;
import com.anywide.springboot.filter.ValidateFilter.BodyReaderHttpServletRequestWrapper;
public class ValidateInterceptor implements HandlerInterceptor {
	private static ConcurrentHashMap<Class, ControlValidator> validators = new ConcurrentHashMap<>();
	private static DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
	public static final String MIME_TYPE_JSON="application/json;charset=UTF-8";
	public static final String VALIDATEERROR="validate_error";
	static{
		diskFileItemFactory.setSizeThreshold(1024*1024);
	}
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
    	boolean dovalidate=o instanceof HandlerMethod;
		if(dovalidate){
			HandlerMethod m = (HandlerMethod)o;
			RequestMappingAssist ra = m.getMethodAnnotation(RequestMappingAssist.class);
			boolean jsonBody = request instanceof BodyReaderHttpServletRequestWrapper;
			Map<String, String> errors = new HashMap<String, String>();
			ControlValidator cv = null;
			Class clazz = m.getBean().getClass();
			cv = validators.get(clazz);
			if(cv==null){
				cv = ValidateResourceLoader.getControlValidator(clazz);
				if(cv==null)cv = new ControlValidator();
				ControlValidator preCv = validators.putIfAbsent(clazz, cv);
				if(preCv!=null)cv = preCv;
			}
			String uri = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
			if(!cv.isValidate()||uri==null)return true;
			Map<String,ControlField> rules =  cv.getMappings().get(uri);
			if(rules==null)rules=cv.getGlobalControlFields();
			if(rules!=null){
				if(ra!=null&&ra.generateValidator()&&!rules.isEmpty()){
					StringBuffer sb = new StringBuffer("sir_validate.addRule(");
					Collection<ControlField> cc = rules.values();
					List list = new ArrayList();
					for(Iterator<ControlField> it = cc.iterator();it.hasNext();){
						Map map = new LinkedHashMap();
						ControlField cf = it.next();
						map.put("id",cf.getFieldName());
						map.put("viewname",cf.getFieldExplain());
						map.put("validaterule",cf.getRules());
						list.add(map);
					}
					sb.append(JsonProcessUtil.beanToJson(list));
					sb.append(");\n");
					sb.append("sir_validate.buildFormValidate($formid);");
					System.out.println("######################################");
					System.out.println(sb.toString());
					System.out.println("######################################");
				}
				Set<Entry<String,ControlField>> set = rules.entrySet();
				for(Entry<String,ControlField> entry:set){
					String key = entry.getKey();
					ControlField cf = entry.getValue();
					Map params = parserRequest(jsonBody, request);
					if(params==null)params = new HashMap();
					String error = ValidateParser.validate(cf.getFieldExplain(),params.get(cf.getFieldName()),cf.getRules());
					if(error!=null)errors.put(key,error);
				}
			}
			if(!errors.isEmpty()) {
				if(ra!=null&&ra.input()!=null&&!ra.input().trim().equals("")){
					request.setAttribute(VALIDATEERROR, errors);
					request.getRequestDispatcher(ra.input()).forward(request, response);
				}else{
					response.setContentType(MIME_TYPE_JSON);
					PrintWriter out =  response.getWriter();
					Map map = new HashMap<>();
					map.put(VALIDATEERROR, errors);
					out.print(JsonProcessUtil.beanToJson(map));
					out.flush();
					out.close();
				}
				return false;
			}
		}
		return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
    private static Map parserRequest(boolean jsonType, HttpServletRequest request) throws FileUploadException, UnsupportedEncodingException {
		Map params = new HashMap();
		if (jsonType) {
//			request.getInputStream();
			
//			if(request.getClass().isAssignableFrom(BodyReaderHttpServletRequestWrapper.class)) {
				BodyReaderHttpServletRequestWrapper rw = (BodyReaderHttpServletRequestWrapper) request;
				Map map = JsonProcessUtil.jsonToBean(rw.getBody(),HashMap.class);
				if(map!=null) {
					params.putAll(request.getParameterMap());
					params.putAll(map);
				}else {
					params = request.getParameterMap();
				}
//			}
		}else {
			boolean isMultipart = ServletFileUpload.isMultipartContent(request);
			if (isMultipart) {
				if (request instanceof MultipartHttpServletRequest) {
					MultipartHttpServletRequest re = (MultipartHttpServletRequest) request;
					params = re.getParameterMap();
				} else {
					ServletFileUpload upload = new ServletFileUpload(diskFileItemFactory);
					List items = upload.parseRequest(request);
					Iterator iterator = items.iterator();
					Map fileParams = new HashMap();
					while (iterator.hasNext()) {
						FileItem item = (FileItem) iterator.next();
						if (!item.isFormField()) {
							if ((item.getSize() > 0 && item.getName() != null && !item
									.getName().trim().equals(""))) {
								List list = (List) fileParams.get(item
										.getFieldName());
								if (list == null) {
									list = new ArrayList();
									fileParams.put(item.getFieldName(), list);
								}
								list.add(item);
							}
						} else {
							String fieldName = item.getFieldName();
							List list = (List) params.get(item.getFieldName());
							if (list == null) {
								list = new ArrayList();
								params.put(item.getFieldName(), list);
							}
							list.add(item.getString("utf-8"));
						}
					}
				}
			} else {
				params = request.getParameterMap();
			}
		}
		return params;
	}

}
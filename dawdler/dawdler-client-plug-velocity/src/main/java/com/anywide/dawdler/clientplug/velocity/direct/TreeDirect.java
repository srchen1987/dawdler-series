/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.anywide.dawdler.clientplug.velocity.direct;

import com.anywide.dawdler.util.ToolEL;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * @author jackson.song
 * @version V1.0
 * @Title TreeDirect.java
 * @Description 数型结构的指令 （注释后补的）
 * @date 2007年04月18日
 * @email suxuan696@gmail.com
 */
public class TreeDirect extends Directive {
    @Override
    public String getName() {
        return "tree";
    }

    @Override
    public int getType() {
        return BLOCK;
    }

    @Override
    public boolean render(InternalContextAdapter arg0, Writer arg1, Node arg2)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        if (arg2.jjtGetNumChildren() != 3) {
            throw new ParseErrorException("must has #tree(list,explain) #end !");
        }
        Node rootNode = null;
        try {
            rootNode = arg2.jjtGetChild(0);
        } catch (NullPointerException e) {
            throw new ParseErrorException("list data can't null !");
        }
        List item = (List) rootNode.value(arg0);
        String explain = (String) arg2.jjtGetChild(1).value(arg0);
        if (explain == null) {
            throw new ParseErrorException("explain can't null !");
        }
        String parentname = null;
        String childname = null;
        String parentvalue = "0";
        String remark = "- ";
        String var = null;
        String lastnode = "lastnode";
        String index = "index";
//		String explain="parentname=,childname=,parentvalue=,remark=";
        String[] values = explain.split(",");
        for (String value : values) {
            String[] vs = value.split("=");
            if (vs.length != 2)
                throw new ParseErrorException(
                        "must like explain for example var=tempdata,parentname=id,childname=parendid,parentvalue=0,remark=- ");
            if (vs[0].trim().equals("parentname")) {
                parentname = vs[1];
            }
            if (vs[0].trim().equals("childname")) {
                childname = vs[1];
            }
            if (vs[0].trim().equals("parentvalue")) {
                parentvalue = vs[1];
            }
            if (vs[0].trim().equals("remark")) {
                remark = vs[1];
            }
            if (vs[0].trim().equals("var")) {
                var = vs[1];
            }
            if (vs[0].trim().equals("lastnode")) {
                lastnode = vs[1];
            }
            if (vs[0].trim().equals("index")) {
                index = vs[1];
            }
        }
        if (parentname == null) {
            throw new ParseErrorException("parentname can't null!");
        }
        if (childname == null) {
            throw new ParseErrorException("childname can't null!");
        }
        if (childname == null) {
            throw new ParseErrorException("var can't null!");
        }
        Map<String, List> map = new HashMap();
        int currentnumber = 0;
        for (Iterator it = item.iterator(); it.hasNext(); ) {
            Object o = it.next();
            Object ko = ToolEL.getBeanValue(o, childname);
            if (ko == null)
                throw new ParseErrorException("no find " + childname + " propertie in " + o.getClass().getName());
            String key = ko.toString();
            List value = map.get(key);
            if (value == null) {
                value = new ArrayList();
                value.add(o);
                map.put(key, value);
            } else {
                value = map.get(key);
                value.add(o);
            }
        }
        List targetdata = new ArrayList();
//		StringBuffer sb = new StringBuffer();
        String sb = "";
        createTree(map, targetdata, parentname, parentvalue, sb, remark);
        Node node2 = arg2.jjtGetChild(2);
        int i = 1;
        for (Object o : targetdata) {
            Object[] os = (Object[]) o;
            arg0.put("remark", os[0]);
            arg0.put(var, os[1]);
            arg0.put(lastnode, os[2]);
            arg0.put(index, i++);
            node2.render(arg0, arg1);
        }
        map.clear();
        map = null;
        targetdata = null;
        return false;
    }

    private void createTree(Map<String, List> data, List targetdata, String parentname, String parentvalue,
                            String appendremark, String remark) {
        List list = data.get(parentvalue);
        if (list == null)
            return;
        appendremark += remark;
        for (Object o : list) {
            Object key = ToolEL.getBeanValue(o, parentname);
            boolean temp = false;
            if (data.get(key) == null)
                temp = true;
            targetdata.add(new Object[]{appendremark, o, temp});
            createTree(data, targetdata, parentname, key.toString(), appendremark, remark);
        }
    }
}

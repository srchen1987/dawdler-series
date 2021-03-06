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
package com.anywide.dawdler.util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
/**
 * 
 * @Title:  XmlObject.java
 * @Description:    基于dom4j来做xml操作的类 支持压缩   
 * @author: jackson.song    
 * @date:   2007年07月11日    
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public final class XmlObject {
	private final SAXReader reader = new SAXReader();
	{
//		Map map = new HashMap();
//		map.put("tx", "http://www.springframework.org/schema/tx");
//		reader.getDocumentFactory().setXPathNamespaceURIs(map);
		reader.setEncoding("utf-8");
	}
	private static TransformerFactory factory = TransformerFactory.newInstance();
	private Transformer transformer;
	private Element root;
	private Document document;
	private String filepath;
	private File file;
	private boolean xmlfile=true;
	public boolean isXmlfile() {
		return xmlfile;
	}
	public void setXmlfile(boolean xmlfile) {
		this.xmlfile = xmlfile;
	}
	public XmlObject(){
		initXML();
	}
	public XmlObject(String path) throws DocumentException, IOException{
		readPathToXML(path);
	}
	public XmlObject(Document document){
		this.document = document;
		getXMLRoot();
	}
	public XmlObject(File file) throws DocumentException, IOException{
		fileToXML(file);
	}
	public XmlObject(StringBuffer code) throws DocumentException{
		codeToXML(code.toString());
	}
	public XmlObject(InputStream in) throws DocumentException, IOException{
		inputStreamToXML(in);
	}
	public XmlObject(File file,boolean xmlfile) throws DocumentException, IOException{
		this.xmlfile=xmlfile;
		fileToXML(file);
	}
	public XmlObject(InputStream in,boolean xmlfile) throws DocumentException, IOException{
		this.xmlfile=xmlfile;
		inputStreamToXML(in);
	}
	public XmlObject(String path,boolean xmlfile) throws DocumentException, IOException{
		this.xmlfile=xmlfile;
		readPathToXML(path);
	}
	
	private void initXML(){
		this.document=DocumentHelper.createDocument();
	}
	private void readPathToXML(String path) throws DocumentException, IOException{
		this.filepath=path;
		fileToXML(new File(path));
	}
	private void fileToXML(File file) throws DocumentException, IOException{
		if(xmlfile){
			this.document=this.reader.read(file);
		}
		else{
			inputStreamToXML(new FileInputStream(file));
		}
		getXMLRoot();
		this.filepath=file.getPath();
	}
	private void codeToXML(String code) throws DocumentException{
		this.document = this.reader.read(new StringReader(code));
		getXMLRoot();
	}
	private void inputStreamToXML(InputStream in) throws DocumentException, IOException{
		this.document=this.reader.read(xmlfile?in:new GZIPInputStream(in));
		getXMLRoot();
	}
	public static XmlObject loadClassPathXML(String filename) throws DocumentException, IOException{
		return new XmlObject(DawdlerTool.getcurrentPath()+filename);
	}
	public void CreateRoot(String rootname){
		this.document.addElement(rootname);
		getXMLRoot();
	}
	public Element getRoot() throws NullPointerException{
		if(this.root==null)throw new NullPointerException("not found root!");
		return this.root;
	}
	public List selectNodes(String xpath){
		return root.selectNodes(xpath);
	}
	public List selectNodes(String xpath , String cxe){
		return root.selectNodes(xpath,cxe);
	}
	public Element selectSingleNode(String xpath){
		if(xpath.equals("/"))
			return root;
		else
			return (Element)root.selectSingleNode(xpath);
	}
	public void removeNode(String xpath) throws Exception{
		if(xpath.equals("/"))throw new Exception("Can't remove Root!");
		List list = selectNodes(xpath);
		if(list!=null)
		for(Object o: list){
			Element el = (Element) o;
			el.getParent().remove(el);
		}
		else
			throw new Exception("NotDeFount Element!");
	}
	public String asXML() throws IOException{
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding("UTF-8");
		StringWriter sw=new StringWriter();
		XMLWriter output = new XMLWriter(sw,format);
		output.write(document);
		return sw.getBuffer().toString();
		//return document.asXML();
	}
	public XmlObject transformer(String xslcode) throws TransformerException, IOException, DocumentException{
		return transformer(new StreamSource(new StringReader(xslcode)));
	}
	public XmlObject transformer(File file) throws TransformerException, IOException, DocumentException{
		return transformer(new StreamSource(file));
	}
	public XmlObject transformer(Source source) throws TransformerException, IOException, DocumentException{
		transformer = factory.newTransformer(source);
//		transformer = factory.newTransformer();
		return transformer();
	}
	
	public String transformerToString(String xslcode) throws TransformerException, IOException, DocumentException{
		transformer = factory.newTransformer( new StreamSource(new StringReader(xslcode)));
		Source source = new DocumentSource(document);
		StreamResult streamResult = new StreamResult();
		StringWriter sw = new StringWriter();
		streamResult.setWriter(sw);
		transformer.transform(source, streamResult);
		return sw.toString();
	}
	
	public String transformerToString(File xslFile) throws TransformerException, IOException, DocumentException{
		transformer = factory.newTransformer(new StreamSource(xslFile));
		Source source = new DocumentSource(document);
		StreamResult streamResult = new StreamResult();
		StringWriter sw = new StringWriter();
		streamResult.setWriter(sw);
		transformer.transform(source, streamResult);
		return sw.toString();
	}
	
	private XmlObject transformer() throws TransformerException, IOException, DocumentException{
		Source source = new DocumentSource(document);
		DocumentResult result = new DocumentResult();
		transformer.transform(source, result);
		return new XmlObject(result.getDocument());
	}
	private void getXMLRoot(){
		this.root = this.document.getRootElement();
	}
	public Document getDocument() {
		return document;
	}
	public void setDocument(Document document) {
		this.document = document;
	}
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public synchronized void saveXML() throws IOException, NullPointerException{      
		if(filepath==null||filepath.equals("")) throw new NullPointerException("not have created path to xml!");
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding("UTF-8");
		format.setTrimText(true);
		format.setPadText(false);
		XMLWriter output = new XMLWriter(xmlfile?new FileOutputStream(new File(this.filepath)):new GZIPOutputStream(new FileOutputStream(new File(this.filepath))),format);      
		output.write(document);      
		output.close();
	}
	public String getFilepath() {
		return filepath;
	}
	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}
}

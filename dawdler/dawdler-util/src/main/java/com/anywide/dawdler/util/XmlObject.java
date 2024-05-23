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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author jackson.song
 * @version V1.0
 * 基于dom4j来做xml操作的类 支持压缩(目前已废弃dom4j)
 */
public final class XmlObject {
	private static TransformerFactory factory = TransformerFactory.newInstance();
	private DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	{
		documentBuilderFactory.setIgnoringElementContentWhitespace(true);
		documentBuilderFactory.setIgnoringComments(true);
		documentBuilderFactory.setNamespaceAware(true);
	}
	private Document document;
	private XPathFactory xpathFactory = XPathFactory.newInstance();
	private XPath xpath = xpathFactory.newXPath();
	private Transformer transformer;
	private String filePath;
	private boolean compress = false;

	public XmlObject(String xmlPath, String xsdPath) throws Exception {
		setSchema(xsdPath);
		DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
		builder.setErrorHandler(XmlTool.getErrorHandler());
		document = builder.parse(xmlPath);
	}

	public XmlObject(String xmlPath, InputStream xsdInput) throws Exception {
		setSchema(xsdInput);
		DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
		builder.setErrorHandler(XmlTool.getErrorHandler());
		document = builder.parse(xmlPath);
	}

	public XmlObject(InputStream xmlInput, InputStream xsdInput)
			throws SAXException, ParserConfigurationException, IOException {
		setSchema(xsdInput);
		DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
		builder.setErrorHandler(XmlTool.getErrorHandler());
		document = builder.parse(xmlInput);
	}

	public XmlObject(String xmlPath, URL xsdURL) throws Exception {
		setSchema(xsdURL);
		DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
		builder.setErrorHandler(XmlTool.getErrorHandler());
		document = builder.parse(xmlPath);
	}

	public void setPrefix(String prefix) {
		String xmlns = document.getDocumentElement().getAttribute("xmlns");
		xpath.setNamespaceContext(new SimpleNamespaceResolver(prefix, xmlns));
	}

	public XmlObject() throws ParserConfigurationException {
		initXML();
	}

	public XmlObject(String path) throws Exception {
		readPathToXML(path);
	}

	public XmlObject(Document document) {
		this.document = document;
	}

	public XmlObject(File file) throws Exception {
		fileToXML(file);
	}

	public XmlObject(InputStream in) throws Exception {
		inputStreamToXML(in);
	}

	public XmlObject(InputStream in, URL xsdURL) throws Exception {
		setSchema(xsdURL);
		inputStreamToXML(in);
	}

	public XmlObject(File file, boolean compress) throws Exception {
		this.compress = compress;
		fileToXML(file);
	}

	public XmlObject(InputStream in, boolean compress) throws Exception {
		this.compress = compress;
		inputStreamToXML(in);
	}

	public XmlObject(String path, boolean compress) throws Exception {
		this.compress = compress;
		readPathToXML(path);
	}

	public XmlObject(StringBuffer code) throws ParserConfigurationException, SAXException, IOException {
		codeToXML(code.toString());
	}

	public static XmlObject loadClassPathXML(String filename) throws Exception {
		return new XmlObject(DawdlerTool.getCurrentPath() + filename);
	}

	private void setSchema(String xsdPath) throws MalformedURLException, SAXException {
		if (xsdPath != null) {
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = sf.newSchema(new File(xsdPath));
			documentBuilderFactory.setSchema(schema);
		}
	}

	private void setSchema(InputStream xsdInput) throws SAXException, IOException {
		if (xsdInput != null) {
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = sf.newSchema(new StreamSource(xsdInput));
			documentBuilderFactory.setSchema(schema);
			xsdInput.close();
		}
	}

	private void setSchema(URL xsdURL) throws SAXException {
		if (xsdURL != null) {
			documentBuilderFactory.setNamespaceAware(true);
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = sf.newSchema(xsdURL);
			documentBuilderFactory.setSchema(schema);
		}
	}

	public boolean isCompress() {
		return compress;
	}

	public void setXmlfile(boolean compress) {
		this.compress = compress;
	}

	public void initXML() throws ParserConfigurationException {
		this.document = documentBuilderFactory.newDocumentBuilder().newDocument();
	}

	private void readPathToXML(String filePath) throws Exception {
		this.filePath = filePath;
		fileToXML(new File(filePath));
	}

	private void codeToXML(String code) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
		this.document = builder.parse(new InputSource(new StringReader(code)));
	}

	private void inputStreamToXML(InputStream input) throws Exception {
		try {
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			builder.setErrorHandler(XmlTool.getErrorHandler());
			if (compress) {
				input = new GZIPInputStream(input);
			}
			this.document = builder.parse(input);
		} finally {
			if (input != null) {
				input.close();
			}
		}
	}

	private void fileToXML(File file) throws Exception {
		DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
		if (compress) {
			InputStream input = new FileInputStream(file);
			try {
				this.document = builder.parse(new GZIPInputStream(input));
			} finally {
				if (input != null) {
					input.close();
				}
			}
		} else {
			this.document = builder.parse(file);
		}
		this.filePath = file.getAbsolutePath();
	}

	public void createRoot(String root) {
		document.appendChild(document.createElement(root));
	}

	public Element getRoot() {
		return this.document.getDocumentElement();
	}

	public List<Node> selectNodes(String xpath) throws XPathExpressionException {
		List<Node> nodeList = new ArrayList<>();
		XPathExpression expr = this.xpath.compile(xpath);
		Object result = expr.evaluate(document, XPathConstants.NODESET);
		if (result instanceof NodeList) {
			NodeList nodes = (NodeList) result;
			for (int i = 0; i < nodes.getLength(); i++) {
				nodeList.add(nodes.item(i));
			}
		}
		return nodeList;
	}

	public Node selectSingleNode(String xpath) throws XPathExpressionException {
		if (xpath.equals("/")) {
			return this.document.getDocumentElement();
		} else {
			XPathExpression expr = this.xpath.compile(xpath);
			Object result = expr.evaluate(document, XPathConstants.NODESET);
			if (result instanceof NodeList) {
				NodeList nodes = (NodeList) result;
				if (nodes.getLength() > 0) {
					return nodes.item(0);
				}
			}
		}
		return null;
	}

	public void removeNode(String xpath) throws Exception {
		if (xpath.equals("/")) {
			throw new Exception("can't remove root element!");
		}
		List<Node> list = selectNodes(xpath);
		if (!list.isEmpty()) {
			for (Object o : list) {
				Element el = (Element) o;
				el.getParentNode().removeChild(el);
			}
		} else {
			throw new Exception("not defind element!");
		}
	}

	public String asXML() throws IOException, TransformerException {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		BufferedOutputStream out = new BufferedOutputStream(byteOut);
		TransformerFactory transFactory = TransformerFactory.newInstance();
		Transformer transformer = transFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
		DOMSource source = new DOMSource();
		source.setNode(document);
		StreamResult result = new StreamResult();
		result.setOutputStream(out);
		transformer.transform(source, result);
		out.flush();
		out.close();
		return byteOut.toString();
	}

	public XmlObject transformer(String xslCode) throws TransformerException, IOException {
		return transformer(new StreamSource(new StringReader(xslCode)));
	}

	public XmlObject transformer(File file) throws TransformerException, IOException {
		return transformer(new StreamSource(file));
	}

	public XmlObject transformer(Source source) throws TransformerException, IOException {
		transformer = factory.newTransformer(source);
		return transformer();
	}

	public String transformerToString(String xslCode) throws TransformerException, IOException {
		transformer = factory.newTransformer(new StreamSource(new StringReader(xslCode)));
		Source source = new DOMSource(document);
		StreamResult streamResult = new StreamResult();
		StringWriter sw = new StringWriter();
		streamResult.setWriter(sw);
		transformer.transform(source, streamResult);
		return sw.toString();
	}

	public String transformerToString(File xslFile) throws TransformerException, IOException {
		transformer = factory.newTransformer(new StreamSource(xslFile));
		Source source = new DOMSource(document);
		StreamResult streamResult = new StreamResult();
		StringWriter sw = new StringWriter();
		streamResult.setWriter(sw);
		transformer.transform(source, streamResult);
		return sw.toString();
	}

	private XmlObject transformer() throws TransformerException, IOException {
		Source source = new DOMSource(document);
		DOMResult result = new DOMResult();
		transformer.transform(source, result);
		return new XmlObject((Document) result.getNode());
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public void saveXML(String filePath) throws Exception {
		if (filePath == null || filePath.equals("")) {
			throw new NullPointerException("filePath can't be null !");
		}
		Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
		if (compress) {
			OutputStream out = new GZIPOutputStream(new FileOutputStream(filePath));
			transformer.transform(new DOMSource(document), new StreamResult(out));
			out.flush();
			out.close();
		} else {
			transformer.transform(new DOMSource(document), new StreamResult(new File(filePath)));
		}
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public static class SimpleNamespaceResolver implements NamespaceContext {
		private final String prefix;
		private final String nsURI;

		public SimpleNamespaceResolver(String prefix, String nsURI) {
			this.prefix = prefix;
			this.nsURI = nsURI;
		}

		@Override
		public String getNamespaceURI(String prefix) {
			if (prefix.equals(this.prefix)) {
				return this.nsURI;
			} else {
				return XMLConstants.NULL_NS_URI;
			}
		}

		@Override
		public String getPrefix(String namespaceURI) {
			if (namespaceURI.equals(this.nsURI)) {
				return this.prefix;
			} else {
				return null;
			}
		}

		@Override
		public Iterator<String> getPrefixes(String namespaceURI) {
			return null;
		}
	}

	public static List<Node> getNodes(NodeList nodeList) {
		int length = nodeList.getLength();
		List<Node> list = new ArrayList<>();
		if (nodeList != null && length > 0) {
			for (int i = 0; i < length; i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == 1) {
					list.add(node);
				}
			}
		}
		return list;
	}

	public static String getElementAttribute(NamedNodeMap namedNodeMap, String attribute, String defaultValue) {
		Node attr = namedNodeMap.getNamedItem(attribute);
		if (attr == null) {
			return defaultValue;
		}
		return attr.getNodeValue();
	}

	public static String getElementAttribute(NamedNodeMap namedNodeMap, String attribute) {
		Node attr = namedNodeMap.getNamedItem(attribute);
		if (attr == null) {
			return null;
		}
		return attr.getNodeValue();
	}

	public static int getElementAttribute2Int(NamedNodeMap namedNodeMap, String attribute, int defaultValue) {
		Node attr = namedNodeMap.getNamedItem(attribute);
		if (attr == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(attr.getNodeValue().trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static boolean getElementAttribute2Boolean(NamedNodeMap namedNodeMap, String attribute,
			boolean defaultValue) {
		Node attr = namedNodeMap.getNamedItem(attribute);
		if (attr == null) {
			return defaultValue;
		}
		try {
			return Boolean.parseBoolean(attr.getNodeValue().trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static long getElementAttribute2Long(NamedNodeMap namedNodeMap, String attribute, long defaultValue) {
		Node attr = namedNodeMap.getNamedItem(attribute);
		if (attr == null) {
			return defaultValue;
		}
		try {
			return Long.parseLong(attr.getNodeValue().trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

}

package com.deathclaws.jhipster;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Main {

	private final static StringBuilder entityBuilder = new StringBuilder();
	
	public static void main(String argv[]) {
		File folder = new File("./src");
		File[] files = folder.listFiles();
		try {
			for (File file : files) {
				lol(file);
			}
			try (Writer writer = new BufferedWriter(new OutputStreamWriter(
		              new FileOutputStream("filename.txt"), "utf-8"))) {
		   writer.write(entityBuilder.toString());
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String swap(String rawType) throws Exception {
		switch (rawType) {
		case "char":
			return "String";
		case "java.lang.Long":
			return "Long";
		case "java.lang.Integer":
			return "Integer";
		case "boolean":
			return "Boolean";
		case "date":
			return "Date";
		case "short":
			return "Short";
		case "int":
			return "Integer";
		case "long":
			return "Long";
		case "string":
			return "String";
		case "big_decimal":
			return "BigDecimal";
		default:
			throw new Exception(rawType + " is not defined");
		}
	}

	public static void lol(File fileXML) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		if(!fileXML.getName().endsWith("xml")) return;
		
		System.out.println(fileXML.getName());
		
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document xml = builder.parse(fileXML);
		Element root = xml.getDocumentElement();
		XPathFactory xpf = XPathFactory.newInstance();
		XPath path = xpf.newXPath();

		final String classExp = "/hibernate-mapping/class";
		final Node classNode = (Node) path.evaluate(classExp, root, XPathConstants.NODE);
		final String className = ((Element) classNode).getAttribute("name");

		entityBuilder.append("entity " + className + " {\n");

		final String propertiesExp = "/hibernate-mapping/class/property";
		final NodeList propertiesNodes = (NodeList) path.evaluate(propertiesExp, root, XPathConstants.NODESET);
		for (int i = 0; i < propertiesNodes.getLength(); i++) {
			Node propertyNode = propertiesNodes.item(i);
			final String propertyName = ((Element) propertyNode).getAttribute("name");
			final String typeName = ((Element) propertyNode).getAttribute("type");

			final String test = "column";
			final Node firstChild = (Node) path.evaluate(test, propertyNode, XPathConstants.NODE);
			final String propertyNotNull = ((Element) firstChild).getAttribute("not-null");

			final String printrequired = "true".equals(propertyNotNull) ? "required" : "";

			entityBuilder.append("\t" + propertyName + " " + swap(typeName) + " " + printrequired + "\n");
		}

		entityBuilder.append("}\n");

		entityBuilder.append("relationship ManyToOne {\n");
		
		final String ManytoOneExp = "/hibernate-mapping/class/many-to-one";
		final NodeList ManytoOneNodes = (NodeList) path.evaluate(ManytoOneExp, root, XPathConstants.NODESET);
		for (int i = 0; i < ManytoOneNodes.getLength(); i++) {
			Node propertyNode = ManytoOneNodes.item(i);
			final String propertyName = ((Element) propertyNode).getAttribute("name");
			final String refClassName = ((Element) propertyNode).getAttribute("class");

			final String test = "column";
			final Node firstChild = (Node) path.evaluate(test, propertyNode, XPathConstants.NODE);
			final String propertyNotNull = ((Element) firstChild).getAttribute("not-null");

			final String printrequired = "true".equals(propertyNotNull) ? " required" : "";
			
			entityBuilder.append("\t" + className + "{" + propertyName + printrequired + "} to " + refClassName + "\n");
		}

		entityBuilder.append("}\n");
	}

}

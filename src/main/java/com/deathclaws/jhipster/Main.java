package com.deathclaws.jhipster;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Main {

	public static class Tuple {
		public String s1;
		public String s2;

		public Tuple(String a1, String a2) {
			if (a1.compareTo(a2) > 0) {
				s1 = a1;
				s2 = a2;
			} else {
				s1 = a2;
				s2 = a1;
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((s1 == null) ? 0 : s1.hashCode());
			result = prime * result + ((s2 == null) ? 0 : s2.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Tuple other = (Tuple) obj;
			if (s1 == null) {
				if (other.s1 != null)
					return false;
			} else if (!s1.equals(other.s1))
				return false;
			if (s2 == null) {
				if (other.s2 != null)
					return false;
			} else if (!s2.equals(other.s2))
				return false;
			return true;
		}

	}

	private final static StringBuilder entityBuilder = new StringBuilder();
	private final static HashSet<Tuple> manyToMany = new HashSet<Tuple>();

	public static void main(final String... args) {
		File folder = new File("/home/alecointe/generated-entities");
		File[] files = folder.listFiles();
		
		Arrays.sort(files, new Comparator<File>() {
			public int compare(File o1, File o2) {
				String f1 = o1.getName();
				String f2 = o2.getName();
				return f1.compareTo(f2);
			}
		});
		
		Writer writer = null;
		try {
			for (File file : files) {
				execute(file);
			}
			generateManyToMany();
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("jhipster-jdl.jh"), "utf-8"));
			writer.write(entityBuilder.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static String lowerFirst(String s) {
		return s.substring(0, 1).toLowerCase() + s.substring(1);
	}
	
	private static void generateManyToMany() {
		if (manyToMany.isEmpty())
			return;
		entityBuilder.append("relationship ManyToMany {\n");
		for (Tuple tuple : manyToMany) {
			entityBuilder
				.append("\t" + tuple.s1 + "{" + lowerFirst(tuple.s2) + "} to " + tuple.s2 + "{" + lowerFirst(tuple.s1) + "}\n");
		}
		entityBuilder.append("}\n");
	}

	public static String swap(String rawType) throws Exception {
		if ("char".equals(rawType))
			return "String";
		else if ("java.lang.Long".equals(rawType))
			return "Long";
		else if ("java.lang.Integer".equals(rawType))
			return "Integer";
		else if ("boolean".equals(rawType))
			return "Boolean";
		else if ("date".equals(rawType))
			return "Date";
		else if ("short".equals(rawType))
			return "Short";
		else if ("int".equals(rawType) || "Integer".equals(rawType))
			return "Integer";
		else if ("long".equals(rawType))
			return "Long";
		else if ("string".equals(rawType))
			return "String";
		else if ("big_decimal".equals(rawType) || "BigDecimal".equals(rawType))
			return "BigDecimal";
		else
			throw new Exception(rawType + " is not defined");
	}

	static DocumentBuilderFactory factory;
	static DocumentBuilder builder;
	static XPathFactory xpf;
	static XPath path;

	static {
		try {
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			xpf = XPathFactory.newInstance();
			path = xpf.newXPath();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static void execute(File fileXML) throws Exception {
		if (!fileXML.getName().endsWith("xml"))
			return;

		System.out.println(fileXML.getName());

		Document xml = builder.parse(fileXML);
		Element root = xml.getDocumentElement();

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

		final String ManytoOneExp = "/hibernate-mapping/class/many-to-one";
		final NodeList ManytoOneNodes = (NodeList) path.evaluate(ManytoOneExp, root, XPathConstants.NODESET);

		if (ManytoOneNodes.getLength() > 0) {
			entityBuilder.append("relationship ManyToOne {\n");
			for (int i = 0; i < ManytoOneNodes.getLength(); i++) {
				Node propertyNode = ManytoOneNodes.item(i);
				final String propertyName = ((Element) propertyNode).getAttribute("name");
				final String refClassName = ((Element) propertyNode).getAttribute("class");

				//final String test = "column";
				//final Node firstChild = (Node) path.evaluate(test, propertyNode, XPathConstants.NODE);
				//final String propertyNotNull = ((Element) firstChild).getAttribute("not-null");
				//final String printrequired = "true".equals(propertyNotNull) ? " required" : "";
				final String printrequired = "";

				entityBuilder
						.append("\t" + className + "{" + lowerFirst(propertyName) + printrequired + "} to " + refClassName + "\n");
			}
			entityBuilder.append("}\n");
		}

		final StringBuilder oneToManyBuilder = new StringBuilder();
		
		final String setExp = "/hibernate-mapping/class/set";
		final NodeList SetNodes = (NodeList) path.evaluate(setExp, root, XPathConstants.NODESET);
		for (int i = 0; i < SetNodes.getLength(); i++) {
			Node setNode = SetNodes.item(i);
			final String nodeName = ((Element) setNode).getAttribute("name");
			
//			final String keyColumnExp = "key/column";
//			final Node columnChild = (Node) path.evaluate(keyColumnExp, setNode, XPathConstants.NODE);

			final String manyToManyExp = "many-to-many";
			final Node manyToManyChild = (Node) path.evaluate(manyToManyExp, setNode, XPathConstants.NODE);

			final String oneToManyExp = "one-to-many";
			final Node oneToManyChild = (Node) path.evaluate(oneToManyExp, setNode, XPathConstants.NODE);
			
//			final String columnManyToManyExp = "many-to-many/column";
//			final Node columnManyToManyChild = (Node) path.evaluate(columnManyToManyExp, setNode, XPathConstants.NODE);			

			if (manyToManyChild != null) {
				final String entityName = ((Element) manyToManyChild).getAttribute("entity-name");
				manyToMany.add(new Tuple(entityName, className));
			}
			
			if (oneToManyChild != null) {
				final String entityName = ((Element) oneToManyChild).getAttribute("class");
				oneToManyBuilder.append("\t" + className + "{" + lowerFirst(entityName) + "} to " + entityName + "\n");
			}
		}

		if(oneToManyBuilder.length() > 0) {
			entityBuilder.append("relationship OneToMany {\n");
			entityBuilder.append(oneToManyBuilder);
			entityBuilder.append("}\n");
		}
		
	}

}

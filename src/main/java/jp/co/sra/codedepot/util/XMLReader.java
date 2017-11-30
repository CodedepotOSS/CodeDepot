/**
* Copyright (c) 2009 SRA (Software Research Associates, Inc.)
*
* This file is part of CodeDepot.
* CodeDepot is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 3.0
* as published by the Free Software Foundation and appearing in
* the file GPL.txt included in the packaging of this file.
*
* CodeDepot is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with CodeDepot. If not, see <http://www.gnu.org/licenses/>.
*
**/
package jp.co.sra.codedepot.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath; //misnorm, should be called as XPathCompiler or XPathProcessor
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression; //same as org.w3c.dom.xpath.XPathExpression?
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLReader {

	public static List<String> getStringListByXPath(InputStream is, String xpathExpression)
	throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		List<String> results = new LinkedList<String>();

		//parse and build the dom of an xml file
		DocumentBuilderFactory domBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder domBuilder = domBuilderFactory.newDocumentBuilder();
		Document dom = domBuilder.parse(is);

		//xpath expression parse
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpathProcessor = xpathFactory.newXPath();
		XPathExpression xpathExpr = xpathProcessor.compile(xpathExpression);

		//get the nodes based on xpath expression
		NodeList nodes = (NodeList) xpathExpr.evaluate(dom, XPathConstants.NODESET);
		for (int i=0; i<nodes.getLength(); i++) {
			Node n = nodes.item(i);
			results.add(n.getNodeValue());
		}
		return results;
}
	public static List<String> getStringListByXPath(String fileName, String xpathExpression)
		throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		return getStringListByXPath(new FileInputStream(fileName), xpathExpression);
	}
	/**
	 * @param args
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws XPathExpressionException
	 */
	public static void main(String[] args) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		List<String> strs = getStringListByXPath("test/security/xssAttacks.xml", "//attack/code/text()");
		System.out.println(strs);
	}

}

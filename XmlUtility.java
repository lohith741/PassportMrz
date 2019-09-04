package PassportMrz;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class XmlUtility {

	private Document document = null;

	public XmlUtility(String inputXMLFilePath) {
		try {
			String xmlFilePath = FilenameUtils.removeExtension(inputXMLFilePath) + ".xml";
			File xmlFile = new File(xmlFilePath);
			if (xmlFile.exists()) {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				this.document = dBuilder.parse(xmlFile);
				this.document.getDocumentElement().normalize();
			} else {
			}
		} catch (Exception e) {
		}
	}

	public NodeList getNodeListOfAllCharacters() {
		NodeList charParamsNodeList = this.document.getElementsByTagName("charParams");
		return charParamsNodeList;
	}

	public NodeList getNodeListOfAllLines() {
		NodeList lineNodeList = this.document.getElementsByTagName("line");
		return lineNodeList;
	}

	public CharCoordinate createCharCoordinateObject(Element element) {
		CharCoordinate c = new CharCoordinate();
		try {
			c.setLeft(Double.parseDouble(element.getAttribute("l")));
			c.setRight(Double.parseDouble(element.getAttribute("r")));
			c.setTop(Double.parseDouble(element.getAttribute("t")));
			c.setBottom(Double.parseDouble(element.getAttribute("b")));
			String charConfidence = element.getAttribute("charConfidence");
			if (charConfidence != null && !charConfidence.isEmpty()) {
				c.setCharConfidence((Double.parseDouble(charConfidence)));
			} else {
				c.setCharConfidence(0);
			}
			String suspicious = element.getAttribute("suspicious");
			if (suspicious != null && !suspicious.isEmpty()) {
				c.setSuspicious((Double.parseDouble(suspicious)));
			} else {
				c.setSuspicious(0);
			}
			if (element.getTextContent().length() == 1) {
				c.setString(element.getTextContent());
			} else {
				c.setString(element.getTextContent().replace("\n", ""));
			}
		} catch (Exception e) {
		}
		return c;
	}

	public List<CharCoordinate> getCharCoordinateList() {
		List<CharCoordinate> charList = new ArrayList<CharCoordinate>();
		if (this.document == null) {
		} else {
			NodeList charNodeList = getNodeListOfAllCharacters();
			for (int i = 0; i < charNodeList.getLength(); i++) {
				Node charNode = charNodeList.item(i);
				if (charNode.getNodeType() == Node.ELEMENT_NODE) {
					charList.add(createCharCoordinateObject((Element) charNode));
				}
			}
		}
		return charList;
	}

	public List<CharCoordinate> getLineCoordinateList() {
		List<CharCoordinate> lineList = new ArrayList<CharCoordinate>();
		if (this.document == null) {
		} else {
			NodeList lineNodeList = getNodeListOfAllLines();
			for (int i = 0; i < lineNodeList.getLength(); i++) {
				Node lineNode = lineNodeList.item(i);
				if (lineNode.getNodeType() == Node.ELEMENT_NODE) {
					lineList.add(createCharCoordinateObject((Element) lineNode));
				}
			}
		}
		return lineList;
	}

	public String getOnlyText() {
		String string = "";
		if (this.document == null) {
		} else {
			NodeList charNodeList = getNodeListOfAllCharacters();
			for (int i = 0; i < charNodeList.getLength(); i++) {
				Node charNode = charNodeList.item(i);
				if (charNode.getNodeType() == Node.ELEMENT_NODE) {
					Element charElement = (Element) charNode;
					string = string.concat(charElement.getTextContent());
				}
			}
		}
		return string;
	}

	public List<String> getAllLineTextList() {
		List<String> text = new ArrayList<String>();
		if (this.document == null) {
		} else {
			NodeList lineNodeList = getNodeListOfAllLines();
			for (int i = 0; i < lineNodeList.getLength(); i++) {
				Node lineNode = lineNodeList.item(i);
				if (lineNode.getNodeType() == Node.ELEMENT_NODE) {
					Element lineElement = (Element) lineNode;
					NodeList charNodeList = lineElement.getElementsByTagName("charParams");
					String string = "";
					for (int j = 0; j < charNodeList.getLength(); j++) {
						Node charNode = charNodeList.item(j);
						if (charNode.getNodeType() == Node.ELEMENT_NODE) {
							Element charElement = (Element) charNode;
							string = string + charElement.getTextContent();
						}
					}
					text.add(string);
				}
			}
		}
		return text;
	}

	public LinkedHashMap<CharCoordinate, List<CharCoordinate>> getAllLineMapWithCharList() {
		LinkedHashMap<CharCoordinate, List<CharCoordinate>> hm = new LinkedHashMap<CharCoordinate, List<CharCoordinate>>();
		if (this.document == null) {
		} else {
			NodeList lineNodeList = getNodeListOfAllLines();
			for (int i = 0; i < lineNodeList.getLength(); i++) {
				Node lineNode = lineNodeList.item(i);
				if (lineNode.getNodeType() == Node.ELEMENT_NODE) {
					List<CharCoordinate> charList = new ArrayList<>();
					Element lineElement = (Element) lineNode;
					NodeList charNodeList = lineElement.getElementsByTagName("charParams");
					for (int j = 0; j < charNodeList.getLength(); j++) {
						Node charNode = charNodeList.item(j);
						if (charNode.getNodeType() == Node.ELEMENT_NODE) {
							charList.add(createCharCoordinateObject((Element) charNode));
						}
					}
					hm.put(createCharCoordinateObject(lineElement), charList);
				}
			}
		}
		return hm;
	}
}

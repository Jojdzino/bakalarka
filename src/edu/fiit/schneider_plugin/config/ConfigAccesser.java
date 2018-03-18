package edu.fiit.schneider_plugin.config;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigAccesser {
    private static Project project;
    static {
        project = ProjectManager.getInstance().getOpenProjects()[0];

    }
    //Sets value of given element in XML configuration
    public static void setElement(int value, String element) throws IOException, JDOMException {

        SAXBuilder builder = new SAXBuilder();
        String projectPath=project.getBaseDir().getPath();
        File xmlFile = new File(projectPath + "/.idea/commentFixerConfig.xml");
        Document doc = builder.build(xmlFile);
        Element root = doc.getRootElement();

        Element child = root.getChild(element);
        if(child==null) {
            child = new Element(element);
            child.setText(String.valueOf(value));
            root.addContent(child);
        }
        else child.setText(String.valueOf(value));

        XMLOutputter outter = new XMLOutputter();
        outter.setFormat(Format.getPrettyFormat());
        outter.output(doc,new FileWriter(new File(projectPath + "/.idea/commentFixerConfig.xml")));
    }

    //Returns value of selected element or -1 if element is missing
    public static int getElement(String elementName){
        SAXBuilder builder = new SAXBuilder();
        String projectPath=project.getBaseDir().getPath();
        File xmlFile = new File(projectPath + "/.idea/commentFixerConfig.xml");
        Document doc = null;
        try {
            doc = builder.build(xmlFile);
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
        assert doc != null;
        Element root = doc.getRootElement();

        for(Element child : root.getChildren()){
            if(child.getName().compareTo(elementName)==0)
                return Integer.parseInt(child.getValue());
        }
        return -1;
    }
}

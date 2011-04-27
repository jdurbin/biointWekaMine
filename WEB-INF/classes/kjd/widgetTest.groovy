package kjd

import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.data.*;
import com.vaadin.ui.Form;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.HorizontalLayout;

import com.vaadin.ui.Table;
import com.vaadin.ui.Table.FooterClickEvent;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;

import durbin.hg.*

//Button.ClickListener,
@SuppressWarnings("serial")
public class widgetTest extends com.vaadin.Application 
{
	
	def geneInfoPanel
	def window

	/****************************
	* Called by vaadin Application entry point to set up GUI. 
	*/ 
	public void init() {		
		
		try{
		
		def confName = ".hg.conf"
		def dbName = "hg19"
		def hg = new hgDB(confName,3308,dbName)
		
		setTheme("durbin")	
			
		window = new Window("WIDGET TEST", new Panel());
		setMainWindow(window);
		
				
		def label = new Label('<h3>SU2C Widget Test</h3>', Label.CONTENT_RAW);		
		window.addComponent(label);
		
		def infoLayout = new VerticalLayout()
		geneInfoPanel = new Panel(infoLayout);
		geneInfoPanel.setStyleName(Panel.STYLE_LIGHT);
		infoLayout.setMargin(true,false,false,false)
		geneInfoPanel.addStyleName("feature-info");
		geneInfoPanel.setWidth("264px");

		def geneName = "ERBB2"
		def descArry= hg.getGeneDescriptionFromName(geneName)						
		def desc = descArry[0]		
						
		geneInfoPanel.setCaption("Information for: $geneName" as String);
		def l = new Label(desc)
		l.setValue("Ha!  I replaced your content the setValue way.")
		geneInfoPanel.addComponent(l)
		window.addComponent(geneInfoPanel)				
		
		def cit = geneInfoPanel.getComponentIterator();				
		geneInfoPanel.requestRepaint()				
		window.showNotification("SETUP COMPLETE");	
		
	}catch(Exception e){
		
		System.err.println e
		//if (pf != null) println "pretend disconnect"//pf.disconnect()
	}
	}
}

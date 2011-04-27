package kjd

import com.vaadin.ui.*;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;

import com.vaadin.ui.HorizontalLayout;

import com.vaadin.ui.Table;

import durbin.util.*
import durbin.weka.*
import durbin.bioInt.*
import durbin.hg.*
import durbin.charts.*

import org.vaadin.ui.JFreeChartWrapper;

import org.jfree.data.xy.*;
import org.jfree.ui.*;
import org.jfree.chart.title.*;
import org.jfree.chart.axis.*;
import org.jfree.data.statistics.*;


/***
*  A panel to hold the wekaMine results and feature info. 
*  Controller for events involving these three components.
* 
*  OK, it's not actually a Panel, it's a HorizontalLayout...  
*/ 
class WekaMinePanel extends HorizontalLayout 
implements Property.ValueChangeListener{
	
	def bioint
	def hg
	
	def err = System.err // sugar
		
	WekaMineTable wekaMineTable = new WekaMineTable()
	def wekaMineFeaturesTable = new Label("") // placeholder
	def geneInfoPanel = new Label("") // placeholder
	def geneInfoLabel = ""
	
	def WekaMinePanel(){
		setMargin(true)
		setSpacing(true)		
		addComponent(wekaMineTable)
		addComponent(wekaMineFeaturesTable) // placeholder until something selected.
		addComponent(geneInfoPanel)		
	}
		
	def WekaMinePanel(bioint,hg){
		this.bioint = bioint
		this.hg = hg
		setMargin(true)
		setSpacing(true)		
		addComponent(wekaMineTable)
		addComponent(wekaMineFeaturesTable) // placeholder until something selected.
		addComponent(geneInfoPanel)		
	}
	
	public void valueChange(Property.ValueChangeEvent event) {
		
		if (event.getComponent() == wekaMineTable){	
			doWekaMineTableValueChange(event)
		}else{
			doWekaMineFeaturesValueChange(event)
		}	
	}		
	
	
	def doWekaMineFeaturesValueChange(event){
		def selectedGeneIdx = wekaMineFeaturesTable.getValue()
		def property = wekaMineFeaturesTable.getContainerProperty(selectedGeneIdx,"Gene")
		def geneName = property.getValue()
		err.println "Gene: $geneName selected"
		def descArry= hg.getGeneDescriptionFromName(geneName)						
		def desc = descArry[0]
		err.println desc
		
		if (desc == null) desc = "No information found in refSeqSummary."
		geneInfoPanel.setCaption("Information for : $geneName" as String);
		geneInfoPanel.removeAllComponents()
		geneInfoLabel = new Label(desc, Label.CONTENT_XHTML);
		geneInfoPanel.addComponent(geneInfoLabel)
		
		//def fmtdesc = "<div class=\"outer-deco\"><div class=\"deco\"><span class=\"deco\"></span> $desc </div></div>" as String
		//geneInfoLabel.setValue(desc)
		//geneInfoLabel.setStyleName("description")
				
		VerticalLayout res = new VerticalLayout();
    Label caption = new Label("<span>Additional Resources</span>",
              Label.CONTENT_XHTML);
    caption.setStyleName("section");
    caption.setWidth("100%");
    res.addComponent(caption);
    res.setMargin(false, false, true, false);
/*
		 Put in links to ucsc gene pages...

		 for (NamedExternalResource r : resources) {
          final Link l = new Link(r.getName(), r);
          l.setIcon(new ThemeResource("../runo/icons/16/note.png"));
          res.addComponent(l);
      }
*/		
		geneInfoPanel.addComponent(res)
				
		geneInfoPanel.requestRepaint()				
	}
	
	
	def doWekaMineTableValueChange(event){
		err.println "WekaMine Table item selected: "+event.getProperty()
		if (event.getProperty() == null) return;
		
		removeComponent(wekaMineTable)
		removeComponent(wekaMineFeaturesTable)
		removeComponent(geneInfoPanel)
		
		
		wekaMineFeaturesTable = wekaMineTable.getFeaturesTable()
		wekaMineFeaturesTable.addListener((Property.ValueChangeListener)this); 
	
		
		def infoLayout = new VerticalLayout()
		infoLayout.setMargin(true,false,false,false)
		geneInfoPanel = new Panel(infoLayout);
		geneInfoPanel.setStyleName(Panel.STYLE_LIGHT);
		geneInfoPanel.addStyleName("feature-info");
		geneInfoPanel.setWidth("264px"); // original 319				
		geneInfoPanel.setCaption("Gene Information");

		//def desc = "This gene encodes a member of the sestrin family. Sestrins are induced by the p53 tumor suppressor protein and play a role in the cellular response to DNA damage and oxidative stress. The encoded protein mediates p53 inhibition of cell growth by activating AMP-activated protein kinase, which results in the inhibition of the mammalian target of rapamycin protein. The encoded protein also plays a critical role in antioxidant defense by regenerating overoxidized peroxiredoxins, and the expression of this gene is a potential marker for exposure to radiation. Alternatively spliced transcript variants encoding multiple isoforms have been observed for this gene. [provided by RefSeq]."				
		def desc = ""				 
		geneInfoLabel = new Label(desc, Label.CONTENT_XHTML);

		//def l = new Label(desc)
		geneInfoPanel.addComponent(geneInfoLabel)
		
		addComponent(wekaMineTable)	
		addComponent(wekaMineFeaturesTable)	
		addComponent(geneInfoPanel)				
		requestRepaint()		
	}	
	
	
	def refreshNewDataset(datasetName){
		err.println "remove wekaMineTable"
		if (wekaMineTable == null) err.println "\tOh no, wekaMineTable is null."
		removeComponent(wekaMineTable)
		removeComponent(wekaMineFeaturesTable)
		removeComponent(geneInfoPanel)
		err.println "new wekaMineTable"
		wekaMineTable = new WekaMineTable(bioint,datasetName)
		wekaMineTable.addListener((Property.ValueChangeListener)this)
		wekaMineFeaturesTable = new Label("")
		addComponent(wekaMineTable)
		addComponent(wekaMineFeaturesTable)				
		wekaMineTable.requestRepaint()		
	}	
			
}
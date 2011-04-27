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

import groovy.sql.Sql
import durbin.util.*
import durbin.weka.*
import durbin.bioInt.*
import durbin.bioInt.BioIntDB
import durbin.hg.*
import durbin.charts.*

import org.vaadin.ui.JFreeChartWrapper;

import org.jfree.data.xy.*;
import org.jfree.ui.*;
import org.jfree.chart.title.*;
import org.jfree.chart.axis.*;
import org.jfree.data.statistics.*;

//Button.ClickListener,
@SuppressWarnings("serial")
public class biointWekaMine extends com.vaadin.Application 
implements  Property.ValueChangeListener
{
			
	// UI components
	private Label label;
	private Window window;
	TabSheet ts;
	def clinicalDataPanel = new ClinicalDataPanel()
	def wekaMinePanel = new WekaMinePanel()
	def l3

	
	// Control
	def datasetName="chin2006Exp";
	
	// IO	
	def bioint
	def hg
	def err = System.err // sugar
		
	// Data		
	def datasetTable


	/****************************
	* Called by vaadin Application entry point to set up GUI. 
	*/ 
	public void init() {	
		
		setTheme("durbin")		
		
		try{
		window = new Window("RED TEAM", new Panel());
		setMainWindow(window);
		
		//label = new Label('<h3>SU2C Bio-Integrator</h3>', Label.CONTENT_RAW);		
		//window.addComponent(label);
		
		//------------------------------------------------------------
		// Connect to DB
		def confName = ".hg.conf.entropy"
		def dbName = "bioInt"
		bioint = new BioIntDB(confName,3306,dbName)
		
		// Tunnel over to hgwdev to get to hg19
		def pf = new SSHPortForwarder()
		def host = "hgwdev.cse.ucsc.edu"
		def rhost = "127.0.0.1"
		def lport = 3307
		def rport = 3306
		def user = "james"
		pf.connect(user,host,rhost,lport,rport)

						
		def hgConfName = ".hg.conf.cancer2"
		def hgDBName = "hg19"
		hg = new hgDB(hgConfName,3307,hgDBName)
			
		//--------------------------------------------------------
		// Get list of data tables and create Table listing them...
		
		datasetTable = new DatasetTable(bioint)
		window.addComponent(datasetTable)
		
		// Cast is necessary so that it will know which kind of listener 
		// we want to map it to...
		datasetTable.addListener((Property.ValueChangeListener)this); 
		
		Label brlabel =  new Label("<br>", Label.CONTENT_RAW);
		window.addComponent(brlabel);

		//----------------------------------------------------------
		// Create some tabs to hold different views of bioInt DB
		// 		
		clinicalDataPanel = new ClinicalDataPanel(bioint)
		wekaMinePanel = new WekaMinePanel(bioint,hg)
		
		l3 = new VerticalLayout();
		l3.setMargin(true)
		l3.addComponent(new Label("MORE XRBLK"))

		ts = new TabSheet();
		ts.setHeight("550px")
		ts.setWidth("950px")
		ts.addTab(clinicalDataPanel,"Clinical Data",null)
		ts.addTab(wekaMinePanel,"Pre-run classifiers",null)
		ts.addTab(l3,"Run custom classifier",null)
		//ts.addListener(this);
		window.addComponent(ts)
				
		window.showNotification("SETUP COMPLETE");	
		
	}catch(Exception e){
		
		System.err.println e
		//if (pf != null) println "pretend disconnect"//pf.disconnect()
	}
	}
	

	/*******************************
	* add a table of results.
	*/
	 public void valueChange(Property.ValueChangeEvent event) {
			err.println "valueChange!!"
			
			if (event.getComponent() == datasetTable){
				err.println "Dataset Table.  msg: "+event.getProperty()

				String msg = event.getProperty() as String
				getMainWindow().showNotification("Dataset selected:"+msg as String)
								
				datasetName = datasetTable.getSelectedDatasetName();
						
				// Replace the existing table with new table for named dataset.
				clinicalDataPanel.refreshWithNewTable(datasetName)
			
				// Replace the existing wekaMineResults table with a new one for
				// the selected dataset.  May want to implement a flag that just 
				// says to do this if and when the tab is selected. 
				wekaMinePanel.refreshNewDataset(datasetName)
																														
			}else{
				err.println "Some stranger sent us an event: "+event
			}
	 }
}
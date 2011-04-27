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

import com.vaadin.ui.Table;
import com.vaadin.ui.Table.FooterClickEvent;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;

import groovy.sql.Sql
import durbin.util.*
import durbin.weka.*
import durbin.bioInt.*
import durbin.bioInt.BioIntDB


//Button.ClickListener,
@SuppressWarnings("serial")
public class blueteam extends com.vaadin.Application 
implements  Property.ValueChangeListener, TabSheet.SelectedTabChangeListener, Table.FooterClickListener,
ItemClickListener
{
			
	// UI components
	private Label label;
	private Window window;
	private final ListSelect attributeList = new ListSelect("Select attributes to classify on:");
	private final ComboBox datasetBox = new ComboBox("Please select a dataset:");
	String NOTIFICATION = "BUTTON PRESSED";
	Table table;
	TabSheet ts;
	def l1
	def l2
	def l3
	
	// Control
	private boolean toggle = true;
	def datasetName="chin2006Exp";
	
	// IO	
	def bioint
	def err = System.err // sugar
		
	// Data		
	def datasetTable
	ClinicalDataTable clinicalTable = new ClinicalDataTable()

	/****************************
	* Called by vaadin Application entry point to set up GUI. 
	*/ 
	public void init() {		
		
		try{
		window = new Window("BLUE TEAM", new Panel());
		setMainWindow(window);
		
		label = new Label('<h2>SU2C Bio-Integrator</h2>', Label.CONTENT_RAW);		
		window.addComponent(label);
		
		//------------------------------------------------------------
		// Connect to DB
		def confName = ".hg.conf.entropy"
		def dbName = "bioInt"
		bioint = new BioIntDB(confName,3306,dbName)
		
		datasetTable = new DatasetTable(bioint)
		window.addComponent(datasetTable)
		
		// Cast is necessary so that it will know which kind of listener we want to 
		// map it to...
		datasetTable.addListener((Property.ValueChangeListener)this); 
		
		Label brlabel =  new Label("<br>", Label.CONTENT_RAW);
		window.addComponent(brlabel);

		//-----------------------------------------------------------
		// Add table of results...
		// 
		// Just a place holder for now...
		//table = new Table("Clinical data...");

		//----------------------------------------------------------
		// Create some tabs to hold different views of bioInt DB
		// 
		l1 = new VerticalLayout();
		l1.setMargin(true)
		l1.addComponent(clinicalTable)
	 	l2 = new VerticalLayout();
		l2.setMargin(true)
		l2.addComponent(new Label("XRBLK"))
		l3 = new VerticalLayout();
		l3.setMargin(true)
		l3.addComponent(new Label("MORE XRBLK"))

		ts = new TabSheet();
		ts.setHeight("550px")
		ts.setWidth("100%")
		ts.addTab(l1,"Clinical Data",null)
		ts.addTab(l2,"Pre-run classifiers",null)
		ts.addTab(l3,"Run custom classifier",null)
		ts.addListener(this);
		window.addComponent(ts)
				
		window.showNotification("SETUP COMPLETE");	
		
	}catch(Exception e){
		
		System.err.println e
		//if (pf != null) println "pretend disconnect"//pf.disconnect()
	}
	}
	
	
	/*******************************
	* Handle tab change events...
	*/
	public void selectedTabChange(SelectedTabChangeEvent event) {
			TabSheet tabsheet = event.getTabSheet();
			if (tabsheet != null){
				Tab tab = tabsheet.getTab(tabsheet.getSelectedTab());
				if (tab != null) {
					getWindow().showNotification("Selected tab: " + tab.getCaption());
	    	}else{
					err.println "selected tab from tab sheet is null"
				}
			}else{
				err.println "tabsheet from event is null."
			}
	 }
	
	
	 public void footerClick(FooterClickEvent event) {
			String column = (String) event.getPropertyId();
			String buttonName = event.getButtonName();
		
			err.println "Footer click: $column  with $buttonName"			
	 }
	
	
		public void itemClick(ItemClickEvent event) {				
	    	Object itemId = event.getItemId(); // row
	      Object propertyId = event.getPropertyId(); // Column name
				err.println "itemClick: $itemId \t $propertyId"
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
									
				// Create a new table with appropriate rows and columns...
				l1.removeComponent(clinicalTable)
				clinicalTable = new ClinicalDataTable(bioint,datasetName)
				l1.addComponent(clinicalTable)	
				
				clinicalTable.addListener((Table.FooterClickListener) this)		
				clinicalTable.addListener((ItemClickListener) this)		
						
				clinicalTable.requestRepaint()
			}else{
				err.println "Some stranger sent us an event: "+event
			}
	 }	
}
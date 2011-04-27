package kjd;

import com.vaadin.ui.*;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;

import durbin.util.*
import durbin.bioInt.*
import durbin.hg.*
import durbin.charts.*

import org.vaadin.ui.JFreeChartWrapper;

import org.jfree.data.xy.*;
import org.jfree.ui.*;
import org.jfree.chart.title.*;
import org.jfree.chart.axis.*;
import org.jfree.data.statistics.*;


/******************************
*  A controler for clinical data table + histogram 
*  Handles events for clinical data table clicks, etc. 
*/ 
class ClinicalDataPanel extends VerticalLayout implements ItemClickListener{
	
	def featureHistogramChart = new Label("") // placeholder
	def clinicalTable = new ClinicalDataTable()
	def bioint;
	
	def err = System.err // sugar
	
	/**
	*
	*/ 
	def ClinicalDataPanel(){		
		setMargin(true)
		addComponent(clinicalTable)
		Label brlabel2 =  new Label("<br>", Label.CONTENT_RAW);
		addComponent(brlabel2);
		addComponent(featureHistogramChart)
	}
	
	/**
	*
	*/ 
	def ClinicalDataPanel(bioint){		
		setMargin(true)
		addComponent(clinicalTable)
		Label brlabel2 =  new Label("<br>", Label.CONTENT_RAW);
		addComponent(brlabel2);
		addComponent(featureHistogramChart)
		this.bioint = bioint
	}
					
	/***
	* When a column is clicked, update the histogram for that column. 
	*/ 	
	public void itemClick(ItemClickEvent event) {	
		Object itemId = event.getItemId(); // row
	  Object propertyId = event.getPropertyId(); // Column name
		err.println "itemClick: $itemId \t $propertyId"

		def values = clinicalTable.getValuesForColumn(propertyId)
		if (values != null){
			//err.println "values: $values"
			def maxvalue = values.max()
			//err.println "maxvalue: $maxvalue"			
			def chart = createHistogramFromValues(values,maxvalue,
				"    $propertyId distribution" as String)
			removeComponent(featureHistogramChart)
			featureHistogramChart = new JFreeChartWrapper(chart)
			featureHistogramChart.setWidth("250px");
			featureHistogramChart.setHeight("150px");
			addComponent(featureHistogramChart)				
			featureHistogramChart.requestRepaint()
		}	
	}
				
	/****
	* Replace the existing table with a new table for the given dataset
	*/ 
	def refreshWithNewTable(datasetName){
		removeComponent(clinicalTable)
		removeComponent(featureHistogramChart)  // Chart not connected to this table...
		clinicalTable = new ClinicalDataTable(bioint,datasetName)
		addComponent(clinicalTable)					
		clinicalTable.addListener((ItemClickListener) this)								
		clinicalTable.requestRepaint()
	}					
	
	/***
	* Create a histogram from the given values..
	*/ 						
	def createHistogramFromValues(values,binmax,cName){
		err.println "createHistogramFromValues"
		def series = new HistogramDataset()
		def valarray = new double[values.size()]
		values.eachWithIndex{v,i->valarray[i] = v as double}

		series.addSeries("Series1",valarray,20,0,binmax as int)
		def chartTitle = "$cName"
		def chartpanel = BasicCharts.histogram(chartTitle,"","",series,50,50) 

		def titleFont = new java.awt.Font("SansSerif", java.awt.Font.BOLD,9)
		def title = new org.jfree.chart.title.TextTitle(chartTitle,titleFont)
		def chart = chartpanel.getChart()
		chart.setTitle(title);

		return(chart)
	}
							
}
package kjd;

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

import groovy.sql.Sql
import durbin.util.*
import durbin.weka.*
import durbin.bioInt.*
import durbin.bioInt.BioIntDB

//Button.ClickListener,
@SuppressWarnings("serial")
//implements  Property.ValueChangeListener
public class syntaxteam extends com.vaadin.Application 
{

	def window

	/****************************
	* Called by vaadin Application entry point to set up GUI. 
	*/ 
	public void init() {

		/* Create the table with a caption. */
		Table table = new Table("This is my Table");
		/* Define the names and data types of columns.
 		* The "default value" parameter is meaningless here. */
		table.addContainerProperty("First Name", String.class,  null);
		table.addContainerProperty("Last Name",  String.class,  null);
		table.addContainerProperty("Year",       Integer.class, null);
		/* Add a few items in the table. */
		table.addItem(["Nicolaus","Copernicus",new Integer(1473)] as Object[], new Integer(1));
		table.addItem(["Tycho",   "Brahe", new Integer(1546)] as Object[], new Integer(2));
		table.addItem(["Giordano","Bruno",     new Integer(1548)] as Object[], new Integer(3));
		table.addItem(["Galileo", "Galilei",   new Integer(1564)] as Object[], new Integer(4));
		table.addItem(["Johannes","Kepler",    new Integer(1571)] as Object[], new Integer(5));
		table.addItem(["Isaac",   "Newton",    new Integer(1643)] as Object[], new Integer(6));

		window = new Window("BLUE TEAM", new Panel());
		setMainWindow(window);

		window.addComponent(table);
	}		
}
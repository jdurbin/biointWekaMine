#!/usr/bin/env groovy

import durbin.util.*
import durbin.weka.*
import durbin.bioInt.*
import durbin.bioInt.BioIntDB
import durbin.hg.*
import durbin.charts.*

err = System.err


	//------------------------------------------------------------
	// Connect to DB
	
	err.print "BioIntDB..."
	def confName = ".hg.conf.entropy"
	def dbName = "bioInt"
	bioint = new BioIntDB(confName,3306,dbName)
	err.println "done."
	
	
	err.print "SSHPortForward..."
	// Tunnel over to hgwdev to get to hg19
	def pf = new SSHPortForwarder()
	def host = "hgwdev.cse.ucsc.edu"
	def rhost = "127.0.0.1"
	def lport = 3307
	def rport = 3306
	def user = "james"
	pf.connect(user,host,rhost,lport,rport)
	err.println "done."

	err.print "hgDB..."
	def hgConfName = ".hg.conf.cancer2"
	def hgDBName = "hg19"
	hg = new hgDB(hgConfName,3307,hgDBName)
  err.println "done."
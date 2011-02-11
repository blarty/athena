/*
 * athena.jini.org : org.jini.projects.athena.harness
 * 
 * 
 * ReaderHarness.java
 * Created on 29-Oct-2004
 * 
 * ReaderHarness
 *
 */
package org.jini.projects.athena.harness;

import org.jini.projects.athena.resultset.AthenaResultSet;

/**
 * @author calum
 */
public interface ObjectHarness extends CommandHarness{
	public void handleReturnObject(Object o);
}
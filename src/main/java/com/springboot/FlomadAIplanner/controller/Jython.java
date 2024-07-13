package com.springboot.FlomadAIplanner.controller;

public class Jython {
    /**
     * @param args
     * Needed for 
     */
    public static void main(String[] args) {
        PythonInterpreter interpreter = new PythonInterpreter();
        // Execute the Web Scraper here
        interpreter.execfile("booking_web_scraper.py");
        // Call the Python Function and get the desired results
        org.python.core.PyObject result = interpreter.eval("scrape_booking(city_name)");
        // Convert the PyObject to a Java String
        String scrape = (String) result.__tojava__(String.class);
        // Print the Results
        System.out.println(scrape);
        interpreter.close();            
}

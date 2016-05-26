/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.graphhopper.jsprit.examples;


import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.jsprit.analysis.toolbox.AlgorithmSearchProgressChartListener;
import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer;
import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer.Label;
import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import com.graphhopper.jsprit.core.algorithm.selector.SelectBest;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.io.VrpXMLReader;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.EuclideanDistanceCalculator;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.util.StopWatch;
import com.graphhopper.util.shapes.GHPoint;


import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.crypto.spec.PSource;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;


public class SolomonExampleWithSpecifiedVehicleEndLocations {
	
	
	
	private final static String USER_AGENT = "Mozilla/5.0";

    public static void main(String[] args) {
        /*
         * some preparation - create output folder
		 */
    	
        File dir = new File("output");
        // if the directory does not exist, create it
        if (!dir.exists()) {
            System.out.println("creating directory ./output");
            boolean result = dir.mkdir();
            if (result) System.out.println("./output created");
        }

		/*
         * Build the problem.
		 *
		 * But define a problem-builder first.
		 */
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();

		/*
         * A solomonReader reads solomon-instance files, and stores the required information in the builder.
		 */
        new VrpXMLReader(vrpBuilder).read("input/deliveries_solomon_specifiedVehicleEndLocations_c101.xml");

		/*
         * Finally, the problem can be built. By default, transportCosts are crowFlyDistances (as usually used for vrp-instances).
		 */

        VehicleRoutingTransportCostsMatrix costMatrix = createMatrix(vrpBuilder);
        
        vrpBuilder.setRoutingCost(costMatrix);
        
        VehicleRoutingProblem vrp = vrpBuilder.build();


        Plotter pblmPlotter = new Plotter(vrp);
        pblmPlotter.plot("output/solomon_C101_specifiedVehicleEndLocations.png", "C101");

		/*
         * Define the required vehicle-routing algorithms to solve the above problem.
		 *
		 * The algorithm can be defined and configured in an xml-file.
		 */
//		VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "input/algorithmConfig_fix.xml");
        vra.setMaxIterations(500);
//		vra.setPrematureBreak(100);
        vra.getAlgorithmListeners().addListener(new AlgorithmSearchProgressChartListener("output/sol_progress.png"));
        /*
         * Solve the problem.
		 *
		 *
		 */
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

		/*
         * Retrieve best solution.
		 */
        VehicleRoutingProblemSolution solution = new SelectBest().selectSolution(solutions);

		/*
		 * print solution
		 */
        SolutionPrinter.print(solution);

		/*
		 * Plot solution.
		 */
//		SolutionPlotter.plotSolutionAsPNG(vrp, solution, "output/solomon_C101_specifiedVehicleEndLocations_solution.png","C101");
        Plotter solPlotter = new Plotter(vrp, solution);
        solPlotter.plot("output/solomon_C101_specifiedVehicleEndLocations_solution.png", "C101");


        new GraphStreamViewer(vrp, solution).setRenderDelay(50).labelWith(Label.ID).display();


    }
    
    private static VehicleRoutingTransportCostsMatrix createMatrix(VehicleRoutingProblem.Builder vrpBuilder) {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        for (String from : vrpBuilder.getLocationMap().keySet()) {
            for (String to : vrpBuilder.getLocationMap().keySet()) {
                Coordinate fromCoord = vrpBuilder.getLocationMap().get(from);
                Coordinate toCoord = vrpBuilder.getLocationMap().get(to);
                
                
               double distance_euclidean = EuclideanDistanceCalculator.calculateDistance(fromCoord, toCoord);
               // double distance_street = getDistanceByStreet(fromCoord,toCoord);
                
                
                System.out.println("Consult of the distance :"+distance_euclidean);
                matrixBuilder.addTransportDistance(from, to, distance_euclidean);
                matrixBuilder.addTransportTime(from, to, (distance_euclidean / 2.));
            }
        }
        return matrixBuilder.build();
    }
    
   /* private static void getDistanceByStreet(Coordinate fromCoord,Coordinate toCoord){
    	 StopWatch sw = new StopWatch().start();
    	 
    	 
    	 
    	 //FlagEncoder algoVehicle = hopper.getEncodingManager().getEncoder("car");
    	 List<GHPoint> requestPoints=new ArrayList<GHPoint>();
    	 requestPoints.add(new GHPoint(fromCoord.getX(), fromCoord.getY()));
    	 requestPoints.add(new GHPoint(toCoord.getX(), toCoord.getY()));
    	 
   // Injector injector = Guice.createInjector(createModule());
    	 
    	 GHResponse ghRsp = new GHResponse();
    	 GHRequest request = new GHRequest(requestPoints)
    			 .setVehicle("car")
    			 .setWeighting("fastest")
    			 .setAlgorithm("");
    			 //.getHints().put("calcPoints", true)
    			 //.put("instructions", false)
    			// .put("wayPointMaxDistance", 1d);
    	 float took = sw.stop().getSeconds();
         ghRsp = hopper.route(request);
         
         int alternatives = ghRsp.getAll().size();
         if (ghRsp.hasErrors())
         {
             System.out.println("Errors:" + ghRsp.getErrors());
         } else
         {
             PathWrapper altRsp0 = ghRsp.getBest();
             System.out.println("Alternatives:" + alternatives
                     + ", distance0: " + altRsp0.getDistance()
                     + ", time0: " + Math.round(altRsp0.getTime() / 60000f) + "min"
                     + ", points0: " + altRsp0.getPoints().getSize()
                     + ", debugInfo: " + ghRsp.getDebugInfo());
         }
         System.out.println("Took: "+Math.round(took * 1000));
    }*/
    
    private static Double getDistanceByStreet(Coordinate fromCoord,Coordinate toCoord){
    	//String url = "http://routing.shippify.co/route?instructions=false&vehicle=car&calc_points=true&type=json&point="+fromCoord.getX()+"2C"+fromCoord.getY()+"&point="+toCoord.getX()+"2C"+toCoord.getY();
    	String url = "http://routing.shippify.co/route?instructions=false&vehicle=car&calc_points=true&type=json&point=-2.1627622%2C-79.909927&point=-2.1628622%2C-79.90784";
    	HttpClient httpclient = new HttpClient(new MultiThreadedHttpConnectionManager());
    	 GetMethod httpget = new GetMethod(url);
    	try{
	    	StopWatch sw = new StopWatch().start();
	    	
			
			/*URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	
			// optional default is GET
			con.setRequestMethod("GET");
	
			//add request header
			con.setRequestProperty("User-Agent", USER_AGENT);
	
			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);
	
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();*/
	    	
	    	   
	    	    httpclient.executeMethod(httpget);
	    	    BufferedReader in = new BufferedReader(
				        new InputStreamReader(httpget.getResponseBodyAsStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
		
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				float took = sw.stop().getSeconds();
				System.out.println("Took time: "+Math.round(took * 1000));
				System.out.println("response distance:"+response.toString());
	    	    
			return 20d;
    	}catch(Exception e){
    		e.printStackTrace();
    		return 10d;
    	}finally {
    		 httpget.releaseConnection();
		}
	}
    
    /*private Double getCostByVehicle( int id_city,Double distance,int type_vehicle){
    	
    }*/

}

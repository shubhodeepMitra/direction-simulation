import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;


public class Location_Simulator_App {

    /**
     * 
     * @param encoded String type
     * @return List of LatLng object
     * 
     *  Objective: To decode the Google's Polulinealgo
     *  Followed the encoding steps as provided by the below link
     *  Link: https://developers.google.com/maps/documentation/utilities/polylinealgorithm
     *  Also reference one python implementation due to some decoding issues
     *  Link: https://stackoverflow.com/questions/9217274/how-to-decode-the-google-directions-api-polylines-field-into-lat-long-points-in
     */
    private static List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
    
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
    
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
    
            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
    
        return poly;
    }

    /**
     *  TODO
     * @param encoded
     * @return List of LatLng object, which store the endpoints of each Step in json field
     */
    private static List<LatLng> getEndPoints(String response) {
        List<LatLng> endPoints = new ArrayList<>();

        return endPoints;
    }

    /**
     *  Thought to use it to find whether there is a curve or not.
     *  Idea: Use Heron's formula to calculate the area of the triangle formed by three consecutive points,
     *        If the area is negligible, then we can consider the three lines to be on a straight line
     */
    private static boolean isCollinear(double x1, double y1, double x2, double y2, double x3, double y3) {

        double area = 0.0;

        // heron's equation https://byjus.com/maths/heron-formula/
        area = 0.5 * (x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2));

        if ( area < 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 
     * @param lat1 Source point's latitude 
     * @param lng1 Source point's longitude
     * @param lat2 Destination point's latitude 
     * @param lng2 Destination point's longitude
     * @return distance between source and destination
     * 
     *  Objective: To find the distance between two given points
     */
    private static double dist_two_points(double lat1, double lng1, double lat2, double lng2) {
        double distance = 0.0, area = 0.0, c = 0.0;
        double rad_lat1 = Math.toRadians(lat1);
        double rad_lat2 = Math.toRadians(lat2);
        double dlng = Math.toRadians(lng2 - lng1);
        double dlat = Math.toRadians(lat2 - lat1);
        // approximate radius of earth in km
        int R = 6373;
        
        // Haversine formula to find distance between two points on a sphere 
        // (Although the assigment considered the max dist to be less than radius of Earth)
        area = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(rad_lat1) * Math.cos(rad_lat2) * Math.pow(Math.sin(dlng / 2),2);
        c = 2 * Math.atan2(Math.sqrt(area), Math.sqrt(1 - area));
        
        distance = R * c;
        // converting it into meters
        return distance*1000;
    }

    /**
     * 
     * @param response JSON response saved in string form
     * @return the list of LatLng object, which store the PolyLine Points
     * 
     * Objective: To extract PolyLine Points from "overview_polyline" JSON field
     */
    private static List<LatLng> getPolyLinePointsList(String response) {
        JSONObject resp = new JSONObject(response.toString());
                JSONArray routeObject = resp.getJSONArray("routes");
                // check for empty routes, if there is error in response
                if (routeObject.isEmpty()) {
                    System.out.println("ERR NOT_FOUND: least one of the locations specified in the request's origin," +
                                        "or destination, could not be geocoded");
                    System.exit(0);
                }
                JSONObject routes = routeObject.getJSONObject(0);
                JSONObject overviewPolylines = routes
                        .getJSONObject("overview_polyline");
                String encodedPolyline = overviewPolylines.getString("points");
                List<LatLng> poly = decodePoly(encodedPolyline);
                return poly;

    }

    /**
     * 
     * @param src object storing source latitude and longitude
     * @param dest object storing destination latitude and longitude
     * @return JSON response saved in string formats
     * 
     *  Objective: To save the GET response in String format
     */
    private static String getResponse(LatLng src, LatLng dest) {
        String respString = "";
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");
        urlString.append(Double.toString(src.lat));
        urlString.append(",");
        urlString.append(Double.toString(src.lng));
        urlString.append("&destination=");
        urlString.append(Double.toString(dest.lat));
        urlString.append(",");
        urlString.append(Double.toString(dest.lng));
        //add key

        try {
            URL url = new URL(urlString.toString());
            HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            int responsecode = conn.getResponseCode();

            if(responsecode != 200) {
                throw new RuntimeException("HttpsResponseCode: " +responsecode);
            } else {
                Scanner sc = new Scanner(url.openStream());
                while(sc.hasNext())
                {
                    respString += sc.nextLine();
                }
                sc.close();
            }
    
    
    
        }catch (Exception e){
           System.out.println(e);
        }

        return respString;
    }

    /**
     * 
     * @param D     the actual distance between point 1 and point 2
     * @param lat1  the latitude of point 1
     * @param lng1  the longitude of point 1
     * @param lat2  the latitude of point 2
     * @param lng2  the longitude of point 2
     * @return LatLng object
     * 
     *  Objective: Calculate the latitude and longitude of the point which is 50m from point 1,
     *             store it in a LatLng object and return it.
     */
    private static LatLng newFiftyMeterPoint(double D, double lat1, double lng1, double lat2, double lng2) {
        double lat = 0, lng = 0;
        final int d = 50;

        /*
            Using the equation in https://math.stackexchange.com/questions/2045174/how-to-find-a-point-between-two-points-with-given-distance
            to calcualte a point which is 50m from routePoint_index
         */
        lat = lat1 + (d/D)*(lat2 - lat1);
        lng = lng1 + (d/D)*(lng2 - lng1);

        // Seting the double precision to be #.####
        final DecimalFormat df = new DecimalFormat("#.#####");
        lat = Double.parseDouble(df.format(lat));
        lng = Double.parseDouble(df.format(lng));

        LatLng point = new LatLng(lat, lng);
        return point;
    }

    /**
     * 
     * @param respString JSON response saved in String format
     * @param src        Object containing source latitude and longitude
     * @param dest       Object containing dest latitude and longitude
     * @return return List of LatLng object
     * 
     *  Objective: Function to calculate the points along the route at a constant interval of 50m
     */
    private static List<LatLng> calc_route(String respString, LatLng src, LatLng dest){

        // make the url string
        // parse the GET response
        // call decodePoly
        //List<LatLng> polyList = decodePoly("{u|mAgyxxMdFiBbBg@bBwLLoBIm@]oA_@kAMM@uAJ]h@kAb@eAl@aB~ByFb@kAPe@\\o@`@_@bAcALKLJHJf@h@p@Er@DXB");
        List<LatLng> polyList = getPolyLinePointsList(respString);
        // TODO: get all the endpoints of the step
        // List<LatLng> endPointList = getEndPoints(respString);

        // new List for storing the points which are 50m apart
        List<LatLng> route_points = new ArrayList<>();
        
        // start with the sorce latlng, then update it to the 50th meter latlng
        int routePoint_index = 0;
        // First point will always be part of the solution
        route_points.add(polyList.get(routePoint_index));

        // TODO endPoint calc
        // int endPoint_index = 0;

        for ( int point_index = 1; point_index < polyList.size(); ) {

            //using these variables to clean and shorten the function calls length
            double lat1, lng1, lat2, lng2;
            lat1 = route_points.get(routePoint_index).lat;
            lng1 = route_points.get(routePoint_index).lng;
            lat2 = polyList.get(point_index).lat;
            lng2 = polyList.get(point_index).lng;
            double dist = dist_two_points(lat1, lng1, lat2, lng2);
            // Skip all the points in b/w which are less than 50m, Once we have a point which is more than 50m, we can calculate the point which is 50m from srcPoint_index
            if ( dist > 50) {

                route_points.add(newFiftyMeterPoint(dist, lat1, lng1, lat2, lng2));
                ++routePoint_index;
            } else {
                // go to the next point only when dist is less than 50m
                point_index++;
            }

        }
        // Add the Destination point
        route_points.add(polyList.get(polyList.size()-1));

        return route_points;
    }

    /**
     *  Objective: Main execution of the code.
     *             Take the input and outputs the points along the path
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");  
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        double lat,lng;

        System.out.println("Enter Source Latitude");
        lat = Double.parseDouble(br.readLine());
        System.out.println("Enter Source Longitude");
        lng = Double.parseDouble(br.readLine());
        LatLng src = new LatLng(lat, lng);

        System.out.println("Enter Destination Latitude");
        lat = Double.parseDouble(br.readLine());
        System.out.println("Enter Destination Longitude");
        lng = Double.parseDouble(br.readLine());
        LatLng dest = new LatLng(lat ,lng);

        String respString = getResponse(src, dest);

        List<LatLng> route_points = calc_route(respString, src, dest);

        // Output
        for (LatLng point : route_points)
            System.out.println(point.lat + "," + point.lng + ",blue,marker");


    }
}

import java.util.*;
import java.lang.Math;

public class Coord {
    public double[] coordinates = new double[2];
    public String[] tempCoords;
    public String[] directions = new String[2];
    public String label = "";
    public static ArrayList<ArrayList<String>> AcceptableFormats = new ArrayList<ArrayList<String>>();
    public String type = "";
    public ArrayList<String> formatAL = new ArrayList<String>();
    public ArrayList<ArrayList<String>> arrayList = new ArrayList<ArrayList<String>>();

    public Coord(String in) throws Exception{
        Position(in);
        coordinates = FlipArray(coordinates);
    }

    public String GeoJSON(){
        String out = "{ \"type\": \"Feature\", \"geometry\": {\"type\": \"Point\", \"coordinates\": [" + coordinates[0] + "," + coordinates[1] + "] }, \"properties\": { \"name\": \"" + label.strip() + "\" } }";
        return out;
    }

    void Position(String pos) throws Exception{
        pos = pos.replace(",","");
        pos = pos.replace("by","");

        String[] c = CleanArray(pos.split(" "));

        c = SplitNumsArray(c);
        String[] d = CleanFormat(Format(c));
        if(CompareFormat(d)){
            SetPos(c,d);
        } else {
            throw new Exception("Incorrect format");
        }
    }

    void PopulateDMS(String[] in, int[] indices) throws Exception{
        String[] nums = {in[indices[0]],in[indices[1]],in[indices[2]],in[indices[3]],in[indices[4]],in[indices[5]]};
        int xMod = isNegative(Double.parseDouble(in[indices[0]]));
        int yMod = isNegative(Double.parseDouble(in[indices[3]]));

        coordinates[0] = xMod*(xMod*(Double.parseDouble(in[indices[0]]))+Double.parseDouble(in[indices[1]])/60.0+Double.parseDouble(in[indices[2]])/3600.0);
        coordinates[1] = yMod*(yMod*(Double.parseDouble(in[indices[3]]))+Double.parseDouble(in[indices[4]])/60.0+Double.parseDouble(in[indices[5]])/3600.0);
        CheckNumbers(nums);
        type = "DMS";
    }

    int isNegative(Double d){
        if(d < 0){
            return -1;
        } else {
            return 1;
        }
    }

    double NegativeNum(Double d){
        if(d < 0){
            return d*-1;
        } else {
            return d;
        }
    }

    void PopulateDD(String[] in, int[] indices) throws Exception{
        String[] nums = {in[indices[0]],in[indices[1]]};
        coordinates[0] = Double.parseDouble(in[indices[0]]);
        coordinates[1] = Double.parseDouble(in[indices[1]]);
        CheckNumbers(nums);
        type = "DD";
    }

    void PopulateDMD(String[] in, int[] indices) throws Exception{
        String[] nums = {in[indices[0]],in[indices[1]],in[indices[2]],in[indices[3]]};
        int xMod = isNegative(Double.parseDouble(in[indices[0]]));
        int yMod = isNegative(Double.parseDouble(in[indices[2]]));
        coordinates[0] = xMod*(xMod*Double.parseDouble(in[indices[0]])+Double.parseDouble(in[indices[1]])/60.0);
        coordinates[1] = yMod*(yMod*Double.parseDouble(in[indices[2]])+Double.parseDouble(in[indices[3]])/60.0);
        CheckNumbers(nums);
        type = "DMD";
    }

    void CheckNumbers(String[] in) throws Exception{
        int x = in.length/2;
        for(int i = 0; i < in.length; i++){
            String s = in[i];
            Double d = Double.parseDouble(s);
            boolean isInteger = false;
            if(i==0 || i==x){
                LatVLong(d,(i/x));
            } else {
                Check60(d);
            }
            if(i%x != x-1){
                isInteger = true;
            }
            CheckLength(s, isInteger);
        }
    }

    void LatVLong(Double num, int i) throws Exception{
        if(ContainsLongitude(directions[i].toLowerCase())){
            CheckLong(num);
        } else {
            CheckLat(num);
        }
    }

    void CheckLength(String s, boolean IsInteger) throws Exception{
        String[] t = s.split("\\.");
        if(t.length > 1){
            if(t[1].length() > 6 || IsInteger){
                throw new Exception("Incorrect length: " + s);
            }
        }
    }

    void CheckLat(double d) throws Exception{
        if(Math.abs(d) > 90){
            throw new Exception("Incorrect lat size: " + d);
        }
    }

    void CheckLong(double d) throws Exception{
        if(Math.abs(d) > 180){
            throw new Exception("Incorrect long size: " + d);
        }
    }

    void Check60(double d) throws Exception{
        if(d > 60 || d < 0){
            throw new Exception("Incorrect DMS size: " + d);
        }
    }

    void SetPos(String[] in, String[] format) throws Exception{
        Directions(in, format);
        int x = formatAL.size();
        if(x < format.length){
            Label(x, in);
        }
    }

    void FlipCoordinates(){
        if(ContainsLongitude(directions[0].toLowerCase()) || ContainsLatitude(directions[1].toLowerCase())){
            coordinates = FlipArray(coordinates);
            directions = FlipArray(directions);
        }
    }

    String[] ExtractDoubles(String[] in, String[] format){
        ArrayList<String> out = new ArrayList<String>();
        for(int i = 0; i < format.length; i++){
            if(format[i].equals("Double")){
                out.add(in[i]);
            }
        }
        return List2Array(out);
    }

    void Directions(String[] in, String[] format) throws Exception{
        directions = FindDirections(in, format);

        int[] doubles = FindIndices(formatAL, "Double");
        if(doubles.length == 2){
            PopulateDD(in, doubles);
        } else if(doubles.length == 4){
            PopulateDMD(in, doubles);
        } else if(doubles.length==6){
            PopulateDMS(in, doubles);
        } else {
            throw new Exception("Incorrect number of doubles: " + Arrays.toString(format));
        }

        FlipCoordinates();

        if(ContainsSouth(directions[0].toLowerCase())){
            coordinates[0] = -coordinates[0];
            directions[0] = "N";
        }
        
        if(ContainsWest(directions[1].toLowerCase())){
            coordinates[1] = -coordinates[1];
            directions[1] = "E";
        }
    }

    String[] FlipArray(String[] in){
        String t = in[0];
        in[0] = in[1];
        in[1] = t;
        return in;
    }

    double[] FlipArray(double[] in){
        double t = in[0];
        in[0] = in[1];
        in[1] = t;
        return in;
    }

    String[] FindDirections(String[] in, String[] format) throws Exception{
        String[] out = {"",""};
        String[] def = {"N","E"};
        int[] indices = FindIndices(format, "Direction");

        if(indices.length == 0){
            return def;
        }

        else if(indices.length == 1){
            double d = CountOccurrences(format, "Double");
            int index;
            if(indices[0] < d){
                index = 0;
            } else {
                index = 1;
            }
            out[index] = in[indices[0]];
            if(ContainsLatitude(out[index].toLowerCase())){
                out[out.length-index-1] = "E";
            } else {
                out[out.length-index-1] = "N";
            }

        } else {
            int a = (CountOccurrences(format, "Double") + CountOccurrences(format, "DMS") + CountOccurrences(format, "South/Seconds"))/2;
            if(indices[0] > a+1){
                out[1] = in[indices[0]];
            } else {
                out[0] = in[indices[0]];
                if(indices.length > 1){
                    out[1] = in[indices[1]];
                }
            }
        }

        return out;
    }

    void Label(int x, String[] in){
        for(int i = x; i < in.length; i++){
            label += in[i] + " ";
        }
    }

    int[] FindIndices(String[] in, String word){
        ArrayList<Integer> temp = new ArrayList<Integer>();
        for(int i = 0; i < in.length; i++){
            if(in[i].equals(word)){
                temp.add(i);
            }
        }
        int[] out = new int[temp.size()];
        for(int i = 0; i < out.length; i++){
            out[i] = temp.get(i);
        }
        return out;
    }

    int CountOccurrences(String[] format, String word){
        int x = 0;
        for(String s : format){
            if(s.equals(word)){
                x++;
            }
        }
        return x;
    }

    int[] FindIndices(ArrayList<String> in, String word){
        return FindIndices(List2Array(in), word);
    }

    String[] CleanFormat(String[] in){
        return List2Array(CleanFormat(new ArrayList<String>(Arrays.asList(in))));
    }

    ArrayList<String> CleanFormat(ArrayList<String> in){
        boolean foundDMS = false;
        for(int i = 0; i < in.size(); i++){
            String s = in.get(i);
            if(s.equals("DMS")){
                foundDMS = true;
                type = "DMS";
            }
            if(s.equals("South/Seconds")){
                if(!type.equals("DMS")){
                    in.set(i,"Direction");
                } else if(i < in.size()-1){
                    String t = in.get(i+1);
                    if(t.equals("South/Seconds")){
                        in.set(i, "DMS");
                        in.set(i+1,"Direction");
                    }
                }
            }
        }
        return in;
    }

    String[] ClipLabel(String[] in){
        if(in[in.length-1].equals("Label")){
            String[] out = new String[in.length-1];
            for(int i = 0; i < out.length; i++){
                out[i] = in[i];
            }
            return out;
        } else {
            return in;
        }
    }

    boolean CompareFormat(String[] in){
        in = ClipLabel(in);
        for(int j = 0; j < AcceptableFormats.size(); j++){
            ArrayList<String> a = AcceptableFormats.get(j);
            if(a.size() == in.length){
                boolean flag = true;
                for(int i = 0; i < in.length; i++){
                    if(in[i].equals("South/Seconds")){
                        if(!(a.get(i).equals("DMS") || a.get(i).equals("Direction"))){
                            flag = false;
                        }
                    } else if(!a.get(i).equals(in[i])){
                        flag = false;
                    }
                }
                if(flag){
                    formatAL = a;
                    return true;
                }
            }
        }

        return false;
    }

    String[] Format(String[] in) throws Exception{
        ArrayList<String> temp = new ArrayList<String>();
        String[] out = in;
        for(int i = 0; i < out.length; i++){
            String s = out[i].toLowerCase();
            if(IsDouble(s)){
                s = "Double";
            } else if(ContainsDMS(s)){
                if(ContainsDirection(s)){
                    s = "South/Seconds";
                } else {
                    s = "DMS";
                }
            } else if(ContainsDirection(s)){
                s = "Direction";
            } else {
                s = "Label";
                temp.add(s);
                return List2Array(temp);
            }
            temp.add(s);
        }
        System.out.println(temp);
        return List2Array(temp);
    }

    String[] List2Array(ArrayList<String> in){
        String[] out = new String[in.size()];
        for(int i = 0; i < in.size(); i++){
            out[i] = in.get(i);
        }
        return out;
    }

    ArrayList<Double> String2Double(ArrayList<String> in){
        ArrayList<Double> out = new ArrayList<Double>();
        for(String s : in){
            out.add(Double.parseDouble(s));
        }
        return out;
    }

    /**
     * A method for splitting number-character combo strings into their own entries in an ArrayList
     * @param in an ArrayList (etc. [123A, 123B])
     * @return [123A, 123B] -> [123, A, 123, B]
     */
    ArrayList<String> SplitNums(ArrayList<String> in){
        ArrayList<String> out = new ArrayList<String>();
        for(int i = 0; i < in.size(); i++){
            String s = in.get(i);
            String t = "";
            for(int j = s.length()-1; j >= 0; j--){
                if(IsDouble(s.charAt(0) + "") || s.charAt(0) == '-'){
                    String u = s.charAt(j) + "";
                    if(IsDouble(u)){
                        break;
                    } else {
                        t = u + t;
                        s = s.substring(0,s.length()-1);
                    }
                }
            }
            out.add(s);
            if(!t.equals("")){
                out.add(t);
            }
        }
        return out;
    }

    String[] SplitNumsArray(String[] in){
        ArrayList<String> temp = SplitNums(ArrayToList(in));
        String[] out = new String[temp.size()];
        for(int i = 0; i < temp.size(); i++){
            out[i] = temp.get(i);
        }
        return out;
    }

    ArrayList<String> ArrayToList(String[] in){
        ArrayList<String> arr = new ArrayList<String>();
        for(int i = 0; i < in.length; i++){
            arr.add(in[i]);
        }
        return arr;
    }

    String[] CleanArray(String[] arr){
        String[] out = new String[arr.length];
        for(int i = 0; i < arr.length; i++){
            out[i] = arr[i].strip();
        }
        return out;
    }

    boolean IsDouble(String in){
        try{
            double x = Double.parseDouble(in);
            return true;
        } catch(Exception e){
            return false;
        }
    }

    boolean PositiveDirection(String dir) throws Exception{
        if(ContainsWest(dir) || ContainsSouth(dir)){
            return false;
        } else if(ContainsNorth(dir) || ContainsEast(dir)){
            return true;
        } else {
            throw new Exception("Not a direction: " + dir);
        }
    }

    boolean IsNumberNegative(double num){
        return num < 0;
    }

    boolean ContainsLatitude(String in){
        return ContainsNorth(in) || ContainsSouth(in);
    }

    boolean ContainsLongitude(String in){
        return ContainsEast(in) || ContainsWest(in);
    }

    boolean ContainsDirection(String in){
        return ContainsNorth(in) || ContainsSouth(in) || ContainsEast(in) || ContainsWest(in);
    }

    boolean ContainsSouth(String in){
        //in = in.toLowerCase();
        return in.equals("s") || in.toLowerCase().equals("south");
    }

    boolean ContainsWest(String in){
        //in = in.toLowerCase();
        return in.equals("w") || in.toLowerCase().equals("west");
    }

    boolean ContainsEast(String in){
        //in = in.toLowerCase();
        return in.equals("e") || in.toLowerCase().equals("east");
    }

    boolean ContainsNorth(String in){
        //in = in.toLowerCase();
        return in.equals("n") || in.toLowerCase().equals("north");
    }

    boolean ContainsDMS(String in){
        in.toLowerCase();
        return in.equals("d") || in.equals("m") || in.equals("s") || in.equals("degrees") || in.equals("minutes") || in.equals("seconds") || in.equals("°") || in.equals("\"") || in.equals("'");
    }

    public String toString(){
        return label + " | " + coordinates[1] + " " + directions[0] + ", " + coordinates[0] + " " + directions[1];
    }

}

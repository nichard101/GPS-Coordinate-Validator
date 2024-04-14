import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.net.*;
import javax.swing.*;
import java.awt.image.*;
import javax.imageio.*;


public class world{

    final static String url = "http://geojson.io/#data=data";
    final static String url2 = ":application/json,";
    static String geoJSON = "{\"type\": \"FeatureCollection\", \"features\": [";
    static String geoJSONend = "";
    static ArrayList<Coord> coordinateList = new ArrayList<Coord>();

    static ImageIcon icon;
    static JFrame myframe;
    static JLabel mylabel;
    static BufferedImage image = null;

    public static void main(String[] args){
        PopulateFormats(getFileContents("AcceptableFormats.txt"));

        if(args.length > 0){
            AutoEntry(getFileContents(args[0]));
        } else {
            ManualEntry();
        }

        DisplayWindow();

        GenerateURL();

        String geoJSONFull = geoJSON + geoJSONend + "] }";

        StringSelection selection = new StringSelection(geoJSONFull);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection,null); 

        System.out.println("GeoJSON file has been copied to clipboard: " + geoJSONFull);

        System.out.print("Open link in GeoJSON.io? Y/N ");
        Scanner scan = new Scanner(System.in);
        String input = scan.nextLine();
        input = input.toLowerCase();

        if(input.equals("y")){
            try{
                String encoded = URLEncoder.encode(url2+geoJSONFull, "UTF-8");
                String urlFinal = url+encoded;
                URI uri = new URL((urlFinal).replace("+","%20")).toURI();
                Desktop desktop = Desktop.getDesktop();
                desktop.browse(uri);        
            } catch(Exception e){
                System.err.println("Error: Link could not be opened");
            }
        }
    }

    static void DisplayWindow(){
        JFrame frame = new JFrame("Etude 7: Where in the World...?");
        ImagePane ip = new ImagePane(coordinateList);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1100,1100));
        frame.add(ip);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    static void AddCoordinate(String in){
        try{
            Coord c = new Coord(in);
            coordinateList.add(c);
        } catch(Exception e){
            System.err.println("\nUnable to process: " + in);
        }
    }

    static String GenerateURL(){
        for(Coord c : coordinateList){
            if(geoJSONend != ""){
                geoJSONend += ",";
            }
            geoJSONend += c.GeoJSON();
        }
        return "";
    }

    static void AutoEntry(String in){
        Scanner sc = new Scanner(in);
        while(sc.hasNext()){
            AddCoordinate(sc.nextLine());
        }
        //sc.close();
    }

    static void ManualEntry(){
        Scanner sc = new Scanner(System.in);
        while(true){
            System.out.println("Enter a coordinate, or Q to end: ");
            String s = sc.nextLine();
            String sL = s.toLowerCase();
            if(sL.equals("q")){
                break;
            }
            AddCoordinate(s);
        }
        //sc.close();
    }

    /**
     * A method to read in the contents of a file
     * @param fileName the name of the file
     * @return a String with the contents of the file
     */
    static String getFileContents(String fileName){
        String fileContents = "";
        try {
            fileContents = Files.readString(Path.of(fileName));
        } catch (IOException e) {
            System.out.println("File not found.");
        }
        return fileContents;
    }

    /**
     * Reads in each listed format from the input file
     * @param in the name of the file containing the list of formats
     */
    static void PopulateFormats(String in){
        ArrayList<ArrayList<String>> formatList = new ArrayList<ArrayList<String>>();
        Scanner sc = new Scanner(in);
        while(sc.hasNext()){
            String[] t = sc.nextLine().split(" ");
            ArrayList<String> a = new ArrayList<String>();
            for(String s : t){
                a.add(s);
            }
            formatList.add(a);
        }
        sc.close();

        Coord.AcceptableFormats = formatList;
    }
    
}

class ImagePane extends JPanel{
    public ArrayList<Coord> coordinateList;
    public BufferedImage image;
    final int RADIUS = 6371;

    public ImagePane(ArrayList<Coord> list){
        coordinateList = list;
        try{
            image = ImageIO.read(new File("map.png"));
        } catch(Exception e){

        }
    }

    public Dimension Size(){
        return image == null ? new Dimension(200,200) : new Dimension(image.getWidth(), image.getHeight());
    }

    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        if(image != null){
            g2d.drawImage(image,0,0,this);
        }
        g2d.setColor(Color.BLUE);
        int centerX = image.getWidth()/2;
        int centerY = image.getHeight()/2;
        g2d.fillOval(centerX-4, centerY-4, 8, 8);
        g2d.setColor(Color.RED);
        for(Coord c : coordinateList){
            double pixelsPerDegreeX = centerX/180.0;
            int x = centerX + (int)(c.coordinates[0]*pixelsPerDegreeX);
            int y = getY(c.coordinates[1]);
            g2d.fillOval(x-4, y-4, 8, 8);
            int labelBuffer = 7*c.label.length();
            int dif = image.getWidth()-(x+labelBuffer);
            if(dif < 0){
                x += dif;
            }
            g2d.drawString(c.label, x, y-5);
        }
        g2d.dispose();
    }

    double DegreesToRadians(Double deg){
        return deg * (Math.PI/180);
    }

    int getY(Double lat){
        int centerY = image.getHeight()/2;
        int newY = (int)(0.315*centerY*Math.log(Math.tan(Math.PI/4 + DegreesToRadians(lat)/2)));
        return centerY - newY;
    }
}
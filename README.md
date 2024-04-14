# 07 Where in the World

### Nick Brown

## Included Files

- **world.java**: The main code, handles coordinate entry, geoJSON generation, and map display.
- **Coord.java**: An object class that handles the data validation and cleansing for each coordinate.
- **map.png**: A blank world map.
- **Where in the World.mp4**: A demonstration video, for those who aren't into the whole reading thing ;).=
- **AcceptableFormats.txt**: A list of valid coordinate formats used for comparison against the user's input. Can be altered for further inclusivity and robustness.
- **locations.txt** and **places.txt**: Examples of .txt files containing location values, some of which are valid coordinates and others invalid.

## How To Use

### Compilation

Simply open the folder in the terminal and run the following commands:

    javac *.java

    java world

This will begin the coordinate entry process. 

### Coordinate Entry

You can type your desired coordinates into the terminal using your choice of degrees, degree-minutes, or degree-minute-seconds format. Directions can optionally be specified (defaulting to north-east in that order if not) and a label can be added to the end.

Valid entries include:

- 50 N, 50 E
- 50 N 50 E
- 50N 50E
- 50 N 50
- 50 50 N
- 50 50 W
- -50 -50
- 50 d 50 m 50 s N, 50 d 50 m 50s E
- 50 50 50 N 50 50 50 W
- 50 d 50 m N 50 d 50 m W
- 50 50 50 50
- 50 50 50 50 50 50

A valid entry must contain either 2, 4, or 6 coordinate numbers with precision at most 6dp, a maximum of 2 directions, and be separated by spaces. Commas are optionally supported.

Invalid entries include:

- 50 50 50
- 50
- 50 50 50 50 50
- 50 50 50 50 50 50 50
- [label] 50 N, 50 W
- 91 N, 50 E
- -91 N, 50 E
- 50 N, 181 E
- 50 N, -181 E
- 50 N S 50 W
- 40 50 N W (this will actually generate a coordinate at 50N,40E with label "W")

The entry of an invalid coordinate will result in an error message detailing the invalid input.

### Map Display

Typing 'Q' into the coordinate entry will end that process and display all previously entered valid coordinates onto a mercator map projection. Additionally, a geoJSON file containing the coordinate information will be printed to the terminal and copied to the user's clipboard. Finally, a prompt to open the website [geojson.io](https://geojson.io/#map=2/0/20) using this geoJSON file will appear, allowing the user to view their chosen points on a flat map as well as a 3D globe. 

Closing the internal map window will end the program.

### Automation of Input

Running the program with the following command will enable it to read directly from a .txt file containing one coordinate on each line:

    java world filename.txt

This will automatically fill in each valid coordinate contained within the file and display it onto the map, with accompanying geoJSON information as usual.
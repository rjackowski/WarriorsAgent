package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class MapField {

    private char[][] map;
    private int sizex, sizey;
    private int treasuresLeft;
    private int warriorsLeft;
    private List<Position> warriorsPosition;

    public int getSizeX() {
        return sizex;
    }

    public int getSizeY() {
        return sizey;
    }

    public MapField(int warriors, int treasures)
    {
        warriorsLeft = warriors;
        treasuresLeft = treasures;
        generateMap();
    }

    private void generateMap()
    {
        List<Position> possibleTreasuresPos = new Vector<Position>();
        List<Position> possibleWarriorsPos = new Vector<Position>();

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("Resources/mapa.txt"));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            int countLines = 0;
            int lineLength = 0;
            while (line != null) {
                countLines++;
                lineLength = line.length() / 2;
                line = br.readLine();
            }

            map = new char[countLines][lineLength];
            sizey = countLines;
            sizex = lineLength;

            br = new BufferedReader(new FileReader("Resources/mapa.txt"));
            line = br.readLine();

            int tempLine = 0;
            while(line != null) {
                int numberOfInputedChars = 0;
                for(int i = 1; i < line.length(); i+=2) {
                    char inputChar = line.charAt(i);
                    if(inputChar == ' ' || inputChar == '#') {
                        map[tempLine][numberOfInputedChars] = inputChar;
                    }
                    else
                    {
                        map[tempLine][numberOfInputedChars] = ' ';
                        if(inputChar == 't')
                            possibleTreasuresPos.add(new Position(tempLine, numberOfInputedChars));
                        else
                            possibleWarriorsPos.add(new Position(tempLine, numberOfInputedChars));
                    }
                    numberOfInputedChars++;
                }
                line = br.readLine();
                tempLine++;
            }

            generateTreasures(possibleTreasuresPos);
            generateWarriors(possibleWarriorsPos);
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }

    private void generateTreasures(List<Position> positions)
    {
        Random random = new Random();
        for(int i = 0; i < treasuresLeft; i++) {
            int index = random.nextInt(positions.size());
            setMapField(positions.get(index), 't');
            positions.remove(index);
        }
    }

    private void generateWarriors(List<Position> positions)
    {
        Random random = new Random();
        warriorsPosition = new Vector<Position>();
        for(int i = 0; i < warriorsLeft; i++) {
            int index = random.nextInt(positions.size());
            setMapField(positions.get(index), (char)i);
            warriorsPosition.add(positions.get(index));
            positions.remove(index);
        }
    }

    private void setMapField(Position p, char c)
    {
        map[p.getX()][p.getY()] = c;
    }


    private void printMap()
    {
        for(int i = 0; i < sizey; i++)
        {
            for(int j = 0; j < sizex; j++)
                System.out.print(map[i][j]);
            System.out.println();
        }
    }

    public char getFromPosition(Position p) {
        return getFromPosition(p.getX(), p.getY());
    }

    public char getFromPosition(int x, int y) {
        return map[x][y];
    }
}

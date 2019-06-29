package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class MapField {

    private final int VISIBLE_FIELDS = 4;
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

    public MapField(int warriors, int treasures) {
        warriorsLeft = warriors;
        treasuresLeft = treasures;
        generateMap();
    }

    public boolean changeWariorLocation(int warrior, char direction) {
        boolean treasureCollected = false;
       // System.out.println("Warrior: "  + warrior);
        Position pos = warriorsPosition.get(warrior);

        System.out.println("X:" +pos.getX() + " Y:" + pos.getY() );
        Position newPos = new Position(0,0);
        switch (direction) {
            case 'L':
                newPos = new Position(pos.getX()-1,pos.getY());
                break;
            case 'R':
                newPos = new Position(pos.getX()+1,pos.getY());
                break;
            case 'T':
                newPos = new Position(pos.getX(),pos.getY()-1);
                break;
            case 'D':
                newPos = new Position(pos.getX(),pos.getY()+1);
                break;
        }

        if(map[newPos.getX()][newPos.getY()] == 't') {
            treasureCollected = true;
            treasuresLeft--;
        }

        setMapField(newPos,Character.forDigit(warrior, 10));
        setMapField(pos,' ');
        warriorsPosition.set(warrior,newPos);
        return treasureCollected;
    }

    private void generateMap() {
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

            map = new char[lineLength][countLines];
            sizey = countLines;
            sizex = lineLength;

            br = new BufferedReader(new FileReader("Resources/mapa.txt"));
            line = br.readLine();

            int tempLine = 0;
            while (line != null) {
                int numberOfInputedChars = 0;
                for (int i = 1; i < line.length(); i += 2) {
                    char inputChar = line.charAt(i);
                    if (inputChar == ' ' || inputChar == '#') {
                        map[numberOfInputedChars][tempLine] = inputChar;
                    } else {
                        map[numberOfInputedChars][tempLine] = ' ';
                        if (inputChar == 't')
                            possibleTreasuresPos.add(new Position(numberOfInputedChars,tempLine));
                        else
                            possibleWarriorsPos.add(new Position(numberOfInputedChars,tempLine));
                    }
                    numberOfInputedChars++;
                }
                line = br.readLine();
                tempLine++;
            }


            generateTreasures(possibleTreasuresPos);
            generateWarriors(possibleWarriorsPos);
        } catch (Exception e) {
          //  System.out.println(e.toString());
        }
    }

    private void generateTreasures(List<Position> positions) {
        Random random = new Random();
        for (int i = 0; i < treasuresLeft; i++) {
            int index = random.nextInt(positions.size());
            setMapField(positions.get(index), 't');
            positions.remove(index);
        }
    }

    private void generateWarriors(List<Position> positions) {
        Random random = new Random();
        warriorsPosition = new Vector<Position>();
        for (int i = 0; i < warriorsLeft; i++) {
            int index = random.nextInt(positions.size());
            setMapField(positions.get(index), Character.forDigit(i, 10));
            warriorsPosition.add(positions.get(index));
            positions.remove(index);
        }
    }

    private void setMapField(Position p, char c) {
        map[p.getX()][p.getY()] = c;
    }

    private void printMap() {
        for (int i = 0; i < sizey; i++) {
            for (int j = 0; j < sizex; j++)
                System.out.print(map[i][j]);
            System.out.println();
        }
    }

    private Vector<Character> getFields(Position position, int dx, int dy)
    {
        Vector<Character> fields = new Vector<Character>();
        int x = position.getX() + dx;
        int y = position.getY() + dy;
        for(int i = 0; i < VISIBLE_FIELDS; i++) {
            fields.add(map[x][y]);
            if(map[x][y] == '#')
                break;
            x += dx;
            y += dy;
        }
        return fields;
    }

    public InformationPackage getVisibleFields(int index) {
        InformationPackage infPackage = new InformationPackage();
        Position warriorPosition = warriorsPosition.get(index);
        infPackage.setLeftVisible(getFields(warriorPosition, -1, 0));
        infPackage.setRightVisible(getFields(warriorPosition, 1, 0));
        infPackage.setTopVisible(getFields(warriorPosition, 0, -1));
        infPackage.setDownVisible(getFields(warriorPosition, 0, 1));
        return infPackage;
    }

    public boolean allTreasuresCollected()
    {
        if(treasuresLeft == 0)
            return true;
        return false;
    }


    public char getFromPosition(Position p) {
        return getFromPosition(p.getX(), p.getY());
    }

    public char getFromPosition(int x, int y) {
        return map[x][y];
    }
}

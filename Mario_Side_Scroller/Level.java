import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Scanner;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.ArrayList;

//Code used from your level class

public class Level {
	private BufferedImage pixelMap, enemyPixelMap, background;
	private int tileWidth, tileHeight, levelCount;
	private HashMap<Integer, Image>tilePics;
	private int[][] mapLayout;
	private String backPath;

    public Level(String name, ArrayList<Goomba> goombas, int levelCount){//takes in level.txt filepath, the enemies arraylist, and the current level
    	tilePics = new HashMap<Integer, Image>();
    	this.levelCount=levelCount;
    	loadHeader(name);
    	makeFull();
    	makeFullv2();
    	loadEnemies(goombas);
    }

    public int[][] getArray(){
    	return mapLayout;//returns the current grid layout of the level
    }
    public Image getBackground(){//returns the level background
    	return background;
    }

	public Image loadImage(String name){//image loader
		InputStream stream = getClass().getResourceAsStream(name);
		Image icon=null;
		try{
			icon= ImageIO.read(stream);
		}
		catch(IOException e){
			System.out.println(e);
		}
		return icon;
	}
	public BufferedImage loadBuffImage(String name){//loads buffered image
		BufferedImage image=null;
		try {
			image=ImageIO.read(getClass().getResourceAsStream(name));
		}
		catch (IOException e) {
			System.out.println(e);
		}
		return image;
	}

    public void update(int[][] newMap){//level updater-->takes in changed map grid
    	mapLayout=newMap;
    	try{
    		background =  ImageIO.read(getClass().getResourceAsStream(backPath));
    	}
		catch(IOException e){
			System.out.println(e);
		}
    	makeFullv2();//redraws based on 2d array
    }

	/* Level file format
	 * ------------------
	 * tile width
	 * tile height
	 * background pic
	 * # of tile types
	 * ---- colour #
	 * ---- tile picture
	 */

    public void loadHeader(String name) {//loads values from level.txt file
    	try{
		    InputStream targetStream = getClass().getResourceAsStream(name);
	    	Scanner inFile = new Scanner(targetStream);
    		tileWidth = Integer.parseInt(inFile.nextLine());
    		tileHeight = Integer.parseInt(inFile.nextLine());
    		backPath =inFile.nextLine();
    		background= loadBuffImage(backPath);
    		pixelMap = loadBuffImage(inFile.nextLine());
    		enemyPixelMap = loadBuffImage(inFile.nextLine());
    		int numTile = Integer.parseInt(inFile.nextLine());

    		for(int i=0; i<numTile; i++){
    			int col = Integer.parseInt(inFile.nextLine(), 16);
    			tilePics.put(col, loadImage(inFile.nextLine()));
    		}
    	}
    	catch(Exception ex){
    		System.out.println(ex);
    	}
    }

    public void makeFull(){//turns pixel map into 2d array
    	Graphics buffG = background.getGraphics();

    	int wid = pixelMap.getWidth();
    	int height = pixelMap.getHeight();

    	mapLayout = new int[height][wid];

    	for(int x=0; x<wid; x++){//cycling through pixel map
	    	for(int y=0; y<height; y++){
    			int colour = pixelMap.getRGB(x,y);
    			colour = colour & 0xffffff;
    			if(tilePics.containsKey(colour)){//if the colour is a tile in the hashmap
    				Image tile = tilePics.get(colour);
    				int offset = tileHeight - tile.getHeight(null);
    				mapLayout[y][x]=colour;//add the colour to the 2d array
    			}
    			else{//empty space
    				mapLayout[y][x]=0;
    			}
	    	}
    	}
    }
    public void makeFullv2(){//draws level backgroud based on 2d array
    	Graphics buffG = background.getGraphics();
    	int wid = pixelMap.getWidth();
    	int height = pixelMap.getHeight();

    	for(int x=0; x<wid; x++){//cycling through 2d array
	    	for(int y=0; y<height; y++){
    			int colour = mapLayout[y][x];
    			colour = colour & 0xffffff;
    			if(tilePics.containsKey(colour)){
    				Image tile = tilePics.get(colour);
    				int offset = tileHeight - tile.getHeight(null);
    				buffG.drawImage(tile, x*tileWidth, y*tileHeight+offset,null);
    			}
	    	}
    	}
    }

	public void loadEnemies(ArrayList<Goomba> goombas){//takes in enemies arraylist
		int wid = enemyPixelMap.getWidth();
    	int height = enemyPixelMap.getHeight();

    	for(int x=0; x<wid; x++){//cycles through the enemy pixel map
	    	for(int y=0; y<height; y++){
    			int colour = enemyPixelMap.getRGB(x,y);
    			colour = colour & 0xffffff;
    			if(colour==3281153){//if an enemy is to be drawn
    				goombas.add(new Goomba(x*tileWidth, y*tileHeight, 0, mapLayout, levelCount));//add enemy to the arraylist
    			}
	    	}
    	}
	}
}
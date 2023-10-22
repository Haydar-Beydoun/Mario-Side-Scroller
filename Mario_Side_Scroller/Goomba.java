import javax.swing.*;
import javax.imageio.ImageIO;
import java.io.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.*;
import java.util.Arrays;
import java.util.ArrayList;

public class Goomba{
	private double x,y, dx, dy, speed;//x/y coords., change in coords, movement speed
	private int spriteCounter, spriteNum, dir, finalOffset, levelCount, squishedDelay;
	private int[][] mapLayout;//level map grid
	private long squishedTimer;
	private boolean leftCollision, rightCollision, upCollision, downCollision, squished;
	private BufferedImage goomba_run1, goomba_run2, goomba_squish, goombaImage;
	private String direction;//direction the goomba is headed in

	public Goomba(double x, double y, int finalOffset, int[][] map, int levelCount){//takes in its starting coords, level offset, updated map layout, and the current level
		this.x=x;
		this.finalOffset=finalOffset;
		this.y=y;
		this.levelCount=levelCount;
		dir=1;//starts facing right
		mapLayout=map;
		speed=2;
		dx=speed;
		dy=10;
		spriteCounter=0;
		spriteNum=1;
		squishedDelay=500;//how long it shows itself squished(defeated) before being removed
		squished=false;
		getImages();
	}

	public void getImages(){//getting sprites
		try{
			if(levelCount==1){//the first level-->they are coloured brown
				goomba_run1 =ImageIO.read(getClass().getResourceAsStream("/enemies/goomba_run1.png"));
				goomba_run2 =ImageIO.read(getClass().getResourceAsStream("/enemies/goomba_run2.png"));
				goomba_squish =ImageIO.read(getClass().getResourceAsStream("/enemies/goomba_squish.png"));
			}
			else{//the second level-->tehy are coloured blue
				goomba_run1 =ImageIO.read(getClass().getResourceAsStream("/enemies/blue_goomba_run1.png"));
				goomba_run2 =ImageIO.read(getClass().getResourceAsStream("/enemies/blue_goomba_run2.png"));
				goomba_squish =ImageIO.read(getClass().getResourceAsStream("/enemies/blue_goomba_squish.png"));
			}
		}
		catch(IOException e){
			System.out.println(e);
		}
	}
	public double getx(){//returns the x-coord.
		return x;
	}
	public double gety(){//returns the y-coord.
		return y;
	}
	public Rectangle getRect(){//returns a rectangle that is drawn around the enemy
		return new Rectangle((int)x, (int)y, 32, 32);//rectangle is based on in-game coords.
	}
	public boolean isSquished(){//used to check if the enemy is already defeated
		return squished;
	}

	public void changeImage(){//sprite cycler
		spriteCounter++;
		if(spriteCounter>5){
			if(spriteNum==1){
				spriteNum=2;
			}
			else if(spriteNum==2){
				spriteNum=1;
			}
			spriteCounter=0;
		}
	}
	public BufferedImage setImage(){//sprite setter
		BufferedImage image=null;
		if(squished){
			image = goomba_squish;
		}
		else{
			if(spriteNum==1){
				image = goomba_run1;
			}
			else if(spriteNum==2){
				image = goomba_run2;
			}
		}
		return image;
	}

	public void collisionChecker(){//enemy level collision checker
		double playerLeftX, playerRightX, playerTopY, playerBotY;//enemy sprite corners

		playerLeftX=x-13;
		playerRightX=x+13;
		playerTopY=y;
		playerBotY=y+32;

		double playerSpeed=speed;
		double playerCol=x/32;
		double playerLeftCol=playerLeftX/32;
		double playerRightCol=playerRightX/32;
		double playerTopRow=playerTopY/32;
		double playerBotRow=playerBotY/32;

		int tileNum1, tileNum2;


		//Down Collision Checker//////////////////////////////////////////////////////////////////////////////

		tileNum1 = mapLayout[(int)playerBotRow][(int)playerLeftCol];//bottom left corner tile
		tileNum2 = mapLayout[(int)playerBotRow][(int)playerRightCol];//bottom right corner tile

		if(tileNum1!=0 || tileNum2!=0 && tileNum1!=16760832 && tileNum2!=16760832 && tileNum1!=20223 && tileNum2!=20223){//if the bottom is not empty, coins, or flag poles
			if(tileNum1!=16760832 && tileNum2!=16760832){//prevents jumping on coins
				direction="down";
				downCollision=true;
			}
		}
		else{
			downCollision=false;
		}
		playerBotRow=playerBotY/32;

		//Left Collision Checker//////////////////////////////////////////////////////////////////////////////
		playerLeftCol = (playerLeftX-playerSpeed)/32;

		tileNum1 = mapLayout[(int)playerTopRow][(int)playerLeftCol];//top left corner tile
		tileNum2 = mapLayout[(int)playerBotRow][(int)playerLeftCol];//bottom left corner tile
		int tileNum3 = mapLayout[(int)playerBotRow][(int)playerLeftCol];//bottom left corner tile

		if(tileNum1!=0 && tileNum2!=0 && tileNum1!=16760832 && tileNum2!=16760832 && tileNum1!=9837596 && tileNum2!=9837596 && tileNum1!=16750080 && tileNum2!=16750080){//if the tiles are not empty space, coins, or powerups
			leftCollision=true;
			dir=1;//change direction
		}
		else if(tileNum3==0){//if the bottom left is empty space-->no more tile to move on
			leftCollision=true;
			dir=1;//change direction
		}
		else{
			leftCollision=false;
		}
		playerLeftCol=playerLeftX/32;

		//Right Collision Checker//////////////////////////////////////////////////////////////////////////////
		playerRightCol = (playerRightX+playerSpeed)/32;
		tileNum1 = mapLayout[(int)playerTopRow][(int)playerRightCol];//top right corner tile
		tileNum2 = mapLayout[(int)playerBotRow][(int)playerRightCol];//bottom right corner tile
		tileNum3 = mapLayout[(int)playerBotRow][(int)playerRightCol];//bottom right corner tile

		if(tileNum1!=0 && tileNum2!=0 && tileNum1!=16760832 && tileNum2!=16760832 && tileNum1!=9837596 && tileNum2!=9837596 && tileNum1!=16750080 && tileNum2!=16750080){//if the tiles are not empty space, coins, or powerups
			//Changes upon Collision
			rightCollision=true;
			dir=-1;//change direction
		}
		else if(tileNum3==0){//if the bottom right is empty space-->no more tile to move on
			rightCollision=true;
			dir=-1;//change direction
		}
		else{
			rightCollision=false;
		}
		playerRightCol=playerRightX/32;
	}

	public void move(){//enemy movement
		if(leftCollision==false || rightCollision==false){//if the enemy can move in at least a single direction
			x+=dx*dir;
		}
		if(downCollision==false){//if enemy is not on the ground
			y+=dy;//gravity
		}

	}
	public void squish(){//used to put enemy in squished(defeated) state
		squished=true;
	}

	public boolean update(int[][] map, int finalOffset){//takes in updated map and level offset
		mapLayout=map;
		this.finalOffset=finalOffset;
		
		if(y>0 && y<mapLayout[0].length*32 && x>0 && x<mapLayout[0].length*32){//out of bounds prevention
			collisionChecker();
		}
		if(squished==false){//if enemy isn't squished-->they can move
			move();
		}
		changeImage();//change sprite

		if(squished==false){//reset timer until enemy is squished
			squishedTimer=System.nanoTime();
		}
		
		long elapsed = (System.nanoTime()-squishedTimer)/1000000;//time elapsed since since enemy became squished

		if(squished && elapsed>squishedDelay){//if the enemy is defeated and has been squished for a certain amount of time-->return yes so the enemy could be removed from the array list
			return true;
		}

		if(y>=800){//if the enemy falls out of the map-->get removed from the array list
			return true;
		}
		else{
			return false;
		}
	}

	public void draw(Graphics g){//draw enemy
		goombaImage=setImage();//set current sprite

		int flipOffset=0;

		if(dir==-1){
			flipOffset=32;
		}

		g.drawImage(goombaImage, ((int)x+flipOffset-16)+finalOffset, (int)y, 32*dir, 32, null);
	}
}
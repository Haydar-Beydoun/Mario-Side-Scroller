import javax.swing.*;
import javax.imageio.ImageIO;
import java.io.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.*;
import java.util.Arrays;
import java.util.ArrayList;

public class Shot{
	private double x,y, dx, dy, speed;//x/y coords., change in coords, movement speed
	private long shootTimer;//keeps track of how much time has passed since the shot was fired
	private int shootMax, spriteCounter, spriteNum, dir, finalOffset;//max time of shot existing
	private int[][] mapLayout;
	private boolean leftCollision, rightCollision, upCollision, downCollision, hit;
	private BufferedImage fireBallImage, fireball1, fireball2, fireball3, fireball4, fireball_explode1, fireball_explode2, fireball_explode3;
	private String direction;//direction of movement

	public Shot(double x, double y, int finalOffset,int dir, int[][] map){//takes in player coords, level offset, direction it should move(where player is facing), and the map grid
		this.x=x;
		this.finalOffset=finalOffset;
		this.y=y+15;
		this.dir=dir;
		mapLayout=map;
		speed=12;
		dx=speed*dir;
		dy=10;
		shootTimer=System.nanoTime();
		shootMax=1000;
		spriteCounter=0;
		spriteNum=1;
		getImages();//getting all the sprites
	}

	public void getImages(){//getting sprites
		try{
			fireball1 =ImageIO.read(getClass().getResourceAsStream("/player/fireball1.png"));
			fireball2 =ImageIO.read(getClass().getResourceAsStream("/player/fireball2.png"));
			fireball3 =ImageIO.read(getClass().getResourceAsStream("/player/fireball3.png"));
			fireball4 =ImageIO.read(getClass().getResourceAsStream("/player/fireball4.png"));

			fireball_explode1 =ImageIO.read(getClass().getResourceAsStream("/player/fireball_explode1.png"));
			fireball_explode2 =ImageIO.read(getClass().getResourceAsStream("/player/fireball_explode2.png"));
			fireball_explode3 =ImageIO.read(getClass().getResourceAsStream("/player/fireball_explode3.png"));
		}
		catch(IOException e){
			System.out.println(e);
		}
	}
	public double getx(){//returns the x coord
		return x;
	}
	public double gety(){//returns y coord
		return y;
	}
	public Rectangle getRect(){//returns a rectangle that is drawn around the fireball
		return new Rectangle((int)x, (int)y, 16, 16);//rectangle is based on in-game coords.
	}

	public void changeImage(){//sprite frame count changer
		spriteCounter++;
		if(spriteCounter>1){
			if(spriteNum==1){
				spriteNum=2;
			}
			else if(spriteNum==2){
				spriteNum=3;
			}
			else if(spriteNum==3){
				spriteNum=4;
			}
			else if(spriteNum==4){
				spriteNum=1;
			}
			spriteCounter=0;
		}
	}
	public BufferedImage setImage(){//sprite setter for fireball
		BufferedImage image=null;
		if(hit==false){//if the ball hasn't hit anything-->spinn normally
			if(spriteNum==1){
				image = fireball1;
			}
			else if(spriteNum==2){
				image = fireball2;
			}
			else if(spriteNum==3){
				image = fireball3;
			}
			else if(spriteNum==4){
				image = fireball4;
			}
		}
		else{//if the shot has hit something-->go into explosion animation
			if(spriteNum==1){
				image = fireball_explode1;
			}
			else if(spriteNum==2){
				image = fireball_explode2;
			}
			else if(spriteNum>=3){
				image = fireball_explode3;
			}	
		}
		return image;
	}
	public void setHit(){//puts shot into hit mode-->allows for external state change
		hit=true;
	}

	public void collisionChecker(){//fireball level collision checker
		double playerLeftX, playerRightX, playerTopY, playerBotY;//fireball sprite corners

		playerLeftX=x;
		playerRightX=x+16;
		playerTopY=y;
		playerBotY=y+16;

		double playerSpeed=speed;
		double playerCol=x/32;
		double playerLeftCol=playerLeftX/32;
		double playerRightCol=playerRightX/32;
		double playerTopRow=playerTopY/32;
		double playerBotRow=playerBotY/32;

		int tileNum1, tileNum2;


		//Down Collision Checker//////////////////////////////////////////////////////////////////////////////
		playerBotRow = (playerBotY+10)/32;//incorporates gravity into determination of bottom row

		tileNum1 = mapLayout[(int)playerBotRow][(int)playerLeftCol];//bottom left corner tile
		tileNum2 = mapLayout[(int)playerBotRow][(int)playerRightCol];//bottom right corner tile

		if(tileNum1!=0 || tileNum2!=0 && tileNum1!=16760832 && tileNum2!=16760832 && tileNum1!=20223 && tileNum2!=20223){//if the fireball is not touching empty space, coins, or flag pole
			if(tileNum1!=16760832 && tileNum2!=16760832){//prevents bouncing on coins
				direction="down";
				downCollision=true;
			}
		}
		else{
			downCollision=false;
		}
		playerBotRow=playerBotY/32;

		//Left Collision Checker//////////////////////////////////////////////////////////////////////////////
		playerLeftCol = (playerLeftX-playerSpeed)/32;//incorporates speed to the left for column determination

		tileNum1 = mapLayout[(int)playerTopRow][(int)playerLeftCol];//top left corner tile

		if(tileNum1!=0 && tileNum1!=16760832 && dir==-1){//if the tile is not an empty space, coin, and only checks for left collision if the fire is moving to the left
			leftCollision=true;
		}
		playerLeftCol=playerLeftX/32;

		//Right Collision Checker//////////////////////////////////////////////////////////////////////////////
		playerRightCol = (playerRightX+playerSpeed)/32;
		tileNum1 = mapLayout[(int)playerTopRow][(int)playerRightCol];

		if(tileNum1!=0 && tileNum1!=16760832 && dir==1){
			//Changes upon Collision
			rightCollision=true;
		}
		playerRightCol=playerRightX/32;
	}

	public void move(){//fireball movemnet
		if(leftCollision==false || rightCollision==false){//if the fire hasn't collided with anything on the left/right
			x+=dx;
		}
		if(downCollision==false){//not on the ground
			y+=dy;//gravity
		}

	}
	public boolean update(int[][] map, int finalOffset){//takes in an updated level map and level offset, returns true if it should be removed from shot array list
		mapLayout=map;
		this.finalOffset=finalOffset;

		if(y>0 && y<mapLayout[0].length*32 && x>0 && x<mapLayout[0].length*32){//out of bounds prevention
			collisionChecker();
		}
		if(hit==false){//move if shot hasn't hit anything
			move();
		}
		changeImage();//cycle through sprites

		long elapsed = (System.nanoTime()-shootTimer)/1000000;//time elapsed since the shot spawned

		if(leftCollision || rightCollision || elapsed>shootMax){
			hit=true;
		}

		if(elapsed>shootMax || leftCollision || rightCollision || hit){
			if(fireBallImage == fireball_explode3){//if the shot has made it to the last frame of its explosion
				return true;
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}

	public void draw(Graphics g){//drawing fireball
		fireBallImage=setImage();//setting current sprite

		int flipOffset=0;

		if(dir==-1){
			flipOffset=16;
		}

		g.drawImage(fireBallImage, ((int)x+flipOffset-8)+finalOffset, (int)y, 16*dir, 16, null);
	}
}
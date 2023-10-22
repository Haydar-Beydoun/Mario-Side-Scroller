/*Side Scroller FSE
 *Haydar Beydoun
 *Side scroller Game - Controls: (L/R Arrow Keys: Movement)(Up key: Shooting (if power is active))(Spcae Key: Jumping)
 */

import javax.swing.*;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.io.*;
import java.awt.*;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Scanner;

class main{
 	public static void main(String [] args){
		Mario frame = new Mario();
 	}
}

class Mario extends JFrame{
	GamePanel game = new GamePanel();
	public Mario(){
		super("Super Plumber Fella");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(960, 800);
		setLocationRelativeTo(null);
		setResizable(false);
		add(game);
		setVisible(true);
	}
}

class GamePanel extends JPanel implements KeyListener, ActionListener{
	private double x, y,jumpSpeed;//x an y coordinates, jump movement interval
	private float opacity;
	private int offset, finalOffset, tileSize, playerSpeed, flip, spriteCounter, spriteNum, coins, lives, levelCount, scale, flipOffset;
	private long transitionDelay, transitionTimer, transitionElapsed, shootTimer, shootDelay, hurtElapsed,hurtDelay, hurtTimer;
	private int[][]mapLayout;
	private boolean[] keys;
	private ArrayList<Shot> shots = new ArrayList<Shot>();//fireball array list
	private ArrayList<Goomba> goombas = new ArrayList<Goomba>();//enemy array list
	private boolean spacePressed, leftPressed, rightPressed, upCollision, downCollision, leftCollision, rightCollision, flagCollision, jump, start, transition, shoot, invincible;
	private BufferedImage small_run1, small_run2, small_run3, small_idle, small_jump,small_death, small_flag,big_run1, big_run2, big_run3, big_idle, 
						  big_jump, big_flag,fire_run1, fire_run2, fire_run3, fire_idle, fire_jump, fire_shoot, fire_flag;//player sprites
	private String  direction, playerPower,
					music1Path, music2Path, break_blockPath, bump_blockPath, coinPath, deathPath, fireballPath, one_upPath,
					flag_polePath, game_overPath, jumpPath, kickPath, power_appearsPath, power_upPath, stage_clearPath, bouncePath, power_downPath;//sound directories, and more
	private Level level;
	private Sound music1, music2, block, coinSound, jumpSound, power_appearsSound, power_upSound, flag_poleSound, stage_clearSound, game_overSound, 
				  deathSound, one_upSound, fireballSound, kickSound, power_downSound;//custom sound class objects that play sound effects/music
	Image coin, lifeIcon, titleScreen, titleText;//UI icons
	Timer timer;

	public GamePanel(){
		x=100;
		y=700;
		scale=1;//player sprite scaler
		flipOffset=0;//player sprite position corrector
		spriteCounter=0;//frame counter
		spriteNum=1;//what sprite will be drawn
		playerSpeed=5;
		jumpSpeed=10;
		tileSize=32;
		coins=0;//coin counter
		lives=3;
		levelCount=1;
		opacity=1f;//the transperancy of the player
		transitionTimer=System.nanoTime();
		transitionDelay=4000;//how long the transition screen will last
		hurtTimer=System.nanoTime();
		hurtDelay=2000;//invincibility length upon being hit
		shootTimer=System.nanoTime();
		shootDelay=250;//delay between fireball shots
		direction="right";//direction starts in the right
		playerPower="small";
		flip=1;//sprite facing right
		upCollision=false;
		downCollision=false;
		leftCollision=false;
		rightCollision=false;
		flagCollision=false;
		spacePressed=false;
		invincible=false;
		shoot=false;
		jump=false;
		start=false;//the game is being played and not on the title/transition screen
		transition=false;//not in transition screen
		keys = new boolean[KeyEvent.KEY_LAST+1];
		level = new Level("maps/level1.txt", goombas, levelCount);//loads level 1 from txt file, passes in Goombas to be loaded, the level nnumber is inputted to determine tile/enemy colour themes
		mapLayout=level.getArray();//2d array for grid based collision
		addKeyListener(this);
		getPlayerImages();//load sprites
		getUIImages();//load UI images
		getSounds();//load sounds, and assisting variables
	//	setDoubleBuffered(true);
		setFocusable(true);
		requestFocus();
		timer = new Timer(20, this);
		timer.start();
	}

	public void getUIImages(){//loads UI images
		try{
			coin = ImageIO.read(getClass().getResourceAsStream("/Object/Coin.png"));
			lifeIcon = ImageIO.read(getClass().getResourceAsStream("/Object/Life.png"));
			titleScreen = ImageIO.read(getClass().getResourceAsStream("/BG/title.png"));
			titleText = ImageIO.read(getClass().getResourceAsStream("/BG/title_text.png"));
		}
		catch(IOException e){
			System.out.println(e);
		}
	}
	public void getPlayerImages(){//loads player sprites
		try{
			small_run1  = ImageIO.read(getClass().getResourceAsStream("/player/small_run1.png"));
			small_run2  = ImageIO.read(getClass().getResourceAsStream("/player/small_run2.png"));
			small_run3  = ImageIO.read(getClass().getResourceAsStream("/player/small_run3.png"));
			small_idle  = ImageIO.read(getClass().getResourceAsStream("/player/small_idle.png"));
			small_jump  = ImageIO.read(getClass().getResourceAsStream("/player/small_jump.png"));
			small_death = ImageIO.read(getClass().getResourceAsStream("/player/small_death.png"));
			small_flag  = ImageIO.read(getClass().getResourceAsStream("/player/small_flag.png"));

			big_run1 = ImageIO.read(getClass().getResourceAsStream("/player/big_run1.png"));
			big_run2 = ImageIO.read(getClass().getResourceAsStream("/player/big_run2.png"));
			big_run3 = ImageIO.read(getClass().getResourceAsStream("/player/big_run3.png"));
			big_idle = ImageIO.read(getClass().getResourceAsStream("/player/big_idle.png"));
			big_jump = ImageIO.read(getClass().getResourceAsStream("/player/big_jump.png"));
			big_flag = ImageIO.read(getClass().getResourceAsStream("/player/big_flag.png"));

			fire_run1 = ImageIO.read(getClass().getResourceAsStream("/player/fire_run1.png"));
			fire_run2 = ImageIO.read(getClass().getResourceAsStream("/player/fire_run2.png"));
			fire_run3 = ImageIO.read(getClass().getResourceAsStream("/player/fire_run3.png"));
			fire_idle = ImageIO.read(getClass().getResourceAsStream("/player/fire_idle.png"));
			fire_jump = ImageIO.read(getClass().getResourceAsStream("/player/fire_jump.png"));
			fire_flag = ImageIO.read(getClass().getResourceAsStream("/player/fire_flag.png"));
			fire_shoot = ImageIO.read(getClass().getResourceAsStream("/player/fire_shoot.png"));
		}
		catch(IOException e){
			System.out.println(e);
		}
	}
	public void getSounds(){//loads sounds, sets filePath strings
		//Sound Objects
		music1 = new Sound();
		music2 = new Sound();
		block = new Sound();
		coinSound = new Sound();
		jumpSound = new Sound();
		power_appearsSound = new Sound();
		power_upSound = new Sound();
		flag_poleSound = new Sound();
		stage_clearSound = new Sound();
		game_overSound = new Sound();
		deathSound = new Sound();
		one_upSound =  new Sound();
		fireballSound = new Sound();
		kickSound = new Sound();
		power_downSound = new Sound();
		
		//Sound paths
		music1Path        = "sounds/level1_music.wav";
		music2Path        = "sounds/level2_music.wav";
		break_blockPath   = "sounds/break_block.wav";
		bump_blockPath    = "sounds/bump_block.wav";
		coinPath          = "sounds/coin.wav";
		deathPath         = "sounds/death.wav";
		fireballPath      = "sounds/fireball.wav";
		flag_polePath     = "sounds/flag_pole.wav";
		game_overPath     = "sounds/game_over.wav";
		jumpPath          = "sounds/jump.wav";
		kickPath   		  = "sounds/kick.wav";
		power_appearsPath = "sounds/power_appears.wav";
		power_upPath      = "sounds/power_up.wav";
		stage_clearPath   = "sounds/stage_clear.wav";
		bouncePath        = "sounds/bounce.wav";
		one_upPath        = "sounds/one_up.wav";
		power_downPath   = "sounds/power_down.wav";

		music1.play(music1Path, true);
	}

	public BufferedImage setPlayerImage(){//sprite setter for player
		BufferedImage playerImage=null;

		if(playerPower=="small"){//sets based on power-->small
			if(flagCollision==true){//if player has interacted with the flag
				playerImage=small_flag;
			}
	    	else if(downCollision==false){//not touching ground-->jumping or falling
	    		playerImage	= small_jump;
	    	}
	    	else if(leftPressed || rightPressed){//moving to the right or left
				if(spriteNum==1){//based on the sprite number, will draw a different frame of the run animation
					playerImage	= small_run1;
				}
				if(spriteNum==2){
					playerImage	= small_run2;
				}
				if(spriteNum==3){
					playerImage	= small_run3;
				}
	    	}
	    	else{
	    		playerImage	= small_idle;
	    	}
		}
		else if(playerPower=="big"){//sets based on power-->big
			if(flagCollision==true){
				playerImage=big_flag;
			}
	    	else if(downCollision==false){
	    		playerImage	= big_jump;
	    	}
	    	else if(leftPressed || rightPressed){
				if(spriteNum==1){
					playerImage	= big_run1;
				}
				if(spriteNum==2){
					playerImage	= big_run2;
				}
				if(spriteNum==3){
					playerImage	= big_run3;
				}
	    	}
	    	else{
	    		playerImage	= big_idle;
	    	}
		}
		else if(playerPower=="fire"){//sets based on power-->fire
			long elapsed = (System.nanoTime()-shootTimer)/1000000;
			if(flagCollision==true){
				playerImage=fire_flag;
			}
			else if(elapsed<shootDelay/2){
				playerImage=fire_shoot;
			}
	    	else if(downCollision==false){
	    		playerImage	= fire_jump;
	    	}
	    	else if(leftPressed || rightPressed){
				if(spriteNum==1){
					playerImage	= fire_run1;
				}
				if(spriteNum==2){
					playerImage	= fire_run2;
				}
				if(spriteNum==3){
					playerImage	= fire_run3;
				}
	    	}
	    	else{
	    		playerImage	= fire_idle;
	    	}
		}
		if(direction=="right"){//sets the sprite flip value based on the direction headed
			flip=1;
		}
		if(direction=="left"){
			flip=-1;
		}
    	return playerImage;//returns what the player image should equal
	}
	public void changePlayerImage(){//sprite frame count changer
		spriteCounter++;//used to change the sprite every set amount of function calls
		if(spriteCounter>2){//if the function has been called two or more times-->change sprite
			if(spriteNum==1){
				spriteNum=2;
			}
			else if(spriteNum==2){
				spriteNum=3;
			}
			else if(spriteNum==3){
				spriteNum=1;
			}
			spriteCounter=0;//counter reset
		}
	}

	public void collisionChecker(){//player map collision checker
		double playerLeftX, playerRightX, playerTopY, playerBotY;//player sprite corners

		if(playerPower=="small"){//if the player is small(32x32)
			playerLeftX=x-13;
			playerRightX=x+13;
			playerTopY=y;
			playerBotY=y+32;
		}
		else{//if the player is big or fir(32*64)
			playerLeftX=x-13;
			playerRightX=x+13;
			playerTopY=y;
			playerBotY=y+64;
		}

		double playerCol=x/32;//what level grid column the player is currently at
		double playerLeftCol=playerLeftX/32;//the column to the left of the player
		double playerRightCol=playerRightX/32;//the column to the right of the player
		double playerTopRow=playerTopY/32;//the row above the player
		double playerBotRow=playerBotY/32;//the row under the player

		int tileNum1, tileNum2;//what tiles the corners of the player are hitting

		//Up Collision Checker//////////////////////////////////////////////////////////////////////////////
		playerTopRow = (playerTopY-(jumpSpeed*2)+10)/32;//changed value to accomodate player jumping-->subtracts by jump(jumpSpeed*2) and adds gravity(10)
		tileNum1 = mapLayout[(int)playerTopRow][(int)playerLeftCol];//tile at top left corner of the player
		tileNum2 = mapLayout[(int)playerTopRow][(int)playerRightCol];//tile at top right corner of the player

		if(tileNum1!=0 || tileNum2!=0){//if a tile is at specified locations-->not empty spaces
			//Brick Collision
			if(tileNum1==9127433){//if the left corner is hitting a brick block
				if((int)playerTopRow-1>=0){//out of bounds prevention when checking 2 rows above you
					if(mapLayout[(int)playerTopRow-1][(int)playerLeftCol]==16760832){//coin on top of brick
						mapLayout[(int)playerTopRow-1][(int)playerLeftCol]=0;//change the coin space to empty
						coins++;//add a coin
						level.update(mapLayout);//update the level-->redraws level based on updated mapLayout
						coinSound.play(coinPath, false);//play coin sound effect-->coin sound path is given, the boolean determines if the sound will be looped
					}
				}
				if(playerPower=="big" || playerPower=="fire"){//if the player power isn't small-->can break the brick
					mapLayout[(int)playerTopRow][(int)playerLeftCol]=0;//make the brick space an empty space(break brick)
					level.update(mapLayout);//map update
					block.play(break_blockPath, false);//play brick break sound effect
				}
				else{//if block cannot be brocken
					block.play(bump_blockPath, false);//only play the block bumped sound effect
				}
			}
			else if(tileNum2==9127433){//if the right corner is hitting a brick block-->same as above code within
				if((int)playerTopRow-1>=0){
					if(mapLayout[(int)playerTopRow-1][(int)playerRightCol]==16760832){//coin on top of brick
						mapLayout[(int)playerTopRow-1][(int)playerRightCol]=0;
						coins++;
						level.update(mapLayout);
						coinSound.play(coinPath, false);
					}
				}
				if(playerPower=="big" || playerPower=="fire"){
					mapLayout[(int)playerTopRow][(int)playerRightCol]=0;
					level.update(mapLayout);
					block.play(break_blockPath, false);
				}
				else{
					block.play(bump_blockPath, false);
				}
			}

			//Coin Collision
			if(mapLayout[(int)playerTopRow][(int)playerLeftCol]==16760832){//if there is a coin at your top left
				mapLayout[(int)playerTopRow][(int)playerLeftCol]=0;//empty the space
				level.update(mapLayout);//update the map
				coins++;//add a coin
				coinSound.play(coinPath, false);//play effect
			}
			if(mapLayout[(int)playerTopRow][(int)playerRightCol]==16760832){//if there is a coin at your top right
				mapLayout[(int)playerTopRow][(int)playerRightCol]=0;
				level.update(mapLayout);
				coins++;
				coinSound.play(coinPath, false);
			}

			//Mystery Block Collision
			if(mapLayout[(int)playerTopRow][(int)playerCol]==65280 || mapLayout[(int)playerTopRow][(int)playerCol]==8295936){//mystery block hit (whether it is a fake or real one)
				if(mapLayout[(int)playerTopRow][(int)playerCol]==65280){//if not a fake mystery-->give power up
					if(playerPower=="small"){//mushroom power up spawn if you are small
						mapLayout[(int)playerTopRow-1][(int)playerCol]=9837596;//spawn a mushroom on top of the block
						power_appearsSound.play(power_appearsPath, false);//sound effect
					}
					else if(playerPower=="big"){//fire spawn
						mapLayout[(int)playerTopRow-1][(int)playerCol]=16750080;
						power_appearsSound.play(power_appearsPath, false);
					}
					else{//if you already have the best power up-->give a coin
						coins++;
						coinSound.play(coinPath, false);
					}
				}
				else{//if it is a fake
					coins++;
					coinSound.play(coinPath, false);
				}
				mapLayout[(int)playerTopRow][(int)playerCol]=16580352;
				level.update(mapLayout);
			}

			//Mushroom Collision
			if(mapLayout[(int)playerTopRow][(int)playerLeftCol]==9837596){//if there is a mushroom at your top left
				mapLayout[(int)playerTopRow][(int)playerLeftCol]=0;//make space empty
				if(playerPower!="fire"){//if you dont already have a better power
					playerPower="big";//make big
				}
				level.update(mapLayout);
				power_upSound.play(power_upPath, false);
			}
			if(mapLayout[(int)playerTopRow][(int)playerRightCol]==9837596){//if there is a mushroom at your top right
				mapLayout[(int)playerTopRow][(int)playerRightCol]=0;
				playerPower="big";
				level.update(mapLayout);
				power_upSound.play(power_upPath, false);
			}

			//Fire Flower Collision
			if(mapLayout[(int)playerTopRow][(int)playerLeftCol]==16750080){//if there is a fire flower at your top left
				mapLayout[(int)playerTopRow][(int)playerLeftCol]=0;//make space empty
				playerPower="fire";//give fire power
				level.update(mapLayout);//update map
				power_upSound.play(power_upPath, false);//play sound effect
			}
			if(mapLayout[(int)playerTopRow][(int)playerRightCol]==16750080){//if there is a fire flower at your top right
				mapLayout[(int)playerTopRow][(int)playerRightCol]=0;
				playerPower="fire";
				level.update(mapLayout);
				power_upSound.play(power_upPath, false);
			}

			//Changes upon Collision
			if(tileNum1!=16760832 && tileNum2!=16760832){//if you are not colliding with a coin
				jump=false;//stop jumping
				jumpSpeed=0;//stop jump movement intervel
				upCollision=true;//upward collision is now true
				direction="down";//will now be falling down
			}
		}
		else{//if the space above you is empty
			upCollision=false;//you are not colliding upwards
		}
		playerTopRow=playerTopY/32;//rests player top row

		//Down Collision Checker//////////////////////////////////////////////////////////////////////////////
		if(jump){//if you ar jumping
			playerBotRow = (playerBotY+(jumpSpeed*2)+10)/32;//accomodates jump movement and gravity to determine what row is under you
		}
		else{
			playerBotRow = (playerBotY+10)/32;//accomodates gravity to determine what row is under you
		}

		tileNum1 = mapLayout[(int)playerBotRow][(int)playerLeftCol];//tile at bottom left corner of the player
		tileNum2 = mapLayout[(int)playerBotRow][(int)playerRightCol];//tile at bottom right corner of the player

		if(tileNum1!=0 || tileNum2!=0 && tileNum1!=16760832 && tileNum2!=16760832 && tileNum1!=20223 && tileNum2!=20223){//if the tile your hitting isnt empty space, a coin, or a flag pole
			//Coin Collision
			if(mapLayout[(int)playerBotRow][(int)playerLeftCol]==16760832){//if the tile at the bottom left is a coin
				mapLayout[(int)playerBotRow][(int)playerLeftCol]=0;//make the coin space empty
				level.update(mapLayout);//update level
				coins++;//add a coin
				coinSound.play(coinPath, false);//play sound effect
			}
			if(mapLayout[(int)playerBotRow][(int)playerRightCol]==16760832){//if the tile at the bottom right is a coin
				mapLayout[(int)playerBotRow][(int)playerRightCol]=0;
				level.update(mapLayout);
				coins++;
				coinSound.play(coinPath, false);
			}
			//Mushroom Collision
			if(mapLayout[(int)playerBotRow][(int)playerLeftCol]==9837596){//if the tile at the bottom left is a mushroom
				mapLayout[(int)playerBotRow][(int)playerLeftCol]=0;//make the mushroom space empty
				if(playerPower!="fire"){//if you don't already have a better power up
					playerPower="big";//give power up
				}
				level.update(mapLayout);//update the map
				power_upSound.play(power_upPath, false);//play sound effect
			}
			if(mapLayout[(int)playerBotRow][(int)playerRightCol]==9837596){//if the tile at the bottom right is a mushroom
				mapLayout[(int)playerBotRow][(int)playerRightCol]=0;
				playerPower="big";
				level.update(mapLayout);
				power_upSound.play(power_upPath, false);
			}

			//Fire Flower Collision
			if(mapLayout[(int)playerBotRow][(int)playerLeftCol]==16750080){//if the tile at the bottom left is a fire flower
				mapLayout[(int)playerBotRow][(int)playerLeftCol]=0;//make the space empty
				playerPower="fire";//give fire power
				level.update(mapLayout);//update map
				power_upSound.play(power_upPath, false);//play sound effect
			}
			if(mapLayout[(int)playerBotRow][(int)playerRightCol]==16750080){//if the tile at the bottom right is a fire flower
				mapLayout[(int)playerBotRow][(int)playerRightCol]=0;
				playerPower="fire";
				level.update(mapLayout);
				power_upSound.play(power_upPath, false);
			}

			//Changes upon Collision
			if(tileNum1!=16760832 && tileNum2!=16760832){//prevents jumping on coins
				jumpSpeed=10;//if you are on the ground, reset the jumpSpeed so you can jump
				direction="down";//you are moving down
				jump=false;//allows you to jump again
				downCollision=true;//there is down collision
			}
		}
		else{//spaces are empty
			downCollision=false;//there is no down collision
		}
		playerBotRow=playerBotY/32;//reset player bot row

		//Left Collision Checker//////////////////////////////////////////////////////////////////////////////
		playerLeftCol = (playerLeftX-playerSpeed)/32;//accomodating player speed to the left to determine the column to the left of the player

		int tileNum0 = mapLayout[(int)playerTopRow+1][(int)playerLeftCol];//player middle left section tile(used as player  can grow and thus needs mor points)
		tileNum1 = mapLayout[(int)playerTopRow][(int)playerLeftCol];//player top left corner tile
		tileNum2 = mapLayout[(int)playerBotRow][(int)playerLeftCol];//player bottom left corner tile

		if(tileNum1!=0 || tileNum2!=0 || tileNum0!=0){//if a tile space isn't empty
			//Coin Collision
			if(mapLayout[(int)playerTopRow][(int)playerLeftCol]==16760832){//if the top left tile is a coin
				mapLayout[(int)playerTopRow][(int)playerLeftCol]=0;
				level.update(mapLayout);
				coins++;
				coinSound.play(coinPath, false);
			}
			if(mapLayout[(int)playerBotRow][(int)playerLeftCol]==16760832){//if the bottom left tile is a coin
				mapLayout[(int)playerBotRow][(int)playerLeftCol]=0;
				level.update(mapLayout);
				coins++;
				coinSound.play(coinPath, false);
			}
			//Mushroom Collision
			if(mapLayout[(int)playerTopRow][(int)playerLeftCol]==9837596){//if the top left tile is a mushroom
				mapLayout[(int)playerTopRow][(int)playerLeftCol]=0;
				if(playerPower!="fire"){//if you dont have a better power
					if(playerPower=="small"){//if you are small the player dimensions would be changed
						y-=32;//offset sprite by a tile as when the tile increases you would phase into the ground
					}
					playerPower="big";//give big power
				}
				level.update(mapLayout);
				power_upSound.play(power_upPath, false);
			}
			if(mapLayout[(int)playerBotRow][(int)playerLeftCol]==9837596){//if the bottom left tile is a mushroom
				mapLayout[(int)playerBotRow][(int)playerLeftCol]=0;
				if(playerPower=="small"){
					y-=32;
				}
				playerPower="big";
				level.update(mapLayout);
				power_upSound.play(power_upPath, false);
			}
			//Fire Flower Collision
			if(mapLayout[(int)playerTopRow][(int)playerLeftCol]==16750080){//if the top left tile is a fire flower
				mapLayout[(int)playerTopRow][(int)playerLeftCol]=0;
				if(playerPower=="small"){
					y-=32;
				}
				playerPower="fire";
				level.update(mapLayout);
				power_upSound.play(power_upPath, false);
			}
			if(mapLayout[(int)playerBotRow][(int)playerLeftCol]==16750080){//if the bottom left tile is a fire flower
				mapLayout[(int)playerBotRow][(int)playerLeftCol]=0;
				if(playerPower=="small"){
					y-=32;
				}
				playerPower="fire";
				level.update(mapLayout);
				power_upSound.play(power_upPath, false);
			}

			//Changes upon Collision
			leftCollision=true;//left collision is true
		}
		else{//if the tile spaces are empty
			leftCollision=false;//left collision is false
		}
		playerLeftCol=playerLeftX/32;//reset variable

		//Right Collision Checker//////////////////////////////////////////////////////////////////////////////
		playerRightCol = (playerRightX+playerSpeed)/32;//accomodating player speed to the right to determine the column to the right of the player
		tileNum0 = mapLayout[(int)playerTopRow+1][(int)playerRightCol];//player middle right section tile(used as player  can grow and thus needs mor points)
		tileNum1 = mapLayout[(int)playerTopRow][(int)playerRightCol];//player top right corner tile
		tileNum2 = mapLayout[(int)playerBotRow][(int)playerRightCol];//player bottom right corner tile

		if(tileNum1!=0 || tileNum2!=0 || tileNum0!=0){//if none of the tiles are empty spaces
			//Coin Collision
			if(mapLayout[(int)playerTopRow][(int)playerRightCol]==16760832){//checks if the top right tile is a coin
				mapLayout[(int)playerTopRow][(int)playerRightCol]=0;
				level.update(mapLayout);
				coins++;
				coinSound.play(coinPath, false);
			}
			if(mapLayout[(int)playerBotRow][(int)playerRightCol]==16760832){//checks if the bottom right tile is a coin
				mapLayout[(int)playerBotRow][(int)playerRightCol]=0;
				level.update(mapLayout);
				coins++;
				coinSound.play(coinPath, false);
			}
			//Mushroom Collision
			if(mapLayout[(int)playerTopRow][(int)playerRightCol]==9837596){//checks if the top right tile is a mushroom
				mapLayout[(int)playerTopRow][(int)playerRightCol]=0;
				if(playerPower=="small"){
					y-=32;
				}
				if(playerPower!="fire"){
					playerPower="big";
				}
				level.update(mapLayout);
				power_upSound.play(power_upPath, false);
			}
			if(mapLayout[(int)playerBotRow][(int)playerRightCol]==9837596){//checks if the bottom right tile is a mushroom
				mapLayout[(int)playerBotRow][(int)playerRightCol]=0;
				if(playerPower=="small"){
					y-=32;
				}
				playerPower="big";
				level.update(mapLayout);
				power_upSound.play(power_upPath, false);
			}
			//Fire Flower Collision
			if(mapLayout[(int)playerTopRow][(int)playerRightCol]==16750080){//checks if the top right tile is a fire flower
				mapLayout[(int)playerTopRow][(int)playerRightCol]=0;
				if(playerPower=="small"){
					y-=32;
				}
				playerPower="fire";
				level.update(mapLayout);
				power_upSound.play(power_upPath, false);
			}
			if(mapLayout[(int)playerBotRow][(int)playerRightCol]==16750080){//checks if the bottom right tile is a fire flower
				mapLayout[(int)playerBotRow][(int)playerRightCol]=0;
				if(playerPower=="small"){
					y-=32;
				}
				playerPower="fire";
				level.update(mapLayout);
				power_upSound.play(power_upPath, false);
			}
			//Flag Collision
			if(mapLayout[(int)playerTopRow][(int)playerRightCol]==20223 && mapLayout[(int)playerBotRow][(int)playerRightCol]==20223 && flagCollision==false ){//checks if the top right or bottom right corner are flag poles and you havent alr. collided with the flag
				flagCollision=true;//colliding with a flag-->prevents repeated entry into statement
				x+=16;//shifts player toward the pole so it looks like he is attached
				if(levelCount==1){//stops level 1 music if  its is playing
					music1.stop();
				}
				else{//stop level 2 music
					music2.stop();
				}
				flag_poleSound.play(flag_polePath, false);//flag sliding sound 
				stage_clearSound.play(stage_clearPath, false);//stage clear sound
				shots.clear();//clear all fireballs
			}

			//Changes upon Collision
			rightCollision=true;//right collision is true
		}
		else{//all tiles are empty spaces
			rightCollision=false;//right collision is false
			flagCollision=false;//flag collision reset
		}
		playerRightCol=playerRightX/32;//variable reset
	}
	public void timerUpdate(){//updates timers and acts on timer/delay conditions
			transitionElapsed = (System.nanoTime()-transitionTimer)/1000000;//amount of time passed after transition started
			if(transitionElapsed>transitionDelay){//if more time has elapsed than the specified cooldown value
				if(transition){//if transitioning and the timer is done
					start=true;//start/continue the game
				}
				transition=false;//no longer transitioning
				transitionTimer=System.nanoTime();//reset the timer
				if(lives==0 || levelCount>2){//if you win the game or get a game over
					reset();//reset the game
				}
				else{//transitioning into level
					if(levelCount==1){//play level 1 music
						music1.play(music1Path, true);
					}
					else if(levelCount==2){//play level 2 music
						music2.play(music2Path, true);
					}
				}

			}

			hurtElapsed = (System.nanoTime()-hurtTimer)/1000000;//amount of time passed after you became invincible
			if(hurtElapsed>hurtDelay){//if more time has elapsed than  the specified invincible/hurt duration
				invincible=false;//no longer invincible
				opacity=1f;//player transperancy is set to solid
			}
	}
	public void reset(){//game reset-->changes all variables to starting value
		x=100;
		y=700;
		shots.clear();
		goombas.clear();
		spriteCounter=0;
		spriteNum=1;
		jumpSpeed=10;
		coins=0;
		lives=3;
		levelCount=1;
	   	offset = 0;
	   	finalOffset=offset;
		direction="right";
		playerPower="small";
		flip=1;
		upCollision=false;
		downCollision=false;
		leftCollision=false;
		rightCollision=false;
		flagCollision=false;
		spacePressed=false;
		jump=false;
		start=false;
		transition=false;
		transitionTimer=System.nanoTime();
		transitionDelay=5000;
		level = new Level("maps/level1.txt", goombas, levelCount);
		mapLayout=level.getArray();
		music1.play(music1Path, true);
	}

	public void move(){//player movement controller
		if(flagCollision==false){//you can move if you are not colliding with the flag
			if(keys[KeyEvent.VK_RIGHT] && rightCollision==false){//if the right arrow is pressed and your right side is not colliding
				x+=playerSpeed;
				direction="right";
				rightPressed=true;
				leftCollision=false;
			}
			if(keys[KeyEvent.VK_LEFT] && leftCollision==false){//if the left arrow is pressed and your left side is not colliding
				x-=playerSpeed;
				direction="left";
				leftPressed=true;
				rightCollision=false;
			}
			if(keys[KeyEvent.VK_SPACE] && downCollision==true && jump==false && spacePressed==false){//if the space key is pressed and you are on the ground, not jumping, and not pressing space
				jumpSound.play(jumpPath, false);
				y-=15;//initial impulse of the ground
				spacePressed=true;
				direction="up";
				jump=true;
			}
			if(keys[KeyEvent.VK_UP] && shoot==false && playerPower=="fire"){//if the upkey is pressed and you have the fire flower
				shoot=true;
				shoot();
			}
		}
		if(downCollision==false){//if you are not on the ground
			y+=10;//y coord. increase acts as gravity
		}
		if(jump==true && upCollision==false){//if your are jumping and haven't hit your head on anything
			y-=jumpSpeed*2;//decrease y-coord by jumpSpeed*2
			jumpSpeed-=0.25;//decrease jumpSpeed
			if(jumpSpeed<0){//no downward acceleration-->don't let jumpSpeed go into the negatives
				jumpSpeed=0;
			}
		}
	}
	public void die(){//if the player dies...
		shots.clear();//clear the shots
		start=false;//stop game
		transition=true;//start the transition screen
		playerPower="small";//reset powers
		x=100;//reset x
		y=700;//reset y
		offset=0;//reset offset
		finalOffset=0;//reset offset
		lives--;//decrease life count
		if(lives>0){//if you have more lives to play
			if(levelCount==1){
				music1.stop();
			}
			else{
				music2.stop();
			}
			deathSound.play(deathPath, false);
		}
		else{//lives hit 0-->game over
			if(levelCount==1){
				music1.stop();
			}
			else{
				music2.stop();
			}
			game_overSound.play(game_overPath, false);
		}
	}
	public void invincible(){
		invincible=true;//become invincible
		opacity=0.3f;//lower player transperancy-->can visually see how long you are invincible
	}
	public void shoot(){
		if(shoot){//if you are shooting
			long elapsed = (System.nanoTime()-shootTimer)/1000000;//amount of time elapsed since last shot
			if(elapsed>shootDelay){//if you are trying to shoot and the cooldown time has passes
				shots.add(new Shot(x, y, finalOffset, flip, mapLayout));//create a new shot object and add to the Shot object array list
				shootTimer=System.nanoTime();//reset the timer
				fireballSound.play(fireballPath, false);//play shooting sound effect
			}
		}
	}

	public void playerImageValueUpdater(){//image helper variable updater
		if(flip==-1){
			flipOffset=32;//flip offset prevents visual issues when scaling image negatively when changing direction
		}
		else{
			flipOffset=0;
		}
		if(playerPower!="small"){//if you are not small-->your dimension will be (32*64)
			scale=2;//used to multiply image y-scale
		}
		else{
			scale=1;//no change to scale is made if you are small
		}
	}
	public void shotUpdate(){//shots array list updater
		for(int i=0;i<shots.size();i++){//cycling through all shot objects in the shots class
			boolean remove = shots.get(i).update(mapLayout, finalOffset);//updater that takes in the current mapLayout and offset for collision and drawing purposes-->returns boolean to whether it should be rempved
			if(remove){
				shots.remove(i);//remove the current shot
				i--;
			}
		}
	}
	public void goombaUpdate(){//goombas array list updater
		for(int i=0;i<goombas.size();i++){//cycles through goombas array list 
			boolean remove = goombas.get(i).update(mapLayout, finalOffset);//goomba updater-->same parameters as shots-->returns boolean on whether it should be removed
			if(remove){
				goombas.remove(i);
				i--;
				break;
			}
			Rectangle playerRect = new Rectangle((int)x, (int)y, 32, 32*scale);//rect crreated around player based on in-game coords, used to check collision with enemies
			if(goombas.get(i).getRect().intersects(playerRect) && invincible==false && goombas.get(i).isSquished()==false){//if the goombas are not already squished and the rectangle of the goomba and the enemy intersect
				if(downCollision==false){//if the player is not on the ground
					if(jumpSpeed<=5 || jumpSpeed==10){//if the jumpSpeed is 10 meaning he is falling without jumping or is at a certain point of his jump
						//goombas.remove(i);
						//i--;
						goombas.get(i).squish();//squish the goomba-->goes into squish state before being removed after a certtain period of time
						kickSound.play(kickPath, false);
						//Goomba bounce
						jumpSpeed=5;
						jumpSound.play(jumpPath, false);
						y-=15;
						direction="up";
						jump=true;
						break;
					}
					else{//the player is hit while off the ground-->decreasing power or killing
						if(playerPower=="fire"){
							playerPower="big";
							power_downSound.play(power_downPath, false);
						}
						else if(playerPower=="big"){
							playerPower="small";
							y+=32;
							power_downSound.play(power_downPath, false);
						}
						else{
							die();
						}
						invincible();//make invincible when hit
					}
				}
				else{//the player is hit on ground-->decreasing power or killing
					if(playerPower=="fire"){
						playerPower="big";
						power_downSound.play(power_downPath, false);
					}
					else if(playerPower=="big"){
						playerPower="small";
						y+=32;
						power_downSound.play(power_downPath, false);
					}
					else{
						die();
					}
					invincible();
				}
			}

			for(int j=0;j<shots.size();j++){//cycling through shots
				if(goombas.get(i).getRect().intersects(shots.get(j).getRect())){//checking rect intersection between goomba and fireball rect
					goombas.remove(i);//remove the gooba
					i--;
					shots.get(j).setHit();//put the fireball in hit state
					kickSound.play(kickPath, false);
					break;
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e){
		if(y>0 && y<mapLayout[0].length*32 && x>0 && x<mapLayout[0].length*32){//out of bounds prevention-->player is on the level map
			collisionChecker();
		}
		if(start){//if the game has started
			move();//move the player
			changePlayerImage();//change sprites
			shotUpdate();//update shots
			goombaUpdate();//update enemies
		}
		if(transition==false){
			transitionTimer=System.nanoTime();//reset the timer until transition is true
		}
		if(invincible==false){
			hurtTimer=System.nanoTime();//reset the timer until you are invincible
		}
		if(downCollision && flagCollision){//you have slid to the bottom of the flag pole
			x=100;//resetting variables
			y=700;
	    	offset = 0;
	    	finalOffset=offset;
			if(playerPower=="big" || playerPower=="fire"){//prevents starting in the ground
				y-=32;
			}
			goombas.clear();//clear enemies
			if(levelCount==1){
				levelCount=2;
			}
			else if(levelCount==2){
				levelCount=3;
			}
			level = new Level("maps/level2.txt", goombas, levelCount);//load new level from txt file, feed in enemies and the current level number
			mapLayout=level.getArray();
			start=false;
			transition=true;
		}
		if(y>=800){//if you fall out of the map
			die();
		}
		if(coins>=100){//if you have 100 coins-->gain a life
			lives++;
			coins=0;
			one_upSound.play(one_upPath, false);
		}
		timerUpdate();
		repaint();
	}

	@Override
	public void keyPressed(KeyEvent ke){
		int key = ke.getKeyCode();
		keys[key]=true;

		if(key==KeyEvent.VK_ENTER && start==false){//press enter at the beginning to begin game
			if(transition==false){
				start=true;
			}
		}
	}
	@Override
	public void keyReleased(KeyEvent ke){
		int key = ke.getKeyCode();
		keys[key]=false;
		if(key==KeyEvent.VK_RIGHT){
			rightPressed=false;
		}
		if(key==KeyEvent.VK_LEFT){
			leftPressed=false;
		}
		if(key==KeyEvent.VK_SPACE){
			spacePressed=false;
		}
		if(key==KeyEvent.VK_UP){
			shoot=false;
		}
	}
	@Override
	public void keyTyped(KeyEvent ke){}

	@Override
	public void paint(Graphics g){
		if(start==false){//if the game hasnt started
			if(transition){//if you are in a transition screen-->draw items below
				if(levelCount>2){
					g.setColor(new Color(75, 179, 41));
				}
				else if(lives<=0){
					g.setColor(new Color(196, 8, 8));
				}
				else{
					g.setColor(new Color(0, 0, 0));
				}
				g.fillRect(0, 0, getWidth(), getHeight());

				//Title
	    		g.drawImage(titleText, 70, 100 ,null);

		    	//Coin counter
				g.setFont(new Font("Calibri", Font.BOLD, 20));
				g.setColor(new Color(252, 255, 255));
				g.drawString(" x "+ coins, getWidth()-460, 400);
		    	g.drawImage(coin, getWidth()-510, 380, null);

				//Life counter
				g.drawString(" x "+ lives, getWidth()-460, 350);
		    	g.drawImage(lifeIcon, getWidth()-510, 330, null);

		    	//Level
				g.setFont(new Font("Calibri", Font.BOLD, 40));
				if(lives>0){
					if(levelCount>2){
						g.drawString("You Win!!", 390, 275);
					}
					else{
						g.drawString("Level: "+levelCount, 420, 275);
					}
				}
				else{
					g.drawString("Game Over", 390, 275);
				}

			}
			else{//you are at the start screen-->draw items below
				g.drawImage(titleScreen, 0, 0, null);
				g.setFont(new Font("Calibri", Font.PLAIN, 20));
				g.drawString("Press Enter To Start", 410, 550);
			}
		}
		else{//start is true
			int levelWidth=mapLayout[0].length;
			
			//level offsetter
			if(x>=500 && x<(levelWidth-14)*32){//camera limit at the edge of the level
	    		offset = 500 - (int)x;
	    		finalOffset=offset;
			}
			else{
				if(x<500){
					offset=0;
				}
				else{
					offset=finalOffset;
				}
			}
			
			//Draw the level
	    	g.drawImage(level.getBackground(), offset, 0,null);

	    	//Coin counter
			g.setFont(new Font("Calibri", Font.BOLD, 20));
			g.setColor(new Color(252, 255, 255));
			g.drawString(" x "+ coins, getWidth()-50, getHeight()-740);
	    	g.drawImage(coin, getWidth()-85, getHeight()-760, null);

			//Life counter
			g.drawString(" x "+ lives, getWidth()-150, getHeight()-740);
	    	g.drawImage(lifeIcon, getWidth()-185, getHeight()-760, null);

	    	BufferedImage playerImage =setPlayerImage();//setting the player sprite
			playerImageValueUpdater();
			
			//Changing player opacity
			//https://gamedev.stackexchange.com/questions/105519/java-game-how-to-change-opacity-of-an-image/105522
			Graphics2D g2d = (Graphics2D)g;
			AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity); 
			g2d.setComposite(ac);//changing opacity
			
			//Drawing Player
			if(x>500 && x<(levelWidth-15)*32){//incorporates offset and level movent-->set spot on screen
	    		g.drawImage(playerImage, 500+flipOffset-16,(int)y, flip*32, 32*scale, null);
			}
			else{
				if(x<500){
	    			g.drawImage(playerImage, (int)x+flipOffset-16,(int)y, flip*32, 32*scale, null);
				}
				else{
	    			g.drawImage(playerImage, ((int)x+flipOffset-16)+finalOffset,(int)y, flip*32, 32*scale, null);
				}
			}
				
			ac=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1f);
			g2d.setComposite(ac);//resetting opacity
		}
		if(start){//drawing enemies and shots if the game has started
			//Drawing fireballs
			for(int i=0;i<shots.size();i++){
				shots.get(i).draw(g);
			}
			//Drawing Goombas
			for(int i=0;i<goombas.size();i++){
				goombas.get(i).draw(g);
			}
		}
	}
}


package com.matthew.tranov.game2;

/**
 * Created by Matthew on 2/28/2017.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

public class GameSurfaceView extends SurfaceView implements Runnable {



    public boolean isRunning = false;

    private Thread gameThread;

    private SurfaceHolder holder;

    SharedPreferences sharedPref;

    private Paint rPaint = new Paint(),bPaint = new Paint(),wPaint = new Paint();

    private int screenWidth=Resources.getSystem().getDisplayMetrics().widthPixels;
    private int screenHeight=Resources.getSystem().getDisplayMetrics().heightPixels;

    int birdWidth=screenWidth/10;
    int birdHeight=screenWidth/10;
    int groundHeight=3*screenHeight/4;
    int score;
    int finalBird;

    int highScore;

    int touchX;
    int dx;
    int deadCounter =0;
    int fingerX;

    private int frameWidth = birdWidth;
    private int frameHeight = birdHeight;

    // How many frames are there on the sprite sheet?
    private int frameCount = 4;

    // What time was it when we last changed frames
    long frames = 0;

    // How long should each frame last
    private int frameLengthInMilliseconds = 1000;



    private boolean game =false,started = false,lost=false,titleScreen=true,paused = false;
            Boolean[] newBird={
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false
            };
    private Rect tryAgainButton = new Rect(screenWidth/4,groundHeight,3*screenWidth/4,groundHeight+screenHeight/6);

    Bitmap cloudA= BitmapFactory.decodeResource(getResources(), R.drawable.cloud_1);
    Bitmap cloudB=BitmapFactory.decodeResource(getResources(), R.drawable.cloud_2);
    Bitmap ground =getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ground), screenWidth, screenHeight/2);
    Bitmap snailo =getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.snailo), screenWidth/8, screenHeight/18);
    Bitmap title =getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.text2), screenWidth-50, screenHeight/12);
    Bitmap deadBird=getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bird_dead2), birdWidth, birdHeight/2);
    Bitmap deadVerticalBird=getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.dead_vertical_bird), birdWidth*3/4, birdHeight*3/4);
    Bitmap deadWeirdBird=getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.dead_weird_bird), birdWidth, birdHeight/2);
    Bitmap goldenEgg=getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.golden_egg), birdWidth, birdHeight);
    Bitmap deadGoldenEgg=getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.golden_egg_broken), birdWidth, birdHeight);
    Bitmap refresh=getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.refresh), screenWidth/3, screenWidth/3);
    Bitmap touch=getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.touch), screenWidth/6, screenWidth/6);
    Bitmap play=getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.play), screenWidth/3,screenWidth/3);
    Bitmap poof=getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.poof), birdWidth, birdHeight);
    Bitmap weirdBirdFlying =BitmapFactory.decodeResource(getResources(), R.drawable.weirdbirdflying);
    Bitmap downBirdFlying =BitmapFactory.decodeResource(getResources(), R.drawable.downbirdflying);
    Bitmap flyingBird =BitmapFactory.decodeResource(getResources(), R.drawable.birdflying);
    Bitmap back=getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.back), screenWidth/6,screenWidth/6);

    int buttonX=(screenWidth  - play.getWidth()) /2,buttonY=groundHeight-play.getHeight()/2;

    boolean titlebird=false;


    public static Random rand=new Random();
    private bird birds[];
    private cloud[] Cloud = new cloud[]{
        new cloud(cloudA.getWidth()),
                new cloud(cloudB.getWidth())
    };
    private snail Snailo= new snail(snailo.getWidth(),snailo.getHeight());
    private final static int MAX_FPS = 80; //desired fps
    private final static int FRAME_PERIOD = 1000 / MAX_FPS; // the frame period


    public GameSurfaceView(Context context) {
        super(context);
        sharedPref = getContext().getSharedPreferences("Userinfo", Context.MODE_PRIVATE);
        highScore  =sharedPref.getInt("savedHighScore",0);
        StartGame();
    }

    public void StartGame() {
        flyingBird = Bitmap.createScaledBitmap(flyingBird,
                frameWidth * frameCount,
                frameHeight*2,
                false);
        weirdBirdFlying = Bitmap.createScaledBitmap(weirdBirdFlying,
                frameWidth * frameCount,
                frameHeight*2,
                false);
        downBirdFlying = Bitmap.createScaledBitmap(downBirdFlying,
                frameWidth * frameCount,
                frameHeight,
                false);

        isRunning = true;
        score=0;
        Snailo.snailX=(screenWidth-snailo.getWidth())/2;
        if (titleScreen){
           Snailo.snailY= screenHeight/4-snailo.getHeight();
        }
        else{
            Snailo.snailY=groundHeight+screenHeight/32;
        }
        birds = new bird[]{
                new bird(birdHeight,birdWidth),
                new bird(birdHeight,birdWidth),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        };



        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                screenWidth = width;
                screenHeight = height;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });

       // patternSelect();


    }


     //Start or resume the game.

    public void resume() {
        if (game) {paused =true;}
        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }


     //Pause the game loop

    public void pause() {
        isRunning = false;
        boolean retry = true;
        while (retry) {
            try {
                gameThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // try again shutting down the thread
            }
        }
    }

//--------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void run() {

        while(isRunning) {
            // We need to make sure that the surface is ready
            if (! holder.getSurface().isValid()) {
                continue;
            }
            long started = System.currentTimeMillis();

            // update
            step();
            // draw
            Canvas canvas = holder.lockCanvas();
            if (canvas != null) {
                render(canvas);
                holder.unlockCanvasAndPost(canvas);
            }

            float deltaTime = (System.currentTimeMillis() - started);
            int sleepTime = (int) (FRAME_PERIOD - deltaTime);
            if (sleepTime > 0) {
                try {
                    gameThread.sleep(sleepTime);
                }
                catch (InterruptedException e) {
                }
            }
            while (sleepTime < 0) {
                step();
                sleepTime += FRAME_PERIOD;
            }

        }
    }


    protected void step() {
        frames++;
        for (int i = 0, length = Cloud.length; i < length; i++)
        {
            Cloud[i].moveCloud();
        }
        if((game&&started&&!paused)||titleScreen) {
            for (int i = 0, length = birds.length; i < length; i++) {
                //canvas.drawCircle(birds[i].X, birds[i].Y,50,mPaint);
                if (birds[i]!=null) {
                    if ((Snailo.body.intersect(birds[i].birdLocation) || (Snailo.shell.intersect(birds[i].birdLocation))) && !birds[i].dead&&birds[i].visible) {
                        //isRunning=false;
                        if(birds[i].AttackType==5)
                        {
                            birds[i].visible=false;
                            for (int i2 = 0; i2 < length; i2++){
                                if (birds[i2]!=null&&i2!=i) {
                                    birds[i2].visible=false;
                                    birds[i2].lastSeenX=birds[i2].X;
                                    birds[i2].lastSeenY=birds[i2].Y;
                                }
                            }
                            break;
                        }
                        else if(!titleScreen) {

                            game = false;
                            lost = true;
                            if (highScore < score) {
                                highScore = score;
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putInt("savedHighScore",highScore);
                                editor.commit();
                            }
                        }
                        else{
                            StartGame();
                            highScore=0;
                        }
                    }
                    birds[i].moveBird();

                    if (birds[i].dead) {
                        deadCounter++;
                        if (deadCounter == 20) {
                            deadCounter = 0;
                            birds[i].visible = false;
                            newBird[i]=true;
                            //if (i ==finalBird){
                             //   patternSelect();
                             //   }
                            }
                        }
                   if (newBird[i]) {
                        newBird[i] = false;
                        birds[i] = null;
                        birds[i] = new bird(birdHeight, birdWidth);

                   }
                }




            }

        }
    }

    protected void render(Canvas canvas) {
        canvas.drawColor(Color.parseColor("#5CC5F2"));
        int textSize=screenWidth/8;
        rPaint.setColor(Color.RED);
        bPaint.setColor(Color.BLUE);
        wPaint.setColor(Color.WHITE);
        Paint bkPaint = new Paint();
        Paint smallbkPaint = new Paint();
        bkPaint.setColor(Color.BLACK);
        bkPaint.setTextSize(textSize);
        bkPaint.setTextAlign(Paint.Align.CENTER);
        smallbkPaint.setColor(Color.BLACK);
        smallbkPaint.setTextSize(textSize/2);
        smallbkPaint.setTextAlign(Paint.Align.CENTER);

        canvas.drawBitmap(cloudA, Cloud[0].X,Cloud[0].Y, null);
        canvas.drawBitmap(cloudB, Cloud[1].X,Cloud[1].Y, null);
        canvas.drawBitmap(ground, 0,groundHeight, null);
        int centreX = (screenWidth  - title.getWidth()) /2;

        if (game&&started||titleScreen) {
            //canvas.drawRect(0, screenHeight / 2, screenWidth, screenHeight, wPaint);

            for (int i = 0, length = birds.length; i < length; i++) {

                //DRAWING SPRITES

                if (birds[i] != null&&birds[i].visible) {  //canvas.drawRect(birds[i].returnSquare(), rPaint);
                    if (birds[i].dead&&birds[i].AttackType==1)
                        canvas.drawBitmap(deadVerticalBird, birds[i].X, birds[i].Y+birdHeight/2, null);
                    else if(birds[i].dead&&birds[i].AttackType==2)
                        canvas.drawBitmap(deadBird, birds[i].X, birds[i].Y+birdHeight/2, null);
                    else if(birds[i].dead&&birds[i].AttackType==3)
                        canvas.drawBitmap(flip(deadBird), birds[i].X, birds[i].Y+birdHeight/2, null);
                    else if(birds[i].dead&&birds[i].AttackType==4)
                        canvas.drawBitmap(deadWeirdBird, birds[i].X, birds[i].Y+birdHeight/2, null);
                    else if(birds[i].dead&&birds[i].AttackType==5)
                        canvas.drawBitmap(deadGoldenEgg, birds[i].X, birds[i].Y+birdHeight/2, null);
                    else if(birds[i].AttackType==1)
                        canvas.drawBitmap(downBirdFlying,birds[i].frameToDraw,birds[i].birdLocation,null);
                    else if(birds[i].AttackType==2) {
                        canvas.drawBitmap(flyingBird,birds[i].frameToDraw,birds[i].birdLocation,null);
                    }
                    else if(birds[i].AttackType==3) {
                        canvas.drawBitmap(flyingBird,birds[i].frameToDraw,birds[i].birdLocation,null);
                    }
                    else if(birds[i].AttackType==4)
                        canvas.drawBitmap(weirdBirdFlying,birds[i].frameToDraw,birds[i].birdLocation,null);
                    else if(birds[i].AttackType==5)
                        canvas.drawBitmap(goldenEgg, birds[i].X, birds[i].Y, null);
                }
                else if(birds[i] != null&&!birds[i].visible)
                    canvas.drawBitmap(poof, birds[i].lastSeenX, birds[i].lastSeenY, null);
            }
            //MAIN GAME CODE
            if(!titleScreen) {
                if (Snailo.right)
                    canvas.drawBitmap(snailo, Snailo.snailX, Snailo.snailY, null);
                else {
                    canvas.drawBitmap(flip(snailo), Snailo.snailX, Snailo.snailY, null);
                }
                //((textPaint.descent() + textPaint.ascent()) / 2) is the distance from the baseline to the center.

                canvas.drawText("" + score, screenWidth / 2, groundHeight + textSize, bkPaint);
                if (paused ) {
                    canvas.drawText("Paused", screenWidth / 2, screenHeight / 2, bkPaint);
                }
            }

        }
        //TUTORIAL CODE
        if (game&&!started&&highScore==0){
            if (Snailo.right) {
                Snailo.snailX+=3;
                if (Snailo.snailX > screenWidth - snailo.getWidth()*3) {
                    Snailo.right=false;
                }
                canvas.drawBitmap(snailo, Snailo.snailX,Snailo.snailY, null);
            }
            else{
                Snailo.snailX-=3;
                if (Snailo.snailX < snailo.getWidth()*2) {
                    Snailo.right=true;
                }
                canvas.drawBitmap(flip(snailo), Snailo.snailX,Snailo.snailY, null);
            }
            canvas.drawBitmap(touch, Snailo.snailX-(touch.getWidth()/2),buttonY+touch.getHeight()*2, null);
        }
        if (game&&!started&&highScore!=0){
            canvas.drawBitmap(snailo, Snailo.snailX,Snailo.snailY, null);
        }

        if (lost){
            canvas.drawText("Game Over ",screenWidth/2,screenHeight/4, bkPaint);
            canvas.drawText("High Score: "+highScore,screenWidth/2, groundHeight+textSize*2,smallbkPaint);
            canvas.drawText(""+score,screenWidth/2,groundHeight+textSize, bkPaint);
            canvas.drawBitmap(refresh, buttonX,buttonY-refresh.getHeight(), null);
        }
        //TITLE SCREEN CODE
        if (titleScreen){
            if (!titlebird){
                patternSelect();
                titlebird=true;
            }

            if (Snailo.right) {
                Snailo.snailX+=3;
                if (Snailo.snailX > screenWidth/4+title.getWidth() - snailo.getWidth()*3) {
                    Snailo.right=false;
                }
                canvas.drawBitmap(snailo, Snailo.snailX,Snailo.snailY, null);
            }
            else{
                Snailo.snailX-=3;
                if (Snailo.snailX < screenWidth/4-snailo.getWidth()) {
                    Snailo.right=true;
                }
                canvas.drawBitmap(flip(snailo), Snailo.snailX,Snailo.snailY, null);
            }
            //canvas.drawBitmap(snailo, centreX+snailo.getWidth(),screenHeight/4-snailo.getHeight(), null);
            canvas.drawBitmap(title, centreX,screenHeight/4, null);
            canvas.drawBitmap(play, buttonX,buttonY-play.getHeight(), null);
        }
        //Bitmap b=BitmapFactory.decodeResource(getResources(), R.drawable.snailo);
       // canvas.drawBitmap(getResizedBitmap(b, 100, 100), X,Y, null);
       // canvas.drawCircle(X,Y,50,mPaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();



        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startTouch(x, y);


                break;
            case MotionEvent.ACTION_MOVE:
                moveTouch(x, y);

                break;
            case MotionEvent.ACTION_UP:
                upTouch(x, y);
                //isRunning=true;
                break;
        }
        return true;
    }
    private void startTouch(float x, float y) {
        int touchX = Math.round(x);

        if (game&&!started){
            started=true;
        }
        if (lost&&x>buttonX&&x<buttonX+refresh.getWidth()&&y>buttonY-refresh.getHeight()&&y<buttonY){
            lost =false;
            game=true;
            started=false;
            StartGame();
        }
        if (paused){
            paused=false;
        }
        // if (y>screenHeight/2)
        // touch.set(X-40,Y-40-screenHeight/2,X+40,Y+40-screenHeight/2);

        if (!isRunning) {
            resume();
        }

        // X= screenWidth-50;

    }


    private void moveTouch(float x, float y) {
        if (game&&started) {
             dx = Math.round(x) - touchX;
            touchX+=dx;

            if ( Snailo.snailX + dx>0&& Snailo.snailX+snailo.getWidth()+ dx<screenWidth&&Math.abs(dx)<screenWidth/6)
            //touchX += dx;
            Snailo.moveSnail(Snailo.snailX+dx);
            if (dx>0)
                Snailo.right=true;
            else if (dx<0)
                Snailo.right=false;
        }
    }

    private void upTouch(float x, float y) {
        if (titleScreen&&x>buttonX&&x<buttonX+play.getWidth()&&y>buttonY-play.getHeight()&&y<buttonY) {
            titleScreen=false;
            game=true;
        }
    }

    public void patternSelect (){
        int birdXDistance;
        int birdYDistance;
        int speed =10;
        /*int patternType=rand.nextInt(100)+1;
        if(patternType<=6&&score>30)
            patternType = 5;
        else if(patternType<=18&&score>20)
            patternType = 4;
        else if(patternType<=30&&score>10)
            patternType = 3;
        else if(patternType<=42&&score>10)
            patternType = 2;
        else*/
            int patternType = 1;

        //x shape
        if (patternType==1){
          // (screenWidth-birdWidth)/2
          //  birds[0] = new bird (birdHeight, birdWidth, speed,true, 1,screenWidth-birdWidth*4/3,-birdWidth*4/3);
         //   birds[1] = new bird (birdHeight, birdWidth, speed,true, 1,birdWidth*1/3,-birdWidth*4/3);
          //  birds[2] = new bird (birdHeight, birdWidth, speed,true, 1,(screenWidth-birdWidth)/2,-birdWidth*10);
          //  birds[3] = new bird (birdHeight, birdWidth, speed,true, 1,screenWidth-birdWidth*4/3,-birdWidth*20);
          //  birds[4] = new bird (birdHeight, birdWidth, speed,true, 1,birdWidth*1/3,-birdWidth*20);
            birds[0] = new bird (birdHeight, birdWidth, speed,true, 1,Snailo.snailX+birdWidth/2,-birdWidth*4/3);
            finalBird=0;
        }
        if (patternType==2){

        }
        if (patternType==3){

        }
        if (patternType==4){

        }
        if (patternType==5){

        }

    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    public Bitmap flip(Bitmap d)
    {
        Matrix m = new Matrix();
        m.preScale(-1, 1);

        return Bitmap.createBitmap(d, 0, 0, d.getWidth(), d.getHeight(), m, false);
    }
//----------------------------------------------------------------------------------------------------------------------------------------------------------------------
    class bird {
        int X,Y,speed=3,size,birdWidth,birdHeight,AttackType, intialX,lastSeenX,lastSeenY,birdFrame=0,birdFrameCounter;
        boolean left=true,dead =false, visible ;
        Rect birdLocation;
        Rect frameToDraw;

        public bird(int ImageHeight, int ImageWidth)
        {
            frameToDraw = new Rect(0, 0, frameWidth, frameHeight);
            birdFrameCounter=0;
            birdHeight=ImageHeight;
            birdWidth=ImageWidth;
            visible =true;

            AttackType=rand.nextInt(100)+1;
            if(AttackType<=6&&score>30)
                AttackType = 5;
            else if(AttackType<=18&&score>20)
                AttackType = 4;
            else if(AttackType<=30&&score>10)
                AttackType = 3;
            else if(AttackType<=42&&score>10)
                AttackType = 2;
            else
                AttackType = 1;

            if (AttackType==1){
                frameToDraw = new Rect(0, 0, frameWidth, frameHeight);
                speed = speed*2;
                X=rand.nextInt(screenWidth-birdWidth);
                Y=0-birdHeight;
            }
            if (AttackType==2){
                frameToDraw = new Rect(0, 0, frameWidth, frameHeight);
                X=0-birdWidth;
                Y=rand.nextInt(screenHeight/3);
                left=false;
            }
            if (AttackType==3){
                frameToDraw = new Rect(0, frameHeight, frameWidth, frameHeight*2);
                X=screenWidth+birdWidth;
                Y=rand.nextInt(screenHeight/3);
                left=true;
            }
            if (AttackType==4){
                X=rand.nextInt(2*screenWidth/3)+screenWidth/6;
                intialX = X;
                Y=0-birdHeight+rand.nextInt(screenWidth/6);
                left=false;
            }
            if (AttackType==5){
                X=rand.nextInt(screenWidth-birdWidth);
                Y=0-birdHeight;
            }
            birdLocation=new Rect(X,Y,X+birdWidth,Y+birdHeight);
        }

    public bird(int ImageHeight, int ImageWidth,int speed,boolean left,int attackType,int X,int Y)
    {
        frameToDraw = new Rect(0, 0, frameWidth, frameHeight);
        birdFrameCounter=0;
        birdHeight=ImageHeight;
        birdWidth=ImageWidth;
        this.speed = speed;
        this.left = left;
        AttackType = attackType;
        visible =true;
        this.X =X;
        this.Y =Y;
        birdLocation=new Rect(X,Y,X+birdWidth,Y+birdHeight);
    }


        public void moveBird(){
        //control animation
            if (!dead) {
                //determine movement type
                if (AttackType == 1) {
                    moveY();
                } else if (AttackType == 2) {
                    moveX();
                    moveY();
                } else if (AttackType == 3) {
                    moveX();
                    moveY();
                }
                else if (AttackType == 4) {
                    weirdMoveX();
                    moveY();
                }
                else if (AttackType == 5) {
                    moveY();
                }
                birdLocation=new Rect(X,Y,X+birdWidth,Y+birdHeight);
            }
        }


        public void moveX()
        {
            if (X>screenWidth-50)
                left=true;
            if (X<50)
                left=false;
            if (left)
                X = X-speed ;
            else
                X += speed ;


        }

        public void weirdMoveX()
         {

             if(left) {
                 X = (int) Math.round(Math.sin((Y*3.14)/180)*screenWidth/6)+intialX;

                 if (Math.round(Math.sin((Y * 3.14) / 180)) == 1 && frameToDraw.top == 0) {
                     frameToDraw.top = frameHeight;
                     frameToDraw.bottom = frameHeight * 2;
                 } else if (Math.round(Math.sin((Y * 3.14) / 180)) == -1) {
                     frameToDraw.top = 0;
                     frameToDraw.bottom = frameHeight;
                 }
             }
             else{
                 X =  intialX-(int)Math.round(Math.sin((Y*3.14)/180)*screenWidth/6);

                 if (Math.round(Math.sin((Y * 3.14) / 180)) == -1 && frameToDraw.top == 0) {
                     frameToDraw.top = frameHeight;
                     frameToDraw.bottom = frameHeight * 2;
                 } else if (Math.round(Math.sin((Y * 3.14) / 180)) == 1) {
                     frameToDraw.top = 0;
                     frameToDraw.bottom = frameHeight;
                 }
             }

         }

    public void moveY() {

        birdFrameCounter++;
        if(birdFrameCounter %10==0&&AttackType!=5) {
        birdFrame++;
            if (birdFrame == 4) {
                birdFrame = 0;
            }
            frameToDraw.left = birdFrame * frameWidth;
            frameToDraw.right = frameToDraw.left + frameWidth;

        }

        if (Y + speed > groundHeight - 50||(titleScreen&&Y + speed > screenHeight/4 - 50)) {
            score++;
            dead = true;
            if (score ==10||score ==25||score ==45||score ==70||score==100||score==135||score==175||score==220) {
                for (int i = 0; i < birds.length; i++) {
                    if (birds[i] == null) {
                        birds[i] = new bird(birdHeight, birdWidth);
                        break;
                    }
                }
            }
        }
        else{
            Y += speed;
        }
    }

    }

    class cloud
    {
        int X=rand.nextInt(screenWidth);
        int Y=rand.nextInt(screenHeight/3)+1;
        int CloudImageWidth;

        public cloud(int ImageWidth ) {
            CloudImageWidth= ImageWidth;
        }

        public void moveCloud()
        {
            if(X<screenWidth&&frames%5==0)
            X++;
            else if (frames%5==0){
                X=-rand.nextInt(50)-CloudImageWidth;
                Y=rand.nextInt(screenHeight/6)+1;
            }
        }
    }
    class snail
    {

        int snailHeight;
        int snailWidth;
        int snailX;
        int snailY;
        boolean right = true;
        Rect body;
        Rect shell;


        public snail(int ImageWidth, int ImageHeight) {
            snailHeight= ImageHeight;
            snailWidth=ImageWidth;
            body = new Rect(snailX+0, snailY+3*snailHeight/5, snailX+snailWidth, snailY+snailHeight);
            shell = new Rect(snailX+snailWidth/10 ,snailY, snailX+3*snailWidth/5 ,snailY+snailHeight);
            snailY-=ImageHeight;

        }

        public void moveSnail(int newSnailX)
        {
            snailX = newSnailX;
            if (right)
            {
            body.set(snailX+0, snailY+3*snailHeight/5, snailX+snailWidth, groundHeight+snailHeight);
            shell.set(snailX+snailWidth/10 ,snailY, snailX+3*snailWidth/5 ,groundHeight+snailHeight);
            }
            else
            {
                body.set(snailX+0, snailY+3*snailHeight/5, snailX+snailWidth, groundHeight+snailHeight);
                shell.set(snailX+2*snailWidth/5,snailY, snailX+ 9*snailWidth/10,groundHeight+snailHeight);
            }
        }
    }
}
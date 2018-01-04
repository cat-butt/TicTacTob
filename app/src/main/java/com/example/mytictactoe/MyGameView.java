package com.example.mytictactoe;

//import com.example.android.tictactoe.library.R;



import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class MyGameView extends View {

	public static final long FPS_MS = 1000;
	private static final int MARGIN = 10;
	private static final int MSG_COMPUTER_TURN = 1;
	private static final int MSG_BLINK_VIEW = 2;
	private static final String TAG = "MyGameView";
	
	public enum State { EMPTY, CROSS, CIRCLE};
	public enum Player { HUMAN, COMPUTER };
	public enum Outcome { WIN, DRAW, UNDECIDED };
	
	private int mSxy;

	private int rowLines[] = new int[4];
	private int colLines[] = new int[4];
	
	private final Rect mSrcRect = new Rect();
    private final Rect mDstRect = new Rect();
	
	private Bitmap mBmpPlayerX;
    private Bitmap mBmpPlayerO;
    private Drawable mDrawableBg;
    
    private Paint mBlackLine;
    private Paint mRedLine;
    private Paint mBmpPaint;
    
    private boolean  mGameIsInProgress = false;
    private boolean  mFirstDraw = false;
    private GameGrid gameGrid;
    
    private Handler mHandler = new Handler(this.new MyHandler());
    
    private TextView mTextView;
    
	public MyGameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mDrawableBg = getResources().getDrawable(R.drawable.lib_bg);
		//setBackgroundDrawable(mDrawableBg);
		setBackground(mDrawableBg);
		
		mBmpPlayerX = getResBitmap(R.drawable.lib_cross);
        mBmpPlayerO = getResBitmap(R.drawable.lib_circle);
        
        if (mBmpPlayerX != null) {
            mSrcRect.set(0, 0, mBmpPlayerX.getWidth() -1, mBmpPlayerX.getHeight() - 1);
            mDstRect.set(0, 0, mBmpPlayerX.getWidth() -1, mBmpPlayerX.getHeight() - 1);
        }
        
        mBlackLine = new Paint();
        mBlackLine.setColor(Color.WHITE);
        mBlackLine.setStrokeWidth(10);
        mBlackLine.setStyle(Style.STROKE);
        
        mRedLine = new Paint();
        mRedLine.setColor(Color.RED);
        mRedLine.setStrokeWidth(20);
        mRedLine.setStyle(Style.STROKE);
        
        mBmpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        gameGrid = new GameGrid(Player.HUMAN);
        
	}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Keep the view squared
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        int d = w == 0 ? h : h == 0 ? w : w < h ? w : h;
        Log.v(TAG + "::onMeasure", "  w:" + w + "   h:" + h + "   d:" + d);
        setMeasuredDimension(d, d);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        int sx = (w - 2 * MARGIN) / 3;
        int sy = (h - 2 * MARGIN) / 3;

        int size = sx < sy ? sx : sy;

        mSxy = size;
        int mOffetX = (w - 3 * size) / 2;
        int mOffetY = (h - 3 * size) / 2;

        Log.v(TAG, "mOffetX: " + mOffetX + "      mOffetY: " + mOffetY);
        
        colLines[0] = 0;
        colLines[1] = mOffetY + size;
        colLines[2] = mOffetY + 2*size;
        colLines[3] = mOffetY + 3*size;
        rowLines[0] = 0;
        rowLines[1] = mOffetX + size;
        rowLines[2] = mOffetX + 2*size;
        rowLines[3] = mOffetX + 3*size;
        
        mDstRect.set(rowLines[0], colLines[0], rowLines[1], colLines[1]);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        for (int i = 1;  i < 3; i++ ) {
            canvas.drawLine(colLines[0]   , rowLines[i] , colLines[3] , rowLines[i], mBlackLine);
            canvas.drawLine(colLines[i]   , rowLines[0] , colLines[i] , rowLines[3], mBlackLine);
        }
        
        for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				mDstRect.offsetTo(colLines[j], rowLines[i]);
				if( gameGrid.getState(i, j) == State.CROSS ) {
					if( false ) {
						Matrix matrix = new Matrix();
						matrix.setRotate(2, mBmpPlayerX.getWidth()/2, mBmpPlayerX.getHeight()/2);
						canvas.drawBitmap(mBmpPlayerX, matrix, mBmpPaint) ;  //    mBmpPlayerX, null, mDstRect, mBmpPaint);
						mFirstDraw = false;
					}
					else{
						canvas.drawBitmap(mBmpPlayerX, null, mDstRect, mBmpPaint);
					}
				}
				else if (gameGrid.getState(i, j)  == State.CIRCLE) {
		            canvas.drawBitmap(mBmpPlayerO, null, mDstRect, mBmpPaint);
				}
			}
        }
        
        if(gameGrid.mWinRow >= 0) {
        	mGameIsInProgress = false;
        	canvas.drawLine(colLines[0]   , rowLines[gameGrid.mWinRow] + mSxy/2 , colLines[3] , rowLines[gameGrid.mWinRow] + mSxy/2, mRedLine);
        }
        else if(gameGrid.mWinCol >= 0) {
        	mGameIsInProgress = false;
        	canvas.drawLine(colLines[gameGrid.mWinCol] + mSxy/2  , rowLines[0]  , colLines[gameGrid.mWinCol] + mSxy/2, rowLines[3] , mRedLine);
        }
        else if(gameGrid.mWinDiag == 0) {
        	mGameIsInProgress = false;
        	canvas.drawLine(colLines[0] + mSxy/4  , rowLines[0] + mSxy/4  , colLines[3] - mSxy/4, rowLines[3]  - mSxy/4, mRedLine);
        }
        else if(gameGrid.mWinDiag == 1) {
        	mGameIsInProgress = false;
        	canvas.drawLine(colLines[3] - mSxy/4  , rowLines[0] + mSxy/4  , colLines[0] + mSxy/4, rowLines[3]  - mSxy/4, mRedLine);
        }
    }
	
    private Bitmap getResBitmap(int bmpResId) {
        Options opts = new Options();
        opts.inDither = false;
        

        Resources res = getResources();
        Bitmap bmp = BitmapFactory.decodeResource(res, bmpResId, opts);

        if (bmp == null && isInEditMode()) {
            // BitmapFactory.decodeResource doesn't work from the rendering
            // library in Eclipse's Graphical Layout Editor. Use this workaround instead.

        	Log.v(TAG, "BitmapFactory.decodeResource() failed");
            Drawable d = res.getDrawable(bmpResId);
            int w = d.getIntrinsicWidth();
            int h = d.getIntrinsicHeight();
            bmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
            Canvas c = new Canvas(bmp);
            d.setBounds(0, 0, w - 1, h - 1);
            d.draw(c);
        }

        return bmp;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	
    	if( !mGameIsInProgress )
    		return false;
    	
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            return true;

        } else if (action == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            int y = (int) event.getY();

			for (int i = 0; i < 3; i++) {
				if (y > rowLines[i] && y < rowLines[i + 1]) {
					for (int j = 0; j < 3; j++) {
						if( x > colLines[j] && x < colLines[j+1]) {
							if( gameGrid.getState(i, j) == State.EMPTY) {
								gameGrid.setState(i,j);
								invalidate();
								if( gameGrid.isWin() == Outcome.WIN) {
									Log.v(TAG, "Human wins!!!");
									mTextView.setText("You win HUMAN.");
									return true;
								}
								mHandler.sendEmptyMessageDelayed(MSG_COMPUTER_TURN, FPS_MS);
//								gameGrid.computerPlay();
//								invalidate();
//								if( gameGrid.isWin() == GameGrid.Outcome.WIN ) {
//									Log.v(TAG, "Computer wins!!!");
//									return true;
//								}
							}
						}
					}
				}
			}
            invalidate();
            return true;
        }

        return false;
    }
    
    public void startGame(Player player, TextView textView) {
    	this.mTextView = textView;
    	mTextView.setText(player.toString());
    	gameGrid = new GameGrid(player);
    	mGameIsInProgress = true;
    	invalidate();
    }
    
    private class MyHandler implements Callback {
        public boolean handleMessage(Message msg) {
            Log.v(TAG, msg.toString());
            if( msg.what == MSG_COMPUTER_TURN ) {
            	Log.v(TAG, "COMPUTER TURN!!!");
            	gameGrid.computerPlay();
            	mFirstDraw = true;
				invalidate();
				if( gameGrid.isWin() == Outcome.WIN ) {
					Log.v(TAG, "Computer wins!!!");
					mTextView.setText("I crushed you!!!!");
					return true;
				}
            }
            else if ( msg.what == MSG_BLINK_VIEW ) {
            	
            }
            return false;
        }
    }
    
    private class GameGrid {

    	private final String TAG = "GameGrid";
    	
//    	public enum State { EMPTY, CROSS, CIRCLE};
//    	public enum Player { HUMAN, COMPUTER };
//    	public enum Outcome { WIN, DRAW, UNDECIDED };
    	
    	private State[][] gameState = new State[3][3];	
    	private State mCurrentState = State.CROSS;
    	
    	private Player mCurrentPlayer = Player.HUMAN;
    	private int mNumPlays = 0;
    	
    	private State mHumanPlayer;
    	private State mComputerPlayer;
    	
    	private int mHumanRowCount[] = new int[3];
    	private int mHumanColCount[] = new int[3];
    	private int mHumanDiag1Count = 0;
    	private int mHumanDiag2Count = 0;
    	private int mCompRowCount[] = new int[3];
    	private int mCompColCount[] = new int[3];
    	private int mCompDiag1Count = 0;
    	private int mCompDiag2Count = 0;
    	
    	
    	public int mWinCol = -1;
    	public int mWinRow = -1;
    	public int mWinDiag = -1;
    	
    	public GameGrid(Player player) {
    		for(int i = 0; i < 3; i++) {
    			mHumanRowCount[i] = 0;
    			mHumanColCount[i] = 0;
    			mCompRowCount[i] = 0;
    			mCompColCount[i] = 0;
    			for(int j = 0; j < 3; j++)
    				gameState[i][j] = State.EMPTY;
    		}
    		mCurrentPlayer = player;
    		if( mCurrentPlayer == Player.COMPUTER ) {
    			computerPlay();
    			mHumanPlayer = State.CIRCLE;
    			mComputerPlayer = State.CROSS;
    		}
    		else {
    			mHumanPlayer = State.CROSS;
    			mComputerPlayer = State.CIRCLE;
    		}
    	}
    	
    	public State getState(int i, int j)
    	{
    		return gameState[i][j];
    	}
    	
    	public boolean setState(int i, int j)
    	{
    		if( gameState[i][j] == State.EMPTY ) {
    			gameState[i][j] = mCurrentState;
    			if(mCurrentPlayer == Player.COMPUTER ) {
    				mCompRowCount[i]++;
    				mCompColCount[j]++;
    				if(i==j)
    					mCompDiag1Count++;
    				if( i+j == 2)
    					mCompDiag2Count++;
    			}
    			else {
    				mHumanRowCount[i]++;
    				mHumanColCount[j]++;
    				if(i==j)
    					mHumanDiag1Count++;
    				if( i+j == 2)
    					mHumanDiag2Count++;
    			}
    			mCurrentState = mCurrentState == State.CROSS ? State.CIRCLE : State.CROSS; 
    			mCurrentPlayer = mCurrentPlayer == Player.COMPUTER ? Player.HUMAN : Player.COMPUTER;
    			//Log.v(TAG, "mHumanDiag2Count: " + mHumanDiag2Count);
    			//Log.v(TAG, "mRowCount[0]: " + mRowCount[0] + "       mRowCount[1]: " + mRowCount[1] +"       mRowCount[2]: " + mRowCount[2]);
    			//Log.v(TAG, "mColCount[0]: " + mColCount[0] + "       mColCount[1]: " + mColCount[1] +"       mColCount[2]: " + mColCount[2]);
    			mNumPlays++;
    			if( mNumPlays == 9)
    			{
    				MyGameView.this.mHandler.sendEmptyMessageDelayed(MSG_BLINK_VIEW, FPS_MS);
    			}
    			return true;
    		}
    		else 
    			return false;
    	}
    	
		public void computerPlay() {
			
			if (mNumPlays == 9)
				return;
			else if (mNumPlays <= 1) {
				if (gameState[1][1] == State.EMPTY) {
					setState(1, 1);
				} else {
					if (gameState[2][2] == State.EMPTY) {
						Random rnd = new Random();
						int ii = rnd.nextInt(2) * 2;
						int jj = rnd.nextInt(2) * 2;
						setState(ii, jj);
					}
				}
			}
			else if( mNumPlays <=1  ) { //if (numPlays <= 1) {
				Log.v(TAG, "mCurrentPlayer: " + mCurrentPlayer);
				Random rnd = new Random();
				while (true) {
					int ii = rnd.nextInt(3);
					int jj = rnd.nextInt(3);
					if (gameState[ii][jj] == State.EMPTY) {
						setState(ii, jj);
						return;
					}
				}
			}
			else {
				//Check for winning row
				for(int i = 0; i < 3; i++) {
					if( mCompRowCount[i] == 2 && mHumanRowCount[i] == 0) {
						for(int j = 0; j < 3; j++) 
							if( gameState[i][j] == State.EMPTY) {
								setState(i,j);
								return;
							}
					}
						
				}
				//Check for winning col
				for(int i = 0; i < 3; i++) {
					if( mCompColCount[i] == 2 && mHumanColCount[i] == 0) {
						for(int j = 0; j < 3; j++) 
							if( gameState[j][i] == State.EMPTY) {
								setState(j,i);
								return;
							}
					}
						
				}
				//Check for a winning diagonal
				if( mCompDiag1Count == 2 && mHumanDiag1Count == 0) {
					for(int j = 0; j < 3; j++) 
						if( gameState[j][j] == State.EMPTY) {
							setState(j,j);
							return;
						}
				}
				if( mCompDiag2Count == 2 && mHumanDiag2Count == 0) {
					for(int j = 0; j < 3; j++) 
						if( gameState[j][2-j] == State.EMPTY) {
							setState(j,2-j);
							return;
						}
				}
				
				//Check for blocking row
				for(int i = 0; i < 3; i++) {
					if( mCompRowCount[i] == 0 && mHumanRowCount[i] == 2) {
						for(int j = 0; j < 3; j++) 
							if( gameState[i][j] == State.EMPTY) {
								setState(i,j);
								return;
							}
					}
						
				}
				//Check for blocking col
				for(int i = 0; i < 3; i++) {
					if( mCompColCount[i] == 0 && mHumanColCount[i] == 2) {
						for(int j = 0; j < 3; j++) 
							if( gameState[j][i] == State.EMPTY) {
								setState(j,i);
								return;
							}
					}
						
				}
				//Check for a blocking diagonal
				if( mCompDiag1Count == 0 && mHumanDiag1Count == 2) {
					for(int j = 0; j < 3; j++) 
						if( gameState[j][j] == State.EMPTY) {
							setState(j,j);
							return;
						}
				}
				if( mCompDiag2Count == 0 && mHumanDiag2Count == 2) {
					for(int j = 0; j < 3; j++) 
						if( gameState[j][2-j] == State.EMPTY) {
							setState(j,2-j);
							return;
						}
				}
				Random rnd = new Random();
				int nCount = 0;
				while (nCount++ < 100) {
					int ii = rnd.nextInt(3);
					int jj = rnd.nextInt(3);
					if (gameState[ii][jj] == State.EMPTY) {
						setState(ii, jj);
						return;
					}
				}
				Log.e(TAG,"Ooooppps!!!");
				
			}
//			else {
//				for(int i = 0; i < 3; i++) {
//					if(gameState[0][i] == State.EMPTY)
//				}
//			}
		}

		public Outcome isWin() {
			// check rows
			for (int i = 0; i < 3; i++) {
				if (gameState[i][0] != State.EMPTY
						&& gameState[i][0] == gameState[i][1]
						&& gameState[i][0] == gameState[i][2]) {
					mWinRow = i;
					return Outcome.WIN;
				}
			}
			// check colums
			for (int i = 0; i < 3; i++) {
				if (gameState[0][i] != State.EMPTY
						&& gameState[0][i] == gameState[1][i]
						&& gameState[0][i] == gameState[2][i]) {
					mWinCol = i;
					return Outcome.WIN;
				}
			}
			// check diagonals
			if (gameState[0][0] != State.EMPTY
					&& gameState[0][0] == gameState[1][1]
					&& gameState[0][0] == gameState[2][2]) {
				mWinDiag = 0;
				return Outcome.WIN;
			}
			if (gameState[0][2] != State.EMPTY
					&& gameState[0][2] == gameState[1][1]
					&& gameState[0][2] == gameState[2][0]) {
				mWinDiag = 1;
				return Outcome.WIN;
			}
			if( mNumPlays >= 9 )
				return Outcome.DRAW;
			
			return Outcome.UNDECIDED;
		}

    }
    
}

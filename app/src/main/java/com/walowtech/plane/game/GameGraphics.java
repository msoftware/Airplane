package com.walowtech.plane.game;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.shapes.OvalShape;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import com.google.android.gms.games.Game;
import com.walowtech.plane.collision.CollisionDetector;
import com.walowtech.plane.collision.CollisionType;
import com.walowtech.plane.data.GameComponents;
import com.walowtech.plane.data.TailCurveType;
import com.walowtech.plane.data.TailDataPoint;
import com.walowtech.plane.player.Plane;
import com.walowtech.plane.player.Player;
import com.walowtech.plane.player.PlayerManager;
import com.walowtech.plane.util.ConversionUtils;

import java.util.ArrayList;

/**
 * Is the actual canvas view that the game gets
 * drawn onto.
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2018-08-09
 */
public class GameGraphics extends SurfaceView implements GameComponent{

    Paint mPaint;
    ConversionUtils convert;
    float dp;
    private boolean mHasBeenDrawn;
    private boolean mInitialized = false;
    private int mScreenWidth;
    private int mScreenHeight;
    private Bitmap mBackground;
    private Activity mActivity;
    private GameLoop mGameLoop;
    private Runnable mInvaidate = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };

    public GameGraphics(Context context, GameLoop pGameLoop, Activity pActivity) {
        super(context);
        convert = new ConversionUtils(context);
        dp = convert.dpToPx(1);
        setWillNotDraw(false);
        mGameLoop = pGameLoop;
        mActivity = pActivity;

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        mBackground = BackgroundGenerator.GenerateBackground((int)PlayerManager.GAME_BOUNDS.right, (int)PlayerManager.GAME_BOUNDS.bottom);
        mHasBeenDrawn = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mInitialized) {
            Plane p = mGameLoop.getCore().getPlayerManager().getPlayers().get(0).getPlane();
            canvas.drawBitmap(mBackground, -p.getScreenX(), -p.getScreenY(), mPaint);
            drawPlane(canvas);
            mHasBeenDrawn = true;
        }
    }

    /**
     * Draws all planes onto canvas
     * @param canvas Canvas to draw onto
     */
    private void drawPlane(Canvas canvas){
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.GREEN);

        //TODO: Remove this after done debugging


        //Loop through all players to draw
        PlayerManager manager = mGameLoop.getCore().getPlayerManager();
        for(Player player : manager.getPlayers()){
            Plane plane = player.getPlane();

//            ArrayList<Point> hitboxPoints = plane.getHitboxPoints();
//            for(int i = 0; i < hitboxPoints.size(); i++){
//                Log.i("PLANE", "Hitbox: " + hitboxPoints.size());
//                mPaint.setColor(Color.RED);
//                mPaint.setStrokeWidth(10.0f);
//                try {
//
//                    if (hitboxPoints.size() == 4)
//                        canvas.drawLine(hitboxPoints.get(i).x, hitboxPoints.get(i).y, hitboxPoints.get(i + 1 >= hitboxPoints.size() ? 0 : i + 1).x, hitboxPoints.get(i + 1 >= hitboxPoints.size() ? 0 : i + 1).y, mPaint);
//                }
//                catch(Exception e)
//                {
//
//                }
//            }

            // If the player is local, use relative coordinates. If not use real coordinates
            float planeX = plane.isLocal() || plane.inDisplayMode() ? plane.getX() : plane.getRealX() - manager.getLocalPlayer().getPlane().getScreenX();
            float planeY = plane.isLocal() || plane.inDisplayMode() ? plane.getY() : plane.getRealY() - manager.getLocalPlayer().getPlane().getScreenY();


            // Rotate plane
            Point centerOfRotation = new Point((int)(planeX + plane.getPlaneSprite().getWidth() / 2), (int)(planeY + plane.getPlaneSprite().getHeight() / 2));
            canvas.save();
            canvas.rotate(plane.getHeading() + 180, centerOfRotation.x, centerOfRotation.y);
            canvas.drawBitmap(plane.getPlaneSprite(), planeX, planeY, mPaint);
            canvas.restore();

            // Draw tail
            mPaint.setColor(plane.getTail().getTailColor());

            for(TailDataPoint data : plane.getTail().getTailData()){
                // Draw all tail data points
                mPaint.setStrokeWidth(plane.getTail().getTailWidth());
                if(data.getCurveType() == TailCurveType.STRAIGHT){
                    float startX = plane.isLocal() || plane.inDisplayMode() ? data.getStartX() : data.getRealStartX() - manager.getLocalPlayer().getPlane().getScreenX();
                    float startY = plane.isLocal() || plane.inDisplayMode() ? data.getStartY() : data.getRealStartY() - manager.getLocalPlayer().getPlane().getScreenY();
                    float endX = plane.isLocal() || plane.inDisplayMode() ? data.getEndX() : data.getRealEndX() - manager.getLocalPlayer().getPlane().getScreenX();
                    float endY = plane.isLocal() || plane.inDisplayMode() ? data.getEndY() : data.getRealEndY() - manager.getLocalPlayer().getPlane().getScreenY();
                    canvas.drawLine(startX, startY, endX, endY, mPaint);
                }else if(data.getCurveType() == TailCurveType.CURVED){
                    //canvas.save();
                    //canvas.rotate(data.getStartHeading() - 90, data.getBounds().centerX(), data.getBounds().centerY());
                    //mPaint.setColor(Color.BLACK);
                    //canvas.drawRect(data.getBounds(), mPaint);
                    //mPaint.setColor(Color.RED);
                    //canvas.drawArc(data.getBounds(), data.getStartAngle(), data.getSweepAngle(), false, mPaint);
                    //canvas.restore();
                }
            }
        }
    }

    @Override
    public void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mPaint.setColor(Color.GREEN);
        mInitialized = true;
    }

    @Override
    public void update() {
        mActivity.runOnUiThread(mInvaidate);
    }

    @Override
    public void stop() {

    }

    @Override
    public String getName() {
        return GameComponents.GRAPHICS.getName();
    }

    public boolean hasBeenDrawn() {
        return mHasBeenDrawn;
    }
}

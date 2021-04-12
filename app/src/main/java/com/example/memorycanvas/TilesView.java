package com.example.user.memorine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;

class Card {
    Paint p = new Paint();

    public Card(float x, float y, float width, float height, int color) {
        this.color = color;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int color, backColor = Color.DKGRAY;
    boolean isOpen = false; 
    float x, y, width, height;
    public void draw(Canvas c) {
        if (isOpen) {
            p.setColor(color);
        } else p.setColor(backColor);
        c.drawRect(x,y, width, height, p);
    }

    public boolean flip (float touch_x, float touch_y) {
        if (touch_x >= x && touch_x <= width && touch_y >= y && touch_y <= height) {
            isOpen = ! isOpen;
            return true;
        } else return false;
    }

}

public class TilesView extends View {
    final int PAUSE_LENGTH = 2; 
    boolean isOnPauseNow = false;
    boolean game;

    int openedCard = 0;
    MainActivity activity;

    ArrayList<Card> cards = new ArrayList<>();
    ArrayList<Integer> colors;

    int width, height;

    public TilesView(Context context) {
        super(context);
    }

    public TilesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        game = false;
        activity = (MainActivity) context;
        Integer[] test = new Integer[] {Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.GREEN, Color.BLUE,
                Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.GREEN, Color.BLUE,
                Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.GREEN, Color.BLUE,
                Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.GREEN, Color.BLUE};
        colors = new ArrayList<Integer>(Arrays.asList(test));
    }

    protected void createField() {
        float widthCard = (width - (4 * 30)) / 4;
        float heightCard = (height - (5 * 30)) / 5;
        float start_x = 15, start_y = 15, end_x = start_x + widthCard, end_y = start_y + heightCard;

        ArrayList<Integer> indexs = new ArrayList<>();
        for (int i = 0;i < 20;i++) {
            int c = (int)(Math.random() * colors.size());
            cards.add(new Card(start_x, start_y, end_x, end_y, colors.get(c)));
            colors.remove(c);
            if (end_x + 30 < width) {
                start_x = end_x + 30;
                end_x = start_x + widthCard;
            }
            else {
                start_x = 15;
                end_x = start_x + widthCard;
                start_y = end_y + 30;
                end_y = start_y + heightCard;
            }
            indexs.add(i);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!game) {
            width = canvas.getWidth();
            height = canvas.getHeight();
            createField();
            game = true;
            invalidate();
        }
        else {
            for (Card c: cards) {
                c.draw(canvas);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN && !isOnPauseNow) {
            for (Card c: cards) {

                if (openedCard == 0) {
                    if (c.flip(x, y)) {
                        openedCard ++;
                        invalidate();
                        return true;
                    }
                }

                if (openedCard == 1) {
                    if (c.flip(x, y)) {
                        openedCard ++;
                        invalidate();
                        PauseTask task = new PauseTask();
                        task.execute(PAUSE_LENGTH);
                        isOnPauseNow = true;

                        if (checkOpenCardsEqual(c)) {
                            if (cards.size() == 0) {
                                Toast toast = Toast.makeText(activity, "YOU WIN", Toast.LENGTH_LONG);
                                toast.show();
                                return true;
                            }
                        }

                        return true;
                    }
                }

            }
        }
        return true;
    }

    public boolean checkOpenCardsEqual(Card card) {
        for (Card c: cards) {
            if (c.isOpen && (card.x != c.x || card.y != c.y)) {
                if (card.color == c.color) {
                    cards.remove(c);
                    cards.remove(card);
                    return true;
                }
            }
        }

        return false;
    }

    class PauseTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... integers) {
            try {
                Thread.sleep(integers[0] * 1000);
            } catch (InterruptedException e) {}
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            for (Card c: cards) {
                if (c.isOpen) {
                    c.isOpen = false;
                }
            }
            openedCard = 0;
            isOnPauseNow = false;
            invalidate();
        }
    }
}

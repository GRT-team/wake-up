
package com.grt_team.wakeup.entity.puzzle.card;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.grt_team.wakeup.R;
import com.grt_team.wakeup.entity.puzzle.PuzzleView;

public class CardView extends PuzzleView {
    /**
     * Five card to collect in poker mode.
     */
    private static final int POKER_TASK_COUNT = 5;

    public static final int POKER_COMB_HIGH_CARD = 0;
    public static final int POKER_COMB_ONE_PAIR = 1;
    public static final int POKER_COMB_TWO_PAIR = 2;
    public static final int POKER_COMB_THREE_OF_A_KIND = 3;
    public static final int POKER_COMB_STRAIGHT = 4;
    public static final int POKER_COMB_FLUSH = 5;
    public static final int POKER_COMB_FULL_HOUSE = 6;
    public static final int POKER_COMB_FOUR_OF_A_KIND = 7;
    public static final int POKER_COMB_STRAIGHT_FLUSH = 8;
    public static final int POKER_COMB_ROYAL_FLUSH = 9;
    public static final int POKER_COMB_COUNT = 10;

    private static final int PLACED_EMPTY = 0;
    private static final int PLACED_CORRECT = 1;
    private static final int PLACED_WRONG = -1;

    private static final int CARD_NOT_PLACED = -1;

    private int cardsOnLine = 6;
    private boolean shuffleDeck = true;
    private boolean pokerMode = false;
    private final double cardProportion = 1.4;

    private TextView task;

    private Card selectedItem;
    private int freeColor, correctColor, wrongColor;
    private Stack<Card> cards = new Stack<Card>();
    private List<Card> cardsToMove = Collections.synchronizedList(new ArrayList<Card>());
    private List<Place> places = new ArrayList<Place>();
    private int cardTask[];
    private int pokerTask;
    private Canvas canvasScene;
    private Bitmap sceneBitmap;
    private Bitmap cardIcon[];
    private BitmapDrawable background;
    private Paint placePaint;

    private int selectedCardOffsetX, selectedCardOffsetY;
    private int width, height;
    private int taskIconSize;
    private float taskCardProportion = .8f;
    private int cardWidth, cardHeight;
    private int cardSpace;
    private int cardOffsetX, cardOffsetY;
    private int delta = 90; // %
    private int taskCount = 10;
    private float placeCorner;

    private static final int CARD_RES_INDEX = 0;
    private static final int CARD_VALUE_INDEX = 1;
    private static final int CARD_SUIT_INDEX = 2;

    private Integer[][] cardsDeck;

    private OnCardCompletedListener listener;

    public interface OnCardCompletedListener {
        public void onCardCompleted();
    }

    public CardView(Context context) {
        super(context);
        cardSpace = getResources().getDimensionPixelSize(R.dimen.puzzle_card_space);
        placeCorner = getResources().getDimensionPixelSize(R.dimen.puzzle_card_place_corner);
        background = (BitmapDrawable) getResources().getDrawable(R.drawable.card_background);
        background.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

    public void setCardsDeck(Integer[][] deck) {
        cardsDeck = deck;
        cardsOnLine = deck.length / 4;
    }

    public void setPokerMode(boolean pokerMode) {
        this.pokerMode = pokerMode;
    }

    public void setShuffleDeck(boolean shuffleDeck) {
        this.shuffleDeck = shuffleDeck;
    }

    /**
     * This method need to be called first
     */
    public void init(int width, int height, int taskIconSize) {
        this.width = width;
        this.height = height;
        this.taskIconSize = taskIconSize;
        cardHeight = calculateCardHeight(height, cardSpace, cardsDeck.length);
        cardWidth = calculateCardWidth(cardHeight);
        cardOffsetX = (width - (cardWidth * cardsOnLine + cardSpace * (cardsOnLine + 1))) / 2;
        cardOffsetY = 0;

        // if card width are too big then recalculate it
        if (cardWidth * cardsOnLine + cardSpace * (cardsOnLine + 1) >= width) {
            cardWidth = calculateCardWidth(width, cardSpace, cardsDeck.length);
            cardHeight = calculateCardHeight(cardWidth);
            cardOffsetX = 0;
            int rows = (int) (Math.ceil(cardsDeck.length / (float) cardsOnLine) + 1);
            cardOffsetY = (height - (cardHeight * rows + cardSpace * (rows + 1))) / 2;
        }

        freeColor = Color.rgb(0xcc, 0xcc, 0xcc); // silver
        correctColor = Color.rgb(110, 247, 61); // light green
        wrongColor = Color.rgb(208, 89, 70); // light red

        int placeWidth = getResources().getDimensionPixelSize(R.dimen.puzzle_card_place_width);
        placePaint = new Paint();
        placePaint.setAntiAlias(true);
        placePaint.setStrokeWidth(placeWidth < cardWidth / 5 ? placeWidth : cardWidth / 5);

        sceneBitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
        canvasScene = new Canvas(sceneBitmap);
    }

    private int calculateCardHeight(int totalHeight, int space, int cardNum) {
        int rows = (int) (Math.ceil(cardNum / (float) cardsOnLine) + 1);
        int totalSpace = (rows + 2) * space;
        int cardHeight = (totalHeight - totalSpace) / rows;
        return cardHeight;
    }

    private int calculateCardHeight(int cardWidth) {
        return (int) (cardWidth * cardProportion);
    }

    private int calculateCardWidth(int totalWidth, int space, int cardNum) {
        int totalSpace = (cardsOnLine + 1) * space;
        int cardWidth = (totalWidth - totalSpace) / cardsOnLine;
        return cardWidth;
    }

    private int calculateCardWidth(int cardHeight) {
        return (int) (cardHeight / cardProportion);
    }

    public void generatePuzzle() {
        int x = cardSpace + cardOffsetX;
        int y = cardSpace + cardOffsetY;
        int numOfPlaces = (pokerMode) ? POKER_TASK_COUNT : taskCount;

        List<Integer[]> cardsList = Arrays.asList(cardsDeck);
        if (shuffleDeck) {
            Collections.shuffle(cardsList);
        }

        for (int i = 0; i < cardsList.size(); i++) {
            Bitmap bitmap = Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(getResources(), cardsList.get(i)[CARD_RES_INDEX]),
                    cardWidth, cardHeight, true);
            Card card = new Card(bitmap, cardsList.get(i)[CARD_RES_INDEX], x, y, i,
                    cardsList.get(i)[CARD_SUIT_INDEX], cardsList.get(i)[CARD_VALUE_INDEX]);
            card.setX(x);
            card.setY(y);
            x += cardWidth + cardSpace;
            if ((i + 1) % cardsOnLine == 0) {
                x = cardSpace + cardOffsetX;
                y += cardHeight + cardSpace;
            }
            this.cards.add(card);
        }
        int x2, y2;
        for (int i = 0; i < numOfPlaces; i++) {
            // center of part of desk for plate
            x = width / numOfPlaces * places.size() + width / numOfPlaces / 2 - cardWidth / 2;
            y = height - cardHeight - 20; // center of desk
            x2 = x + cardWidth;
            y2 = y + cardHeight;
            RectF rect = new RectF(x, y, x2, y2);
            places.add(new Place(rect));
        }
        generateTask(cardsDeck, numOfPlaces);
        initSceneBitmap();
    }

    private void initSceneBitmap() {
        background.setBounds(canvasScene.getClipBounds());
        background.draw(canvasScene);

        synchronized (this) {
            Card card;
            for (int i = 0; i < cards.size(); i++) {
                card = cards.get(i);
                if (card.getPlace() == CARD_NOT_PLACED) {
                    sceneBitmapDrawCard(card);
                }
            }
        }
    }

    private void sceneBitmapRemove(Card card) {
        background.setBounds(card.x, card.y, card.x + cardWidth, card.y + cardHeight);
        background.draw(canvasScene);
    }

    private void sceneBitmapDrawCard(Card card) {
        canvasScene.drawBitmap(card.getBitmap(), card.getX(), card.getY(), null);
    }

    public void regenerateCardsBitmap() {
        Card card;
        for (int i = 0; i < cards.size(); i++) {
            card = cards.get(i);
            card.setBitmap(Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(getResources(), card.getBitmapResource()),
                    cardWidth,
                    cardHeight, true));
        }
        initSceneBitmap();
    }

    private void generateTask(Integer[][] cards, int numOfPlaces) {
        cardTask = new int[numOfPlaces];
        List<Integer> indexes = new ArrayList<Integer>();
        for (int i = 0; i < cards.length; i++) {
            indexes.add(i);
        }
        Collections.shuffle(indexes);
        Random r = new Random();
        for (int i = 0; i < numOfPlaces; i++) {
            int cardId = r.nextInt(indexes.size());
            cardTask[i] = indexes.remove(cardId);
        }

        cardIcon = new Bitmap[cardTask.length];
        for (int i = 0; i < cardTask.length; i++) {
            // generate card icons for task
            cardIcon[i] = scaleTaskCardBitmap(cards[cardTask[i]][CARD_RES_INDEX]);
        }

        pokerTask = r.nextInt(POKER_COMB_COUNT);
    }

    private Bitmap scaleTaskCardBitmap(int res) {
        Bitmap tmp = BitmapFactory.decodeResource(getResources(), res);
        Bitmap result = Bitmap.createScaledBitmap(tmp, (int) (taskIconSize * taskCardProportion),
                taskIconSize, true);
        tmp.recycle();
        return result;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        task = (TextView) ((LinearLayout) (getParent().getParent()))
                .findViewById(R.id.card_puzzle_task);
        updateTask();
    }

    private void updateTask() {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        if (pokerMode) {
            ssb.append(getPokerCombinationName(pokerTask));
        } else {
            for (int i = 0; i < cardTask.length; i++) {
                BitmapDrawable drawable = new BitmapDrawable(getResources(), cardIcon[i]);
                drawable.setBounds(0, 0, cardIcon[i].getWidth(), cardIcon[i].getHeight());
                if (places.get(i).getPlacedCard() == PLACED_CORRECT) {
                    drawable.setAlpha(50);
                }
                ImageSpan im = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
                ssb.append("  ");
                ssb.setSpan(im, ssb.length() - 2, ssb.length() - 1, 0);
            }
        }
        task.setText(ssb);
    }

    @Override
    public void doDraw(Canvas canvas) {
        Place place;
        canvas.drawBitmap(sceneBitmap, null, canvas.getClipBounds(), null);

        // Draw places
        for (int i = 0; i < places.size(); i++) {
            place = places.get(i);
            if (place.getPlacedCard() == PLACED_CORRECT) {
                placePaint.setColor(correctColor);
                placePaint.setStyle(Style.FILL_AND_STROKE);
            } else if (place.getPlacedCard() == PLACED_EMPTY) {
                placePaint.setColor(freeColor);
                placePaint.setStyle(Style.STROKE);
            } else {
                placePaint.setColor(wrongColor);
                placePaint.setStyle(Style.FILL_AND_STROKE);
            }
            canvas.drawRoundRect(place.getRect(), placeCorner, placeCorner,
                    placePaint);
        }

        synchronized (this) {
            Card card;
            // Draw cards that is moving
            for (int i = 0; i < cardsToMove.size(); i++) {
                card = cardsToMove.get(i);
                canvas.drawBitmap(card.getBitmap(), card.getX(), card.getY(), null);
            }
            // Draw cards that is placed
            for (int i = 0; i < cards.size(); i++) {
                card = cards.get(i);
                if (card.place == CARD_NOT_PLACED) {
                    continue;
                }
                canvas.drawBitmap(card.getBitmap(), card.getX(), card.getY(), null);
            }
            // Draw card that is under finger
            if (selectedItem != null) {
                canvas.drawBitmap(selectedItem.getBitmap(), selectedItem.x,
                        selectedItem.y, null);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        if (pointerIndex == 0) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    if (null != selectedItem) {
                        checkCardPosition(selectedItem);
                        updateTask();
                    } else {
                        int lastIndex = cards.size() - 1;
                        if ((cards.size() > 0)
                                && (cards.get(lastIndex).getPlace() == CARD_NOT_PLACED)) {
                            checkCardPosition(cards.get(lastIndex));
                            updateTask();
                        }
                    }
                    selectedItem = null;
                    return false;
                case MotionEvent.ACTION_DOWN:
                    selectedItem = moveElementOnTop(event.getX(), event.getY());
                    cardsToMove.remove(selectedItem);
                    if (null != selectedItem) {
                        selectedCardOffsetX = (int) (event.getX() - selectedItem.getX());
                        selectedCardOffsetY = (int) (event.getY() - selectedItem.getY());
                    }
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (null != selectedItem) {
                        moveCard((int) event.getX(), (int) event.getY());
                    }
                    return true;
                default:
                    selectedItem = null;
                    return super.onTouchEvent(event);
            }
        }
        return true;
    }

    private Card moveElementOnTop(float x, float y) {
        synchronized (this) {
            Card item;
            for (int i = cards.size() - 1; i >= 0; i--) {
                item = cards.get(i);
                int x1 = item.getX();
                int y1 = item.getY();
                int x2 = x1 + item.getBitmap().getWidth();
                int y2 = y1 + item.getBitmap().getHeight();
                if ((x >= x1 && x <= x2) && (y >= y1 && y <= y2)) {
                    if (item.getPlace() != CARD_NOT_PLACED) {
                        places.get(item.getPlace()).setPlacedCard(PLACED_EMPTY);
                        if (pokerMode) {
                            updatePlacedCardsPokerCombination(pokerTask);
                        }
                        item.setPlace(CARD_NOT_PLACED);
                    }
                    cards.remove(i);
                    cards.push(item);
                    if ((item.x == item.originalX) && (item.y == item.originalY)) {
                        sceneBitmapRemove(item);
                    }
                    return item;
                }
            }
            return null;
        }
    }

    private void moveCard(int x, int y) {
        selectedItem.setX(x - selectedCardOffsetX);
        selectedItem.setY(y - selectedCardOffsetY);
    }

    private void checkCardPosition(Card card) {
        synchronized (this) {
            Place place;
            int x1 = card.getX();
            int y1 = card.getY();
            int deltaX = card.getBitmap().getWidth() * delta / 100;
            int deltaY = card.getBitmap().getHeight() * delta / 100;
            for (int i = 0; i < places.size(); i++) {
                place = places.get(i);
                if (place.getPlacedCard() != PLACED_EMPTY) {
                    continue;
                }
                int x2 = (int) places.get(i).getRect().left;
                int y2 = (int) places.get(i).getRect().top;
                if (Math.abs(x1 - x2) <= deltaX && Math.abs(y1 - y2) <= deltaY) {
                    card.setX(x2);
                    card.setY(y2);
                    card.setPlace(i);

                    if (pokerMode) {
                        place.setPlacedCard(PLACED_CORRECT);
                        place.setCard(card);
                        updatePlacedCardsPokerCombination(pokerTask);
                    } else {
                        place.setPlacedCard((cardTask[i] == card.getPosition()) ? PLACED_CORRECT
                                : PLACED_WRONG);
                        place.setCard(card);
                    }
                    checkCardCompleted();
                    return;
                }
            }
            cardsToMove.add(card);
        }
    }

    private void updatePlacedCardsPokerCombination(int combination) {
        int min = 0;
        int max = 0;
        boolean hasAce = false;

        SparseIntArray cardValueCount = new SparseIntArray();
        SparseIntArray cardSuitCount = new SparseIntArray();

        int placedCardCount = 0;

        // Collect information about all placed cards. The max, min card value.
        // Count card according their suit and value, and check if there is ace
        // in placed card. Ace is needed to check if there is low straight
        // starting from Ace.
        for (int i = 0; i < places.size(); i++) {
            Place place = places.get(i);
            Card card = place.getCard();

            if (place.getPlacedCard() == PLACED_EMPTY) {
                continue;
            }
            placedCardCount++;

            if (card.value == Card.ACE) {
                hasAce = true;
            } else {
                int value = card.value;
                if (min == 0 || value < min) {
                    min = value;
                }
                if (max == 0 || value > max) {
                    max = card.value;
                }
            }
            int valueCount = cardValueCount.get(card.value);
            cardValueCount.put(card.value, valueCount + 1);

            int suitCount = cardSuitCount.get(card.suit);
            cardSuitCount.put(card.suit, suitCount + 1);
        }

        // Check all poker combination current state or complete state.
        boolean allCorrect = true;
        boolean flush = cardSuitCount.size() == 1;

        // If different between max and min is less or equals 5 and
        // there is 5 different groups then it is straight. One
        // exceptional condition to check if there is low straight.
        boolean straight = (max - min < POKER_TASK_COUNT)
                && (cardValueCount.size() == placedCardCount);
        if (hasAce && straight) {
            straight = (Card.ACE - min < POKER_TASK_COUNT)
                    || (max - Card.ACE_INVERSE < POKER_TASK_COUNT);
        }

        switch (combination) {
            case POKER_COMB_HIGH_CARD:
                if (placedCardCount == POKER_TASK_COUNT) {
                    allCorrect = (cardValueCount.size() == placedCardCount) && (!flush)
                            && (!straight);
                } else {
                    allCorrect = (cardValueCount.size() == placedCardCount);
                }
                break;
            case POKER_COMB_ONE_PAIR:
                if (placedCardCount == POKER_TASK_COUNT) {
                    // One pair if there is 4 different cards values and there
                    // is no flush.
                    allCorrect = (cardValueCount.size() == 4) && (!flush);
                } else {
                    // If placed cards equals cards values count or values count
                    // - 1, then it is correct combination at the moment.
                    allCorrect = (placedCardCount == cardValueCount.size())
                            || (placedCardCount - 1 == cardValueCount.size());
                }
                break;
            case POKER_COMB_TWO_PAIR:
                if (placedCardCount == POKER_TASK_COUNT) {
                    // Two pair if there is 3 different cards values and there
                    // is at least one group with count 2
                    allCorrect = (cardValueCount.size() == 3)
                            && (!flush)
                            && ((cardValueCount.get(cardValueCount.keyAt(0)) == 2)
                                || (cardValueCount.get(cardValueCount.keyAt(1)) == 2));
                } else {
                    // If there are all groups has card value less then 2 it is
                    // correct combination at the moment.
                    for (int i = 0; i < cardValueCount.size(); i++) {
                        if (cardValueCount.get(cardValueCount.keyAt(i)) > 2) {
                            allCorrect = false;
                            break;
                        }
                    }
                    allCorrect &= cardValueCount.size() <= 3;
                }
                break;
            case POKER_COMB_THREE_OF_A_KIND:
                if (placedCardCount == POKER_TASK_COUNT) {
                    // If cards value count equals 3 and there is two groups
                    // with less then 2 cards in it.
                    allCorrect = (cardValueCount.size() == 3)
                            && (!flush)
                            && ((cardValueCount.get(cardValueCount.keyAt(0)) != 2)
                                && (cardValueCount.get(cardValueCount.keyAt(1)) != 2));
                } else {
                    // If there is two or more groups with cards count bigger
                    // then 1 it is wrong combination
                    int count = 0;
                    for (int i = 0; i < cardValueCount.size(); i++) {
                        if (cardValueCount.get(cardValueCount.keyAt(i)) > 1) {
                            count++;
                        }
                    }
                    allCorrect = (count < 2) && (cardValueCount.size() <= 3);
                }
                break;
            case POKER_COMB_STRAIGHT_FLUSH:
                // check if it flash then go to POKER_COMB_STRAIGHT
                allCorrect = (straight) && (flush);
                break;
            case POKER_COMB_STRAIGHT:
                if (placedCardCount == POKER_TASK_COUNT) {
                    allCorrect = (straight) && (!flush);
                } else {
                    allCorrect = straight;
                }
                break;
            case POKER_COMB_FLUSH:
                allCorrect = flush;
                break;
            case POKER_COMB_FULL_HOUSE:
                if (placedCardCount == POKER_TASK_COUNT) {
                    // If there is two groups and one of them is cound 3 then it
                    // is correct combination
                    allCorrect = (cardValueCount.size() == 2)
                            && ((cardValueCount.get(cardValueCount.keyAt(0)) == 3)
                                || (cardValueCount.get(cardValueCount.keyAt(1)) == 3));
                } else {
                    // If there is less then two groups this combination current
                    // state is correct.
                    allCorrect = cardValueCount.size() <= 2;
                }
                break;
            case POKER_COMB_FOUR_OF_A_KIND:
                if (placedCardCount == POKER_TASK_COUNT) {
                    // If there is two groups and one of them is count 4 then it
                    // is correct combination
                    allCorrect = (cardValueCount.size() == 2)
                            && ((cardValueCount.get(cardValueCount.keyAt(0)) == 4)
                                || (cardValueCount.get(cardValueCount.keyAt(1)) == 4));
                } else {
                    // If there is less then two groups and one of them with
                    // count 1 then it is correct current combination
                    allCorrect = (cardValueCount.size() <= 2);
                    if (cardValueCount.size() == 2) {
                        allCorrect &= ((cardValueCount.get(cardValueCount.keyAt(0)) == 1)
                                || (cardValueCount.get(cardValueCount.keyAt(1)) == 1));
                    }
                }
                break;
            case POKER_COMB_ROYAL_FLUSH:
                allCorrect = (straight) && (flush) && ((min >= 10) || (min == 0));
                break;
        }

        // Update color state of placed cards according poker combination. If
        // combination is correct at the moment then all cards will be
        // highlighted with correct color.
        for (int i = 0; i < places.size(); i++) {
            if (places.get(i).getPlacedCard() == PLACED_EMPTY) {
                continue;
            }
            places.get(i).setPlacedCard((!allCorrect ? PLACED_WRONG : PLACED_CORRECT));
        }
    }

    private void checkCardCompleted() {
        for (int i = 0; i < places.size(); i++) {
            if (places.get(i).getPlacedCard() != 1)
                return;
        }
        if (null != listener)
            listener.onCardCompleted();
    }

    public List<Place> getPlaces() {
        return places;
    }

    public void setPlaces(List<Place> places) {
        this.places.clear();
        this.places.addAll(places);
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards.clear();
        this.cards.addAll(cards);
    }

    public List<Card> getCardsToMove() {
        return cardsToMove;
    }

    public void setCardsToMove(List<Card> cardsToMove) {
        this.cardsToMove.clear();
        this.cardsToMove.addAll(cardsToMove);
    }

    public int[] getCardTask() {
        return cardTask;
    }

    public void setPokerTask(int pokerTask) {
        this.pokerTask = pokerTask;
    }

    public int getPokerTask() {
        return this.pokerTask;
    }

    public void setCardTask(int[] cardTask) {
        this.cardTask = cardTask.clone();

        cardIcon = new Bitmap[cardTask.length];
        for (int i = 0; i < cardTask.length; i++) {
            // generate card icons for task
            cardIcon[i] = scaleTaskCardBitmap(findCardByPosition(cardTask[i]).getBitmapResource());
        }
    }

    private Card findCardByPosition(int position) {
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).position == position) {
                return cards.get(i);
            }
        }
        return null;
    }

    public static class Card implements Parcelable {

        public static int SUIT_CLUBS = 1;// (♣)
        public static int SUIT_DIAMONDS = 2; // (♦)
        public static int SUIT_HEARTS = 3; // (♥)
        public static int SUIT_SPADES = 4; // (♠)

        public static int ACE_INVERSE = 1;
        public static int JACK = 11;
        public static int QUEEN = 12;
        public static int KING = 13;
        public static int ACE = 14;

        private int imgRes;
        private int suit;
        private int value;
        private Bitmap bitmap;
        /**
         * Index of card in shuffled list.
         */
        private int position;
        private int place;
        private int originalX;
        private int originalY;
        private int x;
        private int y;

        public Card(Bitmap bitmap, int imgRes, int x, int y, int position, int suit, int value) {
            this.bitmap = bitmap;
            this.imgRes = imgRes;
            this.originalX = x;
            this.originalY = y;
            this.position = position;
            this.suit = suit;
            this.value = value;
            place = CARD_NOT_PLACED;
        }

        private Card(Parcel in) {
            this.imgRes = in.readInt();
            this.position = in.readInt();
            this.place = in.readInt();
            this.originalX = in.readInt();
            this.originalY = in.readInt();
            this.x = in.readInt();
            this.y = in.readInt();
            this.suit = in.readInt();
            this.value = in.readInt();
        }

        public static final Parcelable.Creator<Card> CREATOR = new Parcelable.Creator<Card>() {
            public Card createFromParcel(Parcel in) {
                return new Card(in);
            }

            public Card[] newArray(int size) {
                return new Card[size];
            }
        };

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public int getBitmapResource() {
            return imgRes;
        }

        public int getPosition() {
            return position;
        }

        public void setPlace(int place) {
            this.place = place;
        }

        public int getPlace() {
            return place;
        }

        public int getOriginalX() {
            return originalX;
        }

        public int getOriginalY() {
            return originalY;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(imgRes);
            dest.writeInt(position);
            dest.writeInt(place);
            dest.writeInt(originalX);
            dest.writeInt(originalY);
            dest.writeInt(x);
            dest.writeInt(y);
            dest.writeInt(suit);
            dest.writeInt(value);
        }

        public boolean sameSuit(Card card) {
            return (suit == card.suit);
        }

        public boolean sameValue(Card card) {
            return (value == card.value);
        }
    }

    public static class Place implements Parcelable {
        private RectF rect;
        private int placedCard;
        private Card card;

        public Place(RectF rect) {
            this.rect = rect;
        }

        private Place(Parcel in) {
            this.rect = in.readParcelable(Bitmap.class.getClassLoader());
            this.placedCard = in.readInt();
            this.card = in.readParcelable(Card.class.getClassLoader());
        }

        public static final Parcelable.Creator<Place> CREATOR = new Parcelable.Creator<Place>() {
            public Place createFromParcel(Parcel in) {
                return new Place(in);
            }

            public Place[] newArray(int size) {
                return new Place[size];
            }
        };

        public void setCard(Card card) {
            this.card = card;
        }

        public void setPlacedCard(int placedCard) {
            if (placedCard == PLACED_EMPTY) {
                this.card = null;
            }
            this.placedCard = placedCard;
        }

        public int getPlacedCard() {
            return placedCard;
        }

        public Card getCard() {
            return card;
        }

        public void setRect(RectF rect) {
            this.rect = rect;
        }

        public RectF getRect() {
            return rect;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(rect, flags);
            dest.writeInt(placedCard);
            dest.writeParcelable(card, flags);
        }
    }

    public void setOnCardCompletedListener(OnCardCompletedListener listener) {
        this.listener = listener;
    }

    private int getY(int x, int x1, int y1, int x2, int y2) {
        int a = y1 - y2;
        int b = x2 - x1;
        int c = x1 * y2 - x2 * y1;
        int y = (-c - a * x) / b;
        return y;
    }

    private int getX(int y, int x1, int y1, int x2, int y2) {
        int a = y1 - y2;
        int b = x2 - x1;
        int c = x1 * y2 - x2 * y1;
        int x = (-b * y - c) / a;
        return x;
    }

    @Override
    protected boolean updatePuzzleState(long time) {
        if (time < 10) {
            return false;
        }
        List<Card> toRemove = new ArrayList<Card>();
        for (int i = 0; i < cardsToMove.size(); i++) {
            Card card = cardsToMove.get(i);
            if (card.getOriginalX() == card.getX() && card.getOriginalY() == card.getY()) {
                toRemove.add(card);
                sceneBitmapDrawCard(card);
                continue;
            } else {
                int step = 5;
                int distanceX = Math.abs(card.getX() - card.getOriginalX());
                int distanceY = Math.abs(card.getY() - card.getOriginalY());
                if (distanceX <= step && distanceY <= step) {
                    card.setX(card.getOriginalX());
                    card.setY(card.getOriginalY());
                } else {
                    if (distanceX > step) {
                        if (card.getOriginalX() > card.getX()) {
                            int newX = card.getX() + distanceX / step;
                            card.setY(getY(newX, card.getX(), card.getY(), card.getOriginalX(),
                                    card.getOriginalY()));
                            card.setX(newX);
                        } else if (card.getOriginalX() < card.getX()) {
                            int newX = card.getX() - distanceX / step;
                            card.setY(getY(newX, card.getX(), card.getY(), card.getOriginalX(),
                                    card.getOriginalY()));
                            card.setX(newX);
                        }
                    } else {
                        if (card.getOriginalY() > card.getY()) {
                            int newY = card.getY() + distanceY / step;
                            card.setX(getX(newY, card.getX(), card.getY(), card.getOriginalX(),
                                    card.getOriginalY()));
                            card.setY(newY);
                        } else if (card.getOriginalY() < card.getY()) {
                            int newY = card.getY() - distanceY / step;
                            card.setX(getX(newY, card.getX(), card.getY(), card.getOriginalX(),
                                    card.getOriginalY()));
                            card.setY(newY);
                        }
                    }
                }
            }
        }
        cardsToMove.removeAll(toRemove);
        return true;
    }

    private String getPokerCombinationName(int pokerCombination) {
        int resId = R.string.puzzle_card_comb_high_card;
        switch (pokerCombination) {
            case POKER_COMB_HIGH_CARD:
                resId = R.string.puzzle_card_comb_high_card;
                break;
            case POKER_COMB_ONE_PAIR:
                resId = R.string.puzzle_card_comb_one_pair;
                break;
            case POKER_COMB_TWO_PAIR:
                resId = R.string.puzzle_card_comb_two_pair;
                break;
            case POKER_COMB_THREE_OF_A_KIND:
                resId = R.string.puzzle_card_comb_three_of_a_kind;
                break;
            case POKER_COMB_STRAIGHT:
                resId = R.string.puzzle_card_comb_straight;
                break;
            case POKER_COMB_FLUSH:
                resId = R.string.puzzle_card_comb_flush;
                break;
            case POKER_COMB_FULL_HOUSE:
                resId = R.string.puzzle_card_comb_full_house;
                break;
            case POKER_COMB_FOUR_OF_A_KIND:
                resId = R.string.puzzle_card_comb_four_of_a_kind;
                break;
            case POKER_COMB_STRAIGHT_FLUSH:
                resId = R.string.puzzle_card_comb_straight_flush;
                break;
            case POKER_COMB_ROYAL_FLUSH:
                resId = R.string.puzzle_card_comb_royal_flush;
                break;
        }
        return getContext().getString(resId);
    }
}

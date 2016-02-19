
package com.grt_team.wakeup.entity.puzzle.card;

import java.util.ArrayList;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html.ImageGetter;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.grt_team.wakeup.R;
import com.grt_team.wakeup.SettingsActivity;
import com.grt_team.wakeup.entity.puzzle.Puzzle;
import com.grt_team.wakeup.entity.puzzle.card.CardView.Card;
import com.grt_team.wakeup.entity.puzzle.card.CardView.OnCardCompletedListener;
import com.grt_team.wakeup.entity.puzzle.card.CardView.Place;
import com.grt_team.wakeup.utils.DisplayHelper;

public class CardPuzzle extends Puzzle implements OnCardCompletedListener {

    private final static String CARD_CARDS = "CARD_CARDS";
    private final static String CARD_TO_MOVE = "CARD_TO_MOVE";
    private final static String CARD_PLACES = "CARD_PLACES";
    private final static String CARD_TASK = "CARD_TASK";
    private final static String CARD_POKER_TASK = "CARD_POKER_TASK";

    private final static String CARD_DECK_SIZE = "CARD_DECK_SIZE";
    private final static String CARD_SHUFFLE = "CARD_SHUFFLE";
    private final static String CARD_TASK_NUMBER = "CARD_TASK_NUMBER";
    private final static String CARD_POKER_MODE = "CARD_POKER_MODE";

    private CardView view;
    private int[] cardTask;
    private int pokerTask;
    private ArrayList<Card> cards = new ArrayList<Card>();
    private ArrayList<Card> cardsToMove = new ArrayList<Card>();
    private ArrayList<Place> places = new ArrayList<Place>();

    int deckSize;
    boolean shuffle;
    int taskNumber;
    boolean pokerMode;

    private static int CARD_DECK_MIN_SIZE = 24;
    private static int CARD_DECK_MAX_SIZE = 52;

    private static final Integer[][] CARDS_RES = {
            { R.drawable.card_hearts_ace, Card.ACE, Card.SUIT_HEARTS },
            { R.drawable.card_hearts_king, Card.KING, Card.SUIT_HEARTS },
            { R.drawable.card_hearts_queen, Card.QUEEN, Card.SUIT_HEARTS },
            { R.drawable.card_hearts_jack, Card.JACK, Card.SUIT_HEARTS },
            { R.drawable.card_hearts_10, 10, Card.SUIT_HEARTS },
            { R.drawable.card_hearts_9, 9, Card.SUIT_HEARTS },
            { R.drawable.card_hearts_8, 8, Card.SUIT_HEARTS },
            { R.drawable.card_hearts_7, 7, Card.SUIT_HEARTS },
            { R.drawable.card_hearts_6, 6, Card.SUIT_HEARTS },
            { R.drawable.card_hearts_5, 5, Card.SUIT_HEARTS },
            { R.drawable.card_hearts_4, 4, Card.SUIT_HEARTS },
            { R.drawable.card_hearts_3, 3, Card.SUIT_HEARTS },
            { R.drawable.card_hearts_2, 2, Card.SUIT_HEARTS },
            
            { R.drawable.card_diamonds_ace, Card.ACE, Card.SUIT_DIAMONDS },
            { R.drawable.card_diamonds_king, Card.KING, Card.SUIT_DIAMONDS },
            { R.drawable.card_diamonds_queen, Card.QUEEN, Card.SUIT_DIAMONDS },
            { R.drawable.card_diamonds_jack, Card.JACK, Card.SUIT_DIAMONDS },
            { R.drawable.card_diamonds_10, 10, Card.SUIT_DIAMONDS },
            { R.drawable.card_diamonds_9, 9, Card.SUIT_DIAMONDS },
            { R.drawable.card_diamonds_8, 8, Card.SUIT_DIAMONDS },
            { R.drawable.card_diamonds_7, 7, Card.SUIT_DIAMONDS },
            { R.drawable.card_diamonds_6, 6, Card.SUIT_DIAMONDS },
            { R.drawable.card_diamonds_5, 5, Card.SUIT_DIAMONDS },
            { R.drawable.card_diamonds_4, 4, Card.SUIT_DIAMONDS },
            { R.drawable.card_diamonds_3, 3, Card.SUIT_DIAMONDS },
            { R.drawable.card_diamonds_2, 2, Card.SUIT_DIAMONDS },
            
            { R.drawable.card_clubs_ace, Card.ACE, Card.SUIT_CLUBS },
            { R.drawable.card_clubs_king, Card.KING, Card.SUIT_CLUBS },
            { R.drawable.card_clubs_queen, Card.QUEEN, Card.SUIT_CLUBS },
            { R.drawable.card_clubs_jack, Card.JACK, Card.SUIT_CLUBS },
            { R.drawable.card_clubs_10, 10, Card.SUIT_CLUBS },
            { R.drawable.card_clubs_9, 9, Card.SUIT_CLUBS },
            { R.drawable.card_clubs_8, 8, Card.SUIT_CLUBS },
            { R.drawable.card_clubs_7, 7, Card.SUIT_CLUBS },
            { R.drawable.card_clubs_6, 6, Card.SUIT_CLUBS },
            { R.drawable.card_clubs_5, 5, Card.SUIT_CLUBS },
            { R.drawable.card_clubs_4, 4, Card.SUIT_CLUBS },
            { R.drawable.card_clubs_3, 3, Card.SUIT_CLUBS },
            { R.drawable.card_clubs_2, 2, Card.SUIT_CLUBS },
            
            { R.drawable.card_spades_ace, Card.ACE, Card.SUIT_SPADES },
            { R.drawable.card_spades_king, Card.KING, Card.SUIT_SPADES },
            { R.drawable.card_spades_queen, Card.QUEEN, Card.SUIT_SPADES },
            { R.drawable.card_spades_jack, Card.JACK, Card.SUIT_SPADES },
            { R.drawable.card_spades_10, 10, Card.SUIT_SPADES },
            { R.drawable.card_spades_9, 9, Card.SUIT_SPADES },
            { R.drawable.card_spades_8, 8, Card.SUIT_SPADES },
            { R.drawable.card_spades_7, 7, Card.SUIT_SPADES },
            { R.drawable.card_spades_6, 6, Card.SUIT_SPADES },
            { R.drawable.card_spades_5, 5, Card.SUIT_SPADES },
            { R.drawable.card_spades_4, 4, Card.SUIT_SPADES },
            { R.drawable.card_spades_3, 3, Card.SUIT_SPADES },
            { R.drawable.card_spades_2, 2, Card.SUIT_SPADES }
    };

    public Integer[][] getDeck(int size) {
        if (size > CARD_DECK_MAX_SIZE) {
            size = CARD_DECK_MAX_SIZE;
        }
        if (size < CARD_DECK_MIN_SIZE) {
            size = CARD_DECK_MIN_SIZE;
        }

        int shift = CARDS_RES.length / 4;
        int part = size / 4;
        Integer[][] result = new Integer[size][3];

        for (int i = 0; i < part; i++) {

            result[i] = CARDS_RES[i];
            result[part + i] = CARDS_RES[shift + i];
            result[2 * part + i] = CARDS_RES[2 * shift + i];

            if (3 * part + i < size) {
                result[3 * part + i] = CARDS_RES[3 * shift + i];
            }
        }

        return result;
    }

    public CardPuzzle() {
        this.setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public void initDefaults(Context context) {
        super.initDefaults(context);

        deckSize = Integer.valueOf(SettingsActivity.getPref(getContext()).getString(
                SettingsActivity.PREF_PUZZLE.CARDS_DECK_SIZE, "24"));
        shuffle = SettingsActivity.getPref(getContext()).getBoolean(
                SettingsActivity.PREF_PUZZLE.CARDS_SHUFFLE, true);
        taskNumber = SettingsActivity.getPref(getContext()).getInt(
                SettingsActivity.PREF_PUZZLE.CARDS_TASK_NUMBER, 3);
        pokerMode = SettingsActivity.getPref(getContext()).getBoolean(
                SettingsActivity.PREF_PUZZLE.CARDS_USE_POKER_COMB, false);
    }

    @Override
    public View onPuzzleRun(DisplayMetrics metrics) {

        View scene = LayoutInflater.from(getContext()).inflate(R.layout.puzzle_card_layout, null);
        FrameLayout card_place = (FrameLayout) scene.findViewById(R.id.card_pazle_field);

        int card_title_height = (int) getContext().getResources().getDimension(
                R.dimen.puzzle_title_height);
        int card_place_height = DisplayHelper.getScreenHeight(getContext()) - card_title_height;

        view = new CardView(getContext());

        // Load user settings
        view.setCardsDeck(getDeck(deckSize));
        view.setShuffleDeck(shuffle);
        view.setTaskCount(taskNumber);
        view.setPokerMode(pokerMode);

        view.init(DisplayHelper.getScreenWidth(getContext()), card_place_height, (int) (card_title_height * 0.8));
        view.setOnUserActionPerformedListener(this);
        view.setOnCardCompletedListener(this);
        if (cards.size() != 0) {
            view.setCards(cards);
            view.regenerateCardsBitmap();
            view.setCardsToMove(cardsToMove);
            view.setPlaces(places);
            view.setCardTask(cardTask);
            view.setPokerTask(pokerTask);
        } else {
            view.generatePuzzle();
        }

        card_place.addView(view);
        return scene;
    }

    @Override
    public void onSave(Bundle savedInstanceState) {
        if (view == null) {
            return;
        }
        cards.clear();
        cardsToMove.clear();
        places.clear();
        cards.addAll(view.getCards());
        cardsToMove.addAll(view.getCardsToMove());
        places.addAll(view.getPlaces());
        cardTask = view.getCardTask().clone();
        pokerTask = view.getPokerTask();
        savedInstanceState.putParcelableArrayList(CARD_CARDS, cards);
        savedInstanceState.putParcelableArrayList(CARD_TO_MOVE, cardsToMove);
        savedInstanceState.putParcelableArrayList(CARD_PLACES, places);
        savedInstanceState.putInt(CARD_POKER_TASK, pokerTask);
        savedInstanceState.putIntArray(CARD_TASK, cardTask);

        savedInstanceState.putInt(CARD_DECK_SIZE, deckSize);
        savedInstanceState.putBoolean(CARD_SHUFFLE, shuffle);
        savedInstanceState.putInt(CARD_TASK_NUMBER, taskNumber);
        savedInstanceState.putBoolean(CARD_POKER_MODE, pokerMode);
    }

    @Override
    public void onRestore(Bundle savedInstanceState) {
        if (null != savedInstanceState) {
            ArrayList<Card> cards = savedInstanceState.getParcelableArrayList(CARD_CARDS);
            ArrayList<Card> cardsToMove = savedInstanceState.getParcelableArrayList(CARD_TO_MOVE);
            ArrayList<Place> places = savedInstanceState.getParcelableArrayList(CARD_PLACES);
            if (cards == null || cardsToMove == null || places == null) {
                return;
            }
            this.cards.clear();
            this.cardsToMove.clear();
            this.places.clear();
            this.cards.addAll(cards);
            this.cardsToMove.addAll(cardsToMove);
            this.places.addAll(places);
            this.cardTask = savedInstanceState.getIntArray(CARD_TASK);
            this.pokerTask = savedInstanceState.getInt(CARD_POKER_TASK);

            this.deckSize = savedInstanceState.getInt(CARD_DECK_SIZE);
            this.shuffle = savedInstanceState.getBoolean(CARD_SHUFFLE);
            this.taskNumber = savedInstanceState.getInt(CARD_TASK_NUMBER);
            this.pokerMode = savedInstanceState.getBoolean(CARD_POKER_MODE);
        }
    }

    @Override
    public void onCardCompleted() {
        endPuzzle();
    }

    @Override
    public int getPuzzleResTitle() {
        return R.string.puzzle_card_title;
    }

    @Override
    public int getPuzzleResBigDescription() {
        return R.string.puzzle_card_aim;
    }

    @Override
    public ImageGetter getPuzzleDescriptionGetter(Context context) {
        return new CardsResouceGetter(context);
    }

    private static class CardsResouceGetter extends Puzzle.SimpleResourceGetter {
        private static final String CARD_PREFIX = "card_";

        public CardsResouceGetter(Context context) {
            super(context);
        }

        @Override
        public Drawable getDrawable(String source) {
            TypedValue value = new TypedValue();
            DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
            getContext().getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, value,
                    true);
            int heightPixel = TypedValue.complexToDimensionPixelSize(value.data, metrics);

            if (source != null && source.startsWith(CARD_PREFIX)) {
                Drawable drawable = getContext().getResources().getDrawable(getResourceId(source));
                float scaleRate = (float) drawable.getIntrinsicWidth()
                        / drawable.getIntrinsicHeight();
                drawable.setBounds(0, 0, (int) (heightPixel * scaleRate), heightPixel);
                return drawable;
            }
            return super.getDrawable(source);
        }
    }

}

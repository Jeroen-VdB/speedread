package vdb.jeroen.speedread;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

public class MainActivity extends AppCompatActivity {

    private TextView txtWord;
    private ClipboardManager clipboard;
    private String fullText;
    private String[] words;
    private int position;
    private Handler m_handler;
    private Runnable m_handlerTask ;
    private EditText txtWaitTime;
    private boolean isReading;
    private TextView txtFullText;
    private ImageButton btnPlay;
    private ImageButton btnPause;
    private ImageButton btnStop;
    private final int BLUE_COLOR = Color.argb(255, 30, 126, 229);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Add layout and setup the variables
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        position = 0;
        isReading = false;

        txtWaitTime = (EditText) findViewById(R.id.txt_wait_time);
        txtWord = (TextView) findViewById(R.id.txt_word);
        txtFullText = (TextView) findViewById(R.id.txt_full_text);
        btnPlay = (ImageButton) findViewById(R.id.img_btn_play);
        btnPause = (ImageButton) findViewById(R.id.image_btn_pause);
        btnStop = (ImageButton) findViewById(R.id.image_btn_stop);

        //Clipboard manager to manage an action when the clipboard content has been changed.
        clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                txtFullText.setText(getClipboardText()); //keep function to get clipboard content!
                m_handler.removeCallbacks(m_handlerTask);
                isReading = false;
                position = 0;
                setWord();
            }
        });

        txtWaitTime.addTextChangedListener( new TextWatcher() {
            public void afterTextChanged(Editable s) {
                saveSpeed();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }}
        );

        txtFullText.setText(getClipboardText()); //keep function to get clipboard content!
        txtWord.setHint("Press play to read");

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String savedSpeed = sharedPref.getString("speed", "");

        if (!savedSpeed.equalsIgnoreCase("")){
            txtWaitTime.setText(savedSpeed);
        }

        //Runnable handler for delay between word display
        m_handler = new Handler();
        m_handlerTask = new Runnable() {
            @Override
            public void run() {
                if (words == null)
                    return;

                if (position < words.length){
                    colorStartBtn();
                    setWord();
                    position++;
                } else {
                    colorStopBtn();
                }

                m_handler.postDelayed(m_handlerTask, Integer.parseInt(txtWaitTime.getText().toString()));
            }
        };

        //TODO Add copy function for chrome
        Toast.makeText(this, "Copying from chrome is currently unavailable", Toast.LENGTH_SHORT).show();
    }

    /**
     * Start the runnable handler and set isReading status to 'reading'
     * @param v
     */
    public void play(View v) {
        if(words == null){
            txtWord.setHint("Copy your text first");
        } else if (!isReading) {
            m_handlerTask.run();
            isReading = true;
            colorStartBtn();
        }
    }

    /**
     * Remove the runnable handler's callback and set isReading status to 'not reading'
     * @param v
     * @throws InterruptedException
     */
    public void pause(View v) throws InterruptedException {
        if (words == null)
            return;

        if (position > 0 && isReading) {
            position--;
        }
        m_handler.removeCallbacks(m_handlerTask);
        isReading = false;
        colorPauseBtn();
    }

    /**
     * Remove the runnable handler's callback, reset the position and set isReading status to 'not reading'
     * @param v
     * @throws InterruptedException
     */
    public void stop(View v) throws InterruptedException {
        if (words == null)
            return;

        m_handler.removeCallbacks(m_handlerTask);
        isReading = false;
        position = 0;
        setWord();
        colorPauseBtn(); //not colorStopBtn because we're resetting the position
    }

    /**
     * Set position to position - 1 and update text
     * @param v
     */
    public void prevWord(View v){
        if (words == null)
            return;

        if (position > 0){
            position--;
            setWord();
        }
    }

    /**
     * Set position to posistion + 1 and update text
     * @param v
     */
    public void nextWord(View v){
        if (words != null && position < words.length - 1){
            position++;
            setWord();
        } else if (words != null){
            colorStopBtn();
        }
    }

    /**
     * Check the context of the clipboard. Incase it's plain text split the string and put it in the words array
     * @return String
     */
    public String getClipboardText(){
        if (!(clipboard.hasPrimaryClip())) {
            //Clipboard is empty
            return "Oops! Something went wrong while copying text";
        } else if (!(clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN))) {
            // clipboard doesn't contain plain text
            return "Oops! Something went wrong while copying text";
        } else {
            // Gets the clipboard as text.
            ClipData data = clipboard.getPrimaryClip();

            if (data.getItemCount() > 0)
            {
                ClipData.Item item = data.getItemAt(0);
                if (item != null)
                {
                    //String text = item.getText().toString();
                    String text = data.getItemAt(0).coerceToText(this).toString();
                    fullText = text;
                    words = text.split("\\s+");
                    return fullText;
                }
            }
            words = null;
            return "Oops! Something went wrong while copying text";
        }
    }

    /**
     * Update the current word showing on the screen including focus color
     */
    public void setWord(){
        Spannable word;

        if (words == null){
            return;
        }

        if (words[position].length() % 2 != 0){
            word = new SpannableString(words[position]);
        } else {
            word = new SpannableString(words[position] + " ");
        }

        if (word.length() > 2){
            word.setSpan(new ForegroundColorSpan(Color.parseColor("#2196F3")), word.length() / 2, word.length() / 2 + 1, 0);
        } else if (word.length() == 1){
            word.setSpan(new ForegroundColorSpan(Color.parseColor("#2196F3")), 0, 1, 0);
        }

        txtWord.setText(word, TextView.BufferType.SPANNABLE);
    }

    /**
     * Increase speed input by 100ms, get the value of the speed input, save it and replace the handler's delay
     * @param v
     */
    public void speedUp(View v){
        int more = Integer.parseInt(txtWaitTime.getText().toString()) + 100;
        txtWaitTime.setText(String.valueOf(more));

        m_handler.removeCallbacks(m_handlerTask);

        saveSpeed();

        if (isReading){
            m_handler.postDelayed(m_handlerTask, more);
        }
    }

    /**
     * Reduce speed input by 100ms, get the value of the speed input, save it and replace the handler's delay
     * @param v
     */
    public void speedDown(View v){
        if(Integer.parseInt(txtWaitTime.getText().toString()) <= 100)
            return;

        int less = Integer.parseInt(txtWaitTime.getText().toString()) - 100;

        txtWaitTime.setText(String.valueOf(less));

        m_handler.removeCallbacks(m_handlerTask);

        saveSpeed();

        if (isReading){
            m_handler.postDelayed(m_handlerTask, less);
        }
    }

    /**
     * Save the speed input as SharedPreference
     */
    public void saveSpeed(){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("speed", txtWaitTime.getText().toString());
        editor.apply();
    }

    /**
     * Color play button blue, clears pause and stop color
     */
    public void colorStartBtn(){
        btnPlay.setColorFilter(BLUE_COLOR);
        btnPause.clearColorFilter();
        btnStop.clearColorFilter();
    }

    /**
     * Color pause button blue, clears play and stop color
     */
    public void colorPauseBtn(){
        btnPause.setColorFilter(BLUE_COLOR);
        btnPlay.clearColorFilter();
        btnStop.clearColorFilter();
    }

    /**
     * Color stop button blue, clears pause and play color
     */
    public void colorStopBtn(){
        btnStop.setColorFilter(BLUE_COLOR);
        btnPlay.clearColorFilter();
        btnPause.clearColorFilter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item){
        switch(item.getItemId()){
            case R.id.action_settings:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

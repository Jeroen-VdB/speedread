package vdb.jeroen.speedread;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AboutActivity extends AppCompatActivity {

    EditText txtTitle;
    EditText txtDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        txtTitle = (EditText) findViewById(R.id.txt_title);
        txtDescription = (EditText) findViewById(R.id.txt_description);
    }

    /**
     *
     * @param v
     */
    public void sendReport(View v){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"jeroen.van.den.broeck@outlook.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, txtTitle.getText().toString());
        i.putExtra(Intent.EXTRA_TEXT   , txtDescription.getText().toString());
        try {
            startActivity(Intent.createChooser(i, "Select mail app:"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(AboutActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
}

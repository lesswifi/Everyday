package compsci290.edu.duke.myeveryday.Tag;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import compsci290.edu.duke.myeveryday.R;

/**
 * Created by wangerxiao on 4/23/17.
 */

public class TagList extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_activity);
        //Toolbar mtoolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(mtoolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        openFragment(new TagListFragment(), "Tags");


    }

    private void openFragment(Fragment fragment, String screenTitle){
        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.container, fragment)
                .addToBackStack(screenTitle)
                .commit();
        getSupportActionBar().setTitle(screenTitle);
    }

    @Override
    public void onBackPressed()
    {
        int count = getFragmentManager().getBackStackEntryCount();
        if(count == 0)
            super.onBackPressed();
        else
        {
            getFragmentManager().popBackStack();
        }
    }

}

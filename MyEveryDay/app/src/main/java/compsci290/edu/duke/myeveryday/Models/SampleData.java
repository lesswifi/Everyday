package compsci290.edu.duke.myeveryday.Models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import compsci290.edu.duke.myeveryday.util.Constants;

/**
 * Created by wangerxiao on 4/17/17.
 */

public class SampleData {

    public static List<String> getSampleCategories() {
        List<String> categoryNames = new ArrayList<>();

        categoryNames.add("Family");
        categoryNames.add("Food");
        categoryNames.add("Music");
        categoryNames.add("Dream");
        categoryNames.add("Sports");
        categoryNames.add("Travel");
        categoryNames.add("Productivity");
        categoryNames.add("Personal");
        categoryNames.add("Finance");
        categoryNames.add("Fitness");
        categoryNames.add("Blog Posts");
        categoryNames.add("Social Media");


        return categoryNames;

    }


    /*public static List<JournalEntry> getSampleNotes() {

        List<JournalEntry> notes = new ArrayList<>();
        //create the dummy note
        JournalEntry note1 = new JournalEntry();
        note1.setmTitle("DisneyLand Trip");
        note1.setmContent("We went to Disneyland today and the kids had lots of fun!");
        Calendar calendar1 = GregorianCalendar.getInstance();
        note1.setmDateModified(calendar1.getTimeInMillis());
        note1.setmJourneyType(Constants.NOTE_TYPE_AUDIO);
        notes.add(note1);


        //create the dummy note
        JournalEntry note2 = new JournalEntry();
        note2.setmTitle("Gym Work Out");
        note2.setmContent("I went to the Gym today and I got a lot of exercises");

        //change the date to random time
        Calendar calendar2 = GregorianCalendar.getInstance();
        calendar2.add(Calendar.DAY_OF_WEEK, -1);
        calendar2.add(Calendar.MILLISECOND, 10005623);
        note2.setmDateModified(calendar2.getTimeInMillis());
        note2.setmJourneyType(Constants.NOTE_TYPE_IMAGE);
        notes.add(note2);


        //create the dummy note
        JournalEntry note3 = new JournalEntry();
        note3.setmTitle("Blog Post Idea");
        note3.setmContent("I will like to write a blog post about how to make money online");





        //create the dummy note
        JournalEntry note4 = new JournalEntry();
        note4.setmTitle("Cupcake Recipe");
        note4.setmContent("Today I found a recipe to make cup cake from www.google.");

        //pad the date with random number of days and minute
        //so all the notes do not have the same time stamp
        Calendar calendar4 = GregorianCalendar.getInstance();
        calendar4.add(Calendar.DAY_OF_WEEK, -4);
        calendar4.add(Calendar.MILLISECOND, 49762311);
        note4.setmDateModified(calendar4.getTimeInMillis());
        note4.setmJourneyType(Constants.NOTE_TYPE_TEXT);
        notes.add(note4);


        //create the dummy note
        JournalEntry note5 = new JournalEntry();
        note5.setmTitle("Notes from Networking Event");
        note5.setmContent("Today I attended a developer's networking event and it was great");

        //pad the date with two days
        //pad the date with random number of days and minute
        //so all the notes do not have the same time stamp
        Calendar calendar5 = GregorianCalendar.getInstance();
        calendar4.add(Calendar.MONTH, -2);
        calendar5.add(Calendar.MILLISECOND, 2351689);
        note5.setmDateModified(calendar5.getTimeInMillis());
        note5.setmJourneyType(Constants.NOTE_TYPE_AUDIO);
        notes.add(note5);

        return notes;
    }*/
}


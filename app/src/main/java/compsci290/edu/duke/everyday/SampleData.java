package compsci290.edu.duke.everyday;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by FD on 4/4/2017.
 */

public class SampleData {

    public static List<JournalEntry> getSampleJournalEntries() {

        List<JournalEntry> journalEnrties = new ArrayList<>();
        //create the dummy journal
        JournalEntry journalEntry1 = new JournalEntry();
        journalEntry1.setTitle("Disney");
        journalEntry1.setContent("We went to Disneyland today and the kids had lots of fun!");
        Calendar calendar1 = GregorianCalendar.getInstance();
        journalEntry1.setDateModified(calendar1.getTimeInMillis());
        journalEnrties.add(journalEntry1);

        JournalEntry journalEntry2 = new JournalEntry();
        journalEntry2.setTitle("Universal Studio");
        journalEntry2.setContent("We went to hike!");
        Calendar calendar2 = GregorianCalendar.getInstance();
        journalEntry2.setDateModified(calendar2.getTimeInMillis());
        journalEnrties.add(journalEntry2);

        return journalEnrties;
    }

    public static List<String> getSampleTags(){

        List<String> names = new ArrayList<>(Arrays.asList("xyz", "abc"));
        return names;
    }

}

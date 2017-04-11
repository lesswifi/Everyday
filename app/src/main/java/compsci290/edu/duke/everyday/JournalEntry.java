package compsci290.edu.duke.everyday;

/**
 * Created by FD on 4/4/2017.
 */

public class JournalEntry {
    public String journalId;
    public String title;
    public String content;
    public long dateCreated;
    public long dateModified;
    public String tagName;


    public void setTitle(String t){
        title = t;
    }

    public void setContent(String c){
        content = c;
    }

    public void setDateModified(long t){
        dateModified = t;
    }

    public void setJournalId(String j){
        journalId = j;
    }

    public String getJournalId(){
        return journalId;
    }

    public String getTitle(){
        return title;
    }

    public long getDateModified(){
        return dateModified;
    }


}
